package com.example.takstud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.takstud.model.Permission
import com.example.takstud.model.Role
import com.example.takstud.util.SessionManager

/**
 * Guard composable that checks if user is authenticated
 * If not authenticated, shows login redirect message
 *
 * @param fallbackRoute Lambda to navigate to login
 * @param content The content to show if authenticated
 */
@Composable
fun RequireAuthentication(
    fallbackRoute: () -> Unit,
    content: @Composable () -> Unit
) {
    if (SessionManager.isAuthenticated()) {
        content()
    } else {
        AuthenticationRequiredScreen(fallbackRoute)
    }
}

/**
 * Guard composable that checks if user has specific permission
 * If not authorized, shows unauthorized message
 *
 * @param permission Required permission
 * @param fallbackRoute Lambda to navigate back
 * @param content The content to show if authorized
 */
@Composable
fun RequirePermission(
    permission: Permission,
    fallbackRoute: () -> Unit,
    content: @Composable () -> Unit
) {
    if (SessionManager.hasPermission(permission)) {
        content()
    } else {
        UnauthorizedScreen(fallbackRoute)
    }
}

/**
 * Guard composable that checks if user has a specific role
 * If not authorized, shows unauthorized message
 *
 * @param role Required role
 * @param fallbackRoute Lambda to navigate back
 * @param content The content to show if role matches
 */
@Composable
fun RequireRole(
    role: Role,
    fallbackRoute: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentRole = SessionManager.getCurrentRole()
    if (currentRole == role) {
        content()
    } else {
        UnauthorizedScreen(fallbackRoute)
    }
}

/**
 * Screen shown when user needs to be authenticated
 */
@Composable
fun AuthenticationRequiredScreen(
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Autenticação Necessária",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Você precisa fazer login para acessar esta área",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = onLoginClick,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Ir para Login")
        }
    }
}

/**
 * Screen shown when user doesn't have required permission
 */
@Composable
fun UnauthorizedScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Acesso Negado",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Você não tem permissão para acessar esta área",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Red
        )
        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Voltar")
        }
    }
}