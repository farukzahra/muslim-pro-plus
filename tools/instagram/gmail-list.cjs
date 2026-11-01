const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9222');
  const ctx = browser.contexts()[0];
  const mail = await ctx.newPage();
  await mail.goto('https://mail.google.com/mail/u/0/#search/from%3Ainstagram', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await mail.waitForTimeout(6000);
  const rows = mail.locator('tr.zA');
  console.log('rows', await rows.count());
  for (let i = 0; i < Math.min(6, await rows.count()); i++) {
    console.log(i, JSON.stringify((await rows.nth(i).innerText()).replace(/\n/g, ' | ').slice(0, 200)));
  }
  await mail.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/gmail-list.png', fullPage: true });
  await mail.close();
  await browser.close();
})().catch(e => { console.error(e.message); process.exit(1); });
