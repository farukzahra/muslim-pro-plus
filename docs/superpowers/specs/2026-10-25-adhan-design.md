# Chamado (adhan) por oração

Data: 25/10/2026. Aprovado pelo usuário com as decisões abaixo.
Status: implementado em 25/10/2026; ver `plans/2026-10-25-adhan.md`.

## Decisões

- **Orações**: Fajr, Dhuhr, Asr, Maghrib e Isha. Checkbox individual; todas
  ligadas por padrão, **menos Fajr**, que começa desligada. Nascer do sol não é
  oração e nunca toca.
- **Áudio**: duas gravações embarcadas, com licença verificada, e opção de
  trocar. Uma CC0 e uma em domínio público, ambas do Wikimedia Commons.
- **Silencioso**: padrão **não respeitar** o modo silencioso (toca no volume de
  alarme). Opção "respeitar modo silencioso" nas configurações, desligada por
  padrão.
- **Mecanismo**: o alarme exato já existente inicia um serviço em primeiro plano
  com Media3 (`mediaPlayback`). A notificação atual continua aparecendo, agora
  com ação "Parar" quando o áudio está tocando.
- Sem backend, conta, analytics ou download de áudio pela rede.

## Comportamento

1. Ao disparar o alarme de uma oração, o app mostra a notificação como hoje.
2. Se o adhan daquela oração estiver ligado (e o modo silencioso permitir), o
   serviço toca a gravação escolhida uma vez e para sozinho no fim.
3. "Parar" na notificação encerra o áudio imediatamente.
4. Trocar a voz ou as orações vale a partir do próximo alarme; não interrompe
   um adhan já tocando (a mudança de voz é aplicada na próxima abertura do app).
5. O som usa o volume de alarme; o usuário pode silenciar pelo volume do
   aparelho.

## Critérios de aceite

- Fajr desligado e as outras quatro ligadas na primeira abertura.
- Desligar uma oração impede o áudio dela, sem afetar a notificação.
- "Respeitar silencioso" ligado bloqueia o áudio em silencioso/vibrar e mantém a
  notificação.
- Trocar a gravação persiste e a próxima oração usa a nova.
- Testes JVM da política, teste instrumentado da tela e verificação no Pixel6.
- Atribuição das gravações na tela de Licenças.
