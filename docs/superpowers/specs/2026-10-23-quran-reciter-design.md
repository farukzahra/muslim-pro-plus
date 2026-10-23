# Alcorão — seleção de recitador

Data: 23/10/2026. Status: proposta aguardando aprovação do usuário.
Não autoriza implementação nem representa plano de execução.

## Intenção e decisões já dadas

Permitir escolher quem recita o Alcorão. O usuário escolheu esta funcionalidade
e esclareceu: “sempre arabe audio”. Portanto o áudio desta etapa é somente a
recitação árabe; a tradução permanece texto. Não haverá narrador de tradução,
alternância árabe/tradução ou voz sintetizada nesta etapa.

As demais funcionalidades estão em `docs/superpowers/TODO.md` e exigem pergunta
e autorização próprias antes de começar.

## Proposta de experiência

- Adicionar a opção Recitador nas preferências do Alcorão acessíveis pelo Mais.
- Exibir uma lista de recitadores com nome, estilo e qualidade identificáveis,
  seleção atual e busca por nome. Mostrar o recitador escolhido no leitor.
- Manter Alafasy 128 kbps como padrão para quem não alterou a preferência.
- Salvar a escolha entre aberturas do app, independentemente do idioma da UI.
- Ao trocar com áudio tocando, pausar a fila antiga, aplicar a seleção e deixar
  o mesmo verso selecionado. O usuário toca em reproduzir para ouvir a nova voz;
  a troca não inicia áudio sozinha.
- Baixar a sura para a voz selecionada. O indicador de download se refere àquela
  recitação. Sem arquivo local e sem rede, mostrar erro localizado e opção de
  tentar novamente, sem tocar outra voz silenciosamente.
- Manter controles da notificação, reprodução em segundo plano e destaque do
  verso em reprodução. O nome do artista deve acompanhar a seleção real.

Esses comportamentos são propostas do agente, ainda não aprovadas pelo usuário.

## Fonte de áudio — decisão pendente

1. **Recomendação: ampliar o EveryAyah já usado pelo app.** Catálogo revisado e
   embarcado, com IDs estáveis, somente áudio árabe por verso compatível com o
   texto atual em Hafs. Seleção e busca funcionam offline. Não é necessário
   backend ou dependência nova. Catálogo completo não significa aceitar toda
   variante: validar metadados, cobertura e condições de uso de cada entrada.
2. **Alternativa: Al Quran Cloud.** Oferece edições e áudio por verso; exigiria
   adaptar descoberta/metadados e considerar diferença de numeração. Sem
   credenciais nas consultas públicas verificadas, mas é uma integração adicional.

Quran Foundation não é proposta para esta etapa porque a integração oficial
atual da Content API exige backend com segredo; o projeto proíbe backend.
MP3Quran não é a primeira opção para o player atual por trabalhar também com
áudio de sura inteira. Nenhuma fonte foi escolhida definitivamente nesta proposta.

## Restrições de dados e downloads

- Catálogo inicial só inclui recitações árabes por ayah alinhadas ao texto atual.
  Não introduzir outra riwaya com texto incompatível; nem anunciar 114 suras sem
  validar a cobertura. Nomes e variantes não podem depender da ordem do catálogo.
- Identificar arquivos por provedor + recitação + sura + ayah; uma voz nunca
  pode reutilizar o arquivo de outra. Limite global existente de 300 MiB mantido.
- Proposta para legado: reconhecer os downloads atuais como EveryAyah/Alafasy
  128 kbps; preservar esses arquivos para esse recitador e nunca para os demais.
  A estratégia concreta será detalhada no plano após aprovação.
- Download em andamento usa a recitação capturada no início. Trocar a seleção
  não mistura fontes; seu progresso e conclusão permanecem associados à voz
  original. A UI só marca a seleção atual como baixada se ela estiver completa.
- Seleção indisponível após atualização não deve trocar voz sem aviso: exibir
  estado localizado e pedir nova escolha. IDs desconhecidos não viram URLs livres.
- Rever atribuições e condições de download/uso das gravações antes de habilitar
  entradas. Disponibilidade pública não é licença comprovada de redistribuição.

## Implementação dentro da stack existente

Reutilizar Kotlin, Compose/Material 3, preferências existentes e Media3.
Não adicionar conta, analytics, backend ou locale nesta etapa. Todas as strings
novas entram em en/pt-BR/ar; RTL e acessibilidade precisam cobrir lista e leitor.
Detalhes internos sem impacto no escopo ficam a cargo do plano.

## Critérios de aceite para o futuro plano

1. Escolher entre pelo menos Alafasy e mais um recitador árabe validado; o catálogo
   final depende da validação de conteúdo e condições de uso, sem promessa de contagem.
2. Escolha persiste após recriar o processo e não muda com idioma ou tema.
3. Verso 1:1 de duas vozes gera URLs e arquivos locais distintos; downloads antigos
   só atendem Alafasy. Testar também sura 9, último verso e limites de numeração.
4. Metadados, fila, destaque e download usam a mesma identidade de recitação.
5. Trocar durante reprodução pausa; trocar durante download não mistura arquivos.
6. Offline com a voz baixada funciona; offline sem ela mostra estado recuperável.
7. TDD para catálogo/preferência/cache/fila e E2E Android para seleção, persistência,
   troca e estados offline; rede de testes controlada, sem baixar catálogos completos.
8. Executar testes, lint e assembleDebug, verificar no Pixel6 e revisar versão
   conforme semantic-version antes do commit da implementação.

## Próximo passo

Aprovar esta descrição e escolher a fonte. Depois produzir o plano em
`docs/superpowers/plans/`; só então implementar. Não começar outro item do TODO
ao concluir Q01: perguntar ao usuário qual funcionalidade vamos fazer.
