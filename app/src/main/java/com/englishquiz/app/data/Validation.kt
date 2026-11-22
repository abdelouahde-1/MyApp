package com.englishquiz.app.data

object Validation {
    /** Returns a list of human-readable problems; empty means the unit is valid. */
    fun problems(unit: StudyUnit): List<String> {
        val out = mutableListOf<String>()
        if (unit.title.isBlank()) out += "The unit needs a title."
        if (unit.totalQuestions == 0) out += "Add at least one question."
        unit.sections.forEach { s ->
            s.questions.forEachIndexed { i, q ->
                val n = "${s.skill.label} question ${i + 1}"
                if (q.prompt.isBlank()) out += "$n has no text."
                when (q.type) {
                    QuestionType.MULTIPLE_CHOICE -> {
                        if (q.options.size < 2) out += "$n needs at least 2 options."
                        if (q.options.any { it.isBlank() }) out += "$n has an empty option."
                        if (q.correctIndex !in q.options.indices) out += "$n: choose the correct option."
                    }
                    QuestionType.TRUE_FALSE ->
                        if (q.correctIndex !in 0..1) out += "$n: choose True or False."
                    QuestionType.FILL_BLANK ->
                        if (q.acceptedAnswers.none { it.isNotBlank() }) out += "$n needs at least one accepted answer."
                }
            }
        }
        return out
    }
}
