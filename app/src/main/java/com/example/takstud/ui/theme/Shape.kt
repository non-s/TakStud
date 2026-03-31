package com.example.takstud.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // Padrão para Cards
    large = RoundedCornerShape(24.dp),  // Padrão para Dialogs/BottomSheets
    extraLarge = RoundedCornerShape(32.dp)
)
