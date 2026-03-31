package com.example.takstud.util

import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

/**
 * Script para simplificar o banco de dados do TakStud
 * 
 * Remove collections desnecessárias e mantém apenas:
 * - users (professores)
 * - schedules (horários)
 * - notices (avisos)
 * - tasks (atividades)
 * 
 * ATENÇÃO: Esta ação é IRREVERSÍVEL!
 * Faça backup antes de executar!
 */
object SimplificarBancoDados {
    
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Collections que serão DELETADAS
     */
    private val collectionsParaDeletar = listOf(
        "students",
        "attendance",
        "grades",
        "student_grades",
        "student_stats",
        "student_timeline",
        "report_cards",
        "assessments",
        "events",
        "notifications"
    )
    
    /**
     * Collections que serão MANTIDAS
     */
    private val collectionsParaManter = listOf(
        "users",
        "schedules",
        "notices",
        "tasks"
    )
    
    /**
     * Executa a limpeza completa do Firebase
     */
    fun executarLimpeza(
        onProgress: (String) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Timber.d("🗑️ Iniciando limpeza do banco de dados...")
        onProgress("🗑️ Iniciando limpeza do banco de dados...")
        
        var collectionsProcessadas = 0
        val totalCollections = collectionsParaDeletar.size
        
        collectionsParaDeletar.forEach { collectionName ->
            deletarCollection(collectionName) { sucesso, mensagem ->
                collectionsProcessadas++
                
                if (sucesso) {
                    val msg = "✅ [$collectionsProcessadas/$totalCollections] $mensagem"
                    Timber.d(msg)
                    onProgress(msg)
                } else {
                    val msg = "❌ [$collectionsProcessadas/$totalCollections] $mensagem"
                    Timber.e(msg)
                    onProgress(msg)
                }
                
                // Quando terminar todas
                if (collectionsProcessadas == totalCollections) {
                    val msgFinal = """
                        
                        ✅ Limpeza concluída!
                        
                        Collections deletadas: $totalCollections
                        Collections mantidas: ${collectionsParaManter.size}
                        
                        Mantidas:
                        ${collectionsParaManter.joinToString("\n") { "  ✅ $it" }}
                    """.trimIndent()
                    
                    Timber.d(msgFinal)
                    onProgress(msgFinal)
                    onComplete()
                }
            }
        }
    }
    
    /**
     * Deleta uma collection inteira
     */
    private fun deletarCollection(
        collectionName: String,
        onResult: (sucesso: Boolean, mensagem: String) -> Unit
    ) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(true, "Collection '$collectionName' já estava vazia")
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
                                onResult(true, "Collection '$collectionName' deletada ($total documentos)")
                            }
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Erro ao deletar '$collectionName': ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Erro ao acessar '$collectionName': ${e.message}")
            }
    }
    
    /**
     * Verifica quais collections existem no Firebase
     */
    fun verificarCollections(onResult: (String) -> Unit) {
        val resultado = StringBuilder()
        resultado.appendLine("🔍 Verificando collections no Firebase...")
        resultado.appendLine()
        
        // Tentar acessar cada collection
        val todasCollections = collectionsParaDeletar + collectionsParaManter
        var verificadas = 0
        
        todasCollections.forEach { collectionName ->
            db.collection(collectionName)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    verificadas++
                    
                    val status = if (collectionName in collectionsParaManter) "✅ MANTER" else "❌ DELETAR"
                    val count = if (documents.isEmpty) "vazia" else "${documents.size()}+ docs"
                    
                    resultado.appendLine("$status - $collectionName ($count)")
                    
                    if (verificadas == todasCollections.size) {
                        resultado.appendLine()
                        resultado.appendLine("Total de collections: ${todasCollections.size}")
                        resultado.appendLine("Para manter: ${collectionsParaManter.size}")
                        resultado.appendLine("Para deletar: ${collectionsParaDeletar.size}")
                        
                        onResult(resultado.toString())
                    }
                }
                .addOnFailureListener {
                    verificadas++
                    resultado.appendLine("⚠️ - $collectionName (não existe ou erro)")
                    
                    if (verificadas == todasCollections.size) {
                        onResult(resultado.toString())
                    }
                }
        }
    }
    
    /**
     * Limpa apenas dados de alunos (mais conservador)
     */
    fun limparApenasAlunos(
        onProgress: (String) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        val collectionsAlunos = listOf(
            "students",
            "attendance",
            "grades",
            "student_grades",
            "student_stats",
            "student_timeline"
        )
        
        var processadas = 0
        
        collectionsAlunos.forEach { collectionName ->
            deletarCollection(collectionName) { sucesso, mensagem ->
                processadas++
                onProgress(mensagem)
                
                if (processadas == collectionsAlunos.size) {
                    onComplete()
                }
            }
        }
    }
}
