package it.pixiekevin.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.pixiekevin.innertube.Innertube
import it.pixiekevin.innertube.models.Context
import it.pixiekevin.innertube.models.PlayerResponse
import it.pixiekevin.innertube.models.bodies.PlayerBody
import it.pixiekevin.innertube.utils.runCatchingNonCancellable
import kotlinx.serialization.Serializable

/**
 * Invidious audio format response
 */
@Serializable
data class InvidiousFormat(
    val itag: String,
    val url: String,
    val mimeType: String,
    val bitrate: Long? = null,
    val qualityLabel: String? = null,
    val audioQuality: String? = null,
    val audioSampleRate: String? = null,
    val contentLength: Long? = null
)

/**
 * Invidious video info response
 */
@Serializable
data class InvidiousVideoInfo(
    val title: String? = null,
    val author: String? = null,
    val lengthSeconds: Int? = null,
    val formatStreams: List<InvidiousFormat>? = null,
    val adaptiveFormats: List<InvidiousFormat>? = null
)

suspend fun Innertube.player(body: PlayerBody) = runCatchingNonCancellable {
    @Serializable
    data class AudioStream(
        val url: String,
        val bitrate: Long
    )

    @Serializable
    data class PipedResponse(
        val audioStreams: List<AudioStream>
    )

    // Instancias Piped públicas (más servidores = mejor disponibilidad)
    val pipedInstances = listOf(
        "https://pipedapi.adminforge.de",
        "https://api.piped.projectkodiak.com",
        "https://pipedapi.moomoo.me",
        "https://pipedapi.nerdyfam.tech",
        "https://api.piped.privacydev.net",
        "https://pipedapi.drgns.space",
        "https://pipedapi.drgns.space",
        "https://api.piped.projectsegfault.com"
    )

    // Instancias Invidious públicas (más estables)
    val invidiousInstances = listOf(
        "https://iv.datura.network",
        "https://iv.nboeck.de",
        "https://iv.melmac.space",
        "https://yt.artemislena.eu",
        "https://iv.nboeck.de",
        "https://iv.datura.network",
        "https://yt.odycdn.com",
        "https://iv.melmac.space"
    )

    // Función para crear response con formato de audio
    fun createAudioResponse(url: String, itag: Int = 140): PlayerResponse {
        return PlayerResponse(
            playabilityStatus = PlayerResponse.PlayabilityStatus(status = "OK"),
            playerConfig = null,
            streamingData = PlayerResponse.StreamingData(
                adaptiveFormats = listOf(
                    PlayerResponse.StreamingData.AdaptiveFormat(
                        itag = itag,
                        mimeType = if (itag == 251) "audio/webm; codecs=\"opus\"" else "audio/mp4; codecs=\"mp4a.40.2\"",
                        bitrate = 128000,
                        averageBitrate = null,
                        contentLength = null,
                        audioQuality = if (itag == 251) "AUDIO_QUALITY_MEDIUM" else "AUDIO_QUALITY_LOW",
                        approxDurationMs = null,
                        lastModified = null,
                        loudnessDb = null,
                        audioSampleRate = 48000,
                        url = url
                    )
                )
            ),
            videoDetails = PlayerResponse.VideoDetails(videoId = body.videoId)
        )
    }

    // ===== MÉTODO 1: Intentar con Invidious (más estable) =====
    // Intentar con más instancias para mejor disponibilidad
    for (instance in invidiousInstances.shuffled().take(4)) {
        try {
            val videoInfo = client.get("$instance/api/v1/videos/${body.videoId}") {
                contentType(ContentType.Application.Json)
            }.body<InvidiousVideoInfo>()

            // Buscar formato de audio preferido (251 opus > 140 m4a)
            val allFormats = videoInfo.adaptiveFormats ?: videoInfo.formatStreams ?: emptyList()
            val audioFormats = allFormats.filter { 
                it.mimeType.contains("audio/") || it.itag == "251" || it.itag == "140" || it.itag == "250" || it.itag == "249"
            }

            // Prioridad: 251 (opus, mejor calidad) > 140 (m4a, universal) > otros
            val selectedFormat = audioFormats.find { it.itag == "251" }
                ?: audioFormats.find { it.itag == "140" }
                ?: audioFormats.find { it.itag == "250" }
                ?: audioFormats.find { it.itag == "249" }
                ?: audioFormats.firstOrNull()

            selectedFormat?.url?.let { url ->
                val itagNum = selectedFormat.itag.toIntOrNull() ?: 140
                return@runCatchingNonCancellable createAudioResponse(url, itagNum)
            }
        } catch (e: Exception) {
            continue
        }
    }

    // ===== MÉTODO 2: Intentar con Piped =====
    for (instance in pipedInstances.shuffled().take(4)) {
        try {
            val pipedData = client.get("$instance/streams/${body.videoId}") {
                contentType(ContentType.Application.Json)
            }.body<PipedResponse>()

            val stream = pipedData.audioStreams.firstOrNull()
            stream?.url?.let { url ->
                return@runCatchingNonCancellable createAudioResponse(url, 140)
            }
        } catch (e: Exception) {
            continue
        }
    }

    // ===== MÉTODO 3: Intentar InnerTube con iOS client =====
    try {
        val iosResponse = client.post(player) {
            setBody(body.copy(context = Context.DefaultIOS))
            mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
        }.body<PlayerResponse>()

        if (iosResponse.playabilityStatus?.status == "OK" && 
            iosResponse.streamingData?.adaptiveFormats?.any { !it.url.isNullOrEmpty() } == true) {
            return@runCatchingNonCancellable iosResponse
        }
    } catch (e: Exception) {
        // Fall through to next method
    }

    // ===== MÉTODO 4: Intentar InnerTube con Android client =====
    try {
        val androidResponse = client.post(player) {
            setBody(body)
            mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
        }.body<PlayerResponse>()

        if (androidResponse.playabilityStatus?.status == "OK" &&
            androidResponse.streamingData?.adaptiveFormats?.any { !it.url.isNullOrEmpty() } == true) {
            return@runCatchingNonCancellable androidResponse
        }
    } catch (e: Exception) {
        // Fall through
    }

    // ===== MÉTODO 5: TV embed client =====
    try {
        val tvResponse = client.post(player) {
            setBody(
                body.copy(
                    context = Context.DefaultAgeRestrictionBypass.copy(
                        thirdParty = Context.ThirdParty(
                            embedUrl = "https://www.youtube.com/watch?v=${body.videoId}"
                        )
                    )
                )
            )
            mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
        }.body<PlayerResponse>()

        if (tvResponse.playabilityStatus?.status == "OK" &&
            tvResponse.streamingData?.adaptiveFormats?.any { !it.url.isNullOrEmpty() } == true) {
            return@runCatchingNonCancellable tvResponse
        }
    } catch (e: Exception) {
        // Fall through
    }

    // Si todo falla, lanzar error
    throw Exception("No playable streams found for video ${body.videoId}. Try again later or check your internet connection.")
}
