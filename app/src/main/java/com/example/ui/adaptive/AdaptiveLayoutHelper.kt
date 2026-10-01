package com.example.ui.adaptive

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive Form Factor & Foldable Layout Engine.
 * Provides responsive breakpoint detection and dual-pane canonical layouts
 * for handheld phones, foldables, tablets, and desktop/DeX form factors.
 */
object AdaptiveLayoutHelper {

    enum class WindowWidthSizeClass {
        COMPACT,  // < 600dp (standard portrait phones)
        MEDIUM,   // 600dp - 839dp (foldables unfolded, small tablets, landscape phones)
        EXPANDED  // >= 840dp (large tablets, desktop, ChromeOS)
    }

    enum class FoldablePosture {
        FLAT,
        TABLETOP,
        BOOK
    }

    @Composable
    fun getWindowWidthSizeClass(): WindowWidthSizeClass {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.dp
        return when {
            screenWidthDp < 600.dp -> WindowWidthSizeClass.COMPACT
            screenWidthDp < 840.dp -> WindowWidthSizeClass.MEDIUM
            else -> WindowWidthSizeClass.EXPANDED
        }
    }

    @Composable
    fun isTabletOrExpanded(): Boolean {
        return getWindowWidthSizeClass() != WindowWidthSizeClass.COMPACT
    }
}

/**
 * Reusable Adaptive Two-Pane Layout. Renders side-by-side split panes on Medium/Expanded
 * displays and single-pane vertical flows on Compact devices.
 */
@Composable
fun AdaptiveSplitPane(
    primaryPane: @Composable BoxScope.() -> Unit,
    secondaryPane: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    splitRatio: Float = 0.45f,
    paneSpacing: Dp = 16.dp
) {
    val sizeClass = AdaptiveLayoutHelper.getWindowWidthSizeClass()

    if (sizeClass == AdaptiveLayoutHelper.WindowWidthSizeClass.COMPACT) {
        // Vertical stacked layout for phones
        Column(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                primaryPane()
            }
            Spacer(modifier = Modifier.height(paneSpacing))
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                secondaryPane()
            }
        }
    } else {
        // Horizontal side-by-side layout for tablets and unfolded foldables
        Row(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(splitRatio)
                    .fillMaxHeight()
            ) {
                primaryPane()
            }
            Spacer(modifier = Modifier.width(paneSpacing))
            Box(
                modifier = Modifier
                    .weight(1f - splitRatio)
                    .fillMaxHeight()
            ) {
                secondaryPane()
            }
        }
    }
}
