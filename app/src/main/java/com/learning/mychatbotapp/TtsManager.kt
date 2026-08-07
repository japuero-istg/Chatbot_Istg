package com.learning.mychatbotapp

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

// Reproducción de las respuestas del modelo en voz alta usando el TTS del sistema.
class TtsManager(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var pendingText: String? = null
    private var languageReady = false

    // Silenciado global (persistido): el estado se observa desde Compose para
    // alternar el icono de parlante. true = no se lee nada en voz alta.
    var muted by mutableStateOf(
        appContext.getSharedPreferences("tts_prefs", Context.MODE_PRIVATE)
            .getBoolean("muted", false)
    )
        private set

    fun toggleMuted() {
        muted = !muted
        appContext.getSharedPreferences("tts_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("muted", muted)
            .apply()
        if (muted) stop()
    }

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupLanguage()
                pendingText?.let { speak(it) }
                pendingText = null
            }
        }
    }

    private fun setupLanguage() {
        val engine = tts ?: return
        val candidates = listOf(
            Locale("es", "EC"),
            Locale("es", "MX"),
            Locale("es", "US"),
            Locale("es", "ES"),
            Locale("es")
        )
        for (locale in candidates) {
            if (engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = locale
                languageReady = true
                return
            }
        }
        // Sin voz en español instalada -> abrir el asistente de descarga de TTS.
        languageReady = false
        runCatching {
            val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
        }
    }

    fun speak(text: String) {
        if (muted) return
        val clean = sanitize(text)
        val engine = tts
        if (engine == null || !languageReady) {
            pendingText = clean
            return
        }
        engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "istg-${System.nanoTime()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    // Quita símbolos de markdown y normaliza espacios antes de leer.
    private fun sanitize(text: String): String =
        text
            .replace(Regex("[*_#`>]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
}
