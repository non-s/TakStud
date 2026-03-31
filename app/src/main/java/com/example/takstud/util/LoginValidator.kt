package com.example.takstud.util

/**
 * LoginValidator - Utilitário para validação de dados de login
 * Implementa regras de validação para RA e código de acesso
 */
object LoginValidator {

    /**
     * Validação de RA (Registro do Aluno)
     * Regras:
     * - Não pode estar vazio
     * - Deve ter entre 2 e 20 caracteres
     * - Pode conter: letras, números, hífens e underscores
     *
     * @param ra O RA do aluno
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateRA(ra: String): Pair<Boolean, String> {
        return when {
            ra.isBlank() -> false to "RA não pode estar vazio"
            ra.length < 2 -> false to "RA deve ter pelo menos 2 caracteres"
            ra.length > 20 -> false to "RA não pode exceder 20 caracteres"
            !ra.matches(Regex("^[a-zA-Z0-9_-]+$")) -> false to "RA contém caracteres inválidos. Use apenas letras, números, hífens e underscores"
            else -> true to ""
        }
    }

    /**
     * Validação de código de acesso (Professor)
     * Regras:
     * - Não pode estar vazio
     * - Deve ter entre 6 e 20 dígitos
     * - Deve conter apenas números
     *
     * @param code O código de acesso
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateAccessCode(code: String): Pair<Boolean, String> {
        return when {
            code.isBlank() -> false to "Código não pode estar vazio"
            code.length < 6 -> false to "Código deve ter pelo menos 6 dígitos"
            code.length > 20 -> false to "Código não pode exceder 20 dígitos"
            !code.matches(Regex("^\\d+$")) -> false to "Código deve conter apenas números"
            else -> true to ""
        }
    }

    /**
     * Validação de email (futuro uso)
     *
     * @param email O email a validar
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateEmail(email: String): Pair<Boolean, String> {
        return when {
            email.isBlank() -> false to "Email não pode estar vazio"
            !email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> false to "Email inválido"
            else -> true to ""
        }
    }

    /**
     * Validação de senha (futuro uso)
     * Regras:
     * - Mínimo 8 caracteres
     * - Deve conter letra maiúscula
     * - Deve conter letra minúscula
     * - Deve conter número
     *
     * @param password A senha a validar
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validatePassword(password: String): Pair<Boolean, String> {
        return when {
            password.length < 8 -> false to "Senha deve ter pelo menos 8 caracteres"
            !password.any { it.isUpperCase() } -> false to "Senha deve conter pelo menos uma letra maiúscula"
            !password.any { it.isLowerCase() } -> false to "Senha deve conter pelo menos uma letra minúscula"
            !password.any { it.isDigit() } -> false to "Senha deve conter pelo menos um número"
            else -> true to ""
        }
    }

    /**
     * Validação de nome de turma/classe
     *
     * @param className O nome da classe
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateClassName(className: String): Pair<Boolean, String> {
        return when {
            className.isBlank() -> false to "Nome da turma não pode estar vazio"
            className.length > 50 -> false to "Nome da turma não pode exceder 50 caracteres"
            else -> true to ""
        }
    }

    /**
     * Validação de data (formato DD/MM/YYYY)
     *
     * @param date A data em formato DD/MM/YYYY
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateDate(date: String): Pair<Boolean, String> {
        return when {
            date.isBlank() -> false to "Data não pode estar vazia"
            !date.matches(Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/\\d{4}$")) -> false to "Data deve estar no formato DD/MM/YYYY"
            else -> true to ""
        }
    }

    /**
     * Validação de nota (0-10)
     *
     * @param grade A nota
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateGrade(grade: String): Pair<Boolean, String> {
        return when {
            grade.isBlank() -> false to "Nota não pode estar vazia"
            !grade.matches(Regex("^(10|[0-9])(\\.[0-9]{1,2})?$")) -> false to "Nota deve estar entre 0 e 10"
            else -> {
                val gradeValue = grade.toDoubleOrNull() ?: return false to "Nota inválida"
                when {
                    gradeValue < 0 || gradeValue > 10 -> false to "Nota deve estar entre 0 e 10"
                    else -> true to ""
                }
            }
        }
    }
}
