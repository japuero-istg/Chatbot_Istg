const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

const app = express();
app.use(cors());
app.use(express.json({ limit: '5mb' }));
app.use(express.static(path.join(__dirname, 'public')));

// Rutas a los assets de la app Android (padre de web-admin).
// Si FAQ_DATA_DIR está definido (p.ej. tests), se usa ese directorio en su lugar.
const ASSETS_DIR = process.env.FAQ_DATA_DIR
  ? path.resolve(process.env.FAQ_DATA_DIR)
  : path.join(__dirname, '..', 'app', 'src', 'main', 'assets');
const FAQ_PATH = path.join(ASSETS_DIR, 'faq.json');
const ALIASES_PATH = path.join(ASSETS_DIR, 'faq_aliases.json');

function readJson(p) {
  return JSON.parse(fs.readFileSync(p, 'utf8'));
}
function writeJson(p, data) {
  fs.writeFileSync(p, JSON.stringify(data, null, 2) + '\n', 'utf8');
}
function nextId(faq) {
  return faq.reduce((m, x) => Math.max(m, x.id), 0) + 1;
}

app.get('/api/faq', (req, res) => res.json(readJson(FAQ_PATH)));
app.get('/api/aliases', (req, res) => res.json(readJson(ALIASES_PATH)));
app.get('/api/sections', (req, res) => {
  const faq = readJson(FAQ_PATH);
  res.json([...new Set(faq.map((e) => e.section))]);
});

app.post('/api/faq', (req, res) => {
  const faq = readJson(FAQ_PATH);
  const e = req.body || {};
  if (!e.question || !e.answer) {
    return res.status(400).json({ error: 'question y answer son requeridos' });
  }
  const id = e.id != null ? Number(e.id) : nextId(faq);
  if (faq.some((x) => x.id === id)) {
    return res.status(400).json({ error: 'id duplicado: ' + id });
  }
  const entry = {
    id,
    section: e.section || 'General',
    question: e.question,
    answer: e.answer,
    keywords: Array.isArray(e.keywords) ? e.keywords : (e.keywords || '').split(',').map((s) => s.trim()).filter(Boolean),
  };
  faq.push(entry);
  writeJson(FAQ_PATH, faq);
  res.status(201).json(entry);
});

app.put('/api/faq/:id', (req, res) => {
  const faq = readJson(FAQ_PATH);
  const id = Number(req.params.id);
  const i = faq.findIndex((x) => x.id === id);
  if (i < 0) return res.status(404).json({ error: 'entrada no encontrada: ' + id });
  const e = req.body || {};
  const cur = faq[i];
  faq[i] = {
    id,
    section: e.section != null ? e.section : cur.section,
    question: e.question != null ? e.question : cur.question,
    answer: e.answer != null ? e.answer : cur.answer,
    keywords:
      e.keywords != null
        ? Array.isArray(e.keywords)
          ? e.keywords
          : e.keywords.split(',').map((s) => s.trim()).filter(Boolean)
        : cur.keywords,
  };
  writeJson(FAQ_PATH, faq);
  res.json(faq[i]);
});

app.delete('/api/faq/:id', (req, res) => {
  const faq = readJson(FAQ_PATH);
  const id = Number(req.params.id);
  const next = faq.filter((x) => x.id !== id);
  if (next.length === faq.length) return res.status(404).json({ error: 'entrada no encontrada: ' + id });
  writeJson(FAQ_PATH, next);
  const aliases = readJson(ALIASES_PATH).filter((a) => a.entryId !== id);
  writeJson(ALIASES_PATH, aliases);
  res.json({ ok: true, removed: faq.length - next.length });
});

app.put('/api/aliases', (req, res) => {
  const aliases = req.body;
  if (!Array.isArray(aliases)) return res.status(400).json({ error: 'se espera un arreglo de aliases' });
  const ids = new Set(readJson(FAQ_PATH).map((e) => e.id));
  for (const a of aliases) {
    if (!ids.has(a.entryId)) return res.status(400).json({ error: 'entryId inválido: ' + a.entryId });
  }
  writeJson(ALIASES_PATH, aliases);
  res.json({ ok: true, count: aliases.length });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`FAQ admin en http://localhost:${PORT}`));
