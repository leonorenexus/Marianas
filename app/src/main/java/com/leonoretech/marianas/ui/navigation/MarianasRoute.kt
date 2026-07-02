package com.leonoretech.marianas.ui.navigation

/**
 * Each dashboard is its own destination/screen, navigated to via the
 * side menu — replacing the single long scrollable drawer from the web app.
 */
sealed class MarianasRoute(val route: String, val label: String) {
    object Chat : MarianasRoute("chat", "Chat")
    object Provider : MarianasRoute("dashboard_provider", "Provider")
    object Appearance : MarianasRoute("dashboard_appearance", "Tampilan")
    object DataSync : MarianasRoute("dashboard_data", "Data & Sesi")

    companion object {
        val dashboardItems = listOf(Provider, Appearance, DataSync)
    }
}
