package com.farukzahra.muslimplus.quran

/**
 * As duas secoes que a busca da aba Alcorao mostra na mesma lista.
 *
 * [query] carrega o texto digitado para a tela decidir o que esta na frente
 * (card de continuar leitura so aparece com a busca vazia) sem duplicar estado.
 */
data class SearchUiState(
    val query: String = "",
    val surahMatches: List<Surah> = emptyList(),
    val verseMatches: List<Verse> = emptyList(),
    /** A consulta de versos ainda esta no debounce/busca: nao mostrar "nenhum". */
    val versesPending: Boolean = false,
) {
    /** A consulta ja pede busca no texto dos versos (>= [SearchResults.MIN_QUERY]). */
    val searchesVerses: Boolean
        get() = SearchResults.isVerseQuery(query)

    val isEmptyQuery: Boolean
        get() = query.isBlank()
}

/**
 * Regras puras da busca, fora do ViewModel para rodar em JVM:
 * - consulta vazia devolve o catalogo inteiro (comportamento antigo);
 * - consulta de 1 caractere so filtra suras, nunca varre os versos;
 * - a partir de 2 caracteres a secao de versos entra, limitada a
 *   [MAX_VERSES] resultados (o repositorio devolve mais, a tela nao precisa).
 */
object SearchResults {
    const val MIN_QUERY = 2
    const val MAX_VERSES = 30

    /** Mesma normalizacao arabica do texto dos versos: harakat e alef/ya unificados. */
    fun normalize(query: String): String = ArabicText.normalize(query.trim())

    fun isVerseQuery(query: String): Boolean = normalize(query).length >= MIN_QUERY

    fun split(query: String, surahs: List<Surah>, verses: List<Verse>): SearchUiState {
        val term = normalize(query)
        if (term.isEmpty()) return SearchUiState(query = query, surahMatches = surahs)
        val surahMatches = surahs.filter { surah ->
            surah.nameLatin.contains(term, ignoreCase = true) ||
                ArabicText.normalize(surah.nameArabic).contains(term) ||
                surah.id.toString() == term
        }
        val verseMatches = if (term.length < MIN_QUERY) {
            emptyList()
        } else {
            verses.take(MAX_VERSES)
        }
        return SearchUiState(query = query, surahMatches = surahMatches, verseMatches = verseMatches)
    }
}
