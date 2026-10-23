package com.example.calculator.ui

import androidx.lifecycle.ViewModel
import com.example.calculator.data.CalculationHistoryItem
import com.example.calculator.domain.CalculationResult
import com.example.calculator.domain.CalculatorEngine
import com.example.calculator.domain.Operation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SpecialFunction {
    SQRT,
    SQUARE,
    RECIPROCAL
}

data class CalculatorUiState(
    val displayValue: String = "0",
    val expression: String = "",
    val livePreview: String? = null,
    val firstOperand: Double? = null,
    val pendingOperation: Operation? = null,
    val isEnteringNewNumber: Boolean = false,
    val errorMessage: String? = null,
    val history: List<CalculationHistoryItem> = emptyList(),
    val isHistoryOpen: Boolean = false,
    val isScientificExpanded: Boolean = false,
    val isHapticEnabled: Boolean = true
) {
    /**
     * Whether the clear button should display "C" (Clear Entry) or "AC" (All Clear).
     */
    val clearButtonLabel: String
        get() = if (errorMessage != null || (displayValue == "0" && pendingOperation == null)) "AC" else "C"
}

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onDigit(digit: String) {
        _uiState.update { state ->
            if (state.errorMessage != null) {
                // Clear error and start fresh
                return@update state.copy(
                    displayValue = digit,
                    expression = "",
                    livePreview = null,
                    firstOperand = null,
                    pendingOperation = null,
                    errorMessage = null,
                    isEnteringNewNumber = false
                )
            }

            val newDisplay = if (state.isEnteringNewNumber || state.displayValue == "0") {
                digit
            } else {
                if (state.displayValue.replace(".", "").replace("-", "").length >= 14) {
                    state.displayValue // prevent overflow
                } else {
                    state.displayValue + digit
                }
            }

            val preview = calculateLivePreview(state.firstOperand, state.pendingOperation, newDisplay)

            state.copy(
                displayValue = newDisplay,
                isEnteringNewNumber = false,
                livePreview = preview
            )
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            if (state.errorMessage != null) {
                return@update state.copy(
                    displayValue = "0.",
                    expression = "",
                    livePreview = null,
                    firstOperand = null,
                    pendingOperation = null,
                    errorMessage = null,
                    isEnteringNewNumber = false
                )
            }

            val newDisplay = if (state.isEnteringNewNumber) {
                "0."
            } else if (!state.displayValue.contains(".")) {
                state.displayValue + "."
            } else {
                state.displayValue
            }

            state.copy(
                displayValue = newDisplay,
                isEnteringNewNumber = false
            )
        }
    }

    fun onOperation(op: Operation) {
        _uiState.update { state ->
            if (state.errorMessage != null) {
                return@update state // Must clear error first
            }

            val currentNum = state.displayValue.toDoubleOrNull() ?: 0.0

            // If user already had a first operand and was entering a second number, chain the calculation
            if (state.firstOperand != null && state.pendingOperation != null && !state.isEnteringNewNumber) {
                when (val result = CalculatorEngine.calculate(state.firstOperand, currentNum, state.pendingOperation)) {
                    is CalculationResult.Success -> {
                        val intermediateFormatted = result.formatted
                        state.copy(
                            firstOperand = result.value,
                            pendingOperation = op,
                            expression = "$intermediateFormatted ${op.symbol}",
                            displayValue = intermediateFormatted,
                            livePreview = null,
                            isEnteringNewNumber = true,
                            errorMessage = null
                        )
                    }
                    is CalculationResult.Error -> {
                        state.copy(
                            errorMessage = result.message,
                            displayValue = "Ошибка",
                            expression = "${CalculatorEngine.formatDouble(state.firstOperand)} ${state.pendingOperation.symbol} $currentNum",
                            livePreview = null,
                            firstOperand = null,
                            pendingOperation = null,
                            isEnteringNewNumber = true
                        )
                    }
                }
            } else {
                // Set first operand
                val first = state.firstOperand ?: currentNum
                val firstFormatted = CalculatorEngine.formatDouble(first)
                state.copy(
                    firstOperand = first,
                    pendingOperation = op,
                    expression = "$firstFormatted ${op.symbol}",
                    livePreview = null,
                    isEnteringNewNumber = true
                )
            }
        }
    }

    fun onPercent() {
        _uiState.update { state ->
            if (state.errorMessage != null) return@update state

            val current = state.displayValue.toDoubleOrNull() ?: return@update state

            if (state.firstOperand != null && state.pendingOperation != null) {
                // Percent in binary context: e.g. 200 + 10%
                val percentVal = (state.firstOperand * current) / 100.0
                val formatted = CalculatorEngine.formatDouble(percentVal)
                val preview = calculateLivePreview(state.firstOperand, state.pendingOperation, formatted)
                state.copy(
                    displayValue = formatted,
                    livePreview = preview,
                    isEnteringNewNumber = false
                )
            } else {
                // Standalone percent: e.g. 50% = 0.5
                when (val result = CalculatorEngine.singlePercent(current)) {
                    is CalculationResult.Success -> {
                        state.copy(
                            displayValue = result.formatted,
                            expression = "$current% =",
                            livePreview = null,
                            isEnteringNewNumber = true
                        )
                    }
                    is CalculationResult.Error -> {
                        state.copy(errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onEquals() {
        _uiState.update { state ->
            if (state.errorMessage != null) return@update state

            val first = state.firstOperand
            val op = state.pendingOperation

            if (first == null || op == null) {
                return@update state // nothing to evaluate
            }

            val second = state.displayValue.toDoubleOrNull() ?: 0.0
            val firstFormatted = CalculatorEngine.formatDouble(first)
            val secondFormatted = CalculatorEngine.formatDouble(second)
            val fullExpression = "$firstFormatted ${op.symbol} $secondFormatted"

            when (val result = CalculatorEngine.calculate(first, second, op)) {
                is CalculationResult.Success -> {
                    val historyItem = CalculationHistoryItem(
                        expression = fullExpression,
                        result = result.formatted
                    )
                    state.copy(
                        displayValue = result.formatted,
                        expression = "$fullExpression =",
                        livePreview = null,
                        firstOperand = null,
                        pendingOperation = null,
                        isEnteringNewNumber = true,
                        errorMessage = null,
                        history = listOf(historyItem) + state.history.take(49)
                    )
                }
                is CalculationResult.Error -> {
                    state.copy(
                        errorMessage = result.message,
                        displayValue = "Ошибка",
                        expression = "$fullExpression =",
                        livePreview = null,
                        firstOperand = null,
                        pendingOperation = null,
                        isEnteringNewNumber = true
                    )
                }
            }
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            if (state.errorMessage != null) return@update state
            if (state.displayValue == "0") return@update state

            val toggled = if (state.displayValue.startsWith("-")) {
                state.displayValue.removePrefix("-")
            } else {
                "-${state.displayValue}"
            }

            val preview = calculateLivePreview(state.firstOperand, state.pendingOperation, toggled)
            state.copy(
                displayValue = toggled,
                livePreview = preview
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.errorMessage != null) {
                return@update onAllClear()
            }
            if (state.isEnteringNewNumber) return@update state

            val current = state.displayValue
            val newDisplay = when {
                current.length <= 1 || (current.length == 2 && current.startsWith("-")) -> "0"
                else -> current.dropLast(1)
            }

            val preview = calculateLivePreview(state.firstOperand, state.pendingOperation, newDisplay)
            state.copy(
                displayValue = newDisplay,
                livePreview = preview
            )
        }
    }

    fun onClear() {
        _uiState.update { state ->
            if (state.errorMessage != null || state.displayValue == "0") {
                // All Clear
                onAllClear()
            } else {
                // Clear Entry
                state.copy(
                    displayValue = "0",
                    livePreview = null
                )
            }
        }
    }

    fun onAllClear(): CalculatorUiState {
        val newState = CalculatorUiState(
            history = _uiState.value.history,
            isScientificExpanded = _uiState.value.isScientificExpanded,
            isHapticEnabled = _uiState.value.isHapticEnabled
        )
        _uiState.value = newState
        return newState
    }

    fun onSpecial(fn: SpecialFunction) {
        _uiState.update { state ->
            if (state.errorMessage != null) return@update state
            val current = state.displayValue.toDoubleOrNull() ?: return@update state
            val currentFormatted = CalculatorEngine.formatDouble(current)

            val (result, expr) = when (fn) {
                SpecialFunction.SQRT -> CalculatorEngine.squareRoot(current) to "√($currentFormatted)"
                SpecialFunction.SQUARE -> CalculatorEngine.square(current) to "sqr($currentFormatted)"
                SpecialFunction.RECIPROCAL -> CalculatorEngine.reciprocal(current) to "1/($currentFormatted)"
            }

            when (result) {
                is CalculationResult.Success -> {
                    val historyItem = CalculationHistoryItem(
                        expression = expr,
                        result = result.formatted
                    )
                    state.copy(
                        displayValue = result.formatted,
                        expression = "$expr =",
                        livePreview = null,
                        isEnteringNewNumber = true,
                        errorMessage = null,
                        history = listOf(historyItem) + state.history.take(49)
                    )
                }
                is CalculationResult.Error -> {
                    state.copy(
                        errorMessage = result.message,
                        displayValue = "Ошибка",
                        expression = "$expr =",
                        livePreview = null,
                        firstOperand = null,
                        pendingOperation = null,
                        isEnteringNewNumber = true
                    )
                }
            }
        }
    }

    fun onSelectHistoryItem(item: CalculationHistoryItem) {
        _uiState.update { state ->
            state.copy(
                displayValue = item.result,
                expression = item.expression,
                livePreview = null,
                firstOperand = null,
                pendingOperation = null,
                errorMessage = null,
                isEnteringNewNumber = true,
                isHistoryOpen = false
            )
        }
    }

    fun onClearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun toggleHistory(isOpen: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = isOpen) }
    }

    fun toggleScientific() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(isHapticEnabled = !it.isHapticEnabled) }
    }

    private fun calculateLivePreview(first: Double?, op: Operation?, secondStr: String): String? {
        if (first == null || op == null) return null
        val second = secondStr.toDoubleOrNull() ?: return null

        // Division by zero preview
        if (op == Operation.DIVIDE && kotlin.math.abs(second) < 1e-15) {
            return "= Деление на 0"
        }

        return when (val res = CalculatorEngine.calculate(first, second, op)) {
            is CalculationResult.Success -> "= ${res.formatted}"
            is CalculationResult.Error -> null
        }
    }
}
