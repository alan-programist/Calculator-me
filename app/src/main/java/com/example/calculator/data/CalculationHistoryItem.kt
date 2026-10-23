package com.example.calculator.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a single saved calculation in history.
 */
data class CalculationHistoryItem(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
