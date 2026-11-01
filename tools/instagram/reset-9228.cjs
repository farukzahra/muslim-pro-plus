const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9228');
  const ctx = browser.contexts()[0];
  const page = ctx.pages().find(p => p.url().includes('instagram.com')) || ctx.pages()[0];
  await page.bringToFront();
  await page.waitForTimeout(2500);
  console.log('URL', page.url());
  console.log('BODY', JSON.stringify((await page.locator('body').innerText()).slice(0, 300)));
  const inputs = await page.locator('input').evaluateAll(els => els.map((e,i) => ({i, type:e.type, aria:e.getAttribute('aria-label')})));
  console.log('INPUTS', JSON.stringify(inputs));
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
