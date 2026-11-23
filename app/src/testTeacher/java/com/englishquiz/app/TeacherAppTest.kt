package com.englishquiz.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.englishquiz.app.data.AnswerRecord
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.RemoteSettings
import com.englishquiz.app.data.Repository
import com.englishquiz.app.data.Skill
import com.englishquiz.app.data.StudyUnit
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.UUID

/** Teacher app against the Firebase emulator: sign in, results, statistics, publish settings and units. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class TeacherAppTest {
    private val tag = (1000..9999).random().toString()

    @get:Rule(order = 0) val clean = object : org.junit.rules.ExternalResource() {
        val inner = CleanApp(online = true)
        override fun before() {
            inner.before()
            // Some tablets have sent results.
            val c = emulatorClient()
            val device = c.signUpAnonymously().idToken
            val now = System.currentTimeMillis()
            listOf(
                Triple("Amine $tag", "2BAC SH 1", listOf(4, 6, 8)),
                Triple("Hiba $tag", "2BAC SH 3", listOf(9, 7, 5)),
            ).forEach { (name, cls, scores) ->
                scores.forEachIndexed { i, sc ->
                    val answers = List(10) { k ->
                        AnswerRecord(if (k == 0) "Hard question $tag" else "Q$k", "x", "y", k < sc, "", if (k % 2 == 0) Skill.GRAMMAR else Skill.VOCABULARY)
                    }
                    c.upload(device, QuizResult(UUID.randomUUID().toString(), name, "unit-0${i + 1}", "Unit ${i + 1}", null,
                        Difficulty.entries[i], now - (3 - i) * 86_400_000L, sc, 10, answers, cls), cls, "Tablet")
                }
            }
        }
        override fun after() = inner.after()
    }
    @get:Rule(order = 1) val rule = createAndroidComposeRule<MainActivity>()

    @Test fun signInSeeResultsStatsAndPublish() {
        rule.shot("t01-signin")
        rule.onNodeWithText("Email").performTextInput(TEACHER_EMAIL)
        rule.onNodeWithText("Password").performTextInput(TEACHER_PASSWORD)
        rule.onNodeWithText("Sign in").performClick()
        rule.waitUntil(20_000) { rule.exists("test(s)", substring = true) }
        rule.onNodeWithText("Search by student or unit").performTextInput(tag)
        rule.waitUntil(5_000) { rule.exists("Hiba $tag · 2BAC SH 3") }
        rule.shot("t02-results")

        // Statistics
        rule.onNodeWithText("Statistics").performClick()
        rule.onNodeWithText("By part").assertExists()
        rule.shot("t03-stats")
        rule.onNode(androidx.compose.ui.test.hasScrollToIndexAction()).performScrollToNode(androidx.compose.ui.test.hasText("Hiba $tag ▼"))
        rule.onNodeWithText("Hiba $tag ▼").performClick()      // her scores went down
        rule.onNodeWithText("Tests (newest first)").assertExists()
        rule.shot("t04-student")
        rule.onNodeWithContentDescription("Back").performClick()

        // Settings: classes, 5 questions, publish
        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithText("New class, e.g. 2BAC SH 3").performTextInput("2BAC SH $tag")
        rule.onNodeWithText("Add").performClick()
        rule.onNodeWithText("5").performClick()
        rule.onNodeWithText("Save & send to tablets").performScrollTo().performClick()
        rule.waitUntil(15_000) { rule.exists("Saved") }
        rule.shot("t05-settings")
        val published = Repository.json.decodeFromString<RemoteSettings>(emulatorClient().getJsonDoc(teacherToken(), "config/settings")!!)
        check("2BAC SH $tag" in published.classes && published.questionsPerTest == 5)

        // Units: edit Unit 2's title, publish
        rule.onNodeWithText("Units").performClick()
        rule.onNode(androidx.compose.ui.test.hasScrollToIndexAction()).performScrollToNode(androidx.compose.ui.test.hasText("Unit 2:", substring = true))
        rule.onNodeWithText("Unit 2:", substring = true).performClick()
        rule.onNodeWithText("Unit title *").performTextClearance()
        rule.onNodeWithText("Unit title *").performTextInput("Humour $tag")
        rule.onNodeWithText("SAVE").performClick()
        rule.waitUntil(5_000) { rule.exists("Publish to tablets") }
        rule.shot("t06-units-dirty")
        rule.onNodeWithText("Publish to tablets").performClick()
        rule.waitUntil(20_000) { rule.exists("Tablets have this version", substring = true) }
        val units = emulatorClient().listJsonDocs(teacherToken(), "units").map { Repository.json.decodeFromString<StudyUnit>(it.second) }
        check(units.any { it.title == "Humour $tag" }) { "edited unit not published" }
        check(units.size >= 10)
    }
}
