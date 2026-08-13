# Guion — Presentador C: Fundamentos (JSON, `.task` y capas en cascada)

**Formato:** tercer expositor, 3 minutos (teleprompter, texto literal).
**Instrucciones:** tiempos referenciales; `[...]` son acotaciones de escena, no se leen.
**Complementa:** `GUION_PRESENTACION_VIDEO.md` (Presentadores A y B) y `DOCUMENTO_TECNICO_MVP.md`.

---

## Presentador C — "La teoría: por qué JSON, qué es `.task` y las capas" (0:00 – 3:00)

**[0:00]**
Yo soy el tercer expositor y voy a la parte teórica: por qué guardamos el conocimiento en **JSON y no en SQLite**, qué es ese archivo **`.task`** y cómo las preguntas libres se responden en **capas en cascada**.

**[0:25]**
Empecemos por la base de datos. Esta app es *on-device*: no consulta un servidor, sino que razona en el teléfono con un modelo de lenguaje. Por eso la respuesta se **genera** en tiempo de ejecución a partir de un texto, no se recupera con un `SELECT`. No hay esquema relacional que consultar.

**[0:55]**
Entonces, ¿por qué JSON y no SQLite? Porque el cerebro no es una base de datos, es un modelo `.task`. El conocimiento es pequeño —60 entradas—, se lee una vez al iniciar y se guarda en memoria. Para eso JSON es más simple, legible y versionable que montar SQLite. La regla es clara: el **`.task` es el motor que razona** y el **JSON es el conocimiento fuente**. Y ojo: las preguntas libres no se resuelven con un árbol de decisiones, sino filtrando nodos JSON.

**[1:35]**
¿Y qué es un `.task`? Es el formato de MediaPipe *Tasks*: un solo archivo binario que empaqueta los pesos del modelo, el *tokenizer* y los metadatos. El nuestro es **Gemma 3 270M**, pequeño y cuantizado a 8 bits, unos 304 megas. Se carga con la API de inferencia de MediaPipe y genera texto en el teléfono, token a token. `ModelManager.kt` lo descarga y verifica la primera vez; `LlmEngine.kt` lo ejecuta.

**[2:10]**
Ahora las **capas en cascada** sobre nodos JSON, de la 0 a la 6: la Capa 0, `InputClassifier.kt`, normaliza y detecta saludos o ecos; la Capa 1 y 2, `FaqRetriever.kt` con `FaqAlias.kt`, buscan coincidencia exacta de frase y palabras clave; la Capa 3, `SemanticRetriever.kt`, compara significado con *embeddings*; la Capa 4 sugiere secciones si nada coincide; la Capa 5, `LlmEngine.kt` con `PromptBuilder.kt`, redacta con contexto del JSON; y la Capa 6 cae a la respuesta oficial si el modelo falla.

**[2:45]**
Los archivos clave son `ModelManager.kt` y `LlmEngine.kt` para el `.task`; `FaqRepository.kt`, `FaqEntry.kt` y `FaqAlias.kt` para el JSON; y `InputClassifier.kt`, `FaqRetriever.kt`, `SemanticRetriever.kt`, `ChatViewModel.kt` y `PromptBuilder.kt` para la cascada. Como teoría futura, un `web-admin` edita ese JSON; hoy requiere reconstruir la app, pero la meta es cargarlo en vivo. Muchas gracias.

---

## Anexo — Archivos clave (cheat-sheet del Presentador C)
- **Modelo `.task`:** `llm/LlmEngine.kt`, `llm/ModelManager.kt`.
- **Conocimiento JSON:** `data/FaqEntry.kt`, `data/FaqRepository.kt`, `data/FaqAlias.kt`, `data/FaqAliasRepository.kt`.
- **Cascada:** `data/InputClassifier.kt` (0), `data/FaqRetriever.kt` (1–2), `data/SemanticRetriever.kt` (3), `ChatViewModel.kt` (4 + orquestación), `llm/PromptBuilder.kt` (5), respaldo de calidad (6).
- **Teoría futura:** `web-admin/server.js`, `web-admin/public/`, `web-admin/tests/admin.spec.ts`.

## Consejos para lucirse
- Al decir "JSON vs SQLite", muestra `faq.json` abierto en el editor como ejemplo de nodo.
- Al explicar el `.task`, señala la barra de "Descargando modelo" en el teléfono (es `ModelManager.kt` en acción).
- Al recorrer las capas, puede ir señalando los archivos `data/` y `llm/` en el editor.
