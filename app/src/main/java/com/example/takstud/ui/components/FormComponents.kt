@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.ui.theme.AccentBlue
import com.example.takstud.ui.theme.DarkGray
import com.example.takstud.ui.theme.ErrorRed
import com.example.takstud.ui.theme.LightGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite
import com.example.takstud.ui.theme.SuccessGreen
import com.example.takstud.ui.theme.WarningYellow

/**
 * 📝 Componentes avançados para formulários
 * Inclui inputs validados, selectors, e campos customizados
 */

/**
 * 🔤 ValidatedTextField - Campo de texto com validação em tempo real
 * @param value Valor do campo
 * @param onValueChange Callback quando o valor muda
 * @param label Rótulo do campo
 * @param placeholder Texto de placeholder
 * @param validator Função de validação que retorna mensagem de erro ou null
 * @param keyboardType Tipo de teclado
 * @param maxLength Comprimento máximo do texto
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    validator: (String) -> String? = { null },
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = Int.MAX_VALUE,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val error = validator(value)
    val hasError = error != null

    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> ErrorRed
            value.isNotEmpty() -> SuccessGreen
            else -> LightGray
        },
        label = "borderColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Rótulo
        Text(
            text = label,
            color = NavyBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )

        // Campo de texto
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = DarkGray.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            isError = hasError,
            enabled = enabled,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = {
                when {
                    isPassword -> {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Alternar visibilidade",
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    hasError -> {
                        Icon(
                            Icons.Default.Error,
                            "Erro",
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    value.isNotEmpty() && !hasError -> {
                        Icon(
                            Icons.Default.Check,
                            "Válido",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = borderColor,
                errorBorderColor = ErrorRed,
                focusedLabelColor = AccentBlue,
                cursorColor = NavyBlue
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Mensagem de erro ou contador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasError) {
                Text(
                    text = error ?: "",
                    color = ErrorRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (value.isNotEmpty()) {
                Text(
                    text = "✓ Válido",
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "${value.length}/$maxLength",
                color = DarkGray,
                fontSize = 10.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 🔢 NumericField - Campo para entrada de números com validação
 * @param value Valor numérico
 * @param onValueChange Callback quando o valor muda
 * @param label Rótulo
 * @param min Valor mínimo permitido
 * @param max Valor máximo permitido
 */
@Composable
fun NumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 100f,
    decimals: Int = 2
) {
    val validator: (String) -> String? = { text ->
        when {
            text.isEmpty() -> null
            text.toFloatOrNull() == null -> "Digite um número válido"
            text.toFloat() < min -> "Mínimo: $min"
            text.toFloat() > max -> "Máximo: $max"
            else -> null
        }
    }

    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = "Ex: 85.5",
        validator = validator,
        keyboardType = KeyboardType.Decimal,
        maxLength = 6
    )
}

/**
 * 🔐 PasswordField - Campo seguro para entrada de senha
 * @param value Valor da senha
 * @param onValueChange Callback quando a senha muda
 * @param label Rótulo
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Senha",
    modifier: Modifier = Modifier,
    minLength: Int = 6
) {
    val validator: (String) -> String? = { password ->
        when {
            password.isEmpty() -> null
            password.length < minLength -> "Mínimo $minLength caracteres"
            !password.any { it.isDigit() } -> "Deve conter número"
            !password.any { it.isUpperCase() } -> "Deve conter maiúscula"
            else -> null
        }
    }

    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = "••••••••",
        validator = validator,
        isPassword = true,
        maxLength = 20
    )
}

/**
 * ✉️ EmailField - Campo para entrada de email
 * @param value Email
 * @param onValueChange Callback
 * @param label Rótulo
 */
@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Email",
    modifier: Modifier = Modifier
) {
    val validator: (String) -> String? = { email ->
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
        when {
            email.isEmpty() -> null
            !emailRegex.matches(email) -> "Email inválido"
            else -> null
        }
    }

    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = "exemplo@email.com",
        validator = validator,
        keyboardType = KeyboardType.Email
    )
}

/**
 * 📅 DateField - Campo para entrada de data
 * @param value Data no formato DD/MM/YYYY
 * @param onValueChange Callback
 * @param label Rótulo
 */
@Composable
fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Data",
    modifier: Modifier = Modifier
) {
    val validator: (String) -> String? = { date ->
        val dateRegex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        when {
            date.isEmpty() -> null
            !dateRegex.matches(date) -> "Formato: DD/MM/YYYY"
            else -> null
        }
    }

    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = "DD/MM/YYYY",
        validator = validator,
        keyboardType = KeyboardType.Number,
        maxLength = 10
    )
}

/**
 * 🎯 SelectField - Campo de seleção com lista de opções
 * @param value Valor selecionado
 * @param options Lista de opções disponíveis
 * @param onValueChange Callback quando o valor muda
 * @param label Rótulo
 */
@Composable
fun SelectField(
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = NavyBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LightGray, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { "Selecione..." },
                    color = if (value.isEmpty()) DarkGray.copy(alpha = 0.5f) else NavyBlue,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = AccentBlue,
                    fontSize = 12.sp
                )
            }
        }

        if (isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentBlue, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(option)
                                    isExpanded = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (value == option) {
                                Icon(
                                    Icons.Default.Check,
                                    "Selecionado",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Box(modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = option,
                                color = if (value == option) SuccessGreen else NavyBlue,
                                fontSize = 14.sp,
                                fontWeight = if (value == option) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ✅ CheckboxField - Campo de checkbox customizado
 * @param value Se está marcado
 * @param onValueChange Callback quando marca/desmarca
 * @param label Rótulo/descrição
 */
@Composable
fun CheckboxField(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onValueChange(!value) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (value) SuccessGreen else PureWhite,
                    shape = RoundedCornerShape(6.dp)
                )
                .border(2.dp, if (value) SuccessGreen else LightGray, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (value) {
                Icon(
                    Icons.Default.Check,
                    "Marcado",
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = label,
            color = NavyBlue,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 🔘 RadioButtonField - Grupo de radio buttons
 * @param value Opção selecionada
 * @param options Lista de opções
 * @param onValueChange Callback
 * @param label Rótulo do grupo
 */
@Composable
fun RadioButtonField(
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = NavyBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onValueChange(option) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = PureWhite,
                            shape = RoundedCornerShape(50)
                        )
                        .border(2.dp, if (value == option) AccentBlue else LightGray, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    if (value == option) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color = AccentBlue, shape = RoundedCornerShape(50))
                        )
                    }
                }
                Text(
                    text = option,
                    color = if (value == option) AccentBlue else NavyBlue,
                    fontSize = 14.sp,
                    fontWeight = if (value == option) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 🎨 PrimaryButton - Botão principal estilo da app
 * @param text Texto do botão
 * @param onClick Callback
 * @param enabled Se está habilitado
 * @param isLoading Se está carregando
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NavyBlue,
            disabledContainerColor = LightGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = if (isLoading) "Carregando..." else text,
            color = PureWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

/**
 * 🎯 SecondaryButton - Botão secundário
 * @param text Texto do botão
 * @param onClick Callback
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PureWhite,
            disabledContainerColor = LightGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = NavyBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

/**
 * ❌ DangerButton - Botão de ação perigosa (deletar, etc)
 * @param text Texto do botão
 * @param onClick Callback
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ErrorRed
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = PureWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}