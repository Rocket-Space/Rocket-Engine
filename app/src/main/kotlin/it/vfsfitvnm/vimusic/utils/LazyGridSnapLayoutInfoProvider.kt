package it.vfsfitvnm.vimusic.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastSumBy

fun Density.calculateDistanceToDesiredSnapPosition(
    layoutInfo: LazyGridLayoutInfo,
    item: LazyGridItemInfo,
    positionInLayout: Density.(layoutSize: Float, itemSize: Float) -> Float
): Float {
    val containerSize =
        with(layoutInfo) { singleAxisViewportSize - beforeContentPadding - afterContentPadding }

    val desiredDistance = positionInLayout(containerSize.toFloat(), item.size.width.toFloat())
    val itemCurrentPosition = item.offset.x.toFloat()

    return itemCurrentPosition - desiredDistance
}

private val LazyGridLayoutInfo.singleAxisViewportSize: Int
    get() = if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width

@ExperimentalFoundationApi
fun SnapLayoutInfoProvider(
    lazyGridState: LazyGridState,
    positionInLayout: Density.(layoutSize: Float, itemSize: Float) -> Float =
        { layoutSize, itemSize -> (layoutSize / 2f - itemSize / 2f) }
): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {

    private val layoutInfo: LazyGridLayoutInfo
        get() = lazyGridState.layoutInfo

    override fun Density.calculateSnappingOffset(currentVelocity: Float): Float {
        var lowerBoundOffset = Float.NEGATIVE_INFINITY
        var upperBoundOffset = Float.POSITIVE_INFINITY

        layoutInfo.visibleItemsInfo.fastForEach { item ->
            val offset =
                calculateDistanceToDesiredSnapPosition(layoutInfo, item, positionInLayout)

            // Find item that is closest to the center
            if (offset <= 0 && offset > lowerBoundOffset) {
                lowerBoundOffset = offset
            }

            // Find item that is closest to center, but after it
            if (offset >= 0 && offset < upperBoundOffset) {
                upperBoundOffset = offset
            }
        }

        // Return the offset closest to zero (nearest item)
        return when {
            lowerBoundOffset == Float.NEGATIVE_INFINITY -> upperBoundOffset
            upperBoundOffset == Float.POSITIVE_INFINITY -> lowerBoundOffset
            else -> if (kotlin.math.abs(lowerBoundOffset) < kotlin.math.abs(upperBoundOffset)) {
                lowerBoundOffset
            } else {
                upperBoundOffset
            }
        }
    }

    override val Density.snapStepSize: Float
        get() = with(layoutInfo) {
            if (visibleItemsInfo.isNotEmpty()) {
                visibleItemsInfo.fastSumBy { it.size.width } / visibleItemsInfo.size.toFloat()
            } else {
                0f
            }
        }
}
