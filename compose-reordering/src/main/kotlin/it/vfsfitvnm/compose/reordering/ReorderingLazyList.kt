package it.vfsfitvnm.compose.reordering

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun ReorderingLazyList(
    modifier: Modifier,
    reorderingState: ReorderingState,
    contentPadding: PaddingValues,
    reverseLayout: Boolean,
    isVertical: Boolean,
    flingBehavior: FlingBehavior,
    userScrollEnabled: Boolean,
    horizontalAlignment: Alignment.Horizontal? = null,
    verticalArrangement: Arrangement.Vertical? = null,
    verticalAlignment: Alignment.Vertical? = null,
    horizontalArrangement: Arrangement.Horizontal? = null,
    content: Any
) {
    if (isVertical) {
        LazyColumn(
            modifier = modifier,
            state = reorderingState.lazyListState,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement ?: Arrangement.Top,
            horizontalAlignment = horizontalAlignment ?: Alignment.Start,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content as @Composable () -> Unit
        )
    } else {
        LazyRow(
            modifier = modifier,
            state = reorderingState.lazyListState,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            horizontalArrangement = horizontalArrangement ?: Arrangement.Start,
            verticalAlignment = verticalAlignment ?: Alignment.Top,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content as @Composable () -> Unit
        )
    }
}
