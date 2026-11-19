# Assets de loja

Tudo o que o Play Console pede para a ficha do Muslim Plus, gerado por nós
(nenhum asset de terceiros).

| Arquivo | Uso |
|---|---|
| `icon-512.png` | ícone da loja, 512×512, PNG 24 bits |
| `feature-graphic.png` | feature graphic neutro, 1024×500 |
| `feature-graphic-pt.png` / `feature-graphic-en.png` | mesma arte com legenda no idioma da ficha |
| `listing-pt.md` / `listing-en.md` / `listing-ar.md` | textos dos campos da ficha |
| `screenshots/src/{pt,en}` | capturas cruas do app, 1080×1920, entrada do gerador |
| `screenshots/{pt,en}` | 7 telas com legenda, 1080×1920, o que sobe para a loja |
| `checklist.md` | passo a passo do Play Console com os dados reais do app |

## Regenerar a arte

```powershell
python tools/store/make_store_assets.py
```

As formas vêm do mesmo vetor do ícone adaptativo
(`app/src/main/res/drawable/ic_launcher.xml`): crescente esmeralda `#2FB98A`,
estrela dourada `#D9B25F`, fundo azul-noite `#08131C`. O script usa Pillow com
supersampling 4× e reduz com média de área (BOX) — LANCZOS deixava ringing
escuro nas bordas do crescente.

O ícone sai em RGB (sem canal alfa). Miolo transparente faz o Play renderizar um
disco branco no centro do crescente, que é o primeiro contato visual com o app.

## Regenerar as capturas cruas

1. Suba o emulador **sem som** (`.\scripts\start-emulator.ps1`): o adhan toca
   durante a captura se o áudio estiver ligado.
2. Instale o build (`.\scripts\install-local.ps1` ou `adb install -r`).
3. `adb shell wm size 1080x1920` (o Play recusa proporção acima de 2:1; o Pixel 6
   é 20:9). `adb shell wm size reset` no fim.
4. Escolha a cidade em **Configurações → Cidade** (São Paulo) e marque as cinco
   orações no rastreador, para o cartão não aparecer zerado.
5. Idioma em **Mais → Idioma**; camadas do Alcorão em **Mais → Exibição do
   Alcorão**.
6. Capture cada tela e salve em `docs/store/screenshots/src/<idioma>/`:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb shell screencap -p /sdcard/s.png
& $adb pull /sdcard/s.png docs\store\screenshots\src\pt\today.png
```

Nomes usados pelo gerador: `today`, `reader`, `qibla`, `tracker`, `adhan`,
`prayers` (tabela do mês), `surahs` (lista de suras, opcional).

## Gerar as telas da loja

```powershell
python tools/store/make_screenshots.py
```

O gerador corta a barra de status e a barra de gestos (hora e ícone de idioma do
sistema não entram na loja), coloca a captura numa moldura arredondada e escreve
o título do quadro em Georgia Bold sobre `#0A0C10`. O sétimo quadro é o painel de
privacidade, sem captura.

Ordem na loja: 01 Hoje, 02 Alcorão, 03 Qibla, 04 rastreador, 05 adhan, 06 tabela
do mês, 07 privacidade. A ordem dos arquivos é a ordem exibida.

## Conferir

```powershell
python tools/store/check_store.py
```

Valida limites de texto (30 / 80 / 4.000 / 500) nos três idiomas, dimensões,
ausência de canal alfa e se as telas têm conteúdo (não são tela preta ou
branca).

## Publicar

```powershell
python tools/store/publish_listing.py           # ensaio
python tools/store/publish_listing.py --apply   # grava no Play
```

Sobe texto dos três idiomas, ícone, feature graphic, as 7 telas de pt-BR e en-US
e as notas de versão de todas as trilhas. A ficha em árabe não tem imagem
própria: o Play mostra as imagens do idioma padrão.

## Idioma padrao da loja

- **Default: ingles (en-US)** — publico internacional.
- Localizacoes adicionais: **pt-BR** e **ar**.
- O app continua seguindo o idioma do aparelho; a ficha e' que tem default en.
