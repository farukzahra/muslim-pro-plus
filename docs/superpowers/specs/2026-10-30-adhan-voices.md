# Vozes do chamado (adhan) — 10 gravações

Data: 30/10/2026. Status: **proposta de nomes, aguardando conferência do
usuário**. Nada de áudio novo entrou no app; as strings dos rótulos já estão
nos três idiomas para revisão.

## Por que

O usuário apontou que **Sabah Fakhry era cantor de muwashshah, não muezzin**.
A tela hoje tem duas vozes: uma gravação anônima (CC0) e o Sabah Fakhry. O
pedido é ter **10 chamados diferentes, todos sunitas, de muezzins de verdade**,
com os nomes conferidos antes de entrar no app.

## Parada do áudio (já implementado nesta data)

Pedido: ao tocar o adhan, apertar volume (mais/menos), apertar power/desligar a
tela ou girar o aparelho **para o áudio**.

- `AdhanService` ganhou três gatilhos de parada:
  - broadcast `android.media.VOLUME_CHANGED_ACTION` e
    `android.media.STREAM_MUTE_CHANGED_ACTION` (ações escondidas do
    `AudioManager`);
  - `ContentObserver` em `Settings.System` para `volume_alarm` e `volume_music`
    (rede de segurança: o broadcast escondido nem sempre é entregue);
  - `ACTION_SCREEN_OFF` para o botão de power;
  - `ComponentCallbacks.onConfigurationChanged` para mudança de orientação.
- Texto novo `adhan_stop_triggers` nos três idiomas, abaixo do aviso de volume.
- Testes instrumentados: `volumeChangeStopsPlayback`, `screenOffStopsPlayback`,
  `rotationStopsPlayback` (Pixel 6, 6/6 verdes em 30/10/2026).

Efeito colateral aceito: quem aperta volume para **aumentar** o som também
para o chamado. É o comportamento pedido.

## Lista proposta (10)

| # | Nome | Mesquita | País | Áudio | Licença |
| --- | --- | --- | --- | --- | --- |
| 1 | Sheikh Ali Ahmed Mulla | Masjid al-Haram | Arábia Saudita | sim | Wikimedia Commons, CC BY 3.0 (vídeo) |
| 2 | Sheikh Majid Al-Hakim | Masjid al-Haram | Arábia Saudita | sim | archive.org, marca de domínio público |
| 3 | Sheikh Abdulmalik Al-Mulla | Masjid al-Haram | Arábia Saudita | sim | archive.org, marca de domínio público |
| 4 | Sheikh Essam Bukhari | Masjid an-Nabawi | Arábia Saudita | sim | archive.org, marca de domínio público |
| 5 | Sheikh Faisal Al-Na'man | Masjid an-Nabawi | Arábia Saudita | sim | archive.org, marca de domínio público |
| 6 | Sheikh Naji Qazzaz | Masjid al-Aqsa | Jerusalém | sim | archive.org, marca de domínio público |
| 7 | Sheikh Nasser Al-Qatami | Grande Mesquita do Kuwait | Kuwait | **não achei** | pendente |
| 8 | Sheikh Abdul Baset Abdus Samad | Cairo | Egito | sim | archive.org, marca de domínio público |
| 9 | Istambul (mesquita não identificada) | — | Turquia | sim | Wikimedia Commons, CC0 |
| 10 | Mesquita Hassan II | Casablanca | Marrocos | sim | Wikimedia Commons, CC BY-SA 4.0 |

Rótulos já escritos em `values/`, `values-pt-rBR/` e `values-ar/`
(`adhan_sound_makkah_mulla`, `adhan_sound_makkah_hakim`,
`adhan_sound_makkah_mulla_abdulmalik`, `adhan_sound_madinah_bukhari`,
`adhan_sound_madinah_nafman`, `adhan_sound_aqsa`, `adhan_sound_kuwait`,
`adhan_sound_egypt`, `adhan_sound_istanbul`, `adhan_sound_casablanca`).

## Dúvidas para o usuário decidir

1. **Licença do acervo archive.org.** O pacote "Azaan of World Mosques" declara
   *Public Domain Mark 1.0* pelo uploader, mas são gravações de rádio/TV das
   mesquitas. A marca é auto-declarada e juridicamente fraca. Alternativas:
   (a) usar mesmo assim, com atribuição; (b) só as quatro do Wikimedia
   Commons (vozes 1, 9, 10 — poucas); (c) licenciar com quem detém os direitos.
2. **Identidade das vozes 2, 3, 5 e 6.** O nome vem só do arquivo enviado ao
   archive.org. Confirmar antes de publicar com nome de pessoa.
3. **Abdul Baset (#8) era recitador, não muezzin.** Se a regra é "só muezzin",
   ele sai e entra outro (sugestão: Sheikh Ahmed Younis Khoja, muezzin atual do
   Haram, ou Sheikh Abdulaziz Bukhari, do Nabawi).
4. **Kuwait (#7) não tem áudio livre localizado.** Sem gravação, o rótulo não
   pode existir. Trocar por outra voz ou deixar para depois.
5. **Sabah Fakhry.** Manter a gravação renomeada para "Adhan (Síria, 1985)" ou
   remover de vez?
6. **Tamanho.** 10 gravações de ~2,5 MB somam ~25 MB no APK. Convertendo para
   mono 48 kbps, ~1 MB cada (~10 MB no total). Qual o limite aceitável?
7. **Fajr.** As gravações trazem "as-salatu khayrun min an-nawm"? Se não,
   aceitamos o adhan comum no Fajr ou procuramos uma gravação específica?

## Critérios de aceite (depois da aprovação)

- 10 vozes no seletor, cada uma com prévia e rótulo nos três idiomas.
- Atribuição de cada gravação em Licenças, com URL e licença.
- Teste JVM garantindo 10 vozes, ids únicos e rótulo em todos os locales.
- Teste instrumentado de troca de voz e prévia no Pixel 6.
- `install-local.ps1` com `OK <serial>`.
