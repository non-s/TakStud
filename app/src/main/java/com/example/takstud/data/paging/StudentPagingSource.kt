package com.example.takstud.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.takstud.model.Student

/**
 * StudentPagingSource - Fonte de dados paginada para estudantes.
 *
 * FUNCIONALIDADES:
 * - Carregamento incremental de estudantes
 * - Suporte a busca e filtros
 * - Tratamento de erros e fallback
 *
 * PADRÃO:
 * - Page size: 20 itens por página
 * - Initial load size: 40 itens
 *
 * NOTA: Implementação base sem dependências externas
 * Em produção, integrar com Firestore e Room
 */
class StudentPagingSource(
    private val students: List<Student>,
    private val searchQuery: String = ""
) : PagingSource<Int, Student>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Student> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // Filtra por busca se necessário
            val filtered: List<Student> = if (searchQuery.isNotEmpty()) {
                students.filter {
                    it.name?.contains(searchQuery, ignoreCase = true) ?: false
                }
            } else {
                students
            }

            // Aplica paginação
            val pagedStudents = filtered
                .drop(page * pageSize)
                .take(pageSize)

            LoadResult.Page(
                data = pagedStudents,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (pagedStudents.count() < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Student>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
