package com.englishquiz.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishquiz.app.data.AnswerChecker
import com.englishquiz.app.data.Lesson
import com.englishquiz.app.data.Question
import com.englishquiz.app.data.QuestionType
import com.englishquiz.app.data.Validation
import com.englishquiz.app.ui.AppViewModel

private const val MAX_OPTIONS = 6

@Composable
fun LessonEditorScreen(vm: AppViewModel, lessonId: String?) {
    val original = remember(lessonId) { lessonId?.let { vm.lessonById(it) } }
    var lesson by remember(lessonId) { mutableStateOf(original ?: Lesson()) }
    // Raw text for fill-in-the-blank answers, keyed by question id, so typing "|" is not disrupted.
    val rawAccepted = remember(lessonId) {
        mutableStateMapOf<String, String>().apply {
            lesson.questions.forEach { put(it.id, it.acceptedAnswers.joinToString(" | ")) }
        }
    }
    var problems by remember { mutableStateOf<List<String>>(emptyList()) }
    var confirmDiscard by remember { mutableStateOf(false) }

    fun build(): Lesson = lesson.copy(questions = lesson.questions.map { q ->
        if (q.type == QuestionType.FILL_BLANK) q.copy(acceptedAnswers = AnswerChecker.splitAccepted(rawAccepted[q.id].orEmpty()), options = emptyList())
        else if (q.type == QuestionType.TRUE_FALSE) q.copy(options = emptyList(), acceptedAnswers = emptyList())
        else q.copy(options = q.options.map { it.trim() }, acceptedAnswers = emptyList())
    }, title = lesson.title.trim())

    fun updateQ(i: Int, f: (Question) -> Question) {
        lesson = lesson.copy(questions = lesson.questions.toMutableList().also { it[i] = f(it[i]) })
    }

    val save = {
        val built = build()
        val p = Validation.problems(built)
        if (p.isEmpty()) { vm.saveLesson(built); vm.message = "Lesson saved."; vm.back() } else problems = p
    }

    AppScaffold(
        vm,
        title = if (original == null) "New lesson" else "Edit lesson",
        onBack = { if (build() != (original ?: Lesson(id = lesson.id))) confirmDiscard = true else vm.back() },
        actions = { TextButton(onClick = save) { Text("SAVE") } },
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(lesson.title, { lesson = lesson.copy(title = it) }, label = { Text("Lesson title *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(lesson.level, { lesson = lesson.copy(level = it) }, label = { Text("Level (e.g. A2)") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(lesson.topic, { lesson = lesson.copy(topic = it) }, label = { Text("Topic") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(
                        lesson.content, { lesson = lesson.copy(content = it) },
                        label = { Text("Lesson text (what students read first)") },
                        minLines = 5, modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Questions (${lesson.questions.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                }
            }
            itemsIndexed(lesson.questions, key = { _, q -> q.id }) { i, q ->
                QuestionEditor(
                    number = i + 1,
                    q = q,
                    rawAccepted = rawAccepted[q.id].orEmpty(),
                    onRawAccepted = { rawAccepted[q.id] = it },
                    onChange = { nq -> updateQ(i) { nq } },
                    onDelete = { lesson = lesson.copy(questions = lesson.questions.filterIndexed { j, _ -> j != i }) },
                    onMove = { dir ->
                        val j = i + dir
                        if (j in lesson.questions.indices) {
                            val list = lesson.questions.toMutableList()
                            list[i] = list[j].also { list[j] = list[i] }
                            lesson = lesson.copy(questions = list)
                        }
                    },
                    isFirst = i == 0, isLast = i == lesson.questions.lastIndex,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AddButton("Multiple choice", Modifier.weight(1f)) {
                        lesson = lesson.copy(questions = lesson.questions + Question(type = QuestionType.MULTIPLE_CHOICE, options = List(4) { "" }))
                    }
                    AddButton("True / False", Modifier.weight(1f)) {
                        lesson = lesson.copy(questions = lesson.questions + Question(type = QuestionType.TRUE_FALSE))
                    }
                    AddButton("Fill blank", Modifier.weight(1f)) {
                        lesson = lesson.copy(questions = lesson.questions + Question(type = QuestionType.FILL_BLANK))
                    }
                }
                Spacer(Modifier.height(48.dp))
            }
        }
    }

    if (problems.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { problems = emptyList() },
            title = { Text("Please fix these first") },
            text = { Text(problems.take(8).joinToString("\n") { "• $it" } + if (problems.size > 8) "\n…" else "") },
            confirmButton = { TextButton(onClick = { problems = emptyList() }) { Text("OK") } },
        )
    }
    if (confirmDiscard) ConfirmDialog(
        "Discard changes?", "Your changes to this lesson have not been saved.", confirmLabel = "Discard",
        onConfirm = { confirmDiscard = false; vm.back() }, onDismiss = { confirmDiscard = false },
    )
}

@Composable
private fun AddButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionEditor(
    number: Int,
    q: Question,
    rawAccepted: String,
    onRawAccepted: (String) -> Unit,
    onChange: (Question) -> Unit,
    onDelete: () -> Unit,
    onMove: (Int) -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Question $number", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { onMove(-1) }, enabled = !isFirst) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up") }
                IconButton(onClick = { onMove(1) }, enabled = !isLast) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete question") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(QuestionType.MULTIPLE_CHOICE to "Choice", QuestionType.TRUE_FALSE to "True/False", QuestionType.FILL_BLANK to "Fill blank").forEach { (t, label) ->
                    FilterChip(
                        selected = q.type == t,
                        onClick = {
                            if (q.type != t) onChange(
                                when (t) {
                                    QuestionType.MULTIPLE_CHOICE -> q.copy(type = t, options = q.options.ifEmpty { List(4) { "" } }, correctIndex = 0)
                                    QuestionType.TRUE_FALSE -> q.copy(type = t, correctIndex = 0)
                                    QuestionType.FILL_BLANK -> q.copy(type = t)
                                }
                            )
                        },
                        label = { Text(label) },
                    )
                }
            }
            OutlinedTextField(
                q.prompt, { onChange(q.copy(prompt = it)) },
                label = { Text(if (q.type == QuestionType.FILL_BLANK) "Sentence with a blank, e.g. She ___ (go) home." else "Question") },
                modifier = Modifier.fillMaxWidth(),
            )
            when (q.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    Text("Options — tap the circle next to the correct one", style = MaterialTheme.typography.labelMedium)
                    q.options.forEachIndexed { oi, opt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = q.correctIndex == oi, onClick = { onChange(q.copy(correctIndex = oi)) })
                            OutlinedTextField(
                                opt,
                                { v -> onChange(q.copy(options = q.options.toMutableList().also { it[oi] = v })) },
                                label = { Text("Option ${oi + 1}") }, singleLine = true, modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = {
                                val newOpts = q.options.filterIndexed { j, _ -> j != oi }
                                val ci = when {
                                    q.correctIndex == oi -> 0
                                    q.correctIndex > oi -> q.correctIndex - 1
                                    else -> q.correctIndex
                                }
                                onChange(q.copy(options = newOpts, correctIndex = ci))
                            }, enabled = q.options.size > 2) { Icon(Icons.Filled.Close, contentDescription = "Remove option") }
                        }
                    }
                    if (q.options.size < MAX_OPTIONS) {
                        TextButton(onClick = { onChange(q.copy(options = q.options + "")) }) {
                            Icon(Icons.Filled.Add, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Add option")
                        }
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Correct answer:", modifier = Modifier.padding(end = 8.dp))
                        Question.TRUE_FALSE_OPTIONS.forEachIndexed { oi, label ->
                            RadioButton(selected = q.correctIndex == oi, onClick = { onChange(q.copy(correctIndex = oi)) })
                            Text(label, modifier = Modifier.padding(end = 12.dp))
                        }
                    }
                }
                QuestionType.FILL_BLANK -> {
                    OutlinedTextField(
                        rawAccepted, onRawAccepted,
                        label = { Text("Accepted answers") },
                        supportingText = { Text("Separate alternatives with |  e.g.  went | did go. Capitals and final punctuation are ignored.") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            OutlinedTextField(
                q.explanation, { onChange(q.copy(explanation = it)) },
                label = { Text("Explanation (optional, shown after answering)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
