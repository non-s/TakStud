package com.example.takstud.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AdvancedBottomNavigation - Bottom navigation com recursos avançados.
 *
 * FUNCIONALIDADES:
 * - Múltiplas abas com badges/notificações
 * - Transições suaves entre seções
 * - Deep linking support
 * - Preservação de estado entre abas
 * - Ícones com labels customizados
 * - Animações de seleção
 *
 * ESTRUTURA:
 * - NavigationItem: Modelo para itens da navegação
 * - BottomNavigationBar: Componente principal
 * - NavigationBadge: Identificador visual para notificações
 *
 * EXEMPLO DE USO:
 * val items = listOf(
 *     NavigationItem(
 *         label = "Grades",
 *         icon = Icons.Default.School,
 *         badge = BadgeContent(count = 3)
 *     ),
 *     NavigationItem(
 *         label = "Tarefas",
 *         icon = Icons.Default.Assignment
 *     )
 * )
 * AdvancedBottomNav(
 *     items = items,
 *     selectedIndex = selectedIndex,
 *     onIndexChange = { selectedIndex = it }
 * )
 */

/**
 * Conteúdo do badge para notificações.
 */
data class BadgeContent(
    val count: Int? = null,
    val hasNotification: Boolean = false,
    val label: String? = null
)

/**
 * Item de navegação com ícone, label e badge.
 */
data class NavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badge: BadgeContent? = null,
    val contentDescription: String = label,
    val enabled: Boolean = true,
    val deepLink: String? = null
)

/**
 * Bottom navigation bar avançada com badges e animações.
 */
@Composable
fun AdvancedBottomNav(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    NavigationBar(
        modifier = modifier
            .height(80.dp)
            .semantics {
                contentDescription = "Navegação principal com ${items.size} abas"
            },
        containerColor = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = {
                    if (item.enabled) {
                        onIndexChange(index)
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badge != null) {
                                NavigationBadgeContent(item.badge)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription,
                            modifier = Modifier
                                .size(24.dp)
                                .semantics {
                                    contentDescription = item.contentDescription
                                }
                        )
                    }
                },
                enabled = item.enabled,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "${item.label} ${if (index == selectedIndex) "selecionado" else ""}"
                    }
            )
        }
    }
}

/**
 * Badge para exibir notificações (contador ou indicador).
 */
@Composable
fun NavigationBadgeContent(badge: BadgeContent) {
    when {
        badge.count != null -> {
            Badge(
                modifier = Modifier.offset(x = 12.dp, y = -8.dp),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = if (badge.count > 99) "99+" else badge.count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
        badge.hasNotification -> {
            Badge(
                modifier = Modifier.offset(x = 12.dp, y = -8.dp),
                containerColor = MaterialTheme.colorScheme.error
            )
        }
        badge.label != null -> {
            Badge(
                modifier = Modifier.offset(x = 12.dp, y = -8.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ) {
                Text(
                    text = badge.label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Controlador de estado para navegação avançada com preservação de estado.
 */
class AdvancedNavigationController {
    private val _selectedIndex = mutableIntStateOf(0)
    val selectedIndex: Int get() = _selectedIndex.intValue

    private val _savedStates = mutableMapOf<Int, Any>()
    val savedStates: Map<Int, Any> get() = _savedStates

    fun selectTab(index: Int) {
        _selectedIndex.intValue = index
    }

    fun saveState(tabIndex: Int, state: Any) {
        _savedStates[tabIndex] = state
    }

    fun getState(tabIndex: Int): Any? {
        return _savedStates[tabIndex]
    }

    fun clearState(tabIndex: Int) {
        _savedStates.remove(tabIndex)
    }

    fun clearAllStates() {
        _savedStates.clear()
    }
}

/**
 * Composable que lembra o controlador de navegação.
 */
@Composable
fun rememberAdvancedNavigationController(): AdvancedNavigationController {
    return remember { AdvancedNavigationController() }
}

/**
 * Composable com navigation rail para modo landscape/tablet.
 */
@Composable
fun NavigationRail(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedContentColor: Color = MaterialTheme.colorScheme.primary
) {
    NavigationRail(
        modifier = modifier.width(80.dp),
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                selected = index == selectedIndex,
                onClick = {
                    if (item.enabled) {
                        onIndexChange(index)
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badge != null) {
                                NavigationBadgeContent(item.badge)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                enabled = item.enabled,
                modifier = Modifier.semantics {
                    contentDescription = item.label
                }
            )
        }
    }
}

/**
 * Composable para navegação responsiva (bottom em portrait, rail em landscape).
 */
@Composable
fun ResponsiveNavigation(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isPortrait: Boolean = true
) {
    if (isPortrait) {
        AdvancedBottomNav(
            items = items,
            selectedIndex = selectedIndex,
            onIndexChange = onIndexChange,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        NavigationRail(
            items = items,
            selectedIndex = selectedIndex,
            onIndexChange = onIndexChange,
            modifier = modifier
        )
    }
}

/**
 * Deep link handler para navegação.
 */
object DeepLinkHandler {
    fun getIndexFromDeepLink(items: List<NavigationItem>, deepLink: String): Int? {
        return items.indexOfFirst { it.deepLink == deepLink }.takeIf { it >= 0 }
    }

    fun getDeepLinkFromIndex(items: List<NavigationItem>, index: Int): String? {
        return items.getOrNull(index)?.deepLink
    }
}

/**
 * Construtor fluente para NavigationItem.
 */
class NavigationItemBuilder {
    var id: String = ""
    var label: String = ""
    var icon: ImageVector = Icons.Default.Home
    var badge: BadgeContent? = null
    var contentDescription: String = ""
    var enabled: Boolean = true
    var deepLink: String? = null

    fun build(): NavigationItem {
        return NavigationItem(
            id = id,
            label = label,
            icon = icon,
            badge = badge,
            contentDescription = contentDescription.ifEmpty { label },
            enabled = enabled,
            deepLink = deepLink
        )
    }
}

/**
 * DSL para criar NavigationItem.
 */
fun navigationItem(block: NavigationItemBuilder.() -> Unit): NavigationItem {
    return NavigationItemBuilder().apply(block).build()
}
