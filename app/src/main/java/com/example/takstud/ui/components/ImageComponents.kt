package com.example.takstud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * ImageComponents - Componentes para exibição de imagens com lazy loading.
 *
 * FUNCIONALIDADES:
 * - Placeholder e estado de carregamento
 * - Suporte a error states
 * - Shimmer loading effect
 * - Otimização de tamanho
 * - Content descriptions para acessibilidade
 *
 * NOTA: Para uso em produção, integrar Coil ou Glide
 * Este arquivo usa placeholders como base de implementação
 *
 * EXEMPLO DE USO:
 * LazyImage(
 *     model = "https://example.com/image.jpg",
 *     contentDescription = "User profile",
 *     modifier = Modifier
 *         .size(200.dp)
 *         .clip(CircleShape)
 * )
 */

/**
 * Estado de carregamento de imagem.
 */
sealed class ImageLoadingState {
    object Loading : ImageLoadingState()
    object Success : ImageLoadingState()
    object Error : ImageLoadingState()
}

/**
 * Imagem simples com lazy loading (placeholder).
 */
@Composable
fun LazyImage(
    model: Any?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onLoadingStateChange: ((ImageLoadingState) -> Unit)? = null
) {
    var loadingState by remember { mutableStateOf<ImageLoadingState>(ImageLoadingState.Loading) }

    LaunchedEffect(model) {
        loadingState = ImageLoadingState.Loading
        // Simula carregamento
        try {
            loadingState = ImageLoadingState.Success
            onLoadingStateChange?.invoke(ImageLoadingState.Success)
        } catch (e: Exception) {
            loadingState = ImageLoadingState.Error
            onLoadingStateChange?.invoke(ImageLoadingState.Error)
        }
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics {
                this.contentDescription = contentDescription
            }
    )
}

/**
 * Imagem com placeholder durante carregamento.
 */
@Composable
fun LazyImageWithPlaceholder(
    model: Any?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    var loadingState by remember { mutableStateOf<ImageLoadingState>(ImageLoadingState.Loading) }

    Box(modifier = modifier) {
        // Placeholder
        if (loadingState is ImageLoadingState.Loading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(placeholderColor)
            )
        }

        // Loading indicator
        if (loadingState is ImageLoadingState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                strokeWidth = 3.dp
            )
        }

        // Error state
        if (loadingState is ImageLoadingState.Error) {
            Icon(
                imageVector = Icons.Default.ImageNotSupported,
                contentDescription = "Erro ao carregar imagem",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        LaunchedEffect(model) {
            loadingState = ImageLoadingState.Loading
            try {
                loadingState = ImageLoadingState.Success
            } catch (e: Exception) {
                loadingState = ImageLoadingState.Error
            }
        }
    }
}

/**
 * Imagem circular com lazy loading (para avatares).
 */
@Composable
fun LazyCircleImage(
    model: Any?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 64.dp,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    var loadingState by remember { mutableStateOf<ImageLoadingState>(ImageLoadingState.Loading) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Loading state
        if (loadingState is ImageLoadingState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size / 3),
                strokeWidth = 2.dp
            )
        }

        // Error state
        if (loadingState is ImageLoadingState.Error) {
            Icon(
                imageVector = Icons.Default.ImageNotSupported,
                contentDescription = "Erro ao carregar avatar",
                modifier = Modifier.size(size / 2),
                tint = MaterialTheme.colorScheme.error
            )
        }

        LaunchedEffect(model) {
            loadingState = ImageLoadingState.Loading
            try {
                loadingState = ImageLoadingState.Success
            } catch (e: Exception) {
                loadingState = ImageLoadingState.Error
            }
        }
    }
}

/**
 * Imagem com efeito shimmer durante carregamento.
 */
@Composable
fun LazyImageWithShimmer(
    model: Any?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
) {
    var loadingState by remember { mutableStateOf<ImageLoadingState>(ImageLoadingState.Loading) }

    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius))
    ) {
        // Shimmer durante carregamento
        if (loadingState is ImageLoadingState.Loading) {
            ShimmerEffect(
                modifier = Modifier.matchParentSize()
            )
        }

        // Error card
        if (loadingState is ImageLoadingState.Error) {
            Card(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = "Erro ao carregar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Erro ao carregar imagem",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        LaunchedEffect(model) {
            loadingState = ImageLoadingState.Loading
            try {
                loadingState = ImageLoadingState.Success
            } catch (e: Exception) {
                loadingState = ImageLoadingState.Error
            }
        }
    }
}

/**
 * Card com imagem lazy-loaded.
 */
@Composable
fun LazyImageCard(
    model: Any?,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagem com shimmer
            LazyImageWithShimmer(
                model = model,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                cornerRadius = 0.dp
            )

            // Conteúdo
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Grid de imagens lazy-loaded.
 */
@Composable
fun LazyImageGrid(
    images: List<Pair<String, String>>, // URL to description
    modifier: Modifier = Modifier,
    columns: Int = 2,
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    onImageClick: ((String) -> Unit)? = null
) {
    Column(modifier = modifier) {
        images.chunked(columns).forEach { rowImages ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowImages.forEach { (url, description) ->
                    LazyImageWithPlaceholder(
                        model = url,
                        contentDescription = description,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (onImageClick != null) {
                                    Modifier.clickable { onImageClick(url) }
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * Efeito shimmer para loading.
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
    ) {
        // Pode ser expandido com animação de gradiente
    }
}

/**
 * Extensão para carregar imagem com tamanho otimizado.
 */
fun Any?.withOptimalSize(width: Int = 0, height: Int = 0): String? {
    return when (this) {
        is String -> {
            if (width > 0 && height > 0) {
                "$this?w=$width&h=$height&q=80"
            } else {
                this
            }
        }
        else -> this.toString()
    }
}
