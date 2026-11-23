package com.englishquiz.app

import com.englishquiz.app.data.AnswerRecord
import com.englishquiz.app.data.CloudConfig
import com.englishquiz.app.data.CloudException
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.FirebaseClient
import com.englishquiz.app.data.Endpoints
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Runs the real Firebase client against the local Firebase emulators (auth :9099, firestore :8080),
 * with the same security rules the teacher pastes into the console (teacher = teacher@example.com).
 * Skipped automatically when the emulators are not running.
 */
class FirebaseEmulatorTest {

    private val endpoints = Endpoints.emulator()
    private val config = CloudConfig(projectId = "demo-quiz", apiKey = "fake-api-key", teacherEmail = "teacher@example.com")
    private val client = FirebaseClient(config, endpoints)

    @Before fun emulatorRunning() {
        val up = runCatching { (URL("http://127.0.0.1:9099/").openConnection() as HttpURLConnection).apply { connectTimeout = 500 }.responseCode }.isSuccess
        assumeTrue("Firebase emulators not running", up)
    }

    private fun result(name: String) = QuizResult(
        id = UUID.randomUUID().toString(), studentName = name, unitId = "unit-04", unitTitle = "Unit 4: Sustainable Development",
        skill = Skill.GRAMMAR, difficulty = Difficulty.HARD, timestamp = System.currentTimeMillis(), score = 7, total = 10,
        answers = listOf(AnswerRecord("By 2030, …", "will have built", "will have built", true, "future perfect", Skill.GRAMMAR)),
        className = "2BAC SH 3",
    )

    private fun teacher(email: String = "teacher@example.com") =
        runCatching { client.signInWithPassword(email, "secret123") }.getOrElse { client.signUpWithPassword(email, "secret123") }

    @Test fun tabletUploadsAndTeacherReadsBack() {
        val device = client.signUpAnonymously()
        val r = result("Sara " + UUID.randomUUID().toString().take(4))
        client.upload(device.idToken, r, r.className, "Test tablet")
        client.upload(device.idToken, r, r.className, "Test tablet") // second upload of the same result is harmless

        val online = client.listResults(teacher().idToken)
        val mine = online.filter { it.result.id == r.id }
        assertEquals(1, mine.size)
        val got = mine.single()
        assertEquals(r.studentName, got.result.studentName)
        assertEquals("2BAC SH 3", got.className)
        assertEquals("Test tablet", got.device)
        assertEquals(Skill.GRAMMAR, got.result.skill)
        assertEquals(Difficulty.HARD, got.result.difficulty)
        assertEquals(70, got.result.percent)
        assertEquals(r.timestamp, got.result.timestamp)
        assertEquals(r.answers, got.result.answers)
    }

    @Test fun studentsCannotReadResults() {
        val device = client.signUpAnonymously()
        client.upload(device.idToken, result("Omar"), "", "t")
        try {
            client.listResults(device.idToken); fail("anonymous tablet must not read results")
        } catch (e: CloudException) {
            assertEquals(CloudException.Kind.PERMISSION, e.kind)
        }
        try {
            client.listResults(teacher("someone.else@example.com").idToken); fail("other accounts must not read results")
        } catch (e: CloudException) {
            assertEquals(CloudException.Kind.PERMISSION, e.kind)
        }
    }

    @Test fun refreshAndDeleteWork() {
        val t = teacher()
        val refreshed = client.refresh(t.refreshToken, t.email)
        val device = client.signUpAnonymously()
        val r = result("Delete me")
        client.upload(device.idToken, r, "", "t")
        client.delete(refreshed.idToken, r.id)
        assertTrue(client.listResults(refreshed.idToken).none { it.result.id == r.id })
    }

    @Test fun wrongPasswordIsReported() {
        teacher()
        try {
            client.signInWithPassword("teacher@example.com", "wrong-password"); fail()
        } catch (e: CloudException) {
            assertEquals(CloudException.Kind.AUTH, e.kind)
        }
    }

    @Test fun teacherPublishesAndTabletsReadButCannotWrite() {
        val t = teacher().idToken
        val device = client.signUpAnonymously().idToken
        client.commit(t, listOf(
            FirebaseClient.Write("units/test-b", "{\"id\":\"test-b\"}", 2),
            FirebaseClient.Write("units/test-a", "{\"id\":\"test-a\"}", 1),
            FirebaseClient.Write("config/test", "{\"classes\":[\"A\"]}"),
        ))
        assertEquals("{\"classes\":[\"A\"]}", client.getJsonDoc(device, "config/test"))
        val units = client.listJsonDocs(device, "units").filter { it.first.startsWith("test-") }
        assertEquals(listOf("test-a", "test-b"), units.map { it.first })   // sorted by order
        assertEquals(null, client.getJsonDoc(device, "config/does-not-exist"))
        try {
            client.putJsonDoc(device, "config/test", "{}"); fail("tablets must not change settings")
        } catch (e: CloudException) {
            assertEquals(CloudException.Kind.PERMISSION, e.kind)
        }
        try {
            client.commit(device, listOf(FirebaseClient.Write("units/test-a", null))); fail("tablets must not delete units")
        } catch (e: CloudException) {
            assertEquals(CloudException.Kind.PERMISSION, e.kind)
        }
        client.commit(t, listOf(FirebaseClient.Write("units/test-a", null), FirebaseClient.Write("units/test-b", null)))
        assertTrue(client.listJsonDocs(device, "units").none { it.first.startsWith("test-") })
    }
}
