package com.learning.mychatbotapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de color fijo, oscuro, tomado del mockup de diseño (Lunacy).
// A propósito NO se usa dynamicColor (Material You) ni se sigue el tema del
// sistema: la marca de Emprende ISTG es siempre oscura con acento azul,
// sin importar la configuración del teléfono del usuario.
private val EmprendeIstgDarkScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextOnDark,
    secondary = AccentBlueLight,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = TextOnDark,
    surface = SurfaceCard,
    onSurface = TextOnDark,
    surfaceVariant = InputBarBackground,
    onSurfaceVariant = PlaceholderGray,
    error = Color(0xFFCF6679)
)

@Composable
fun MyChatBotAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EmprendeIstgDarkScheme,
        typography = Typography,
        content = content
    )
}
