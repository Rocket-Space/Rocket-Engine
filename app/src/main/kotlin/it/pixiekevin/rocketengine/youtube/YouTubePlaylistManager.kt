package it.pixiekevin.rocketengine.youtube

import android.content.Context
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.PlaylistListResponse
import it.pixiekevin.rocketengine.auth.GoogleAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YouTubePlaylistManager(
    private val context: Context,
    private val googleAuthManager: GoogleAuthManager
) {
    private var youtubeService: YouTube? = null

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = googleAuthManager.getLastSignedInAccount()
            if (account == null) {
                return@withContext false
            }

            val credential = com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2(
                context,
                listOf("https://www.googleapis.com/auth/youtube.readonly")
            )
            credential.selectedAccount = account.account

            youtubeService = YouTube.Builder(
                com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("ViMusic")
                .build()

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun fetchUserPlaylists(): Result<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            if (youtubeService == null) {
                val initialized = initialize()
                if (!initialized) {
                    return@withContext Result.failure(Exception("YouTube service not initialized"))
                }
            }

            val response: PlaylistListResponse = youtubeService!!.playlists()
                .list("snippet,contentDetails")
                .setMine(true)
                .setMaxResults(50L)
                .execute()

            Result.success(response.items ?: emptyList())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun fetchPlaylistById(playlistId: String): Result<Playlist> = withContext(Dispatchers.IO) {
        try {
            if (youtubeService == null) {
                val initialized = initialize()
                if (!initialized) {
                    return@withContext Result.failure(Exception("YouTube service not initialized"))
                }
            }

            val response: PlaylistListResponse = youtubeService!!.playlists()
                .list("snippet,contentDetails")
                .setId(playlistId)
                .execute()

            val playlist = response.items?.firstOrNull()
            if (playlist != null) {
                Result.success(playlist)
            } else {
                Result.failure(Exception("Playlist not found"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
