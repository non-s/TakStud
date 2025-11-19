package com.example.takstud

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.model.Grade
import com.example.takstud.model.Task
import com.example.takstud.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * EXEMPLO DE INTEGRAÇÃO: Como usar SyncManager para sincronização bidirecional
 *
 * Este arquivo mostra como implementar sincronização automática entre
 * Room (local) e Firestore (remoto) com rastreamento de timestamps.
 *
 * COPIE ESTE CÓDIGO para TakStudViewModel.kt
 */

class SyncIntegrationExample(
    private val repository: TakStudRepository,
    private val syncManager: SyncManager = SyncManager()
) : ViewModel() {

    // ============== SINCRONIZAÇÃO DE TASKS ==============

    /**
     * Exemplo 1: Sincronizar tasks local com remoto
     *
     * PADRÃO:
     * 1. Obter tasks locais (Room)
     * 2. Obter tasks remotas (Firestore)
     * 3. Merge com SyncManager
     * 4. Salvar resultado localmente
     */
    fun syncTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i("SyncExample", "Iniciando sync de tasks...")

                // PASSO 1: Obter todas as tasks
                // Em um ViewModel real, isso viria do seu repository
                // val localTasks = taskDao.getAllTasks()
                // val remoteTasks = repository.getTasks().firstOrNull() ?: emptyList()

                // SIMULADO para exemplo:
                val localTasks = listOf(
                    Task(
                        id = "task1",
                        title = "Matemática",
                        modifiedAt = System.currentTimeMillis() - 10000,  // 10s atrás
                        isSynced = false
                    )
                )

                val remoteTasks = listOf(
                    Task(
                        id = "task1",
                        title = "Matemática",
                        modifiedAt = System.currentTimeMillis() - 20000,  // 20s atrás
                        isSynced = true
                    ),
                    Task(
                        id = "task2",
                        title = "Português",
                        modifiedAt = System.currentTimeMillis() - 5000,
                        isSynced = true
                    )
                )

                // PASSO 2: Sincronizar com SyncManager
                val result = syncManager.syncTasks(localTasks, remoteTasks)

                when (result) {
                    is com.example.takstud.util.ErrorHandler.Result.Success -> {
                        val mergedTasks = result.data
                        Log.i("SyncExample", "✓ Sync bem-sucedido: ${mergedTasks.size} tasks")

                        // PASSO 3: Salvar localmente
                        // taskDao.insertAll(mergedTasks)  // Real implementation

                        // PASSO 4: Marcar como sincronizado
                        // mergedTasks.forEach { task ->
                        //     taskDao.update(task.copy(isSynced = true))
                        // }
                    }

                    is com.example.takstud.util.ErrorHandler.Result.Error -> {
                        Log.e("SyncExample", "✗ Erro no sync: ${result.exception.message}")
                    }

                    is com.example.takstud.util.ErrorHandler.Result.Loading -> {
                        Log.i("SyncExample", "⏳ Sincronizando...")
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncExample", "Erro geral:", e)
            }
        }
    }

    // ============== SINCRONIZAÇÃO DE GRADES (COM AUDITORIA) ==============

    /**
     * Exemplo 2: Sincronizar grades com detecção de conflitos
     *
     * CRÍTICO: Grades são dados sensíveis que precisam auditoria.
     * Este exemplo mostra como SyncManager detecta conflitos.
     */
    fun syncGrades() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i("SyncExample", "Iniciando sync de grades...")

                // SIMULADO: 2 versões conflitantes da mesma grade
                val localGrades = listOf(
                    Grade(
                        id = "grade1",
                        studentId = "student1",
                        value = "9.5",  // Local: 9.5
                        modifiedAt = System.currentTimeMillis() - 5000,  // Modificado agora
                        isSynced = false
                    )
                )

                val remoteGrades = listOf(
                    Grade(
                        id = "grade1",
                        studentId = "student1",
                        value = "8.0",  // Remoto: 8.0
                        modifiedAt = System.currentTimeMillis() - 10000,  // Modificado antes
                        isSynced = true
                    )
                )

                // PASSO 1: Sincronizar (detacta conflito automaticamente)
                val result = syncManager.syncGrades(localGrades, remoteGrades)

                when (result) {
                    is com.example.takstud.util.ErrorHandler.Result.Success -> {
                        val mergedGrades = result.data
                        Log.i("SyncExample", "✓ Grades sincronizadas: ${mergedGrades.size}")

                        // LOCAL WINS (versão local é mais nova)
                        // Conflito foi registrado em audit_logs automaticamente
                        mergedGrades.forEach { grade ->
                            Log.i("SyncExample", "Grade: ${grade.id} = ${grade.value}")
                        }
                    }

                    is com.example.takstud.util.ErrorHandler.Result.Error -> {
                        Log.e("SyncExample", "Erro:", result.exception.message)
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("SyncExample", "Erro geral:", e)
            }
        }
    }

    // ============== BATCH SYNC (MÚLTIPLOS ITENS) ==============

    /**
     * Exemplo 3: Sincronização em batch para múltiplas coleções
     *
     * PADRÃO: Use batch sync para sincronizar múltiplas coleções
     * atomicamente (transação Firestore).
     */
    fun batchSyncMultipleCollections() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i("SyncExample", "Iniciando batch sync...")

                // PASSO 1: Preparar dados para batch sync
                val itemsToSync = mapOf(
                    "tasks" to listOf(
                        mapOf(
                            "id" to "task1",
                            "title" to "Matemática",
                            "modifiedAt" to System.currentTimeMillis()
                        )
                    ),
                    "grades" to listOf(
                        mapOf(
                            "id" to "grade1",
                            "studentId" to "student1",
                            "value" to "9.5",
                            "modifiedAt" to System.currentTimeMillis()
                        )
                    )
                )

                // PASSO 2: Sincronizar em batch (atômico)
                val result = syncManager.batchSyncItems(
                    items = itemsToSync,
                    operation = "set"
                )

                when (result) {
                    is com.example.takstud.util.ErrorHandler.Result.Success -> {
                        val itemsCount = result.data
                        Log.i("SyncExample", "✓ Batch sync bem-sucedido: $itemsCount itens sincronizados")
                    }

                    is com.example.takstud.util.ErrorHandler.Result.Error -> {
                        Log.e("SyncExample", "Erro no batch:", result.exception.message)
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("SyncExample", "Erro geral:", e)
            }
        }
    }

    // ============== MONITORAR ESTATÍSTICAS DE SYNC ==============

    /**
     * Exemplo 4: Obter estatísticas de sincronização
     *
     * Útil para UI: mostrar "Sincronizando..." ou "Última sync: 5 min atrás"
     */
    fun getSyncStatistics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = syncManager.getSyncStats()

                when (result) {
                    is com.example.takstud.util.ErrorHandler.Result.Success -> {
                        val stats = result.data
                        Log.i("SyncExample", """
                            Estatísticas de Sync:
                            ├─ Última sincronização: ${stats.lastSyncTime}
                            ├─ Itens sincronizados: ${stats.itemsSynced}
                            ├─ Conflitos resolvidos: ${stats.conflictsResolved}
                            └─ Duração: ${stats.syncDuration}ms
                        """.trimIndent())
                    }

                    is com.example.takstud.util.ErrorHandler.Result.Error -> {
                        Log.e("SyncExample", "Erro ao obter stats:", result.exception.message)
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("SyncExample", "Erro geral:", e)
            }
        }
    }

    // ============== INTEGRAÇÃO REAL (TakStudViewModel) ==============

    /**
     * COMO INTEGRAR NO TakStudViewModel.kt:
     *
     * Passo 1: Instanciar SyncManager
     * private val syncManager = SyncManager()
     *
     * Passo 2: Chamar sync ao carregar dados
     * fun loadAllData() {
     *     viewModelScope.launch {
     *         // 1. Carregar local
     *         val localTasks = taskDao.getAllTasks()
     *         val localGrades = gradeDao.getAllGrades()
     *
     *         // 2. Carregar remoto
     *         val remoteTasks = repository.getTasks().first()
     *         val remoteGrades = repository.getGrades().first()
     *
     *         // 3. Merge
     *         val mergedTasks = syncManager.syncTasks(localTasks, remoteTasks)
     *         val mergedGrades = syncManager.syncGrades(localGrades, remoteGrades)
     *
     *         // 4. Atualizar UI
     *         _tasks.value = mergedTasks.data ?: emptyList()
     *         _grades.value = mergedGrades.data ?: emptyList()
     *     }
     * }
     *
     * Passo 3: Chamar ao salvar dados
     * fun saveTask(task: Task) {
     *     viewModelScope.launch {
     *         // 1. Salvar localmente
     *         taskDao.insert(task.copy(
     *             modifiedAt = System.currentTimeMillis(),
     *             isSynced = false
     *         ))
     *
     *         // 2. Enviar para remoto (SyncManager faz retry automático)
     *         syncManager.uploadTaskToFirestore(task)
     *     }
     * }
     */
}

// ============== COMPARAÇÃO: ANTES vs DEPOIS ==============

/**
 * ANTES (Sem Sync):
 * ❌ Dados locais e remotos desincronizados
 * ❌ Conflitos não detectados
 * ❌ Sem timestamp tracking
 * ❌ Sem auditoria de mudanças
 * ❌ Usuário perde dados ao desconectar
 *
 * DEPOIS (Com SyncManager):
 * ✅ Sincronização automática bidirecional
 * ✅ Detecção automática de conflitos
 * ✅ Timestamp em cada mudança
 * ✅ Auditoria completa em Firestore
 * ✅ Funciona offline com auto-sync ao reconectar
 * ✅ Batch operations para múltiplas coleções
 * ✅ Retry automático com backoff exponencial
 */

// ============== TESTES RECOMENDADOS ==============

/*
TESTE 1: Sincronizar Task Modificada Localmente
- Criar task localmente (modifiedAt = agora)
- Ter task remota mais antiga
- ✓ Esperado: Versão local prevalece

TESTE 2: Sincronizar Task Modificada Remotamente
- Task remota mais nova (modifiedAt > local)
- ✓ Esperado: Versão remota prevalece

TESTE 3: Conflito de Grade
- Grade local: 9.5, modifiedAt = T1
- Grade remota: 8.0, modifiedAt = T0 (T0 < T1)
- ✓ Esperado: Local wins, conflito registrado em audit_logs

TESTE 4: Batch Sync Atômico
- Sincronizar 3 tarefas e 2 grades simultaneamente
- Simular falha Firestore na segunda operação
- ✓ Esperado: Ou tudo sincroniza, ou nada (atomic)

TESTE 5: Estatísticas de Sync
- Fazer 5 sincronizações
- ✓ Esperado: Statisticas mostram 5 sincronizações, últimas timestamps, conflitos
*/
