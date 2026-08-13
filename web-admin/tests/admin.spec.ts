import { test, expect } from '@playwright/test';
import fs from 'fs';
import path from 'path';

const FIX = path.join(__dirname, 'fixtures');
const FAQ = path.join(FIX, 'faq.json');
const ALIASES = path.join(FIX, 'faq_aliases.json');

const PRISTINE_FAQ = [
  { id: 101, section: 'Tests', question: 'Pregunta de prueba uno', answer: 'Respuesta original uno', keywords: ['test', 'uno'] },
  { id: 102, section: 'Tests', question: 'Pregunta de prueba dos', answer: 'Respuesta original dos', keywords: ['test', 'dos'] },
  { id: 103, section: 'Tests', question: 'Pregunta de prueba tres', answer: 'Respuesta original tres', keywords: ['test', 'tres'] },
];
const PRISTINE_ALIASES = [
  { entryId: 101, aliases: ['prueba uno', 'pregunta 1'] },
  { entryId: 102, aliases: ['prueba dos', 'pregunta 2'] },
  { entryId: 103, aliases: ['prueba tres', 'pregunta 3'] },
];

function resetFixtures() {
  fs.writeFileSync(FAQ, JSON.stringify(PRISTINE_FAQ, null, 2) + '\n');
  fs.writeFileSync(ALIASES, JSON.stringify(PRISTINE_ALIASES, null, 2) + '\n');
}
function readFaq() {
  return JSON.parse(fs.readFileSync(FAQ, 'utf8'));
}
function readAliases() {
  return JSON.parse(fs.readFileSync(ALIASES, 'utf8'));
}

test.beforeEach(async ({ page }) => {
  resetFixtures();
  await page.goto('/');
  await expect(page.locator('.entry')).toHaveCount(3);
});

test('edita una entrada y persiste en disco', async ({ page }) => {
  await page.locator('.entry[data-id="101"]').click();
  await expect(page.locator('#f_question')).toHaveValue('Pregunta de prueba uno');
  await page.locator('#f_answer').fill('Respuesta MODIFICADA por test');
  await page.locator('#form button[type="submit"]').click();
  await expect(page.locator('#status')).toContainText('Guardado');

  await page.reload();
  await page.locator('.entry[data-id="101"]').click();
  await expect(page.locator('#f_answer')).toHaveValue('Respuesta MODIFICADA por test');

  const disk = readFaq();
  expect(disk.find((e) => e.id === 101).answer).toBe('Respuesta MODIFICADA por test');
});

test('crea una entrada nueva', async ({ page }) => {
  await page.locator('#newBtn').click();
  await page.locator('#f_section').fill('Tests');
  await page.locator('#f_question').fill('Pregunta creada por test');
  await page.locator('#f_answer').fill('Respuesta nueva');
  await page.locator('#form button[type="submit"]').click();
  await expect(page.locator('#status')).toContainText('Guardado');

  await expect(page.locator('.entry')).toHaveCount(4);
  const disk = readFaq();
  expect(disk.some((e) => e.question === 'Pregunta creada por test')).toBe(true);
});

test('edita aliases y persiste', async ({ page }) => {
  await page.locator('.entry[data-id="102"]').click();
  await page.locator('#f_aliases').fill('alias a\nalias b\nalias c');
  await page.locator('#form button[type="submit"]').click();
  await expect(page.locator('#status')).toContainText('Guardado');

  const disk = readAliases();
  const a = disk.find((x) => x.entryId === 102);
  expect(a.aliases).toEqual(['alias a', 'alias b', 'alias c']);
});

test('elimina una entrada', async ({ page }) => {
  page.on('dialog', (d) => d.accept());
  await page.locator('.entry[data-id="103"]').click();
  await page.locator('#deleteBtn').click();
  await expect(page.locator('.entry')).toHaveCount(2);
  const disk = readFaq();
  expect(disk.some((e) => e.id === 103)).toBe(false);
});
