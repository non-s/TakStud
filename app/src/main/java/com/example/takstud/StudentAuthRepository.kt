package com.example.takstud

import android.util.Log
import com.example.takstud.model.Student
import com.example.takstud.util.Result
import com.example.takstud.util.SessionManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Repository for student authentication and registration.
 * Handles login validation for parents (by RA) and teacher code verification.
 * Also manages student registration by teachers.
 */
class StudentAuthRepository {

    private val db = Firebase.firestore
    private val TEACHER_CONFIG_DOC = "config/teacherAccess"
    private val STUDENTS_COLLECTION = "students"

    /**
     * Login parent using RA.
     * Validates that the RA exists in the Firestore students collection.
     *
     * @param ra The student RA to validate
     * @return Result with Student data if found, Error if not found or exception occurs
     */
    suspend fun loginWithRA(ra: String): Result<Student> = try {
        val snapshot = db.collection(STUDENTS_COLLECTION)
            .whereEqualTo("ra", ra)
            .limit(1)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            val doc = snapshot.documents.first()
            val student = doc.toObject(Student::class.java)?.copy(id = doc.id)
            if (student != null) {
                Result.Success(student)
            } else {
                Result.Error(Exception("Erro ao processar dados do estudante"))
            }
        } else {
            Result.Error(Exception("RA não encontrado no sistema"))
        }
    } catch (e: Exception) {
        Log.e("StudentAuth", "Error logging in with RA", e)
        Result.Error(e)
    }

    /**
     * Validate teacher access code.
     * Checks if the provided code matches the stored code in config/teacherAccess.
     *
     * @param code The access code to validate
     * @return Result with Boolean (true if code is correct, false otherwise)
     */
    suspend fun validateTeacherCode(code: String): Result<Boolean> = try {
        val doc = db.document(TEACHER_CONFIG_DOC).get().await()

        if (doc.exists()) {
            val storedCode = doc.getString("code") ?: ""
            Result.Success(code == storedCode)
        } else {
            Log.w("StudentAuth", "Teacher config document does not exist")
            Result.Success(false)
        }
    } catch (e: Exception) {
        Log.e("StudentAuth", "Error validating teacher code", e)
        Result.Error(e)
    }

    /**
     * Register a new student in the system.
     * Teachers use this to register students by their RA.
     *
     * @param ra Student RA (unique identifier)
     * @param name Student full name
     * @param studentClass Class/grade of the student
     * @param parent Parent/guardian name
     * @param phone Contact phone number
     * @return Result with Unit on success, Error on failure
     */
    suspend fun registerStudent(
        ra: String,
        name: String,
        studentClass: String,
        parent: String,
        phone: String
    ): Result<Unit> = try {
        // Use RA as document ID for easy lookup
        db.collection(STUDENTS_COLLECTION)
            .document(ra)
            .set(
                mapOf(
                    "ra" to ra,
                    "name" to name,
                    "studentClass" to studentClass,
                    "parent" to parent,
                    "phone" to phone,
                    "createdAt" to System.currentTimeMillis()
                )
            )
            .await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("StudentAuth", "Error registering student", e)
        Result.Error(e)
    }

    /**
     * Get all registered students.
     * Teachers use this to view all registered RAs in the system.
     *
     * @return Result with List of all registered Students
     */
    suspend fun getAllStudents(): Result<List<Student>> = try {
        val snapshot = db.collection(STUDENTS_COLLECTION).get().await()
        val studentList = snapshot.mapNotNull { doc ->
            try {
                doc.toObject(Student::class.java).copy(id = doc.id)
            } catch (e: Exception) {
                Log.e("StudentAuth", "Error converting student document", e)
                null
            }
        }
        Result.Success(studentList)
    } catch (e: Exception) {
        Log.e("StudentAuth", "Error getting all students", e)
        Result.Error(e)
    }

    /**
     * Delete a student by RA.
     * Teachers use this to remove students from the system.
     *
     * @param ra The RA of the student to delete
     * @return Result with Unit on success, Error on failure
     */
    suspend fun deleteStudent(ra: String): Result<Unit> = try {
        db.collection(STUDENTS_COLLECTION)
            .document(ra)
            .delete()
            .await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("StudentAuth", "Error deleting student", e)
        Result.Error(e)
    }

    /**
     * Check if RA already exists in the system.
     * Used for validation before registration.
     *
     * @param ra The RA to check
     * @return Result with Boolean (true if RA exists, false otherwise)
     */
    suspend fun raExists(ra: String): Result<Boolean> = try {
        val doc = db.collection(STUDENTS_COLLECTION)
            .document(ra)
            .get()
            .await()
        Result.Success(doc.exists())
    } catch (e: Exception) {
        Log.e("StudentAuth", "Error checking if RA exists", e)
        Result.Error(e)
    }

    /**
     * Login parent using RA and create session.
     * This combines authentication and session creation.
     *
     * @param ra The student RA to validate
     * @return Result with Student data if successful, Error otherwise
     */
    suspend fun loginParentWithSession(ra: String): Result<Student> {
        return try {
            val loginResult = loginWithRA(ra)
            when (loginResult) {
                is Result.Success -> {
                    val student = loginResult.data
                    // Create parent session
                    SessionManager.createParentSession(
                        studentId = student.id,
                        studentRa = student.ra,
                        studentName = student.name
                    )
                    Log.d("StudentAuth", "Parent session created for student: ${student.ra}")
                    Result.Success(student)
                }
                is Result.Error -> loginResult
                is Result.Loading -> loginResult
            }
        } catch (e: Exception) {
            Log.e("StudentAuth", "Error in parent login with session", e)
            Result.Error(e)
        }
    }

    /**
     * Login teacher using access code and create session.
     * This combines authentication and session creation.
     *
     * @param code The teacher access code
     * @return Result with Boolean (true if successful), Error otherwise
     */
    suspend fun loginTeacherWithSession(code: String): Result<Boolean> {
        return try {
            val validateResult = validateTeacherCode(code)
            when (validateResult) {
                is Result.Success -> {
                    if (validateResult.data) {
                        // Create teacher session
                        SessionManager.createTeacherSession()
                        Log.d("StudentAuth", "Teacher session created")
                        Result.Success(true)
                    } else {
                        Result.Success(false)
                    }
                }
                is Result.Error -> validateResult
                is Result.Loading -> validateResult
            }
        } catch (e: Exception) {
            Log.e("StudentAuth", "Error in teacher login with session", e)
            Result.Error(e)
        }
    }

    /**
     * Logout current user and clear session.
     */
    fun logout() {
        SessionManager.logout()
        Log.d("StudentAuth", "User logged out and session cleared")
    }

    /**
     * Get current session if authenticated.
     *
     * @return UserSession or null if not authenticated
     */
    fun getCurrentSession() = SessionManager.currentSession.value
}