package it.pixiekevin.rocketengine.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import it.pixiekevin.compose.persist.PersistMapCleanup
import it.pixiekevin.compose.routing.RouteHandler
import it.pixiekevin.compose.routing.defaultStacking
import it.pixiekevin.compose.routing.defaultStill
import it.pixiekevin.compose.routing.defaultUnstacking
import it.pixiekevin.compose.routing.isStacking
import it.pixiekevin.compose.routing.isUnknown
import it.pixiekevin.compose.routing.isUnstacking
import it.pixiekevin.rocketengine.Database
import it.pixiekevin.rocketengine.R
import it.pixiekevin.rocketengine.models.SearchQuery
import it.pixiekevin.rocketengine.query
import it.pixiekevin.rocketengine.ui.components.themed.Scaffold
import it.pixiekevin.rocketengine.ui.screens.albumRoute
import it.pixiekevin.rocketengine.ui.screens.artistRoute
import it.pixiekevin.rocketengine.ui.screens.builtInPlaylistRoute
import it.pixiekevin.rocketengine.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import it.pixiekevin.rocketengine.ui.screens.globalRoutes
import it.pixiekevin.rocketengine.ui.screens.home.YouTubePlaylists
import it.pixiekevin.rocketengine.ui.screens.localPlaylistRoute
import it.pixiekevin.rocketengine.ui.screens.localplaylist.LocalPlaylistScreen
import it.pixiekevin.rocketengine.ui.screens.playlistRoute
import it.pixiekevin.rocketengine.ui.screens.search.SearchScreen
import it.pixiekevin.rocketengine.ui.screens.searchResultRoute
import it.pixiekevin.rocketengine.ui.screens.searchRoute
import it.pixiekevin.rocketengine.ui.screens.searchresult.SearchResultScreen
import it.pixiekevin.rocketengine.ui.screens.settings.SettingsScreen
import it.pixiekevin.rocketengine.ui.screens.settingsRoute
import it.pixiekevin.rocketengine.utils.homeScreenTabIndexKey
import it.pixiekevin.rocketengine.utils.pauseSearchHistoryKey
import it.pixiekevin.rocketengine.utils.preferences
import it.pixiekevin.rocketengine.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen(onPlaylistUrl: (String) -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl
            )
        }

        host {
            val (tabIndex, onTabChanged) = rememberPreference(
                homeScreenTabIndexKey,
                defaultValue = 0
            )

            Scaffold(
                topIconButtonId = R.drawable.equalizer,
                onTopIconButtonClick = { settingsRoute() },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Quick picks", R.drawable.sparkles)
                    Item(1, "Songs", R.drawable.musical_notes)
                    Item(2, "Playlists", R.drawable.playlist)
                    Item(3, "YouTube", R.drawable.playlist)
                    Item(4, "Artists", R.drawable.person)
                    Item(5, "Albums", R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> YouTubePlaylists(
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        5 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                    }
                }
            }
        }
    }
}
