package it.pixiekevin.rocketengine.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.api.services.youtube.model.Playlist
import it.pixiekevin.rocketengine.LocalGoogleAuthManager
import it.pixiekevin.rocketengine.R
import it.pixiekevin.rocketengine.ui.components.themed.Header
import it.pixiekevin.rocketengine.ui.components.themed.IconButton
import it.pixiekevin.rocketengine.ui.items.PlaylistItem
import it.pixiekevin.rocketengine.youtube.YouTubePlaylistManager
import it.pixiekevin.rocketengine.utils.semiBold
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun YouTubePlaylists(
    onPlaylistClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val googleAuthManager = LocalGoogleAuthManager.current
    val coroutineScope = rememberCoroutineScope()
    val (colorPalette, typography) = it.pixiekevin.rocketengine.ui.styling.LocalAppearance.current

    var playlists by remember { mutableStateOf<List<Playlist>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val playlistManager = remember { YouTubePlaylistManager(context, googleAuthManager) }

    LaunchedEffect(googleAuthManager.isSignedIn()) {
        if (googleAuthManager.isSignedIn()) {
            isLoading = true
            error = null
            playlistManager.fetchUserPlaylists()
                .onSuccess { playlists = it }
                .onFailure { error = it.message }
            isLoading = false
        }
    }

    Header(
        title = "YouTube Playlists",
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    if (!googleAuthManager.isSignedIn()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.text.BasicText(
                text = "Sign in to your Google account to view your YouTube playlists",
                style = typography.xs.semiBold.copy(
                    color = colorPalette.textSecondary
                )
            )
        }
        return
    }

    if (isLoading) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.text.BasicText(
                text = "Loading playlists...",
                style = typography.xs.semiBold.copy(
                    color = colorPalette.textSecondary
                )
            )
        }
        return
    }

    if (error != null) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.text.BasicText(
                text = "Error: $error",
                style = typography.xs.semiBold.copy(
                    color = colorPalette.red
                )
            )
        }
        return
    }

    if (playlists.isNullOrEmpty()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.text.BasicText(
                text = "No playlists found",
                style = typography.xs.semiBold.copy(
                    color = colorPalette.textSecondary
                )
            )
        }
        return
    }

    androidx.compose.foundation.lazy.LazyColumn {
        items(
            items = playlists!!,
            key = { it.id }
        ) { playlist ->
            PlaylistItem(
                thumbnailUrl = playlist.snippet.thumbnails?.default?.url,
                songCount = playlist.contentDetails?.itemCount?.toInt(),
                name = playlist.snippet.title,
                channelName = null,
                thumbnailSizePx = 200,
                thumbnailSizeDp = 60.dp,
                modifier = Modifier.clickable { onPlaylistClick(playlist.id) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
