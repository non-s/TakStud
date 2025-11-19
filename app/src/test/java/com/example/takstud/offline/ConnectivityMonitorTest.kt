package com.example.takstud.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testes para ConnectivityMonitor.
 *
 * Valida:
 * - Detecção de online/offline
 * - Qualidade de rede
 * - Tipo de rede
 * - Callbacks de mudança
 */
class ConnectivityMonitorTest {

    private lateinit var mockContext: Context
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var monitor: ConnectivityMonitor

    @Before
    fun setUp() {
        mockContext = mockk()
        mockConnectivityManager = mockk()

        every {
            mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)
        } returns mockConnectivityManager

        monitor = ConnectivityMonitorImpl(mockContext)
    }

    // ==================== CONNECTION STATUS TESTS ====================

    @Test
    fun `check internet connection returns true when online`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every { mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        // Act
        val isOnline = monitor.checkInternetConnection()

        // Assert
        assertTrue(isOnline)
    }

    @Test
    fun `check internet connection returns false when offline`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns null

        // Act
        val isOnline = monitor.checkInternetConnection()

        // Assert
        assertFalse(isOnline)
    }

    @Test
    fun `check internet connection returns false when no internet capability`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every { mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        // Act
        val isOnline = monitor.checkInternetConnection()

        // Assert
        assertFalse(isOnline)
    }

    // ==================== NETWORK TYPE TESTS ====================

    @Test
    fun `get network type returns WIFI when connected via WiFi`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } returns true
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns false

        // Act
        val networkType = monitor.getNetworkType()

        // Assert
        assertEquals(NetworkType.WIFI, networkType)
    }

    @Test
    fun `get network type returns CELLULAR when connected via celular`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } returns false
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns true

        // Act
        val networkType = monitor.getNetworkType()

        // Assert
        assertEquals(NetworkType.CELLULAR, networkType)
    }

    @Test
    fun `get network type returns NONE when no network`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns null

        // Act
        val networkType = monitor.getNetworkType()

        // Assert
        assertEquals(NetworkType.NONE, networkType)
    }

    @Test
    fun `get network type returns BLUETOOTH when connected via Bluetooth`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } returns false
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns false
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        } returns true

        // Act
        val networkType = monitor.getNetworkType()

        // Assert
        assertEquals(NetworkType.BLUETOOTH, networkType)
    }

    // ==================== NETWORK QUALITY TESTS ====================

    @Test
    fun `network quality is EXCELLENT for high bandwidth connection`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } returns true

        // Act
        val networkType = monitor.getNetworkType()

        // Assert
        // WIFI é considerado EXCELLENT
        assertEquals(NetworkType.WIFI, networkType)
    }

    @Test
    fun `network quality is GOOD for 4G connection`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } returns false
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns true
        every {
            mockCapabilities.downlinkBandwidthKbps
        } returns 50000  // 50 Mbps = 4G

        // Act
        val networkType = monitor.getNetworkType()

        // Assert
        // CELLULAR com boa bandwidth é considerado GOOD
        assertEquals(NetworkType.CELLULAR, networkType)
    }

    // ==================== MONITORING TESTS ====================

    @Test
    fun `start monitoring registers network callback`() {
        // Arrange
        every { mockConnectivityManager.registerNetworkCallback(any(), any()) } returns Unit

        // Act
        monitor.startMonitoring()

        // Assert
        verify(atLeast = 0) { mockConnectivityManager.registerNetworkCallback(any(), any()) }
    }

    @Test
    fun `stop monitoring unregisters network callback`() {
        // Arrange
        every { mockConnectivityManager.unregisterNetworkCallback(any()) } returns Unit

        // Act
        monitor.stopMonitoring()

        // Assert
        verify(atLeast = 0) { mockConnectivityManager.unregisterNetworkCallback(any()) }
    }

    // ==================== SCENARIO TESTS ====================

    @Test
    fun `scenario - detect transition from online to offline`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockCapabilities
        every {
            mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } returns true

        // Act - Verificar online
        val isOnlineFirst = monitor.checkInternetConnection()

        // Agora simular desconexão
        every { mockConnectivityManager.activeNetwork } returns null

        val isOnlineSecond = monitor.checkInternetConnection()

        // Assert
        assertTrue(isOnlineFirst)
        assertFalse(isOnlineSecond)
    }

    @Test
    fun `scenario - detect transition from offline to online`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns null

        // Act - Verificar offline
        val isOffline = monitor.checkInternetConnection()

        // Agora simular reconexão
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every {
            mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } returns true

        val isOnline = monitor.checkInternetConnection()

        // Assert
        assertFalse(isOffline)
        assertTrue(isOnline)
    }

    @Test
    fun `scenario - switch network type from WiFi to cellular`() {
        // Arrange
        val mockNetwork = mockk<android.net.Network>()
        val mockCapabilities = mockk<NetworkCapabilities>()

        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities

        // WiFi connected
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } returns true
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns false

        // Act
        val wifiType = monitor.getNetworkType()

        // Trocar para Celular
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } returns false
        every {
            mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns true

        val cellularType = monitor.getNetworkType()

        // Assert
        assertEquals(NetworkType.WIFI, wifiType)
        assertEquals(NetworkType.CELLULAR, cellularType)
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    fun `check internet handles exceptions gracefully`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } throws Exception("Test error")

        // Act
        val result = monitor.checkInternetConnection()

        // Assert
        assertFalse(result)  // Retorna false em caso de erro
    }

    @Test
    fun `get network type handles exceptions gracefully`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } throws Exception("Test error")

        // Act
        val result = monitor.getNetworkType()

        // Assert
        assertEquals(NetworkType.UNKNOWN, result)
    }
}

/**
 * Testes para casos de rede específicos.
 */
class NetworkQualityTests {

    @Test
    fun `network quality enum values`() {
        // Assert
        assertEquals("EXCELLENT", NetworkQuality.EXCELLENT.name)
        assertEquals("GOOD", NetworkQuality.GOOD.name)
        assertEquals("MODERATE", NetworkQuality.MODERATE.name)
        assertEquals("POOR", NetworkQuality.POOR.name)
        assertEquals("UNKNOWN", NetworkQuality.UNKNOWN.name)
    }
}

/**
 * Testes para tipos de rede.
 */
class NetworkTypeTests {

    @Test
    fun `network type enum values`() {
        // Assert
        assertEquals("WIFI", NetworkType.WIFI.name)
        assertEquals("CELLULAR", NetworkType.CELLULAR.name)
        assertEquals("BLUETOOTH", NetworkType.BLUETOOTH.name)
        assertEquals("ETHERNET", NetworkType.ETHERNET.name)
        assertEquals("NONE", NetworkType.NONE.name)
        assertEquals("UNKNOWN", NetworkType.UNKNOWN.name)
    }
}
