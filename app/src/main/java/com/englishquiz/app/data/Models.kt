package com.englishquiz.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class QuestionType { MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK }

/**
 * A single question.
 *  - MULTIPLE_CHOICE / TRUE_FALSE use [options] and [correctIndex].
 *  - FILL_BLANK uses [acceptedAnswers] (any one of them is accepted, case-insensitive).
 */
@Serializable
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val acceptedAnswers: List<String> = emptyList(),
    val explanation: String = "",
) {
    /** Options actually shown to the student. */
    fun displayOptions(): List<String> =
        if (type == QuestionType.TRUE_FALSE) TRUE_FALSE_OPTIONS else options

    fun correctAnswerText(): String = when (type) {
        QuestionType.FILL_BLANK -> acceptedAnswers.firstOrNull().orEmpty()
        else -> displayOptions().getOrNull(correctIndex).orEmpty()
    }

    companion object {
        val TRUE_FALSE_OPTIONS = listOf("True", "False")
    }
}

@Serializable
data class Lesson(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val level: String = "",
    val topic: String = "",
    /** The lesson text students read before the quiz. */
    val content: String = "",
    val questions: List<Question> = emptyList(),
)

@Serializable
data class LessonsFile(val lessons: List<Lesson> = emptyList())

@Serializable
data class AnswerRecord(
    val prompt: String,
    val givenAnswer: String,
    val correctAnswer: String,
    val correct: Boolean,
    val explanation: String = "",
)

@Serializable
data class QuizResult(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val lessonId: String,
    val lessonTitle: String,
    val timestamp: Long,
    val score: Int,
    val total: Int,
    val answers: List<AnswerRecord>,
) {
    val percent: Int get() = if (total == 0) 0 else Math.round(score * 100f / total)
}

@Serializable
data class ResultsFile(val results: List<QuizResult> = emptyList())
