package com.example.finance.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.data.model.Transaction
import com.example.finance.data.repository.TransactionRepository
import com.example.finance.util.NotificationUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Fragment for managing user settings, including currency, backup/restore, and notifications.
 */
class SettingsFragment : Fragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionRepository: TransactionRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        preferencesManager = PreferencesManager(requireContext())
        transactionRepository = TransactionRepository(requireContext())

        // Currency selection
        val spCurrency = view.findViewById<Spinner>(R.id.spCurrency)
        val currencies = arrayOf("$", "€", "£", "₹")
        spCurrency.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        spCurrency.setSelection(currencies.indexOf(preferencesManager.getCurrency()))
        spCurrency.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                preferencesManager.setCurrency(currencies[position])
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // Backup and restore
        val btnExport = view.findViewById<Button>(R.id.btnExport)
        val btnImport = view.findViewById<Button>(R.id.btnImport)
        
        btnExport.setOnClickListener {
            createFileLauncher.launch("finance_backup.json")
        }

        btnImport.setOnClickListener {
            pickFileLauncher.launch("application/json")
        }

        // Notifications
        val swNotifications = view.findViewById<Switch>(R.id.swNotifications)
        swNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                NotificationUtils.scheduleDailyReminder(requireContext())
            } else {
                // Cancel reminders (implement cancellation logic if needed)
            }
        }

        // Dark mode
        val swDarkMode = view.findViewById<Switch>(R.id.swDarkMode)
        swDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        return view
    }

    private val createFileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            exportToUri(it)
        }
    }

    private val pickFileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            importFromUri(it)
        }
    }

    private fun exportToUri(uri: android.net.Uri) {
        val transactions = transactionRepository.getAllTransactions()
        val json = Gson().toJson(transactions)
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFromUri(uri: android.net.Uri) {
        try {
            val json = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("Failed to read file")
            
            val gson = Gson()
            val type = object : TypeToken<List<Transaction>>() {}.type
            val transactions = gson.fromJson<List<Transaction>>(json, type)
            
            if (transactions == null || transactions.isEmpty()) {
                throw Exception("No valid transactions found in file")
            }
            
            // Validate each transaction
            transactions.forEach { transaction ->
                if (transaction.title.isBlank() || 
                    transaction.amount <= 0 || 
                    transaction.category.isBlank() || 
                    transaction.date.isBlank() || 
                    (transaction.type != "Income" && transaction.type != "Expense")) {
                    throw Exception("Invalid transaction data found")
                }
            }
            
            // Clear existing transactions and add imported ones
            val existingTransactions = transactionRepository.getAllTransactions()
            existingTransactions.forEach { transactionRepository.deleteTransaction(it) }
            transactions.forEach { transactionRepository.addTransaction(it) }
            
            Toast.makeText(context, "Successfully imported ${transactions.size} transactions", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}