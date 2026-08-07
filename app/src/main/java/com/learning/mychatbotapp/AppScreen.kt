package com.learning.mychatbotapp

// Navegación simple entre las 3 pantallas, sin Navigation Compose: MainActivity
// guarda cuál está activa en un mutableStateOf.
sealed class AppScreen {
    data object Splash : AppScreen()
    data object Menu : AppScreen()
    data object Chat : AppScreen()
}
