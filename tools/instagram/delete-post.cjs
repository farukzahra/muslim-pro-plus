const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const page = browser.contexts()[0].pages()[0];
  await page.bringToFront();
  await page.getByText('Eliminar', { exact: true }).first().click();
  await page.waitForTimeout(2500);
  const body = (await page.locator('body').innerText().catch(()=>'')).slice(-350);
  console.log('DIALOG', JSON.stringify(body));
  const confirm = page.getByRole('button', { name: 'Eliminar' }).last();
  console.log('confirm count', await confirm.count());
  if (await confirm.count()) { await confirm.click(); }
  else { await page.getByText('Eliminar', { exact: true }).last().click(); }
  await page.waitForTimeout(8000);
  console.log('URL', page.url());
  await page.goto('https://www.instagram.com/muslimplus.br/', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await page.waitForTimeout(5000);
  const profile = (await page.locator('body').innerText().catch(()=>'')).slice(0, 260);
  console.log('PROFILE', JSON.stringify(profile));
  console.log('post links', await page.locator('a[href*="/p/"]').count());
  await browser.close();
})().catch(e => { console.error('ERRO', e.message); process.exit(1); });
