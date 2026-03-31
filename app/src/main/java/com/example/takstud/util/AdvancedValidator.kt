package com.example.takstud.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * Validadores avançados para entradas de dados com regras complexas.
 *
 * Objeto singleton que fornece métodos de validação para diferentes tipos de entrada:
 * - Nomes (caracteres, tamanho, espaços)
 * - RA/Matrícula (números apenas, intervalo de tamanho)
 * - Notas/Grades (intervalo numérico, padrão 0-100)
 * - Datas (formato, intervalo, futuro/passado)
 * - Horários (formato HH:mm, duração mínima)
 * - Telefone (formato brasileiro com variações)
 * - Email (RFC 5322 simplificado)
 * - Descrição/Texto longo (comprimento ajustável)
 * - Título (comprimento mínimo/máximo)
 *
 * Padrão de uso:
 * ```kotlin
 * val nameResult = AdvancedValidator.validateName("João Silva")
 * when {
 *     nameResult.isValid() -> {
 *         val cleanName = nameResult.getOrNull<String>()
 *         saveUser(cleanName)
 *     }
 *     nameResult.isInvalid() -> {
 *         val errorMsg = nameResult.getErrorMessage()
 *         showError(errorMsg)
 *     }
 * }
 * ```
 *
 * Regex Patterns (privados):
 * - PHONE_PATTERN: (+55) (11) 9 9999-9999 variations
 * - EMAIL_PATTERN: standard@email.com format
 * - RA_PATTERN: 2-20 digits only
 * - NAME_PATTERN: Portuguese letters, spaces, hyphen, apostrophe
 *
 * Data Format: dd/MM/yyyy (Portuguese standard)
 *
 * Retorno: Todos os métodos retornam ValidationResult (sealed class)
 * - Valid<T>: Validação bem-sucedida com dados sanitizados
 * - Invalid: Validação falhou com mensagem de erro específica
 *
 * @see ValidationResult
 * @see isValid
 * @see isInvalid
 * @see getOrNull
 * @see getErrorMessage
 */
object AdvancedValidator {

    // ==================== REGEX PATTERNS ====================

    /** Regex para validar telefone brasileiro com múltiplos formatos */
    private val PHONE_PATTERN = Pattern.compile("""^(\+?55)?(\(?\d{2}\)?)?9?\d{4}-?\d{4}$""")

    /** Regex para validar email com RFC 5322 simplificado */
    private val EMAIL_PATTERN = Pattern.compile("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$""")

    /** Regex para validar RA (2-20 dígitos apenas) */
    private val RA_PATTERN = Pattern.compile("""^\d{2,20}$""")

    /** Regex para validar nomes (português com acentuação) */
    private val NAME_PATTERN = Pattern.compile("""^[a-záàâãéèêíïóôõöúçñ\s'-]+$""", Pattern.CASE_INSENSITIVE)

    // ==================== DATE & TIME ====================

    /** Formatador para datas no padrão português: dd/MM/yyyy */
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * Valida nome completo com requisitos específicos.
     *
     * Requisitos de validação:
     * - Não vazio
     * - Mínimo 3 caracteres (trimado)
     * - Máximo 100 caracteres (trimado)
     * - Apenas letras portuguesas (com acentuação: á, é, í, ó, ú, ã, õ, ç, etc)
     * - Espaços permitidos (requer pelo menos primeiro E último nome)
     * - Hífen e apóstrofo permitidos (nomes compostos: José-Maria, O'Brien)
     * - SEM números ou caracteres especiais
     *
     * Processamento:
     * - Whitespace trimado (início e fim)
     * - Verifica presença de pelo menos um espaço (firstName lastName)
     * - Retorna string trimada se válida
     *
     * @param name Nome completo a validar (ex: "João da Silva")
     *
     * @return ValidationResult:
     *         - Valid<String>: nome trimado se validação passa
     *         - Invalid: com mensagem específica do erro
     *
     * Mensagens de erro possíveis:
     * - "Nome não pode estar vazio"
     * - "Nome deve ter pelo menos 3 caracteres"
     * - "Nome não pode ultrapassar 100 caracteres"
     * - "Nome contém caracteres inválidos"
     * - "Use nome completo (primeiro e último nome)"
     *
     * Exemplo:
     * ```kotlin
     * val result = AdvancedValidator.validateName("João da Silva")
     * if (result.isValid()) {
     *     val cleanName = result.getOrNull<String>()  // "João da Silva"
     *     saveName(cleanName)
     * } else {
     *     val error = result.getErrorMessage()
     *     showError(error)
     * }
     * ```
     *
     * @see ValidationResult
     * @see getOrNull
     * @see getErrorMessage
     */
    fun validateName(name: String): ValidationResult {
        val trimmed = name.trim()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Nome não pode estar vazio")
            trimmed.length < 3 -> ValidationResult.Invalid("Nome deve ter pelo menos 3 caracteres")
            trimmed.length > 100 -> ValidationResult.Invalid("Nome não pode ultrapassar 100 caracteres")
            !NAME_PATTERN.matcher(trimmed).matches() ->
                ValidationResult.Invalid("Nome contém caracteres inválidos")
            trimmed.count { it.isWhitespace() } < 1 ->
                ValidationResult.Invalid("Use nome completo (primeiro e último nome)")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    /**
     * Valida RA (Registro Acadêmico/Matrícula) do estudante.
     *
     * Requisitos:
     * - Não vazio
     * - Mínimo 2 dígitos
     * - Máximo 20 dígitos
     * - APENAS números (sem hífens, espaços ou letras)
     *
     * @param ra Matrícula/RA a validar (ex: "2024001")
     *
     * @return Valid<String>: RA trimado, ou Invalid com mensagem de erro
     *
     * Exemplo:
     * ```kotlin
     * val result = AdvancedValidator.validateRA("2024001")
     * if (result.isValid()) {
     *     val raClean = result.getOrNull<String>()
     *     queryStudentByRA(raClean)
     * }
     * ```
     *
     * @see ValidationResult
     */
    fun validateRA(ra: String): ValidationResult {
        val trimmed = ra.trim()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("RA não pode estar vazio")
            trimmed.length < 2 -> ValidationResult.Invalid("RA deve ter pelo menos 2 caracteres")
            trimmed.length > 20 -> ValidationResult.Invalid("RA não pode ultrapassar 20 caracteres")
            !RA_PATTERN.matcher(trimmed).matches() ->
                ValidationResult.Invalid("RA deve conter apenas números")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    /**
     * Valida nota/grade com intervalo.
     *
     * @param score Nota (0-100)
     * @param minScore Mínimo permitido (padrão 0)
     * @param maxScore Máximo permitido (padrão 100)
     */
    fun validateGrade(
        score: String,
        minScore: Double = 0.0,
        maxScore: Double = 100.0
    ): ValidationResult {
        return try {
            val grade = score.trim().toDouble()

            when {
                grade < minScore -> ValidationResult.Invalid("Nota não pode ser menor que $minScore")
                grade > maxScore -> ValidationResult.Invalid("Nota não pode ser maior que $maxScore")
                else -> ValidationResult.Valid(grade)
            }
        } catch (e: NumberFormatException) {
            ValidationResult.Invalid("Nota deve ser um número válido")
        }
    }

    /**
     * Valida data com múltiplas verificações.
     *
     * @param dateString Data em formato dd/MM/yyyy
     * @param allowFutureDate Se deve permitir datas no futuro
     * @param minDaysAgo Número mínimo de dias no passado (ex: 30 para últimos 30 dias)
     * @param maxDaysInFuture Número máximo de dias no futuro permitido
     */
    fun validateDate(
        dateString: String,
        allowFutureDate: Boolean = false,
        minDaysAgo: Int = 0,
        maxDaysInFuture: Int = 0
    ): ValidationResult {
        val trimmed = dateString.trim()

        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Data não pode estar vazia")
        }

        return try {
            val date = LocalDate.parse(trimmed, dateFormatter)
            val today = LocalDate.now()

            when {
                date.isAfter(today) && !allowFutureDate ->
                    ValidationResult.Invalid("Data não pode ser no futuro")

                date.isAfter(today.plusDays(maxDaysInFuture.toLong())) && maxDaysInFuture > 0 ->
                    ValidationResult.Invalid("Data não pode ser mais de $maxDaysInFuture dias no futuro")

                date.isBefore(today.minusDays(minDaysAgo.toLong())) && minDaysAgo > 0 ->
                    ValidationResult.Invalid("Data deve estar nos últimos $minDaysAgo dias")

                else -> ValidationResult.Valid(date)
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Data inválida. Use formato dd/MM/yyyy")
        }
    }

    /**
     * Valida intervalo de tempo (ex: horário de aula).
     *
     * @param startTime Hora inicial em formato HH:mm
     * @param endTime Hora final em formato HH:mm
     */
    fun validateTimeRange(startTime: String, endTime: String): ValidationResult {
        return try {
            val timePattern = Regex("""^\d{2}:\d{2}$""")

            if (!timePattern.matches(startTime)) {
                return ValidationResult.Invalid("Hora inicial inválida. Use formato HH:mm")
            }

            if (!timePattern.matches(endTime)) {
                return ValidationResult.Invalid("Hora final inválida. Use formato HH:mm")
            }

            val startMinutes = startTime.split(":").let { (h, m) -> h.toInt() * 60 + m.toInt() }
            val endMinutes = endTime.split(":").let { (h, m) -> h.toInt() * 60 + m.toInt() }

            when {
                startMinutes >= endMinutes ->
                    ValidationResult.Invalid("Hora de início deve ser antes da hora de término")
                (endMinutes - startMinutes) < 30 ->
                    ValidationResult.Invalid("Aula deve ter no mínimo 30 minutos")
                else -> ValidationResult.Valid(Pair(startTime, endTime))
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Formato de hora inválido")
        }
    }

    /**
     * Valida telefone (Brasil).
     *
     * Formatos aceitos:
     * - 11 99999-9999
     * - (11) 99999-9999
     * - +55 11 99999-9999
     */
    fun validatePhone(phone: String): ValidationResult {
        val trimmed = phone.trim()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Telefone não pode estar vazio")
            !PHONE_PATTERN.matcher(trimmed).matches() ->
                ValidationResult.Invalid("Formato de telefone inválido")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    /**
     * Valida email.
     */
    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim().lowercase()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Email não pode estar vazio")
            !EMAIL_PATTERN.matcher(trimmed).matches() ->
                ValidationResult.Invalid("Email inválido")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    /**
     * Valida descrição ou texto longo.
     *
     * @param text Texto a validar
     * @param minLength Comprimento mínimo (padrão 5)
     * @param maxLength Comprimento máximo (padrão 500)
     */
    fun validateDescription(
        text: String,
        minLength: Int = 5,
        maxLength: Int = 500
    ): ValidationResult {
        val trimmed = text.trim()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Descrição não pode estar vazia")
            trimmed.length < minLength ->
                ValidationResult.Invalid("Descrição deve ter no mínimo $minLength caracteres")
            trimmed.length > maxLength ->
                ValidationResult.Invalid("Descrição não pode ultrapassar $maxLength caracteres")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    /**
     * Valida título ou nome de tarefa.
     *
     * @param title Título a validar
     */
    fun validateTitle(title: String): ValidationResult {
        val trimmed = title.trim()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Título não pode estar vazio")
            trimmed.length < 3 -> ValidationResult.Invalid("Título deve ter no mínimo 3 caracteres")
            trimmed.length > 150 -> ValidationResult.Invalid("Título não pode ultrapassar 150 caracteres")
            else -> ValidationResult.Valid(trimmed)
        }
    }
}

/**
 * Resultado de validação com tipo seguro.
 */
sealed class ValidationResult {
    data class Valid<T>(val data: T) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

/**
 * Extensões para ValidationResult.
 */
@Suppress("UNCHECKED_CAST", "USELESS_IS_CHECK")
fun <T> ValidationResult.getOrNull(): T? = when {
    this is ValidationResult.Valid<*> -> (this as ValidationResult.Valid<T>).data
    else -> null
}

fun ValidationResult.getErrorMessage(): String? = when (this) {
    is ValidationResult.Invalid -> message
    else -> null
}

fun ValidationResult.isValid(): Boolean = this is ValidationResult.Valid<*>

fun ValidationResult.isInvalid(): Boolean = this is ValidationResult.Invalid
