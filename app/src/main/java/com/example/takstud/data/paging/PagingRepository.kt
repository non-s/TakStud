package com.example.takstud.data.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.takstud.model.Student
import com.example.takstud.model.Grade
import com.example.takstud.model.Task
import com.example.takstud.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

/**
 * PagingRepository - Repositório com suporte a Paging 3 para listas grandes.
 *
 * FUNCIONALIDADES:
 * - Paginação eficiente para grandes datasets
 * - Suporte a busca dentro de páginas
 * - Configuração customizável de tamanho de página
 *
 * CONFIG PADRÃO:
 * - Page size: 20 itens
 * - Initial load size: 40 itens
 * - Prefetch distance: 10 itens
 *
 * EXEMPLO DE USO:
 * val pagingRepository = PagingRepository()
 * val students: Flow<PagingData<Student>> = pagingRepository.getStudentsPaged(studentList)
 *
 * Em ViewModel:
 * val studentsPaged = pagingRepository.getStudentsPaged(studentList)
 *     .cachedIn(viewModelScope)
 */
class PagingRepository {
    companion object {
        private const val STUDENTS_PAGE_SIZE = 20
        private const val GRADES_PAGE_SIZE = 25
        private const val TASKS_PAGE_SIZE = 15
        private const val ATTENDANCE_PAGE_SIZE = 30
    }

    /**
     * Configuração padrão para paginação.
     */
    private fun defaultPagingConfig(pageSize: Int) = PagingConfig(
        pageSize = pageSize,
        initialLoadSize = pageSize * 2,
        prefetchDistance = pageSize / 2,
        enablePlaceholders = true,
        maxSize = pageSize * 5
    )

    /**
     * Obtém estudantes de forma paginada.
     */
    fun getStudentsPaged(students: List<Student>): Flow<PagingData<Student>> {
        return Pager(
            config = defaultPagingConfig(STUDENTS_PAGE_SIZE),
            pagingSourceFactory = {
                StudentPagingSource(students)
            }
        ).flow
    }

    /**
     * Obtém estudantes paginados com busca.
     */
    fun searchStudentsPaged(students: List<Student>, query: String): Flow<PagingData<Student>> {
        return Pager(
            config = defaultPagingConfig(STUDENTS_PAGE_SIZE),
            pagingSourceFactory = {
                StudentPagingSource(students, query)
            }
        ).flow
    }

}
