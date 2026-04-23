package it.pixiekevin.rocketengine.service

import android.net.Uri
import it.pixiekevin.innertube.Innertube
import it.pixiekevin.innertube.models.bodies.PlayerBody
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages YouTube stream URLs with proactive refresh and pre-fetching.
 * Ensures URLs are always fresh and valid before ExoPlayer needs them.
 */
class PlaybackUrlManager(private val coroutineScope: CoroutineScope) {
    
    data class UrlEntry(
        val url: String,
        val timestamp: Long,
        val formatItag: Int,
        val mimeType: String
    )
    
    // Cache of valid URLs with their timestamps
    private val urlCache = ConcurrentHashMap<String, UrlEntry>()
    
    // Pre-fetched URLs for next songs
    private val preFetchCache = ConcurrentHashMap<String, Deferred<UrlEntry?>>()
    
    companion object {
        // URLs expire after 20 seconds (safer than 30)
        const val URL_EXPIRY_MS = 20000L
        // Pre-fetch next song when current is at 70% progress
        const val PRE_FETCH_THRESHOLD = 0.7
    }
    
    /**
     * Get a valid URL for playback. Returns cached if fresh, fetches new if expired.
     */
    suspend fun getValidUrl(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        val cached = urlCache[videoId]
        val now = System.currentTimeMillis()
        
        // Use cached URL if less than 20 seconds old
        if (cached != null && (now - cached.timestamp) < URL_EXPIRY_MS) {
            return@withContext Result.success(cached.url)
        }
        
        // Check pre-fetch cache
        val preFetched = preFetchCache[videoId]?.await()
        if (preFetched != null && (now - preFetched.timestamp) < URL_EXPIRY_MS) {
            urlCache[videoId] = preFetched
            preFetchCache.remove(videoId)
            return@withContext Result.success(preFetched.url)
        }
        
        // Fetch new URL
        return@withContext fetchFreshUrl(videoId)
    }
    
    /**
     * Pre-fetch URL for a video to have it ready before playback.
     */
    fun preFetchUrl(videoId: String) {
        if (preFetchCache.containsKey(videoId) || urlCache.containsKey(videoId)) {
            return // Already cached or pre-fetching
        }
        
        preFetchCache[videoId] = coroutineScope.async(Dispatchers.IO) {
            fetchFreshUrl(videoId).getOrNull()?.let { url ->
                UrlEntry(
                    url = url,
                    timestamp = System.currentTimeMillis(),
                    formatItag = 140, // Default
                    mimeType = "audio/mp4"
                )
            }
        }
    }
    
    /**
     * Force refresh a URL even if not expired (for recovery after errors).
     */
    suspend fun forceRefreshUrl(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        urlCache.remove(videoId)
        preFetchCache.remove(videoId)
        fetchFreshUrl(videoId)
    }
    
    /**
     * Check if URL needs refresh (approaching expiry).
     */
    fun needsRefresh(videoId: String): Boolean {
        val cached = urlCache[videoId] ?: return true
        return (System.currentTimeMillis() - cached.timestamp) > (URL_EXPIRY_MS * 0.8) // 80% of expiry
    }
    
    /**
     * Clear all cached URLs.
     */
    fun clearCache() {
        urlCache.clear()
        preFetchCache.values.forEach { it.cancel() }
        preFetchCache.clear()
    }
    
    private suspend fun fetchFreshUrl(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = Innertube.player(PlayerBody(videoId = videoId))
                .getOrThrow()
            
            if (response.playabilityStatus?.status != "OK") {
                return@withContext Result.failure(Exception("Video unplayable: ${response.playabilityStatus?.status}"))
            }
            
            val format = response.streamingData?.highestQualityFormat
                ?: return@withContext Result.failure(Exception("No audio format found"))
            
            val url = format.url
                ?: return@withContext Result.failure(Exception("No stream URL found"))
            
            // Cache the result
            urlCache[videoId] = UrlEntry(
                url = url,
                timestamp = System.currentTimeMillis(),
                formatItag = format.itag,
                mimeType = format.mimeType
            )
            
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get cached entry if valid.
     */
    fun getValidCachedEntry(videoId: String): UrlEntry? {
        val cached = urlCache[videoId] ?: return null
        return if ((System.currentTimeMillis() - cached.timestamp) < URL_EXPIRY_MS) {
            cached
        } else {
            urlCache.remove(videoId)
            null
        }
    }
}
