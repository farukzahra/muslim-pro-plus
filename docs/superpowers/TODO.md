# Muslim Plus — funcionalidades a avaliar

Atualizado em 23/10/2026.

## Como trabalhar esta lista

Pedido do usuário: antes de iniciar qualquer outra funcionalidade desta lista,
perguntar explicitamente se vamos fazê-la e aguardar resposta. Uma conclusão
não autoriza começar o próximo item. Silêncio não é aprovação.

Fluxo por funcionalidade: escolha explícita → brainstorming → spec aprovado em
`specs/` → plano em `plans/` → TDD e E2E Android quando houver UI → verificação
→ versão quando aplicável → commit. Registrar a decisão na linha do item.
Se faltar resposta, cobrar a pergunta pendente na mensagem seguinte.

A confirmação humana é uma decisão contextual: não será simulada por um teste
ou por um campo preenchido pelo agente. Os testes do app não comprovam consentimento.

## Trabalho escolhido agora

| ID | Funcionalidade | Estado e decisão |
| --- | --- | --- |
| Q01 | Selecionar recitador do Alcorão | Implementado em 23/10/2026: vozes, cache LRU, downloads por voz/sura/Alcorão completo, tela de armazenamento; testes e Pixel6 verdes; commit local feito |
| Q02 | Narrador de tradução | Fora da etapa atual: usuário respondeu “sempre arabe audio”; tradução continua em texto. Só retomar após nova pergunta e autorização |

Spec de Q01: [seleção de recitador](specs/2026-10-23-quran-reciter-design.md).

## Demais itens — perguntar antes de começar

Todos estão aguardando decisão, sem autorização de implementação e sem ordem
de prioridade aprovada. Os agrupamentos não são milestones prometidos.

| ID | Funcionalidade | Estado atual no Plus / próximo passo autorizado |
| --- | --- | --- |
| Q03 | Navegação por Juz | Ausente; perguntar se vamos fazer |
| Q04 | Playlists do usuário | Só existe fila interna por sura; perguntar se vamos fazer |
| Q05 | Plano Khatam / metas de conclusão | Ausente; perguntar se vamos fazer |
| Q06 | Repetição de ayah | Ausente; perguntar se vamos fazer |
| Q07 | Continuar automaticamente na próxima sura | Sem preferência equivalente; perguntar se vamos fazer |
| Q08 | Controle de rolagem automática | Sem configuração equivalente; perguntar se vamos fazer |
| Q09 | Manter a tela ligada durante leitura | Sem configuração equivalente; perguntar se vamos fazer |
| Q10 | Tajwid colorido | Ausente; perguntar se vamos fazer |
| Q11 | Escolha de escrita/fonte árabe, como IndoPak | Sem seletor equivalente; perguntar se vamos fazer |
| D01 | Tasbih / contador de dhikr | Ausente; perguntar se vamos fazer |
| D02 | Biblioteca de Duas / súplicas | Ausente; perguntar se vamos fazer |
| D03 | Rastreador de práticas religiosas | Ausente; perguntar se vamos fazer e definir o que rastrear |
| D04 | Calculadora de Zakat | Ausente; perguntar se vamos fazer e validar regras antes de implementar |
| D05 | Shahadah | Ausente; perguntar se vamos fazer |
| D06 | Nomes de Allah | Ausente; perguntar se vamos fazer e definir conteúdo |
| L01 | Mesquitas próximas | Ausente; perguntar se vamos fazer e avaliar fonte de dados |
| L02 | Locais halal | Ausente; perguntar se vamos fazer e avaliar fonte de dados |
| P01 | Guia de Hajj e Umrah | Ausente; perguntar se vamos fazer |
| P02 | Jornada de Hajj | Ausente; perguntar se vamos fazer; pode se sobrepor ao guia |
| C01 | Calendário islâmico completo | Parcial: data hijri e ajuste já existem; perguntar se vamos ampliar |
| C02 | Feriados islâmicos | Ausente; perguntar se vamos fazer |
| C03 | Mensagens de saudação | Ausente; perguntar se vamos fazer |
| C04 | Inspiração | Parcial: verso diário já existe; perguntar se vamos ampliar |

## Itens que exigem avaliação de escopo/evidência

| ID | Item | Condição antes de qualquer trabalho |
| --- | --- | --- |
| X01 | Comunidade | Fora do escopo sem conta/backend; perguntar se o usuário quer reabrir essa restrição |
| X02 | Loja | Fora do escopo atual; perguntar se faz sentido para o produto |
| X03 | Makkah | Título no legado não comprova transmissão ao vivo; esclarecer objetivo e evidência antes de propor feature |
| X04 | Voos | Funcionamento não confirmado no legado; esclarecer utilidade antes de propor feature |
| X05 | Ajuda dedicada | Navegação do legado não confirmada; perguntar se precisamos de ajuda no nosso app |

## Já existentes — não tratar como novos TODOs

Busca no Alcorão, traduções em texto, transliteração, favoritos, continuar
leitura, áudio por verso, download por sura, tema, idiomas, horários de oração,
Qibla e data hijri. Melhorias específicas precisam de escopo próprio.

Implementado em 25/10/2026, fora de novos itens: cartão **Apoie o projeto**
(Pix, cópia por toque) na grade do Mais e **checagem de atualização** pelo Play
In-App Updates (diálogo de versão nova, download em segundo plano e aviso para
reiniciar), com status "Atualizações" no Mais.

## Pedido em aberto — chamado (adhan) por oração

Pedido do usuário em 25/10/2026. **Não iniciar sem spec aprovado.** Perguntas
que mudam o resultado:

1. Quais orações tocam: as cinco (Fajr, Dhuhr, Asr, Maghrib, Isha), com Fajr
   separado? Nascer do sol não é oração, então não entraria.
2. Fonte do áudio: gravação própria/CC0 embarcada, gravação licenciada ou
   apenas o som de notificação atual? Sem asset com licença clara, não há adhan.
3. Comportamento em silêncio/vibrar e volume do adhan (ignorar modo silencioso
   ou respeitar).
4. Substituir a notificação atual ou tocar junto com ela.


Referência de pesquisa: `old/docs/2026-10-23-comparacao-features-recitadores.md`.
O legado serve somente à comparação; implementar com código, assets e textos próprios.
