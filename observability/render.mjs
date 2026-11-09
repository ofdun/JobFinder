import { chromium } from 'playwright';
import { readFile, writeFile, mkdir, cp } from 'node:fs/promises';
import path from 'node:path';

const [source, suite, address = 'http://127.0.0.1:3000'] = process.argv.slice(2);
if (!source || !suite) throw new Error('Usage: node render.mjs RESULTS SUITE [GRAFANA_URL]');
const root = path.resolve(source);
const data = JSON.parse(await readFile(path.join(root, 'comparison.json'), 'utf8'));
const charts = path.join(root, 'grafana');
await mkdir(charts, { recursive: true });
await cp(new URL('./grafana/dashboards/', import.meta.url), path.join(root, 'dashboards'), { recursive: true });
const browser = await chromium.launch({ headless: true, ...(process.env.CHROME_CHANNEL ? { channel: process.env.CHROME_CHANNEL } : {}) });
const escape = value => String(value).replace(/[&<>"']/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch]);
const document = (title, content) => `<!doctype html><html lang="ru"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${escape(title)}</title><style>body{font:16px system-ui;background:#f3f5f9;color:#17263c;margin:24px auto;padding:0 18px;max-width:1440px}img{display:block;max-width:100%;height:auto;margin:20px 0;border:1px solid #d8deea}a{color:#2563eb}nav{display:flex;gap:20px;flex-wrap:wrap}table{border-collapse:collapse;width:100%}td,th{padding:10px;border-bottom:1px solid #d8deea;text-align:left}.table{overflow:auto}</style><h1>${escape(title)}</h1>${content}</html>`;
const labels = {default:'Обычные логи',tracing:'Обычные логи + трейсы',logging:'Расширенные логи',combined:'Расширенные логи + трейсы'};
const profiles = [...new Set(data.runs.map(r => r.profile))];

async function capture(page, dashboard, variables, filename, from, to, expectedPanels) {
  const url = new URL(`/d/${dashboard}/${dashboard}`, address);
  for (const [key, value] of Object.entries(variables)) url.searchParams.set(`var-${key}`, value);
  url.searchParams.set('theme', 'light');
  url.searchParams.set('kiosk', '1');
  url.searchParams.set('from', String(Math.floor(from * 1000)));
  url.searchParams.set('to', String(Math.ceil(to * 1000)));
  const successfulQueries = new Set();
  const pending = [];
  const errors = [];
  const handler = response => {
    if (!response.url().includes('/api/ds/query')) return;
    const request=response.request().postDataJSON();
    const query=request?.queries?.find(q=>q.refId==='A');
    if (!query) return;
    pending.push((async () => {
      const payload = await response.json();
      for (const result of Object.values(payload.results || {})) {
        if (result.error || result.status >= 400) errors.push(result.error || `HTTP ${result.status}`);
        else if (result.frames?.some(f => f.data?.values?.some(v => v.length))) successfulQueries.add(query.rawSql);
      }
    })());
  };
  page.on('response', handler);
  try {
    await page.goto(url.href, { waitUntil: 'domcontentloaded' });
    const deadline = Date.now() + 90000;
    while (successfulQueries.size < expectedPanels && Date.now() < deadline && !errors.length) {
      await page.waitForTimeout(250);
      await Promise.all(pending);
    }
    if (errors.length || successfulQueries.size < expectedPanels) throw new Error(`Grafana ${dashboard}: ${errors.join('; ')}; panels with data ${successfulQueries.size}/${expectedPanels}`);
    await page.waitForTimeout(1000);
    const height=await page.locator('section[data-testid^="data-testid Panel header"]').evaluateAll(
      panels=>Math.ceil(Math.max(...panels.map(panel=>panel.getBoundingClientRect().bottom))+12));
    await page.screenshot({path:path.join(charts,filename),clip:{x:0,y:0,width:1600,height:Math.min(1600,height)}});
  } finally {
    page.off('response',handler);
  }
}

try {
  const page = await browser.newPage({ viewport: { width:1600, height:1600 } });
  let overview = `<p>${data.runs[0].smoke ? 'SMOKE' : 'Полный прогон'}</p><nav><a href="dashboards/comparison.json" download>Дашборд сравнения</a><a href="dashboards/load.json" download>Дашборд нагрузки</a><a href="dashboards/resources.json" download>Дашборд ресурсов</a></nav>`;
  for (const profile of profiles) {
    const runs = data.runs.filter(r => r.profile === profile);
    const summaries = await Promise.all(runs.map(async r => JSON.parse(await readFile(path.join(root,r.run,'summary.json'),'utf8'))));
    await capture(page,'comparison',{suite,profile},`${profile}.png`,Math.min(...summaries.map(s=>s.measurement_start)),Math.max(...summaries.map(s=>s.measurement_stop)),8);
    overview += `<h2>${escape(profile)}</h2><img src="grafana/${profile}.png" alt="${escape(profile)} — Grafana">`;
  }
  overview += '<h2>Прогоны</h2><div class="table"><table><tr><th>Сценарий</th><th>Настройки</th><th>Запросы</th><th>Ошибки</th></tr>';
  for (const run of data.runs) {
    const summary=JSON.parse(await readFile(path.join(root,run.run,'summary.json'),'utf8'));
    const variables={suite,run:`${suite}/${run.run}`};
    await capture(page,'load',variables,`${run.run}-load.png`,summary.measurement_start,summary.measurement_stop,6);
    let content=`<nav><a href="../index.html">Сравнение</a><a href="traces/index.html">Трейсы</a></nav><img src="../grafana/${run.run}-load.png" alt="Нагрузка — Grafana">`;
    for (const component of Object.keys(run.resources)) {
      await capture(page,'resources',{...variables,component},`${run.run}-${component}.png`,summary.measurement_start,summary.measurement_stop,3);
      content+=`<h2>${escape(component)}</h2><img src="../grafana/${run.run}-${component}.png" alt="${escape(component)} — Grafana">`;
    }
    await writeFile(path.join(root,run.run,'index.html'),document(`${run.profile} · ${labels[run.mode]}`,content));
    overview+=`<tr><td><a href="${run.run}/index.html">${escape(run.profile)}</a></td><td>${escape(labels[run.mode])}</td><td>${run.requests}</td><td>${run.unexpected_errors}</td></tr>`;
    console.log(`Rendered ${run.run}`);
  }
  overview+='</table></div><h2>Разница ресурсов приложения</h2><div class="table"><table><tr><th>Сценарий</th><th>Изменение</th><th>Δ CPU, мс</th><th>Δ RAM средняя, МБ</th><th>Δ RAM пик, МБ</th></tr>';
  for (const row of data.differences) overview+=`<tr><td>${escape(row.profile)}</td><td>${escape(row.effect)}</td><td>${(row.cpu_seconds * 1000).toFixed(2)}</td><td>${(row.ram_mean_mib * 2 ** 20 / 1e6).toFixed(2)}</td><td>${(row.ram_peak_mib * 2 ** 20 / 1e6).toFixed(2)}</td></tr>`;
  await writeFile(path.join(root,'index.html'),document('Трассировка и логирование',overview+'</table></div>'));
} finally {
  await browser.close();
}
