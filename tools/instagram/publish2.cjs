const { chromium } = require('playwright');
const fs = require('fs');
(async () => {
  const caption = fs.readFileSync('C:/repo/muslim-pro-plus/docs/instagram/caption-1.txt', 'utf8').trim();
  const browser = await chromium.connectOverCDP('http://127.0.0.1:9227');
  const page = browser.contexts()[0].pages()[0];
  await page.bringToFront();
  await page.goto('https://www.instagram.com/', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await page.waitForTimeout(4000);
  for (const t of ['Agora Não', 'Not Now']) {
    const b = page.getByText(t, { exact: true }).first();
    if (await b.count()) { await b.click().catch(()=>{}); await page.waitForTimeout(1000); }
  }
  await page.mouse.click(33, 512);
  await page.waitForTimeout(3000);
  const file = page.locator('input[type="file"]');
  console.log('files', await file.count());
  await file.first().setInputFiles('C:/repo/muslim-pro-plus/docs/instagram/post-2.jpg');
  await page.waitForTimeout(5000);

  for (let i = 0; i < 5; i++) {
    if (await page.getByRole('button', { name: /^Partilhar$|^Compartilhar$|^Share$/ }).count()) break;
    const next = page.getByRole('button', { name: /^Seguinte$|^Avançar$|^Next$/ }).first();
    if (!(await next.count())) { console.log('sem next', i); break; }
    await next.click();
    await page.waitForTimeout(3500);
  }
  const editor = page.locator('div[contenteditable="true"]').first();
  console.log('editor', await editor.count());
  await editor.click();
  await editor.fill(caption);
  await page.waitForTimeout(1500);
  console.log('caption len', (await editor.innerText()).length);
  await page.getByRole('button', { name: 'Partilhar', exact: true }).first().click();
  await page.waitForTimeout(18000);
  console.log('URL', page.url());
  console.log('BODY', JSON.stringify((await page.locator('body').innerText()).slice(-200)));
  await page.goto('https://www.instagram.com/muslimplus.br/', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await page.waitForTimeout(6000);
  const hrefs = await page.locator('a[href*="/p/"]').evaluateAll(els => els.map(e => e.href));
  console.log('POSTS', JSON.stringify([...new Set(hrefs)]));
  console.log('PROFILE', JSON.stringify((await page.locator('body').innerText()).slice(0, 200)));
  await page.screenshot({ path: 'C:/repo/muslim-pro-plus/build/instagram/profile-final.png', fullPage: false });
  await browser.close();
})().catch(e => { console.error('ERRO', e.message); process.exit(1); });
