package com.example.takstud.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.takstud.util.InputValidator
import com.example.takstud.util.Result
import com.example.takstud.viewmodel.LoginViewModel

/**
 * Admin access code login screen for teachers.
 * Integra com LoginViewModel para validar código de acesso.
 *
 * @param modifier Composable modifier
 * @param viewModel LoginViewModel para gerenciar autenticação do professor
 * @param onAdminLoginSuccess Callback quando professor faz login com sucesso
 */
@Composable
fun AdminLoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onAdminLoginSuccess: () -> Unit
) {
    var adminCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loginState by viewModel.teacherLoginState.collectAsState()

    // Handle login result
    LaunchedEffect(loginState) {
        when (loginState) {
            is Result.Success -> {
                if ((loginState as Result.Success).data) {
                    onAdminLoginSuccess()
                    viewModel.resetLoginState()
                } else {
                    codeError = "Código de acesso incorreto"
                }
            }
            is Result.Error -> {
                codeError = (loginState as Result.Error).exception.message
            }
            is Result.Loading -> {}
        }
    }
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.admin_code_prompt), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        // Código de acesso com validação em tempo real
        OutlinedTextField(
            value = adminCode,
            onValueChange = {
                adminCode = it
                // Validar conforme digita
                codeError = if (it.isNotEmpty() && !InputValidator.isValidAccessCode(it)) {
                    "Código deve ter 4-10 dígitos"
                } else null
            },
            label = { Text(stringResource(R.string.access_code_hint)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = codeError != null,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Mensagem de erro de validação
        if (codeError != null) {
            Text(
                codeError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Botão de login com validação
        Button(
            onClick = {
                // Validar formato do código
                if (!InputValidator.isValidAccessCode(adminCode)) {
                    codeError = "Código deve ter 4-10 dígitos"
                } else {
                    // Validar código com o ViewModel
                    viewModel.loginAsTeacher(adminCode)
                }
            },
            enabled = codeError == null && adminCode.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.login))
            }
        }
    }
}
