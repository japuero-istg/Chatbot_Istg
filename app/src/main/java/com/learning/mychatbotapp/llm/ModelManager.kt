package com.learning.mychatbotapp.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class ModelManager(private val context: Context) {

    val modelFile: File
        get() = File(context.getExternalFilesDir(null), MODEL_FILE_NAME)

    fun isModelReady(): Boolean {
        val ready = modelFile.exists() && modelFile.length() == EXPECTED_MODEL_SIZE
        Log.d("ModelManager", "modelFile=${modelFile.path} length=${modelFile.length()} esperado=$EXPECTED_MODEL_SIZE ready=$ready")
        return ready
    }

    // Descarga el modelo con progreso (0f..1f) desde Dispatchers.IO.
    // Verifica el tamaño real antes de renombrar: nunca deja un archivo truncado.
    suspend fun downloadModel(onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val connection = URL(MODEL_URL).openConnection() as HttpURLConnection
        connection.connect()

        // GitHub envía Content-Length; si no viniera, se usa el tamaño esperado del modelo.
        val totalBytes: Long = if (connection.contentLength > 0) connection.contentLength.toLong() else EXPECTED_MODEL_SIZE
        val tempFile = File(context.getExternalFilesDir(null), "$MODEL_FILE_NAME.part")

        connection.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                val buffer = ByteArray(1024 * 64)
                var downloaded = 0L
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    downloaded += read
                    onProgress((downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
                }
            }
        }
        connection.disconnect()

        if (tempFile.length() != totalBytes) {
            tempFile.delete()
            error("Descarga incompleta: se obtuvieron ${tempFile.length()} de $totalBytes bytes")
        }

        if (!tempFile.renameTo(modelFile)) {
            tempFile.copyTo(modelFile, overwrite = true)
            tempFile.delete()
        }
    }

    companion object {
        private const val MODEL_FILE_NAME = "faq_model.task"

        // Tamaño exacto del modelo gemma-3-270m-it-q8 (verificado contra el asset
        // de GitHub Releases v1.0.0: sha256 0f7147f1...ef5).
        private const val EXPECTED_MODEL_SIZE = 303_950_933L

        // URL real de descarga del modelo .tasks.
        private const val MODEL_URL = "https://github.com/japuero-istg/Chatbot_Istg/releases/download/v1.0.0/faq_model.task"
    }
}
