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

    // Lista de instancias Piped públicas como fallback
    val pipedInstances = listOf(
        "https://pipedapi.adminforge.de",
        "https://api.piped.projectkodiak.com",
        "https://pipedapi.moomoo.me",
        "https://pipedapi.nerdyfam.tech",
        "https://api.piped.privacydev.net"
    )

    // Intentar con iOS client primero (menos bloqueado)
    val iosResponse = client.post(player) {
        setBody(
            body.copy(context = Context.DefaultIOS)
        )
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    if (iosResponse.playabilityStatus?.status == "OK" && iosResponse.streamingData?.adaptiveFormats?.isNotEmpty() == true) {
        return@runCatchingNonCancellable iosResponse
    }

    // Intentar con cliente Android original
    val androidResponse = client.post(player) {
        setBody(body)
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    if (androidResponse.playabilityStatus?.status == "OK" && androidResponse.streamingData?.adaptiveFormats?.isNotEmpty() == true) {
        return@runCatchingNonCancellable androidResponse
    }

    // Intentar con TV embed client
    val safePlayerResponse = client.post(player) {
        setBody(
            body.copy(
                context = Context.DefaultAgeRestrictionBypass.copy(
                    thirdParty = Context.ThirdParty(
                        embedUrl = "https://www.youtube.com/watch?v=${body.videoId}"
                    )
                ),
            )
        )
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    if (safePlayerResponse.playabilityStatus?.status == "OK" && safePlayerResponse.streamingData?.adaptiveFormats?.isNotEmpty() == true) {
        return@runCatchingNonCancellable safePlayerResponse
    }

    // Intentar con instancias Piped como último recurso
    var lastException: Throwable? = null
    for (instance in pipedInstances) {
        try {
            val audioStreams = client.get("$instance/streams/${body.videoId}") {
                contentType(ContentType.Application.Json)
            }.body<PipedResponse>().audioStreams

            if (audioStreams.isNotEmpty()) {
                // Usar la respuesta más reciente exitosa y mapear los streams de Piped
                val baseResponse = safePlayerResponse.takeIf { it.playabilityStatus?.status == "OK" }
                    ?: androidResponse.takeIf { it.playabilityStatus?.status == "OK" }
                    ?: iosResponse

                val firstAudioStream = audioStreams.firstOrNull()
                if (firstAudioStream != null) {
                    return@runCatchingNonCancellable baseResponse.copy(
                        streamingData = baseResponse.streamingData?.copy(
                            adaptiveFormats = listOf(
                                baseResponse.streamingData.adaptiveFormats?.firstOrNull()?.copy(
                                    url = firstAudioStream.url
                                ) ?: PlayerResponse.StreamingData.AdaptiveFormat(
                                    itag = 140,
                                    mimeType = "audio/mp4; codecs=\"mp4a.40.2\"",
                                    bitrate = firstAudioStream.bitrate,
                                    averageBitrate = null,
                                    contentLength = null,
                                    audioQuality = null,
                                    approxDurationMs = null,
                                    lastModified = null,
                                    loudnessDb = null,
                                    audioSampleRate = null,
                                    url = firstAudioStream.url
                                )
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            lastException = e
            continue
        }
    }

    // Si todo falla, devolver la mejor respuesta que tengamos
    safePlayerResponse.takeIf { it.playabilityStatus?.status == "OK" }
        ?: androidResponse.takeIf { it.playabilityStatus?.status == "OK" }
        ?: iosResponse.takeIf { it.playabilityStatus?.status == "OK" }
        ?: throw lastException ?: Exception("No playable streams found")
}
