package it.pixiekevin.rocketengine.ui.screens.builtinplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import it.pixiekevin.compose.persist.PersistMapCleanup
import it.pixiekevin.compose.routing.RouteHandler
import it.pixiekevin.rocketengine.R
import it.pixiekevin.rocketengine.enums.BuiltInPlaylist
import it.pixiekevin.rocketengine.ui.components.themed.Scaffold
import it.pixiekevin.rocketengine.ui.screens.globalRoutes

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun BuiltInPlaylistScreen(builtInPlaylist: BuiltInPlaylist) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabIndexChanged) = rememberSaveable {
        mutableStateOf(when (builtInPlaylist) {
            BuiltInPlaylist.Favorites -> 0
            BuiltInPlaylist.Offline -> 1
        })
    }

    PersistMapCleanup(tagPrefix = "${builtInPlaylist.name}/")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabIndexChanged,
                tabColumnContent = { Item ->
                    Item(0, "Favorites", R.drawable.heart)
                    Item(1, "Offline", R.drawable.airplane)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> BuiltInPlaylistSongs(builtInPlaylist = BuiltInPlaylist.Favorites)
                        1 -> BuiltInPlaylistSongs(builtInPlaylist = BuiltInPlaylist.Offline)
                    }
                }
            }
        }
    }
}
