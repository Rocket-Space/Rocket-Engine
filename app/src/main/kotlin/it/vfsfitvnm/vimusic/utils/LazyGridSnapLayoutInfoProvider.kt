package it.pixiekevin.rocketengine.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.lazy.grid.LazyGridState

@ExperimentalFoundationApi
fun LazyGridSnapLayoutInfoProvider(
    lazyGridState: LazyGridState
): SnapLayoutInfoProvider {
    return SnapLayoutInfoProvider(lazyGridState)
}
