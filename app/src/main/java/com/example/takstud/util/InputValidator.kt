package com.example.takstud.util

/**
 * Utilitário para validação de entrada de dados.
 * Fornece funções para validar campos de formulário comuns.
 */
object InputValidator {

    /**
     * Valida se uma string está vazia ou contém apenas espaços em branco.
     *
     * @param input String a ser validada
     * @return true se válida (não vazia), false caso contrário
     */
    fun isNotEmpty(input: String): Boolean {
        return input.trim().isNotEmpty()
    }

    /**
     * Valida comprimento mínimo de uma string.
     *
     * @param input String a ser validada
     * @param minLength Comprimento mínimo requerido
     * @return true se válida, false caso contrário
     */
    fun hasMinLength(input: String, minLength: Int): Boolean {
        return input.trim().length >= minLength
    }

    /**
     * Valida comprimento máximo de uma string.
     *
     * @param input String a ser validada
     * @param maxLength Comprimento máximo permitido
     * @return true se válida, false caso contrário
     */
    fun hasMaxLength(input: String, maxLength: Int): Boolean {
        return input.trim().length <= maxLength
    }

    /**
     * Valida se um RA (Registro de Aluno) está no formato correto.
     * Aceita números e caracteres alfanuméricos.
     *
     * @param ra String do RA a ser validada
     * @return true se válida, false caso contrário
     */
    fun isValidRA(ra: String): Boolean {
        if (!isNotEmpty(ra)) return false
        if (!hasMinLength(ra, 2)) return false
        if (!hasMaxLength(ra, 20)) return false
        // Apenas letras, números e alguns caracteres especiais
        return ra.matches(Regex("^[a-zA-Z0-9\\-_]*$"))
    }

    /**
     * Valida se um email está no formato correto.
     *
     * @param email String do email a ser validada
     * @return true se válida, false caso contrário
     */
    fun isValidEmail(email: String): Boolean {
        if (!isNotEmpty(email)) return false
        return email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"))
    }

    /**
     * Valida um título de tarefa/aviso.
     *
     * @param title String do título a ser validada
     * @return true se válida, false caso contrário
     */
    fun isValidTitle(title: String): Boolean {
        if (!isNotEmpty(title)) return false
        if (!hasMinLength(title, 3)) return false
        if (!hasMaxLength(title, 200)) return false
        return true
    }

    /**
     * Valida uma descrição.
     *
     * @param description String da descrição a ser validada
     * @return true se válida, false caso contrário
     */
    fun isValidDescription(description: String): Boolean {
        if (!isNotEmpty(description)) return false
        if (!hasMaxLength(description, 5000)) return false
        return true
    }

    /**
     * Valida um código de acesso de professor.
     *
     * @param code String do código a ser validada
     * @return true se válida (apenas números, 4-10 dígitos), false caso contrário
     */
    fun isValidAccessCode(code: String): Boolean {
        if (!isNotEmpty(code)) return false
        if (!hasMinLength(code, 4)) return false
        if (!hasMaxLength(code, 10)) return false
        return code.matches(Regex("^[0-9]*$"))
    }

    /**
     * Valida uma data no formato dd/MM/yyyy.
     *
     * @param dateString String da data a ser validada
     * @return true se válida, false caso contrário
     */
    fun isValidDate(dateString: String): Boolean {
        if (!isNotEmpty(dateString)) return false
        return dateString.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$"))
    }

    /**
     * Valida um score/nota (pode ser número decimal).
     *
     * @param score String da nota a ser validada
     * @return true se válida, false caso contrário
     */
    fun isValidScore(score: String): Boolean {
        if (!isNotEmpty(score)) return false
        return try {
            val value = score.toDouble()
            value >= 0.0 && value <= 100.0
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Sanitiza uma string removendo caracteres potencialmente perigosos.
     *
     * @param input String a ser sanitizada
     * @return String sanitizada
     */
    fun sanitize(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"'%;()&+]"), "")
            .take(5000) // Limita o tamanho
    }

    /**
     * Valida uma turma/classe.
     *
     * @param studentClass String da turma a ser validada
     * @return true se válida, false caso contrário
     */
    fun isValidClass(studentClass: String): Boolean {
        if (!isNotEmpty(studentClass)) return false
        if (!hasMinLength(studentClass, 2)) return false
        if (!hasMaxLength(studentClass, 50)) return false
        return true
    }
}