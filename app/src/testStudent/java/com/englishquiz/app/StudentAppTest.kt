package com.englishquiz.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.FirebaseClient
import com.englishquiz.app.data.Question
import com.englishquiz.app.data.QuestionType
import com.englishquiz.app.data.RemoteSettings
import com.englishquiz.app.data.Repository
import com.englishquiz.app.data.Section
import com.englishquiz.app.data.Skill
import com.englishquiz.app.data.StudyUnit
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Student app, offline: built-in units, default settings, no teacher area. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class StudentAppTest {
    @get:Rule(order = 0) val clean = CleanApp(online = false)
    @get:Rule(order = 1) val rule = createAndroidComposeRule<MainActivity>()

    @Test fun studentTakesGrammarTestOffline() {
        rule.shot("s01-home")
        check(!rule.exists("Teacher", substring = true)) { "Student app must not show any teacher area" }
        rule.onNodeWithText("Your name").performTextInput("Sara")
        rule.onNodeWithText("Start").performClick()
        rule.shot("s02-units")
        rule.onNodeWithText("The Gifts of Youth").performClick()
        rule.onNodeWithText("Medium").performClick()
        rule.onNodeWithText("Grammar: Gerund & infinitive").performClick()
        rule.shot("s03-setup")
        rule.onNodeWithText("Next: read the lesson").performClick()
        rule.shot("s04-lesson")
        rule.onNodeWithText("Start the test", substring = true).performClick()
        val n = rule.takeTest { rule.shot("s05-question") }
        check(n == 10) { "expected 10 questions, got $n" }
        rule.onNodeWithText("out of 10 correct", substring = true).assertExists()
        rule.onNodeWithText("Grammar · Medium").assertExists()
        rule.shot("s06-result")
    }
}

/** Student app, online (Firebase emulator): gets the teacher's settings & units, sends the result. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class StudentOnlineTest {
    private val student = "Yasmine " + (1000..9999).random()

    @get:Rule(order = 0) val clean = object : org.junit.rules.ExternalResource() {
        val inner = CleanApp(online = true)
        override fun before() {
            inner.before()
            // The teacher has published: two classes, 5 questions per test, feedback on, and an edited Unit 1.
            val t = teacherToken()
            val builtIn = Repository.parseUnits(java.io.File("src/main/assets/units.json").readText())
            val edited = builtIn.first().copy(title = "Youth (teacher's version)")
            val extra = StudyUnit(
                id = "unit-99", number = 99, title = "Teacher's own unit",
                sections = listOf(Section(Skill.VOCABULARY, "Colours", "# Colours\n• red, blue", List(6) {
                    Question(id = "q$it", type = QuestionType.MULTIPLE_CHOICE, difficulty = Difficulty.EASY, prompt = "Colour $it?", options = listOf("red", "blue"))
                }), Section(Skill.GRAMMAR), Section(Skill.FUNCTIONS)),
            )
            val units = listOf(edited) + builtIn.drop(1) + extra
            val compact = kotlinx.serialization.json.Json(Repository.json) { prettyPrint = false }
            val settings = RemoteSettings(listOf("2BAC SH 1", "2BAC SH 3"), 5, true, System.currentTimeMillis())
            emulatorClient().commit(t,
                units.mapIndexed { i, u -> FirebaseClient.Write("units/${u.id}", compact.encodeToString(StudyUnit.serializer(), u), i.toLong()) } +
                    FirebaseClient.Write("config/settings", compact.encodeToString(RemoteSettings.serializer(), settings)))
        }
        override fun after() = inner.after()
    }
    @get:Rule(order = 1) val rule = createAndroidComposeRule<MainActivity>()

    @Test fun getsTeacherSettingsAndSendsResult() {
        rule.waitUntil(20_000) { rule.exists("2BAC SH 3") }        // class list downloaded
        rule.onNodeWithText("Your name").performTextInput(student)
        check(!rule.exists("Start") || rule.exists("Choose your class.")) { "class must be required" }
        rule.onNodeWithText("2BAC SH 3").performClick()
        rule.shot("s10-home-classes")
        rule.onNodeWithText("Start").performClick()
        rule.waitUntil(10_000) { rule.exists("Youth (teacher's version)") } // published units downloaded
        rule.onNode(androidx.compose.ui.test.hasScrollToIndexAction()).performScrollToNode(androidx.compose.ui.test.hasText("Teacher's own unit"))
        rule.onNodeWithText("Teacher's own unit").assertExists()
        rule.onNode(androidx.compose.ui.test.hasScrollToIndexAction()).performScrollToNode(androidx.compose.ui.test.hasText("Youth (teacher's version)"))
        rule.onNodeWithText("Youth (teacher's version)").performClick()
        rule.onNodeWithText("Next: read the lesson").performClick()
        rule.onNodeWithText("Start the test (5 questions · Easy)").performClick()  // 5 per test
        rule.onAllNodes(androidx.compose.ui.test.isSelectable())[0].performClick()
        rule.onNodeWithText("Check").performClick()                                   // instant feedback on
        rule.takeTest()
        rule.onNodeWithText("out of 5 correct", substring = true).assertExists()

        // The result reaches the teacher's database, with the class.
        val t = teacherToken()
        var found: com.englishquiz.app.data.CloudResult? = null
        val deadline = System.currentTimeMillis() + 20_000
        while (found == null && System.currentTimeMillis() < deadline) {
            found = emulatorClient().listResults(t).firstOrNull { it.result.studentName == student }
            if (found == null) { rule.mainClock.advanceTimeBy(500); Thread.sleep(500) }
        }
        check(found != null) { "result not uploaded" }
        check(found.className == "2BAC SH 3")
        check(found.result.total == 5)
    }
}
