package com.example.takstud.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.takstud.data.local.AppDatabase
import com.example.takstud.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes abrangentes para [AttendanceDao] usando Room in-memory database.
 *
 * Cobre:
 * - CRUD operations
 * - Queries com filtros (por estudante, turma e data)
 * - Ordenação por data
 * - Batch operations
 * - Sync status management
 *
 * @see AttendanceDao
 * @see AttendanceEntity
 */
@RunWith(AndroidJUnit4::class)
class AttendanceDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var attendanceDao: AttendanceDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        attendanceDao = database.attendanceDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== TESTES DE CRIAÇÃO ====================

    /**
     * Teste: Inserir novo registro de frequência
     * Esperado: Registro é salvo com sucesso
     */
    @Test
    fun insertAttendance_savesSuccessfully() = runBlocking {
        // Arrange
        val attendance = AttendanceEntity(
            id = "student_001-2025-11-14",
            studentId = "student_001",
            studentClass = "6A",
            date = "2025-11-14",
            isPresent = true,
            isSynced = false,
            timestamp = System.currentTimeMillis()
        )

        // Act
        attendanceDao.insertAttendance(attendance)

        // Assert
        val retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")
        assertNotNull(retrieved)
        assertTrue(retrieved.isPresent)
    }

    /**
     * Teste: Inserir múltiplos registros em batch
     * Esperado: Todos são salvos
     */
    @Test
    fun insertAttendances_batch_savesAll() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("student_001-2025-11-14", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_002-2025-11-14", "student_002", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_003-2025-11-14", "student_003", "6A", "2025-11-14", false, false, System.currentTimeMillis())
        )

        // Act
        attendanceDao.insertAttendances(attendances)

        // Assert
        val allAttendance = attendanceDao.getAllAttendance().first()
        assertEquals(3, allAttendance.size)
    }

    /**
     * Teste: Inserir com ID duplicado
     * Esperado: OnConflictStrategy.REPLACE substitui o existente
     */
    @Test
    fun insertAttendance_withDuplicateId_replaces() = runBlocking {
        // Arrange
        val attendance1 = AttendanceEntity(
            "student_001-2025-11-14",
            "student_001",
            "6A",
            "2025-11-14",
            true,
            false,
            System.currentTimeMillis()
        )
        val attendance2 = AttendanceEntity(
            "student_001-2025-11-14",
            "student_001",
            "6A",
            "2025-11-14",
            false,
            true,
            System.currentTimeMillis()
        )

        // Act
        attendanceDao.insertAttendance(attendance1)
        attendanceDao.insertAttendance(attendance2)

        // Assert
        val retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")
        assertNotNull(retrieved)
        assertFalse(retrieved.isPresent)
        assertTrue(retrieved.isSynced)
    }

    // ==================== TESTES DE LEITURA ====================

    /**
     * Teste: Recuperar todos os registros de frequência
     * Esperado: Flow emite lista completa, ordenada por data DESC
     */
    @Test
    fun getAllAttendance_returnsAllOrderedByDate() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("attendance_001", "student_001", "6A", "2025-11-12", true, false, System.currentTimeMillis()),
            AttendanceEntity("attendance_002", "student_001", "6A", "2025-11-13", true, false, System.currentTimeMillis()),
            AttendanceEntity("attendance_003", "student_001", "6A", "2025-11-14", false, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        val result = attendanceDao.getAllAttendance().first()

        // Assert
        assertEquals(3, result.size)
        assertEquals("2025-11-14", result[0].date) // Mais recente primeiro
        assertEquals("2025-11-13", result[1].date)
        assertEquals("2025-11-12", result[2].date)
    }

    /**
     * Teste: Recuperar frequência de um estudante
     * Esperado: Apenas registros desse estudante são retornados
     */
    @Test
    fun getAttendanceByStudent_filtersCorrectly() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("student_001-2025-11-14", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_001-2025-11-13", "student_001", "6A", "2025-11-13", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_002-2025-11-14", "student_002", "6A", "2025-11-14", false, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        val student1Attendance = attendanceDao.getAttendanceByStudent("student_001").first()
        val student2Attendance = attendanceDao.getAttendanceByStudent("student_002").first()

        // Assert
        assertEquals(2, student1Attendance.size)
        assertEquals(1, student2Attendance.size)
        assertTrue(student1Attendance.all { it.studentId == "student_001" })
        assertTrue(student2Attendance.all { it.studentId == "student_002" })
    }

    /**
     * Teste: Recuperar frequência de turma em data específica
     * Esperado: Apenas registros dessa turma nessa data são retornados
     */
    @Test
    fun getAttendanceForClassByDate_filtersCorrectly() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("student_001-2025-11-14", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_002-2025-11-14", "student_002", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_003-2025-11-14", "student_003", "6B", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_001-2025-11-13", "student_001", "6A", "2025-11-13", true, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        val class6A_14Nov = attendanceDao.getAttendanceForClassByDate("6A", "2025-11-14").first()
        val class6B_14Nov = attendanceDao.getAttendanceForClassByDate("6B", "2025-11-14").first()
        val class6A_13Nov = attendanceDao.getAttendanceForClassByDate("6A", "2025-11-13").first()

        // Assert
        assertEquals(2, class6A_14Nov.size)
        assertEquals(1, class6B_14Nov.size)
        assertEquals(1, class6A_13Nov.size)
    }

    /**
     * Teste: Recuperar registro por ID
     * Esperado: Registro correto é retornado
     */
    @Test
    fun getAttendanceById_returnsCorrectRecord() = runBlocking {
        // Arrange
        val attendance = AttendanceEntity(
            "student_001-2025-11-14",
            "student_001",
            "6A",
            "2025-11-14",
            true,
            false,
            System.currentTimeMillis()
        )
        attendanceDao.insertAttendance(attendance)

        // Act
        val retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")

        // Assert
        assertNotNull(retrieved)
        assertTrue(retrieved.isPresent)
        assertEquals("2025-11-14", retrieved.date)
    }

    /**
     * Teste: Recuperar registro por ID que não existe
     * Esperado: Retorna null
     */
    @Test
    fun getAttendanceById_nonExistent_returnsNull() = runBlocking {
        // Act
        val attendance = attendanceDao.getAttendanceById("non_existent_id")

        // Assert
        assertNull(attendance)
    }

    // ==================== TESTES DE ATUALIZAÇÃO ====================

    /**
     * Teste: Atualizar registro de frequência
     * Esperado: Dados são atualizados
     */
    @Test
    fun updateAttendance_updatesSuccessfully() = runBlocking {
        // Arrange
        val original = AttendanceEntity(
            "student_001-2025-11-14",
            "student_001",
            "6A",
            "2025-11-14",
            true,
            false,
            System.currentTimeMillis()
        )
        attendanceDao.insertAttendance(original)

        // Act
        val updated = original.copy(isPresent = false, isSynced = true)
        attendanceDao.updateAttendance(updated)

        // Assert
        val retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")
        assertNotNull(retrieved)
        assertFalse(retrieved.isPresent)
        assertTrue(retrieved.isSynced)
    }

    /**
     * Teste: Marcar registros como sincronizados
     * Esperado: Flag isSynced é atualizada
     */
    @Test
    fun markAsSynced_updatesFlag() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("attendance_001", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("attendance_002", "student_002", "6A", "2025-11-14", true, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        attendanceDao.markAsSynced(listOf("attendance_001", "attendance_002"))

        // Assert
        val att1 = attendanceDao.getAttendanceById("attendance_001")
        val att2 = attendanceDao.getAttendanceById("attendance_002")
        assertTrue(att1?.isSynced ?: false)
        assertTrue(att2?.isSynced ?: false)
    }

    // ==================== TESTES DE DELEÇÃO ====================

    /**
     * Teste: Deletar registro específico
     * Esperado: Registro é removido
     */
    @Test
    fun deleteAttendance_removesSuccessfully() = runBlocking {
        // Arrange
        val attendance = AttendanceEntity(
            "student_001-2025-11-14",
            "student_001",
            "6A",
            "2025-11-14",
            true,
            false,
            System.currentTimeMillis()
        )
        attendanceDao.insertAttendance(attendance)

        // Act
        attendanceDao.deleteAttendance(attendance)

        // Assert
        val retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar registro por ID
     * Esperado: Registro é removido
     */
    @Test
    fun deleteAttendanceById_removesSuccessfully() = runBlocking {
        // Arrange
        val attendance = AttendanceEntity(
            "student_001-2025-11-14",
            "student_001",
            "6A",
            "2025-11-14",
            true,
            false,
            System.currentTimeMillis()
        )
        attendanceDao.insertAttendance(attendance)

        // Act
        attendanceDao.deleteAttendanceById("student_001-2025-11-14")

        // Assert
        val retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar todos os registros
     * Esperado: Tabela fica vazia
     */
    @Test
    fun deleteAll_removesAll() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("attendance_001", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("attendance_002", "student_002", "6A", "2025-11-14", true, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        attendanceDao.deleteAll()

        // Assert
        val allAttendance = attendanceDao.getAllAttendance().first()
        assertEquals(0, allAttendance.size)
    }

    // ==================== TESTES DE SYNC ====================

    /**
     * Teste: Recuperar registros não sincronizados
     * Esperado: Apenas registros com isSynced=false são retornados
     */
    @Test
    fun getUnsyncedAttendance_returnsOnlyUnsynced() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("attendance_001", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("attendance_002", "student_002", "6A", "2025-11-14", true, true, System.currentTimeMillis()),
            AttendanceEntity("attendance_003", "student_003", "6A", "2025-11-14", false, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        val unsynced = attendanceDao.getUnsyncedAttendance()

        // Assert
        assertEquals(2, unsynced.size)
        assertTrue(unsynced.all { !it.isSynced })
        assertTrue(unsynced.any { it.id == "attendance_001" })
        assertTrue(unsynced.any { it.id == "attendance_003" })
        assertFalse(unsynced.any { it.id == "attendance_002" })
    }

    /**
     * Teste: Sincronizar registros parcialmente
     * Esperado: Apenas os selecionados são marcados como sincronizados
     */
    @Test
    fun markAsSynced_partial_onlySelectedMarked() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("attendance_001", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("attendance_002", "student_002", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("attendance_003", "student_003", "6A", "2025-11-14", false, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        attendanceDao.markAsSynced(listOf("attendance_001", "attendance_003"))

        // Assert
        val unsynced = attendanceDao.getUnsyncedAttendance()
        assertEquals(1, unsynced.size)
        assertEquals("attendance_002", unsynced[0].id)
    }

    // ==================== TESTES DE EDGE CASES ====================

    /**
     * Teste: Registros presentes vs ausentes
     * Esperado: Ambos são salvos corretamente
     */
    @Test
    fun insertAttendance_presentAndAbsent_bothSaveCorrectly() = runBlocking {
        // Arrange
        val present = AttendanceEntity("attendance_001", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis())
        val absent = AttendanceEntity("attendance_002", "student_002", "6A", "2025-11-14", false, false, System.currentTimeMillis())

        // Act
        attendanceDao.insertAttendance(present)
        attendanceDao.insertAttendance(absent)

        // Assert
        val p = attendanceDao.getAttendanceById("attendance_001")
        val a = attendanceDao.getAttendanceById("attendance_002")
        assertTrue(p?.isPresent ?: false)
        assertFalse(a?.isPresent ?: true)
    }

    /**
     * Teste: Filtro por turma com data inexistente
     * Esperado: Retorna lista vazia
     */
    @Test
    fun getAttendanceForClassByDate_noRecords_returnsEmpty() = runBlocking {
        // Arrange

        // Act
        val attendance = attendanceDao.getAttendanceForClassByDate("6Z", "2025-12-31").first()

        // Assert
        assertEquals(0, attendance.size)
    }

    /**
     * Teste: Múltiplos estudantes mesma turma mesma data
     * Esperado: Todos são recuperados e ordenados por studentId
     */
    @Test
    fun multipleStudentsSameClassDate_allRetrievedOrdered() = runBlocking {
        // Arrange
        val attendances = listOf(
            AttendanceEntity("student_003-2025-11-14", "student_003", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_001-2025-11-14", "student_001", "6A", "2025-11-14", true, false, System.currentTimeMillis()),
            AttendanceEntity("student_002-2025-11-14", "student_002", "6A", "2025-11-14", false, false, System.currentTimeMillis())
        )
        attendanceDao.insertAttendances(attendances)

        // Act
        val result = attendanceDao.getAttendanceForClassByDate("6A", "2025-11-14").first()

        // Assert
        assertEquals(3, result.size)
        // Ordenado por studentId
        assertEquals("student_001", result[0].studentId)
        assertEquals("student_002", result[1].studentId)
        assertEquals("student_003", result[2].studentId)
    }

    /**
     * Teste: Múltiplas operações em sequência
     * Esperado: Todas são executadas corretamente
     */
    @Test
    fun sequentialOperations_allSucceed() = runBlocking {
        // Arrange
        val attendance = AttendanceEntity(
            "student_001-2025-11-14",
            "student_001",
            "6A",
            "2025-11-14",
            true,
            false,
            System.currentTimeMillis()
        )

        // Act
        attendanceDao.insertAttendance(attendance) // Create
        var retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14") // Read
        assertNotNull(retrieved)

        attendanceDao.updateAttendance(attendance.copy(isPresent = false)) // Update
        retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")
        assertFalse(retrieved?.isPresent ?: true)

        attendanceDao.deleteAttendance(attendance) // Delete
        retrieved = attendanceDao.getAttendanceById("student_001-2025-11-14")
        assertNull(retrieved)

        // Assert
        assertTrue(true)
    }
}
