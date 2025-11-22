package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.NoticeRepository
import com.example.takstud.model.Notice
import com.example.takstud.model.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    val notices: StateFlow<List<Notice>> = noticeRepository.getNotices()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getNoticesForStudent(student: Student): StateFlow<List<Notice>> {
        return notices.map { list ->
            list.filter { it.studentClass == student.studentClass || it.studentClass == "ALL" }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun saveNotice(notice: Notice, onComplete: () -> Unit) {
        noticeRepository.saveNotice(notice, onComplete)
    }

    fun deleteNotice(notice: Notice) {
        noticeRepository.deleteNotice(notice)
    }
}
