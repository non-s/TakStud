package com.example.takstud.offline

import kotlinx.coroutines.flow.Flow

/**
 * Interface para Monitoramento de Conectividade de Rede.
 *
 * Define contrato para monitorar estado de conexão de rede e notificar subscribers
 * sobre mudanças. Usado para sincronização automática quando volta online.
 *
 * Responsabilidades:
 * - Monitorar estado de conexão (online/offline)
 * - Detectar mudanças de rede
 * - Expor qualidade de conexão
 * - Notificar quando volta online
 *
 * Implementações:
 * - ConnectivityMonitorImpl: Implementação completa com monitoring ativo
 *
 * PADRÃO:
 * - Flows reativos para Composables
 * - Monitoramento de mudanças automático
 * - Suporte para diferentes tipos de rede (WiFi, Cellular, Ethernet)
 *
 * @see ConnectivityMonitorImpl
 * @see NetworkQuality
 * @see NetworkType
 */
interface ConnectivityMonitor {

    /**
     * Flow que emite estado de conectividade.
     * true = online, false = offline
     */
    val isOnline: Flow<Boolean>

    /**
     * Flow que emite mudanças no estado de conexão.
     * null = nenhuma mudança ainda, true = voltou online, false = desconectou
     */
    val connectionChanged: Flow<Boolean?>

    /**
     * Flow que emite qualidade da conexão atual.
     */
    val networkQuality: Flow<NetworkQuality>

    /**
     * Inicia monitoramento ativo de conectividade.
     * Deve ser chamado em onCreate() da Activity.
     */
    fun startMonitoring()

    /**
     * Para monitoramento ativo de conectividade.
     * Deve ser chamado em onDestroy() da Activity.
     */
    fun stopMonitoring()

    /**
     * Verifica estado atual da conexão de forma síncrona.
     *
     * @return true se conectado à internet, false caso contrário
     */
    fun checkInternetConnection(): Boolean

    /**
     * Aguarda até estar online (útil para sincronização).
     *
     * @param timeoutMs Timeout em milissegundos (default 30s)
     * @return true se ficou online, false se timeout
     */
    suspend fun waitUntilOnline(timeoutMs: Long): Boolean

    /**
     * Obtém tipo de rede atual (WiFi, Celular, Ethernet, Nenhuma).
     *
     * @return NetworkType enum indicando tipo de rede
     */
    fun getNetworkType(): NetworkType

    /**
     * Enumeração para qualidade de conexão.
     */
    enum class NetworkQuality {
        EXCELLENT,
        GOOD,
        MODERATE,
        POOR,
        UNKNOWN,
        OFFLINE
    }

    /**
     * Enumeração para tipos de rede.
     */
    enum class NetworkType {
        WIFI,
        CELLULAR,
        ETHERNET,
        BLUETOOTH,
        NONE,
        UNKNOWN
    }
}
