package com.example.calculator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CalculatorButtonType {
    NUMBER,
    OPERATOR,
    ACTION,
    SPECIAL
}

@Composable
fun CalculatorButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = text,
    testTag: String,
    backgroundColor: Color,
    contentColor: Color,
    fontSize: TextUnit = 26.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    isHapticEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Using custom scale & ripple via box click
                role = Role.Button,
                onClick = {
                    if (isHapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onClick()
                }
            )
            .testTag(testTag)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
