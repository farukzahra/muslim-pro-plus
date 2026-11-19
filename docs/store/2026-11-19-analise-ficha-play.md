# Análise da ficha na Play Store · 19/11/2026

Pedido: acessar o relatório de aquisição (19/10 a 15/11/2026) e dizer o que
melhora a atração da ficha.

## O que foi lido

| Fonte | Situação |
|---|---|
| Ficha pública pt-BR (`play.google.com/store/apps/details?id=com.farukzahra.muslimplus`) | Lida em 19/11/2026 |
| Ficha publicada via API (`androidpublisher` v3, service account) | Lida: en-US e pt-BR, 4 telas em cada |
| Assets em `docs/store/` | Ícone, feature graphic, 8 telas, conferidos por `check_store.py` |
| Data safety publicado | Lido na ficha pública |
| Console: relatório de aquisição (URL enviada) | **Não foi possível**: o login Google recusa navegador automatizado e a service account não tem a permissão de relatórios em massa |

Estado público hoje: **10+ downloads**, **nenhuma avaliação**, 0 reviews na API,
atualizado em 17/11/2026 (v1.5.2), categoria Lifestyle.

## Diagnóstico

O gargalo não é o desenho da ficha. Com 10+ instalações e nenhuma avaliação, a
ficha recebe pouquíssimas visitas; melhorar a ficha sem tráfego muda pouco. Mas
há três coisas que queimam a conversão de quem chega e precisam ser resolvidas
antes de qualquer campanha:

1. **Data safety diz que o app coleta e compartilha "Mensagens"** com terceiros.
   Isso aparece na ficha, logo abaixo da descrição, e contradiz o texto "sem
   rastreamento". Para um app de oração, lê como alerta.
2. **Nenhuma avaliação.** É o maior freio de clique em app religioso: o usuário
   compara com Muslim Pro (milhões de avaliações) e desiste.
3. **As telas não vendem.** São 4 capturas cruas de UI, sem legenda. No tamanho
   de miniatura da loja (526x296) viram quatro retângulos escuros com texto
   ilegível. O primeiro quadro mostra "01:03:10", não o benefício.

Somam-se: ícone com furo transparente (no Play o miolo aparece branco, some o
traço da marca), só 2 idiomas na ficha (o app tem árabe), título gasto com marca
desconhecida, e nota de versão "Nada mais mudou".

## Prioridade

| # | Ação | Impacto | Esforço |
|---|---|---|---|
| 1 | Tirar o "compartilha Mensagens" do Data safety (ver abaixo) | Alto | Baixo |
| 2 | 7 telas com legenda, na ordem de venda | Alto | Médio |
| 3 | Ícone sólido (sem transparência) e feature graphic de benefício | Alto | Baixo |
| 4 | Título e descrição curta com os termos que se busca | Médio | Baixo |
| 5 | Ficha em árabe (app já tem UI em árabe) | Médio | Baixo |
| 6 | Reescrever descrição completa e notas de versão | Médio | Baixo |
| 7 | Primeiras 10 a 20 avaliações (pedido dentro do app) | Alto | Médio |
| 8 | Tráfego com UTM (Instagram/WhatsApp) para o relatório ter o que medir | Alto | Contínuo |

## 1. Data safety: o que está publicado hoje

A ficha mostra:

> Este app pode compartilhar estes tipos de dados com terceiros: **Mensagens**
> Este app pode coletar estes tipos de dados: **Mensagens**

A origem é o formulário "Fale conosco" (`api.web3forms.com`), documentado em
`docs/releases/1.5.1-data-safety.md`: o texto digitado sai do aparelho.

Duas saídas:

- **A. Trocar o formulário por `mailto:`** (abre o Gmail do aparelho). O app deixa
  de enviar qualquer dado a terceiros, o Data safety volta a "não coleta nada" e
  a frase "sem rastreamento" fica verdadeira sem ressalva. É a opção mais
  alinhada ao posicionamento e a que remove o alerta da ficha.
- **B. Manter o Web3Forms** e aceitar o aviso. Nesse caso, a descrição e o
  onboarding precisam dizer com todas as letras que só o texto do formulário
  sai do aparelho, senão o aviso parece contradição.

Decisão pendente do Faruk. Sem ela, o resto da ficha continua com um furo de
confiança.

## 2. Telas

Formato: 1080x1920, PNG 24 bits, fundo `#0a0c10`, título 88 px em `#e9edf3`,
linha de apoio 44 px em `#98a3b3`, destaque `#5ecb8d`. Uma mensagem por quadro,
UI real emoldurada, nada de texto solto sobre a tela inteira.

| Ordem | Título | Apoio | Tela |
|---|---|---|---|
| 1 | Horários de oração no seu fuso, mesmo offline | Cálculo no aparelho, 3.000 cidades | Hoje, com o contador grande |
| 2 | Alcorão completo, com tradução e áudio | 6.236 versos, 114 suras, download por sura | Leitor, árabe + português |
| 3 | Qibla com bússola e distância até a Caaba | Correção de declinação e calibração | Qibla |
| 4 | Lembrete em cada oração, com a voz que você escolher | 22 vozes de adhan e aviso antes do horário | Ajustes de adhan |
| 5 | Acompanhe sua sequência de orações e leituras | Anel do dia, semana, minutos lidos | Hoje com anel |
| 6 | Tabela do mês inteiro, com ajuste por oração | 12 métodos de cálculo | Orações |
| 7 | Sem conta. Sem anúncios. Sem rastreamento. | Nada sai do aparelho | Mais / privacidade |

Regras: as três primeiras precisam se entender sem zoom; a sétima é a que
diferencia do Muslim Pro, que tem anúncios e assinatura.

## 3. Ícone e feature graphic

- **Ícone**: o miolo do crescente é transparente e o Play renderiza sobre branco
  (`og:image` da ficha mostra um disco branco no meio). Em 48 dp o símbolo vira
  um círculo verde com um furo claro. Corrigir: preencher o miolo com o mesmo
  azul-noite, aumentar a estrela em ~10% e conferir em 48, 72 e 512 px.
- **Feature graphic**: hoje é o logo com a linha "Horários de oração · Qibla ·
  Alcorão". Ela não aparece no topo da ficha no celular e é o que o WhatsApp usa
  como prévia (hoje a prévia sai como o ícone com barras brancas). Vale uma arte
  com o benefício principal legível em 1,9:1.
- O `icon-512.png` está em RGBA e o `check_store.py` acusa: o Play exige PNG 24
  bits. Corrigir junto.

## 4. Textos

Título hoje: `Muslim Plus: orações e Alcorão` (30/30). A marca vem primeiro, mas
"Muslim Plus" tem 6 apps homônimos no Play e um deles passa de 1M de downloads.
Quem busca o nome cai no concorrente; quem busca função encontra os dois.

Propostas (limites conferidos):

| Campo | pt-BR | en-US |
|---|---|---|
| Título | `Orações, Qibla e Alcorão` (24) | `Prayer times, Qibla & Quran` (27) |
| Curta | `Horários de oração, Qibla e Alcorão offline. Sem conta e sem anúncios.` (70) | `Prayer times, Qibla and the Quran offline. No account, no ads, no tracking.` (75) |

Descrição completa: inverter a ordem. As duas primeiras linhas aparecem antes do
"ler mais" e hoje são uma frase genérica de três linhas. Proposta de abertura:

> Horários de oração calculados no seu aparelho, Alcorão completo para ler e
> ouvir, e Qibla por bússola. Sem conta, sem anúncios, sem rastreamento, e o
> essencial funciona em modo avião.

Depois: blocos curtos por função, com os termos que as pessoas digitam
("athan", "hora da oração", "salat", "Ramadã", "Alcorão em português",
"tradução", "offline") dentro de frases naturais. Métodos de cálculo, créditos e
detalhes técnicos no fim, não na abertura.

Notas de versão: parar de escrever "Nada mais mudou". Duas linhas concretas do
que a pessoa ganha, como já é feito nas versões 1.2.0 e 1.4.0.

## 5. Idioma

O app tem UI em árabe, mas a ficha só existe em en-US e pt-BR. Quem lê em árabe
vê a ficha em inglês. Criar a ficha `ar` é alcance novo sem tocar no código.
Proposta de título: `أوقات الصلاة والقرآن الكريم` (27).

## 6. Medição

Desde julho de 2026 a Play mede **cliques únicos em Instalar** e **CTR**, não
mais conversão de aquisição. O relatório da URL enviada, para 19/10 a 15/11, tem
três números que importam:

1. Visitantes da ficha (por fonte: busca, explorar, anúncios e indicações).
2. Cliques em Instalar.
3. CTR contra a mediana dos pares no mesmo idioma e país.

Com a base atual, espere números pequenos e ruidosos. Experimento de ficha
(A/B de telas) só passa a dar sinal acima de ~1.000 visitantes por mês.

Para o relatório ter atribuição: usar link com UTM
(`https://play.google.com/store/apps/details?id=com.farukzahra.muslimplus&utm_source=instagram&utm_medium=bio`)
na bio do Instagram e nos posts. A Play mostra UTM source e campanha quando a
fonte é anúncio ou indicação.

Para liberar a leitura automática desses números no futuro: em **Usuários e
permissões**, a service account `muslim-plus-service-account@...` precisa de
**"Ver informações do app e baixar relatórios em massa"**. Sem isso a API só
devolve a ficha, não as estatísticas.

## 7. Tráfego

A ficha é boa de produto e invisível de distribuição. Com 10+ instalações, o
trabalho de curto prazo é levar gente até ela:

- Instagram `@muslimplus.br`: reels curtos de 15 s (contador de oração, leitor do
  Alcorão, bússola) com link na bio.
- WhatsApp: status e grupos, com o link UTM.
- Comunidades: r/brasil, r/islam, grupos de muçulmanos em Curitiba e São Paulo.
  Post útil, não propaganda.
- Pedido de avaliação dentro do app depois da terceira oração marcada ou da
  primeira sura concluída (In-App Review). Sem isso a ficha continua em 0.

## 8. O que não fazer

- Não repetir palavra-chave em sequência na descrição: a Play rebaixa.
- Não copiar tela, texto ou marca do Muslim Pro: o risco jurídico é maior que o
  ganho.
- Não mudar o pacote. O `com.farukzahra.muslimplus` é permanente.
- Não rodar experimento de ficha enquanto o volume for de dezenas de visitas.

## Pendências para decidir

1. Data safety: remover o formulário (opção A) ou manter e explicar (opção B)?
2. Autorizo gerar as 7 telas com legenda, o ícone corrigido e a feature graphic
   nova em `docs/store/`?
3. Autorizo criar a ficha em árabe?
4. Autorizo trocar título e descrição curta para as versões propostas?

## Feito em 19/11/2026

O Faruk autorizou tudo e o que dependia do repositório foi executado:

| Item | Estado |
|---|---|
| 7 telas novas por idioma, com legenda | Feito. Capturadas no Pixel 6 (1.5.2) e compostas por `tools/store/make_screenshots.py` |
| Ícone sem transparência | Feito. `make_store_assets.py` regerou em RGB; miolo azul-noite |
| Título e descrição curta | Feito. `Orações, Qibla e Alcorão` (24) e `Prayer times, Qibla & Quran` (27) |
| Descrição completa | Reescrita com abertura de benefício e blocos por função |
| Notas de versão | Trocado "Nada mais mudou" por três linhas concretas |
| Ficha em árabe | Criada (`listing-ar.md`, 27/69/2.152) sem imagens próprias |
| Publicação | `publish_listing.py --apply` commitado em 19/11/2026 nos três idiomas |
| Contato/privacidade | Site único `https://muslimplus.com.br/` |

Continua pendente, porque depende do Console ou de decisão de produto:

1. **Data safety**: o aviso "coleta e compartilha Mensagens" segue publicado. A
   remoção do formulário de contato é mudança de app (spec + versão nova), então
   não foi feita aqui.
2. **Primeiras avaliações**: precisa pedido dentro do app (In-App Review) ou
   pedido direto a quem já usa.
3. **Relatório de aquisição**: liberar para a service account a permissão "Ver
   informações do app e baixar relatórios em massa" em Usuários e permissões.
4. **Tráfego com UTM** no Instagram e no WhatsApp.

Bug encontrado durante as capturas: com 3 de 5 orações marcadas, o cartão do
rastreador continua exibindo "Marque uma oração para começar" como legenda. Em
5/5 a legenda vira "Sequência de 1 dia". Vale um teste de UI para o texto do
meio.

