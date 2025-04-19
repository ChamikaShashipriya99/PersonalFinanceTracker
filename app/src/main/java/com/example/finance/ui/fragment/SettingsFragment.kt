package com.example.finance.ui.fragment

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
import androidx.fragment.app.Fragment
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.data.model.Transaction
import com.example.finance.data.repository.TransactionRepository
import com.example.finance.util.NotificationUtils
import com.google.gson.Gson
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
            exportData(transactionRepository.getAllTransactions())
        }
        btnImport.setOnClickListener {
            val transactions = restoreData()
            transactions.forEach { transactionRepository.addTransaction(it) }
            Toast.makeText(context, "Data restored", Toast.LENGTH_SHORT).show()
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

    private fun exportData(transactions: List<Transaction>) {
        val gson = Gson()
        val json = gson.toJson(transactions)
        try {
            requireContext().openFileOutput("finance_backup.json", android.content.Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
            Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreData(): List<Transaction> {
        try {
            val json = requireContext().openFileInput("finance_backup.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            return gson.fromJson(json, Array<Transaction>::class.java).toList()
        } catch (e: Exception) {
            Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
            return emptyList()
        }
    }
}