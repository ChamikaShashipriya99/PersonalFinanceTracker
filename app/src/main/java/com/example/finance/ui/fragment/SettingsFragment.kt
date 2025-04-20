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
import com.google.android.material.switchmaterial.SwitchMaterial
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
            if (checkStoragePermission()) {
                createFileLauncher.launch("finance_backup_${System.currentTimeMillis()}.json")
            }
        }

        btnImport.setOnClickListener {
            if (checkStoragePermission()) {
                pickFileLauncher.launch("application/json")
            }
        }

        // Notifications
        val swNotifications = view.findViewById<SwitchMaterial>(R.id.swNotifications)
        swNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkNotificationPermission()) {
                    NotificationUtils.scheduleDailyReminder(requireContext())
                } else {
                    swNotifications.isChecked = false
                }
            } else {
                NotificationUtils.cancelDailyReminder(requireContext())
            }
        }

        // Dark mode
        val swDarkMode = view.findViewById<SwitchMaterial>(R.id.swDarkMode)
        swDarkMode.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        swDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        return view
    }

    private fun checkStoragePermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
            false
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
            false
        }
    }

    private val createFileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val transactions = transactionRepository.getAllTransactions()
                val json = Gson().toJson(transactions)
                
                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                    outputStream.flush()
                    Toast.makeText(context, "Backup saved successfully", Toast.LENGTH_SHORT).show()
                } ?: throw Exception("Failed to create output stream")
            } catch (e: Exception) {
                Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickFileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                    val json = inputStream.bufferedReader().use { it.readText() }
                    val type = object : TypeToken<List<Transaction>>() {}.type
                    val transactions = Gson().fromJson<List<Transaction>>(json, type)
                    
                    if (transactions != null && transactions.isNotEmpty()) {
                        // Clear existing transactions
                        val existingTransactions = transactionRepository.getAllTransactions()
                        existingTransactions.forEach { transactionRepository.deleteTransaction(it) }
                        
                        // Add imported transactions
                        transactions.forEach { transactionRepository.addTransaction(it) }
                        Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        throw Exception("No valid transactions found in file")
                    }
                } ?: throw Exception("Failed to read file")
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Export button click listener
        view.findViewById<Button>(R.id.btnExport).setOnClickListener {
            if (checkStoragePermission()) {
                createFileLauncher.launch("finance_backup_${System.currentTimeMillis()}.json")
            }
        }

        // Import button click listener
        view.findViewById<Button>(R.id.btnImport).setOnClickListener {
            if (checkStoragePermission()) {
                pickFileLauncher.launch("application/json")
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val NOTIFICATION_PERMISSION_CODE = 101
    }
}