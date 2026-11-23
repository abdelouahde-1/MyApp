package com.englishquiz.app

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
 * Drives the real app on the JVM: a student picks a unit, level and focus, reads the lesson,
 * takes the test, then the teacher checks the result. Screenshots go to app/build/outputs/roborazzi/.
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

    private fun takeTest(expected: Int, firstShot: String? = null) {
        var n = 0
        while (true) {
            n++
            if (exists("Your answer")) rule.onNodeWithText("Your answer").performTextInput("x")
            else rule.onAllNodes(isSelectable())[0].performClick()
            if (n == 1 && firstShot != null) shot(firstShot)
            if (exists("Check")) rule.onNodeWithText("Check").performClick()
            if (exists("Finish")) { rule.onNodeWithText("Finish").performClick(); break }
            rule.onNodeWithText("Next").performClick()
            check(n < 30) { "Test never finished" }
        }
        check(n == expected) { "Expected $expected questions, got $n" }
    }

    @Test fun studentTakesGrammarTestThenTeacherSeesResult() {
        shot("01-home")
        rule.onNodeWithText("Your name").performTextInput("Sara")
        rule.onNodeWithText("Start").performClick()
        shot("02-units")

        rule.onNodeWithText("The Gifts of Youth").performClick()
        rule.onNodeWithText("Medium").performClick()
        rule.onNodeWithText("Grammar: Gerund & infinitive").performClick()
        shot("03-setup")
        rule.onNodeWithText("Next: read the lesson").performClick()
        shot("04-lesson")
        rule.onNodeWithText("Start the test", substring = true).performClick()
        takeTest(10, "05-question")
        rule.onNodeWithText("out of 10 correct", substring = true).assertExists()
        rule.onNodeWithText("Grammar · Medium").assertExists()
        shot("06-result")

        rule.onNodeWithText("All units").performClick()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithContentDescription("Teacher area").performClick()
        rule.onNodeWithText("PIN").performTextInput("1234")
        rule.onNodeWithText("Unlock").performClick()
        shot("07-teacher-home")
        rule.onNodeWithText("Results on this tablet").performClick()
        check(exists("Sara")) { "Result not listed" }
        shot("08-teacher-results")

        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithText("Units & questions").performClick()
        rule.onNodeWithText("Unit 1: The Gifts of Youth").performClick()
        shot("09-editor")
    }

    @Test fun mixedEasyVocabularyWithPicturesAndFeedback() {
        rule.onNodeWithContentDescription("Teacher area").performClick()
        rule.onNodeWithText("PIN").performTextInput("1234")
        rule.onNodeWithText("Unlock").performClick()
        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithText("Show feedback after each question").performClick()
        rule.onNodeWithText("5").performClick()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithContentDescription("Back").performClick()

        rule.onNodeWithText("Your name").performTextInput("Omar")
        rule.onNodeWithText("Start").performClick()
        rule.onNodeWithText("Sustainable Development").performScrollTo().performClick()
        rule.onNodeWithText("Everything (mixed)").performScrollTo().performClick()
        shot("10-setup-mixed")
        rule.onNodeWithText("Next: read the lesson").performClick()
        rule.onNodeWithText("Start the test (5 questions · Easy)").performClick()
        rule.onAllNodes(isSelectable())[0].performClick()
        rule.onNodeWithText("Check").performClick()
        shot("11-feedback")
        takeTest(5)
        rule.onNodeWithText("out of 5 correct", substring = true).assertExists()
    }
}
