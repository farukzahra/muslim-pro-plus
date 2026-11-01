const { chromium } = require('playwright');
const fs = require('fs');

async function newestCode(browser) {
  const ctx = browser.contexts()[0];
  const mail = await ctx.newPage();
  await mail.goto('https://mail.google.com/mail/u/0/#search/from%3Ainstagram', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await mail.waitForTimeout(6000);
  const rows = mail.locator('tr.zA');
  let code = null;
  if (await rows.count()) {
    console.log('mail0', JSON.stringify((await rows.first().innerText()).replace(/\n/g,' | ').slice(0,160)));
    await rows.first().click();
    await mail.waitForTimeout(5000);
    const text = await mail.locator('body').innerText();
    const m = text.match(/\b(\d{3})\s?(\d{3})\b/) || text.match(/\b(\d{6})\b/);
    if (m) code = m[1] + (m[2] || '');
  }
  await mail.close();
  return code;
}

(async () => {
  const acc = JSON.parse(fs.readFileSync('C:/repo/muslim-pro-plus/build/instagram/new-account.json', 'utf8'));
  const NEW = 'MuslimPlus2026!';
  const gb = await chromium.connectOverCDP('http://127.0.0.1:9222');
  const code = await newestCode(gb);
  await gb.close();
  console.log('code_masked', code ? code.slice(0,2)+'****' : 'none');
  if (!code) throw new Error('sem codigo');

  const ib = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const page = ib.contexts()[0].pages()[0];
  await page.bringToFront();
  const codeInput = page.locator('input[type="text"]').filter({ hasNot: page.locator('[type="checkbox"]') }).first();
  const textInputs = page.locator('input[type="text"]');
  console.log('text inputs', await textInputs.count());
  await textInputs.last().fill(code);
  await page.getByRole('button', { name: 'Continuar' }).first().click();
  await page.waitForTimeout(7000);

  const pw = page.locator('input[type="password"]');
  console.log('password inputs', await pw.count());
  if (await pw.count() >= 3) {
    await pw.nth(0).fill(acc.password);
    await pw.nth(1).fill(NEW);
    await pw.nth(2).fill(NEW);
    await pw.nth(2).press('Enter');
    await page.waitForTimeout(8000);
  }
  console.log('URL', page.url());
  const body = (await page.locator('body').innerText().catch(()=>'')).slice(-500);
  console.log('BODY', JSON.stringify(body));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/password-changed.png', fullPage: true });
  await ib.close();
  fs.writeFileSync('C:/repo/muslim-pro-plus/build/instagram/password-new.txt', NEW);
})().catch(e => { console.error('ERRO', e.message); process.exit(1); });
