package com.example.finance.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.finance.R
import com.example.finance.data.model.Transaction
import com.example.finance.data.repository.TransactionRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

/**
 * Fragment for displaying category-wise spending analysis.
 */
class AnalyticsFragment : Fragment() {

    private lateinit var transactionRepository: TransactionRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        transactionRepository = TransactionRepository(requireContext())

        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        val tvSummary = view.findViewById<TextView>(R.id.tvSummary)

        updateChart(pieChart, tvSummary)

        return view
    }

    private fun updateChart(pieChart: PieChart, tvSummary: TextView) {
        val transactions = transactionRepository.getAllTransactions()
        val spending = getCategorySpending(transactions)

        val entries = spending.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "Spending by Category")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#F44336"),
            Color.parseColor("#9C27B0")
        )
        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.invalidate()

        tvSummary.text = spending.entries.joinToString("\n\n") { "${it.key} : LKR : ${String.format("%.2f", it.value)}" }
    }

    private fun getCategorySpending(transactions: List<Transaction>): Map<String, Double> {
        return transactions
            .filter { it.type == "Expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}