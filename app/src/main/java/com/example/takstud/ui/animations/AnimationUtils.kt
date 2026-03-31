package com.example.takstud.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * AnimationUtils - Helpers para animações e transições.
 *
 * FUNCIONALIDADES:
 * - Transições de slide
 * - Fade in/out
 * - Scale animations
 * - Animações customizadas
 * - Material motion principles
 *
 * ESPECIFICAÇÕES:
 * - Short (150ms): Alterações de UI
 * - Standard (300ms): Transições
 * - Long (450ms): Entradas/saídas
 *
 * EXEMPLO DE USO:
 * AnimatedVisibility(
 *     visible = isVisible,
 *     enter = slideInVertically() + fadeIn(),
 *     exit = slideOutVertically() + fadeOut()
 * ) {
 *     MyContent()
 * }
 */

object AnimationDurations {
    const val SHORT = 150       // Mudanças rápidas de UI
    const val STANDARD = 300    // Transições padrão
    const val LONG = 450        // Entradas/saídas
}

/**
 * Animações padrão do Material Design.
 */
object MaterialAnimations {
    /**
     * Slide In: Entra deslizando da borda.
     */
    fun slideInFromStart(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    fun slideInFromEnd(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    fun slideInFromTop(): EnterTransition =
        slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    fun slideInFromBottom(): EnterTransition =
        slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    /**
     * Slide Out: Sai deslizando para a borda.
     */
    fun slideOutToStart(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    fun slideOutToEnd(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    fun slideOutToTop(): ExitTransition =
        slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    fun slideOutToBottom(): ExitTransition =
        slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    /**
     * Scale: Cresce ou diminui.
     */
    fun scaleIn(): EnterTransition =
        scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.SHORT))

    fun scaleOut(): ExitTransition =
        scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(durationMillis = AnimationDurations.STANDARD)
        ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.SHORT))

    /**
     * Fade: Apenas opacidade.
     */
    fun fadeInOnly(): EnterTransition =
        fadeIn(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))

    fun fadeOutOnly(): ExitTransition =
        fadeOut(animationSpec = tween(durationMillis = AnimationDurations.STANDARD))
}

/**
 * Animação de infinitude (loading, shimmer, etc).
 */
object InfiniteAnimations {
    /**
     * Animação de loading (rotação).
     */
    @Composable
    fun loadingRotation(): Float {
        val rotation = rememberInfiniteTransition(label = "loading")
            .animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 800,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
        return rotation.value
    }

    /**
     * Animação de pulsing (respiração).
     */
    @Composable
    fun pulsingAlpha(): Float {
        val alpha = rememberInfiniteTransition(label = "pulsing")
            .animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1500,
                        easing = EaseInOutQuad
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
        return alpha.value
    }

    /**
     * Animação de shimmer (carregamento de imagem).
     */
    @Composable
    fun shimmerAnimation(): Float {
        val shimmer = rememberInfiniteTransition(label = "shimmer")
            .animateFloat(
                initialValue = -1f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "shimmer"
            )
        return shimmer.value
    }
}

/**
 * Modificadores de animação reutilizáveis.
 */
object AnimationModifiers {
    /**
     * Aplica efeito de pressão (pressão visual).
     */
    @Composable
    fun Modifier.pressEffect(isPressed: Boolean): Modifier {
        val scale = animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "press"
        )
        return this.graphicsLayer(
            scaleX = scale.value,
            scaleY = scale.value
        )
    }

    /**
     * Aplica efeito de foco (outline animado).
     */
    @Composable
    fun Modifier.focusEffect(hasFocus: Boolean): Modifier {
        val alpha = animateFloatAsState(
            targetValue = if (hasFocus) 1f else 0f,
            animationSpec = tween(durationMillis = AnimationDurations.SHORT),
            label = "focus"
        )
        return this.drawBehind {
            if (alpha.value > 0) {
                val strokeWidth = 2.dp.toPx()
                drawRect(
                    color = Color.Blue,
                    style = Stroke(
                        width = strokeWidth
                    ),
                    alpha = alpha.value
                )
            }
        }
    }
}

/**
 * Easing functions customizadas.
 */
object CustomEasings {
    val easeInOutQuad: Easing = Easing { t ->
        if (t < 0.5) 2 * t * t else -1 + (4 - 2 * t) * t
    }

    val easeOutCubic: Easing = Easing { t ->
        1 + (t - 1) * (t - 1) * (t - 1)
    }

    val easeInCirc: Easing = Easing { t ->
        1 - kotlin.math.sqrt(1 - t * t)
    }

    val easeOutCirc: Easing = Easing { t ->
        kotlin.math.sqrt(1 - (t - 1) * (t - 1))
    }
}

/**
 * Composable para animação de mudança de conteúdo.
 */
@Composable
fun CrossfadeAnimation(
    targetState: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Crossfade(
        targetState = targetState,
        animationSpec = tween(durationMillis = AnimationDurations.STANDARD),
        modifier = modifier,
        label = "crossfade"
    ) { state ->
        if (state) {
            content()
        }
    }
}

/**
 * Composable para animação de visibilidade com slide.
 */
@Composable
fun AnimatedSlideVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = MaterialAnimations.slideInFromBottom(),
    exit: ExitTransition = MaterialAnimations.slideOutToBottom(),
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = { content() }
    )
}

/**
 * Composable para animação de tamanho.
 */
@Composable
fun AnimatedSizeChange(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        content()
    }
}
