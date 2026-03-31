package com.example.takstud.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.takstud.ui.theme.*
import com.example.takstud.util.InputValidator
import com.example.takstud.util.Result
import com.example.takstud.viewmodel.LoginViewModel

/**
 * ParentLoginScreen Premium - Login moderno para responsáveis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentLoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var ra by remember { mutableStateOf("") }
    var raError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val parentLoginState by viewModel.parentLoginState.collectAsState()

    // Observar mudanças no estado de login
    LaunchedEffect(parentLoginState) {
        when (val state = parentLoginState) {
            is Result.Success -> {
                onLoginSuccess()
                viewModel.resetLoginState()
            }
            is Result.Error -> {
                raError = state.exception.message ?: "RA não encontrado"
            }
            is Result.Loading -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AccentTeal.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
    ) {
        // Botão Voltar
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = AccentTeal
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AccentTeal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "Acesso Responsável",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Neutral900
            )

            Text(
                text = "Digite o RA do seu filho",
                style = MaterialTheme.typography.bodyLarge,
                color = Neutral500,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Card de Login
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Campo RA
                    OutlinedTextField(
                        value = ra,
                        onValueChange = {
                            ra = it
                            // Validar conforme digita
                            raError = if (it.isNotEmpty() && !InputValidator.isValidRA(it)) {
                                "RA deve ter 2-20 caracteres (letras, números, - e _)"
                            } else null
                        },
                        label = { Text("RA do Aluno") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        isError = raError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = Neutral300
                        )
                    )

                    // Mensagem de Erro
                    AnimatedVisibility(visible = raError != null) {
                        Text(
                            text = raError ?: "",
                            color = Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão Entrar
                    Button(
                        onClick = {
                            if (ra.isNotBlank() && InputValidator.isValidRA(ra)) {
                                viewModel.loginAsParent(ra)
                            } else {
                                raError = "Digite um RA válido"
                            }
                        },
                        enabled = !isLoading && ra.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentTeal
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "ENTRAR",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info
                    Text(
                        text = "📋 O RA está no cartão do aluno",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
