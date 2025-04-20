package com.example.finance.data.repository

import android.content.Context
import android.os.Environment
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.data.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Repository for managing transaction data.
 */
class TransactionRepository(context: Context) {

    private val preferencesManager = PreferencesManager(context)
    private val gson = Gson()
    private val transactionsKey = "transactions"

    fun getAllTransactions(): List<Transaction> {
        return loadTransactions()
    }

    fun addTransaction(transaction: Transaction) {
        val transactions = loadTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val transactions = loadTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        val transactions = loadTransactions().toMutableList()
        transactions.removeAll { it.id == transaction.id }
        saveTransactions(transactions)
    }

    private fun loadTransactions(): List<Transaction> {
        val json = preferencesManager.sharedPreferences.getString(transactionsKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Saves a list of transactions to SharedPreferences.
     * @param transactions List of transactions to save.
     */
    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        preferencesManager.sharedPreferences.edit().putString(transactionsKey, json).apply()
    }
}
