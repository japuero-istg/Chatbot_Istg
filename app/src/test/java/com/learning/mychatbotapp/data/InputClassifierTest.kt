package com.learning.mychatbotapp.data

import org.junit.Assert.*
import org.junit.Test

class InputClassifierTest {

    @Test
    fun saludoSeguidoDePregunta_esNormal() {
        // Antes del fix esto devolvía GREETING y tragaba la pregunta.
        assertEquals(
            InputClassifier.Intent.NORMAL,
            InputClassifier.classify(InputClassifier.normalize("buenas, cómo publico"))
        )
    }

    @Test
    fun saludoMasVerbo_esNormal() {
        assertEquals(
            InputClassifier.Intent.NORMAL,
            InputClassifier.classify(InputClassifier.normalize("hola registrarme"))
        )
    }

    @Test
    fun saludoSolo_esGreeting() {
        assertEquals(
            InputClassifier.Intent.GREETING,
            InputClassifier.classify(InputClassifier.normalize("hola"))
        )
    }

    @Test
    fun graciasSolo_esThanks() {
        assertEquals(
            InputClassifier.Intent.THANKS,
            InputClassifier.classify(InputClassifier.normalize("gracias"))
        )
    }

    @Test
    fun ecoExacto_esEcho() {
        assertTrue(InputClassifier.isEcho("como publico un negocio", "cómo publico un negocio"))
    }

    @Test
    fun ecoConAcentosYSignos_esEcho() {
        assertTrue(InputClassifier.isEcho("¿Cómo publico un negocio?", "cómo publico un negocio"))
    }

    @Test
    fun ecoConPrefijo_esEcho() {
        assertTrue(
            InputClassifier.isEcho(
                "Pregunta del usuario: como publico un negocio",
                "como publico un negocio"
            )
        )
    }

    @Test
    fun respuestaReal_noEsEcho() {
        assertFalse(
            InputClassifier.isEcho(
                "Para publicar tu negocio ve al menú principal.",
                "como publico un negocio"
            )
        )
    }
}
