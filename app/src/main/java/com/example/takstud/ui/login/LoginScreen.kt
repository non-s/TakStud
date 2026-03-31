package com.example.takstud.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.takstud.R
import com.example.takstud.ui.theme.TakStudTheme
import com.example.takstud.util.InputValidator
import com.example.takstud.util.Result
import com.example.takstud.viewmodel.LoginViewModel

/**
 * Tela de login do TakStud.
 * Permite login como responsável (com RA) ou professor (com código).
 * Integrada com LoginViewModel para gerenciar autenticação.
 *
 * @param modifier Modificador Compose
 * @param viewModel LoginViewModel para gerenciar estado de login
 * @param onTeacherLogin Callback quando professor clica para fazer login
 * @param onParentLoginSuccess Callback quando responsável faz login com sucesso
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onTeacherLogin: () -> Unit,
    onParentLoginSuccess: () -> Unit
) {
    var studentRa by remember { mutableStateOf("") }
    var raError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loginState by viewModel.parentLoginState.collectAsState()

    // Handle login result
    LaunchedEffect(loginState) {
        when (loginState) {
            is Result.Success -> {
                onParentLoginSuccess()
                viewModel.resetLoginState()
            }
            is Result.Error -> {
                raError = (loginState as Result.Error).exception.message
            }
            is Result.Loading -> {}
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(R.drawable.ic_launcher_foreground), "TakStud Logo", Modifier.size(150.dp))
        Spacer(Modifier.height(32.dp))

        // Campo de entrada com validação
        OutlinedTextField(
            value = studentRa,
            onValueChange = {
                studentRa = it
                // Validar conforme digita
                raError = if (it.isNotEmpty() && !InputValidator.isValidRA(it)) {
                    "RA deve ter 2-20 caracteres (letras, números, - e _)"
                } else null
            },
            label = { Text(stringResource(R.string.student_ra_hint)) },
            isError = raError != null,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Mensagem de erro
        if (raError != null) {
            Text(
                raError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Botão entrar como responsável
        Button(
            onClick = {
                if (InputValidator.isValidRA(studentRa)) {
                    viewModel.loginAsParent(studentRa)
                } else {
                    raError = "RA inválido"
                }
            },
            enabled = raError == null && studentRa.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.login_as_parent))
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(stringResource(R.string.or))
        Spacer(Modifier.height(32.dp))

        // Botão entrar como professor
        Button(onClick = onTeacherLogin) {
            Text(stringResource(R.string.name_teacher_login))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TakStudTheme {
        LoginScreen(onTeacherLogin = {}, onParentLoginSuccess = {})
    }
}
