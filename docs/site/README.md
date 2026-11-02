# Site — muslimplus.com.br

Site estático do app, publicado na VPS `92.112.177.249` (Hostinger, São Paulo).

## Onde está

| Item | Caminho |
| --- | --- |
| Fonte no repo | `site/` |
| Destino na VPS | `/opt/muslimplus/` |
| Caddy | `/etc/caddy/Caddyfile` |
| Backup do site antigo | `/opt/muslimplus/index.2026-11-01.html` |

## Domínios

- `https://muslimplus.com.br/` — site (HTTP 200, certificado Let's Encrypt).
- `https://www.muslimplus.com.br/` — redireciona 301 para o apex.
- `https://muslimplus.faruk.dev.br/` — o mesmo site, mantido como espelho.

Páginas verificadas em 02/11/2026: home, `/privacidade/`, `/privacy/`,
`/ar/privacy/`, `robots.txt`, `sitemap.xml`, `img/today.png` e `assets/site.css`,
todas 200.

## DNS

Registro.br, conta `FAZAH3`. Zona de `muslimplus.com.br`:

- `A @ → 92.112.177.249`
- `A www → 92.112.177.249`

A transição de nameservers do Registro.br terminou e os certificados do apex e do
www foram emitidos pelo Caddy. O www é um redirect permanente para o apex.

## Atualizar

```powershell
$key = "C:\repo\secrets\vps\ssh\github-actions-vps-shared"
scp -i $key -r site\index.html site\assets site\img site\robots.txt site\sitemap.xml `
  root@92.112.177.249:/opt/muslimplus/
```

As páginas de privacidade (`/privacidade/`, `/privacy/`, `/ar/privacy/`) e o
`/fgs-demo.mp4` continuam as antigas e não são tocados pelo upload acima.

## Conteúdo

- Hero, recursos, telas reais do app, privacidade, FAQ e CTA para o Google Play.
- Sem cookies, sem analytics, sem dependências externas.
- Imagens em `site/img/`: ícone, print do Hoje, leitor, Qibla e feature graphic.

## Próxima versão do app

O `PRIVACY_URL` do app aponta para `https://muslimplus.com.br/privacy/` desde
02/11/2026. O endereço antigo (`muslimplus.faruk.dev.br`) continua respondendo
como espelho, mas não é mais usado no app nem na ficha da loja.
