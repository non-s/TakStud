package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.EventRepository
import com.example.takstud.model.EventCalendar
import com.example.takstud.model.EventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * CalendarViewModel - Gerencia estado da Agenda Digital.
 *
 * Funcionalidades:
 * - Visualização de eventos por mês/dia
 * - Criação e edição de eventos
 * - Filtragem por tipo e turma
 * - Gerenciamento de participantes
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Estado do calendário
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    // Filtros
    private val _selectedEventType = MutableStateFlow<EventType?>(null)
    val selectedEventType: StateFlow<EventType?> = _selectedEventType.asStateFlow()

    private val _selectedClass = MutableStateFlow<String?>(null)
    val selectedClass: StateFlow<String?> = _selectedClass.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Todos os eventos
    val allEvents: StateFlow<List<EventCalendar>> = eventRepository.getEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Eventos do mês atual
    val monthEvents: StateFlow<List<EventCalendar>> =
        MutableStateFlow(emptyList<EventCalendar>()).apply {
            viewModelScope.launch {
                selectedDate.collect { calendar ->
                    eventRepository.getEventsByMonth(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)
                    ).collect { events ->
                        value = events
                    }
                }
            }
        }

    // Eventos do dia selecionado
    val dayEvents: StateFlow<List<EventCalendar>> =
        MutableStateFlow(emptyList<EventCalendar>()).apply {
            viewModelScope.launch {
                selectedDate.collect { calendar ->
                    val dateStr = dateFormat.format(calendar.time)
                    eventRepository.getEventsByDate(dateStr).collect { events ->
                        value = events
                    }
                }
            }
        }

    // Próximos eventos
    val upcomingEvents: StateFlow<List<EventCalendar>> =
        eventRepository.getUpcomingEvents()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado de UI
    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Idle)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Evento em edição
    private val _selectedEvent = MutableStateFlow<EventCalendar?>(null)
    val selectedEvent: StateFlow<EventCalendar?> = _selectedEvent.asStateFlow()

    /**
     * Define o usuário atual
     */
    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
    }

    /**
     * Seleciona uma data
     */
    fun selectDate(calendar: Calendar) {
        _selectedDate.value = calendar
    }

    /**
     * Navega para o próximo mês
     */
    fun nextMonth() {
        val newCalendar = _selectedDate.value.clone() as Calendar
        newCalendar.add(Calendar.MONTH, 1)
        _selectedDate.value = newCalendar
    }

    /**
     * Navega para o mês anterior
     */
    fun previousMonth() {
        val newCalendar = _selectedDate.value.clone() as Calendar
        newCalendar.add(Calendar.MONTH, -1)
        _selectedDate.value = newCalendar
    }

    /**
     * Navega para a próxima semana
     */
    fun nextWeek() {
        val newCalendar = _selectedDate.value.clone() as Calendar
        newCalendar.add(Calendar.WEEK_OF_YEAR, 1)
        _selectedDate.value = newCalendar
    }

    /**
     * Navega para a semana anterior
     */
    fun previousWeek() {
        val newCalendar = _selectedDate.value.clone() as Calendar
        newCalendar.add(Calendar.WEEK_OF_YEAR, -1)
        _selectedDate.value = newCalendar
    }

    /**
     * Volta para hoje
     */
    fun goToToday() {
        _selectedDate.value = Calendar.getInstance()
    }

    /**
     * Altera o modo de visualização
     */
    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    /**
     * Filtra por tipo de evento
     */
    fun filterByEventType(type: EventType?) {
        _selectedEventType.value = type
    }

    /**
     * Filtra por turma
     */
    fun filterByClass(classId: String?) {
        _selectedClass.value = classId
    }

    /**
     * Seleciona evento para visualização/edição
     */
    fun selectEvent(event: EventCalendar?) {
        _selectedEvent.value = event
    }

    /**
     * Cria novo evento
     */
    fun createEvent(event: EventCalendar) {
        viewModelScope.launch {
            _uiState.value = CalendarUiState.Loading
            val result = eventRepository.saveEvent(event)
            _uiState.value = if (result.isSuccess) {
                CalendarUiState.Success("Evento criado com sucesso")
            } else {
                CalendarUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao criar evento")
            }
        }
    }

    /**
     * Atualiza evento existente
     */
    fun updateEvent(event: EventCalendar) {
        viewModelScope.launch {
            _uiState.value = CalendarUiState.Loading
            val result = eventRepository.saveEvent(event)
            _uiState.value = if (result.isSuccess) {
                CalendarUiState.Success("Evento atualizado com sucesso")
            } else {
                CalendarUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao atualizar evento")
            }
        }
    }

    /**
     * Deleta evento
     */
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = CalendarUiState.Loading
            val result = eventRepository.deleteEvent(eventId)
            _uiState.value = if (result.isSuccess) {
                _selectedEvent.value = null
                CalendarUiState.Success("Evento deletado com sucesso")
            } else {
                CalendarUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao deletar evento")
            }
        }
    }

    /**
     * Adiciona participante ao evento
     */
    fun addParticipant(eventId: String, userId: String) {
        viewModelScope.launch {
            eventRepository.addParticipant(eventId, userId)
        }
    }

    /**
     * Remove participante do evento
     */
    fun removeParticipant(eventId: String, userId: String) {
        viewModelScope.launch {
            eventRepository.removeParticipant(eventId, userId)
        }
    }

    /**
     * Obtém eventos de uma turma
     */
    fun getClassEvents(classId: String): StateFlow<List<EventCalendar>> {
        return eventRepository.getEventsByClass(classId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /**
     * Obtém eventos do usuário
     */
    fun getUserEvents(userId: String): StateFlow<List<EventCalendar>> {
        return eventRepository.getEventsByUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /**
     * Limpa o estado de UI
     */
    fun clearUiState() {
        _uiState.value = CalendarUiState.Idle
    }

    /**
     * Verifica se uma data tem eventos
     */
    fun hasEvents(date: Calendar): Boolean {
        val dateStr = dateFormat.format(date.time)
        return monthEvents.value.any { it.date == dateStr }
    }

    /**
     * Obtém contagem de eventos em uma data
     */
    fun getEventCount(date: Calendar): Int {
        val dateStr = dateFormat.format(date.time)
        return monthEvents.value.count { it.date == dateStr }
    }
}

/**
 * Modos de visualização do calendário
 */
enum class CalendarViewMode {
    MONTH,  // Visualização mensal
    WEEK,   // Visualização semanal
    DAY,    // Visualização diária
    AGENDA  // Lista de eventos
}

/**
 * Estados de UI do calendário
 */
sealed class CalendarUiState {
    object Idle : CalendarUiState()
    object Loading : CalendarUiState()
    data class Success(val message: String) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}
