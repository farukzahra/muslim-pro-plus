# Ficha da Play Store — pt-BR

Textos prontos para o Play Console, na ordem dos campos. Os limites (título 30,
curta 80, completa 4.000, novidades 500) são conferidos por
`python tools/store/check_store.py`.

## Título (até 30)

Muslim Plus: orações e Alcorão

## Descrição curta (até 80)

Horários de oração, Qibla e Alcorão no aparelho. Sem conta e sem anúncios.

## Descrição completa (até 4.000)

Muslim Plus reúne horários de oração, Qibla e o Alcorão em um app que funciona
sem conta, sem anúncios e sem rastreamento. Os horários são calculados no
próprio aparelho e o texto do Alcorão fica embarcado: modo avião não quebra o
essencial.

HORÁRIOS DE ORAÇÃO
- Cálculo local, sem API: 12 métodos (MWL, ISNA, Egito, Karachi, Makkah, Dubai,
  Moonsighting Committee, Turquia, Teerã, Kuwait, Catar e Singapura).
- Ajuste de −30 a +30 minutos em cada oração.
- Latitude alta tratada pela regra do ângulo do crepúsculo.
- Aba Hoje com contador para a próxima oração e a lista do dia; a tabela do mês
  inteiro fica na aba Orações.
- Cidade escolhida à mão ou localização do aparelho. São 3.000 cidades com
  coordenadas e fuso próprio: o horário segue a cidade, não o fuso do aparelho.
- Um lembrete em cada horário de oração. Se o Android não permitir alarme
  exato, o app avisa que o lembrete pode atrasar alguns minutos.

QIBLA
- Bússola com sensor de rotação e correção de declinação magnética.
- Graus, distância até a Caaba e dica de calibração em figura-8.
- Sem bússola no aparelho, o app mostra o ângulo a partir do norte verdadeiro e
  explica o motivo.

ALCORÃO
- 114 suras e 6.236 versos com o texto uthmani offline.
- Camadas ligáveis: árabe, transliteração, tradução em português (Helmi Nasr) e
  em inglês (Pickthall).
- Busca por sura e por versículo, marcadores e "continuar de onde parei".
- Áudio por versículo (Mishary Alafasy) com controles na tela de bloqueio. Dá
  para ouvir por stream ou baixar a sura e escutar sem internet.

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

Primeira versão (1.0.0).

- Horários de oração calculados no aparelho, 12 métodos e ajuste por oração.
- Qibla com bússola, declinação magnética e distância até a Caaba.
- Alcorão com árabe, transliteração e tradução em português e inglês.
- Busca, marcadores, última leitura e áudio por versículo.
- Português, inglês e árabe. Sem conta, sem anúncios, sem rastreamento.
