package com.example.finance.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.finance.R
import com.example.finance.data.manager.BudgetManager
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.data.repository.TransactionRepository
import com.example.finance.util.NotificationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragment for setting and tracking the monthly budget.
 */
class BudgetFragment : Fragment() {

    private lateinit var budgetManager: BudgetManager
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        budgetManager = BudgetManager(requireContext())
        transactionRepository = TransactionRepository(requireContext())
        preferencesManager = PreferencesManager(requireContext())

        // Initialize UI elements
        val etBudget = view.findViewById<EditText>(R.id.etBudget)
        val btnSetBudget = view.findViewById<Button>(R.id.btnSetBudget)
        val pbBudget = view.findViewById<ProgressBar>(R.id.pbBudget)
        val tvBudgetStatus = view.findViewById<TextView>(R.id.tvBudgetStatus)
        val tvCurrentMonth = view.findViewById<TextView>(R.id.tvCurrentMonth)
        val tvDaysRemaining = view.findViewById<TextView>(R.id.tvDaysRemaining)
        val tvSpent = view.findViewById<TextView>(R.id.tvSpent)
        val tvRemaining = view.findViewById<TextView>(R.id.tvRemaining)
        val tvLastMonth = view.findViewById<TextView>(R.id.tvLastMonth)
        val tvAverageSpending = view.findViewById<TextView>(R.id.tvAverageSpending)

        // Set current month and days remaining
        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        tvCurrentMonth.text = currentMonth

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = daysInMonth - dayOfMonth + 1
        tvDaysRemaining.text = "$daysRemaining days remaining in this month"

        // Pre-fill current budget
        etBudget.setText(budgetManager.getBudget().toString())

        btnSetBudget.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()
            if (budget != null && budget > 0) {
                budgetManager.setBudget(budget)
                updateBudgetStatus(pbBudget, tvBudgetStatus, tvSpent, tvRemaining)
                Toast.makeText(context, "Budget set", Toast.LENGTH_SHORT).show()
            } else {
                etBudget.error = "Enter a valid budget"
            }
        }

        // Update budget status and history
        updateBudgetStatus(pbBudget, tvBudgetStatus, tvSpent, tvRemaining)
        updateBudgetHistory(tvLastMonth, tvAverageSpending)

        return view
    }

    private fun updateBudgetStatus(
        pbBudget: ProgressBar,
        tvBudgetStatus: TextView,
        tvSpent: TextView,
        tvRemaining: TextView
    ) {
        val transactions = transactionRepository.getAllTransactions()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        val monthlyExpenses = transactions
            .filter { it.type == "Expense" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }
        
        val budget = budgetManager.getBudget()
        val remaining = budget - monthlyExpenses
        
        // Update progress bar
        pbBudget.progress = if (budget > 0) ((monthlyExpenses / budget) * 100).toInt() else 0

        // Update status text
        val status = when {
            monthlyExpenses >= budget -> "Budget Exceeded!"
            monthlyExpenses >= budget * 0.9 -> "Warning: Nearing Budget!"
            else -> "Within Budget"
        }
        tvBudgetStatus.text = status

        // Update spent and remaining amounts
        val currency = preferencesManager.getCurrency()
        tvSpent.text = "Spent: $currency ${String.format("%.2f", monthlyExpenses)}"
        tvRemaining.text = "Remaining: $currency ${String.format("%.2f", remaining)}"

        // Send notification if budget is nearing or exceeded
        if (status != "Within Budget") {
            NotificationUtils.sendBudgetNotification(requireContext(), status)
        }
    }

    private fun updateBudgetHistory(tvLastMonth: TextView, tvAverageSpending: TextView) {
        val transactions = transactionRepository.getAllTransactions()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val lastMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        
        // Calculate last month's spending
        val lastMonthExpenses = transactions
            .filter { it.type == "Expense" && it.date.startsWith(lastMonth) }
            .sumOf { it.amount }
        
        // Calculate average monthly spending
        val monthlyExpenses = transactions
            .filter { it.type == "Expense" }
            .groupBy { it.date.substring(0, 7) }
            .map { it.value.sumOf { transaction -> transaction.amount } }
        
        val averageSpending = if (monthlyExpenses.isNotEmpty()) {
            monthlyExpenses.average()
        } else {
            0.0
        }

        // Update UI
        val currency = preferencesManager.getCurrency()
        val lastMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        tvLastMonth.text = "Last Month ($lastMonthName): $currency ${String.format("%.2f", lastMonthExpenses)}"
        tvAverageSpending.text = "Average Monthly Spending: $currency ${String.format("%.2f", averageSpending)}"
    }
}