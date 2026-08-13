# Reporte de Pulido de UI — Emprende ISTG ChatBot

## Estado
- Redmi conectado (`2ca1d0f0`), APK con las Capas 3/4/6 + aliases instalado.
- Captura de pantallas: funciona (`adb shell screencap`).
- **Bloqueo**: `adb shell input tap/text` lanza `SecurityException: INJECT_EVENTS`
  en MIUI → no se puede enviar un mensaje para capturar una respuesta del bot.
  La pantalla de bienvenida ("Toca en cualquier parte para comenzar") no tiene burbujas.

## Análisis de visión (OpenRouter)
> El modelo del config `qwen/qwen2.5-vl-32b-instruct:free` **no existe** en
> OpenRouter (404). Se usó `qwen/qwen2.5-vl-72b-instruct` (la cuenta tiene
> créditos). El gratis `nvidia/nemotron-nano-12b-v2-vl:free` existe pero da 429
> (límite de tasa). → Corregir el modelo en `opencode.json` del MCP
> `mcp-vision-gratuito` a uno válido.

Hallazgos priorizados (sobre las capturas disponibles):
1. **Contraste en burbuja del asistente**: texto claro sobre gris medio →
   riesgo de bajo contraste. (El modelo puede haber malinterpretado la pantalla
   de bienvenida, pero se endureció el fondo de todos modos.)
2. **Espaciado entre burbujas** algo justo.
3. **Tamaño de fuente** de la burbuja mejorable para legibilidad.
4. **Avatar** algo cerca del texto.
5. **Botón de enviar** (azul) podría ganar definición.

## Fixes aplicados (código)
- `ui/theme/Color.kt:25` — `ColorModelMessage` = `0xFF262A32` (gris oscuro,
  alto contraste con texto claro `TextOnDark`), en vez de `SurfaceCard`.
- `ChatPage.kt` `MessageRow`:
  - Fuente de burbuja explícita `16.sp` + `lineHeight 22.sp` (legibilidad).
  - Separación vertical entre burbujas `8.dp` → `12.dp`.
  - Avatar `28.dp` → `32.dp`; padding horizontal burbuja `8.dp` → `10.dp`.
  - Borde sutil 1.dp en la burbuja (definición visual).
  - Import añadido: `androidx.compose.foundation.border`.
- Compila (`assembleDebug` → BUILD SUCCESSFUL) e instala en el Redmi.

## Verificación pendiente
La mejora de las burbujas NO pudo confirmarse visualmente porque no se pudo
enviar un mensaje (bloqueo INJECT_EVENTS de MIUI). Para cerrar el bucle:
- **Opción A (rápida)**: en el Redmi, Opciones de desarrollador →
  **Depuración USB (ajustes de seguridad)** → ON; luego `adb shell input` funciona
  y capturo una respuesta real para re-analizar.
- **Opción B**: revisa en el dispositivo que las burbujas se lean bien.

## Archivos
- `app/src/main/java/com/learning/mychatbotapp/ui/theme/Color.kt`
- `app/src/main/java/com/learning/mychatbotapp/ChatPage.kt`
- `web-admin/scripts/vision_analyze.py` (análisis de visión vía OpenRouter)
- `web-admin/scripts/capture.sh` (captura por adb)
