package com.example.takstud.data.repository

import com.example.takstud.model.Notice
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getNotices(): Flow<List<Notice>> = firestoreCollectionFlow(
        db.collection("notices"),
        Notice::class.java,
        "TakStud"
    )

    fun saveNotice(notice: Notice, onComplete: () -> Unit) {
        val noticeRef = if (notice.id.isBlank()) db.collection("notices").document() else db.collection("notices").document(notice.id)
        noticeRef.set(notice.copy(id = noticeRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    fun deleteNotice(notice: Notice) {
        db.collection("notices").document(notice.id).delete()
    }
}
