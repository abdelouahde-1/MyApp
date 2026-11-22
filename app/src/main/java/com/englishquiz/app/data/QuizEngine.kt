package com.englishquiz.app.data

import kotlin.random.Random

/** A question prepared for display (multiple-choice options may be shuffled). */
data class QuizItem(
    val question: Question,
    val skill: Skill,
    val options: List<String>,
    val correctIndex: Int,
)

object QuizEngine {

    /**
     * Picks up to [count] random questions of [difficulty] from [unit].
     * [skill] = null means a mixed test: the skills are interleaved so each gets a fair share.
     */
    fun prepare(unit: StudyUnit, skill: Skill?, difficulty: Difficulty, count: Int, random: Random = Random.Default): List<QuizItem> {
        val pools = unit.sections
            .filter { skill == null || it.skill == skill }
            .map { s -> s.questions.filter { it.difficulty == difficulty }.shuffled(random).map { s.skill to it } }
            .filter { it.isNotEmpty() }
            .shuffled(random)
        val picked = mutableListOf<Pair<Skill, Question>>()
        val iters = pools.map { it.iterator() }
        while (picked.size < count && iters.any { it.hasNext() }) {
            for (it in iters) if (it.hasNext() && picked.size < count) picked += it.next()
        }
        return picked.shuffled(random).map { (sk, q) -> toItem(q, sk, random) }
    }

    fun toItem(q: Question, skill: Skill, random: Random = Random.Default): QuizItem = when (q.type) {
        QuestionType.MULTIPLE_CHOICE -> {
            val order = q.options.indices.shuffled(random)
            QuizItem(q, skill, order.map { q.options[it] }, order.indexOf(q.correctIndex))
        }
        QuestionType.TRUE_FALSE -> QuizItem(q, skill, Question.TRUE_FALSE_OPTIONS, q.correctIndex)
        QuestionType.FILL_BLANK -> QuizItem(q, skill, emptyList(), -1)
    }

    /** [selected] is used for choice questions, [typed] for fill-in-the-blank. */
    fun grade(item: QuizItem, selected: Int?, typed: String): AnswerRecord {
        val q = item.question
        return if (q.type == QuestionType.FILL_BLANK) {
            AnswerRecord(
                prompt = q.prompt,
                givenAnswer = typed.trim(),
                correctAnswer = q.acceptedAnswers.joinToString(" / "),
                correct = AnswerChecker.isFillBlankCorrect(typed, q.acceptedAnswers),
                explanation = q.explanation,
                skill = item.skill,
            )
        } else {
            AnswerRecord(
                prompt = q.prompt,
                givenAnswer = selected?.let { item.options.getOrNull(it) }.orEmpty(),
                correctAnswer = item.options.getOrNull(item.correctIndex).orEmpty(),
                correct = selected != null && selected == item.correctIndex,
                explanation = q.explanation,
                skill = item.skill,
            )
        }
    }
}
