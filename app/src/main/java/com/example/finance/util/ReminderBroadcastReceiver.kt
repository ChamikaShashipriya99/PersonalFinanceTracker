package com.example.finance.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Broadcast receiver for handling daily reminder notifications.
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtils.sendBudgetNotification(context, "Don't forget to record your expenses today!")
    }
}