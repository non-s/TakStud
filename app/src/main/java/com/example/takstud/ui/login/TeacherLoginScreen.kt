package com.example.takstud.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.takstud.ui.theme.*
import com.example.takstud.util.Result
import com.example.takstud.viewmodel.LoginViewModel

/**
 * TeacherLoginScreen Premium - Login moderno para professores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var codigoAcesso by remember { mutableStateOf("") }
    var codigoError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val teacherLoginState by viewModel.teacherLoginState.collectAsState()

    // Observar mudanças no estado de login
    LaunchedEffect(teacherLoginState) {
        when (teacherLoginState) {
            is Result.Success -> {
                onLoginSuccess()
                viewModel.resetLoginState()
            }
            is Result.Error -> {
                codigoError = (teacherLoginState as Result.Error).exception.message
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
                        PrimaryBlue.copy(alpha = 0.05f),
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
                tint = PrimaryBlue
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
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "Acesso Professor",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Neutral900
            )

            Text(
                text = "Digite seu código de acesso",
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
                    // Campo Código
                    OutlinedTextField(
                        value = codigoAcesso,
                        onValueChange = {
                            codigoAcesso = it
                            codigoError = null
                        },
                        label = { Text("Código de Acesso") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLoading,
                        singleLine = true,
                        isError = codigoError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Neutral300
                        )
                    )

                    // Mensagem de Erro
                    AnimatedVisibility(visible = codigoError != null) {
                        Text(
                            text = codigoError ?: "",
                            color = Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão Entrar
                    Button(
                        onClick = {
                            if (codigoAcesso.isNotBlank()) {
                                viewModel.loginAsTeacher(codigoAcesso)
                            } else {
                                codigoError = "Digite o código de acesso"
                            }
                        },
                        enabled = !isLoading && codigoAcesso.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
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
                        text = "💡 Peça o código ao administrador",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
