import json
import re
import unicodedata

SRC = "faq_source.md"
OUT = "faq.json"

STOPWORDS = {
    "como","que","para","los","las","del","una","uno","con","por","los",
    "puedo","hago","son","que","mis","que","esta","este","estan","son",
    "y","o","de","la","el","en","a","al","es","un","mi","tu","se","no",
    "si","sin","the"
}

def strip_accents(s: str) -> str:
    nfkd = unicodedata.normalize("NFKD", s)
    return "".join(c for c in nfkd if not unicodedata.combining(c))

def keywords_from(text: str):
    norm = strip_accents(text.lower())
    norm = re.sub(r"[^a-z0-9\s]", " ", norm)
    tokens = [t for t in norm.split() if len(t) > 2 and t not in STOPWORDS]
    # dedupe preserving order
    seen = set()
    out = []
    for t in tokens:
        if t not in seen:
            seen.add(t)
            out.append(t)
    return out

with open(SRC, encoding="utf-8") as f:
    lines = f.readlines()

entries = []
current_section = ""
current_question = None
current_answer_lines = []
entry_id = 1

def flush():
    global current_question, current_answer_lines, entry_id
    if current_question is not None:
        answer = " ".join(l.strip() for l in current_answer_lines if l.strip())
        answer = re.sub(r"\*\*", "", answer)  # strip bold markers
        answer = re.sub(r"\s+", " ", answer).strip()
        if answer:
            q_kw = keywords_from(current_question)
            entries.append({
                "id": entry_id,
                "section": current_section,
                "question": current_question.strip(),
                "answer": answer,
                "keywords": q_kw
            })
            entry_id += 1
    current_question = None
    current_answer_lines = []

for raw in lines:
    line = raw.rstrip("\n")
    if line.startswith("## "):
        flush()
        current_section = line[3:].strip()
        current_section = re.sub(r"^Sección\s*\d+:\s*", "", current_section, flags=re.IGNORECASE)
        continue
    if line.startswith("### "):
        flush()
        current_question = line[4:].strip()
        continue
    if line.strip() == "---":
        continue
    if current_question is not None:
        current_answer_lines.append(line)

flush()

with open(OUT, "w", encoding="utf-8") as f:
    json.dump(entries, f, ensure_ascii=False, indent=2)

print(f"Generadas {len(entries)} entradas en {OUT}")
