const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9228');
  const page = browser.contexts()[0].pages().find(p => p.url().includes('instagram.com'));
  await page.bringToFront();
  const input = page.locator('input[type="text"]').first();
  if (!(await input.inputValue())) await input.fill('muslimplus.br');
  await page.getByRole('button', { name: /Avançar|Continuar/ }).first().click();
  await page.waitForTimeout(9000);
  console.log('URL', page.url());
  console.log('BODY', JSON.stringify((await page.locator('body').innerText()).slice(0, 700)));
  const buttons = await page.getByRole('button').evaluateAll(els => els.map(e => (e.innerText||'').trim()).filter(Boolean));
  console.log('BUTTONS', JSON.stringify(buttons.slice(0,10)));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/reset-step3.png' });
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
