# Site — muslimplus.com.br

Site estático do app, publicado na VPS `92.112.177.249` (Hostinger, São Paulo).

## Onde está

| Item | Caminho |
| --- | --- |
| Fonte no repo | `site/` |
| Destino na VPS | `/opt/muslimplus/` |
| Caddy | `/etc/caddy/Caddyfile` |
| Backup do site antigo | `/opt/muslimplus/index.2026-11-01.html` |

O Caddy serve a mesma pasta em:

- `https://muslimplus.faruk.dev.br/` (já no ar)
- `https://muslimplus.com.br/` (bloco criado; aguardando a transição de
  nameservers do Registro.br)

## Atualizar

```powershell
$key = "C:\repo\secrets\vps\ssh\github-actions-vps-shared"
scp -i $key -r site\index.html site\assets site\img site\robots.txt site\sitemap.xml `
  root@92.112.177.249:/opt/muslimplus/
```

As páginas de privacidade (`/privacidade/`, `/privacy/`, `/ar/privacy/`) e o
`/fgs-demo.mp4` continuam as antigas e não são tocados pelo upload acima.

## DNS

Registro.br, conta `FAZAH3`. A zona de `muslimplus.com.br` foi configurada com:

- `A @ → 92.112.177.249`
- `A www → 92.112.177.249`

No momento da publicação, o painel avisava que os nameservers do domínio estavam
em transição por cerca de 1h48. O Caddy tenta o certificado a cada 10 minutos e
emite sozinho quando o DNS responder. Para conferir:

```powershell
nslookup muslimplus.com.br 8.8.8.8
curl.exe -I https://muslimplus.com.br/
```

## Conteúdo

- Hero, recursos, telas reais do app, privacidade, FAQ e CTA para o Google Play.
- Sem cookies, sem analytics, sem dependências externas.
- Imagens em `site/img/`: ícone, print do Hoje, leitor, Qibla e feature graphic.
