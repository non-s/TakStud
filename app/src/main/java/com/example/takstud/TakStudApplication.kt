package com.example.takstud

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.firebase.Firebase
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class para TakStud.
 *
 * Responsabilidades:
 * - Inicializar Firebase e dependências globais
 * - Configurar logging com Timber
 * - Inicializar WorkManager para tarefas em background
 * - Hilt para injeção de dependências
 *
 * Esta classe é carregada antes da MainActivity e do primeiro Activity.
 */
@HiltAndroidApp
class TakStudApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Inicializar Timber
        initializeTimber()

        try {
            // Inicializar Firebase (geralmente automático com google-services.json)
            Firebase.initialize(this)
            Timber.i("✓ Firebase inicializado com sucesso")

            // Log inicial
            Timber.i("Aplicação TakStud iniciando...")

        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao inicializar Firebase")
        }
    }

    /**
     * Configura Timber baseado no build type
     * - Debug: Logs completos no Logcat
     * - Release: Logs apenas de erros (sem logs sensíveis)
     */
    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            // Debug mode: log everything com tags
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber inicializado em modo DEBUG")
        } else {
            // Release mode: apenas erros críticos
            Timber.plant(ReleaseTree())
            Timber.i("Timber inicializado em modo RELEASE")
        }
    }

    /**
     * Timber Tree customizada para produção
     * Logs apenas erros e warnings, sem informações sensíveis
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Em produção, só loga erros e warnings
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                // Aqui você pode integrar com Crashlytics/Firebase Analytics
                // Por enquanto, apenas loga no sistema
                if (t != null) {
                    android.util.Log.println(priority, tag ?: "TakStud", message)
                    android.util.Log.println(priority, tag ?: "TakStud", t.stackTraceToString())
                } else {
                    android.util.Log.println(priority, tag ?: "TakStud", message)
                }
            }
        }
    }
}
