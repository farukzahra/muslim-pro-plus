const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const ctx = browser.contexts()[0];
  const page = ctx.pages()[0];
  await page.goto('https://accountscenter.instagram.com/password_and_security/', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await page.waitForTimeout(5000);
  await page.getByText('Alterar palavra-passe', { exact: true }).first().click();
  await page.waitForTimeout(4000);
  console.log('URL', page.url());
  const inputs = await page.locator('input').evaluateAll(els => els.map((e,i) => ({i, type:e.type, name:e.name, aria:e.getAttribute('aria-label'), ph:e.placeholder})));
  console.log('INPUTS', JSON.stringify(inputs));
  const buttons = await page.getByRole('button').evaluateAll(els => els.map(e => (e.innerText||'').trim()).filter(Boolean));
  console.log('BUTTONS', JSON.stringify(buttons.slice(0,10)));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/change-password.png', fullPage: true });
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
