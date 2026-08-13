package com.learning.mychatbotapp.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta extraída del mockup de diseño (Lunacy) provisto por el usuario.
val BackgroundDark = Color(0xFF1A1D23)   // fondo general de toda la app
val SurfaceCard = Color(0xFF3A3C41)      // tarjetas de categoría en el menú
val InputBarBackground = Color(0xFF323339) // fondo de la barra "Pregunta directa"
val PlaceholderGray = Color(0xFF9B9C9E)  // texto placeholder sobre fondo oscuro
val AccentBlue = Color(0xFF0C70F2)       // azul principal: botones, burbuja de usuario
val AccentBlueLight = Color(0xFF37A6F0)  // azul del ícono circular de enviar
val TextOnDark = Color(0xFFE1E7F2)       // texto casi blanco sobre fondo oscuro/azul

// Se mantienen por compatibilidad con el resto del tema de Compose generado
// originalmente por la plantilla; ya no se usan en las pantallas nuevas.
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val ColorUserMessage = AccentBlue
// Burbuja del asistente: gris oscuro con texto claro (alto contraste y legible).
val ColorModelMessage = Color(0xFF262A32)
