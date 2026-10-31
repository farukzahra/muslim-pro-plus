# Spec — paridade de experiência com o Muslim Pro 17.10.1 (P0)

Data: 31/10/2026
Referência de pesquisa: `docs/research/2026-10-31-muslim-pro-17.10.1.md`
Relatório visual: `build/muslimpro/report/index.html` (fora do git)

## Objetivo

Fechar as seis lacunas P0 identificadas na comparação com o Muslim Pro 17.10.1,
mantendo as regras do produto: offline, sem conta, sem anúncios, sem analytics e
sem backend. Nada de asset, string, layout ou ícone do concorrente.

## Escopo por tarefa

### T1 — Hoje acionável (rastreador de orações)

- Registrar por dia quais das cinco orações obrigatórias foram cumpridas
  (Fajr, Dhuhr, Asr, Maghrib, Isha). Nascer do sol não entra.
- Persistir localmente por data, sem conta; reter 60 dias e podar o resto.
- Na tela Hoje: anel de progresso `n/5`, uma linha por oração com botão de check,
  "marcar todas" quando o dia estiver incompleto, faixa dos últimos sete dias e
  sequência de dias completos.
- Sino por oração na própria linha, ligando/desligando o adhan daquela oração
  (mesma preferência usada pelo agendador).
- Sequência conta dias completos terminando hoje; se hoje ainda está incompleto,
  a sequência de ontem continua valendo.

### T2 — Setup do Alcorão

- Na primeira entrada na aba Alcorão, um wizard de três passos: escrita árabe
  (Uthmani, única disponível; explica que IndoPak exige texto e fonte próprios e
  fica para depois), camadas de tradução (idioma do app, inglês ou só árabe) e
  meta diária em minutos (5, 10, 15 ou 30).
- "Pular" também grava a conclusão; os padrões atuais continuam valendo.
- Meta e conclusão ficam no DataStore; trocar depois é possível pela aba
  Progresso e pelas configurações de exibição.

### T3 — Experiência do leitor

- Configurações em bottom sheet com: tema (Moderno, Papel, Escuro), tamanho do
  texto (quatro passos), modo de leitura (Lista ou Página), manter tela ligada,
  camadas e atalho para áudio e armazenamento.
- Tema aplicado ao leitor: fundo, superfície, texto e cor de destaque próprios,
  sem alterar o tema global do app.
- Modo Página: um verso por página, com anterior/próximo e registro da última
  leitura igual ao modo Lista.
- Mini-player fixo quando houver verso carregado: recitador, anterior,
  play/pause, próximo e velocidade (0,75×, 1×, 1,25×, 1,5×). A velocidade fica
  salva e é aplicada na próxima fila.
- Chip de referência (`sura:verso`) em cada verso.

### T4 — Adhan por oração e lembrete antes

- Manter as nove vozes e o switch por oração; o sino do Hoje usa a mesma
  preferência.
- Lembrete antes do adhan: desligado, 5, 10, 15 ou 30 minutos. Alarme separado do
  adhan, com notificação própria; o adhan continua no horário exato.
- O lembrete nunca toca áudio e nunca aparece depois do horário da oração.

### T5 — Qibla confiável

- Expor a precisão do sensor (alta, média, baixa, não confiável) e avisar quando
  houver interferência magnética.
- Ajuda de calibração em diálogo: figura 8, evitar ímã/capo/carro/notebook,
  conferir se a bússola do sistema está correta.
- Indicar "de frente para Makkah" quando o rumo verdadeiro estiver a até 5° do
  rumo da Caaba. Sem sensor, mantém a explicação atual.

### T6 — Polimento visual e onboarding

- Cartões de 20 dp, chips em pílula e espaçamento consistente; formas no tema.
- Onboarding com cartões de permissão: localização, notificações e privacidade,
  cada um com o motivo e a consequência de negar.
- Estados vazios e de erro continuam dizendo o que fazer; sem banner promocional.

## Fora de escopo

Widgets, Khatam/Juz, notas e itens salvos, Duas/Tasbih/99 nomes, calendário
completo, Zakat, Hajj, conta, Ummah, Qalbox, AiDeen, diretório de mesquitas e
locais halal, Premium e anúncios. IndoPak também fica fora desta rodada.

## Critérios de aceite

1. Toda string nova existe nos três locales (`LocaleParityTest`).
2. Toda regra de domínio nova tem teste JVM.
3. Toda tela alterada tem teste instrumentado (Compose) verde.
4. `gradlew test lint assembleDebug connectedDebugAndroidTest` verde.
5. `scripts/install-local.ps1` instala o build novo com `OK <serial>`.
6. Nenhum asset ou texto do Muslim Pro no repositório.
