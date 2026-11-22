package com.englishquiz.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.englishquiz.app.data.AnswerRecord
import com.englishquiz.app.data.Lesson
import com.englishquiz.app.data.QuizEngine
import com.englishquiz.app.data.QuizItem
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.Repository

sealed interface Screen {
    data object Home : Screen
    data object Lessons : Screen
    data class LessonDetail(val lessonId: String) : Screen
    data object Quiz : Screen
    data class Result(val resultId: String, val fromTeacher: Boolean = false) : Screen
    data object TeacherLogin : Screen
    data object TeacherHome : Screen
    data object TeacherResults : Screen
    data object TeacherLessons : Screen
    data class LessonEditor(val lessonId: String?) : Screen
    data object TeacherSettings : Screen
}

class QuizSession(val lesson: Lesson, val items: List<QuizItem>, val instantFeedback: Boolean) {
    var index by mutableIntStateOf(0)
    var selected by mutableStateOf<Int?>(null)
    var typed by mutableStateOf("")
    /** Feedback for the current question when instant feedback is on. */
    var feedback by mutableStateOf<AnswerRecord?>(null)
    val answers = mutableListOf<AnswerRecord>()

    val current: QuizItem get() = items[index]
    val isLast: Boolean get() = index == items.lastIndex
    val hasAnswer: Boolean
        get() = if (current.options.isEmpty()) typed.isNotBlank() else selected != null
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository(app)

    val backStack = mutableStateListOf<Screen>(Screen.Home)
    val screen: Screen get() = backStack.last()

    var lessons by mutableStateOf(repo.loadLessons())
        private set
    var results by mutableStateOf(repo.loadResults())
        private set

    /** Not persisted: on a shared classroom device each student types their own name. */
    var studentName by mutableStateOf("")
        private set
    var instantFeedback by mutableStateOf(repo.instantFeedback)
        private set
    var shuffleQuestions by mutableStateOf(repo.shuffleQuestions)
        private set

    var quiz by mutableStateOf<QuizSession?>(null)
        private set

    var message by mutableStateOf<String?>(null)

    // ---------- Navigation ----------

    fun navigate(s: Screen) { backStack.add(s) }

    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    private fun replaceTop(s: Screen) { backStack[backStack.lastIndex] = s }

    fun goHome() { backStack.clear(); backStack.add(Screen.Home) }

    // ---------- Student ----------

    fun setStudent(name: String) { studentName = name }

    fun lessonById(id: String): Lesson? = lessons.firstOrNull { it.id == id }

    fun resultById(id: String): QuizResult? = results.firstOrNull { it.id == id }

    fun bestPercent(lessonId: String): Int? = results
        .filter { it.lessonId == lessonId && it.studentName.equals(studentName.trim(), ignoreCase = true) }
        .maxOfOrNull { it.percent }

    fun startQuiz(lesson: Lesson) {
        if (lesson.questions.isEmpty()) return
        quiz = QuizSession(lesson, QuizEngine.prepare(lesson, shuffleQuestions), instantFeedback)
        navigate(Screen.Quiz)
    }

    /** Primary button on the quiz screen: Check / Next / Finish. */
    fun quizPrimaryAction() {
        val q = quiz ?: return
        if (q.instantFeedback && q.feedback == null) {
            q.feedback = QuizEngine.grade(q.current, q.selected, q.typed)
            return
        }
        q.answers += q.feedback ?: QuizEngine.grade(q.current, q.selected, q.typed)
        if (q.isLast) {
            finishQuiz(q)
        } else {
            q.index++
            q.selected = null
            q.typed = ""
            q.feedback = null
        }
    }

    private fun finishQuiz(q: QuizSession) {
        val result = QuizResult(
            studentName = studentName.trim(),
            lessonId = q.lesson.id,
            lessonTitle = q.lesson.title,
            timestamp = System.currentTimeMillis(),
            score = q.answers.count { it.correct },
            total = q.answers.size,
            answers = q.answers.toList(),
        )
        results = results + result
        repo.saveResults(results)
        quiz = null
        replaceTop(Screen.Result(result.id))
    }

    fun abandonQuiz() {
        quiz = null
        back()
    }

    // ---------- Teacher ----------

    fun checkPin(pin: String): Boolean = pin == repo.teacherPin

    fun changePin(newPin: String) { repo.teacherPin = newPin }

    fun updateInstantFeedback(v: Boolean) { instantFeedback = v; repo.instantFeedback = v }

    fun updateShuffle(v: Boolean) { shuffleQuestions = v; repo.shuffleQuestions = v }

    fun saveLesson(lesson: Lesson) {
        lessons = if (lessons.any { it.id == lesson.id }) lessons.map { if (it.id == lesson.id) lesson else it }
        else lessons + lesson
        repo.saveLessons(lessons)
    }

    fun deleteLesson(id: String) {
        lessons = lessons.filterNot { it.id == id }
        repo.saveLessons(lessons)
    }

    fun deleteResult(id: String) {
        results = results.filterNot { it.id == id }
        repo.saveResults(results)
    }

    fun clearResults() {
        results = emptyList()
        repo.saveResults(results)
    }

    fun restoreSampleLessons() {
        val samples = repo.loadSampleLessons()
        val ids = samples.map { it.id }.toSet()
        lessons = samples + lessons.filterNot { it.id in ids }
        repo.saveLessons(lessons)
        message = "Sample lessons restored."
    }

    /** Merges lessons from a JSON file; lessons with the same id are replaced. */
    fun importLessons(text: String) {
        runCatching { Repository.parseLessons(text) }
            .onSuccess { imported ->
                val ids = imported.map { it.id }.toSet()
                lessons = lessons.filterNot { it.id in ids } + imported
                repo.saveLessons(lessons)
                message = "Imported ${imported.size} lesson(s)."
            }
            .onFailure { message = "Could not read that file: ${it.message?.take(120)}" }
    }

    fun lessonsJson(): String = repo.lessonsToJson(lessons)

    fun resultsCsv(): String = Repository.resultsToCsv(results)
}
