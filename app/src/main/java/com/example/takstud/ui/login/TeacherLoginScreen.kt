package com.example.takstud.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.takstud.util.Result
import com.example.takstud.viewmodel.LoginViewModel

/**
 * Tela de login para professors.
 * Solicita o código de acesso do professor.
 *
 * @param viewModel LoginViewModel para gerenciar o estado
 * @param onLoginSuccess Callback quando o login é bem-sucedido
 * @param onBackClick Callback quando clica em voltar
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login - Professor") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Titulo
            Text(
                "Acesso Professor",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Descricao
            Text(
                "Digite seu código de acesso para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Campo Código
            OutlinedTextField(
                value = codigoAcesso,
                onValueChange = {
                    codigoAcesso = it
                    codigoError = null  // Limpar erro ao digitar
                },
                label = { Text("Código de Acesso") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading,
                singleLine = true,
                isError = codigoError != null
            )

            // Mensagem de Erro
            if (codigoError != null) {
                Text(
                    codigoError!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
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
                    .height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("ENTRAR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info
            Text(
                "Peça o código ao administrador",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
