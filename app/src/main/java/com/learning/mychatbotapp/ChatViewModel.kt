package com.learning.mychatbotapp

import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.learning.mychatbotapp.data.FaqRepository
import com.learning.mychatbotapp.data.FaqRetriever
import com.learning.mychatbotapp.data.SemanticRetriever
import com.learning.mychatbotapp.llm.EmbeddingEngine
import com.learning.mychatbotapp.llm.FALLBACK_ANSWER
import com.learning.mychatbotapp.llm.LlmEngine
import com.learning.mychatbotapp.llm.ModelManager
import com.learning.mychatbotapp.llm.PromptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Estado del modelo local expuesto a la UI (descarga/progreso/error).
sealed interface ModelState {
    data object Checking : ModelState
    data class Downloading(val progress: Float) : ModelState
    data object Ready : ModelState
    data class Error(val message: String) : ModelState
}

/** Una categoría del menú principal = una sección del FAQ. */
data class CategoryItem(val section: String, val sampleQuestion: String)

// Eventos de texto-a-voz emitidos por el ViewModel y consumidos por MainActivity.
sealed interface TtsEvent {
    data class Speak(val text: String) : TtsEvent
    data object Stop : TtsEvent
}

class ChatViewModel(private val appContext: Context) : ViewModel() {

    val messageList = mutableStateListOf<MessageModel>()

    val ttsEvents = MutableSharedFlow<TtsEvent>(extraBufferCapacity = 8)

    var modelState by mutableStateOf<ModelState>(ModelState.Checking)
        private set

    private val faqRepository = FaqRepository(appContext)
    private val retriever by lazy { FaqRetriever(faqRepository.getAll()) }
    private val modelManager = ModelManager(appContext)
    private var llmEngine: LlmEngine? = null
    // Búsqueda semántica (respaldo del buscador por palabras clave).
    // Quedan null hasta que prepareSemanticRetriever() termina de cargarlos.
    private var semanticRetriever: SemanticRetriever? = null

    val categories: List<CategoryItem> by lazy {
        faqRepository.getAll()
            .groupBy { it.section }
            .map { (section, entries) -> CategoryItem(section, entries.first().question) }
    }

    init {
        prepareModel()
        prepareSemanticRetriever()
    }

    private fun prepareModel() {
        viewModelScope.launch {
            if (modelManager.isModelReady()) {
                loadEngine()
                return@launch
            }
            modelState = ModelState.Downloading(0f)
            try {
                modelManager.downloadModel { progress ->
                    modelState = ModelState.Downloading(progress)
                }
                loadEngine()
            } catch (e: Exception) {
                modelState = ModelState.Error(
                    "No se pudo descargar el modelo: ${e.message}. Verifica tu conexión e inténtalo de nuevo."
                )
            }
        }
    }
    private fun prepareSemanticRetriever() {
        viewModelScope.launch {
            // Un fallo del respaldo semántico no debe tumbar la app: se loguea y se
            // deja semanticRetriever=null (la búsqueda por palabras clave sigue sola).
            try {
                val engine = withContext(Dispatchers.Default) { EmbeddingEngine.create(appContext) }
                val retrieverInstance = SemanticRetriever(faqRepository.getAll(), engine)
                withContext(Dispatchers.Default) { retrieverInstance.warmUp() }
                semanticRetriever = retrieverInstance
            } catch (e: Exception) {
                Log.e("Retrieval", "EmbeddingEngine no disponible; solo búsqueda por palabras clave", e)
            }
        }
    }
    private fun loadEngine() {
        try {
            llmEngine = LlmEngine.create(appContext, modelManager.modelFile.absolutePath)
            modelState = ModelState.Ready
        } catch (e: Exception) {
            modelState = ModelState.Error("No se pudo cargar el modelo: ${e.message}")
        }
    }

    fun sendMessage(question: String) {
        val engine = llmEngine
        if (modelState != ModelState.Ready || engine == null) {
            messageList.add(MessageModel(question, "user"))
            messageList.add(
                MessageModel(
                    "El asistente todavía se está preparando, espera un momento e inténtalo de nuevo.",
                    "model"
                )
            )
            return
        }

        viewModelScope.launch {
            ttsEvents.tryEmit(TtsEvent.Stop)
            messageList.add(MessageModel(question, "user"))
            val startTime = SystemClock.elapsedRealtime()
            messageList.add(MessageModel("Escribiendo...", "model", startedAtMs = startTime))

            // Todo el flujo en try/catch: si algo falla se muestra un error claro en vez
            // de dejar el "Escribiendo..." colgado (ni texto ni voz).
            val answer = try {
                var matches = retriever.search(question)
                val via = if (matches.isEmpty()) "semantico" else "palabras"
                if (matches.isEmpty()) {
                    matches = semanticRetriever?.search(question) ?: emptyList()
                }
                Log.d("Retrieval", "capa=$via matches=${matches.size} scores=${matches.map { it.score }}")
                if (matches.isEmpty()) {
                    FALLBACK_ANSWER
                } else {
                    val official = matches.first().entry.answer
                    val prompt = PromptBuilder.build(question, matches.take(2))
                    val generated = engine.generate(prompt)
                    // Respaldo de calidad: si el modelo falla (vacío, fallback genérico,
                    // eco de la pregunta o texto insignificante), se muestra la respuesta
                    // oficial del FAQ. Así las cards del menú responden SIEMPRE.
                    val quality = generated.isNotBlank() &&
                        generated != FALLBACK_ANSWER &&
                        generated.trim() != question.trim() &&
                        generated.length >= 15
                    Log.d("ChatViewModel", "modelo=${if (quality) "ok" else "fallo"} generatedChars=${generated.length} -> ${if (quality) "respuesta_modelo" else "respuesta_oficial"}")
                    if (quality) generated else official
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error generando respuesta", e)
                "Ocurrió un error generando la respuesta: ${e.message}"
            }

            messageList.removeLast()
            val elapsedMs = SystemClock.elapsedRealtime() - startTime
            messageList.add(MessageModel(answer, "model", elapsedMs, startTime))
            ttsEvents.tryEmit(TtsEvent.Speak(answer))
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmEngine?.close()
    }

    /** Factory necesaria porque el ViewModel ahora requiere un Context. */
    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(appContext.applicationContext) as T
        }
    }
}
