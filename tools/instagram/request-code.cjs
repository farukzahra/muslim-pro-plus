const { chromium } = require('playwright');
(async () => {
  const ib = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const page = ib.contexts()[0].pages()[0];
  await page.bringToFront();
  const again = page.getByText('Obter um código novo', { exact: true }).first();
  console.log('again', await again.count());
  if (await again.count()) { await again.click(); await page.waitForTimeout(12000); }
  const body = (await page.locator('body').innerText().catch(()=>'')).slice(-260);
  console.log('BODY', JSON.stringify(body));
  await ib.close();

  const gb = await chromium.connectOverCDP('http://127.0.0.1:9222');
  const mail = await gb.contexts()[0].newPage();
  await mail.goto('https://mail.google.com/mail/u/0/#search/from%3Ainstagram', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await mail.waitForTimeout(6000);
  const rows = mail.locator('tr.zA');
  console.log('rows', await rows.count());
  for (let i = 0; i < Math.min(4, await rows.count()); i++) {
    console.log(i, JSON.stringify((await rows.nth(i).innerText()).replace(/\n/g, ' | ').slice(0, 200)));
  }
  await mail.close();
  await gb.close();
})().catch(e => { console.error(e.message); process.exit(1); });
