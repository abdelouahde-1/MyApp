package com.englishquiz.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englishquiz.app.BuildConfig
import com.englishquiz.app.data.AnswerRecord
import com.englishquiz.app.data.CloudException
import com.englishquiz.app.data.CloudResult
import com.englishquiz.app.data.CloudSync
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.QuizEngine
import com.englishquiz.app.data.QuizItem
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.RemoteSettings
import com.englishquiz.app.data.Repository
import com.englishquiz.app.data.Skill
import com.englishquiz.app.data.StudyUnit
import kotlinx.coroutines.launch

sealed interface Screen {
    // Student app
    data object Home : Screen
    data object Units : Screen
    data class UnitSetup(val unitId: String) : Screen
    /** [skill] null = all three parts (mixed test). */
    data class Lesson(val unitId: String, val skill: Skill?, val difficulty: Difficulty) : Screen
    data object Quiz : Screen
    data class Result(val resultId: String, val fromTeacher: Boolean = false) : Screen

    // Teacher app
    data object TeacherSignIn : Screen
    data class TeacherMain(val tab: TeacherTab = TeacherTab.RESULTS) : Screen
    data class UnitEditor(val unitId: String?) : Screen
    data class StudentHistory(val name: String) : Screen
}

enum class TeacherTab(val label: String) { RESULTS("Results"), STATS("Statistics"), UNITS("Units"), SETTINGS("Settings") }

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

    val isTeacher = BuildConfig.IS_TEACHER
    private val repo = Repository(app)
    private val cloud = CloudSync(app)

    val backStack = mutableStateListOf<Screen>()
    val screen: Screen get() = backStack.last()

    var units by mutableStateOf(repo.loadUnits())
        private set
    var results by mutableStateOf(repo.loadResults())
        private set
    var message by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)
        private set

    // ---------- Settings (published by the teacher) ----------

    /** Student app: the downloaded settings. Teacher app: the settings being edited/published. */
    var settings by mutableStateOf(cloud.cachedSettings)
        private set
    val questionsPerTest: Int get() = settings.questionsPerTest.coerceIn(1, 50)

    // ---------- Student state ----------

    /** Not persisted: on a shared classroom device each student types their own name. */
    var studentName by mutableStateOf("")
        private set
    var studentClass by mutableStateOf("")
        private set
    var lastDifficulty by mutableStateOf(Difficulty.EASY)
    var lastSkill by mutableStateOf<Skill?>(null)
    var quiz by mutableStateOf<QuizSession?>(null)
        private set

    // ---------- Teacher state ----------

    var teacherEmail by mutableStateOf(cloud.teacherSignedInEmail)
        private set
    var onlineResults by mutableStateOf<List<CloudResult>>(emptyList())
        private set
    var onlineLoadedAt by mutableStateOf(0L)
        private set
    /** Units edited in the teacher app but not yet published to the tablets. */
    var unitsDirty by mutableStateOf(repo.unitsDirty)
        private set

    init {
        if (isTeacher) {
            backStack.add(if (teacherEmail == null) Screen.TeacherSignIn else Screen.TeacherMain())
            if (teacherEmail != null) loadTeacherData()
        } else {
            backStack.add(Screen.Home)
            syncStudentDevice()
        }
    }

    // ---------- Navigation ----------

    fun navigate(s: Screen) { backStack.add(s) }

    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    private fun replaceTop(s: Screen) { backStack[backStack.lastIndex] = s }

    fun goHome() { backStack.clear(); backStack.add(if (isTeacher) Screen.TeacherMain() else Screen.Home) }

    fun selectTab(tab: TeacherTab) { backStack.clear(); backStack.add(Screen.TeacherMain(tab)) }

    private fun launchCloud(quiet: Boolean = false, block: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            try { block() } catch (e: CloudException) {
                if (!quiet) message = e.message
                if (e.kind == CloudException.Kind.AUTH && isTeacher && cloud.teacherSignedInEmail == null) {
                    teacherEmail = null
                    backStack.clear(); backStack.add(Screen.TeacherSignIn)
                }
            } finally { busy = false }
        }
    }

    // ---------- Student ----------

    /** Sends waiting results and downloads the teacher's latest settings and units. Silent when offline. */
    fun syncStudentDevice() = launchCloud(quiet = true) {
        if (cloud.pending().isNotEmpty()) { CloudSync.schedule(getApplication()); runCatching { cloud.flush() } }
        val newUnits = cloud.syncFromTeacher()
        settings = cloud.cachedSettings
        if (newUnits != null) {
            units = newUnits
            repo.saveUnits(newUnits)
        }
        if (studentClass.isNotBlank() && studentClass !in settings.classes) studentClass = ""
    }

    fun setStudent(name: String) { studentName = name }

    fun selectClass(c: String) { studentClass = c }

    val canStart: Boolean get() = studentName.isNotBlank() && (settings.classes.isEmpty() || studentClass.isNotBlank())

    fun unitById(id: String): StudyUnit? = units.firstOrNull { it.id == id }

    fun resultById(id: String): QuizResult? =
        results.firstOrNull { it.id == id } ?: onlineResults.firstOrNull { it.result.id == id }?.result

    fun bestPercent(unitId: String): Int? = results
        .filter { it.unitId == unitId && it.studentName.equals(studentName.trim(), ignoreCase = true) }
        .maxOfOrNull { it.percent }

    fun startQuiz(unit: StudyUnit, skill: Skill?, difficulty: Difficulty) {
        val items = QuizEngine.prepare(unit, skill, difficulty, questionsPerTest)
        if (items.isEmpty()) { message = "No questions for this choice yet."; return }
        quiz = QuizSession(unit, skill, difficulty, items, settings.instantFeedback)
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
        if (q.isLast) finishQuiz(q) else {
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
            className = studentClass,
        )
        results = results + result
        repo.saveResults(results)
        quiz = null
        replaceTop(Screen.Result(result.id))
        cloud.enqueue(result)
        launchCloud(quiet = true) { cloud.flush() }
    }

    fun abandonQuiz() {
        quiz = null
        back()
    }

    // ---------- Teacher: account ----------

    fun teacherSignIn(email: String, password: String) = launchCloud {
        cloud.teacherSignIn(email, password)
        teacherEmail = cloud.teacherSignedInEmail
        backStack.clear(); backStack.add(Screen.TeacherMain())
        loadTeacherDataNow()
    }

    fun teacherSignOut() {
        cloud.teacherSignOut()
        teacherEmail = null
        onlineResults = emptyList()
        onlineLoadedAt = 0
        backStack.clear(); backStack.add(Screen.TeacherSignIn)
    }

    fun securityRules(): String = cloud.config.copy(teacherEmail = teacherEmail.orEmpty()).securityRules()

    private fun loadTeacherData() = launchCloud { loadTeacherDataNow() }

    private suspend fun loadTeacherDataNow() {
        settings = cloud.fetchSettings()
        if (!unitsDirty) {
            cloud.fetchPublishedUnits()?.let { published ->
                units = published
                repo.saveUnits(published)
            }
        }
        onlineResults = cloud.downloadResults()
        onlineLoadedAt = System.currentTimeMillis()
    }

    fun refreshTeacherData() = launchCloud { loadTeacherDataNow() }

    // ---------- Teacher: results ----------

    fun deleteOnlineResult(id: String) = launchCloud {
        cloud.deleteOnline(id)
        onlineResults = onlineResults.filterNot { it.result.id == id }
        message = "Result deleted."
    }

    fun resultsCsv(list: List<QuizResult>): String = Repository.resultsToCsv(list)

    // ---------- Teacher: settings ----------

    fun publishSettings(newSettings: RemoteSettings) = launchCloud {
        val s = newSettings.copy(unitsVersion = settings.unitsVersion)
        cloud.publishSettings(s)
        settings = s
        message = "Settings saved. Tablets get them next time the app opens."
    }

    // ---------- Teacher: units ----------

    private fun replaceUnits(newUnits: List<StudyUnit>) {
        units = newUnits.sortedWith(compareBy({ it.number == 0 }, { it.number }))
        repo.saveUnits(units)
        repo.unitsDirty = true
        unitsDirty = true
    }

    fun saveUnit(unit: StudyUnit) =
        replaceUnits(if (units.any { it.id == unit.id }) units.map { if (it.id == unit.id) unit else it } else units + unit)

    fun deleteUnit(id: String) = replaceUnits(units.filterNot { it.id == id })

    fun restoreBuiltInUnits() {
        val builtIn = repo.loadBuiltInUnits()
        val ids = builtIn.map { it.id }.toSet()
        replaceUnits(builtIn + units.filterNot { it.id in ids })
        message = "Built-in units restored. Publish to send them to the tablets."
    }

    /** Merges units from a JSON file; units with the same id are replaced. */
    fun importUnits(text: String) {
        runCatching { Repository.parseUnits(text) }
            .onSuccess { imported ->
                val ids = imported.map { it.id }.toSet()
                replaceUnits(units.filterNot { it.id in ids } + imported)
                message = "Imported ${imported.size} unit(s). Publish to send them to the tablets."
            }
            .onFailure { message = "Could not read that file: ${it.message?.take(120)}" }
    }

    fun unitsJson(): String = Repository.unitsToJson(units)

    fun publishUnits() = launchCloud {
        settings = cloud.publishUnits(units, settings)
        repo.unitsDirty = false
        unitsDirty = false
        message = "Published! Tablets download the new units next time the app opens."
    }

    fun discardUnitChanges() = launchCloud {
        val published = cloud.fetchPublishedUnits() ?: repo.loadBuiltInUnits()
        units = published
        repo.saveUnits(published)
        repo.unitsDirty = false
        unitsDirty = false
        message = "Changes discarded."
    }
}
