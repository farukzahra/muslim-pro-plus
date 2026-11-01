const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const ctx = browser.contexts()[0];
  const page = ctx.pages()[0];
  await page.goto('https://www.instagram.com/muslimplus.br/p/DfLOsK-hULz/', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await page.waitForTimeout(5000);
  const buttons = await page.locator('button, [role="button"]').evaluateAll(els => els.map((e,i) => ({i, aria:e.getAttribute('aria-label'), text:(e.innerText||'').trim().slice(0,30)})).filter(x=>x.aria||x.text));
  console.log(JSON.stringify(buttons.slice(0,30), null, 1));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/post-page.png' });
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
