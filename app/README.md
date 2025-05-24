# Personal Finance Tracker - App Module

This README provides detailed information about the `app` module of the Personal Finance Tracker application.

## Module Overview

The `app` module is the main application module, containing the core user interface, business logic, and features of the Personal Finance Tracker. It is built using Kotlin and leverages standard Android development practices.

## Features

Based on the available string resources, the app module implements the following features:

*   **User Authentication:**
    *   User registration (Create Account)
    *   User login
*   **Dashboard/Home Screen:**
    *   Displays a welcome message (e.g., "Welcome Back," "Hello, User !")
    *   Summarizes key financial figures:
        *   Income
        *   Expenses
        *   Savings
        *   Budget status (e.g., "Budget: $0.00", "Within Budget")
*   **Transaction Management:**
    *   Adding new transactions (both income and expenses)
    *   Specifying transaction details:
        *   Title
        *   Amount
        *   Category
    *   Viewing a list of transactions.
*   **Budgeting:**
    *   Setting and managing budgets.
    *   Tracking spending against budgets.
*   **Analytics & Reporting:**
    *   Viewing spending summaries.
    *   (Planned: More detailed analytics and visualizations)
*   **User Profile Management:**
    *   Viewing user profile information (e.g., "Your Profile").
    *   Editing profile details:
        *   Full Name
        *   Phone Number
        *   Address
        *   Profile Photo (Change/Select Photo)
    *   Saving profile changes.
*   **Settings:**
    *   Enable/Disable notifications.
    *   Toggle Dark Mode.
    *   Data management:
        *   Export Data
        *   Import Data
    *   Logout functionality.

## Architecture (Assumed)

While a deep architectural analysis isn't possible without inspecting the source code, a typical Android application structure is expected:

*   **UI Layer:** Activities/Fragments for each screen (e.g., Home, Transactions, Budget, Profile, Settings, Login, Register).
*   **ViewModel Layer:** Likely using Android Jetpack ViewModels to hold and manage UI-related data in a lifecycle-conscious way.
*   **Data Layer:** Repositories to manage data sources (local database, network).
*   **Navigation:** Likely using Android Jetpack Navigation Component for navigating between screens.

## Key Libraries (Assumed)

Based on common Android development practices, the following libraries might be in use:

*   **Android Jetpack:**
    *   LiveData and ViewModel
    *   Room (for local database storage)
    *   Navigation Component
    *   DataStore (for simple key-value storage like settings)
*   **Kotlin Coroutines:** For asynchronous operations.
*   **Retrofit/OkHttp:** If network operations (e.g., for future cloud sync) are planned.
*   **Glide/Picasso:** For image loading (e.g., profile pictures).
*   **Material Components for Android:** For UI elements adhering to Material Design.

## Building and Running

Refer to the main `README.md` in the project root for instructions on building and running the application. The `app` module is the default application module and will be built and run as part of the standard process.

## Future Enhancements

*   More detailed financial analytics and charts.
*   Goal setting features.
*   Cloud synchronization of data.
*   Recurring transactions.
*   Bill reminders.

This README will be updated as the module evolves and more specific architectural decisions are made.
