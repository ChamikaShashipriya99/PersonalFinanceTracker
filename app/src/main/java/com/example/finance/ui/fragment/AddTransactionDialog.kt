package com.example.finance.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.finance.R
import com.example.finance.data.model.Transaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dialog fragment for adding or editing a transaction.
 */
class AddTransactionDialog : DialogFragment() {

    var onSave: ((Transaction) -> Unit)? = null
    private var transactionToEdit: Transaction? = null
    private lateinit var binding: View
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_transaction, container, false)
        binding = view

        val etTitle = view.findViewById<TextInputEditText>(R.id.etTitle)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val spCategory = view.findViewById<AutoCompleteTextView>(R.id.spCategory)
        val etDate = view.findViewById<TextInputEditText>(R.id.etDate)
        val tgTransactionType = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        // Populate categories
        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Other")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
        spCategory.setAdapter(adapter)

        // Set up date picker
        etDate.setText(dateFormat.format(Date()))
        etDate.setOnClickListener {
            showDatePicker(etDate)
        }

        // Pre-fill fields if editing
        transactionToEdit?.let { transaction ->
            etTitle.setText(transaction.title)
            etAmount.setText(transaction.amount.toString())
            spCategory.setText(transaction.category, false)
            etDate.setText(transaction.date)
            if (transaction.type == "Income") tgTransactionType.check(R.id.rbIncome) else tgTransactionType.check(R.id.rbExpense)
        }

        setupTransactionTypeSelection()

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = spCategory.text.toString()
            val date = etDate.text.toString()

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }
            if (category.isEmpty()) {
                spCategory.error = "Category is required"
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = transactionToEdit?.id ?: System.currentTimeMillis(),
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = if (tgTransactionType.checkedButtonId == R.id.rbIncome) "Income" else "Expense"
            )

            onSave?.invoke(transaction)
            Toast.makeText(context, "Transaction saved", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return view
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val currentDate = editText.text.toString()
        val parts = currentDate.split("-")
        
        if (parts.size == 3) {
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
        }
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                editText.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onStart() {
        super.onStart()
        // Set dialog to take up 90% of screen width and height
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.9).toInt()
        )
    }

    /**
     * Sets the transaction to edit.
     * @param transaction The transaction to pre-fill in the dialog.
     */
    fun setTransactionToEdit(transaction: Transaction) {
        this.transactionToEdit = transaction
    }

    private fun setupTransactionTypeSelection() {
        val tgTransactionType = binding.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val tilAmount = binding.findViewById<TextInputLayout>(R.id.tilAmount)
        
        tgTransactionType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.rbIncome -> {
                        tilAmount.setStartIconDrawable(R.drawable.ic_income)
                    }
                    R.id.rbExpense -> {
                        tilAmount.setStartIconDrawable(R.drawable.ic_expense)
                    }
                }
            }
        }
    }
}