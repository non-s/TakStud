package com.example.takstud.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val TAG = "AuditRepository"

    suspend fun logAccessAudit(
        userId: String,
        userRole: String,
        resourceType: String,
        resourceId: String,
        granted: Boolean
    ) = try {
        db.collection("access_audit_logs")
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
}
