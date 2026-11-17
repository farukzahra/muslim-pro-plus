# Fale conosco — cartão de sugestão na grade do Mais

Data: 16/11/2026. Status: **aprovado em 16/11/2026 pelo pedido do usuário,
implementado na mesma data, sem publicação**.

## Por que

O app só tem contato escondido dentro de **Sobre**, e é um `mailto:` vazio: abre
o app de e-mail sem assunto nem corpo. Quem quer sugerir algo precisa montar a
mensagem do zero e não sabe o que contar (versão, aparelho, idioma). O pedido do
usuário: um cartão **Fale conosco** ao lado de **Apoie o projeto**, com um campo
onde a pessoa escreve a sugestão e envia para a gente.

## Decisões

1. **Sem backend.** O app não tem conta, servidor nem analytics
   (`AGENTS.md`), e um endpoint novo exigiria hospedagem, LGPD e chave de API.
   O envio é `mailto:` com assunto e corpo prontos, abertos no app de e-mail do
   aparelho. O usuário revisa e envia pelo cliente dele.
2. **Texto simples, não rich text.** Campo `OutlinedTextField` multilinha
   (4 a 8 linhas visíveis). Rich text em Compose exigiria biblioteca externa,
   formato próprio e conversão para HTML no e-mail, sem ganho para uma
   sugestão. O corpo do `mailto:` é texto puro de qualquer forma.
3. **Limite de 2.000 caracteres**, com contador visível. Cabe em qualquer
   cliente de e-mail e evita URI gigante.
4. **Dados técnicos automáticos, avisados na tela.** O corpo leva uma linha
   `-- ` com app + versão + versionCode, Android + modelo e idioma. O diálogo
   diz que isso vai junto, para não ser surpresa.
5. **Fallback quando não há app de e-mail.** `resolveActivity` é consultado
   antes; sem cliente, o diálogo mostra o endereço e um botão **Copiar e-mail**.
   Sem `ActivityNotFoundException` e sem perder a mensagem escrita.
6. **`<queries>` de `SENDTO`/`mailto` no manifesto.** Sem isso o Android 11+
   esconde os clientes de e-mail do `resolveActivity` e o fallback apareceria
   sempre.
7. **Um único endereço.** Reaproveita `CONTACT_EMAIL` de
   `ui/screens/settings/AboutScreen.kt`, que já é o contato do **Sobre**.

## Fluxo

1. Mais → cartão **Fale conosco** (última linha, ao lado de Apoie o projeto).
2. Diálogo: texto de apoio, campo multilinha, contador, aviso dos dados
   técnicos, botões **Cancelar** e **Enviar**.
3. Enviar só habilita com 3 caracteres ou mais (depois de `trim`).
4. Enviar monta o `mailto:` codificado, abre o seletor de apps de e-mail e
   fecha o diálogo.
5. Sem app de e-mail: o diálogo continua aberto, mostra o endereço e o botão
   de copiar.

## Regras puras (`ContactMessage`, testável em JVM)

- `normalize(raw)` — `trim` e `\r\n` → `\n`.
- `isValid(raw)` — texto normalizado com pelo menos 3 caracteres.
- `body(message, header)` — mensagem normalizada + `\n\n-- \n` + linhas do
  cabeçalho; sem cabeçalho, só a mensagem.
- `mailto(email, subject, body)` — percent-encoding UTF-8 próprio, espaço como
  `%20` (não `+`) e quebra de linha como `%0A`, que é o que os clientes de
  e-mail esperam.

## Textos novos (en, pt-BR, ar)

`more_contact`, `more_contact_supporting`, `contact_title`, `contact_body`,
`contact_hint`, `contact_counter`, `contact_details_note`, `contact_send`,
`contact_copy_email`, `contact_copied`, `contact_failed`, `contact_subject`.

## Testes

- JVM `ContactMessageTest`: normalização, validade, corpo com e sem cabeçalho,
  codificação de espaço/acento/quebra de linha, tamanho máximo.
- JVM `LocaleParityTest`: cobre as chaves novas nos três idiomas (existente).
- Instrumentado `ContactDialogTest`:
  - cartão abre o diálogo, contador começa em 0, Enviar desabilitado;
  - digitar habilita Enviar;
  - Cancelar fecha;
  - com `onSend` falso, o diálogo continua aberto, mostra o endereço e o
    botão de copiar;
  - com `onSend` verdadeiro, o diálogo fecha;
  - `contactEmailIntent` sai com `ACTION_SENDTO`, esquema `mailto` e corpo
    codificado.
- Verificação local: `gradlew test lint assembleDebug` e
  `scripts/install-local.ps1` no emulador.

## Envio direto pelo Web3Forms (17/11/2026)

Revisão pedida pelo dono: o `mailto:` obriga a pessoa a ter app de e-mail e
expõe o endereço dela como remetente. O envio passa a ser direto pela API do
Web3Forms, com o `mailto:` virando fallback.

Decisões:

1. **Chave fora do repo.** `WEB3FORMS_ACCESS_KEY` mora em
   `C:\repo\secrets\projects\web3forms.env`. O build lê
   `web3forms.accessKey` do `local.properties` (gitignored) e cai em
   `WEB3FORMS_ACCESS_KEY` no CI; entra em `BuildConfig.WEB3FORMS_ACCESS_KEY`.
   Sem chave o envio devolve `false` e o diálogo oferece o e-mail, então o build
   continua válido em qualquer máquina. A chave é pública por design (vai no
   APK) e deve ser rotacionada se houver spam.
2. **POST** em `https://api.web3forms.com/submit`, JSON em UTF-8, `Accept:
   application/json`, timeouts de 10 s. Sucesso só com HTTP 200 e
   `"success": true` no corpo; qualquer outra coisa vira erro na tela.
3. **Payload** (`ContactRequest.json`): `access_key`, `subject`
   (`Muslim Plus <versão> · sugestão`), `from_name` `Muslim Plus`, `message`
   (texto + assinatura), `app`, `version` (`1.4.8 (18)`), `android`, `device`,
   `language`, `botcheck: false`. JSON escrito à mão, com escape testado: aspas,
   barra invertida, quebra de linha, tab e controles.
4. **Estados** `Idle` / `Sending` / `Error`. Enviando: campo e botão
   desabilitados, rótulo `Enviando…`. Sucesso: fecha o diálogo e abre
   `ContactSentDialog` (`contact_sent_dialog`). Erro: mantém o texto, mostra
   `contact_send_failed`, o botão vira `Tentar de novo` e aparece
   `Usar app de e-mail`.
5. **Fallback em dois níveis.** `Usar app de e-mail` abre o `mailto:`; se não
   houver cliente, o bloco existente com o endereço e `Copiar e-mail` aparece.
6. **Internet.** `android.permission.INTERNET` já estava no manifesto desde o
   áudio do Alcorão; nada a adicionar.
7. **Falha de rede não perde o texto.** O campo nunca é limpo no erro e o
   `rememberSaveable` segura o conteúdo em rotação.

Testes novos: `ContactRequestTest` (6 casos JVM) e, no instrumentado,
`sendingDisablesTheButtonAndSuccessClosesWithConfirmation`,
`errorKeepsTheTextAndOffersRetryAndEmail`,
`emailFallbackThatWorksClosesTheDialog`, com `ContactSender` fake e
`CompletableDeferred` segurando o estado de envio.

## Pendências abertas por esta revisão

O dono pediu para **não alterar textos existentes**. Ficam registrados:

- `contact_body` ainda diz "A mensagem abre no seu app de e-mail para você
  revisar antes de enviar". Com envio direto, a mensagem sai sem abrir o app.
- `about_description` e `privacy_*` continuam dizendo "offline" e "nada é
  enviado". A ação do Fale conosco faz POST para `api.web3forms.com`.
- A política de privacidade publicada (`PRIVACY_URL`) não menciona o Web3Forms
  nem o endereço `farukz@gmail.com` como destino.
- A conta Web3Forms tem limite gratuito mensal; se estourar, só o fallback por
  e-mail funciona.
- O envio real de teste de 17/11/2026 chegou a `farukz@gmail.com`.

## Fora de escopo

- Endpoint HTTP, fila de mensagens, resposta automática.
- Envio de logs, prints ou anexos.
- Rich text, markdown ou emoji picker.
- Publicação no Play ou push: pedido explícito de deixar local.
