package com.learning.mychatbotapp.data

import java.text.Normalizer

object InputClassifier {

    enum class Intent { EMPTY, GREETING, THANKS, GOODBYE, NORMAL }

    private val GREETINGS = setOf(
        "hola", "buenos dias", "buenas tardes", "buenas noches", "buenas",
        "saludos", "hey", "que tal", "como estas", "que hubo"
    )
    private val THANKS = setOf("gracias", "te agradezco", "muchas gracias", "mil gracias")
    private val GOODBYES = setOf("adios", "chao", "chau", "hasta luego", "nos vemos", "hasta pronto", "bye")
    private val ABBREVIATIONS = mapOf(
        "xq" to "porque", "pq" to "porque", "tmb" to "tambien", "tbm" to "tambien",
        "porfa" to "por favor", "porfis" to "por favor", "xfa" to "por favor"
    )
    private val ALL_TRIVIAL = GREETINGS + THANKS + GOODBYES

    fun normalize(text: String): String {
        var value = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
        value = " $value "
        ABBREVIATIONS.forEach { (short, long) ->
            value = value.replace(" $short ", " $long ")
        }
        return value.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Detecta si el modelo solo "repitió" la pregunta del usuario (eco), común con
     * el 270M. No compara igualdad exacta: normaliza ambos lados y además mide el
     * solapamiento de tokens, para atrapar ecos con acentos, mayúsculas o texto
     * alrededor (p.ej. "Pregunta del usuario: como publico").
     */
    fun isEcho(generated: String, question: String): Boolean {
        val q = normalize(question)
        val g = normalize(generated)
        if (q.isEmpty() || g.isEmpty()) return false
        if (g == q) return true
        val qTokens = q.split(" ").filter { it.isNotBlank() }.toSet()
        if (qTokens.isEmpty()) return false
        val gTokens = g.split(" ").filter { it.isNotBlank() }.toSet()
        val overlap = qTokens.intersect(gTokens).size.toFloat() / qTokens.size
        return overlap >= 0.8
    }

    fun classify(normalized: String): Intent {
        val text = normalized.trim()
        if (text.isEmpty()) return Intent.EMPTY
        val tokens = text.split(" ").filter { it.isNotBlank() }
        if (tokens.size > MAX_TOKENS_FOR_TRIVIAL) return Intent.NORMAL
        // Solo se trata como saludo/gracias/despedida si, tras quitar esas frases,
        // NO queda contenido sustantivo. Si queda texto (p.ej. "buenas, cómo publico"),
        // se procesa como pregunta real en vez de tragarse la intención.
        val remaining = removeTrivialPhrases(text)
        if (remaining.isEmpty()) {
            return when {
                contains(text, GOODBYES) -> Intent.GOODBYE
                contains(text, THANKS) -> Intent.THANKS
                contains(text, GREETINGS) -> Intent.GREETING
                else -> Intent.NORMAL
            }
        }
        return Intent.NORMAL
    }

    fun replyFor(intent: Intent): String? = when (intent) {
        Intent.GREETING ->
            "¡Hola! Soy el asistente de Emprende ISTG. Pregúntame sobre registro, negocios, favoritos, modo invitado o problemas técnicos."
        Intent.THANKS -> "¡Con gusto! Aquí estoy para ayudarte."
        Intent.GOODBYE -> "¡Hasta luego! Aquí estaré cuando me necesites."
        Intent.EMPTY -> "No recibí tu pregunta. Escríbeme, por ejemplo: ¿cómo me registro?"
        Intent.NORMAL -> null
    }

    private fun contains(text: String, phrases: Set<String>): Boolean =
        phrases.any { phrase ->
            text == phrase ||
                text.contains(" $phrase ") ||
                text.startsWith("$phrase ") ||
                text.endsWith(" $phrase")
        }

    private fun removeTrivialPhrases(text: String): String {
        var remaining = " $text "
        ALL_TRIVIAL.sortedByDescending { it.length }.forEach { phrase ->
            remaining = remaining.replace(" $phrase ", " ")
        }
        return remaining.replace(Regex("\\s+"), " ").trim()
    }

    private const val MAX_TOKENS_FOR_TRIVIAL = 5
}
