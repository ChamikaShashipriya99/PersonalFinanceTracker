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
import com.example.finance.data.repository.TransactionRepository
import com.example.finance.util.NotificationUtils

/**
 * Fragment for setting and tracking the monthly budget.
 */
class BudgetFragment : Fragment() {

    private lateinit var budgetManager: BudgetManager
    private lateinit var transactionRepository: TransactionRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        budgetManager = BudgetManager(requireContext())
        transactionRepository = TransactionRepository(requireContext())

        val etBudget = view.findViewById<EditText>(R.id.etBudget)
        val btnSetBudget = view.findViewById<Button>(R.id.btnSetBudget)
        val pbBudget = view.findViewById<ProgressBar>(R.id.pbBudget)
        val tvBudgetStatus = view.findViewById<TextView>(R.id.tvBudgetStatus)

        // Pre-fill current budget
        etBudget.setText(budgetManager.getBudget().toString())

        btnSetBudget.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()
            if (budget != null && budget > 0) {
                budgetManager.setBudget(budget)
                updateBudgetStatus(pbBudget, tvBudgetStatus)
                Toast.makeText(context, "Budget set", Toast.LENGTH_SHORT).show()
            } else {
                etBudget.error = "Enter a valid budget"
            }
        }

        updateBudgetStatus(pbBudget, tvBudgetStatus)
        return view
    }

    private fun updateBudgetStatus(pbBudget: ProgressBar, tvBudgetStatus: TextView) {
        val transactions = transactionRepository.getAllTransactions()
        val status = budgetManager.checkBudgetStatus(transactions)
        tvBudgetStatus.text = status

        // Send notification if budget is nearing or exceeded
        if (status != "Within Budget") {
            NotificationUtils.sendBudgetNotification(requireContext(), status)
        }

        val monthlyExpenses = transactions
            .filter { it.type == "Expense" && it.date.startsWith("2025-04") }
            .sumOf { it.amount }
        val budget = budgetManager.getBudget()
        pbBudget.progress = if (budget > 0) ((monthlyExpenses / budget) * 100).toInt() else 0
    }
}