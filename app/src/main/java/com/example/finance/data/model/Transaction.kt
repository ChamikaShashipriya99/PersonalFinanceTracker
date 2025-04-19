package com.example.finance.data.model

import java.io.Serializable

/**
 * Data class representing a financial transaction.
 * @param id Unique identifier for the transaction (default: current timestamp).
 * @param title Description of the transaction.
 * @param amount Amount of the transaction.
 * @param category Category of the transaction (e.g., Food, Transport).
 * @param date Date of the transaction in "YYYY-MM-DD" format.
 * @param type Type of transaction ("Income" or "Expense").
 */
data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String
) : Serializable