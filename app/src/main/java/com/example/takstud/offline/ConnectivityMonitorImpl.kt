package com.example.takstud.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import com.example.takstud.offline.ConnectivityMonitor.NetworkQuality
import com.example.takstud.offline.ConnectivityMonitor.NetworkType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitor de Conectividade de Internet.
 *
 * Detecta mudanças no status de conexão e notifica listeners.
 * Funciona em Android 6.0+ (API 24+).
 *
 * Características:
 * - Monitora WiFi, Celular, Bluetooth
 * - Detecta mudança de online → offline
 * - Dispara evento quando volta internet
 * - Flow reativo para Composables
 * - Tratamento de erros robusto
 *
 * Uso:
 * ```kotlin
 * val monitor = ConnectivityMonitorImpl(context)
 *
 * // Observar em Composable
 * val isOnline = monitor.isOnline.collectAsState()
 * Text(if (isOnline.value) "Online" else "Offline")
 *
 * // Observar mudanças
 * LaunchedEffect(Unit) {
 *     monitor.connectionChanged.collect { isOnline ->
 *         if (isOnline) {
 *             // Internet voltou - sincronizar!
 *             triggerSync()
 *         }
 *     }
 * }
 * ```
 *
 * @see SyncWorker
 */
class ConnectivityMonitorImpl(
    private val context: Context
) : ConnectivityMonitor {

    companion object {
        private const val TAG = "ConnectivityMonitor"
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInternetConnection())
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()

    private val _connectionChanged = MutableStateFlow<Boolean?>(null)
    override val connectionChanged: Flow<Boolean?> = _connectionChanged.asStateFlow()

    private val _networkQuality = MutableStateFlow<NetworkQuality>(NetworkQuality.OFFLINE)
    override val networkQuality: Flow<NetworkQuality> = _networkQuality.asStateFlow()

    private var lastOnlineState = checkInternetConnection()

    /**
     * Inicia monitoramento de conectividade.
     *
     * Deve ser chamado em onCreate() da Activity/ViewModel.
     */
    override fun startMonitoring() {
        try {
            Log.i(TAG, "Iniciando monitoramento de conectividade")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // API 24+ usa registerNetworkCallback
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

                connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            }

            updateConnectionStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar monitoramento", e)
        }
    }

    /**
     * Para o monitoramento de conectividade.
     *
     * Deve ser chamado em onDestroy() da Activity/ViewModel.
     */
    override fun stopMonitoring() {
        try {
            Log.i(TAG, "Parando monitoramento de conectividade")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar monitoramento", e)
        }
    }

    /**
     * Verifica conectividade de internet de forma síncrona.
     *
     * @return true se há conexão de internet
     */
    override fun checkInternetConnection(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities =
                    connectivityManager.getNetworkCapabilities(network) ?: return false

                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar conexão", e)
            false
        }
    }

    /**
     * Callback para mudanças de rede.
     */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            super.onAvailable(network)
            Log.i(TAG, "🌐 Rede disponível")
            updateConnectionStatus()
        }

        override fun onLost(network: android.net.Network) {
            super.onLost(network)
            Log.w(TAG, "📡 Rede perdida")
            updateConnectionStatus()
        }

        override fun onCapabilitiesChanged(
            network: android.net.Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Log.d(TAG, "🔧 Capacidades de rede mudaram")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                updateNetworkQuality(networkCapabilities)
            }
            updateConnectionStatus()
        }

        override fun onLinkPropertiesChanged(
            network: android.net.Network,
            linkProperties: android.net.LinkProperties
        ) {
            super.onLinkPropertiesChanged(network, linkProperties)
            Log.d(TAG, "🔗 Propriedades de link mudaram")
            updateConnectionStatus()
        }
    }

    /**
     * Atualiza status de conexão.
     */
    private fun updateConnectionStatus() {
        try {
            val currentOnline = checkInternetConnection()

            // Detectar mudança de estado
            if (currentOnline != lastOnlineState) {
                lastOnlineState = currentOnline

                if (currentOnline) {
                    Log.i(TAG, "✅ ONLINE - Internet conectada!")
                    // Notificar que voltou online
                    _connectionChanged.value = true
                } else {
                    Log.w(TAG, "❌ OFFLINE - Internet desconectada")
                    // Notificar que saiu online
                    _connectionChanged.value = false
                    _networkQuality.value = NetworkQuality.OFFLINE
                }
            }

            _isOnline.value = currentOnline

            if(currentOnline) {
                val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.activeNetwork
                } else {
                    null
                }
                network?.let {
                    val caps = connectivityManager.getNetworkCapabilities(it)
                    caps?.let { networkCapabilities ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            updateNetworkQuality(networkCapabilities)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar status de conexão", e)
        }
    }

    /**
     * Avalia qualidade de rede baseado em capabilities.
     */
    private fun updateNetworkQuality(capabilities: NetworkCapabilities) {
        try {
            val quality = when {
                !_isOnline.value -> NetworkQuality.OFFLINE
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    NetworkQuality.EXCELLENT
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        when {
                            capabilities.linkDownstreamBandwidthKbps >= 100_000 -> {
                                NetworkQuality.EXCELLENT  // 5G ou melhor
                            }
                            capabilities.linkDownstreamBandwidthKbps >= 30_000 -> {
                                NetworkQuality.GOOD  // 4G
                            }
                            capabilities.linkDownstreamBandwidthKbps >= 10_000 -> {
                                NetworkQuality.MODERATE  // 3G
                            }
                            else -> {
                                NetworkQuality.POOR  // 2G
                            }
                        }
                    } else {
                        NetworkQuality.GOOD // Assume good for older APIs
                    }
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                    NetworkQuality.MODERATE
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    NetworkQuality.EXCELLENT
                }
                else -> {
                    NetworkQuality.UNKNOWN
                }
            }

            if (_networkQuality.value != quality) {
                _networkQuality.value = quality
                Log.d(TAG, "Qualidade de rede: $quality")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao avaliar qualidade de rede", e)
        }
    }


    /**
     * Aguarda até estar online (útil para sincronização).
     *
     * @param timeoutMs Timeout em milissegundos (default 30s)
     * @return true se ficou online, false se timeout
     */
    override suspend fun waitUntilOnline(timeoutMs: Long): Boolean {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (checkInternetConnection()) {
                Log.i(TAG, "✅ Aguardando online: sucesso!")
                return true
            }

            // Aguardar um pouco antes de tentar novamente
            delay(1000)  // 1 segundo
        }

        Log.w(TAG, "⏱️  Timeout aguardando internet")
        return false
    }

    /**
     * Obtém tipo de rede ativa.
     */
    override fun getNetworkType(): NetworkType {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
                val capabilities =
                    connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE

                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                        NetworkType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                        NetworkType.CELLULAR
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ->
                        NetworkType.BLUETOOTH
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                        NetworkType.ETHERNET
                    else -> NetworkType.UNKNOWN
                }
            } else {
                @Suppress("DEPRECATION")
                when (connectivityManager.activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
                    ConnectivityManager.TYPE_BLUETOOTH -> NetworkType.BLUETOOTH
                    else -> NetworkType.UNKNOWN
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter tipo de rede", e)
            NetworkType.UNKNOWN
        }
    }
}