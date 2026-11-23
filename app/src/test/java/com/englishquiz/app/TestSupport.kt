package com.englishquiz.app

import android.content.Context
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.englishquiz.app.data.CloudConfig
import com.englishquiz.app.data.CloudSync
import com.englishquiz.app.data.Endpoints
import com.englishquiz.app.data.FirebaseClient
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume.assumeTrue
import org.junit.rules.ExternalResource
import java.net.HttpURLConnection
import java.net.URL

val EMULATOR_CONFIG = CloudConfig("demo-quiz", "fake-api-key", "teacher@example.com")
const val TEACHER_EMAIL = "teacher@example.com"
const val TEACHER_PASSWORD = "secret123"

fun emulatorRunning() =
    runCatching { (URL("http://127.0.0.1:9099/").openConnection() as HttpURLConnection).apply { connectTimeout = 500 }.responseCode }.isSuccess

fun emulatorClient() = FirebaseClient(EMULATOR_CONFIG, Endpoints.emulator())

fun teacherToken(): String {
    val c = emulatorClient()
    return runCatching { c.signInWithPassword(TEACHER_EMAIL, TEACHER_PASSWORD) }
        .getOrElse { c.signUpWithPassword(TEACHER_EMAIL, TEACHER_PASSWORD) }.idToken
}

/** Wipes app data before each UI test; with [online] it points the app at the Firebase emulators (or skips the test). */
class CleanApp(private val online: Boolean) : ExternalResource() {
    public override fun before() {
        if (online) assumeTrue("Firebase emulators not running", emulatorRunning())
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.filesDir.listFiles()?.forEach { it.deleteRecursively() }
        listOf("settings", "cloud").forEach { ctx.getSharedPreferences(it, 0).edit().clear().commit() }
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx)
        CloudSync.endpointsOverride = if (online) Endpoints.emulator() else null
        CloudSync.configOverride = if (online) EMULATOR_CONFIG else null
    }

    public override fun after() {
        CloudSync.endpointsOverride = null
        CloudSync.configOverride = null
    }
}

fun AndroidComposeTestRule<*, *>.exists(text: String, substring: Boolean = false) =
    onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()

fun AndroidComposeTestRule<*, *>.shot(name: String) = onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")

/** Answers every question (first option / “x”) until the test is finished. Returns the number of questions. */
fun AndroidComposeTestRule<*, *>.takeTest(onFirst: () -> Unit = {}): Int {
    var n = 0
    while (true) {
        n++
        if (exists("Your answer")) onNodeWithText("Your answer").performTextInput("x")
        else onAllNodes(isSelectable())[0].performClick()
        if (n == 1) onFirst()
        if (exists("Check")) onNodeWithText("Check").performClick()
        if (exists("Finish")) { onNodeWithText("Finish").performClick(); return n }
        onNodeWithText("Next").performClick()
        check(n < 60) { "Test never finished" }
    }
}
