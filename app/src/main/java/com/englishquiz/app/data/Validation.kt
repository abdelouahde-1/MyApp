package com.englishquiz.app.data

object Validation {
    /** Returns a list of human-readable problems; empty means the lesson is valid. */
    fun problems(lesson: Lesson): List<String> {
        val out = mutableListOf<String>()
        if (lesson.title.isBlank()) out += "The lesson needs a title."
        if (lesson.questions.isEmpty()) out += "Add at least one question."
        lesson.questions.forEachIndexed { i, q ->
            val n = i + 1
            if (q.prompt.isBlank()) out += "Question $n has no text."
            when (q.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    val filled = q.options.count { it.isNotBlank() }
                    if (filled < 2) out += "Question $n needs at least 2 options."
                    if (q.options.any { it.isBlank() }) out += "Question $n has an empty option."
                    if (q.correctIndex !in q.options.indices) out += "Question $n: choose the correct option."
                }
                QuestionType.TRUE_FALSE ->
                    if (q.correctIndex !in 0..1) out += "Question $n: choose True or False."
                QuestionType.FILL_BLANK ->
                    if (q.acceptedAnswers.none { it.isNotBlank() }) out += "Question $n needs at least one accepted answer."
            }
        }
        return out
    }
}
