package com.englishquiz.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class QuestionType { MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK }

@Serializable
enum class Skill(val label: String, val icon: String) {
    VOCABULARY("Vocabulary", "📖"),
    GRAMMAR("Grammar", "✏️"),
    FUNCTIONS("Functions", "💬"),
}

@Serializable
enum class Difficulty(val label: String, val description: String) {
    EASY("Easy", "Recognise the right word or form"),
    MEDIUM("Medium", "Use it in a sentence"),
    HARD("Hard", "Exam-style: transform, correct, produce"),
}

/**
 * A single question.
 *  - MULTIPLE_CHOICE / TRUE_FALSE use [options] and [correctIndex].
 *  - FILL_BLANK uses [acceptedAnswers] (any one of them is accepted, case-insensitive).
 *  - [image] is optional: an emoji / short text shown big, or "data:image/...;base64,..." for a photo.
 */
@Serializable
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val acceptedAnswers: List<String> = emptyList(),
    val explanation: String = "",
    val image: String? = null,
) {
    fun displayOptions(): List<String> =
        if (type == QuestionType.TRUE_FALSE) TRUE_FALSE_OPTIONS else options

    companion object {
        val TRUE_FALSE_OPTIONS = listOf("True", "False")
    }
}

/** The Vocabulary, Grammar or Functions part of a unit: a lesson to read plus its question bank. */
@Serializable
data class Section(
    val skill: Skill,
    val title: String = "",
    /** Lesson text students read before the test. Lines starting with "# " are headings, "• " are bullets. */
    val content: String = "",
    val questions: List<Question> = emptyList(),
)

@Serializable
data class StudyUnit(
    val id: String = UUID.randomUUID().toString(),
    val number: Int = 0,
    val title: String = "",
    val sections: List<Section> = Skill.entries.map { Section(it) },
) {
    fun section(skill: Skill): Section = sections.firstOrNull { it.skill == skill } ?: Section(skill)

    fun questionCount(skill: Skill?, difficulty: Difficulty): Int =
        sections.filter { skill == null || it.skill == skill }.sumOf { s -> s.questions.count { it.difficulty == difficulty } }

    val totalQuestions: Int get() = sections.sumOf { it.questions.size }

    val displayTitle: String get() = if (number > 0) "Unit $number: $title" else title
}

@Serializable
data class UnitsFile(val units: List<StudyUnit> = emptyList())

@Serializable
data class AnswerRecord(
    val prompt: String,
    val givenAnswer: String,
    val correctAnswer: String,
    val correct: Boolean,
    val explanation: String = "",
    val skill: Skill? = null,
)

@Serializable
data class QuizResult(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val unitId: String,
    val unitTitle: String,
    /** null = mixed test (all three skills). */
    val skill: Skill? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val timestamp: Long,
    val score: Int,
    val total: Int,
    val answers: List<AnswerRecord>,
    /** Class / group name set on the tablet (teacher settings). */
    val className: String = "",
) {
    val percent: Int get() = if (total == 0) 0 else Math.round(score * 100f / total)
    val focusLabel: String get() = skill?.label ?: "Mixed"
}

@Serializable
data class ResultsFile(val results: List<QuizResult> = emptyList())

/** Settings the teacher publishes from the teacher app; every tablet downloads them. */
@Serializable
data class RemoteSettings(
    /** Classes students choose from on the home screen. Empty = no class question. */
    val classes: List<String> = emptyList(),
    val questionsPerTest: Int = 10,
    val instantFeedback: Boolean = false,
    /** Changes every time the teacher publishes units; tablets re-download units when it changes. 0 = never published. */
    val unitsVersion: Long = 0,
)
