package com.example.takstud.util

import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

/**
 * Utilitário temporário para remover dados do aluno BRENO
 * 
 * ATENÇÃO: Este arquivo é temporário e deve ser removido após o uso!
 * 
 * Como usar:
 * 1. Chame RemoverAlunoBreno.executar() de qualquer lugar do app
 * 2. Verifique os logs para confirmar a remoção
 * 3. Delete este arquivo após o uso
 */
object RemoverAlunoBreno {
    
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Remove o aluno BRENO de todas as collections do Firestore
     */
    fun executar(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Timber.d("🗑️ Iniciando remoção do aluno BRENO...")
        
        // Buscar aluno BRENO na collection 'students'
        db.collection("students")
            .get()
            .addOnSuccessListener { documents ->
                var encontrado = false
                
                for (document in documents) {
                    val nome = document.getString("name")?.uppercase() ?: ""
                    
                    // Verificar se é BRENO (case insensitive)
                    if (nome.contains("BRENO")) {
                        encontrado = true
                        val studentId = document.id
                        val ra = document.getString("ra") ?: "N/A"
                        val turma = document.getString("studentClass") ?: "N/A"
                        
                        Timber.d("✅ Aluno encontrado:")
                        Timber.d("   ID: $studentId")
                        Timber.d("   Nome: $nome")
                        Timber.d("   RA: $ra")
                        Timber.d("   Turma: $turma")
                        
                        // Remover dados relacionados
                        removerDadosRelacionados(studentId) {
                            // Remover o aluno
                            db.collection("students")
                                .document(studentId)
                                .delete()
                                .addOnSuccessListener {
                                    Timber.d("✅ Aluno BRENO removido com sucesso!")
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    val erro = "❌ Erro ao remover aluno: ${e.message}"
                                    Timber.e(erro)
                                    onError(erro)
                                }
                        }
                    }
                }
                
                if (!encontrado) {
                    val msg = "⚠️ Aluno BRENO não encontrado no Firestore"
                    Timber.w(msg)
                    onError(msg)
                }
            }
            .addOnFailureListener { e ->
                val erro = "❌ Erro ao buscar aluno: ${e.message}"
                Timber.e(erro)
                onError(erro)
            }
    }
    
    /**
     * Remove dados relacionados ao aluno (notas, frequência, etc.)
     */
    private fun removerDadosRelacionados(studentId: String, onComplete: () -> Unit) {
        var tarefasPendentes = 0
        
        // Collections que podem ter dados do aluno
        val collections = listOf(
            "grades",           // Notas
            "attendance",       // Frequência
            "student_grades",   // Notas detalhadas
            "student_stats",    // Estatísticas
            "student_timeline"  // Linha do tempo
        )
        
        Timber.d("🗑️ Removendo dados relacionados do aluno $studentId...")
        
        collections.forEach { collectionName ->
            tarefasPendentes++
            
            db.collection(collectionName)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Timber.d("   ℹ️ Nenhum dado em $collectionName")
                        tarefasPendentes--
                        if (tarefasPendentes == 0) onComplete()
                    } else {
                        var deletados = 0
                        documents.forEach { doc ->
                            db.collection(collectionName)
                                .document(doc.id)
                                .delete()
                                .addOnSuccessListener {
                                    deletados++
                                    Timber.d("   ✅ Removido de $collectionName: ${doc.id}")
                                    
                                    if (deletados == documents.size()) {
                                        tarefasPendentes--
                                        if (tarefasPendentes == 0) onComplete()
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Timber.e("   ❌ Erro ao remover de $collectionName: ${e.message}")
                    tarefasPendentes--
                    if (tarefasPendentes == 0) onComplete()
                }
        }
        
        // Se não houver tarefas, completar imediatamente
        if (tarefasPendentes == 0) {
            onComplete()
        }
    }
    
    /**
     * Busca e lista informações do aluno BRENO sem remover
     */
    fun buscarInfo(onResult: (String) -> Unit) {
        db.collection("students")
            .get()
            .addOnSuccessListener { documents ->
                val resultado = StringBuilder()
                resultado.appendLine("🔍 Buscando aluno BRENO...")
                resultado.appendLine()
                
                var encontrado = false
                
                for (document in documents) {
                    val nome = document.getString("name")?.uppercase() ?: ""
                    
                    if (nome.contains("BRENO")) {
                        encontrado = true
                        resultado.appendLine("✅ Aluno encontrado:")
                        resultado.appendLine("   ID: ${document.id}")
                        resultado.appendLine("   Nome: ${document.getString("name")}")
                        resultado.appendLine("   RA: ${document.getString("ra")}")
                        resultado.appendLine("   Turma: ${document.getString("studentClass")}")
                        resultado.appendLine("   Email: ${document.getString("email")}")
                        resultado.appendLine()
                    }
                }
                
                if (!encontrado) {
                    resultado.appendLine("⚠️ Aluno BRENO não encontrado")
                }
                
                onResult(resultado.toString())
            }
            .addOnFailureListener { e ->
                onResult("❌ Erro ao buscar: ${e.message}")
            }
    }
}
