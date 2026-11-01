const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9228');
  const page = browser.contexts()[0].pages().find(p => p.url().includes('instagram.com'));
  await page.bringToFront();
  const input = page.locator('input[type="text"]').first();
  await input.click();
  await input.fill('muslimplus.br');
  await page.waitForTimeout(2000);
  console.log('value', await input.inputValue());
  const btn = page.locator('div[role="button"]').filter({ hasText: /Avançar|Continuar/ }).first();
  console.log('disabled', await btn.getAttribute('aria-disabled'));
  await btn.click();
  await page.waitForTimeout(9000);
  console.log('URL', page.url());
  console.log('BODY', JSON.stringify((await page.locator('body').innerText()).slice(0, 800)));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/reset-step4.png' });
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
