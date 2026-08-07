@file:Suppress("DEPRECATION")
package com.learning.mychatbotapp.llm

// La MediaPipe LLM Inference API está marcada "deprecated" por Google (mantenimiento
// hasta migrar a LiteRT-LM), pero es la API que usamos hoy -> silenciar el warning.


import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// Motor on-device (MediaPipe LLM Inference). En tasks-genai 0.10.2x+ el muestreo
// (topK/temp) va en LlmInferenceSession, no en LlmInferenceOptions. El .task de Gemma
// (~555 MB) NO va en el APK: se descarga al primer uso (ver ModelManager.kt).

class LlmEngine private constructor(
    private val llmInference: LlmInference,
    private val maxTokens: Int
) {
    // Sesión nueva por pregunta (RAG): el contexto del FAQ ya va en el prompt.
    // Bloqueante, corre en Dispatchers.Default. Mide tokens, chars y latencia.
    
    suspend fun generate(prompt: String): String = withContext(Dispatchers.Default) {
        // El 270M a veces "se calla" y devuelve solo <end_of_turn> (vacío, rápido).
        // Se reintenta con más temperatura/topK y un formato de prompt distinto.
        val attempts = listOf(
            Triple(40, 0.3f, "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"),
            Triple(30, 0.6f, "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"),
            Triple(30, 0.6f, "<start_of_turn>user\n$prompt\nPor favor, responde de forma breve.<end_of_turn>\n<start_of_turn>model\n")
        )

        for ((topK, temperature, formattedPrompt) in attempts) {
            val sessionOptions = LlmInferenceSessionOptions.builder()
                .setTopK(topK)
                .setTemperature(temperature)
                .build()
            val session = LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
            try {
                val promptTokens = session.sizeInTokens(formattedPrompt)
                // maxTokens cuenta input+output. Si el prompt ya alcanza el tope,
                // generateResponse falla en nativo (OUT_OF_RANGE) y el JNI aborta
                // el proceso (SIGABRT). Se salta el intento y se prueba el siguiente.
                if (promptTokens >= maxTokens) continue
                session.addQueryChunk(formattedPrompt)
                val start = System.currentTimeMillis()
                val raw = session.generateResponse()
                val endsWithEndTurn = raw.endsWith("<end_of_turn>")
                val response = if (endsWithEndTurn) {
                    raw.dropLast("<end_of_turn>".length).trim()
                } else {
                    raw.trim()
                }
                val elapsed = System.currentTimeMillis() - start
                Log.d(
                    "LlmEngine",
                    "tokensPrompt=$promptTokens respuestaChars=${response.length} tiempoMs=$elapsed " +
                        "rawChars=${raw.length} endsWithEndTurn=$endsWithEndTurn topK=$topK temp=$temperature"
                )
                if (response.isNotEmpty()) return@withContext response
            } finally {
                session.close()
            }
        }

        // Ningún intento generó texto: devolver vacío para que el ViewModel aplique
        // el respaldo de calidad con la respuesta oficial del FAQ.
        ""
    }
    fun close() {
        llmInference.close()
    }

    companion object {
        // Lanza excepción si el .task no existe: ChatViewModel verifica isModelReady() antes.
        // maxTokens (512): cuenta input+output. El input = pregunta + contexto del FAQ + marcadores
        // Gemma (~230-300 tokens); el resto es para la respuesta. Un tope menor crashea el 270M
        // (OUT_OF_RANGE: input > maxTokens).
        fun create(context: Context, modelPath: String, maxTokens: Int = 512): LlmEngine {
            val options = LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(maxTokens)
                .setPreferredBackend(LlmInference.Backend.CPU) // GPU no soporta todos los modelos
                .build()
            val inference = LlmInference.createFromOptions(context, options)
            return LlmEngine(inference, maxTokens)
        }
    }
}
