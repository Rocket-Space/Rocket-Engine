package it.pixiekevin.rocketengine.download

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.HttpURLConnection

class DownloadManager(private val context: Context) {
    
    suspend fun downloadAudio(
        url: String,
        filename: String,
        downloadPath: DownloadPath
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val directory = when (downloadPath) {
                DownloadPath.CACHE -> File(context.cacheDir, "downloads")
                DownloadPath.MUSIC -> File(
                    ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_MUSIC).firstOrNull()
                        ?: context.filesDir,
                    "downloads"
                )
                DownloadPath.DOWNLOADS -> File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "ViMusic"
                )
                DownloadPath.CUSTOM -> File(context.filesDir, "downloads")
            }
            
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, "$filename.mp3")
            
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("Download failed: ${connection.responseCode}"))
            }
            
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(file)
            
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            outputStream.close()
            inputStream.close()
            connection.disconnect()
            
            Result.success(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    fun getDownloadPath(downloadPath: DownloadPath): File {
        return when (downloadPath) {
            DownloadPath.CACHE -> File(context.cacheDir, "downloads")
            DownloadPath.MUSIC -> File(
                ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_MUSIC).firstOrNull()
                    ?: context.filesDir,
                "downloads"
            )
            DownloadPath.DOWNLOADS -> File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "ViMusic"
            )
            DownloadPath.CUSTOM -> File(context.filesDir, "downloads")
        }
    }
    
    fun getCustomDownloadPath(): File {
        val prefs = context.getSharedPreferences("download_settings", Context.MODE_PRIVATE)
        val path = prefs.getString("custom_download_path", null)
        return if (path != null) File(path) else File(context.filesDir, "downloads")
    }
    
    fun setCustomDownloadPath(path: String) {
        val prefs = context.getSharedPreferences("download_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("custom_download_path", path).apply()
    }
    
    fun getDownloadPathPreference(): DownloadPath {
        val prefs = context.getSharedPreferences("download_settings", Context.MODE_PRIVATE)
        val pathType = prefs.getString("download_path_type", DownloadPath.CACHE.name)
        return DownloadPath.valueOf(pathType ?: DownloadPath.CACHE.name)
    }
    
    fun setDownloadPathPreference(downloadPath: DownloadPath) {
        val prefs = context.getSharedPreferences("download_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("download_path_type", downloadPath.name).apply()
    }
    
    fun getPromptForLocation(): Boolean {
        val prefs = context.getSharedPreferences("download_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("prompt_for_location", false)
    }
    
    fun setPromptForLocation(prompt: Boolean) {
        val prefs = context.getSharedPreferences("download_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("prompt_for_location", prompt).apply()
    }
}

enum class DownloadPath {
    CACHE,
    MUSIC,
    DOWNLOADS,
    CUSTOM
}
