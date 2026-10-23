package com.example.calculator.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Supported calculator operations.
 * Includes all 5 core operations: +, −, ×, ÷, %
 */
enum class Operation(val symbol: String) {
    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    DIVIDE("÷"),
    PERCENT("%");

    companion object {
        fun fromSymbol(symbol: String): Operation? = entries.find { it.symbol == symbol }
    }
}

/**
 * Result state of a calculation.
 */
sealed class CalculationResult {
    data class Success(val value: Double, val formatted: String) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
}

/**
 * High-precision, rock-solid calculator engine supporting Double precision,
 * all 5 basic operations (+, −, ×, ÷, %), and division by zero protection.
 */
object CalculatorEngine {

    private const val MAX_DISPLAY_DIGITS = 12
    private val mathContext = MathContext(14, RoundingMode.HALF_UP)

    /**
     * Formats a Double into a clean, human-readable string without trailing ".0",
     * avoiding ugly floating point inaccuracies (e.g. 0.30000000000000004 -> 0.3).
     */
    fun formatDouble(value: Double): String {
        if (value.isNaN()) return "Не число"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        // Handle exact zero
        if (value == 0.0 || value == -0.0) return "0"

        // For very large or very small magnitudes, use scientific notation
        val absVal = kotlin.math.abs(value)
        if (absVal >= 1e11 || (absVal < 1e-6 && absVal > 0.0)) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val sciFormat = DecimalFormat("0.######E0", symbols)
            return sciFormat.format(value)
        }

        // Use BigDecimal rounding to strip floating-point precision noise
        val bd = BigDecimal.valueOf(value).round(mathContext).stripTrailingZeros()
        val plain = bd.toPlainString()

        // If it's a whole number, return as is
        if (!plain.contains(".")) return plain

        // If decimal, limit to 10 decimal digits
        val parts = plain.split(".")
        val integerPart = parts[0]
        val decimalPart = parts[1].take(10).trimEnd('0')

        return if (decimalPart.isEmpty()) integerPart else "$integerPart.$decimalPart"
    }

    /**
     * Executes one of the basic operations with full division-by-zero protection.
     */
    fun calculate(first: Double, second: Double, op: Operation): CalculationResult {
        return try {
            val a = BigDecimal.valueOf(first)
            val b = BigDecimal.valueOf(second)

            when (op) {
                Operation.ADD -> {
                    val res = a.add(b, mathContext).toDouble()
                    CalculationResult.Success(res, formatDouble(res))
                }
                Operation.SUBTRACT -> {
                    val res = a.subtract(b, mathContext).toDouble()
                    CalculationResult.Success(res, formatDouble(res))
                }
                Operation.MULTIPLY -> {
                    val res = a.multiply(b, mathContext).toDouble()
                    CalculationResult.Success(res, formatDouble(res))
                }
                Operation.DIVIDE -> {
                    // Check for division by zero (or extremely close to zero)
                    if (kotlin.math.abs(second) < 1e-15) {
                        CalculationResult.Error("Деление на ноль невозможно")
                    } else {
                        val res = a.divide(b, mathContext).toDouble()
                        CalculationResult.Success(res, formatDouble(res))
                    }
                }
                Operation.PERCENT -> {
                    // Percentage calculation: (first * second) / 100
                    val res = a.multiply(b, mathContext)
                        .divide(BigDecimal.valueOf(100), mathContext)
                        .toDouble()
                    CalculationResult.Success(res, formatDouble(res))
                }
            }
        } catch (e: ArithmeticException) {
            CalculationResult.Error(e.message ?: "Ошибка вычисления")
        } catch (e: Exception) {
            CalculationResult.Error("Ошибка")
        }
    }

    /**
     * Computes percentage of a single value (e.g. 50% -> 0.5)
     */
    fun singlePercent(value: Double): CalculationResult {
        return try {
            val bd = BigDecimal.valueOf(value).divide(BigDecimal.valueOf(100), mathContext)
            val res = bd.toDouble()
            CalculationResult.Success(res, formatDouble(res))
        } catch (e: Exception) {
            CalculationResult.Error("Ошибка")
        }
    }

    /**
     * Square root (√x)
     */
    fun squareRoot(value: Double): CalculationResult {
        if (value < 0) {
            return CalculationResult.Error("Корень из отрицательного числа")
        }
        val res = kotlin.math.sqrt(value)
        return CalculationResult.Success(res, formatDouble(res))
    }

    /**
     * Power of 2 (x²)
     */
    fun square(value: Double): CalculationResult {
        val bd = BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(value), mathContext)
        val res = bd.toDouble()
        return CalculationResult.Success(res, formatDouble(res))
    }

    /**
     * Reciprocal (1/x)
     */
    fun reciprocal(value: Double): CalculationResult {
        if (kotlin.math.abs(value) < 1e-15) {
            return CalculationResult.Error("Деление на ноль невозможно")
        }
        val res = 1.0 / value
        return CalculationResult.Success(res, formatDouble(res))
    }
}
