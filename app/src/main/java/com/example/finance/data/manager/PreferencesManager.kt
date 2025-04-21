package com.example.finance.data.manager

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for handling SharedPreferences operations.
 */
class PreferencesManager(context: Context) {

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET = "budget"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }

    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "LKR") ?: "LKR"
    }

    fun setMonthlyBudget(budget: Float) {
        sharedPreferences.edit().putFloat(KEY_BUDGET, budget).apply()
    }

    fun getMonthlyBudget(): Float {
        return sharedPreferences.getFloat(KEY_BUDGET, 0f)
    }

    fun saveUser(username: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("user_$username", password)
        editor.apply()
    }

    fun saveUserDetails(username: String, fullName: String, email: String, phone: String, address: String) {
        val editor = sharedPreferences.edit()
        editor.putString("${username}_fullName", fullName)
        editor.putString("${username}_email", email)
        editor.putString("${username}_phone", phone)
        editor.putString("${username}_address", address)
        editor.apply()
    }

    fun getUserPassword(username: String): String? {
        return sharedPreferences.getString("user_$username", null)
    }

    fun getUserFullName(username: String): String? {
        return sharedPreferences.getString("${username}_fullName", null)
    }

    fun getUserPhone(username: String): String? {
        return sharedPreferences.getString("${username}_phone", null)
    }

    fun getUserAddress(username: String): String? {
        return sharedPreferences.getString("${username}_address", null)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false)
    }

    fun setUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun setEmail(email: String) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }
}