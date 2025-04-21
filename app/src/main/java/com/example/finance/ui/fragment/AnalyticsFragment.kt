package com.example.finance.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.data.model.Transaction
import com.example.finance.data.repository.TransactionRepository
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Fragment for displaying detailed financial analytics and insights.
 */
class AnalyticsFragment : Fragment() {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        transactionRepository = TransactionRepository(requireContext())
        preferencesManager = PreferencesManager(requireContext())

        // Initialize UI elements
        val tvCurrentMonth = view.findViewById<TextView>(R.id.tvCurrentMonth)
        val tvTotalIncome = view.findViewById<TextView>(R.id.tvTotalIncome)
        val tvTotalExpenses = view.findViewById<TextView>(R.id.tvTotalExpenses)
        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        val tvSummary = view.findViewById<TextView>(R.id.tvSummary)
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)
        val tvTrendAnalysis = view.findViewById<TextView>(R.id.tvTrendAnalysis)
        val tvTopCategories = view.findViewById<TextView>(R.id.tvTopCategories)
        val tvSavingsRate = view.findViewById<TextView>(R.id.tvSavingsRate)

        // Set current month
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        tvCurrentMonth.text = currentMonth

        // Update all analytics
        updateMonthlyOverview(tvTotalIncome, tvTotalExpenses)
        updateCategoryDistribution(pieChart, tvSummary)
        updateSpendingTrends(lineChart, tvTrendAnalysis)
        updateTopCategories(tvTopCategories, tvSavingsRate)

        return view
    }

    private fun updateMonthlyOverview(tvTotalIncome: TextView, tvTotalExpenses: TextView) {
        val transactions = transactionRepository.getAllTransactions()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        val monthlyIncome = transactions
            .filter { it.type == "Income" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }
        
        val monthlyExpenses = transactions
            .filter { it.type == "Expense" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }

        val currency = preferencesManager.getCurrency()
        tvTotalIncome.text = "Income: $currency ${String.format("%.2f", monthlyIncome)}"
        tvTotalExpenses.text = "Expenses: $currency ${String.format("%.2f", monthlyExpenses)}"
    }

    private fun updateCategoryDistribution(pieChart: PieChart, tvSummary: TextView) {
        val transactions = transactionRepository.getAllTransactions()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        val categorySpending = transactions
            .filter { it.type == "Expense" && it.date.startsWith(currentMonth) }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }

        val entries = categorySpending.map { PieEntry(it.value.toFloat(), it.key) }
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
        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1400)
        pieChart.legend.isEnabled = true
        pieChart.invalidate()

        val currency = preferencesManager.getCurrency()
        tvSummary.text = categorySpending.entries.joinToString("\n") { 
            "${it.key}: $currency ${String.format("%.2f", it.value)}" 
        }
    }

    private fun updateSpendingTrends(lineChart: LineChart, tvTrendAnalysis: TextView) {
        val transactions = transactionRepository.getAllTransactions()
        val calendar = Calendar.getInstance()
        val months = mutableListOf<String>()
        val expenses = mutableListOf<Float>()
        
        // Get last 6 months
        for (i in 5 downTo 0) {
            calendar.add(Calendar.MONTH, -i)
            val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            months.add(SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time))
            
            val monthlyExpenses = transactions
                .filter { it.type == "Expense" && it.date.startsWith(month) }
                .sumOf { it.amount }
            expenses.add(monthlyExpenses.toFloat())
            
            calendar.add(Calendar.MONTH, i)
        }

        val entries = expenses.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val dataSet = LineDataSet(entries, "Monthly Expenses")
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.setCircleColor(Color.parseColor("#2196F3"))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)

        lineChart.data = LineData(dataSet)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisRight.isEnabled = false
        lineChart.animateX(1400)
        lineChart.invalidate()

        // Analyze trend
        val trend = when {
            expenses.last() > expenses[expenses.size - 2] -> "increasing"
            expenses.last() < expenses[expenses.size - 2] -> "decreasing"
            else -> "stable"
        }
        
        val currency = preferencesManager.getCurrency()
        val lastMonth = expenses.last()
        val previousMonth = expenses[expenses.size - 2]
        val difference = lastMonth - previousMonth
        val percentageChange = if (previousMonth > 0) (difference / previousMonth * 100) else 0f

        tvTrendAnalysis.text = "Your spending is $trend. " +
            "Last month you spent $currency ${String.format("%.2f", lastMonth)}, " +
            "which is ${String.format("%.1f", abs(percentageChange))}% " +
            "${if (difference >= 0) "more" else "less"} than the previous month."
    }

    private fun updateTopCategories(tvTopCategories: TextView, tvSavingsRate: TextView) {
        val transactions = transactionRepository.getAllTransactions()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        val monthlyIncome = transactions
            .filter { it.type == "Income" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }
        
        val monthlyExpenses = transactions
            .filter { it.type == "Expense" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }

        val categorySpending = transactions
            .filter { it.type == "Expense" && it.date.startsWith(currentMonth) }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(3)

        val currency = preferencesManager.getCurrency()
        tvTopCategories.text = categorySpending.joinToString("\n") { 
            "${it.first}: $currency ${String.format("%.2f", it.second)}" 
        }

        val savingsRate = if (monthlyIncome > 0) {
            ((monthlyIncome - monthlyExpenses) / monthlyIncome * 100)
        } else {
            0.0
        }

        tvSavingsRate.text = "Current Savings Rate: ${String.format("%.1f", savingsRate)}%"
    }
}