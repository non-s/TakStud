package com.example.takstud.model.schedule

import androidx.compose.ui.graphics.Color
import com.example.takstud.ui.theme.AccentBlue
import java.util.UUID

/**
 * 📚 Disciplina/Matéria
 * Representa uma disciplina lecionada na escola
 */
data class Subject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",                    // Ex: "Matemática", "Português"
    val shortName: String = "",               // Ex: "MAT", "PORT" (para grade)
    val teacherName: String = "",             // Nome do professor
    val teacherId: String = "",               // ID do professor
    val classroom: String = "",               // Sala/local (ex: "Sala 101", "Laboratório")
    val color: Long = AccentBlue.value.toLong(), // Cor identificadora (em Long para serialização)
    val weeklyHours: Int = 0,                // Carga horária semanal
    val description: String = "",             // Descrição da disciplina
    val requiredMaterials: List<String> = emptyList(), // Materiais necessários
    val isActive: Boolean = true,            // Disciplina ativa?
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Helper para obter a cor como Color do Compose
     */
    fun getComposeColor(): Color = Color(color.toULong())

    /**
     * Validação
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && shortName.isNotBlank()
    }
}
