package com.englishquiz.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.englishquiz.app.data.AnswerChecker
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.Question
import com.englishquiz.app.data.QuestionType
import com.englishquiz.app.data.Section
import com.englishquiz.app.data.Skill
import com.englishquiz.app.data.StudyUnit
import com.englishquiz.app.data.Validation
import com.englishquiz.app.ui.AppViewModel

private const val MAX_OPTIONS = 6

@Composable
fun UnitEditorScreen(vm: AppViewModel, unitId: String?) {
    val original = remember(unitId) { unitId?.let { vm.unitById(it) } }
    var unit by remember(unitId) {
        mutableStateOf((original ?: StudyUnit()).let { u -> u.copy(sections = Skill.entries.map { u.section(it) }) })
    }
    val initial = remember(unitId) { unit }
    // Raw text for fill-in-the-blank answers, keyed by question id, so typing "|" is not disrupted.
    val rawAccepted = remember(unitId) {
        mutableStateMapOf<String, String>().apply {
            unit.sections.flatMap { it.questions }.forEach { put(it.id, it.acceptedAnswers.joinToString(" | ")) }
        }
    }
    var tab by remember { mutableStateOf(0) }
    var levelFilter by remember { mutableStateOf<Difficulty?>(null) }
    var problems by remember { mutableStateOf<List<String>>(emptyList()) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var numberText by remember(unitId) { mutableStateOf(if (unit.number > 0) unit.number.toString() else "") }

    val skill = Skill.entries[tab]
    val section = unit.section(skill)

    fun build(): StudyUnit = unit.copy(
        title = unit.title.trim(),
        number = numberText.toIntOrNull() ?: 0,
        sections = unit.sections.map { s ->
            s.copy(questions = s.questions.map { q ->
                when (q.type) {
                    QuestionType.FILL_BLANK -> q.copy(acceptedAnswers = AnswerChecker.splitAccepted(rawAccepted[q.id].orEmpty()), options = emptyList())
                    QuestionType.TRUE_FALSE -> q.copy(options = emptyList(), acceptedAnswers = emptyList())
                    QuestionType.MULTIPLE_CHOICE -> q.copy(options = q.options.map { it.trim() }, acceptedAnswers = emptyList())
                }.let { if (it.image.isNullOrBlank()) it.copy(image = null) else it }
            })
        },
    )

    fun updateSection(f: (Section) -> Section) {
        unit = unit.copy(sections = unit.sections.map { if (it.skill == skill) f(it) else it })
    }

    fun updateQ(id: String, f: (Question) -> Question) =
        updateSection { s -> s.copy(questions = s.questions.map { if (it.id == id) f(it) else it }) }

    val save = {
        val built = build()
        val p = Validation.problems(built)
        if (p.isEmpty()) { vm.saveUnit(built); vm.message = "Unit saved."; vm.back() } else problems = p
    }

    val visible = section.questions.filter { levelFilter == null || it.difficulty == levelFilter }

    AppScaffold(
        vm,
        title = if (original == null) "New unit" else "Edit unit",
        onBack = { if (build() != initial.copy(number = initial.number)) confirmDiscard = true else vm.back() },
        actions = { TextButton(onClick = save) { Text("SAVE") } },
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        numberText, { numberText = it.filter(Char::isDigit).take(3) }, label = { Text("Unit no.") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.width(96.dp),
                    )
                    OutlinedTextField(unit.title, { unit = unit.copy(title = it) }, label = { Text("Unit title *") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
            item {
                TabRow(selectedTabIndex = tab) {
                    Skill.entries.forEachIndexed { i, sk ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text("${sk.label} (${unit.section(sk).questions.size})") })
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        section.title, { v -> updateSection { it.copy(title = v) } },
                        label = { Text("${skill.label} topic (e.g. Gerund & Infinitive)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        section.content, { v -> updateSection { it.copy(content = v) } },
                        label = { Text("Lesson students read before the test") },
                        supportingText = { Text("Tip: start a line with \"# \" for a heading, \"• \" for a bullet, and wrap words in **stars** for bold.") },
                        minLines = 5, modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Questions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = levelFilter == null, onClick = { levelFilter = null }, label = { Text("All ${section.questions.size}") })
                        Difficulty.entries.forEach { d ->
                            FilterChip(
                                selected = levelFilter == d, onClick = { levelFilter = d },
                                label = { Text("${d.label} ${section.questions.count { it.difficulty == d }}") },
                            )
                        }
                    }
                }
            }
            items(visible, key = { it.id }) { q ->
                val pos = section.questions.indexOfFirst { it.id == q.id }
                QuestionEditor(
                    number = pos + 1,
                    q = q,
                    rawAccepted = rawAccepted[q.id].orEmpty(),
                    onRawAccepted = { rawAccepted[q.id] = it },
                    onChange = { nq -> updateQ(q.id) { nq } },
                    onDelete = { updateSection { s -> s.copy(questions = s.questions.filterNot { it.id == q.id }) } },
                    onMove = { dir ->
                        updateSection { s ->
                            val list = s.questions.toMutableList()
                            val i = list.indexOfFirst { it.id == q.id }
                            val j = i + dir
                            if (i >= 0 && j in list.indices) list[i] = list[j].also { list[j] = list[i] }
                            s.copy(questions = list)
                        }
                    },
                    isFirst = pos == 0, isLast = pos == section.questions.lastIndex,
                )
            }
            item {
                val d = levelFilter ?: Difficulty.MEDIUM
                fun add(q: Question) { updateSection { it.copy(questions = it.questions + q.copy(difficulty = d)) } }
                Text("Add a ${d.label.lowercase()} question:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AddButton("Choice", Modifier.weight(1f)) { add(Question(type = QuestionType.MULTIPLE_CHOICE, options = List(3) { "" })) }
                    AddButton("True/False", Modifier.weight(1f)) { add(Question(type = QuestionType.TRUE_FALSE)) }
                    AddButton("Write", Modifier.weight(1f)) { add(Question(type = QuestionType.FILL_BLANK)) }
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
        "Discard changes?", "Your changes to this unit have not been saved.", confirmLabel = "Discard",
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
    val ctx = LocalContext.current
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) encodeImageAsDataUri(ctx, uri)?.let { onChange(q.copy(image = it)) }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Question $number", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { onMove(-1) }, enabled = !isFirst) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up") }
                IconButton(onClick = { onMove(1) }, enabled = !isLast) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete question") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Difficulty.entries.forEach { d ->
                    FilterChip(selected = q.difficulty == d, onClick = { onChange(q.copy(difficulty = d)) }, label = { Text(d.label) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(QuestionType.MULTIPLE_CHOICE to "Choice", QuestionType.TRUE_FALSE to "True/False", QuestionType.FILL_BLANK to "Write").forEach { (t, label) ->
                    FilterChip(
                        selected = q.type == t,
                        onClick = {
                            if (q.type != t) onChange(
                                when (t) {
                                    QuestionType.MULTIPLE_CHOICE -> q.copy(type = t, options = q.options.ifEmpty { List(3) { "" } }, correctIndex = 0)
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
            // Picture
            if (q.image?.startsWith("data:") == true) {
                QuestionImage(q.image, size = 120.dp)
                TextButton(onClick = { onChange(q.copy(image = null)) }) { Text("Remove picture") }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        q.image.orEmpty(), { onChange(q.copy(image = it.take(8))) },
                        label = { Text("Picture: emoji") }, singleLine = true, modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Add photo") }
                }
            }
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
