package com.example.takstud.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.takstud.data.local.AppDatabase
import com.example.takstud.data.local.entity.StudentEntity
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
 * Testes abrangentes para [StudentDao] usando Room in-memory database.
 *
 * Cobre:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Queries com filtros
 * - Batch operations
 * - Sync status management
 * - Edge cases
 *
 * @see StudentDao
 * @see StudentEntity
 */
@RunWith(AndroidJUnit4::class)
class StudentDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var studentDao: StudentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        studentDao = database.studentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== TESTES DE CRIAÇÃO ====================

    /**
     * Teste: Inserir um novo estudante
     * Esperado: Estudante é salvo com sucesso
     */
    @Test
    fun insertStudent_savesSuccessfully() = runBlocking {
        // Arrange
        val student = StudentEntity(
            id = "student_001",
            ra = "2024001",
            name = "João Silva",
            studentClass = "6A",
            email = "joao@email.com",
            isSynced = false,
            createdAt = System.currentTimeMillis()
        )

        // Act
        studentDao.insertStudent(student)

        // Assert
        val retrievedStudent = studentDao.getStudentById("student_001")
        assertNotNull(retrievedStudent)
        assertEquals("João Silva", retrievedStudent.name)
    }

    /**
     * Teste: Inserir múltiplos estudantes em batch
     * Esperado: Todos são salvos
     */
    @Test
    fun insertStudents_batch_savesAll() = runBlocking {
        // Arrange
        val students = listOf(
            StudentEntity("student_001", "2024001", "João", "6A", "joao@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_002", "2024002", "Maria", "6A", "maria@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_003", "2024003", "Pedro", "6B", "pedro@email.com", false, System.currentTimeMillis())
        )

        // Act
        studentDao.insertStudents(students)

        // Assert
        val allStudents = studentDao.getAllStudents().first()
        assertEquals(3, allStudents.size)
    }

    /**
     * Teste: Inserir estudante com ID duplicado
     * Esperado: OnConflictStrategy.REPLACE substitui o existente
     */
    @Test
    fun insertStudent_withDuplicateId_replaces() = runBlocking {
        // Arrange
        val student1 = StudentEntity(
            id = "student_001",
            ra = "2024001",
            name = "João Original",
            studentClass = "6A",
            email = "joao@email.com",
            isSynced = false,
            createdAt = System.currentTimeMillis()
        )
        val student2 = StudentEntity(
            id = "student_001",
            ra = "2024001",
            name = "João Updated",
            studentClass = "6B",
            email = "joao.new@email.com",
            isSynced = true,
            createdAt = System.currentTimeMillis()
        )

        // Act
        studentDao.insertStudent(student1)
        studentDao.insertStudent(student2)

        // Assert
        val retrievedStudent = studentDao.getStudentById("student_001")
        assertEquals("João Updated", retrievedStudent?.name)
        assertEquals("6B", retrievedStudent?.studentClass)
    }

    // ==================== TESTES DE LEITURA ====================

    /**
     * Teste: Recuperar todos os estudantes
     * Esperado: Flow emite lista completa
     */
    @Test
    fun getAllStudents_returnsAllInOrder() = runBlocking {
        // Arrange
        val students = listOf(
            StudentEntity("student_001", "2024001", "Alice", "6A", "alice@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_002", "2024002", "Bob", "6A", "bob@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_003", "2024003", "Charlie", "6B", "charlie@email.com", false, System.currentTimeMillis())
        )
        studentDao.insertStudents(students)

        // Act
        val result = studentDao.getAllStudents().first()

        // Assert
        assertEquals(3, result.size)
        assertEquals("Alice", result[0].name) // Ordenado por nome
        assertEquals("Bob", result[1].name)
        assertEquals("Charlie", result[2].name)
    }

    /**
     * Teste: Recuperar estudantes por turma
     * Esperado: Apenas estudantes da turma são retornados
     */
    @Test
    fun getStudentsByClass_filtersCorrectly() = runBlocking {
        // Arrange
        val students = listOf(
            StudentEntity("student_001", "2024001", "João", "6A", "joao@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_002", "2024002", "Maria", "6A", "maria@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_003", "2024003", "Pedro", "6B", "pedro@email.com", false, System.currentTimeMillis())
        )
        studentDao.insertStudents(students)

        // Act
        val class6AStudents = studentDao.getStudentsByClass("6A").first()
        val class6BStudents = studentDao.getStudentsByClass("6B").first()

        // Assert
        assertEquals(2, class6AStudents.size)
        assertEquals(1, class6BStudents.size)
        assertTrue(class6AStudents.all { it.studentClass == "6A" })
        assertTrue(class6BStudents.all { it.studentClass == "6B" })
    }

    /**
     * Teste: Recuperar estudante por ID
     * Esperado: Estudante correto é retornado
     */
    @Test
    fun getStudentById_returnsCorrectStudent() = runBlocking {
        // Arrange
        val student = StudentEntity(
            "student_001",
            "2024001",
            "João Silva",
            "6A",
            "joao@email.com",
            false,
            System.currentTimeMillis()
        )
        studentDao.insertStudent(student)

        // Act
        val retrieved = studentDao.getStudentById("student_001")

        // Assert
        assertNotNull(retrieved)
        assertEquals("João Silva", retrieved.name)
        assertEquals("2024001", retrieved.ra)
    }

    /**
     * Teste: Recuperar estudante por ID que não existe
     * Esperado: Retorna null
     */
    @Test
    fun getStudentById_nonExistent_returnsNull() = runBlocking {
        // Act
        val student = studentDao.getStudentById("non_existent_id")

        // Assert
        assertNull(student)
    }

    /**
     * Teste: Recuperar estudante por RA
     * Esperado: Estudante correto é retornado
     */
    @Test
    fun getStudentByRa_returnsCorrectStudent() = runBlocking {
        // Arrange
        val student = StudentEntity(
            "student_001",
            "2024001",
            "João Silva",
            "6A",
            "joao@email.com",
            false,
            System.currentTimeMillis()
        )
        studentDao.insertStudent(student)

        // Act
        val retrieved = studentDao.getStudentByRa("2024001")

        // Assert
        assertNotNull(retrieved)
        assertEquals("João Silva", retrieved.name)
        assertEquals("student_001", retrieved.id)
    }

    /**
     * Teste: Recuperar estudante por RA que não existe
     * Esperado: Retorna null
     */
    @Test
    fun getStudentByRa_nonExistent_returnsNull() = runBlocking {
        // Act
        val student = studentDao.getStudentByRa("9999999")

        // Assert
        assertNull(student)
    }

    // ==================== TESTES DE ATUALIZAÇÃO ====================

    /**
     * Teste: Atualizar estudante existente
     * Esperado: Dados são atualizados
     */
    @Test
    fun updateStudent_updatesSuccessfully() = runBlocking {
        // Arrange
        val original = StudentEntity(
            "student_001",
            "2024001",
            "João Original",
            "6A",
            "joao@email.com",
            false,
            System.currentTimeMillis()
        )
        studentDao.insertStudent(original)

        // Act
        val updated = original.copy(
            name = "João Updated",
            studentClass = "6B",
            email = "joao.new@email.com"
        )
        studentDao.updateStudent(updated)

        // Assert
        val retrieved = studentDao.getStudentById("student_001")
        assertNotNull(retrieved)
        assertEquals("João Updated", retrieved.name)
        assertEquals("6B", retrieved.studentClass)
        assertEquals("joao.new@email.com", retrieved.email)
    }

    /**
     * Teste: Marcar estudantes como sincronizados
     * Esperado: Flag isSynced é atualizada
     */
    @Test
    fun markAsSynced_updatesFlag() = runBlocking {
        // Arrange
        val students = listOf(
            StudentEntity("student_001", "2024001", "João", "6A", "joao@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_002", "2024002", "Maria", "6A", "maria@email.com", false, System.currentTimeMillis())
        )
        studentDao.insertStudents(students)

        // Act
        studentDao.markAsSynced(listOf("student_001", "student_002"))

        // Assert
        val student1 = studentDao.getStudentById("student_001")
        val student2 = studentDao.getStudentById("student_002")
        assertTrue(student1?.isSynced ?: false)
        assertTrue(student2?.isSynced ?: false)
    }

    // ==================== TESTES DE DELEÇÃO ====================

    /**
     * Teste: Deletar estudante específico
     * Esperado: Estudante é removido
     */
    @Test
    fun deleteStudent_removesSuccessfully() = runBlocking {
        // Arrange
        val student = StudentEntity(
            "student_001",
            "2024001",
            "João Silva",
            "6A",
            "joao@email.com",
            false,
            System.currentTimeMillis()
        )
        studentDao.insertStudent(student)

        // Act
        studentDao.deleteStudent(student)

        // Assert
        val retrieved = studentDao.getStudentById("student_001")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar estudante por ID
     * Esperado: Estudante é removido
     */
    @Test
    fun deleteStudentById_removesSuccessfully() = runBlocking {
        // Arrange
        val student = StudentEntity(
            "student_001",
            "2024001",
            "João Silva",
            "6A",
            "joao@email.com",
            false,
            System.currentTimeMillis()
        )
        studentDao.insertStudent(student)

        // Act
        studentDao.deleteStudentById("student_001")

        // Assert
        val retrieved = studentDao.getStudentById("student_001")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar todos os estudantes
     * Esperado: Tabela fica vazia
     */
    @Test
    fun deleteAll_removesAll() = runBlocking {
        // Arrange
        val students = listOf(
            StudentEntity("student_001", "2024001", "João", "6A", "joao@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_002", "2024002", "Maria", "6A", "maria@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_003", "2024003", "Pedro", "6B", "pedro@email.com", false, System.currentTimeMillis())
        )
        studentDao.insertStudents(students)

        // Act
        studentDao.deleteAll()

        // Assert
        val allStudents = studentDao.getAllStudents().first()
        assertEquals(0, allStudents.size)
    }

    // ==================== TESTES DE SYNC ====================

    /**
     * Teste: Recuperar estudantes não sincronizados
     * Esperado: Apenas estudantes com isSynced=false são retornados
     */
    @Test
    fun getUnsyncedStudents_returnsOnlyUnsynced() = runBlocking {
        // Arrange
        val students = listOf(
            StudentEntity("student_001", "2024001", "João", "6A", "joao@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_002", "2024002", "Maria", "6A", "maria@email.com", true, System.currentTimeMillis()),
            StudentEntity("student_003", "2024003", "Pedro", "6B", "pedro@email.com", false, System.currentTimeMillis())
        )
        studentDao.insertStudents(students)

        // Act
        val unsynced = studentDao.getUnsyncedStudents()

        // Assert
        assertEquals(2, unsynced.size)
        assertTrue(unsynced.all { !it.isSynced })
        assertTrue(unsynced.any { it.id == "student_001" })
        assertTrue(unsynced.any { it.id == "student_003" })
        assertFalse(unsynced.any { it.id == "student_002" })
    }

    /**
     * Teste: Sincronizar estudantes parcialmente
     * Esperado: Apenas os selecionados são marcados como sincronizados
     */
    @Test
    fun markAsSynced_partial_onlySelectedMarked() = runBlocking {
        // Arrange
        val students = listOf(
            StudentEntity("student_001", "2024001", "João", "6A", "joao@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_002", "2024002", "Maria", "6A", "maria@email.com", false, System.currentTimeMillis()),
            StudentEntity("student_003", "2024003", "Pedro", "6B", "pedro@email.com", false, System.currentTimeMillis())
        )
        studentDao.insertStudents(students)

        // Act
        studentDao.markAsSynced(listOf("student_001", "student_003"))

        // Assert
        val unsynced = studentDao.getUnsyncedStudents()
        assertEquals(1, unsynced.size)
        assertEquals("student_002", unsynced[0].id)
    }

    // ==================== TESTES DE EDGE CASES ====================

    /**
     * Teste: Inserir estudante com campos vazios
     * Esperado: Ainda assim é salvo (validação é em outro lugar)
     */
    @Test
    fun insertStudent_withEmptyFields_stillSaves() = runBlocking {
        // Arrange
        val student = StudentEntity(
            id = "student_001",
            ra = "",
            name = "",
            studentClass = "",
            email = "",
            isSynced = false,
            createdAt = System.currentTimeMillis()
        )

        // Act
        studentDao.insertStudent(student)

        // Assert
        val retrieved = studentDao.getStudentById("student_001")
        assertNotNull(retrieved)
        assertEquals("", retrieved.name)
    }

    /**
     * Teste: Obter estudantes de turma vazia
     * Esperado: Retorna lista vazia
     */
    @Test
    fun getStudentsByClass_emptyClass_returnsEmpty() = runBlocking {
        // Arrange - Nenhum estudante inserido

        // Act
        val students = studentDao.getStudentsByClass("6Z").first()

        // Assert
        assertEquals(0, students.size)
    }

    /**
     * Teste: Atualizar estudante que não existe
     * Esperado: Não causa erro, mas também não atualiza nada
     */
    @Test
    fun updateStudent_nonExistent_noError() = runBlocking {
        // Arrange
        val student = StudentEntity(
            "non_existent_id",
            "9999999",
            "Ghost Student",
            "6Z",
            "ghost@email.com",
            false,
            System.currentTimeMillis()
        )

        // Act
        studentDao.updateStudent(student) // Should not throw

        // Assert
        val retrieved = studentDao.getStudentById("non_existent_id")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar estudante que não existe
     * Esperado: Não causa erro
     */
    @Test
    fun deleteStudentById_nonExistent_noError() = runBlocking {
        // Act
        studentDao.deleteStudentById("non_existent_id") // Should not throw

        // Assert - Nada para validar além de não lançar exceção
    }

    /**
     * Teste: Filtro case-sensitive em RA
     * Esperado: RA é tratado como string exata
     */
    @Test
    fun getStudentByRa_caseSensitive_exact() = runBlocking {
        // Arrange
        val student = StudentEntity(
            "student_001",
            "2024001",
            "João",
            "6A",
            "joao@email.com",
            false,
            System.currentTimeMillis()
        )
        studentDao.insertStudent(student)

        // Act
        val found = studentDao.getStudentByRa("2024001")
        val notFound = studentDao.getStudentByRa("2024002")

        // Assert
        assertNotNull(found)
        assertNull(notFound)
    }

    /**
     * Teste: Múltiplas operações em sequência
     * Esperado: Todas são executadas corretamente
     */
    @Test
    fun sequentialOperations_allSucceed() = runBlocking {
        // Arrange
        val student = StudentEntity(
            "student_001",
            "2024001",
            "João",
            "6A",
            "joao@email.com",
            false,
            System.currentTimeMillis()
        )

        // Act
        studentDao.insertStudent(student) // Create
        var retrieved = studentDao.getStudentById("student_001") // Read
        assertNotNull(retrieved)

        studentDao.updateStudent(student.copy(name = "João Updated")) // Update
        retrieved = studentDao.getStudentById("student_001")
        assertEquals("João Updated", retrieved?.name)

        studentDao.deleteStudent(student) // Delete
        retrieved = studentDao.getStudentById("student_001")
        assertNull(retrieved)

        // Assert - Tudo passou nas etapas anteriores
        assertTrue(true)
    }
}
