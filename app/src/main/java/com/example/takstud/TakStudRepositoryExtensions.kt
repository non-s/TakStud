package com.example.takstud

import android.util.Log
import com.example.takstud.model.Student
import com.example.takstud.security.AccessValidator
import com.example.takstud.util.firestoreQueryFlow
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Extensões do Repository para gerenciar relacionamentos e validação de acesso.
 *
 * Adiciona funcionalidades de:
 * - Validação de relacionamento parent-student
 * - Validação de relacionamento teacher-class
 * - Queries filtradas por permissões
 */

private const val TAG = "TakStudRepositoryExt"

/**
 * Verifica se um parent é responsável por um student.
 *
 * SEGURANÇA CRÍTICA: Valida antes de permitir acesso aos dados do student.
 *
 * @param parentId ID do parent (pai/responsável)
 * @param studentId ID do student (filho/aluno)
 * @return true se o parent é responsável pelo student
 */
suspend fun TakStudRepository.isParentOfStudent(
    parentId: String,
    studentId: String
): Boolean = try {
    // Opção 1: Procurar por relação direta na collection "parent_student_relationships"
    val relationshipDoc = Firebase.firestore
        .collection("parent_student_relationships")
        .document("${parentId}_${studentId}")
        .get()
        .await()

    if (relationshipDoc.exists()) {
        Log.i(TAG, "✓ Relacionamento confirmado: parent=$parentId, student=$studentId")
        true
    } else {
        // Opção 2: Procurar dados do student para ver se tem o parent como responsável
        val studentDoc = Firebase.firestore
            .collection("students")
            .document(studentId)
            .get()
            .await()

        val student = studentDoc.toObject(Student::class.java)
        val isParent = student?.parent == parentId

        if (isParent) {
            Log.i(TAG, "✓ Parent verificado via student document: parent=$parentId, student=$studentId")
        } else {
            Log.w(TAG, "✗ Parent NÃO é responsável: parent=$parentId, student=$studentId")
        }

        isParent
    }
} catch (e: Exception) {
    Log.e(TAG, "Erro ao verificar relacionamento parent-student", e)
    false
}

/**
 * Obtém todos os students filhos de um parent.
 *
 * Útil para filtrar telas e permissões.
 *
 * @param parentId ID do parent
 * @return Flow<List<Student>> dos filhos do parent
 */
fun TakStudRepository.getStudentsForParent(parentId: String): Flow<List<Student>> {
    Log.i(TAG, "Buscando students para parent $parentId")
    return firestoreQueryFlow(
        Firebase.firestore
            .collection("students")
            .whereEqualTo("parent", parentId),
        Student::class.java,
        TAG
    )
}

/**
 * Obtém students que um teacher pode lecionar (por turma).
 *
 * @param teacherId ID do teacher
 * @param classNames Lista de turmas que o teacher leciona
 * @return Flow<List<Student>> dos alunos do teacher
 */
fun TakStudRepository.getStudentsForTeacher(
    teacherId: String,
    classNames: List<String>
): Flow<List<Student>> {
    Log.i(TAG, "Buscando students para teacher $teacherId em ${classNames.size} turmas")
    return if (classNames.isEmpty()) {
        flowOf(emptyList())
    } else {
        firestoreQueryFlow(
            Firebase.firestore
                .collection("students")
                .whereIn("studentClass", classNames),
            Student::class.java,
            TAG
        )
    }
}

/**
 * Registra um novo relacionamento parent-student no banco de dados.
 *
 * Chamado quando:
 * - Um parent é registrado no sistema
 * - Um novo filho é associado a um parent
 *
 * @param parentId ID do parent
 * @param studentId ID do student
 */
suspend fun TakStudRepository.createParentStudentRelationship(
    parentId: String,
    studentId: String
) = try {
    Firebase.firestore
        .collection("parent_student_relationships")
        .document("${parentId}_${studentId}")
        .set(mapOf(
            "parentId" to parentId,
            "studentId" to studentId,
            "createdAt" to System.currentTimeMillis()
        ))
        .await()

    Log.i(TAG, "Relacionamento criado: parent=$parentId, student=$studentId")
} catch (e: Exception) {
    Log.e(TAG, "Erro ao criar relacionamento parent-student", e)
    throw e
}

/**
 * Remove um relacionamento parent-student.
 *
 * @param parentId ID do parent
 * @param studentId ID do student
 */
suspend fun TakStudRepository.removeParentStudentRelationship(
    parentId: String,
    studentId: String
) = try {
    Firebase.firestore
        .collection("parent_student_relationships")
        .document("${parentId}_${studentId}")
        .delete()
        .await()

    Log.i(TAG, "Relacionamento removido: parent=$parentId, student=$studentId")
} catch (e: Exception) {
    Log.e(TAG, "Erro ao remover relacionamento", e)
    throw e
}

/**
 * Obtém todos os relacionamentos parent-student.
 *
 * Útil para auditoria e gerenciamento.
 */
suspend fun TakStudRepository.getAllParentStudentRelationships():
        List<AccessValidator.ParentStudentRelationship> = try {
    val docs = Firebase.firestore
        .collection("parent_student_relationships")
        .get()
        .await()

    val relationships = mutableMapOf<String, MutableList<String>>()

    docs.forEach { doc ->
        val parentId = doc.getString("parentId") ?: return@forEach
        val studentId = doc.getString("studentId") ?: return@forEach

        relationships.getOrPut(parentId) { mutableListOf() }.add(studentId)
    }

    relationships.map { (parentId, studentIds) ->
        AccessValidator.ParentStudentRelationship(parentId, studentIds)
    }
} catch (e: Exception) {
    Log.e(TAG, "Erro ao obter relacionamentos", e)
    emptyList()
}

/**
 * Auditoria: Registra acesso de usuário para análise.
 *
 * @param userId ID do usuário
 * @param userRole Role (PARENT, TEACHER, ADMIN)
 * @param resourceType Tipo de recurso acessado
 * @param resourceId ID do recurso
 * @param granted Se o acesso foi permitido
 */
suspend fun TakStudRepository.logAccessAudit(
    userId: String,
    userRole: String,
    resourceType: String,
    resourceId: String,
    granted: Boolean
) = try {
    Firebase.firestore
        .collection("access_audit_logs")
        .add(mapOf(
            "userId" to userId,
            "userRole" to userRole,
            "resourceType" to resourceType,
            "resourceId" to resourceId,
            "granted" to granted,
            "timestamp" to System.currentTimeMillis()
        ))
        .await()

    val status = if (granted) "✓ GRANTED" else "✗ DENIED"
    Log.i(TAG, "$status: $userRole($userId) -> $resourceType($resourceId)")
} catch (e: Exception) {
    Log.e(TAG, "Erro ao registrar auditoria", e)
}
