package com.example.finance.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finance.R
import com.example.finance.data.model.Transaction
import com.example.finance.data.repository.TransactionRepository
import com.example.finance.ui.adapter.TransactionAdapter
import com.google.android.material.chip.Chip

/**
 * Fragment for displaying all transactions with filtering options.
 */
class TransactionsFragment : Fragment() {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var adapter: TransactionAdapter
    private var filter: String = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        transactionRepository = TransactionRepository(requireContext())

        // Initialize RecyclerView
        val rvTransactions = view.findViewById<RecyclerView>(R.id.rvTransactions)
        adapter = TransactionAdapter(
            transactions = mutableListOf(),
            onEdit = { showAddTransactionDialog(it) },
            onDelete = {
                transactionRepository.deleteTransaction(it)
                updateUI()
            }
        )
        rvTransactions.adapter = adapter
        rvTransactions.layoutManager = LinearLayoutManager(context)

        // Set up filter chips
        val chipAll = view.findViewById<Chip>(R.id.chipAll)
        val chipIncome = view.findViewById<Chip>(R.id.chipIncome)
        val chipExpense = view.findViewById<Chip>(R.id.chipExpense)

        chipAll.setOnClickListener {
            filter = "All"
            updateUI()
        }
        chipIncome.setOnClickListener {
            filter = "Income"
            updateUI()
        }
        chipExpense.setOnClickListener {
            filter = "Expense"
            updateUI()
        }

        // Update UI with transactions
        updateUI()

        return view
    }

    private fun updateUI() {
        val transactions = transactionRepository.getAllTransactions()
        val filteredTransactions = when (filter) {
            "Income" -> transactions.filter { it.type == "Income" }
            "Expense" -> transactions.filter { it.type == "Expense" }
            else -> transactions
        }
        adapter.updateTransactions(filteredTransactions)
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
}