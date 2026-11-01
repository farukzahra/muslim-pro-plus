const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const ctx = await browser.newContext({ locale: 'pt-BR' });
  const page = await ctx.newPage();
  await page.goto('https://www.instagram.com/accounts/password/reset/', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await page.waitForTimeout(4000);
  console.log('URL', page.url());
  console.log('BODY', JSON.stringify((await page.locator('body').innerText()).slice(0, 500)));
  const inputs = await page.locator('input').evaluateAll(els => els.map((e,i) => ({i, type:e.type, name:e.name, aria:e.getAttribute('aria-label')})));
  console.log('INPUTS', JSON.stringify(inputs));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/reset-start.png' });
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
