package com.englishquiz.app.data

import kotlin.random.Random

/** A question prepared for display (multiple-choice options may be shuffled). */
data class QuizItem(
    val question: Question,
    val options: List<String>,
    val correctIndex: Int,
)

object QuizEngine {

    fun prepare(lesson: Lesson, shuffle: Boolean, random: Random = Random.Default): List<QuizItem> {
        val qs = if (shuffle) lesson.questions.shuffled(random) else lesson.questions
        return qs.map { q ->
            when (q.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    if (shuffle) {
                        val order = q.options.indices.shuffled(random)
                        QuizItem(q, order.map { q.options[it] }, order.indexOf(q.correctIndex))
                    } else QuizItem(q, q.options, q.correctIndex)
                }
                QuestionType.TRUE_FALSE -> QuizItem(q, Question.TRUE_FALSE_OPTIONS, q.correctIndex)
                QuestionType.FILL_BLANK -> QuizItem(q, emptyList(), -1)
            }
        }
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
            )
        } else {
            AnswerRecord(
                prompt = q.prompt,
                givenAnswer = selected?.let { item.options.getOrNull(it) }.orEmpty(),
                correctAnswer = item.options.getOrNull(item.correctIndex).orEmpty(),
                correct = selected != null && selected == item.correctIndex,
                explanation = q.explanation,
            )
        }
    }
}
