package com.englishquiz.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(private val context: Context) {

    private val lessonsFile = File(context.filesDir, "lessons.json")
    private val resultsFile = File(context.filesDir, "results.json")
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // ---------- Lessons ----------

    fun loadLessons(): List<Lesson> {
        if (!lessonsFile.exists()) {
            val samples = loadSampleLessons()
            saveLessons(samples)
            return samples
        }
        return runCatching { json.decodeFromString<LessonsFile>(lessonsFile.readText()).lessons }
            .getOrElse { loadSampleLessons() }
    }

    fun saveLessons(lessons: List<Lesson>) = writeAtomic(lessonsFile, json.encodeToString(LessonsFile.serializer(), LessonsFile(lessons)))

    fun loadSampleLessons(): List<Lesson> =
        parseLessons(context.assets.open("lessons.json").bufferedReader().use { it.readText() })

    fun lessonsToJson(lessons: List<Lesson>): String = json.encodeToString(LessonsFile.serializer(), LessonsFile(lessons))

    // ---------- Results ----------

    fun loadResults(): List<QuizResult> =
        if (!resultsFile.exists()) emptyList()
        else runCatching { json.decodeFromString<ResultsFile>(resultsFile.readText()).results }.getOrElse { emptyList() }

    fun saveResults(results: List<QuizResult>) = writeAtomic(resultsFile, json.encodeToString(ResultsFile.serializer(), ResultsFile(results)))

    // ---------- Settings ----------

    var teacherPin: String
        get() = prefs.getString("pin", DEFAULT_PIN) ?: DEFAULT_PIN
        set(value) = prefs.edit().putString("pin", value).apply()

    var instantFeedback: Boolean
        get() = prefs.getBoolean("instant_feedback", false)
        set(value) = prefs.edit().putBoolean("instant_feedback", value).apply()

    var shuffleQuestions: Boolean
        get() = prefs.getBoolean("shuffle", true)
        set(value) = prefs.edit().putBoolean("shuffle", value).apply()

    private fun writeAtomic(file: File, text: String) {
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(text)
        if (!tmp.renameTo(file)) {
            file.writeText(text)
            tmp.delete()
        }
    }

    companion object {
        const val DEFAULT_PIN = "1234"

        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
            coerceInputValues = true
        }

        /** Accepts either {"lessons":[...]} or a bare [...] array. */
        fun parseLessons(text: String): List<Lesson> {
            val trimmed = text.trim()
            return if (trimmed.startsWith("[")) json.decodeFromString<List<Lesson>>(trimmed)
            else json.decodeFromString<LessonsFile>(trimmed).lessons
        }

        fun resultsToCsv(results: List<QuizResult>): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val sb = StringBuilder("Student,Lesson,Date,Score,Total,Percent\n")
            results.sortedBy { it.timestamp }.forEach { r ->
                sb.append(csv(r.studentName)).append(',')
                    .append(csv(r.lessonTitle)).append(',')
                    .append(fmt.format(Date(r.timestamp))).append(',')
                    .append(r.score).append(',')
                    .append(r.total).append(',')
                    .append(r.percent).append('\n')
            }
            return sb.toString()
        }

        private fun csv(s: String): String =
            if (s.any { it == ',' || it == '"' || it == '\n' }) "\"" + s.replace("\"", "\"\"") + "\"" else s
    }
}
