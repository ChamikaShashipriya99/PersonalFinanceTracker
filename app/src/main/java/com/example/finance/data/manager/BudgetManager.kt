package com.example.finance.data.manager

import android.content.Context

/**
 * Manages budget settings and status checks.
 * @param context Application context for accessing SharedPreferences.
 */
class BudgetManager(context: Context) {
    private val prefs = context.getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
    private val KEY_BUDGET = "monthly_budget"

    /**
     * Sets the monthly budget.
     * @param budget The budget amount to set.
     */
    fun setBudget(budget: Double) {
        prefs.edit().putFloat(KEY_BUDGET, budget.toFloat()).apply()
    }

    /**
     * Retrieves the current monthly budget.
     * @return The budget amount.
     */
    fun getBudget(): Double = prefs.getFloat(KEY_BUDGET, 0f).toDouble()

    /**
     * Checks the budget status based on transactions.
     * @param transactions List of transactions to analyze.
     * @return Status message indicating budget state.
     */
    fun checkBudgetStatus(transactions: List<com.example.finance.data.model.Transaction>): String {
        val monthlyExpenses = transactions
            .filter { it.type == "Expense" && it.date.startsWith("2025-04") }
            .sumOf { it.amount }
        val budget = getBudget()
        return when {
            monthlyExpenses >= budget -> "Budget Exceeded!"
            monthlyExpenses >= budget * 0.9 -> "Warning: Nearing Budget!"
            else -> "Within Budget"
        }
    }
}