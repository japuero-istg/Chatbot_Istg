package com.learning.mychatbotapp.data

import android.content.Context
import kotlinx.serialization.json.Json

class FaqAliasRepository(private val context: Context) {

    private val json = Json {ignoreUnknownKeys = true}
    private var cache: List<FaqAlias>? = null
    fun getAll():List<FaqAlias> {
        cache?.let {return it }
        val text = context.assets.open(ASSET_NAME).bufferedReader().use {it.readText()}
        val aliases = json.decodeFromString<List<FaqAlias>>(text)
        cache =aliases
        return aliases
    }
    companion object {
        private const val ASSET_NAME = "faq_aliases.json"
    }
}