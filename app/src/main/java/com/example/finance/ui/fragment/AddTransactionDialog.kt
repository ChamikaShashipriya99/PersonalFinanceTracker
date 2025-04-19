package com.example.finance.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.finance.R
import com.example.finance.data.model.Transaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog fragment for adding or editing a transaction.
 */
class AddTransactionDialog : DialogFragment() {

    var onSave: ((Transaction) -> Unit)? = null
    private var transactionToEdit: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_transaction, container, false)

        val etTitle = view.findViewById<TextInputEditText>(R.id.etTitle)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val spCategory = view.findViewById<Spinner>(R.id.spCategory)
        val rbIncome = view.findViewById<RadioButton>(R.id.rbIncome)
        val rbExpense = view.findViewById<RadioButton>(R.id.rbExpense)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        // Populate categories
        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = adapter
        spCategory.setSelection(0)

        // Pre-fill fields if editing
        transactionToEdit?.let { transaction ->
            etTitle.setText(transaction.title)
            etAmount.setText(transaction.amount.toString())
            spCategory.setSelection(categories.indexOf(transaction.category))
            if (transaction.type == "Income") rbIncome.isChecked = true else rbExpense.isChecked = true
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = spCategory.selectedItem.toString()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = transactionToEdit?.id ?: System.currentTimeMillis(),
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = if (rbIncome.isChecked) "Income" else "Expense"
            )

            onSave?.invoke(transaction)
            Toast.makeText(context, "Transaction saved", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return view
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
}