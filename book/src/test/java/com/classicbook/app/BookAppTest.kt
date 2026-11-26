package com.classicbook.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.classicbook.app.ui.BookViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class BookAppTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clean() {
        ApplicationProvider.getApplicationContext<android.content.Context>().filesDir.listFiles()?.forEach { it.delete() }
    }

    private fun shot(name: String) {
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    private fun waitFor(text: String) = rule.waitUntil(20_000) {
        rule.onAllNodes(androidx.compose.ui.test.hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
    }

    @Test fun readSampleBook() {
        shot("b01-home")
        rule.onNodeWithText("Read a sample book").performClick()
        waitFor(" / ")
        shot("b02-title-page")
        repeat(4) { i ->
            rule.onRoot().performTouchInput { swipeLeft() }
            shot("b0${i + 3}-page")
        }
        rule.onNodeWithText("Aa").performClick()
        rule.onNodeWithText("Large", substring = true).performClick()
        waitFor(" / ")
        shot("b07-large")

        // (PDF export uses android.graphics.pdf.PdfDocument, which Robolectric does not implement.)
        val vm = rule.activity.run { androidx.lifecycle.ViewModelProvider(this)[BookViewModel::class.java] }
        assertTrue(vm.typeset!!.pages.size >= 7)
    }
}
