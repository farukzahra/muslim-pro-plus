const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const page = browser.contexts()[0].pages()[0];
  await page.bringToFront();
  for (let i = 0; i < 3; i++) { await page.keyboard.press('Escape'); await page.waitForTimeout(700); }
  console.log('URL', page.url());
  console.log('BODY', JSON.stringify((await page.locator('body').innerText().catch(()=>'')).slice(-200)));
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
