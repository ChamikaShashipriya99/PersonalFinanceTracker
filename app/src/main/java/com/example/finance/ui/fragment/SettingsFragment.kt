package com.example.finance.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.finance.R
import com.example.finance.data.manager.BudgetManager
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.data.model.Transaction
import com.example.finance.data.repository.TransactionRepository
import com.example.finance.util.NotificationUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.Executor

/**
 * Fragment for managing user settings, including currency, backup/restore, and notifications.
 */
class SettingsFragment : Fragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var budgetManager: BudgetManager
    private lateinit var executor: Executor

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportToUri(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { restoreFromUri(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        preferencesManager = PreferencesManager(requireContext())
        transactionRepository = TransactionRepository(requireContext())
        budgetManager = BudgetManager(requireContext())
        executor = ContextCompat.getMainExecutor(requireContext())

        // Currency selection
        val spCurrency = view.findViewById<Spinner>(R.id.spCurrency)
        val currencies = arrayOf(

            "LKR (Rs) - Srilankan Rupees",
            "USD ($) - US Dollar",
            "EUR (€) - Euro",
            "GBP (£) - British Pound",
            "JPY (¥) - Japanese Yen",
            "CNY (¥) - Chinese Yuan",
            "INR (₹) - Indian Rupee",
            "AUD ($) - Australian Dollar",
            "CAD ($) - Canadian Dollar",
            "CHF (Fr) - Swiss Franc",
            "SGD ($) - Singapore Dollar",
            "NZD ($) - New Zealand Dollar",
            "MXN ($) - Mexican Peso",
            "BRL (R$) - Brazilian Real",
            "RUB (₽) - Russian Ruble",
            "KRW (₩) - South Korean Won",
            "TRY (₺) - Turkish Lira",
            "ZAR (R) - South African Rand",
            "AED (د.إ) - UAE Dirham",
            "SAR (﷼) - Saudi Riyal",
            "MYR (RM) - Malaysian Ringgit"
        )
        spCurrency.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        val currentCurrency = preferencesManager.getCurrency()
        val currentIndex = currencies.indexOfFirst { it.startsWith(currentCurrency) }
        spCurrency.setSelection(if (currentIndex >= 0) currentIndex else 0)
        spCurrency.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCurrency = currencies[position].split(" ")[0] // Get the currency code (e.g., "USD")
                preferencesManager.setCurrency(selectedCurrency)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // Backup and restore
        val btnExport = view.findViewById<Button>(R.id.btnExport)
        val btnImport = view.findViewById<Button>(R.id.btnImport)
        
        btnExport.setOnClickListener {
            val fileName = "finance_backup_${getCurrentDateString()}.json"
            exportLauncher.launch(fileName)
        }

        btnImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
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

        // Data Management
        val btnClearHistory = view.findViewById<Button>(R.id.btnClearHistory)
        val btnResetBudget = view.findViewById<Button>(R.id.btnResetBudget)

        btnClearHistory.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Transaction History")
                .setMessage("Are you sure you want to clear all transaction history? This action cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    transactionRepository.clearAllTransactions()
                    Toast.makeText(context, "Transaction history cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnResetBudget.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Budget")
                .setMessage("Are you sure you want to reset your budget to zero?")
                .setPositiveButton("Reset") { _, _ ->
                    budgetManager.setBudget(0.0)
                    Toast.makeText(context, "Budget reset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Security
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)
        val swBiometric = view.findViewById<SwitchMaterial>(R.id.swBiometric)

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Check if biometric authentication is available
        val biometricManager = BiometricManager.from(requireContext())
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        swBiometric.isEnabled = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
        swBiometric.isChecked = preferencesManager.isBiometricEnabled()
        swBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showBiometricPrompt()
            } else {
                preferencesManager.setBiometricEnabled(false)
            }
        }

        // About
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
        val btnPrivacyPolicy = view.findViewById<Button>(R.id.btnPrivacyPolicy)

        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            tvVersion.text = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            tvVersion.text = "1.0.0"
        }

        btnPrivacyPolicy.setOnClickListener {
            // TODO: Open privacy policy URL or show dialog
            Toast.makeText(context, "Privacy policy will be available soon", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (newPassword != confirmPassword) {
                    Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val username = preferencesManager.getUsername() ?: return@setPositiveButton
                if (preferencesManager.verifyPassword(username, currentPassword)) {
                    preferencesManager.updatePassword(username, newPassword)
                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Login")
            .setSubtitle("Authenticate to enable biometric login")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    preferencesManager.setBiometricEnabled(true)
                    Toast.makeText(context, "Biometric login enabled", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
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

    private fun exportToUri(uri: Uri) {
        try {
            val exportData = ExportData(
                transactions = transactionRepository.getAllTransactions(),
                budget = budgetManager.getBudget(),
                currency = preferencesManager.getCurrency(),
                username = preferencesManager.getUsername(),
                exportDate = getCurrentDateString()
            )
            
            val json = Gson().toJson(exportData)
            
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            
            Toast.makeText(context, "Backup saved successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreFromUri(uri: Uri) {
        try {
            val json = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("Could not read backup file")
            
            // Try to parse as new format first
            try {
                val exportData = Gson().fromJson<ExportData>(json, ExportData::class.java)
                
                if (exportData != null) {
                    // Show confirmation dialog
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Restore Backup")
                        .setMessage("This will replace all your current data with the backup data. Are you sure you want to continue?")
                        .setPositiveButton("Restore") { _, _ ->
                            // Clear existing transactions
                            val existingTransactions = transactionRepository.getAllTransactions()
                            existingTransactions.forEach { transactionRepository.deleteTransaction(it) }
                            
                            // Add imported transactions
                            exportData.transactions.forEach { transactionRepository.addTransaction(it) }
                            
                            // Update budget if available
                            if (exportData.budget != null) {
                                budgetManager.setBudget(exportData.budget)
                            }
                            
                            // Update currency if available
                            if (!exportData.currency.isNullOrEmpty()) {
                                preferencesManager.setCurrency(exportData.currency)
                            }
                            
                            Toast.makeText(context, "Restore successful", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    return
                }
            } catch (e: Exception) {
                // If parsing as ExportData fails, try old format
                val type = object : TypeToken<List<Transaction>>() {}.type
                val transactions = Gson().fromJson<List<Transaction>>(json, type)
                
                if (transactions != null && transactions.isNotEmpty()) {
                    // Show confirmation dialog
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Restore Backup")
                        .setMessage("This will replace all your current transactions with the backup data. Are you sure you want to continue?")
                        .setPositiveButton("Restore") { _, _ ->
                            // Clear existing transactions
                            val existingTransactions = transactionRepository.getAllTransactions()
                            existingTransactions.forEach { transactionRepository.deleteTransaction(it) }
                            
                            // Add imported transactions
                            transactions.forEach { transactionRepository.addTransaction(it) }
                            Toast.makeText(context, "Restore successful (old format)", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    throw Exception("No valid transactions found in file")
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDateString(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val NOTIFICATION_PERMISSION_CODE = 101
    }
    
    /**
     * Data class for exporting all app data
     */
    private data class ExportData(
        val transactions: List<Transaction>,
        val budget: Double? = null,
        val currency: String? = null,
        val username: String? = null,
        val exportDate: String? = null
    )
}