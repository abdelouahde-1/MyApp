package com.englishquiz.app

import com.englishquiz.app.data.AnswerChecker
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.Question
import com.englishquiz.app.data.QuestionType
import com.englishquiz.app.data.QuizEngine
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.Repository
import com.englishquiz.app.data.Section
import com.englishquiz.app.data.Skill
import com.englishquiz.app.data.StudyUnit
import com.englishquiz.app.data.Validation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class QuizLogicTest {

    private fun q(d: Difficulty, n: Int) =
        Question(id = "$d-$n", type = QuestionType.MULTIPLE_CHOICE, difficulty = d, prompt = "p$n", options = listOf("a", "b", "c"), correctIndex = 0)

    private val unit = StudyUnit(
        id = "u", title = "T",
        sections = listOf(
            Section(Skill.VOCABULARY, questions = List(12) { q(Difficulty.EASY, it) } + List(3) { q(Difficulty.HARD, it) }),
            Section(Skill.GRAMMAR, questions = List(12) { q(Difficulty.EASY, it + 100) }),
            Section(Skill.FUNCTIONS, questions = List(12) { q(Difficulty.EASY, it + 200) }),
        ),
    )

    @Test fun fillBlankIgnoresCaseSpacesAndPunctuation() {
        assertTrue(AnswerChecker.isFillBlankCorrect("  Went. ", listOf("went")))
        assertTrue(AnswerChecker.isFillBlankCorrect("won’t   have finished", listOf("won't have finished")))
        assertFalse(AnswerChecker.isFillBlankCorrect("goed", listOf("went")))
        assertFalse(AnswerChecker.isFillBlankCorrect("   ", listOf("went")))
    }

    @Test fun testIsLimitedAndUsesOnlyChosenSkillAndLevel() {
        val items = QuizEngine.prepare(unit, Skill.VOCABULARY, Difficulty.EASY, 10, Random(1))
        assertEquals(10, items.size)
        assertTrue(items.all { it.skill == Skill.VOCABULARY && it.question.difficulty == Difficulty.EASY })
        assertEquals(10, items.map { it.question.id }.toSet().size)
    }

    @Test fun smallBankGivesFewerQuestions() {
        assertEquals(3, QuizEngine.prepare(unit, Skill.VOCABULARY, Difficulty.HARD, 10).size)
        assertEquals(0, QuizEngine.prepare(unit, Skill.GRAMMAR, Difficulty.MEDIUM, 10).size)
    }

    @Test fun mixedTestBalancesSkills() {
        repeat(20) { seed ->
            val counts = QuizEngine.prepare(unit, null, Difficulty.EASY, 9, Random(seed)).groupingBy { it.skill }.eachCount()
            assertEquals(mapOf(Skill.VOCABULARY to 3, Skill.GRAMMAR to 3, Skill.FUNCTIONS to 3), counts)
        }
    }

    @Test fun shuffledOptionsKeepCorrectAnswer() {
        val question = Question(type = QuestionType.MULTIPLE_CHOICE, prompt = "p", options = listOf("a", "b", "c", "d"), correctIndex = 2)
        repeat(50) { seed ->
            val item = QuizEngine.toItem(question, Skill.GRAMMAR, Random(seed))
            assertEquals("c", item.options[item.correctIndex])
            assertTrue(QuizEngine.grade(item, item.correctIndex, "").correct)
            assertFalse(QuizEngine.grade(item, (item.correctIndex + 1) % 4, "").correct)
        }
    }

    @Test fun validationCatchesProblems() {
        assertTrue(Validation.problems(StudyUnit(title = "")).size >= 2)
        assertTrue(Validation.problems(unit).isEmpty())
    }

    @Test fun builtInUnitsAreValidAndComplete() {
        val units = Repository.parseUnits(File("src/main/assets/units.json").readText())
        assertEquals(10, units.size)
        assertEquals((1..10).toList(), units.map { it.number })
        units.forEach { u ->
            assertEquals(u.title, emptyList<String>(), Validation.problems(u))
            Skill.entries.forEach { sk ->
                assertTrue("${u.title} ${sk.label} lesson", u.section(sk).content.isNotBlank())
                Difficulty.entries.forEach { d ->
                    assertTrue("${u.title} ${sk.label} ${d.label}", u.questionCount(sk, d) >= 10)
                }
            }
        }
        val ids = units.flatMap { u -> u.sections.flatMap { s -> s.questions.map { it.id } } }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun csvIncludesFocusAndLevel() {
        val r = QuizResult(studentName = "Smith, Ana", unitId = "u", unitTitle = "Unit 1: Youth", skill = Skill.GRAMMAR,
            difficulty = Difficulty.HARD, timestamp = 0, score = 3, total = 4, answers = emptyList())
        val line = Repository.resultsToCsv(listOf(r)).lines()[1]
        assertTrue(line, line.startsWith("\"Smith, Ana\",Unit 1: Youth,Grammar,Hard,"))
        assertTrue(line.endsWith(",3,4,75"))
    }
}
