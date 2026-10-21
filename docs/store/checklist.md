# Publicação no Play Console — Muslim Plus

Passo a passo com os dados reais deste app. Tudo que está marcado como
**pendente** precisa ser resolvido antes de subir para a trilha interna.

## 1. Dados do app

| Campo | Valor |
|---|---|
| Pacote | `com.farukzahra.muslimplus` (permanente no Play) |
| Nome exibido | Muslim Plus |
| Versão | `versionName` 1.0.0 · `versionCode` 1 |
| Android mínimo | 8.0 (API 26) · `targetSdk` 36 |
| Bundle gerado | `app/build/outputs/bundle/release/app-release.aab`, 12.141.577 bytes (11,58 MB) |
| Categoria | Lifestyle (alternativa: Books & Reference) |
| Preço | Grátis, sem compras no app (nenhuma dependência de billing no build) |
| Idiomas da ficha | pt-BR e en (o app também tem árabe) |
| Conta / login | Não existe |
| Anúncios / analytics | Não existem |

Permissões declaradas no `AndroidManifest.xml`:

| Permissão | Para quê |
|---|---|
| `INTERNET` | só o áudio dos versículos (stream/download do CDN) |
| `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` | achar a cidade mais próxima para horários e Qibla; opcional, com cidade manual como alternativa |
| `POST_NOTIFICATIONS` | lembrete de oração (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | lembrete no horário exato; sem ela o app agenda inexato e avisa na tela |
| `RECEIVE_BOOT_COMPLETED` | reagendar os lembretes depois de reiniciar |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | áudio tocando com a tela apagada |

`uses-feature` de `android.hardware.location` e `...location.gps` está com
`required="false"`: o app instala em aparelho sem GPS.

## 2. Antes de abrir o console

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat test lint bundleRelease
python tools/store/check_store.py
```

- [ ] `test` e `lint` verdes.
- [ ] `app-release.aab` gerado **assinado** com a chave de upload (keystore em
      `..\secrets\android`, apontado pelo `local.properties`; nunca no repo).
      Sem keystore o `bundleRelease` falha de propósito no `checkReleaseSigning`.
- [ ] Conferir a assinatura:

```powershell
& "$env:JAVA_HOME\bin\jarsigner.exe" -verify -verbose -certs app\build\outputs\bundle\release\app-release.aab
```

- [ ] `versionCode` maior que o da versão já publicada (hoje 1; no próximo
      envio, 2).
- [ ] `python tools/store/check_store.py` sem falhas (limites de texto,
      dimensões dos PNG, screenshots não vazias).

## 3. Política de privacidade — **pendente, bloqueia o envio**

O app aponta para `https://muslimplus.faruk.dev.br/privacy/`
(`PRIVACY_URL` em `PrivacyScreen.kt`).

Estado em 2026-10-21: o domínio **não resolve** (NXDOMAIN). O site está em
`C:\repo\muslim-plus-site` e já foi publicado em `/opt/muslimplus` na VPS com
bloco Caddy; falta o registro DNS.

- [ ] Criar o registro A de `muslimplus` no DNS de `faruk.dev.br` apontando
      para a VPS.
- [ ] Conferir que `https://muslimplus.faruk.dev.br/privacy/` responde 200 com o
      texto da política (e as versões `/privacy/en/`, `/privacy/ar/`).
- [ ] Colar a URL em **App content → Privacy policy** e no campo do app.

Sem essa URL pública o Play não deixa concluir o envio.

## 4. Ficha da loja

Textos em `listing-pt.md` e `listing-en.md` (título, descrição curta, descrição
completa, novidades). Idioma padrão sugerido: pt-BR; adicionar en em
**Store listing → Manage translations**.

| Campo do Play | Arquivo | Especificação |
|---|---|---|
| Ícone do app | `icon-512.png` | 512×512, PNG 24 bits (RGB, sem alfa) |
| Feature graphic | `feature-graphic.png` (neutro) ou `feature-graphic-pt.png` / `feature-graphic-en.png` | 1024×500, PNG 24 bits |
| Screenshots pt-BR | `screenshots/pt/01-hoje.png`, `02-oracoes.png`, `03-leitor-alcorao.png`, `04-qibla.png` | 1080×1920, PNG 24 bits |
| Screenshots en | `screenshots/en/01-today.png`, `02-prayers.png`, `03-quran-reader.png`, `04-qibla.png` | 1080×1920, PNG 24 bits |

Regras respeitadas: mínimo de 2 e máximo de 8 screenshots por idioma; lado menor
≥ 320 px e maior ≤ 3840 px; proporção dentro de 2:1; nada de marca de terceiros
nas imagens (as telas são do próprio app, com dados de exemplo do aparelho).

- [ ] Subir os 4 screenshots na ordem acima (Hoje, Orações, Alcorão, Qibla) em
      cada idioma. A ordem define a ordem na loja.
- [ ] Revisar o texto: sem números de downloads/avaliações (não existem) e sem
      promessa de recompensa.

## 5. Data safety

O app **não coleta nem compartilha dados**. Respostas:

| Pergunta | Resposta |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | Não se aplica (nada é coletado) |
| Do you provide a way to request data deletion? | Não se aplica |
| Location | **Não coletado.** O GPS é lido no aparelho para escolher a cidade; a coordenada não é gravada nem transmitida. |
| Personal info, contacts, files, messages, health, financial | Não |
| App activity / analytics | Não existe SDK de analytics |

O que fica gravado **só no aparelho** (não é "coleta" para o Play):

- DataStore `settings`: cidade, método de cálculo, offsets por oração, offset
  hijri, tema, idioma, camadas do Alcorão, notificações ligadas/desligadas.
- DataStore `reader_state`: última leitura e marcadores.
- `filesDir/audio/`: áudio baixado por sura (some ao desinstalar).

Único tráfego de rede: baixar/transmitir o áudio de
`https://everyayah.com/data/Alafasy_128kbps/`. Como toda requisição HTTP, o CDN
vê o IP do aparelho; isso não é um tipo de dado declarado no Data safety, mas
está no texto da política de privacidade.

- [ ] Marcar "No" em coleta/compartilhamento e conferir que a política pública
      cita a localização e o áudio.

## 6. Classificação de conteúdo (IARC)

Categoria: **Utility, Productivity, Communication or Other**.

| Pergunta | Resposta |
|---|---|
| Violência, sangue, medo | Não |
| Sexualidade, nudez | Não |
| Linguagem imprópria | Não |
| Drogas, álcool, tabaco | Não |
| Jogos de azar, apostas | Não |
| Interação entre usuários / conteúdo gerado por usuário | Não (não há conta, chat, comentário ou feed) |
| Compartilha localização do usuário com outros | Não |
| Compra digital | Não |
| Contém anúncios | Não |
| Acesso livre à web | Não (o app abre apenas páginas de licença e a política, via navegador) |

Resultado esperado: **Livre / Everyone / PEGI 3 / USK 0**. Confirmar o
certificado que o questionário devolver.

## 7. Público-alvo e outras declarações

| Tela do console | Resposta |
|---|---|
| Target audience | **13+**. Evita as regras da Families Policy; o app não tem ads nem conteúdo infantil. |
| App access | "All functionality is available without special access" (não há login). |
| Ads | Não contém anúncios. |
| News app | Não. |
| COVID-19 apps | Não. |
| Government apps | Não. |
| Financial features | Não. |
| Health | Não. |
| Foreground service (Android 14) | Declarar o uso **mediaPlayback**: áudio do Alcorão com a tela apagada. |

## 8. Trilha interna

1. **Play Console → Test and release → Testing → Internal testing → Create new
   release.**
2. Aceitar o **Play App Signing** (a chave de upload local continua sendo a que
   assina o AAB; o Google guarda a chave de distribuição).
3. Subir `app/build/outputs/bundle/release/app-release.aab`.
4. Release notes: colar o bloco "O que há de novo" de `listing-pt.md`.
5. **Testers**: criar lista de e-mails (a sua conta + quem for testar) e salvar.
6. **Next → Save → Review release → Start rollout to Internal testing.**
7. Esperar a revisão (normalmente horas). Copiar o link de opt-in
   (`https://play.google.com/apps/internaltest/...`).
8. No aparelho de teste, abrir o link, aceitar e instalar.

Conferir depois de instalar:

- [ ] 5 abas abrem (Hoje, Orações, Alcorão, Qibla, Mais).
- [ ] Horários batem com a cidade escolhida; trocar de cidade e de método muda
      a tabela.
- [ ] Alcorão abre Al-Fatihah com árabe, transliteração e tradução; áudio toca.
- [ ] Qibla mostra rumo e distância.
- [ ] Idioma troca para en e volta para pt-BR.
- [ ] Modo avião: horários, texto do Alcorão e Qibla continuam funcionando.
- [ ] Pre-launch report, em **Test and release → Publishing overview**, sem
      crash nem ANR.

## 9. Produção

- [ ] Promover o bundle aprovado da trilha interna para **Production**.
- [ ] Rollout escalonado (ex.: 20%) e acompanhar o Android vitals.
- [ ] Só aumentar depois de alguns dias sem crash/ANR acima da média.

## 10. Pendências conhecidas

- **DNS da política de privacidade** (item 3) — bloqueia o envio.
- **Termos do everyayah.com**: o app faz stream direto do CDN e guarda cópia
  privada por sura. Se os termos não permitirem o download, desligar o botão
  "Baixar áudio" antes do release (a fonte já é trocável por interface).
- **`old/`**: o repositório com o material de engenharia reversa precisa sair do
  GitHub público antes de divulgar o app.
- **Ficha em árabe**: só depois de revisão nativa, como está no plano.
- **Screenshots**: capturadas do build 0.1.0 que estava instalado no emulador
  (versionCode 1, mesma UI da 1.0.0; nenhuma tela de captura mostra o número da
  versão). Regerar após o próximo build de release.
- **Fundo do ícone**: o launcher usa `#0B1622`; o ícone da loja foi gerado com
  `#08131C`, o azul-noite do tema. Diferença imperceptível, mas existe.
