# Assets de loja

Tudo o que o Play Console pede para a ficha do Muslim Plus, gerado por nós
(nenhum asset de terceiros).

| Arquivo | Uso |
|---|---|
| `icon-512.png` | ícone da loja, 512×512, PNG 24 bits |
| `feature-graphic.png` | feature graphic neutro, 1024×500 |
| `feature-graphic-pt.png` / `feature-graphic-en.png` | mesma arte com legenda no idioma da ficha |
| `listing-pt.md` / `listing-en.md` | textos dos campos da ficha |
| `screenshots/pt`, `screenshots/en` | 4 telas por idioma, 1080×1920 |
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

## Regenerar as screenshots

1. Instalar o build de release no emulador/aparelho.
2. `adb shell wm size 1080x1920` para a captura sair em 16:9 (o Play recusa
   proporção acima de 2:1; o Pixel 6 é 20:9).
3. Idioma em **Mais → Idioma**; camadas do Alcorão em **Mais → Exibição do
   Alcorão** (árabe, transliteração e a tradução do idioma da ficha).
4. Em cada tela:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb shell screencap -p /sdcard/s.png
& $adb pull /sdcard/s.png docs\store\screenshots\pt\01-hoje.png
```

5. `adb shell wm size reset` no fim.
6. Converter para RGB (o `screencap` sai com alfa; o Play exige PNG 24 bits):

```powershell
python -c "from PIL import Image; import glob; [Image.open(p).convert('RGB').save(p, optimize=True) for p in glob.glob('docs/store/screenshots/*/*.png')]"
```

Ordem das telas: 01 Hoje, 02 Orações, 03 leitor do Alcorão, 04 Qibla (a ordem
na loja é a ordem dos arquivos).

## Conferir

```powershell
python tools/store/check_store.py
```

Valida limites de texto (30 / 80 / 4.000 / 500), dimensões, ausência de canal
alfa e se as screenshots têm conteúdo (não são tela preta ou branca).
