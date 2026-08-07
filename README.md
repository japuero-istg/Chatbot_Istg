# Emprende ISTG - Asistente (Chatbot RAG + LLM local)

Este proyecto une:
1. La UI de Compose del esqueleto original (`ChatPage.kt`, tema, `MessageModel`).
2. El FAQ de Emprende ISTG, convertido de markdown a `assets/faq.json` (60 preguntas).
3. Un pipeline RAG: búsqueda por palabras clave sobre el FAQ + generación con un
   LLM corriendo **en el dispositivo** (MediaPipe LLM Inference API), en vez de
   llamar a Gemini en la nube.
4. **Lectura en voz alta** de las respuestas con el TTS nativo de Android,
   con un icono de parlante en el chat para silenciarlo (preferencia persistida).

## Qué se quitó del esqueleto original
- `Constants.kt` (tenía la API key de Gemini expuesta en texto plano — revócala
  en Google AI Studio si no lo has hecho).
- La dependencia `com.google.ai.client.generativeai` (SDK de Gemini en la nube).
- El `ChatViewModel` anterior, que llamaba directo a Gemini.

## Arquitectura nueva

```
app/src/main/java/com/learning/mychatbotapp/
├── data/
│   ├── FaqEntry.kt            # modelo de una pregunta/respuesta
│   ├── FaqRepository.kt       # carga faq.json desde assets
│   ├── FaqRetriever.kt        # capa 1: búsqueda por palabras clave (sin IA)
│   └── SemanticRetriever.kt   # capa 2: búsqueda por embeddings (significado)
├── llm/
│   ├── ModelManager.kt        # descarga el modelo .task (GitHub Releases)
│   ├── LlmEngine.kt           # wrapper de MediaPipe LLM Inference (Gemma)
│   ├── PromptBuilder.kt       # arma el prompt con el contexto del FAQ
│   └── EmbeddingEngine.kt     # wrapper de MediaPipe TextEmbedder (embeddings)
├── TtsManager.kt              # lectura en voz alta (TextToSpeech) + mute
├── ChatViewModel.kt           # orquesta: busca contexto -> genera respuesta
├── ChatPage.kt                # UI del chat (con banner de descarga del modelo)
├── MainActivity.kt
└── ... (SplashScreen, MenuScreen, AppScreen, MessageModel, tema)
```

`assets/faq.json` fue generado automáticamente desde tu `.md` original con un
script Python (parseo por encabezados `##`/`###`). Si editas el `.md`, puedes
re-generar el JSON sin retocar nada del código Kotlin.

## El modelo `.task` (ya configurado)

- **Qué usa hoy**: **Gemma 3 270M** cuantizado q8, en formato `.task`
  (`faq_model.task`, ~290 MB), del org oficial `litert-community` en
  Hugging Face.
- **Dónde se descarga**: está alojado en GitHub Releases del repo público
  `japuero-istg/Chatbot_Istg` (tag `v1.0.0`) y `MODEL_URL` en
  `ModelManager.kt` ya apunta a esa URL directa (verificada HTTP 200). La
  app lo descarga automáticamente la primera vez que se abre.
- **Runtime**: `com.google.mediapipe:tasks-genai:0.10.35` (0.10.27 no podía
  cargar el formato de la 270M y crasheaba — ver `document/MANUAL_TECNICO.md`).
- Para desarrollo/pruebas rápidas sin descarga, puedes usar `adb push` del
  `.task` a `/sdcard/Android/data/com.learning.mychatbotapp/files/faq_model.task`
  y saltarte `downloadModel()`.

## Funciones

- **Chat con FAQ**: menú de categorías + pregunta directa, con búsqueda
  híbrida (palabras clave → embeddings semánticos → fallback).
- **Respuestas en voz alta (TTS)**: cada respuesta del modelo se lee
  automáticamente usando el `TextToSpeech` del sistema (offline, sin
  permisos). El icono de **parlante** en la barra superior del chat
  silencia/activa la voz y la preferencia se guarda entre sesiones.

## Avisos

1. **MediaPipe LLM Inference API** está en **"maintenance-only"**; Google
   recomienda migrar a **LiteRT-LM** para proyectos nuevos. Sigue siendo
   válida y funcional aquí, pero conviene revisarlo antes de invertir más
   tiempo en esta ruta.
2. **Tamaño y rendimiento**: el modelo pesa cientos de MB (no cabe en el APK
   para Play Store), por eso se descarga la primera vez que se abre la app.
   Pruébalo en un dispositivo físico de gama media/alta — el emulador no
   aprovecha GPU y va muy lento.
3. **Seguridad**: no repitas el error del proyecto original — nunca subas API
   keys ni URLs de modelos con credenciales embebidas directo al repo público.
