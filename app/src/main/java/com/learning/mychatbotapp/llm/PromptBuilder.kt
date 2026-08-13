package com.learning.mychatbotapp.llm

import com.learning.mychatbotapp.data.FaqRetriever

/** Capa 6: mensaje final de respaldo (más útil que la versión anterior).
 *  Se usa como centinela del LLM cuando el contexto no resuelve la pregunta
 *  y como red de seguridad última. */
const val FALLBACK_ANSWER =
    "No tengo información sobre eso todavía. Puedo ayudarte con el registro y tu " +
    "cuenta, publicar y gestionar tu negocio, buscar y filtrar emprendimientos, " +
    "favoritos, calificaciones, ofertas y tu perfil. ¿Sobre cuál quieres que te explique?"

object PromptBuilder {

    // Prompt RAG: contexto del FAQ + pregunta. Prohíbe inventar y limita el
    // contexto (top-2) y la longitud para reducir tokens y latencia.
    fun build(userQuestion: String, matches: List<FaqRetriever.Result>): String {
        val contextBlock = matches.take(2).joinToString("\n\n") { result ->
            val answer = if(result.entry.answer.length >300){
                result.entry.answer.take(300) + "..."
             } else result.entry.answer
            "Pregunta relacionada: ${result.entry.question}\nRespuesta oficial: $answer"   
            }
            
            return """
            Eres el asistente virtual de la app Emprende ISTG (Instituto Superior
            Tecnológico Guayaquil). Responde de forma breve, clara y amable en
            español, en máximo 2 frases, usando ÚNICAMENTE la información del
            contexto de abajo.
            No inventes funciones, pantallas ni pasos que no estén en el contexto.
            Si el contexto no resuelve la pregunta, responde exactamente:
            "$FALLBACK_ANSWER"
                Contexto:
                $contextBlock
                Pregunta del usuario: $userQuestion
                Respuesta:
            """.trimIndent()
    }
}
