package com.example.calculator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.domain.Operation
import com.example.calculator.ui.components.CalculatorButton
import com.example.calculator.ui.components.CalculatorDisplay
import com.example.calculator.ui.components.HistoryBottomSheet
import com.example.ui.theme.LocalCalculatorColors

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalCalculatorColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val isCompactHeight = maxHeight < 650.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Display section
                CalculatorDisplay(
                    uiState = uiState,
                    onHistoryClick = { viewModel.toggleHistory(true) },
                    onToggleScientific = { viewModel.toggleScientific() },
                    onToggleHaptic = { viewModel.toggleHaptic() },
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Keypad section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Optional Scientific Function Row
                    AnimatedVisibility(
                        visible = uiState.isScientificExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                text = "√x",
                                testTag = "button_sqrt",
                                backgroundColor = colors.specialKey,
                                contentColor = colors.specialText,
                                fontSize = 18.sp,
                                isHapticEnabled = uiState.isHapticEnabled,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onSpecial(SpecialFunction.SQRT) }
                            )
                            CalculatorButton(
                                text = "x²",
                                testTag = "button_square",
                                backgroundColor = colors.specialKey,
                                contentColor = colors.specialText,
                                fontSize = 18.sp,
                                isHapticEnabled = uiState.isHapticEnabled,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onSpecial(SpecialFunction.SQUARE) }
                            )
                            CalculatorButton(
                                text = "1/x",
                                testTag = "button_reciprocal",
                                backgroundColor = colors.specialKey,
                                contentColor = colors.specialText,
                                fontSize = 18.sp,
                                isHapticEnabled = uiState.isHapticEnabled,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onSpecial(SpecialFunction.RECIPROCAL) }
                            )
                            CalculatorButton(
                                text = "00",
                                testTag = "button_double_zero",
                                backgroundColor = colors.specialKey,
                                contentColor = colors.specialText,
                                fontSize = 18.sp,
                                isHapticEnabled = uiState.isHapticEnabled,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.onDigit("0")
                                    viewModel.onDigit("0")
                                }
                            )
                        }
                    }

                    // Keypad Row 1: [ AC / C ] [ ± ] [ % ] [ ÷ ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalculatorButton(
                            text = uiState.clearButtonLabel,
                            testTag = "button_clear",
                            backgroundColor = colors.specialKey,
                            contentColor = if (uiState.errorMessage != null) colors.errorColor else colors.specialText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onClear() }
                        )
                        CalculatorButton(
                            text = "±",
                            testTag = "button_toggle_sign",
                            backgroundColor = colors.specialKey,
                            contentColor = colors.specialText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onToggleSign() }
                        )
                        CalculatorButton(
                            text = "%",
                            testTag = "button_percent",
                            backgroundColor = colors.operatorKey,
                            contentColor = colors.operatorText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onPercent() }
                        )
                        CalculatorButton(
                            text = "÷",
                            testTag = "button_divide",
                            backgroundColor = if (uiState.pendingOperation == Operation.DIVIDE) colors.actionAccent else colors.operatorKey,
                            contentColor = if (uiState.pendingOperation == Operation.DIVIDE) colors.actionText else colors.operatorText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperation(Operation.DIVIDE) }
                        )
                    }

                    // Keypad Row 2: [ 7 ] [ 8 ] [ 9 ] [ × ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalculatorButton(
                            text = "7",
                            testTag = "button_7",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("7") }
                        )
                        CalculatorButton(
                            text = "8",
                            testTag = "button_8",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("8") }
                        )
                        CalculatorButton(
                            text = "9",
                            testTag = "button_9",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("9") }
                        )
                        CalculatorButton(
                            text = "×",
                            testTag = "button_multiply",
                            backgroundColor = if (uiState.pendingOperation == Operation.MULTIPLY) colors.actionAccent else colors.operatorKey,
                            contentColor = if (uiState.pendingOperation == Operation.MULTIPLY) colors.actionText else colors.operatorText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperation(Operation.MULTIPLY) }
                        )
                    }

                    // Keypad Row 3: [ 4 ] [ 5 ] [ 6 ] [ − ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalculatorButton(
                            text = "4",
                            testTag = "button_4",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("4") }
                        )
                        CalculatorButton(
                            text = "5",
                            testTag = "button_5",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("5") }
                        )
                        CalculatorButton(
                            text = "6",
                            testTag = "button_6",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("6") }
                        )
                        CalculatorButton(
                            text = "−",
                            testTag = "button_subtract",
                            backgroundColor = if (uiState.pendingOperation == Operation.SUBTRACT) colors.actionAccent else colors.operatorKey,
                            contentColor = if (uiState.pendingOperation == Operation.SUBTRACT) colors.actionText else colors.operatorText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperation(Operation.SUBTRACT) }
                        )
                    }

                    // Keypad Row 4: [ 1 ] [ 2 ] [ 3 ] [ + ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalculatorButton(
                            text = "1",
                            testTag = "button_1",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("1") }
                        )
                        CalculatorButton(
                            text = "2",
                            testTag = "button_2",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("2") }
                        )
                        CalculatorButton(
                            text = "3",
                            testTag = "button_3",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("3") }
                        )
                        CalculatorButton(
                            text = "+",
                            testTag = "button_add",
                            backgroundColor = if (uiState.pendingOperation == Operation.ADD) colors.actionAccent else colors.operatorKey,
                            contentColor = if (uiState.pendingOperation == Operation.ADD) colors.actionText else colors.operatorText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperation(Operation.ADD) }
                        )
                    }

                    // Keypad Row 5: [ 0 ] [ . ] [ ⌫ ] [ = ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalculatorButton(
                            text = "0",
                            testTag = "button_0",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigit("0") }
                        )
                        CalculatorButton(
                            text = ".",
                            testTag = "button_decimal",
                            backgroundColor = colors.numberKey,
                            contentColor = colors.numberText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDecimal() }
                        )
                        CalculatorButton(
                            icon = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Удалить символ",
                            testTag = "button_backspace",
                            backgroundColor = colors.specialKey,
                            contentColor = colors.specialText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onBackspace() }
                        )
                        CalculatorButton(
                            text = "=",
                            testTag = "button_equals",
                            backgroundColor = colors.actionAccent,
                            contentColor = colors.actionText,
                            isHapticEnabled = uiState.isHapticEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onEquals() }
                        )
                    }
                }
            }
        }

        // History Bottom Sheet
        if (uiState.isHistoryOpen) {
            HistoryBottomSheet(
                history = uiState.history,
                onDismiss = { viewModel.toggleHistory(false) },
                onSelectItem = { item -> viewModel.onSelectHistoryItem(item) },
                onClearHistory = { viewModel.onClearHistory() }
            )
        }
    }
}
