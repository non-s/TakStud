package com.example.takstud.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

/**
 * FirestorePagingSource - Generic PagingSource for Firestore queries
 * Enables efficient pagination of large collections
 *
 * @param query The Firestore Query to paginate
 * @param pageSize Number of items per page (default: 20)
 */
class FirestorePagingSource<T : Any>(
    private val query: Query,
    private val pageSize: Int = 20,
    private val mapper: (DocumentSnapshot) -> T
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val pageIndex = params.key ?: 0
            val offset = pageIndex * pageSize

            val pageQuery = query.limit((offset + pageSize).toLong())
            val snapshot = pageQuery.get().await()
            val allDocuments = snapshot.documents
            val pageData = if (offset < allDocuments.size) {
                allDocuments.drop(offset).take(pageSize).map { mapper(it) }
            } else {
                emptyList()
            }

            LoadResult.Page(
                data = pageData,
                prevKey = if (pageIndex > 0) pageIndex - 1 else null,
                nextKey = if (pageData.size == pageSize) pageIndex + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        // Return the page index for refresh
        return state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey?.plus(1) }
    }
}
