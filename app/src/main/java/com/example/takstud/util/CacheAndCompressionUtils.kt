package com.example.takstud.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.collection.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * CacheAndCompressionUtils - Utilitários para cache e compressão de dados.
 *
 * FUNCIONALIDADES:
 * - Cache em memória com LRU
 * - Cache em disco
 * - Compressão GZIP para dados
 * - Compressão de imagens
 * - Otimização automática
 * - Limpeza de cache expirado
 *
 * TIPOS DE CACHE:
 * - Memory: Rápido, limitado a RAM disponível
 * - Disk: Persistente, espaço de armazenamento
 * - Hybrid: Combinação de ambos
 *
 * EXEMPLO DE USO:
 * val cacheManager = CacheManager(context)
 * cacheManager.put("key", "valor")
 * val valor = cacheManager.get("key") ?: "padrão"
 * cacheManager.clear()
 */

/**
 * Gerenciador de cache híbrido (memória + disco).
 */
class CacheManager(private val context: Context) {
    companion object {
        private const val MAX_MEMORY_CACHE_SIZE = 5 * 1024 * 1024 // 5MB
        private const val CACHE_DIR_NAME = "app_cache"
        private const val MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    }

    // Cache em memória (LRU)
    private val memoryCache = object : LruCache<String, String>(MAX_MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, value: String): Int {
            return value.length
        }
    }

    private val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply {
        if (!exists()) mkdirs()
    }

    /**
     * Armazena valor no cache (memória + disco).
     */
    fun put(key: String, value: String) {
        // Armazena em memória
        memoryCache.put(key, value)

        // Armazena em disco (comprimido)
        try {
            val compressed = CompressionUtils.compressString(value)
            val file = File(cacheDir, encodeKey(key))
            file.writeBytes(compressed)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Recupera valor do cache (memória primeiro, depois disco).
     */
    fun get(key: String): String? {
        // Tenta memória primeiro
        memoryCache.get(key)?.let { return it }

        // Tenta disco
        return try {
            val file = File(cacheDir, encodeKey(key))
            if (file.exists()) {
                val compressed = file.readBytes()
                val decompressed = CompressionUtils.decompressString(compressed)
                memoryCache.put(key, decompressed) // Volta para memória
                decompressed
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Remove entrada de cache.
     */
    fun remove(key: String) {
        memoryCache.remove(key)
        val file = File(cacheDir, encodeKey(key))
        file.delete()
    }

    /**
     * Limpa todo o cache.
     */
    fun clear() {
        memoryCache.evictAll()
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    /**
     * Verifica tamanho do cache em disco.
     */
    fun getDiskCacheSize(): Long {
        return cacheDir.walkTopDown().map { it.length() }.sum()
    }

    /**
     * Limpa cache se exceder tamanho máximo.
     */
    fun trimToSize() {
        val size = getDiskCacheSize()
        if (size > MAX_DISK_CACHE_SIZE) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }

    /**
     * Codifica chave para nome de arquivo seguro.
     */
    private fun encodeKey(key: String): String {
        return Base64.encodeToString(key.toByteArray(), Base64.DEFAULT)
            .replace("/", "_")
            .replace("+", "-")
            .replace("=", "")
    }
}

/**
 * Utilitários de compressão.
 */
object CompressionUtils {
    /**
     * Comprime string com GZIP.
     */
    fun compressString(input: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(input.toByteArray(Charsets.UTF_8))
        }
        return bos.toByteArray()
    }

    /**
     * Descomprime string com GZIP.
     */
    fun decompressString(input: ByteArray): String {
        return GZIPInputStream(input.inputStream()).use { gzip ->
            gzip.readBytes().toString(Charsets.UTF_8)
        }
    }

    /**
     * Calcula taxa de compressão.
     */
    fun getCompressionRatio(original: String): Double {
        val compressed = compressString(original)
        return (1.0 - (compressed.size.toDouble() / original.length)) * 100
    }

    /**
     * Avalia se vale a pena comprimir.
     */
    fun shouldCompress(data: String, minSizeBytes: Int = 1024): Boolean {
        return data.length >= minSizeBytes
    }
}

/**
 * Compressor de imagens.
 */
class ImageCompressor {
    companion object {
        const val DEFAULT_QUALITY = 80
        const val MAX_WIDTH = 1920
        const val MAX_HEIGHT = 1920
    }

    /**
     * Comprime bitmap para bytes.
     */
    fun compress(
        bitmap: Bitmap,
        quality: Int = DEFAULT_QUALITY,
        maxWidth: Int = MAX_WIDTH,
        maxHeight: Int = MAX_HEIGHT
    ): ByteArray {
        // Redimensiona se necessário
        val resized = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val ratio = (bitmap.width.toFloat() / maxWidth).coerceAtLeast(
                bitmap.height.toFloat() / maxHeight
            )
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width / ratio).toInt(),
                (bitmap.height / ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        // Comprime para JPEG
        val bos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, quality, bos)
        return bos.toByteArray()
    }

    /**
     * Descomprime bytes para bitmap.
     */
    fun decompress(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * Calcula tamanho estimado em KB.
     */
    fun estimateSize(width: Int, height: Int): Long {
        return (width.toLong() * height * 4) / 1024 // ARGB = 4 bytes
    }

    /**
     * Redimensiona imagem mantendo proporção.
     */
    fun resize(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return bitmap
        }

        val ratio = (bitmap.width.toFloat() / maxWidth).coerceAtLeast(
            bitmap.height.toFloat() / maxHeight
        )

        val newWidth = (bitmap.width / ratio).toInt()
        val newHeight = (bitmap.height / ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

/**
 * Utilitários de otimização de dados.
 */
object OptimizationUtils {
    /**
     * Calcula tamanho de um objeto em bytes (estimado).
     */
    fun estimateObjectSize(obj: Any?): Long {
        return when (obj) {
            is String -> obj.length.toLong()
            is ByteArray -> obj.size.toLong()
            else -> 0L
        }
    }

    /**
     * Formata tamanho em bytes para string legível.
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * Calcula percentual de espaço disponível.
     */
    fun getAvailableSpacePercentage(cacheDir: File, maxSize: Long): Double {
        val used = cacheDir.walkTopDown().map { it.length() }.sum()
        return ((maxSize - used).toDouble() / maxSize) * 100
    }

    /**
     * Verifica se deve fazer cleanup de cache.
     */
    fun shouldCleanup(cacheDir: File, maxSize: Long, threshold: Double = 80.0): Boolean {
        val usedPercentage = 100 - getAvailableSpacePercentage(cacheDir, maxSize)
        return usedPercentage > threshold
    }
}

/**
 * Gerenciador de cache com expiração.
 */
class ExpiringCache(private val context: Context) {
    private val cache = mutableMapOf<String, CacheEntry>()

    data class CacheEntry(
        val data: String,
        val expiresAt: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
    }

    /**
     * Armazena com expiração (TTL em segundos).
     */
    fun put(key: String, value: String, ttlSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + (ttlSeconds * 1000)
        cache[key] = CacheEntry(value, expiresAt)
    }

    /**
     * Recupera se não expirado.
     */
    fun get(key: String): String? {
        val entry = cache[key]
        return if (entry?.isExpired() == true) {
            cache.remove(key)
            null
        } else {
            entry?.data
        }
    }

    /**
     * Remove entradas expiradas.
     */
    fun removeExpired() {
        cache.entries.removeAll { it.value.isExpired() }
    }

    /**
     * Limpa cache.
     */
    fun clear() {
        cache.clear()
    }
}

/**
 * Configurações de cache otimizadas por tipo.
 */
object CacheProfiles {
    /**
     * Perfil agressivo: máximo cache, mais compressão.
     */
    fun aggressive(): CacheConfig {
        return CacheConfig(
            memorySize = 10 * 1024 * 1024,
            diskSize = 100 * 1024 * 1024,
            compressionLevel = 9,
            imageQuality = 75
        )
    }

    /**
     * Perfil balanceado: cache moderado.
     */
    fun balanced(): CacheConfig {
        return CacheConfig(
            memorySize = 5 * 1024 * 1024,
            diskSize = 50 * 1024 * 1024,
            compressionLevel = 6,
            imageQuality = 80
        )
    }

    /**
     * Perfil conservador: mínimo cache.
     */
    fun conservative(): CacheConfig {
        return CacheConfig(
            memorySize = 2 * 1024 * 1024,
            diskSize = 20 * 1024 * 1024,
            compressionLevel = 3,
            imageQuality = 90
        )
    }

    data class CacheConfig(
        val memorySize: Int,
        val diskSize: Int,
        val compressionLevel: Int,
        val imageQuality: Int
    )
}
