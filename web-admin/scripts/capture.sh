#!/usr/bin/env bash
# Captura pantallas del Redmi para el pipeline de pulido con visión.
# Uso:
#   ./scripts/capture.sh auto     -> abre la app, manda una pregunta y captura chat vacío + con respuesta
#   ./scripts/capture.sh all      -> captura varias pantallas con pausas para navegar manualmente
#   ./scripts/capture.sh <nombre> -> captura la pantalla actual a screenshots/<nombre>.png
set -uo pipefail

OUT="$(cd "$(dirname "$0")/../.." && pwd)/screenshots"
mkdir -p "$OUT"

DEVICE=$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')
if [ -z "$DEVICE" ]; then
  echo "ERROR: no hay dispositivo Android conectado/autorizado (adb devices vacío)." >&2
  echo "Habilita Depuración USB en el Redmi y acepta la autorización en pantalla." >&2
  exit 1
fi
echo "Dispositivo: $DEVICE"

PKG=com.learning.mychatbotapp
ACT=.MainActivity

shot() {
  local name="$1"
  local f="$OUT/$name.png"
  adb -s "$DEVICE" shell screencap -p /sdcard/_s.png
  adb -s "$DEVICE" pull /sdcard/_s.png "$f" >/dev/null
  adb -s "$DEVICE" shell rm -f /sdcard/_s.png
  echo "  -> $f"
}

# Devuelve "x y" (centro) del primer nodo cuyo class contiene $1 y cuyo text
# contiene $2 (opcional, case-insensitive).
ui_center() {
  local cls="$1" txt="${2:-}"
  local tmp=/tmp/ui_$$.xml
  adb -s "$DEVICE" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  adb -s "$DEVICE" pull /sdcard/ui.xml "$tmp" >/dev/null 2>&1
  python3 - "$tmp" "$cls" "$txt" <<'PY'
import sys, re
xml = open(sys.argv[1], encoding='utf-8', errors='ignore').read()
cls, txt = sys.argv[2], sys.argv[3].lower()
for n in re.findall(r'<node\b[^>]*>', xml):
    c = re.search(r'class="([^"]*)"', n)
    t = re.search(r'text="([^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if not (c and b): continue
    if cls not in c.group(1): continue
    if txt and (not t or txt not in t.group(1).lower()): continue
    x1, y1, x2, y2 = map(int, b.groups())
    print(f"{(x1+x2)//2} {(y1+y2)//2}")
    break
PY
  adb -s "$DEVICE" shell rm -f /sdcard/ui.xml
}

tap_center() {
  local c
  c=$(ui_center "$@")
  if [ -n "$c" ]; then adb -s "$DEVICE" shell input tap $c; return 0; fi
  return 1
}

cmd="${1:-all}"

if [ "$cmd" = "auto" ]; then
  echo ">>> Abriendo la app..."
  adb -s "$DEVICE" shell am start -n "$PKG/$ACT" >/dev/null 2>&1
  sleep 4
  echo ">>> Captura 01_chat_vacio"; shot 01_chat_vacio
  echo ">>> Escribiendo pregunta 'como publico mi negocio'..."
  if tap_center "EditText" ""; then
    adb -s "$DEVICE" shell input text "como publico mi negocio"
    sleep 1
    tap_center "Button" "enviar" || tap_center "ImageView" "enviar" || echo "  (no encontré botón enviar; envíalo manualmente)"
  else
    echo "  (no encontré el campo de texto; escribe y envía manualmente)"
  fi
  echo ">>> Esperando respuesta del modelo (10s)..."; sleep 10
  echo ">>> Captura 02_chat_respuesta"; shot 02_chat_respuesta
  echo "Hecho. Para más pantallas: './scripts/capture.sh all' y navega manualmente."
  exit 0
fi

if [ "$cmd" = "all" ]; then
  for s in chat saludo registro invitado secciones fallback detalle; do
    echo ">>> Pantalla '$s': capturando en 6s (navega en la app)..."
    sleep 6
    shot "$s"
  done
  exit 0
fi

shot "${1:-screen}"
