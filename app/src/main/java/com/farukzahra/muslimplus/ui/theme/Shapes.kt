package com.farukzahra.muslimplus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Cartões de 20 dp, como no restante do app. As formas ficam no tema para que
 * Card, ModalBottomSheet e diálogos não inventem raios diferentes.
 */
val MuslimPlusShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
