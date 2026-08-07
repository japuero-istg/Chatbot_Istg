@file:Suppress("DEPRECATION")

package com.learning.mychatbotapp.llm

import android.content.Context
import com.google.mediapipe.tasks.components.containers.Embedding
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// Motor de embeddings (MediaPipe TextEmbedder): convierte texto en vector y mide
// similitud coseno. El .tflite (USE, ~5.8 MB) va en assets/ del APK, sin descarga.

class EmbeddingEngine private constructor(private val textEmbedder: TextEmbedder) {

    /** Convierte un texto en su vector de embedding. Llamada bloqueante -> hilo de fondo. */
    suspend fun embed(text: String): Embedding = withContext(Dispatchers.Default) {
        textEmbedder.embed(text).embeddingResult().embeddings().first()
    }

    /** Similitud coseno entre dos embeddings: 1.0 = mismo significado, 0.0 = sin relación. */
    fun similarity(a: Embedding, b: Embedding): Double = TextEmbedder.cosineSimilarity(a, b)

    fun close() = textEmbedder.close()

    companion object {
        /** Nombre del archivo dentro de app/src/main/assets/ (el que ya descargaste en el Paso 2). */
        private const val MODEL_ASSET_PATH = "universal_sentence_encoder.tflite"

        fun create(context: Context): EmbeddingEngine {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET_PATH)
                .build()
            val options = TextEmbedderOptions.builder()
                .setBaseOptions(baseOptions)
                .build()
            val embedder = TextEmbedder.createFromOptions(context, options)
            return EmbeddingEngine(embedder)
        }
    }
}