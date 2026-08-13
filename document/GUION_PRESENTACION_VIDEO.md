# Guion de presentación en video — Emprende ISTG ChatBot (MVP)

**Formato:** 2 expositores, 3 minutos cada uno (6 min total).
**Estilo:** texto literal para leer frente a cámara (teleprompter).
**Instrucciones:** los tiempos entre corchetes son referenciales; las acotaciones `[...]` son indicaciones de escena, no se leen.
**Ubicación:** este archivo vive junto al `DOCUMENTO_TECNICO_MVP.md` en `document/`.

---

## Presentador A — "Experiencia y navegación" (0:00 – 3:00)

**[0:00]**
¡Hola a todos! Somos el equipo de **Emprende ISTG ChatBot** y hoy presentamos el Producto Mínimo Viable de nuestra asistente virtual, pensada para que los estudiantes y emprendedores del Instituto Superior Tecnológico Guayaquil resuelvan sus dudas sobre la app en segundos, directo desde el celular y sin necesidad de internet.

**[0:30]**
Lo primero que deben saber es que esta app es **totalmente on-device**: toda la inteligencia vive en el teléfono, no hay servidores en la nube. Y la navegación es muy sencilla; la controla un único archivo, `AppScreen.kt`. Ahí definimos, con una *sealed class*, las tres pantallas de la app: **Splash**, **Menú** y **Chat**.

**[0:55]**
El pegamento de todo es `MainActivity.kt`. Es nuestra Activity principal: crea el `ChatViewModel`, prepara el motor de voz, y con Jetpack Compose y un *Scaffold* decide qué pantalla mostrar según el estado actual. De ahí saltan las tres vistas que mencioné.

**[1:15]**
Arrancamos en `SplashScreen.kt`, la pantalla de bienvenida con la mascota robot; basta tocar la pantalla para entrar. Luego aparece `MenuScreen.kt`: un grid de dos columnas donde cada tarjeta es una sección del FAQ, con su ícono, y abajo una barra para hacer una pregunta directa al asistente.

**[1:55]**
Y este es el corazón visual, `ChatPage.kt`. Fíjense en sus partes: arriba, un encabezado con botón de inicio y para silenciar la voz; debajo, un banner que avisa el estado del modelo —cargando, listo o error—; en el centro, la lista de mensajes con burbujas, avatar y un pequeño cronómetro del tiempo de respuesta; y al fondo, el campo para escribir. Cada burbuja es un `MessageModel.kt`, y la voz la da `TtsManager.kt`, que lee las respuestas en español.

**[2:40]**
Hasta aquí la experiencia del usuario. Ahora le paso la palabra a mi compañero para que nos cuente cómo piensa la app por detrás.

---

## Presentador B — "La inteligencia detrás" (3:00 – 6:00)

**[3:00]**
Gracias. Detrás de cada respuesta está `ChatViewModel.kt`, el cerebro de la app. Cuando el usuario manda un mensaje, este archivo orquesta todo el flujo en lo que llamamos las **Capas 0 a 6**.

**[3:25]**
La Capa 0 es `InputClassifier.kt`: normaliza el texto, quita acentos y abreviaturas, y clasifica si es un saludo, un agradecimiento, una despedida o una pregunta real; además detecta si el modelo repite la pregunta, lo que llamamos el "eco". Si es una pregunta, buscamos en los datos.

**[3:50]**
Aquí entra el paquete `data`. `FaqEntry.kt` es el modelo de cada pregunta del FAQ. `FaqRepository.kt` carga el `faq.json` desde los *assets* y lo guarda en memoria. Para respuestas rápidas usamos `FaqRetriever.kt`, que busca por alias y palabras clave, y `FaqAlias.kt` con `FaqAliasRepository.kt`, que mapean frases exactas a la respuesta correcta. Si no hay coincidencia literal, `SemanticRetriever.kt` hace la búsqueda semántica con *embeddings*.

**[4:40]**
Toda la generación del lenguaje está en el paquete `llm`. `LlmEngine.kt` corre **Gemma 270M** en el dispositivo con MediaPipe, con tres intentos por si el modelo se "calla". `ModelManager.kt` descarga y verifica el modelo al primer uso; `EmbeddingEngine.kt` crea los vectores con *Universal Sentence Encoder*; y `PromptBuilder.kt` arma el prompt con contexto del FAQ, prohibiendo inventar, o entrega una respuesta de respaldo.

**[5:25]**
Para cerrar: el tema visual lo define `ui/theme` —`Color.kt`, `Theme.kt` y `Type.kt`— y la voz la controla `TtsManager.kt` en español. El resultado es un asistente rápido, offline y fácil de mantener con su *web-admin*. Muchas gracias.

---

## Consejos para lucirse
- Practiquen cada bloque por separado con cronómetro; el objetivo es terminar cerca de 2:50–3:05.
- Presentador A puede ir avanzando pantallas en el teléfono mientras habla de `SplashScreen`, `MenuScreen` y `ChatPage`.
- Presentador B puede abrir `DOCUMENTO_TECNICO_MVP.md` o el código en el editor para señalar los archivos `data/` y `llm/` al nombrarlos.
- Cierran con una **demo en vivo**: una pregunta desde el menú y otra "suelto" para mostrar el cronómetro de latencia.
