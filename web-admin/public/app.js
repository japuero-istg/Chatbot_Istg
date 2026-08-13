const $ = (id) => document.getElementById(id);

let FAQ = [];
let ALIASES = [];
let currentId = null;

async function load() {
  const [faq, aliases, sections] = await Promise.all([
    fetch('/api/faq').then((r) => r.json()),
    fetch('/api/aliases').then((r) => r.json()),
    fetch('/api/sections').then((r) => r.json()),
  ]);
  FAQ = faq;
  ALIASES = aliases;
  $('sections').innerHTML = sections.map((s) => `<option value="${s}">`).join('');
  renderList('');
  setStatus(`${FAQ.length} entradas · ${ALIASES.length} alias`);
}

function setStatus(msg, ok) {
  const el = $('status');
  el.textContent = msg;
  el.style.color = ok ? 'var(--ok)' : 'var(--muted)';
}

function renderList(filter) {
  const f = filter.trim().toLowerCase();
  const entries = FAQ.filter((e) => !f || e.question.toLowerCase().includes(f) || e.answer.toLowerCase().includes(f));
  const bySection = {};
  for (const e of entries) (bySection[e.section] ||= []).push(e);
  let html = '';
  for (const section of Object.keys(bySection)) {
    html += `<div class="section-title">${section} (${bySection[section].length})</div>`;
    for (const e of bySection[section]) {
      html += `<div class="entry" data-id="${e.id}">
        <div class="q">${e.question}</div>
        <div class="meta">#${e.id}</div>
      </div>`;
    }
  }
  $('list').innerHTML = html;
  document.querySelectorAll('.entry').forEach((el) => {
    el.addEventListener('click', () => openEntry(Number(el.dataset.id)));
  });
}

function aliasesFor(id) {
  const a = ALIASES.find((x) => x.entryId === id);
  return a ? a.aliases.join('\n') : '';
}

function openEntry(id) {
  const e = FAQ.find((x) => x.id === id);
  if (!e) return;
  currentId = id;
  $('formTitle').textContent = `Editar entrada #${id}`;
  $('f_id').value = e.id;
  $('f_section').value = e.section;
  $('f_question').value = e.question;
  $('f_answer').value = e.answer;
  $('f_keywords').value = (e.keywords || []).join(', ');
  $('f_aliases').value = aliasesFor(id);
  $('deleteBtn').style.display = 'inline-block';
}

function newEntry() {
  currentId = null;
  $('formTitle').textContent = 'Nueva entrada';
  $('form').reset();
  $('f_id').value = '';
  $('f_aliases').value = '';
  $('deleteBtn').style.display = 'none';
}

async function save(ev) {
  ev.preventDefault();
  const payload = {
    section: $('f_section').value.trim() || 'General',
    question: $('f_question').value.trim(),
    answer: $('f_answer').value.trim(),
    keywords: $('f_keywords').value,
  };
  try {
    let res;
    if (currentId == null) {
      res = await fetch('/api/faq', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    } else {
      res = await fetch('/api/faq/' + currentId, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    }
    if (!res.ok) throw new Error((await res.json()).error || res.statusText);
    const saved = await res.json();

    // guardar aliases de esta entrada
    const aliases = $('f_aliases').value.split('\n').map((s) => s.trim()).filter(Boolean);
    const entryId = currentId != null ? currentId : saved.id;
    const others = ALIASES.filter((a) => a.entryId !== entryId);
    const updated = aliases.length ? [...others, { entryId, aliases }] : others;
    const r2 = await fetch('/api/aliases', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(updated) });
    if (!r2.ok) throw new Error((await r2.json()).error || 'aliases');

    await load();
    setStatus('Guardado ✓', true);
  } catch (err) {
    setStatus('Error: ' + err.message, false);
  }
}

async function remove() {
  if (currentId == null) return;
  if (!confirm('Eliminar entrada #' + currentId + '?')) return;
  const res = await fetch('/api/faq/' + currentId, { method: 'DELETE' });
  if (res.ok) {
    setStatus('Eliminado ✓', true);
    await load();
    newEntry();
  } else {
    setStatus('Error al eliminar', false);
  }
}

$('search').addEventListener('input', (e) => renderList(e.target.value));
$('form').addEventListener('submit', save);
$('newBtn').addEventListener('click', newEntry);
$('cancelBtn').addEventListener('click', newEntry);
$('deleteBtn').addEventListener('click', remove);

load();
