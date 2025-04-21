package com.example.finance.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.data.model.Transaction
import com.example.finance.data.repository.TransactionRepository
import com.example.finance.data.manager.BudgetManager
import com.example.finance.databinding.FragmentHomeBinding
import com.example.finance.ui.adapter.TransactionAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var adapter: TransactionAdapter
    private lateinit var budgetManager: BudgetManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        preferencesManager = PreferencesManager(requireContext())
        transactionRepository = TransactionRepository(requireContext())
        budgetManager = BudgetManager(requireContext())

        // Set personalized greeting
        val username = preferencesManager.getUsername() ?: "User"
        binding.tvGreeting.text = "Hello, $username!"
        
        // Add click listener to greeting text
        binding.tvGreeting.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_profileFragment)
        }

        // Initialize RecyclerView for all transactions
        adapter = TransactionAdapter(
            transactions = mutableListOf(),
            onEdit = { showAddTransactionDialog(it) },
            onDelete = {
                transactionRepository.deleteTransaction(it)
                updateUI()
            }
        )
        binding.rvRecentTransactions.adapter = adapter
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(context)

        // Set up FAB for adding transactions
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog(null)
        }

        // Update UI with transactions and summary
        updateUI()

        return binding.root
    }

    private fun updateUI() {
        val transactions = transactionRepository.getAllTransactions()
        adapter.updateTransactions(transactions) // Show all transactions

        // Update summary
        val income = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val budget = budgetManager.getBudget()
        val savings = budget - expenses

        binding.tvIncome.text = "Income: LKR. ${String.format("%.2f", income)}"
        binding.tvExpenses.text = "Expenses: LKR. ${String.format("%.2f", expenses)}"
        binding.tvSavings.text = "Savings: LKR. ${String.format("%.2f", savings)}"
        binding.tvBudget.text = "Budget: LKR. ${String.format("%.2f", budget)}"
    }

    private fun showAddTransactionDialog(transaction: Transaction?) {
        val dialog = AddTransactionDialog()
        transaction?.let { dialog.setTransactionToEdit(it) }
        dialog.onSave = {
            if (transaction == null) {
                transactionRepository.addTransaction(it)
            } else {
                transactionRepository.updateTransaction(it)
            }
            updateUI()
        }
        dialog.show(parentFragmentManager, "AddTransactionDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}