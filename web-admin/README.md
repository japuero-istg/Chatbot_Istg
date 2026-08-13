# Emprende ISTG · FAQ Admin (web-admin)

Panel web local para editar `faq.json` y `faq_aliases.json` de la app Android
Emprende ISTG ChatBot, más tests e2e con Playwright y un pipeline de captura
de pantallas para pulir la UI con un modelo de visión.

## Requisitos
- Node 18+ (probado con v24)
- Para los tests: `@playwright/test` y navegadores (`npx playwright install`)
- Para capturar pantallas: `adb` y el Redmi conectado con depuración USB

## Arrancar el panel
```bash
cd web-admin
npm install
npm start            # http://localhost:3000
```
El panel edita directamente `../app/src/main/assets/faq.json` y `faq_aliases.json`.
Para apuntar a otro directorio usa `FAQ_DATA_DIR`:
```bash
FAQ_DATA_DIR=/ruta/a/assets npm start
```

## Tests e2e (Playwright)
Usan un directorio aislado (`tests/fixtures`) vía `FAQ_DATA_DIR`, por lo que
**no tocan** el `faq.json` real de la app.
```bash
cd web-admin
npm install
npx playwright install chromium
npm test             # o: npx playwright test
```

## Capturar pantallas para pulido (visión)
Con el Redmi conectado:
```bash
cd web-admin
bash scripts/capture.sh all      # captura varias pantallas con pausas
bash scripts/capture.sh chat     # captura una pantalla con nombre
```
Luego procesa `screenshots/*.png` con `mcp-vision-gratuito` (modelo de visión
gratuito en OpenRouter) para obtener un informe de pulido y aplica los fixes
en Compose (`app/src/main/java/.../ChatPage.kt`, tema/colores/tipografía).

## Estructura
- `server.js` — API Express (CRUD de faq + aliases)
- `public/` — frontend (lista + formulario)
- `tests/` — specs Playwright + fixtures aislados
- `scripts/capture.sh` — captura de pantallas vía adb
