# Funcionalidades pagas do Muslim Pro 17.10.1 — o que observar

Data: 02/11/2026. Pesquisa. Nada aqui é para copiar: nomes de recursos e textos
servem só para comparação e para o comparativo público.

Captura em `emulator-5554`, Muslim Pro 17.10.1, sessão guest, Makkah, trial
expirado. Prints em `build/muslimpro/` (fora do git):

| Arquivo | Conteúdo |
| --- | --- |
| `live-adhan-prayers.png` | lista de orações com ícone de som por linha |
| `live-adhan-sounds.png` | sheet “Maghrib notification settings” |
| `live-adhan-selector-1.png` | lista de sons, topo (Maghrib) |
| `live-adhan-fajr-sounds.png` | lista de sons do Fajr |
| `live-adhan-reminder-options.png` | opções de “Pre-Adhan Reminder” |
| `live-adhan-premium-gate.png` | login/paywall ao tocar um adhan com o trial vencido |

## Pedido

“Pegar as funcionalidades do Muslim Pro versão paga e implementar aqui, vai ser
nosso diferencial […] fazer um comparativo de tabela no Insta falando Muslim Pro
x Plus e quanto paga em um e zero no outro.”

## Como a configuração por oração é feita no Pro

O ícone de alto-falante da linha de cada oração abre uma bottom sheet
(“<oração> notification settings”) com cinco controles:

| Controle | Valores observados |
| --- | --- |
| Notify | liga/desliga o aviso daquela oração |
| Enable alarm for full adhan | alarme completo (toca o adhan inteiro fora do modo silencioso) |
| Repeats | “Everyday” + sete chips de dia da semana |
| Notification sound | abre a tela de escolha de som |
| Pre-Adhan Reminder | None, 15, 20 e 30 minutos |
| Save | aplica |

Três detalhes que importam:

1. Com o sino desligado, o resto da sheet fica desabilitado (todos os nós da
   hierarquia voltam `enabled=false`; só o toggle responde).
2. Sair sem “Save” descarta; a sheet é um formulário, não aplica ao vivo.
3. O aviso “An adhan is required to use alarms” aparece no topo da lista de
   sons quando a oração está sem som de adhan.

## Lista de sons

A tela de som tem duas seções.

**Alerts** (não são adhan): Silent, Default notification sound, Long beep.
No JSON do app são `type=0/1/2`: None, Silent, Beep, Longer beep.

**Adhans**: 33 gravações, agrupadas por país, cada item com nome e país.
Fonte exata: `build/muslimpro/apktool/assets/content/adhans.json`.

| # | Nome | Arquivo | Duração | País |
| --- | --- | --- | --- | --- |
| 1 | Adhan (Madina) | `adhan_madina.mp3` | 1:36 | sa |
| 2 | Adhan Fajr (Madina) | `adhan_fajr_madina.mp3` | 5:19 | sa |
| 3 | Adhan (Madina - 1952) | `adhan_madina_1952.mp3` | 2:52 | sa |
| 4 | Adhan (Nasser Al Qatami) | `adhan_nasser_al_qatami.mp3` | 2:12 | sa |
| 5 | Adhan Fajr (Mansour Zahrani Fajr) | `adhan_fajr_mansour_zahrani.mp3` | 3:04 | ae |
| 6 | Adhan (Abdul Baset) | `adhan_abdul_baset.mp3` | 3:58 | eg |
| 7 | Adhan (Unknown) | `adhan_1.mp3` | 3:57 | |
| 8 | Adhan (Makkah) | `adhan_makkah.mp3` | 3:21 | sa |
| 9 | Adhan (Egypt) | `adhan_egypt.mp3` | 3:02 | eg |
| 10 | Adhan (Halab) | `adhan_halab.mp3` | 3:43 | sy |
| 11 | Adhan (Abdelmajid Salama Sarihi) | `adhan_abdelmajid_salama_sarihi.mp3` | 3:25 | sa |
| 12 | Adhan (Ahmad Alnafes) | `adhan_ahmad_alnafes.mp3` | 3:27 | kw |
| 13 | Adhan (Bosnia) | `adhan_bosnia.mp3` | 3:34 | ba |
| 14 | Adhan (Al Affassi) | `adhan_al-affassi.mp3` | 3:51 | kw |
| 15 | Adhan Fajr (Malek Chebae) | `adhan_fajr_malek_chebae.mp3` | 4:02 | eg |
| 16 | Adhan (Hamad Degheri) | `adhan_hamad_degheri.mp3` | 2:33 | sa |
| 17 | Adhan (Issam Bayan) | `adhan_issam_bayan.mp3` | 2:36 | de |
| 18 | Adhan (Maghreb) | `adhan_maghreb.mp3` | 1:48 | |
| 19 | Adhan (Wadii Hamadi Al Yamani) | `adhan_wadii_hamadi_al_yamani.mp3` | 3:17 | ye |
| 20 | Adhan (Youssef Maati) | `adhan_youssef_maati.mp3` | 4:09 | eg |
| 21 | Adhan (Unknown) | `adhan_yusuf_islam.mp3` | 3:43 | |
| 22 | Adhan (Yusuf Islam) | `adhan_yusuf_islam_2.mp3` | 1:39 | gb |
| 23 | Adhan (Al-Aqsa) | `adhan_al-aqsa.mp3` | 3:45 | ps |
| 24 | Adhan (Fakhrul UNIC) | `adhan_fakhrul_unic.mp3` | 3:25 | my |
| 25 | Adhan (Muzammil) | `adhan_muzammil.mp3` | 4:07 | id |
| 26 | Adhan Fajr (Muzammil) | `adhan_fajr_muzammil.mp3` | 4:50 | id |
| 27 | Adhan Fajr (Salim Bahanan) | `adhan_fajr_salim_bahanan.mp3` | 3:38 | id |
| 28 | Adhan (Salim Bahanan) | `adhan_salim_bahanan.mp3` | 2:48 | id |
| 29 | Adhan Fajr (Syam Elmarusy) | `adhan_ustad_syam_adzan_subuh.mp3` | 4:14 | id |
| 30 | Adhan (Syam Elmarusy) | `adhan_ustad_syam_adzan_1.mp3` | 3:34 | id |
| 31 | Adhan (Mohamed Kalipha) | `@adhan_mohamed_elsyed_khalipha.mp3` | 2:06 | eg |
| 32 | Adhan (Zeeshan Yekin) | `@adhan_mini_muazzin.mp3` | 3:06 | gb |
| 33 | Adhan (Adyan Raziff) | `@adhan_adyan_alhamidi.mp3` | 2:51 | sg |

Campos relevantes do JSON: `flag` (país, duas letras), `duration` em segundos,
`free_with_login_countries` (lista de países onde a voz é liberada com login;
vazio = presa ao Premium) e o prefixo `@` nos três arquivos mais novos.

### Filtro por oração

A lista muda conforme a oração:

- Fajr mostra as 6 gravações de Fajr **e** as comuns.
- As outras orações escondem as gravações de Fajr (tathwib).

Nosso `AdhanSound.fajrOnly` já modela essa distinção.

## Gate de premium

Com o trial vencido, tocar uma voz de adhan abre `LoginSignupActivity`
(“Salam, Create a free account”, benefícios, “Continue with Google”, Email).
Ou seja, o caminho pago começa por **conta obrigatória**, não pela loja.

“Enable alarm for full adhan” não abriu paywall visível no teste, mas o estado
do checkbox não aparece na hierarquia de acessibilidade; confirmar na próxima
captura se é preciso ter conta.

## Preço

Ainda não capturado em BRL. A página do app na Play Store (02/11/2026) informa
“Contém anúncios”, “Compras no app”, 4,3 estrelas, 100 mi+ downloads, sem preço
no HTML. Para o comparativo, tirar o preço da tela de assinatura com a conta
brasileira ou do Play Console antes de publicar qualquer número.

## Comparativo que dá para sustentar hoje

| Critério | Muslim Pro | Muslim Plus |
| --- | --- | --- |
| Preço | assinatura (a confirmar em BRL) | zero |
| Anúncios | sim, e o Premium tira | nenhum |
| Conta | obrigatória para o adhan pago | nenhuma |
| Adhan por oração | sim, com som, alarme completo e repetição por dia | sim, com som, alarme completo e repetição por dia desde a 1.4.0 |
| Lembrete antes da oração | None/15/20/30 por oração | 5/10/15/30 por oração desde a 1.4.0 |
| Alcorão offline | completo no Premium | completo, sempre |
| Qibla | sim | sim, com calibração |
| Coleta de dados | app declara coleta de local e dados pessoais | nenhum backend, sem analytics |

## O que aproveitar no nosso app

1. Sheet por oração no lugar do toggle direto, com os mesmos controles na ordem
   que o usuário já reconhece.
2. Som por oração com fallback para a voz global.
3. Lembrete por oração com fallback para o global.
4. Seletor de som em tela própria, agrupado por país, com prévia e duração,
   como já existe na tela do adhan.
5. Repetição por dia da semana e alarme completo ficam para a fase 2.

## Cuidados

- Nada de áudio, string, bandeira ou layout copiado do Muslim Pro.
- Os números de duração e país são fatos do catálogo deles, usados só para
  comparação; nossas vozes continuam sendo as 22 já embarcadas com licença
  própria.
- O comparativo do Instagram só publica depois que o adhan por oração existir.
