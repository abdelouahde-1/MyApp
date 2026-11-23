package com.englishquiz.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(private val context: Context) {

    private val unitsFile = File(context.filesDir, "units.json")
    private val resultsFile = File(context.filesDir, "results.json")
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // ---------- Units ----------

    fun loadUnits(): List<StudyUnit> {
        if (!unitsFile.exists()) {
            val builtIn = loadBuiltInUnits()
            saveUnits(builtIn)
            return builtIn
        }
        return runCatching { parseUnits(unitsFile.readText()) }.getOrElse { loadBuiltInUnits() }
    }

    fun saveUnits(units: List<StudyUnit>) = writeAtomic(unitsFile, unitsToJson(units))

    fun loadBuiltInUnits(): List<StudyUnit> =
        parseUnits(context.assets.open("units.json").bufferedReader().use { it.readText() })

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

    var questionsPerTest: Int
        get() = prefs.getInt("questions_per_test", DEFAULT_QUESTIONS)
        set(value) = prefs.edit().putInt("questions_per_test", value).apply()

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
        const val DEFAULT_QUESTIONS = 10
        val QUESTION_COUNT_CHOICES = listOf(5, 10, 15, 20)

        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
            coerceInputValues = true
        }

        fun unitsToJson(units: List<StudyUnit>): String = json.encodeToString(UnitsFile.serializer(), UnitsFile(units))

        /** Accepts either {"units":[...]} or a bare [...] array. */
        fun parseUnits(text: String): List<StudyUnit> {
            val trimmed = text.trim()
            return if (trimmed.startsWith("[")) json.decodeFromString<List<StudyUnit>>(trimmed)
            else json.decodeFromString<UnitsFile>(trimmed).units
        }

        fun resultsToCsv(results: List<QuizResult>): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val sb = StringBuilder("Student,Class,Unit,Focus,Level,Date,Score,Total,Percent\n")
            results.sortedBy { it.timestamp }.forEach { r ->
                sb.append(csv(r.studentName)).append(',')
                    .append(csv(r.className)).append(',')
                    .append(csv(r.unitTitle)).append(',')
                    .append(r.focusLabel).append(',')
                    .append(r.difficulty.label).append(',')
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
