#!/usr/bin/env python3
"""Analiza capturas de pantalla con un modelo de visión vía OpenRouter.
Equivalente a mcp-vision-gratuito (mismo modelo qwen2.5-vl-32b-instruct:free).
Lee la API key del config de opencode (no se hardcodea ni se imprime).
Uso: python3 vision_analyze.py <prompt> img1.png [img2.png ...]
"""
import base64
import json
import os
import sys
import urllib.request

CONFIG = os.path.expanduser("~/.config/opencode/opencode.json")
with open(CONFIG, encoding="utf-8") as f:
    cfg = json.load(f)
KEY = cfg["mcp"]["mcp-vision-gratuito"]["env"]["OPENROUTER_API_KEY"]
# El modelo del config (qwen/qwen2.5-vl-32b-instruct:free) NO existe en OpenRouter
# (devuelve 404). Se usa un modelo de visión gratis válido; puedes sobreescribirlo
# con la env VISION_MODEL.
MODEL = os.environ.get("VISION_MODEL", "nvidia/nemotron-nano-12b-v2-vl:free")

prompt = sys.argv[1]
images = sys.argv[2:]

content = [{"type": "text", "text": prompt}]
for img in images:
    with open(img, "rb") as fh:
        b64 = base64.b64encode(fh.read()).decode()
    content.append({"type": "image_url", "image_url": {"url": f"data:image/png;base64,{b64}"}})

payload = {
    "model": MODEL,
    "messages": [{"role": "user", "content": content}],
    "max_tokens": 1200,
}
req = urllib.request.Request(
    "https://openrouter.ai/api/v1/chat/completions",
    data=json.dumps(payload).encode(),
    headers={"Authorization": f"Bearer {KEY}", "Content-Type": "application/json"},
)
with urllib.request.urlopen(req, timeout=120) as r:
    data = json.load(r)
print(data["choices"][0]["message"]["content"])
