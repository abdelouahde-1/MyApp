package com.englishquiz.app

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Drives the real app on the JVM: a student takes a full quiz, then the teacher checks results.
 * Screenshots go to app/build/outputs/roborazzi/ when run with -Proborazzi.test.record=true.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class AppFlowTest {

    @get:Rule(order = 0) val cleanData = object : org.junit.rules.ExternalResource() {
        override fun before() {
            val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            ctx.filesDir.listFiles()?.forEach { it.deleteRecursively() }
            ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    @get:Rule(order = 1) val rule = createAndroidComposeRule<MainActivity>()

    private fun shot(name: String) = rule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")

    private fun exists(text: String) = rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()

    @Test fun studentTakesQuizThenTeacherSeesResult() {
        shot("01-home")
        rule.onNodeWithText("Your name").performTextInput("Sara")
        rule.onNodeWithText("Start").performClick()
        shot("02-lessons")

        rule.onNodeWithText("Reading: A Saturday at the Market").performClick()
        shot("03-lesson")
        rule.onNodeWithText("Start the quiz", substring = true).performClick()

        var n = 0
        while (true) {
            n++
            if (exists("Your answer")) {
                rule.onNodeWithText("Your answer").performTextInput("raining")
            } else {
                rule.onAllNodes(isSelectable())[0].performClick()
            }
            if (n == 1) shot("04-question")
            if (exists("Check")) rule.onNodeWithText("Check").performClick() // instant-feedback mode
            if (exists("Finish")) { rule.onNodeWithText("Finish").performClick(); break }
            rule.onNodeWithText("Next").performClick()
            check(n < 20) { "Quiz never finished" }
        }
        rule.onNodeWithText("out of 7 correct", substring = true).assertExists()
        shot("05-result")

        // Teacher area
        rule.onNodeWithText("All lessons").performClick()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithContentDescription("Teacher area").performClick()
        rule.onNodeWithText("PIN").performTextInput("1234")
        rule.onNodeWithText("Unlock").performClick()
        shot("06-teacher-home")
        rule.onNodeWithText("Student results").performClick()
        rule.onAllNodes(hasText("Sara")).fetchSemanticsNodes().let { check(it.isNotEmpty()) { "Result not listed" } }
        shot("07-teacher-results")

        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithText("Lessons & questions").performClick()
        rule.onNodeWithText("New lesson", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Multiple choice").performClick()
        shot("08-editor")
        rule.onNodeWithText("SAVE").performClick()
        rule.onNodeWithText("Please fix these first").assertExists()
    }

    @Test fun instantFeedbackShowsCorrectness() {
        rule.onNodeWithContentDescription("Teacher area").performClick()
        rule.onNodeWithText("PIN").performTextInput("1234")
        rule.onNodeWithText("Unlock").performClick()
        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithText("Show feedback after each question").performClick()
        rule.onNodeWithText("Shuffle questions and options").performClick() // turn shuffle off
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithContentDescription("Back").performClick()

        rule.onNodeWithText("Your name").performTextInput("Omar")
        rule.onNodeWithText("Start").performClick()
        rule.onNodeWithText("Prepositions of Time: in, on, at").performClick()
        rule.onNodeWithText("Start the quiz", substring = true).performClick()
        // Q1: "The film starts ___ 8 o'clock." correct = "at"
        rule.onNode(hasText("at") and isSelectable()).performClick()
        rule.onNodeWithText("Check").performClick()
        rule.onNodeWithText("Correct!").assertExists()
        shot("09-instant-feedback")
        rule.onNodeWithText("Next").performClick()
        // Q2: "My birthday is ___ June." pick wrong answer "on"
        rule.onNode(hasText("on") and isSelectable()).performClick()
        rule.onNodeWithText("Check").performClick()
        rule.onNodeWithText("Not quite.").assertExists()
        rule.onNodeWithText("Correct answer: in").assertExists()
    }

}
