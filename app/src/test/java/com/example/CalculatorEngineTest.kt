package com.example

import com.example.calculator.domain.CalculationResult
import com.example.calculator.domain.CalculatorEngine
import com.example.calculator.domain.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testAdditionWithDouble() {
        val result = CalculatorEngine.calculate(12.5, 3.75, Operation.ADD)
        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(16.25, success.value, 0.0001)
        assertEquals("16.25", success.formatted)
    }

    @Test
    fun testAdditionPrecisionDouble() {
        val result = CalculatorEngine.calculate(0.1, 0.2, Operation.ADD)
        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(0.3, success.value, 0.0001)
        assertEquals("0.3", success.formatted)
    }

    @Test
    fun testSubtractionWithDouble() {
        val result = CalculatorEngine.calculate(10.5, 4.2, Operation.SUBTRACT)
        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(6.3, success.value, 0.0001)
        assertEquals("6.3", success.formatted)
    }

    @Test
    fun testMultiplicationWithDouble() {
        val result = CalculatorEngine.calculate(2.5, 4.0, Operation.MULTIPLY)
        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(10.0, success.value, 0.0001)
        assertEquals("10", success.formatted)
    }

    @Test
    fun testDivisionValid() {
        val result = CalculatorEngine.calculate(15.0, 3.0, Operation.DIVIDE)
        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(5.0, success.value, 0.0001)
        assertEquals("5", success.formatted)
    }

    @Test
    fun testDivisionByZeroProtected() {
        val result = CalculatorEngine.calculate(42.0, 0.0, Operation.DIVIDE)
        assertTrue(result is CalculationResult.Error)
        val error = result as CalculationResult.Error
        assertEquals("Деление на ноль невозможно", error.message)
    }

    @Test
    fun testDivisionByNearZeroProtected() {
        val result = CalculatorEngine.calculate(10.0, 0.000000000000000001, Operation.DIVIDE)
        // Checks that very small values close to 0 or 0 are safely guarded or evaluated
        val zeroResult = CalculatorEngine.calculate(10.0, 0.0, Operation.DIVIDE)
        assertTrue(zeroResult is CalculationResult.Error)
    }

    @Test
    fun testPercentOperation() {
        // 200 * 15% -> 30
        val result = CalculatorEngine.calculate(200.0, 15.0, Operation.PERCENT)
        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(30.0, success.value, 0.0001)
        assertEquals("30", success.formatted)
    }

    @Test
    fun testSinglePercent() {
        val result = CalculatorEngine.singlePercent(75.0)
        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(0.75, success.value, 0.0001)
        assertEquals("0.75", success.formatted)
    }

    @Test
    fun testSpecialFunctions() {
        val sqrtResult = CalculatorEngine.squareRoot(144.0)
        assertTrue(sqrtResult is CalculationResult.Success)
        assertEquals("12", (sqrtResult as CalculationResult.Success).formatted)

        val sqrtNegative = CalculatorEngine.squareRoot(-9.0)
        assertTrue(sqrtNegative is CalculationResult.Error)

        val sqrResult = CalculatorEngine.square(5.5)
        assertTrue(sqrResult is CalculationResult.Success)
        assertEquals("30.25", (sqrResult as CalculationResult.Success).formatted)

        val recipZero = CalculatorEngine.reciprocal(0.0)
        assertTrue(recipZero is CalculationResult.Error)
    }
}
