import { chromium } from "playwright";
import fs from "node:fs";

const OUT = "C:/Users/T-GAMER/AppData/Local/Temp/opencode/login";
fs.mkdirSync(OUT, { recursive: true });

const vault = fs.readFileSync("C:/repo/secrets/google/conta.txt", "utf8");
const email = /email=(.+)/.exec(vault)?.[1]?.trim();
const password = /password=(.+)/.exec(vault)?.[1]?.trim();
if (!email || !password) throw new Error("credenciais ausentes no cofre");

const browser = await chromium.connectOverCDP("http://127.0.0.1:9222");
const context = browser.contexts()[0] ?? (await browser.newContext());
const page = context.pages()[0] ?? (await context.newPage());

await page.goto("https://play.google.com/console", { waitUntil: "domcontentloaded" });
await page.waitForTimeout(4000);
console.log("URL inicial:", page.url());

if (page.url().includes("accounts.google.com")) {
  await page.fill('input[type="email"]', email);
  await page.click("#identifierNext");
  await page.waitForTimeout(4000);
  await page.fill('input[type="password"]', password);
  await page.click("#passwordNext");
  await page.waitForTimeout(6000);
  console.log("Após senha:", page.url());
  await page.screenshot({ path: `${OUT}/pos-senha.png`, fullPage: false });
}

console.log("Aguardando 2FA/aprovação (90s)...");
for (let i = 0; i < 18; i++) {
  await page.waitForTimeout(5000);
  if (page.url().includes("play.google.com/console")) break;
}
await page.screenshot({ path: `${OUT}/final.png`, fullPage: false });
console.log("URL final:", page.url());
console.log("Título:", await page.title());
console.log("Página tem 'Muslim Plus'?", (await page.content()).includes("Muslim Plus"));
await browser.close();
