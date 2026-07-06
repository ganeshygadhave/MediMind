package com.medrem.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * MedRem Shape System — Rounded aesthetic
 * Based on DESIGN.md: 16dp-24dp corner radius for containers.
 */
val MedRemShapes = Shapes(
    // Small elements: Chips, small badges
    extraSmall = RoundedCornerShape(4.dp),
    // Buttons, text fields
    small = RoundedCornerShape(12.dp),
    // Cards, dialogs
    medium = RoundedCornerShape(16.dp),
    // Bottom sheets, large cards
    large = RoundedCornerShape(24.dp),
    // FABs, pill-shaped buttons
    extraLarge = RoundedCornerShape(28.dp),
)
