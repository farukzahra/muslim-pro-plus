# Muslim Plus: regras do R8 no release.
#
# O app nao usa reflexao propria, kotlinx.serialization nem nomes de classe em
# string. Compose, Media3, DataStore, Navigation e Play Core trazem consumer
# rules nos proprios AARs, entao aqui so fica o que o build pede e o que ajuda
# a ler stack trace de producao.

# Numero de linha preservado no stack trace; o nome do arquivo vira "SourceFile"
# para nao expor caminho da maquina de build.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
