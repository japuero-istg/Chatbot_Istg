package com.learning.mychatbotapp.data

// Búsqueda determinística y barata (sin IA): palabras en común + keywords.
// La parte "inteligente" (redactar) la hace el LLM local después.
class FaqRetriever(
    private val entries: List<FaqEntry>,
    private val aliases: List<FaqAlias> = emptyList()
) {
    // Mapa frase-normalizada -> entryId, construido UNA vez al crear el retriever.
    private val aliasMap: Map<String, Int> = mutableMapOf<String, Int>().apply {
        aliases.forEach { alias ->
            alias.aliases.forEach { phrase ->
                this[InputClassifier.normalize(phrase)] = alias.entryId
            }
        }
        // La pregunta canónica de cada entrada también actúa como alias exacto,
        // para que la frase literal del FAQ siempre resuelva a su propia entrada.
        this@FaqRetriever.entries.forEach { entry ->
            this[InputClassifier.normalize(entry.question)] = entry.id
        }
    }

    /** Umbral mínimo de score para considerar que SÍ hay contexto relevante. */
    private val minScore = 0.15f

    /** Score fijo que hace ganar a una entrada por alias sobre cualquier match débil. */
    private val aliasScore = 10f

    data class Result(val entry: FaqEntry, val score: Float)

    fun search(query: String, topK: Int = 3): List<Result> {
        val normalizedQuery = InputClassifier.normalize(query)
        val aliasHit = matchAlias(normalizedQuery)
        val queryTokens = tokenize(query)
        if (queryTokens.isEmpty()) return aliasHit?.let { listOf(it) } ?: emptyList()

        val results = entries
            .map { entry -> Result(entry, score(queryTokens, entry)) }
            .filter { it.score >= minScore && it.entry.id != aliasHit?.entry?.id }
            .sortedByDescending { it.score }
            .take(topK)

        return if (aliasHit != null) listOf(aliasHit) + results.take(topK - 1) else results
    }

    private fun matchAlias(normalizedQuery: String): Result? {
        val padded = " $normalizedQuery "
        aliasMap.entries
            .sortedByDescending { it.key.length }
            .firstOrNull { (phrase, _) -> padded.contains(" $phrase ") }
            ?.let { (_, entryId) ->
                return entries.firstOrNull { it.id == entryId }?.let { Result(it, aliasScore) }
            }
        return null
    }

    private fun score(queryTokens: Set<String>, entry: FaqEntry): Float {
        val questionTokens = tokenize(entry.question)
        val keywordTokens = entry.keywords.map { InputClassifier.normalize(it) }.toSet()

        val questionOverlap = queryTokens.intersect(questionTokens).size
        val keywordOverlap = queryTokens.intersect(keywordTokens).size

        // Las coincidencias contra keywords pesan un poco más porque
        // suelen ser sinónimos/variantes curadas a mano.
        val raw = questionOverlap + keywordOverlap * 1.5f
        return raw / queryTokens.size.toFloat()
    }

    private fun tokenize(text: String): Set<String> {
        return InputClassifier.normalize(text)
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in STOPWORDS }
            .toSet()
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
