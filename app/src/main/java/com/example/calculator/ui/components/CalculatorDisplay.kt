package com.example.calculator.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.CalculatorUiState
import com.example.ui.theme.LocalCalculatorColors

@Composable
fun CalculatorDisplay(
    uiState: CalculatorUiState,
    onHistoryClick: () -> Unit,
    onToggleScientific: () -> Unit,
    onToggleHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Auto scroll to the end when display value changes
    LaunchedEffect(uiState.displayValue) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Top Toolbar inside display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left toolbar: History & Scientific toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.testTag("button_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "История вычислений",
                        tint = colors.specialText
                    )
                }

                IconButton(
                    onClick = onToggleScientific,
                    modifier = Modifier.testTag("button_scientific_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Дополнительные функции",
                        tint = if (uiState.isScientificExpanded) colors.actionAccent else colors.specialText
                    )
                }

                IconButton(
                    onClick = onToggleHaptic,
                    modifier = Modifier.testTag("button_haptic_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Вибрация",
                        tint = if (uiState.isHapticEnabled) colors.actionAccent else colors.specialText.copy(alpha = 0.4f)
                    )
                }
            }

            // Right toolbar: Copy action
            IconButton(
                onClick = {
                    val textToCopy = if (uiState.errorMessage != null) {
                        uiState.errorMessage
                    } else {
                        uiState.displayValue
                    }
                    clipboardManager.setText(AnnotatedString(textToCopy))
                    Toast.makeText(context, "Скопировано: $textToCopy", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("button_copy")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Скопировать результат",
                    tint = colors.specialText
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Expression line (e.g., "12.5 × 4" or "100 + 20% =")
        Text(
            text = uiState.expression.ifEmpty { " " },
            color = colors.specialText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("display_expression")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Main Display Number
        val mainText = if (uiState.errorMessage != null) "Ошибка" else uiState.displayValue
        val textColor = if (uiState.errorMessage != null) colors.errorColor else colors.numberText

        val fontSize = when {
            mainText.length <= 6 -> 58.sp
            mainText.length <= 9 -> 46.sp
            mainText.length <= 12 -> 36.sp
            else -> 28.sp
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mainText,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .testTag("display_value")
            )
        }

        // Live preview or division-by-zero indicator
        AnimatedVisibility(
            visible = uiState.livePreview != null || uiState.errorMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            if (uiState.errorMessage != null) {
                // Prominent error banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.errorColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("error_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Предупреждение",
                            tint = colors.errorColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage,
                            color = colors.errorColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (uiState.livePreview != null) {
                Text(
                    text = uiState.livePreview,
                    color = colors.operatorText.copy(alpha = 0.8f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("display_live_preview")
                )
            }
        }
    }
}
