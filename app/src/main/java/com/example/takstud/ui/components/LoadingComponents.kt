package com.example.takstud.ui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.takstud.ui.theme.AccentBlue
import com.example.takstud.ui.theme.LightGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite

/**
 * ⏳ Componentes de carregamento
 * Inclui skeleton screens, spinners e indicadores de progresso
 */

/**
 * 💫 SkeletonCard - Card de carregamento animado
 * @param modifier Modifier para customizar
 */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeletonAnimation")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Título skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(16.dp)
                .background(color = LightGray.copy(alpha = shimmerAlpha), shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(12.dp)
                .background(color = LightGray.copy(alpha = shimmerAlpha), shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Conteúdo skeleton (múltiplas linhas)
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(color = LightGray.copy(alpha = shimmerAlpha), shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * 🧩 SkeletonShimmer - Efeito shimmer genérico
 * @param width Largura
 * @param height Altura
 * @param modifier Modifier
 */
@Composable
fun SkeletonShimmer(
    width: Modifier = Modifier.fillMaxWidth(),
    height: Modifier = Modifier.height(16.dp),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerAnimation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = width
            .then(height)
            .then(modifier)
            .background(color = LightGray.copy(alpha = alpha), shape = RoundedCornerShape(4.dp))
    )
}

/**
 * ⏳ LoadingSpinner - Spinner circular de carregamento
 * @param size Tamanho do spinner
 * @param color Cor do spinner
 */
@Composable
fun LoadingSpinner(
    size: Modifier = Modifier.size(48.dp),
    color: Color = AccentBlue
) {
    CircularProgressIndicator(
        modifier = size,
        color = color,
        strokeWidth = 4.dp
    )
}

/**
 * 📊 LoadingStatisticCard - Card de estatística em carregamento
 */
@Composable
fun LoadingStatisticCard(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingAnimation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(color = PureWhite, shape = RoundedCornerShape(12.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .background(color = LightGray.copy(alpha = alpha), shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .background(color = LightGray.copy(alpha = alpha), shape = RoundedCornerShape(4.dp))
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color = LightGray.copy(alpha = alpha), shape = RoundedCornerShape(12.dp))
            )
        }
    }
}

/**
 * 📋 LoadingListItem - Item de lista em carregamento
 */
@Composable
fun LoadingListItem(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "listItemAnimation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "itemAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = LightGray.copy(alpha = alpha), shape = RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp)
                        .background(color = LightGray.copy(alpha = alpha), shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .background(color = LightGray.copy(alpha = alpha), shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * 🔄 LoadingDashboardSkeleton - Skeleton de dashboard completo
 */
@Composable
fun LoadingDashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header skeleton
        SkeletonShimmer(
            width = Modifier.fillMaxWidth(0.7f),
            height = Modifier.height(24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Estatísticas
        repeat(3) {
            LoadingStatisticCard()
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Seção skeleton
        SkeletonShimmer(
            width = Modifier.fillMaxWidth(0.6f),
            height = Modifier.height(18.dp)
        )

        // Itens
        repeat(2) {
            LoadingListItem()
        }
    }
}

/**
 * 🎯 CenteredLoadingSpinner - Spinner centralizado na tela
 */
@Composable
fun CenteredLoadingSpinner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LoadingSpinner()
            androidx.compose.material3.Text(
                text = "Carregando...",
                color = NavyBlue
            )
        }
    }
}

/**
 * ⏳ LinearProgressBar - Barra de progresso linear
 * @param progress Progresso (0-1)
 */
@Composable
fun LinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(color = LightGray, shape = RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .background(color = AccentBlue, shape = RoundedCornerShape(2.dp))
        )
    }
}

/**
 * 🔘 DotLoadingAnimation - Animação de 3 pontos
 */
@Composable
fun DotLoadingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotAnimation")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(600, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = AccentBlue.copy(alpha = dot1Alpha), shape = RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = AccentBlue.copy(alpha = dot2Alpha), shape = RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = AccentBlue.copy(alpha = dot3Alpha), shape = RoundedCornerShape(4.dp))
        )
    }
}