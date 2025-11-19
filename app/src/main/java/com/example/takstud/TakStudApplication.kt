package com.example.takstud

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.initialize

/**
 * Application class para TakStud.
 *
 * Responsabilidades:
 * - Inicializar Firebase e dependências globais
 * - Configurar logging
 * - Inicializar WorkManager para tarefas em background
 *
 * Esta classe é carregada antes da MainActivity e do primeiro Activity.
 */
class TakStudApplication : Application() {

    companion object {
        private const val TAG = "TakStudApp"
    }

    override fun onCreate() {
        super.onCreate()

        try {
            // Inicializar Firebase (geralmente automático com google-services.json)
            Firebase.initialize(this)
            Log.i(TAG, "✓ Firebase inicializado com sucesso")

            // Log inicial
            Log.i(TAG, "Aplicação TakStud iniciando...")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar Firebase", e)
        }
    }
}
