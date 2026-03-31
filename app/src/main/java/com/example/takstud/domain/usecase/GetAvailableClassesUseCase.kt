package com.example.takstud.domain.usecase

import com.example.takstud.data.repository.NoticeRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use Case para obter a lista de turmas disponíveis no sistema.
 * Consolida turmas provenientes de tarefas, comunicados e horários.
 */
class GetAvailableClassesUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val noticeRepository: NoticeRepository,
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(): Flow<List<String>> = combine(
        taskRepository.getTasks(),
        noticeRepository.getNotices(),
        scheduleRepository.getSchedules()
    ) { tasks, notices, schedules ->
        val classSet = mutableSetOf<String>()
        tasks.map { it.classId }.toCollection(classSet)
        notices.map { it.studentClass }.toCollection(classSet)
        schedules.map { it.studentClass }.toCollection(classSet)
        classSet.filter { it.isNotBlank() }.sorted()
    }
}
