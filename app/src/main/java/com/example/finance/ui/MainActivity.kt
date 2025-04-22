package com.example.finance.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.util.NotificationUtils
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main activity hosting the bottom navigation and fragments.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Allow screenshots - more direct approach
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        setContentView(R.layout.activity_main)

        // Initialize notification channel
        NotificationUtils.createNotificationChannel(this)

        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // Check login status
        val preferencesManager = PreferencesManager(this)
        if (!preferencesManager.isLoggedIn()) {
            navController.navigate(R.id.loginFragment)
        }

        // Control bottom navigation visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}