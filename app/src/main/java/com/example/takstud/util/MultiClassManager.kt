package com.example.takstud.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * MultiClassManager - Gerencia múltiplas turmas por professor
 * Permite que um professor gerencie várias turmas simultaneamente
 */
class MultiClassManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "multi_class_preferences"
        private val ASSIGNED_CLASSES = stringSetPreferencesKey("assigned_classes")
        private val SELECTED_CLASS = stringSetPreferencesKey("selected_class")
    }

    /**
     * Obter todas as turmas atribuídas ao professor como Flow
     */
    fun getAssignedClassesFlow(): Flow<Set<String>> {
        return context.multiClassDataStore.data.map { preferences ->
            preferences[ASSIGNED_CLASSES] ?: emptySet()
        }
    }

    /**
     * Obter turma selecionada atualmente
     */
    fun getSelectedClassFlow(): Flow<String?> {
        return context.multiClassDataStore.data.map { preferences ->
            preferences[SELECTED_CLASS]?.firstOrNull()
        }
    }

    /**
     * Adicionar turma ao professor
     */
    suspend fun addClassToTeacher(className: String) {
        context.multiClassDataStore.edit { preferences ->
            val currentClasses = preferences[ASSIGNED_CLASSES] ?: emptySet()
            val updatedClasses = (currentClasses + className).toMutableSet()
            preferences[ASSIGNED_CLASSES] = updatedClasses
        }
    }

    /**
     * Remover turma do professor
     */
    suspend fun removeClassFromTeacher(className: String) {
        context.multiClassDataStore.edit { preferences ->
            val currentClasses = preferences[ASSIGNED_CLASSES] ?: emptySet()
            val updatedClasses = (currentClasses - className).toMutableSet()
            preferences[ASSIGNED_CLASSES] = updatedClasses
        }
    }

    /**
     * Selecionar turma atual para trabalhar
     */
    suspend fun selectClass(className: String) {
        context.multiClassDataStore.edit { preferences ->
            preferences[SELECTED_CLASS] = setOf(className)
        }
    }

    /**
     * Verificar se professor está atribuído a uma turma
     */
    suspend fun isAssignedToClass(className: String): Boolean {
        var result = false
        context.multiClassDataStore.data.collect { preferences ->
            result = (preferences[ASSIGNED_CLASSES] ?: emptySet()).contains(className)
        }
        return result
    }

    /**
     * Obter número de turmas do professor
     */
    suspend fun getClassCount(): Int {
        var result = 0
        context.multiClassDataStore.data.collect { preferences ->
            result = (preferences[ASSIGNED_CLASSES] ?: emptySet()).size
        }
        return result
    }

    /**
     * Limpar todas as turmas (logout)
     */
    suspend fun clearAllClasses() {
        context.multiClassDataStore.edit { preferences ->
            preferences.remove(ASSIGNED_CLASSES)
            preferences.remove(SELECTED_CLASS)
        }
    }

    /**
     * Sincronizar turmas do professor com Firestore
     */
    suspend fun syncClassesFromFirestore(classes: List<String>) {
        context.multiClassDataStore.edit { preferences ->
            preferences[ASSIGNED_CLASSES] = classes.toSet()
        }
    }
}

// Extensão para criar DataStore
private val Context.multiClassDataStore: androidx.datastore.core.DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
    name = "multi_class_preferences"
)

/**
 * Data class para representar uma turma
 */
data class ClassInfo(
    val id: String = "",
    val name: String = "",
    val grade: String = "", // Série/Ano
    val studentCount: Int = 0,
    val schedule: String = "",
    val isActive: Boolean = true
)
