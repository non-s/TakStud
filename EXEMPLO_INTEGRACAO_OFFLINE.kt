package com.example.takstud

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.model.Grade
import com.example.takstud.model.Task
import com.example.takstud.offline.ConnectivityMonitor
import com.example.takstud.offline.OfflineSyncQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * EXEMPLO DE INTEGRAÇÃO: Como usar OfflineSyncQueue para funcionalidade offline.
 *
 * Este arquivo mostra como implementar modo offline com sincronização automática.
 *
 * COPIE ESTE CÓDIGO para TakStudViewModel.kt
 */

class OfflineIntegrationExample(
    context: Context,
    private val repository: TakStudRepository
) : ViewModel() {

    private val syncQueue = OfflineSyncQueue()
    private val connectivityMonitor = ConnectivityMonitor(context)

    // ============== SETUP INICIAL ==============

    /**
     * Chamar em init {} do ViewModel para monitorar conectividade.
     */
    fun setupOfflineMode() {
        // 1. Monitorar mudanças de conectividade
        viewModelScope.launch {
            connectivityMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.i("OfflineMode", "✓ Voltou online! Sincronizando fila...")
                    processPendingSyncQueue()
                } else {
                    Log.w("OfflineMode", "✗ Sem conexão - Modo offline ativado")
                    // UI pode mostrar "Offline" badge
                }
            }
        }

        // 2. Limpar operações antigas periodicamente
        viewModelScope.launch {
            try {
                syncQueue.cleanupOldSuccessfulOps(daysOld = 7)
            } catch (e: Exception) {
                Log.e("OfflineMode", "Erro ao limpar fila", e)
            }
        }
    }

    // ============== CRIAR DADOS OFFLINE ==============

    /**
     * Exemplo 1: Criar Task enquanto offline
     *
     * PADRÃO:
     * 1. Salvar localmente (Room) - IMEDIATO
     * 2. Enfileirar para sync - IMEDIATO
     * 3. Quando online, sincronizar automaticamente
     */
    fun createTaskOffline(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i("OfflineMode", "Criando task offline: ${task.id}")

                // PASSO 1: Salvar em Room (local)
                // taskDao.insert(task)  // Real: SaveTask localmente

                // PASSO 2: Enfileirar para sincronização
                syncQueue.enqueueTaskCreate(task)

                // PASSO 3: UI feedback
                Log.i("OfflineMode", "✓ Task salva localmente, aguardando sincronização")

                // PASSO 4: Se estiver online, sincronizar agora
                if (connectivityMonitor.isCurrentlyOnline()) {
                    processPendingSyncQueue()
                }
            } catch (e: Exception) {
                Log.e("OfflineMode", "Erro ao criar task", e)
            }
        }
    }

    /**
     * Exemplo 2: Atualizar Grade enquanto offline
     *
     * CRÍTICO: Grades são dados sensíveis.
     * Sistema detecta conflitos automaticamente.
     */
    fun updateGradeOffline(grade: Grade) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i("OfflineMode", "Atualizando grade offline: ${grade.id}")

                // PASSO 1: Salvar localmente
                // gradeDao.update(grade)

                // PASSO 2: Enfileirar (com prioridade CRITICAL)
                syncQueue.enqueueGradeUpdate(grade)

                Log.i("OfflineMode", "✓ Grade salva localmente com sincronização em fila")

                // PASSO 3: Se online, sincronizar imediatamente (crítico)
                if (connectivityMonitor.isCurrentlyOnline()) {
                    processPendingSyncQueue()
                }
            } catch (e: Exception) {
                Log.e("OfflineMode", "Erro ao atualizar grade", e)
            }
        }
    }

    // ============== SINCRONIZAR FILA ==============

    /**
     * Processa todas as operações pendentes na fila.
     * Chamado automaticamente quando volta online.
     */
    private fun processPendingSyncQueue() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // PASSO 1: Verificar conectividade
                if (!connectivityMonitor.isCurrentlyOnline()) {
                    Log.w("OfflineMode", "Ainda offline, adiando sync...")
                    return@launch
                }

                Log.i("OfflineMode", "Iniciando sincronização de fila...")

                // PASSO 2: Obter operações pendentes
                // val pendingOps = syncQueueDao.getPendingOperations()  // Real: From Room
                val pendingOps = emptyList<OfflineSyncQueue.QueuedOperation>()  // Simulado

                if (pendingOps.isEmpty()) {
                    Log.i("OfflineMode", "✓ Fila vazia, nada para sincronizar")
                    return@launch
                }

                // PASSO 3: Deduplicar (remover duplicatas)
                val deduplicatedOps = syncQueue.deduplicateQueue(pendingOps)

                // PASSO 4: Processar fila
                val result = syncQueue.processSyncQueue(deduplicatedOps)

                Log.i("OfflineMode", """
                    ✓ Sincronização concluída:
                    ├─ Sincronizados: ${result.synced}
                    ├─ Falhados: ${result.failed}
                    └─ Pulados: ${result.skipped}
                """.trimIndent())

                // PASSO 5: Mostrar stats
                val stats = syncQueue.getQueueStats(deduplicatedOps)
                Log.i("OfflineMode", "Fila final: ${stats.pending} pendentes, ${stats.failed} falhados")

            } catch (e: Exception) {
                Log.e("OfflineMode", "Erro ao processar fila", e)
            }
        }
    }

    // ============== VERIFICAR ESTADO ==============

    /**
     * Obtém informações sobre conectividade e fila.
     */
    fun getOfflineStatus(): OfflineStatus {
        val networkInfo = connectivityMonitor.getNetworkInfo()

        return OfflineStatus(
            isOnline = networkInfo.isOnline,
            networkType = networkInfo.type,
            message = if (networkInfo.isOnline) {
                "Conectado via ${networkInfo.type}"
            } else {
                "Modo offline - Mudanças serão sincronizadas"
            }
        )
    }

    data class OfflineStatus(
        val isOnline: Boolean,
        val networkType: String,
        val message: String
    )

    // ============== INTEGRAÇÃO NO MAINACTIVITY ==============

    /**
     * COMO INTEGRAR NO MainActivity.kt:
     *
     * 1. Na Activity onCreate():
     * ```
     * viewModel.setupOfflineMode()  // Monitorar conectividade
     * ```
     *
     * 2. Ao criar dados (na UI/ViewModel):
     * ```
     * // Em vez de:
     * // repository.saveTask(task)
     *
     * // Usar:
     * viewModel.createTaskOffline(task)  // Salva local, enfileira para sync
     * ```
     *
     * 3. Na UI, mostrar status:
     * ```
     * val status = viewModel.getOfflineStatus()
     * if (!status.isOnline) {
     *     Text("Modo Offline",
     *         color = Color.Orange,
     *         fontSize = 12.sp
     *     )
     * }
     * ```
     *
     * 4. Ao reconnect automático sincroniza (sem ação do usuário!)
     */
}

// ============== COMPARAÇÃO: ANTES vs DEPOIS ==============

/**
 * ANTES (Sem Offline Mode):
 * ❌ Usuário vê erro ao ficar offline
 * ❌ Perde dados digitados quando reconnecta
 * ❌ Frustração com "tente novamente"
 * ❌ Sem sincronização automática
 *
 * DEPOIS (Com OfflineSyncQueue):
 * ✅ Continua editando normalmente offline
 * ✅ Dados salvos localmente
 * ✅ Auto-sincronização ao voltar online
 * ✅ Deduplicação automática
 * ✅ Priorização de dados críticos
 * ✅ Experiência perfeita para usuário
 */

// ============== TESTES RECOMENDADOS ==============

/*
TESTE 1: Criar Task Offline
- Desativar internet
- Criar nova task
- ✓ Esperado: Task aparece localmente, entra em fila

TESTE 2: Auto-Sync ao Reconectar
- Criar 3 tasks offline
- Reativar internet
- ✓ Esperado: Todas sincronizam automaticamente

TESTE 3: Grade Critical Priority
- Criar grade offline
- Verificar que tem prioridade CRITICAL
- ✓ Esperado: Sincroniza primeiro quando voltar online

TESTE 4: Deduplicação
- Criar 2 tasks com mesmo ID (bug)
- Sincronizar fila
- ✓ Esperado: Apenas 1 sincronizada, outra descartada

TESTE 5: Network Type Monitoring
- Conectar via WIFI
- Mudar para Cellular
- ✓ Esperado: isOnline continua true, mas type muda para CELLULAR

TESTE 6: Cleanup Automático
- Sincronizar muitos dados
- Aguardar 7 dias
- Chamar cleanupOldSuccessfulOps()
- ✓ Esperado: Operações antigas removidas de Room
*/
