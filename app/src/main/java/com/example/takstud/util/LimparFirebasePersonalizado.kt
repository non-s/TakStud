package com.example.takstud.util

import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

/**
 * Script personalizado para limpar o Firebase do TakStud
 * 
 * Baseado na análise do Firebase atual:
 * 
 * DELETAR:
 * - attendance (frequência - 11 registros)
 * - fcm_tokens (tokens de notificação - 3 dispositivos)
 * 
 * MANTER:
 * - schedules (horários de aula)
 * - config (código de acesso: 58239617)
 * 
 * CRIAR:
 * - notices (avisos)
 * - tasks (atividades)
 */
object LimparFirebasePersonalizado {
    
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Executa a limpeza personalizada
     */
    fun executar(
        onProgress: (String) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Timber.d("🗑️ Iniciando limpeza personalizada do Firebase...")
        onProgress("🗑️ Iniciando limpeza personalizada...")
        onProgress("")
        
        var tarefasConcluidas = 0
        val totalTarefas = 2 // attendance + fcm_tokens
        
        // 1. Deletar attendance
        deletarCollection("attendance") { sucesso, mensagem ->
            tarefasConcluidas++
            onProgress(mensagem)
            
            if (tarefasConcluidas == totalTarefas) {
                finalizarLimpeza(onProgress, onComplete)
            }
        }
        
        // 2. Deletar fcm_tokens
        deletarCollection("fcm_tokens") { sucesso, mensagem ->
            tarefasConcluidas++
            onProgress(mensagem)
            
            if (tarefasConcluidas == totalTarefas) {
                finalizarLimpeza(onProgress, onComplete)
            }
        }
    }
    
    /**
     * Deleta uma collection
     */
    private fun deletarCollection(
        collectionName: String,
        onResult: (sucesso: Boolean, mensagem: String) -> Unit
    ) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(true, "✅ Collection '$collectionName' já estava vazia")
                    return@addOnSuccessListener
                }
                
                val total = documents.size()
                var deletados = 0
                
                Timber.d("Deletando $total documentos de '$collectionName'...")
                
                documents.forEach { document ->
                    document.reference.delete()
                        .addOnSuccessListener {
                            deletados++
                            if (deletados == total) {
                                val msg = "✅ Collection '$collectionName' deletada ($total documentos)"
                                Timber.d(msg)
                                onResult(true, msg)
                            }
                        }
                        .addOnFailureListener { e ->
                            val msg = "❌ Erro ao deletar '$collectionName': ${e.message}"
                            Timber.e(msg)
                            onResult(false, msg)
                        }
                }
            }
            .addOnFailureListener { e ->
                val msg = "❌ Erro ao acessar '$collectionName': ${e.message}"
                Timber.e(msg)
                onResult(false, msg)
            }
    }
    
    /**
     * Finaliza a limpeza e mostra resumo
     */
    private fun finalizarLimpeza(
        onProgress: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        val resumo = """
            
            ═══════════════════════════════════════
            ✅ LIMPEZA CONCLUÍDA!
            ═══════════════════════════════════════
            
            Collections DELETADAS:
              ❌ attendance (frequência)
              ❌ fcm_tokens (notificações)
            
            Collections MANTIDAS:
              ✅ schedules (horários de aula)
              ✅ config (código: 58239617)
            
            Próximos passos:
              1. Criar collection 'notices' (avisos)
              2. Criar collection 'tasks' (atividades)
              3. Atualizar regras do Firestore
            
            ═══════════════════════════════════════
        """.trimIndent()
        
        Timber.d(resumo)
        onProgress(resumo)
        onComplete()
    }
    
    /**
     * Apenas verifica o que existe (sem deletar)
     */
    fun verificar(onResult: (String) -> Unit) {
        val resultado = StringBuilder()
        resultado.appendLine("🔍 VERIFICANDO FIREBASE...")
        resultado.appendLine()
        
        val collections = listOf("attendance", "fcm_tokens", "schedules", "config")
        var verificadas = 0
        
        collections.forEach { collectionName ->
            db.collection(collectionName)
                .get()
                .addOnSuccessListener { documents ->
                    verificadas++
                    
                    val count = documents.size()
                    val acao = when (collectionName) {
                        "schedules", "config" -> "✅ MANTER"
                        else -> "❌ DELETAR"
                    }
                    
                    resultado.appendLine("$acao - $collectionName ($count docs)")
                    
                    if (verificadas == collections.size) {
                        resultado.appendLine()
                        resultado.appendLine("Total de collections: ${collections.size}")
                        resultado.appendLine("Para manter: 2 (schedules, config)")
                        resultado.appendLine("Para deletar: 2 (attendance, fcm_tokens)")
                        
                        onResult(resultado.toString())
                    }
                }
                .addOnFailureListener {
                    verificadas++
                    resultado.appendLine("⚠️ - $collectionName (erro ou não existe)")
                    
                    if (verificadas == collections.size) {
                        onResult(resultado.toString())
                    }
                }
        }
    }
    
    /**
     * Cria as collections necessárias (notices e tasks)
     */
    fun criarCollectionsNecessarias(
        onProgress: (String) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        onProgress("🆕 Criando collections necessárias...")
        
        // Criar um documento exemplo em notices
        val noticeExemplo = hashMapOf(
            "id" to "exemplo_001",
            "title" to "Bem-vindo ao TakStud!",
            "content" to "Sistema simplificado de avisos e atividades",
            "studentClass" to "TODAS",
            "createdAt" to System.currentTimeMillis(),
            "createdBy" to "system"
        )
        
        db.collection("notices")
            .document("exemplo_001")
            .set(noticeExemplo)
            .addOnSuccessListener {
                onProgress("✅ Collection 'notices' criada com documento exemplo")
                
                // Criar um documento exemplo em tasks
                val taskExemplo = hashMapOf(
                    "id" to "exemplo_001",
                    "title" to "Atividade Exemplo",
                    "description" to "Esta é uma atividade de exemplo",
                    "dueDate" to "2024-12-31",
                    "studentClass" to "6°A",
                    "subject" to "Geral",
                    "createdAt" to System.currentTimeMillis(),
                    "createdBy" to "system"
                )
                
                db.collection("tasks")
                    .document("exemplo_001")
                    .set(taskExemplo)
                    .addOnSuccessListener {
                        onProgress("✅ Collection 'tasks' criada com documento exemplo")
                        onProgress("")
                        onProgress("✅ Todas as collections necessárias foram criadas!")
                        onComplete()
                    }
            }
    }
}
