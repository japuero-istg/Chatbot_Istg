package com.learning.mychatbotapp.data

import android.content.Context
import kotlinx.serialization.json.Json

// Carga assets/faq.json (generado con md_to_json.py) y lo cachea en memoria.
// Es la única puerta de entrada a los datos: migrable a Room sin tocar el resto.
class FaqRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private var cache: List<FaqEntry>? = null

    fun getAll(): List<FaqEntry> {
        cache?.let { return it }
        val text = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        val entries = json.decodeFromString<List<FaqEntry>>(text)
        cache = entries
        return entries
    }

    companion object {
        private const val ASSET_NAME = "faq.json"
    }
}
