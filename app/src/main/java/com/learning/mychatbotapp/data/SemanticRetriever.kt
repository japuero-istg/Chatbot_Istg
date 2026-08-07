package com.learning.mychatbotapp.data

import com.google.mediapipe.tasks.components.containers.Embedding
import com.learning.mychatbotapp.llm.EmbeddingEngine

class SemanticRetriever(
    private val entries: List<FaqEntry>,
    private val embeddingEngine: EmbeddingEngine
) {
    /** Similitud mínima (0.0 a 1.0) para considerar una entrada relevante. */
    private val minSimilarity = 0.5

    private var cachedEmbeddings: List<Pair<FaqEntry, Embedding>>? = null

    // Precalcula el embedding de las 60 preguntas UNA sola vez al iniciar la app,
    // para que la primera pregunta del usuario no espere a calcularlos.
    suspend fun warmUp() {
        if (cachedEmbeddings != null) return
        cachedEmbeddings = entries.map { entry -> entry to embeddingEngine.embed(entry.question) }
    }

    /** Busca las entradas del FAQ con significado más parecido a [query]. */
    suspend fun search(query: String, topK: Int = 3): List<FaqRetriever.Result> {
        warmUp()
        val queryEmbedding = embeddingEngine.embed(query)

        return cachedEmbeddings.orEmpty()
            .map { (entry, embedding) ->
                FaqRetriever.Result(entry, embeddingEngine.similarity(queryEmbedding, embedding).toFloat())
            }
            .filter { it.score >= minSimilarity }
            .sortedByDescending { it.score }
            .take(topK)
    }
}