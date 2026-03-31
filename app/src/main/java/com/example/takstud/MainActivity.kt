package com.example.takstud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.takstud.ui.navigation.TakStudNavHost
import com.example.takstud.ui.theme.TakStudTheme
import com.example.takstud.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Ponto de entrada principal da aplicação.
 * Responsável por configurar o tema global e hospedar o NavHost.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilita layout edge-to-edge
        enableEdgeToEdge()
        
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            TakStudTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                TakStudNavHost(navController = navController)
            }
        }
    }
}
