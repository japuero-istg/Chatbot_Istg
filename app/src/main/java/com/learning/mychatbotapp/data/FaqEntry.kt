package com.learning.mychatbotapp.data

import kotlinx.serialization.Serializable

// "keywords" son palabras clave (extraídas o manuales) para mejorar la búsqueda.
@Serializable
data class FaqEntry(
    val id: Int,
    val section: String,
    val question: String,
    val answer: String,
    val keywords: List<String> = emptyList()
)
