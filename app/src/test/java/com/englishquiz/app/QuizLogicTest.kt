package com.englishquiz.app

import com.englishquiz.app.data.AnswerChecker
import com.englishquiz.app.data.Lesson
import com.englishquiz.app.data.Question
import com.englishquiz.app.data.QuestionType
import com.englishquiz.app.data.QuizEngine
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.Repository
import com.englishquiz.app.data.Validation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class QuizLogicTest {

    @Test fun fillBlankIgnoresCaseSpacesAndPunctuation() {
        assertTrue(AnswerChecker.isFillBlankCorrect("  Went. ", listOf("went")))
        assertTrue(AnswerChecker.isFillBlankCorrect("doesn’t   like", listOf("doesn't like")))
        assertFalse(AnswerChecker.isFillBlankCorrect("goed", listOf("went")))
        assertFalse(AnswerChecker.isFillBlankCorrect("   ", listOf("went")))
    }

    @Test fun splitAcceptedAnswers() {
        assertEquals(listOf("went", "did go"), AnswerChecker.splitAccepted(" went |did go | "))
    }

    @Test fun shuffledOptionsKeepCorrectAnswer() {
        val q = Question(type = QuestionType.MULTIPLE_CHOICE, prompt = "p", options = listOf("a", "b", "c", "d"), correctIndex = 2)
        val lesson = Lesson(questions = listOf(q))
        repeat(50) { seed ->
            val item = QuizEngine.prepare(lesson, shuffle = true, random = Random(seed)).single()
            assertEquals("c", item.options[item.correctIndex])
            assertTrue(QuizEngine.grade(item, item.correctIndex, "").correct)
            assertFalse(QuizEngine.grade(item, (item.correctIndex + 1) % 4, "").correct)
        }
    }

    @Test fun trueFalseGrading() {
        val q = Question(type = QuestionType.TRUE_FALSE, prompt = "p", correctIndex = 1)
        val item = QuizEngine.prepare(Lesson(questions = listOf(q)), shuffle = true).single()
        assertEquals(listOf("True", "False"), item.options)
        assertTrue(QuizEngine.grade(item, 1, "").correct)
        assertEquals("True", QuizEngine.grade(item, 0, "").givenAnswer)
    }

    @Test fun validationCatchesProblems() {
        assertTrue(Validation.problems(Lesson(title = "")).size >= 2)
        val ok = Lesson(title = "T", questions = listOf(Question(type = QuestionType.FILL_BLANK, prompt = "x", acceptedAnswers = listOf("y"))))
        assertTrue(Validation.problems(ok).isEmpty())
        val badMc = ok.copy(questions = listOf(Question(prompt = "x", options = listOf("a", ""), correctIndex = 0)))
        assertFalse(Validation.problems(badMc).isEmpty())
    }

    @Test fun sampleLessonsParseAndAreValid() {
        val text = File("src/main/assets/lessons.json").readText()
        val lessons = Repository.parseLessons(text)
        assertTrue(lessons.size >= 5)
        lessons.forEach { assertEquals(it.title, emptyList<String>(), Validation.problems(it)) }
        assertEquals(lessons.size, lessons.map { it.id }.toSet().size)
    }

    @Test fun parsesBareArrayToo() {
        val lessons = Repository.parseLessons("""[{"title":"A","questions":[{"type":"TRUE_FALSE","prompt":"x","correctIndex":0}]}]""")
        assertEquals("A", lessons.single().title)
    }

    @Test fun csvEscapesCommas() {
        val r = QuizResult(studentName = "Smith, Ana", lessonId = "l", lessonTitle = "Past \"simple\"", timestamp = 0, score = 3, total = 4, answers = emptyList())
        val line = Repository.resultsToCsv(listOf(r)).lines()[1]
        assertTrue(line.startsWith("\"Smith, Ana\",\"Past \"\"simple\"\"\","))
        assertTrue(line.endsWith(",3,4,75"))
    }
}
