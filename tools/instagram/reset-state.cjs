const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9228');
  const page = browser.contexts()[0].pages().find(p => p.url().includes('instagram.com'));
  await page.bringToFront();
  await page.waitForTimeout(5000);
  console.log('URL', page.url());
  console.log('BODY', JSON.stringify((await page.locator('body').innerText()).slice(0, 900)));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/reset-state.png', fullPage: true });
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
