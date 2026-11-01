const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const page = browser.contexts()[0].pages()[0];
  await page.bringToFront();
  await page.mouse.click(855, 62);
  await page.waitForTimeout(2500);
  const body = (await page.locator('body').innerText().catch(()=>'')).slice(-400);
  console.log('BODY', JSON.stringify(body));
  const items = await page.locator('[role="menuitem"], [role="button"]').evaluateAll(els => els.map(e => ({t:(e.innerText||'').trim().slice(0,30), aria:e.getAttribute('aria-label')})).filter(x=>x.t||x.aria));
  console.log('ITEMS', JSON.stringify(items.slice(-15)));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/post-menu.png' });
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
