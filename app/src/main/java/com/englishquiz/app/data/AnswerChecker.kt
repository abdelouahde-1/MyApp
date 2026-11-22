package com.englishquiz.app.data

object AnswerChecker {

    /** Lower-case, unify apostrophes/quotes, collapse spaces, drop surrounding punctuation. */
    fun normalize(text: String): String =
        text.lowercase()
            .replace('’', '\'')
            .replace('‘', '\'')
            .replace('“', '"')
            .replace('”', '"')
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim('.', ',', '!', '?', ';', ':', '"')
            .trim()

    fun isFillBlankCorrect(given: String, accepted: List<String>): Boolean {
        val g = normalize(given)
        if (g.isEmpty()) return false
        return accepted.any { normalize(it) == g }
    }

    /** Splits teacher input like "went | had gone" into a clean list. */
    fun splitAccepted(raw: String): List<String> =
        raw.split('|').map { it.trim() }.filter { it.isNotEmpty() }
}
