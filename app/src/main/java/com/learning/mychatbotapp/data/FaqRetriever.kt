package com.learning.mychatbotapp.data

import java.text.Normalizer

// Búsqueda determinística y barata (sin IA): palabras en común + keywords.
// La parte "inteligente" (redactar) la hace el LLM local después.
class FaqRetriever(private val entries: List<FaqEntry>) {

    /** Umbral mínimo de score para considerar que SÍ hay contexto relevante. */
    private val minScore = 0.15f

    data class Result(val entry: FaqEntry, val score: Float)

    fun search(query: String, topK: Int = 3): List<Result> {
        val queryTokens = tokenize(query)
        if (queryTokens.isEmpty()) return emptyList()

        return entries
            .map { entry -> Result(entry, score(queryTokens, entry)) }
            .filter { it.score >= minScore }
            .sortedByDescending { it.score }
            .take(topK)
    }

    private fun score(queryTokens: Set<String>, entry: FaqEntry): Float {
        val questionTokens = tokenize(entry.question)
        val keywordTokens = entry.keywords.map { normalize(it) }.toSet()

        val questionOverlap = queryTokens.intersect(questionTokens).size
        val keywordOverlap = queryTokens.intersect(keywordTokens).size

        // Las coincidencias contra keywords pesan un poco más porque
        // suelen ser sinónimos/variantes curadas a mano.
        val raw = questionOverlap + keywordOverlap * 1.5f
        return raw / queryTokens.size.toFloat()
    }

    private fun tokenize(text: String): Set<String> {
        return normalize(text)
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in STOPWORDS }
            .toSet()
    }

    private fun normalize(text: String): String {
        val noAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "")
        return noAccents.lowercase().replace(Regex("[^a-z0-9\\s]"), " ")
    }

    companion object {
        private val STOPWORDS = setOf(
            "como", "que", "para", "los", "las", "del", "una", "uno", "con",
            "por", "puedo", "hago", "son", "esta", "estan", "mis", "mi",
            "tu", "y", "o", "de", "la", "el", "en", "a", "al", "es", "un",
            "se", "no", "si", "sin"
        )
    }
}
