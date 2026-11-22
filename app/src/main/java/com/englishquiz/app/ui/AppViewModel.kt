package com.englishquiz.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.englishquiz.app.data.AnswerRecord
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.QuizEngine
import com.englishquiz.app.data.QuizItem
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.Repository
import com.englishquiz.app.data.Skill
import com.englishquiz.app.data.StudyUnit

sealed interface Screen {
    data object Home : Screen
    data object Units : Screen
    data class UnitSetup(val unitId: String) : Screen
    /** [skill] null = all three parts (mixed test). */
    data class Lesson(val unitId: String, val skill: Skill?, val difficulty: Difficulty) : Screen
    data object Quiz : Screen
    data class Result(val resultId: String, val fromTeacher: Boolean = false) : Screen
    data object TeacherLogin : Screen
    data object TeacherHome : Screen
    data object TeacherResults : Screen
    data object TeacherUnits : Screen
    data class UnitEditor(val unitId: String?) : Screen
    data object TeacherSettings : Screen
}

class QuizSession(
    val unit: StudyUnit,
    val skill: Skill?,
    val difficulty: Difficulty,
    val items: List<QuizItem>,
    val instantFeedback: Boolean,
) {
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

    var units by mutableStateOf(repo.loadUnits())
        private set
    var results by mutableStateOf(repo.loadResults())
        private set

    /** Not persisted: on a shared classroom device each student types their own name. */
    var studentName by mutableStateOf("")
        private set
    var instantFeedback by mutableStateOf(repo.instantFeedback)
        private set
    var questionsPerTest by mutableIntStateOf(repo.questionsPerTest)
        private set

    /** Remembered between tests during a session so students don't re-pick each time. */
    var lastDifficulty by mutableStateOf(Difficulty.EASY)
    var lastSkill by mutableStateOf<Skill?>(null)

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

    fun unitById(id: String): StudyUnit? = units.firstOrNull { it.id == id }

    fun resultById(id: String): QuizResult? = results.firstOrNull { it.id == id }

    fun bestPercent(unitId: String): Int? = results
        .filter { it.unitId == unitId && it.studentName.equals(studentName.trim(), ignoreCase = true) }
        .maxOfOrNull { it.percent }

    fun startQuiz(unit: StudyUnit, skill: Skill?, difficulty: Difficulty) {
        val items = QuizEngine.prepare(unit, skill, difficulty, questionsPerTest)
        if (items.isEmpty()) { message = "No questions for this choice yet."; return }
        quiz = QuizSession(unit, skill, difficulty, items, instantFeedback)
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
            unitId = q.unit.id,
            unitTitle = q.unit.displayTitle,
            skill = q.skill,
            difficulty = q.difficulty,
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

    fun updateQuestionsPerTest(v: Int) { questionsPerTest = v; repo.questionsPerTest = v }

    fun saveUnit(unit: StudyUnit) {
        units = (if (units.any { it.id == unit.id }) units.map { if (it.id == unit.id) unit else it } else units + unit)
            .sortedWith(compareBy({ it.number == 0 }, { it.number }))
        repo.saveUnits(units)
    }

    fun deleteUnit(id: String) {
        units = units.filterNot { it.id == id }
        repo.saveUnits(units)
    }

    fun deleteResult(id: String) {
        results = results.filterNot { it.id == id }
        repo.saveResults(results)
    }

    fun clearResults() {
        results = emptyList()
        repo.saveResults(results)
    }

    fun restoreBuiltInUnits() {
        val builtIn = repo.loadBuiltInUnits()
        val ids = builtIn.map { it.id }.toSet()
        units = (builtIn + units.filterNot { it.id in ids }).sortedWith(compareBy({ it.number == 0 }, { it.number }))
        repo.saveUnits(units)
        message = "Built-in units restored."
    }

    /** Merges units from a JSON file; units with the same id are replaced. */
    fun importUnits(text: String) {
        runCatching { Repository.parseUnits(text) }
            .onSuccess { imported ->
                val ids = imported.map { it.id }.toSet()
                units = (units.filterNot { it.id in ids } + imported).sortedWith(compareBy({ it.number == 0 }, { it.number }))
                repo.saveUnits(units)
                message = "Imported ${imported.size} unit(s)."
            }
            .onFailure { message = "Could not read that file: ${it.message?.take(120)}" }
    }

    fun unitsJson(): String = Repository.unitsToJson(units)

    fun resultsCsv(): String = Repository.resultsToCsv(results)
}
