package it.pixiekevin.rocketengine.ui.components.themed

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.pixiekevin.rocketengine.LocalPlayerAwareWindowInsets
import it.pixiekevin.rocketengine.ui.styling.Dimensions
import it.pixiekevin.rocketengine.ui.styling.LocalAppearance
import it.pixiekevin.rocketengine.utils.center
import it.pixiekevin.rocketengine.utils.color
import it.pixiekevin.rocketengine.utils.isLandscape
import it.pixiekevin.rocketengine.utils.semiBold

@Composable
inline fun NavigationRail(
    topIconButtonId: Int,
    noinline onTopIconButtonClick: () -> Unit,
    tabIndex: Int,
    crossinline onTabIndexChanged: (Int) -> Unit,
    content: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val isLandscape = isLandscape

    val paddingValues = LocalPlayerAwareWindowInsets.current
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top).asPaddingValues()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(paddingValues)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .size(
                    width = Dimensions.headerHeight,
                    height = if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth
                )
        ) {
            Image(
                painter = painterResource(topIconButtonId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                modifier = Modifier
                    .offset(
                        x = 48.dp,
                        y = if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onTopIconButtonClick)
                    .padding(all = 12.dp)
                    .size(22.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth)
        ) {
            val transition = updateTransition(targetState = tabIndex, label = null)

            content { index, text, icon ->
                val dothAlpha by transition.animateFloat(label = "") {
                    if (it == index) 1f else 0f
                }

                val textColor by transition.animateColor(label = "") {
                    if (it == index) colorPalette.text else colorPalette.textDisabled
                }

                val iconContent: @Composable () -> Unit = {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = dothAlpha
                                translationY = (1f - dothAlpha) * -48.dp.toPx()
                            }
                            .size(Dimensions.navigationRailIconOffset * 2)
                    )
                }

                val textContent: @Composable () -> Unit = {
                    BasicText(
                        text = text,
                        style = typography.xs.semiBold.center.color(textColor),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }

                val contentModifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = { onTabIndexChanged(index) })

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = contentModifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    iconContent()
                    textContent()
                }
            }
        }
    }
}
