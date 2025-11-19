package com.example.takstud.ui.responsive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ResponsiveLayout - Helpers para layouts responsivos.
 *
 * FUNCIONALIDADES:
 * - Detecção de tamanho de tela
 * - Layouts adaptáveis
 * - Coluna/grid dinâmica
 * - Padding responsivo
 * - Font sizes adaptativos
 *
 * BREAKPOINTS:
 * - Phone: < 600dp
 * - Tablet: 600dp - 900dp
 * - Desktop: > 900dp
 *
 * EXEMPLO DE USO:
 * ResponsiveContainer { screenSize ->
 *     when (screenSize) {
 *         ScreenSize.PHONE -> PhoneLayout()
 *         ScreenSize.TABLET -> TabletLayout()
 *         ScreenSize.DESKTOP -> DesktopLayout()
 *     }
 * }
 */

enum class ScreenSize {
    PHONE,      // < 600dp
    TABLET,     // 600dp - 900dp
    DESKTOP     // > 900dp
}

/**
 * Contém informações sobre tamanho da tela.
 */
data class ScreenInfo(
    val size: ScreenSize,
    val width: Dp,
    val height: Dp,
    val isLandscape: Boolean
) {
    val isPhone: Boolean get() = size == ScreenSize.PHONE
    val isTablet: Boolean get() = size == ScreenSize.TABLET
    val isDesktop: Boolean get() = size == ScreenSize.DESKTOP
    val isPortrait: Boolean get() = !isLandscape
}

/**
 * Composable que detecta tamanho de tela.
 */
@Composable
fun ResponsiveContainer(
    content: @Composable (ScreenInfo) -> Unit
) {
    BoxWithConstraints {
        val screenSize = when {
            maxWidth < 600.dp -> ScreenSize.PHONE
            maxWidth < 900.dp -> ScreenSize.TABLET
            else -> ScreenSize.DESKTOP
        }

        val isLandscape = maxWidth > maxHeight

        val screenInfo = ScreenInfo(
            size = screenSize,
            width = maxWidth,
            height = maxHeight,
            isLandscape = isLandscape
        )

        content(screenInfo)
    }
}

/**
 * Valores responsivos para padding.
 */
object ResponsivePadding {
    fun padding(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 16.dp
        ScreenSize.TABLET -> 24.dp
        ScreenSize.DESKTOP -> 32.dp
    }

    fun horizontalPadding(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 16.dp
        ScreenSize.TABLET -> 32.dp
        ScreenSize.DESKTOP -> 48.dp
    }

    fun verticalPadding(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 12.dp
        ScreenSize.TABLET -> 16.dp
        ScreenSize.DESKTOP -> 24.dp
    }

    fun spacing(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 8.dp
        ScreenSize.TABLET -> 12.dp
        ScreenSize.DESKTOP -> 16.dp
    }
}

/**
 * Valores responsivos para tamanhos de fonte.
 */
object ResponsiveTextSize {
    fun body(screenSize: ScreenSize): androidx.compose.ui.unit.TextUnit = when (screenSize) {
        ScreenSize.PHONE -> 14.sp
        ScreenSize.TABLET -> 16.sp
        ScreenSize.DESKTOP -> 18.sp
    }

    fun title(screenSize: ScreenSize): androidx.compose.ui.unit.TextUnit = when (screenSize) {
        ScreenSize.PHONE -> 18.sp
        ScreenSize.TABLET -> 20.sp
        ScreenSize.DESKTOP -> 24.sp
    }

    fun headline(screenSize: ScreenSize): androidx.compose.ui.unit.TextUnit = when (screenSize) {
        ScreenSize.PHONE -> 24.sp
        ScreenSize.TABLET -> 28.sp
        ScreenSize.DESKTOP -> 32.sp
    }

    fun label(screenSize: ScreenSize): androidx.compose.ui.unit.TextUnit = when (screenSize) {
        ScreenSize.PHONE -> 12.sp
        ScreenSize.TABLET -> 13.sp
        ScreenSize.DESKTOP -> 14.sp
    }
}

/**
 * Valores responsivos para tamanho de elementos.
 */
object ResponsiveSize {
    fun buttonHeight(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 44.dp
        ScreenSize.TABLET -> 48.dp
        ScreenSize.DESKTOP -> 52.dp
    }

    fun cardCornerRadius(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 8.dp
        ScreenSize.TABLET -> 12.dp
        ScreenSize.DESKTOP -> 16.dp
    }

    fun iconSize(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 24.dp
        ScreenSize.TABLET -> 28.dp
        ScreenSize.DESKTOP -> 32.dp
    }

    fun listItemHeight(screenSize: ScreenSize): Dp = when (screenSize) {
        ScreenSize.PHONE -> 56.dp
        ScreenSize.TABLET -> 64.dp
        ScreenSize.DESKTOP -> 72.dp
    }
}

/**
 * Valores responsivos para número de colunas em grid.
 */
object ResponsiveGrid {
    fun columns(screenSize: ScreenSize): Int = when (screenSize) {
        ScreenSize.PHONE -> 1
        ScreenSize.TABLET -> 2
        ScreenSize.DESKTOP -> 3
    }

    fun columnWidth(screenSize: ScreenSize, screenWidth: Dp): Dp {
        val columns = columns(screenSize)
        val spacing = ResponsivePadding.spacing(screenSize)
        val totalSpacing = spacing * (columns + 1)

        return (screenWidth - totalSpacing) / columns
    }
}

/**
 * Composable para layout adaptive de lista.
 */
@Composable
fun AdaptiveListLayout(
    screenInfo: ScreenInfo,
    items: List<String>,
    itemContent: @Composable (String, Int) -> Unit
) {
    when {
        screenInfo.isPhone -> {
            // Single column para phone
            androidx.compose.foundation.lazy.LazyColumn {
                items(items.size) { index ->
                    itemContent(items[index], index)
                }
            }
        }
        screenInfo.isTablet -> {
            // Two columns para tablet
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2)
            ) {
                items(items.size) { index ->
                    itemContent(items[index], index)
                }
            }
        }
        else -> {
            // Three columns para desktop
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3)
            ) {
                items(items.size) { index ->
                    itemContent(items[index], index)
                }
            }
        }
    }
}

/**
 * Composable para drawer responsivo.
 */
@Composable
fun ResponsiveNavigation(
    screenInfo: ScreenInfo,
    drawerContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit
) {
    if (screenInfo.isPhone || (screenInfo.isTablet && screenInfo.isPortrait)) {
        // Drawer modal para phone
        androidx.compose.material3.ModalNavigationDrawer(
            drawerContent = {
                androidx.compose.material3.ModalDrawerSheet {
                    drawerContent()
                }
            },
            content = mainContent
        )
    } else {
        // Drawer permanent para tablet landscape e desktop
        androidx.compose.foundation.layout.Row {
            androidx.compose.material3.PermanentDrawerSheet {
                drawerContent()
            }
            mainContent()
        }
    }
}
