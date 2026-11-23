package com.englishquiz.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.WorkManagerTestInitHelper
import com.englishquiz.app.data.CloudConfig
import com.englishquiz.app.data.CloudSync
import com.englishquiz.app.data.Endpoints
import com.englishquiz.app.data.FirebaseClient
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.net.HttpURLConnection
import java.net.URL

/**
 * Full online flow against the local Firebase emulators: the teacher sets up a tablet,
 * a student finishes a test, the result is uploaded automatically, and the teacher sees it online.
 * Skipped when the emulators are not running.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class OnlineResultsFlowTest {

    @get:Rule(order = 0) val setup = object : ExternalResource() {
        override fun before() {
            val up = runCatching { (URL("http://127.0.0.1:9099/").openConnection() as HttpURLConnection).apply { connectTimeout = 500 }.responseCode }.isSuccess
            assumeTrue("Firebase emulators not running", up)
            val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
            ctx.filesDir.listFiles()?.forEach { it.deleteRecursively() }
            listOf("settings", "cloud").forEach { ctx.getSharedPreferences(it, 0).edit().clear().commit() }
            WorkManagerTestInitHelper.initializeTestWorkManager(ctx)
            CloudSync.endpointsOverride = Endpoints.emulator()
            val client = FirebaseClient(CloudConfig("demo-quiz", "fake-api-key"), Endpoints.emulator())
            runCatching { client.signInWithPassword("teacher@example.com", "secret123") }
                .onFailure { client.signUpWithPassword("teacher@example.com", "secret123") }
        }
        override fun after() { CloudSync.endpointsOverride = null }
    }

    @get:Rule(order = 1) val rule = createAndroidComposeRule<MainActivity>()

    private fun shot(name: String) = rule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    private fun exists(text: String, substring: Boolean = false) =
        rule.onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()

    private fun unlockTeacher() {
        rule.onNodeWithContentDescription("Teacher area").performClick()
        rule.onNodeWithText("PIN").performTextInput("1234")
        rule.onNodeWithText("Unlock").performClick()
    }

    @Test fun resultTravelsFromTabletToTeacher() {
        val student = "Yasmine " + (1000..9999).random()

        // 1. Teacher sets up online results on the tablet.
        unlockTeacher()
        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithText("Firebase project ID").performScrollTo().performTextInput("demo-quiz")
        rule.onNodeWithText("Web API key").performScrollTo().performTextInput("fake-api-key")
        rule.onNodeWithText("Your (teacher) email").performScrollTo().performTextInput("teacher@example.com")
        rule.onNodeWithText("Class / group on this tablet (optional)").performScrollTo().performTextInput("2BAC SH 3")
        rule.onNodeWithText("Save & test").performScrollTo().performClick()
        rule.waitUntil(15_000) { exists("🟢 Set up", substring = true) }
        shot("20-settings-online")
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithContentDescription("Back").performClick()

        // 2. A student takes a test; nothing else to do.
        rule.onNodeWithText("Your name").performTextInput(student)
        rule.onNodeWithText("Start").performClick()
        rule.onNodeWithText("Humour").performClick()
        rule.onNodeWithText("Next: read the lesson").performClick()
        rule.onNodeWithText("Start the test", substring = true).performClick()
        var n = 0
        while (true) {
            n++
            if (exists("Your answer")) rule.onNodeWithText("Your answer").performTextInput("x")
            else rule.onAllNodes(isSelectable())[0].performClick()
            if (exists("Check")) rule.onNodeWithText("Check").performClick() // instant-feedback mode
            if (exists("Finish")) { rule.onNodeWithText("Finish").performClick(); break }
            rule.onNodeWithText("Next").performClick()
            check(n < 30)
        }
        rule.onNodeWithText("out of", substring = true).assertExists()

        // 3. Teacher opens Online results (could be on another phone) and sees it.
        rule.onNodeWithText("All units").performClick()
        rule.onNodeWithContentDescription("Back").performClick()
        unlockTeacher()
        rule.waitUntil(15_000) { exists("all results sent", substring = true) || exists("Results sent from every tablet") }
        rule.onNodeWithText("Online results (all tablets)").performClick()
        rule.onNodeWithText("Password").performTextInput("secret123")
        rule.onNodeWithText("Sign in").performClick()
        rule.waitUntil(15_000) { exists("$student · 2BAC SH 3") }
        shot("21-online-results")
        rule.onNodeWithText("$student · 2BAC SH 3").performClick()
        rule.onNodeWithText("Result details").assertExists()
        rule.onNodeWithText("Mixed · Easy").assertExists()
        shot("22-online-detail")
    }
}
