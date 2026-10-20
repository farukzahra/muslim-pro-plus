# muslim-pro-plus

APK do Muslim Pro baixado, decompilado e rodando no emulador Android local.

## Estado atual

| Item | Valor |
|---|---|
| Pacote | `com.bitsmedia.android.muslimpro` |
| Versao | `12.6.10` (versionCode `12061001`) |
| minSdk / targetSdk | 19 / 30 |
| Classes DEX | 20.524 |
| Recursos | 3.463 |
| Assinatura | `CN=Bitsmedia Pte Ltd, O=Bitsmedia Pte Ltd, L=Singapore` |
| SHA-256 do APK | `ccad4cd7bc5584705701fd58f1d86bd96664532d6507e347ff1cc1e41a08533b` |
| MD5 do APK | `1643a865a9c2f70eb4233945070defdd` |

Instalado e em execucao no emulador `Pixel6` (Android 14, `emulator-5554`):
package `versionName=12.6.10`, PID ativo, `topResumedActivity` do app.

## Estrutura

```
apk/muslimpro-12.6.10.apk          APK baixado (31.6 MB)
decompiled/muslimpro-12.6.10/
  sources/                         20.212 arquivos .java (JADX 1.5.6)
  resources/                       recursos crus
evidence/
  01-inspect-package.json          Evidencia REA (inspect-android-package)
  AndroidManifest.decoded.xml      Manifest legivel
  screen-01.png                    tela do app no emulador
  ui-01.xml                        dump de UI (onboarding)
tools/jadx/                        JADX CLI 1.5.6
tools/jadx-headless-mcp-0.7.1-all.jar   provider JADX exigido pelo REA
```

## Patches aplicados

APK final: `build/muslimplus.apk` (assinado com debug keystore, precisa
desinstalar o original antes).

| Patch | Arquivo | O que faz |
|---|---|---|
| Texto do botao | `resources.arsc` | `Pular` -> `Agora nao` (so pt-BR; ingles segue `Skip`) |
| Marca | `resources.arsc` | `Muslim Pro` -> `Muslim Plus`: 1.020 strings, 25 URLs preservadas |
| Anuncios | `classes2.dex` | `com.google.android.gms.ads.MobileAds.initialize(...)` virou `return-void` |
| Avisos de premium | `classes.dex` | `premium_show_interval_days`, `show_premium_text_on_timeline_button` e a abertura da pagina premium viram no-op |
| Icone de premium | menu `aou` (XML binario) | icone e titulo do item `timelinePremium` zerados |

### Marca

`tools/patch_brand.py` troca `Muslim Pro`, `MuslimPro`, `muslimpro`, `#muslimpro`
e os nomes localizados em arabe (`مسلم برو`) e bengali (`মুসলিম প্রো`).
URLs, dominios e caminhos de API ficam intactos para nao quebrar `api2`,
`download` e deep links. O nome do pacote e das classes continua
`com.bitsmedia.android.muslimpro` (rebrand profundo quebraria updates).

### Premium

Tres patches de dex, todos em `classes.dex`:

| Classe | Metodo | Efeito |
|---|---|---|
| `Lo/setUserIdentifier;` | `RatingCompat(I)V` | intervalo do popup premium fica 0 |
| `Lo/setUserIdentifier;` | `getLastCustomNonConfigurationInstance(Z)V` | texto "Premium" do botao nunca liga |
| `Lo/PlaylistDTO$Companion;` | `peekAvailableContext()V` | toque no icone nao abre a pagina premium |

O icone em si nao pode ser escondido por codigo: ele e um item de menu
(`res` -> `aou`) sem `android:visible`. Zerar `android:id` **quebra** o app
(dialog em thread de IO, `Can't create handler ... Looper.prepare()`);
zerar apenas icone e titulo e seguro e deixa o slot vazio.


Por que o patch de dex resolve: o wrapper de anuncios do app
(`o.getWrappingSdk`) so cria `AdView`, intersticial e app-open ad dentro do
callback de `MobileAds.initialize`. Sem o callback, o flag `IconCompatParcelizer`
nunca vira true e nenhum `AdView` e criado nem carregado.

Caminhos de anuncio cobertos:

- AdMob banners (timeline, prayers, quran, qibla, more): mortos.
- AdMob intersticial e app-open: mortos (dependem do mesmo callback).
- AppLovin MAX e Facebook Audience Network: so entram por mediacao do AdMob,
  entao tambem nao carregam. `o.adOpenedFullscreen` e apenas o **debugger** do
  MAX (`test_mode_*`), nao anuncia para o usuario.
- Nao foi preciso mexer na chave do AppLovin: trocar a chave do manifest quebra
  o fluxo de consentimento e o app crasha com `Can't create handler inside
  thread ... that has not called Looper.prepare()`.

### Ferramentas em `tools/`

| Script | Uso |
|---|---|
| `patch_arsc.py` | troca string no pool UTF-8 do `resources.arsc`, rebuild do pool |
| `patch_manifest.py` | troca string de mesmo tamanho no manifest binario |
| `repack.py` | remonta o APK trocando entradas, mantendo compressao e ordem |
| `dex_patch.py` | `list` / `noop` de metodo em DEX (reescreve code item em `return-void`), com filtro de assinatura |
| `arsc_probe.py` | confere strings e offsets do `resources.arsc` |
| `patch_brand.py` | rebrand em massa no `resources.arsc`, preservando URLs |
| `axml_dump.py` | le XML binario do APK (menus, layouts) |
| `patch_axml_item.py` | zera atributos de um item de menu no XML binario |
| `find_res_id.py` / `find_res_string.py` | acham qual arquivo de recurso referencia um id/string |
| `dex_strings.py` | lista strings do DEX por padrao |
| `ctx.py` | imprime contexto de codigo no fonte do JADX |

O `apktool` nao consegue remontar esse APK: os recursos usam caminho encurtado
(`res/te`, `res/ma`) e o rebuild falha no `aapt2` e no smali. Por isso o caminho
e patch binario.

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$bt = "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.1.0"

# textos e marca
python tools\patch_arsc.py build\resources.arsc build\resources.patched.arsc "Pular=Agora não"
python tools\patch_brand.py build\resources.patched.arsc build\resources.final.arsc

# anuncios
python tools\dex_patch.py noop build\classes2.dex "Lcom/google/android/gms/ads/MobileAds;" initialize

# avisos de premium
python tools\dex_patch.py noop build\classes.dex "Lo/PlaylistDTO`$Companion;" peekAvailableContext "()V"
python tools\dex_patch.py noop build\classes.dex "Lo/setUserIdentifier;" RatingCompat "(I)V"
python tools\dex_patch.py noop build\classes.dex "Lo/setUserIdentifier;" getLastCustomNonConfigurationInstance "(Z)V"

# icone de premium some
python tools\patch_axml_item.py apk\muslimpro-12.6.10.apk aou 7f0b0832 build\menu.blank.xml

python tools\repack.py apk\muslimpro-12.6.10.apk build\out.apk `
  "resources.arsc=build\resources.final.arsc" `
  "classes.dex=build\classes.dex" `
  "classes2.dex=build\classes2.dex" `
  "aou=build\menu.blank.xml"
& "$bt\zipalign.exe" -p -f 4 build\out.apk build\out-aligned.apk
& "$bt\apksigner.bat" sign --ks "$env:USERPROFILE\.android\debug.keystore" --ks-pass pass:android `
  --key-pass pass:android --ks-key-alias androiddebugkey --out build\muslimplus.apk build\out-aligned.apk
```

## Origem do APK

APKPure e APKMirror respondem 403 (Cloudflare) e o Play Store exige conta.
Fonte usada: API publica do Aptoide (`ws75.aptoide.com`), espelho do app oficial.

```
https://ws75.aptoide.com/api/7/apps/search?query=muslim%20pro&limit=15
```

## Skill usada para decompilar

`reverse-engineer-anything` (REA), de `C:\repo\last-epoch\.agents\skills`.
Para APK: `inspect_android_package`, `search_android_classes`,
`inspect_android_class`, `inspect_android_method`, `trace_android_references`,
`decode_android_resources`.

O servidor MCP do REA precisa de `REA_JADX_MCP_JAR` e `JAVA_HOME` no processo.
Se nao estiverem definidos, usar o CLI:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:REA_JADX_MCP_JAR = "C:\repo\muslim-pro-plus\tools\jadx-headless-mcp-0.7.1-all.jar"

npx -y rea-agents@6.4.0 inspect-android-package "C:\repo\muslim-pro-plus\apk\muslimpro-12.6.10.apk" --format json
npx -y rea-agents@6.4.0 search-android-classes "C:\repo\muslim-pro-plus\apk\muslimpro-12.6.10.apk" Church
```

Requisitos: JDK 17+ (JBR 21 em `C:\Program Files\Android\Android Studio\jbr`) e
`jadx-headless-mcp` 0.7.1 (`1013503897/jadx-headless-mcp`).

## Decompilar de novo

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\tools\jadx\bin\jadx.bat -d decompiled\muslimpro-12.6.10 --no-res --show-bad-code `
  --threads-count 4 apk\muslimpro-12.6.10.apk
```

Resultado esperado: termina com `finished with errors, count: 89` (metodos
`bad-code` de app ofuscado com R8). O source e gerado mesmo assim.

## Rodar no emulador

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
Start-Process "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -ArgumentList "-avd","Pixel6"
& $adb wait-for-device
& $adb install -r apk\muslimpro-12.6.10.apk
& $adb shell am start -n com.bitsmedia.android.muslimpro/com.bitsmedia.android.muslimpro.core.activities.SplashActivity
```

## Observacoes

- O app abre no onboarding ("Sign Up with Muslim Pro"). O nome da activity
  aparece ofuscado (`o.ErrorsJsonAdapter`) porque o R8 renomeou as classes.
- `targetSdk=30` indica que essa build do Aptoide nao e a mesma que o Play
  entrega hoje para aparelhos novos. Se precisar da build exata do Play, e
  preciso uma conta Google com o app instalado (ou um split APK do dispositivo).
- O manifest decodificado foi extraido da evidencia do REA, nao do `apktool`.
