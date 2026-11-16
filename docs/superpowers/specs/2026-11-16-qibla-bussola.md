# Qibla: bússola com o mostrador girando (16/11/2026)

Correção de defeito relatado pelo usuário: com o aparelho virado para Makkah, a
agulha aparecia cerca de 60° (duas horas de relógio) à esquerda do que ele
esperava.

## Diagnóstico

Comparação com o Muslim Pro original (12.6.10, material em `old/`):

- Original: `o/FaqEntry.java::onSensorChanged` gira **o mostrador inteiro** por
  `-azimute` (`RotateAnimation` em `binding.read`) e a seta da Qibla, que é filha
  desse mesmo `FrameLayout` (`apktool_out/unknown/vz`), fica parada no ângulo do
  rumo dentro dele. Resultado na tela: `rumo - azimute` — a seta aponta para
  Makkah no mundo, e a rosa dos ventos acompanha.
- Muslim Plus: o anel (com um ponto vermelho fixo no topo, que parecia "norte")
  não girava; só a agulha girava. Com o aparelho **de frente para Makkah** a
  agulha fica no topo, e quem lê o mostrador como uma bússola de norte para cima
  conclui que a direção está uma hora ou duas "à esquerda". A conta estava
  certa; a leitura era ambígua e o marcador fixo de norte era falso.
- Diferença de sensor: o original monta a matriz com acelerômetro + magnetômetro
  e faz `remapCoordinateSystem(AXIS_X, AXIS_Z)` antes de `getOrientation`, porque
  a bússola é usada com o aparelho em pé. O nosso usava
  `TYPE_ROTATION_VECTOR` + `getOrientation` sem remapear: a decomposição em
  ângulos tem singularidade justamente na vertical (topo do aparelho para cima),
  então o rumo ficava instável exatamente na posição de mirar a Qibla.
- O original também compensa o giro do display (`ROTATION_90` soma 90°); o nosso
  ignorava rotação de tela.

## Correção

1. Rumo calculado por vetores, não por ângulos
   (`QiblaCompass.headingDegrees`): gravidade + campo magnético, com o eixo
   "para frente" sendo o topo da tela com o aparelho deitado e a câmera traseira
   com o aparelho em pé. A troca acontece a 45°, onde os dois eixos apontam para
   o mesmo lado, então a agulha não salta. Sem singularidade em nenhuma posição.
2. Giro do display aplicado ao eixo "para frente" (0/90/180/270).
3. Mostrador gira com o rumo (`-rumo verdadeiro`); a agulha da Caaba e as letras
   N/L/S/O são desenhadas no referencial do mundo. A seta fica no topo quando o
   aparelho aponta para Makkah, e o "N" vai para o lado — a leitura do ângulo
   (ex.: 69°) fica sem ambiguidade. O ponto vermelho fixo no topo foi removido.
4. Sem bússola utilizável o mostrador fica parado com o norte no topo, que é o
   modo "ângulo a partir do norte verdadeiro" já explicado na tela.

## Verificação

- `QiblaCompassTest`: rumo deitado (N/L/S/O), rumo em pé (câmera), inclinação de
  45° sem salto, giro de display 90/180/270, casos degenerados.
- `QiblaCompassRenderTest` (instrumentado, Pixel 6): mede no bitmap renderizado
  que a agulha fica a 69° com o norte no topo, a 0° e o norte a 291° com o
  aparelho virado para Makkah, e a 339° ao virar 90° para a direita.
- No emulador: London, 119°, agulha medida em 117,5° e marcador de norte em
  358,4° com o aparelho na pose padrão (topo quase vertical).
