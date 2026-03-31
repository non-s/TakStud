package com.example.takstud.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList

/**
 * PerformanceMonitor - Monitoramento de performance da aplicação.
 *
 * FUNCIONALIDADES:
 * - Rastreamento de uso de memória
 * - Monitoramento de FPS
 * - Tracking de operações lentas
 * - Análise de tempo de execução
 * - Relatórios de performance
 * - Detecção de memory leaks
 * - Profiling de métodos
 *
 * MÉTRICAS:
 * - Heap memory: Memória usada pelo app
 * - Native memory: Memória nativa alocada
 * - FPS: Quadros por segundo
 * - Operation time: Tempo de execução
 * - Memory trend: Tendência de uso de memória
 *
 * EXEMPLO DE USO:
 * val monitor = PerformanceMonitor.getInstance(context)
 * monitor.trackOperation("database_query") {
 *     // código a ser medido
 * }
 * val report = monitor.getReport()
 */

/**
 * Dados de performance capturados.
 */
data class PerformanceMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val heapMemory: Long = 0,
    val nativeMemory: Long = 0,
    val fps: Float = 0f,
    val threadCount: Int = 0,
    val gcCount: Long = 0,
    val isMemoryLow: Boolean = false
)

/**
 * Registro de operação rastreada.
 */
data class OperationTrace(
    val name: String,
    val startTime: Long,
    val endTime: Long = 0,
    val duration: Long = 0,
    val isCompleted: Boolean = false
) {
    fun getDurationMs(): Long = if (isCompleted) duration else System.currentTimeMillis() - startTime
}

/**
 * Relatório de performance.
 */
data class PerformanceReport(
    val peakMemory: Long,
    val averageMemory: Long,
    val currentMemory: Long,
    val slowestOperation: OperationTrace?,
    val totalOperations: Int,
    val averageOperationTime: Long,
    val fpsMin: Float,
    val fpsMax: Float,
    val fpsAverage: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Monitor de performance singleton.
 */
class PerformanceMonitor private constructor(private val context: Context) {
    companion object {
        private var instance: PerformanceMonitor? = null

        fun getInstance(context: Context): PerformanceMonitor {
            return instance ?: PerformanceMonitor(context).also { instance = it }
        }

        private const val MAX_METRICS_HISTORY = 100
        private const val MAX_OPERATION_HISTORY = 50
    }

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val runtime = Runtime.getRuntime()

    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()

    private val metricsHistory = LinkedList<PerformanceMetrics>()
    private val operationTraces = LinkedList<OperationTrace>()
    private val fpsSamples = mutableListOf<Float>()

    private var lastGcCount = 0L
    private var isMonitoring = false

    /**
     * Inicia monitoramento contínuo.
     */
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        captureMetrics()
    }

    /**
     * Para monitoramento.
     */
    fun stopMonitoring() {
        isMonitoring = false
    }

    /**
     * Captura métrica atual.
     */
    fun captureMetrics() {
        val heapMemory = getHeapMemory()
        val nativeMemory = getNativeMemory()
        val metrics = PerformanceMetrics(
            heapMemory = heapMemory,
            nativeMemory = nativeMemory,
            threadCount = Thread.activeCount(),
            gcCount = Runtime.getRuntime().totalMemory(),
            isMemoryLow = isMemoryLow()
        )

        _metrics.value = metrics

        // Mantém histórico
        metricsHistory.add(metrics)
        if (metricsHistory.size > MAX_METRICS_HISTORY) {
            metricsHistory.removeFirst()
        }
    }

    /**
     * Rastreia operação e mede tempo.
     */
    fun <T> trackOperation(name: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        val trace = OperationTrace(name, startTime)
        operationTraces.add(trace)

        return try {
            block().also {
                val endTime = System.currentTimeMillis()
                val completed = trace.copy(
                    endTime = endTime,
                    duration = endTime - startTime,
                    isCompleted = true
                )
                operationTraces[operationTraces.size - 1] = completed
            }
        } catch (e: Exception) {
            if (operationTraces.size > MAX_OPERATION_HISTORY) {
                operationTraces.removeFirst()
            }
            throw e
        }
    }

    /**
     * Rastreia suspensão.
     */
    suspend fun <T> trackSuspendOperation(name: String, block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        return try {
            block().also {
                val duration = System.currentTimeMillis() - startTime
                recordOperationTime(name, duration)
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            recordOperationTime(name, duration, isError = true)
            throw e
        }
    }

    /**
     * Registra amostra de FPS.
     */
    fun recordFps(fps: Float) {
        fpsSamples.add(fps)
        if (fpsSamples.size > 60) { // Mantém último segundo (60fps)
            fpsSamples.removeAt(0)
        }
    }

    /**
     * Obtém relatório de performance.
     */
    fun getReport(): PerformanceReport {
        captureMetrics()

        val peakMemory = metricsHistory.maxOfOrNull { it.heapMemory } ?: 0L
        val averageMemory = if (metricsHistory.isNotEmpty()) {
            metricsHistory.map { it.heapMemory }.average().toLong()
        } else 0L

        val slowestOperation = operationTraces
            .filter { it.isCompleted }
            .maxByOrNull { it.duration }

        val averageOperationTime = if (operationTraces.isNotEmpty()) {
            operationTraces
                .filter { it.isCompleted }
                .map { it.duration }
                .average()
                .toLong()
        } else 0L

        val fpsMin = fpsSamples.minOrNull() ?: 0f
        val fpsMax = fpsSamples.maxOrNull() ?: 0f
        val fpsAverage = fpsSamples.average().toFloat()

        return PerformanceReport(
            peakMemory = peakMemory,
            averageMemory = averageMemory,
            currentMemory = getHeapMemory(),
            slowestOperation = slowestOperation,
            totalOperations = operationTraces.size,
            averageOperationTime = averageOperationTime,
            fpsMin = fpsMin,
            fpsMax = fpsMax,
            fpsAverage = fpsAverage
        )
    }

    /**
     * Limpa histórico de rastreamento.
     */
    fun clearHistory() {
        metricsHistory.clear()
        operationTraces.clear()
        fpsSamples.clear()
    }

    /**
     * Obtém memória do heap em uso.
     */
    private fun getHeapMemory(): Long {
        return runtime.totalMemory() - runtime.freeMemory()
    }

    /**
     * Obtém memória nativa alocada.
     */
    private fun getNativeMemory(): Long {
        return runtime.totalMemory() / 4 // Estimativa: ~25% de memória nativa
    }

    /**
     * Verifica se memória está baixa.
     */
    private fun isMemoryLow(): Boolean {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.lowMemory
    }

    /**
     * Registra tempo de operação.
     */
    private fun recordOperationTime(name: String, duration: Long, isError: Boolean = false) {
        val trace = OperationTrace(
            name = name,
            startTime = System.currentTimeMillis() - duration,
            endTime = System.currentTimeMillis(),
            duration = duration,
            isCompleted = !isError
        )
        operationTraces.add(trace)
        if (operationTraces.size > MAX_OPERATION_HISTORY) {
            operationTraces.removeFirst()
        }
    }
}

/**
 * Analisador de performance.
 */
object PerformanceAnalyzer {
    /**
     * Identifica gargalos de performance.
     */
    fun analyzeBottlenecks(traces: List<OperationTrace>): List<String> {
        val bottlenecks = mutableListOf<String>()

        val slowThreshold = traces
            .filter { it.isCompleted }
            .map { it.duration }
            .average() * 1.5

        traces
            .filter { it.isCompleted && it.duration > slowThreshold }
            .forEach {
                bottlenecks.add("${it.name}: ${it.duration}ms")
            }

        return bottlenecks
    }

    /**
     * Calcula tendência de memória.
     */
    fun analyzeMemoryTrend(metrics: List<PerformanceMetrics>): String {
        if (metrics.size < 2) return "Insuficiente"

        val recent = metrics.takeLast(10)
        val oldAvg = metrics.take(metrics.size / 2).map { it.heapMemory }.average()
        val newAvg = recent.map { it.heapMemory }.average()

        return when {
            newAvg > oldAvg * 1.1 -> "Aumentando ⬆️"
            newAvg < oldAvg * 0.9 -> "Diminuindo ⬇️"
            else -> "Estável ➡️"
        }
    }

    /**
     * Detecta possíveis memory leaks.
     */
    fun detectMemoryLeaks(metrics: List<PerformanceMetrics>): Boolean {
        if (metrics.size < 20) return false

        val recent = metrics.takeLast(20)
        val firstHalf = recent.take(10).map { it.heapMemory }.average()
        val secondHalf = recent.takeLast(10).map { it.heapMemory }.average()

        return secondHalf > firstHalf * 1.2
    }

    /**
     * Gera relatório legível.
     */
    fun generateReport(report: PerformanceReport): String {
        return """
            === Performance Report ===
            Memória:
              - Pico: ${OptimizationUtils.formatBytes(report.peakMemory)}
              - Média: ${OptimizationUtils.formatBytes(report.averageMemory)}
              - Atual: ${OptimizationUtils.formatBytes(report.currentMemory)}

            Operações:
              - Total: ${report.totalOperations}
              - Tempo médio: ${report.averageOperationTime}ms
              - Mais lenta: ${report.slowestOperation?.name} (${report.slowestOperation?.duration}ms)

            FPS:
              - Mínimo: ${report.fpsMin}
              - Máximo: ${report.fpsMax}
              - Média: ${report.fpsAverage}
        """.trimIndent()
    }
}
