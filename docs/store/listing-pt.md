# Ficha da Play Store — pt-BR

Textos prontos para o Play Console, na ordem dos campos. Os limites (título 30,
curta 80, completa 4.000, novidades 500) são conferidos por
`python tools/store/check_store.py`.

## Título (até 30)

Orações, Qibla e Alcorão

## Descrição curta (até 80)

Horários de oração, Qibla e Alcorão offline. Sem conta e sem anúncios.

## Descrição completa (até 4.000)

Horários de oração calculados no seu aparelho, Alcorão completo para ler e
ouvir, e Qibla por bússola. Sem conta, sem anúncios e sem rastreamento: o
essencial funciona em modo avião.

HORÁRIOS DE ORAÇÃO
- Cálculo no próprio aparelho, sem depender de servidor: 12 métodos (MWL, ISNA,
  Egito, Karachi, Makkah, Dubai, Moonsighting Committee, Turquia, Teerã, Kuwait,
  Catar e Singapura).
- São 3.000 cidades com coordenadas e fuso próprio: o horário segue a cidade, não
  o fuso do aparelho. Escolha a cidade à mão ou use a localização.
- Ajuste de −30 a +30 minutos em cada oração.
- Latitude alta tratada pela regra do ângulo do crepúsculo.
- Aba Hoje com contador para a próxima oração e a lista do dia; a tabela do mês
  inteiro fica na aba Orações.

CHAMADO (ADHAN)
- Um lembrete em cada horário de oração, com 22 vozes de adhan e uma gravação
  própria para o Fajr.
- Alarme completo toca mesmo no modo silencioso, no volume de alarme.
- Aviso antes do adhan, de 5 a 30 minutos.
- Se o Android não permitir alarme exato, o app avisa que o lembrete pode
  atrasar alguns minutos.

RASTREADOR DE ORAÇÕES
- Marque cada oração rezada e acompanhe o anel do dia.
- Semana visível no cartão e sequência de dias seguidos.
- O sino de cada linha abre os ajustes daquela oração.

QIBLA
- Bússola com sensor de rotação e correção de declinação magnética.
- Graus, distância até a Caaba e ajuda de calibração em figura-8.
- Sem bússola no aparelho, o app mostra o ângulo a partir do norte verdadeiro e
  explica o motivo.

ALCORÃO
- 114 suras e 6.236 versos com o texto uthmani embarcado, sem download.
- Camadas ligáveis: árabe, transliteração, tradução em português (Helmi Nasr) e
  em inglês (Pickthall).
- Busca por sura e por versículo, marcadores e "continuar de onde parei".
- Áudio por versículo (Mishary Alafasy) com controles na tela de bloqueio. Dá
  para ouvir por stream ou baixar a sura e escutar sem internet.
- Ajuste de tema, tamanho do texto e modo página.

PRIVACIDADE
- Sem cadastro, login ou conta. Nada é enviado para servidor nosso.
- Nenhum SDK de anúncios, de analytics ou de rastreamento.
- A localização serve apenas para achar a cidade mais próxima dos horários e da
  Qibla. Ela não sai do aparelho.
- Preferências, marcadores e áudios baixados ficam no aparelho e somem quando
  você desinstala o app.

IDIOMAS E APARÊNCIA
- Português (Brasil), inglês e árabe. O árabe usa layout da direita para a
  esquerda.
- Tema escuro, claro ou o do sistema.

CRÉDITOS
Texto do Alcorão: projeto Tanzil (CC BY 3.0). Catálogo de cidades: GeoNames
(CC BY 4.0). Cálculo dos horários: adhan-java (MIT). Áudio dos versículos:
everyayah.com. As atribuições completas ficam na tela Licenças, dentro do app.

A internet é usada apenas para transmitir o áudio dos versículos.

## O que há de novo (até 500)

Novidades da 1.5.2

• Cartões do Mais todos com a mesma altura
• Texto curto não deixa mais a coluna torta
• Rastreador de orações com sequência de dias

Sempre offline, sem conta e sem anúncios.
