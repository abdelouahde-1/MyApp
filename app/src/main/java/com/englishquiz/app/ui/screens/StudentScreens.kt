package com.englishquiz.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.Skill
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishquiz.app.data.AnswerRecord
import com.englishquiz.app.data.QuestionType
import com.englishquiz.app.ui.AppViewModel
import com.englishquiz.app.ui.Screen
import com.englishquiz.app.ui.theme.CorrectGreen
import com.englishquiz.app.ui.theme.WrongRed

@Composable
fun HomeScreen(vm: AppViewModel) {
    val go = { if (vm.canStart) vm.navigate(Screen.Units) }
    AppScaffold(vm, title = "English Quiz", onBack = null) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Welcome!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Choose a unit, pick your level, review the lesson, then test yourself.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = vm.studentName,
                onValueChange = { vm.setStudent(it.take(60)) },
                label = { Text("Your name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { go() }),
                modifier = Modifier.fillMaxWidth(),
            )
            if (vm.settings.classes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Your class", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    vm.settings.classes.forEach { c ->
                        androidx.compose.material3.FilterChip(
                            selected = vm.studentClass == c, onClick = { vm.selectClass(c) },
                            label = { Text(c, style = MaterialTheme.typography.bodyLarge) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = go,
                enabled = vm.canStart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Start") }
            if (!vm.canStart) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (vm.studentName.isBlank()) "Type your name to begin." else "Choose your class.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun UnitListScreen(vm: AppViewModel) {
    AppScaffold(vm, title = "Hi, ${vm.studentName.trim()}") { pad ->
        if (vm.units.isEmpty()) {
            Centered(pad) { Text("No units yet. Ask your teacher to add some.") }
            return@AppScaffold
        }
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Choose a unit", style = MaterialTheme.typography.titleMedium) }
            items(vm.units, key = { it.id }) { unit ->
                val best = vm.bestPercent(unit.id)
                Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.UnitSetup(unit.id)) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (unit.number > 0) {
                            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary) {
                                Text(
                                    unit.number.toString(), color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(48.dp).padding(vertical = 10.dp),
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(unit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                Skill.entries.filter { unit.section(it).questions.isNotEmpty() }.joinToString(" · ") { it.label },
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (best != null) {
                                Text(
                                    "Your best: $best%", style = MaterialTheme.typography.labelLarge,
                                    color = if (best >= 50) CorrectGreen else WrongRed,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Centered(pad: androidx.compose.foundation.layout.PaddingValues, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(
        Modifier.padding(pad).fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun ChoiceCard(
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    leading: String? = null,
) {
    OutlinedCard(
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder(enabled),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null, enabled = enabled)
            Spacer(Modifier.width(8.dp))
            if (leading != null) { Text(leading, fontSize = 24.sp); Spacer(Modifier.width(10.dp)) }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun UnitSetupScreen(vm: AppViewModel, unitId: String) {
    val unit = vm.unitById(unitId)
    var difficulty by remember { mutableStateOf(vm.lastDifficulty) }
    var skill by remember { mutableStateOf(vm.lastSkill) }
    val available = unit?.questionCount(skill, difficulty) ?: 0
    AppScaffold(
        vm, title = unit?.let { if (it.number > 0) "Unit ${it.number}" else it.title } ?: "Unit",
        bottomBar = {
            if (unit != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = {
                            vm.lastDifficulty = difficulty; vm.lastSkill = skill
                            vm.navigate(Screen.Lesson(unit.id, skill, difficulty))
                        },
                        enabled = available > 0,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(52.dp),
                    ) { Text("Next: read the lesson") }
                }
            }
        },
    ) { pad ->
        if (unit == null) { Centered(pad) { Text("This unit no longer exists.") }; return@AppScaffold }
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(unit.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("1. How hard should the questions be?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Difficulty.entries.forEach { d ->
                ChoiceCard(
                    selected = difficulty == d, onClick = { difficulty = d },
                    title = d.label, subtitle = d.description,
                    leading = when (d) { Difficulty.EASY -> "🟢"; Difficulty.MEDIUM -> "🟠"; Difficulty.HARD -> "🔴" },
                )
            }
            Text("2. What do you want to practise?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            Skill.entries.forEach { sk ->
                val n = unit.questionCount(sk, difficulty)
                ChoiceCard(
                    selected = skill == sk, enabled = n > 0, onClick = { skill = sk },
                    title = sk.label + unit.section(sk).title.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty(),
                    subtitle = if (n > 0) "${minOf(n, vm.questionsPerTest)} random questions" else "No questions at this level yet",
                    leading = sk.icon,
                )
            }
            val nAll = unit.questionCount(null, difficulty)
            ChoiceCard(
                selected = skill == null, enabled = nAll > 0, onClick = { skill = null },
                title = "Everything (mixed)", subtitle = "${minOf(nAll, vm.questionsPerTest)} random questions from vocabulary, grammar and functions",
                leading = "🔀",
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun LessonScreen(vm: AppViewModel, s: Screen.Lesson) {
    val unit = vm.unitById(s.unitId)
    val n = minOf(unit?.questionCount(s.skill, s.difficulty) ?: 0, vm.questionsPerTest)
    AppScaffold(
        vm, title = "Lesson: " + (s.skill?.label ?: "the whole unit"),
        bottomBar = {
            if (unit != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = { vm.startQuiz(unit, s.skill, s.difficulty) },
                        enabled = n > 0,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(52.dp),
                    ) { Text("Start the test ($n questions · ${s.difficulty.label})") }
                }
            }
        },
    ) { pad ->
        if (unit == null) { Centered(pad) { Text("This unit no longer exists.") }; return@AppScaffold }
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(unit.displayTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            val sections = unit.sections.filter { (s.skill == null || it.skill == s.skill) && it.content.isNotBlank() }
            if (sections.isEmpty()) Text("There is no lesson text for this part. You can start the test.")
            sections.forEach { sec ->
                Spacer(Modifier.height(12.dp))
                Text(
                    "${sec.skill.icon} ${sec.skill.label}" + sec.title.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                LessonText(sec.content)
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.HorizontalDivider()
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun QuizScreen(vm: AppViewModel, onQuit: () -> Unit) {
    val q = vm.quiz
    if (q == null) { vm.back(); return }
    val item = q.current
    val fb = q.feedback
    val locked = fb != null

    AppScaffold(
        vm, title = q.unit.displayTitle, onBack = onQuit,
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                val label = when {
                    q.instantFeedback && fb == null -> "Check"
                    q.isLast -> "Finish"
                    else -> "Next"
                }
                Button(
                    onClick = { vm.quizPrimaryAction() },
                    enabled = q.hasAnswer,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp).height(52.dp),
                ) { Text(label) }
            }
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(
                "Question ${q.index + 1} of ${q.items.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (q.index + 1f) / q.items.size },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "${item.skill.icon} ${item.skill.label} · " + when (item.question.type) {
                    QuestionType.TRUE_FALSE -> "True or false?"
                    QuestionType.FILL_BLANK -> "Write the answer"
                    QuestionType.MULTIPLE_CHOICE -> "Choose the best answer"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            if (!item.question.image.isNullOrBlank()) {
                QuestionImage(item.question.image)
                Spacer(Modifier.height(12.dp))
            }
            Text(item.question.prompt, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(20.dp))

            if (item.question.type == QuestionType.FILL_BLANK) {
                OutlinedTextField(
                    value = q.typed,
                    onValueChange = { if (!locked) q.typed = it.take(200) },
                    readOnly = locked,
                    label = { Text("Your answer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (q.hasAnswer) vm.quizPrimaryAction() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item.options.forEachIndexed { i, opt ->
                        val selected = q.selected == i
                        val border = when {
                            locked && i == item.correctIndex -> BorderStroke(2.dp, CorrectGreen)
                            locked && selected -> BorderStroke(2.dp, WrongRed)
                            selected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else -> CardDefaults.outlinedCardBorder()
                        }
                        OutlinedCard(
                            border = border,
                            modifier = Modifier.fillMaxWidth().selectable(
                                selected = selected,
                                enabled = !locked,
                                role = Role.RadioButton,
                                onClick = { q.selected = i },
                            ),
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selected, onClick = null, enabled = !locked)
                                Spacer(Modifier.width(8.dp))
                                Text(opt, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }

            if (fb != null) {
                Spacer(Modifier.height(20.dp))
                FeedbackCard(fb)
            }
        }
    }
}

@Composable
private fun FeedbackCard(a: AnswerRecord) {
    val color = if (a.correct) CorrectGreen else WrongRed
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (a.correct) Icons.Filled.Check else Icons.Filled.Close, contentDescription = null, tint = color)
                Spacer(Modifier.width(8.dp))
                Text(if (a.correct) "Correct!" else "Not quite.", color = color, fontWeight = FontWeight.Bold)
            }
            if (!a.correct) {
                Spacer(Modifier.height(6.dp))
                Text("Correct answer: ${a.correctAnswer}")
            }
            if (a.explanation.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(a.explanation, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ResultScreen(vm: AppViewModel, resultId: String, fromTeacher: Boolean) {
    val r = vm.resultById(resultId)
    AppScaffold(vm, title = if (fromTeacher) "Result details" else "Your result") { pad ->
        if (r == null) { Centered(pad) { Text("Result not found.") }; return@AppScaffold }
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(r.unitTitle, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text("${r.focusLabel} · ${r.difficulty.label}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("${r.studentName} · ${formatDate(r.timestamp)}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${r.percent}%",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (r.percent >= 50) CorrectGreen else WrongRed,
                    )
                    Text("${r.score} out of ${r.total} correct", style = MaterialTheme.typography.titleMedium)
                    if (!fromTeacher) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when {
                                r.percent == 100 -> "Perfect! Excellent work."
                                r.percent >= 80 -> "Great job! You understood this lesson well."
                                r.percent >= 50 -> "Good effort. Review the mistakes below."
                                else -> "Keep practising. Read the lesson again and retry."
                            },
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = {
                                vm.goHome(); vm.navigate(Screen.Units)
                            }) { Text("All units") }
                            Button(onClick = {
                                vm.unitById(r.unitId)?.let { unit -> vm.back(); vm.startQuiz(unit, r.skill, r.difficulty) }
                            }, enabled = vm.unitById(r.unitId) != null) { Text("New test") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            item { Text("Review", style = MaterialTheme.typography.titleMedium) }
            items(r.answers.withIndex().toList()) { (i, a) -> ReviewCard(i + 1, a) }
        }
    }
}

@Composable
private fun ReviewCard(n: Int, a: AnswerRecord) {
    val color = if (a.correct) CorrectGreen else WrongRed
    OutlinedCard(Modifier.fillMaxWidth(), border = BorderStroke(1.dp, color.copy(alpha = 0.6f))) {
        Row(Modifier.padding(14.dp)) {
            Icon(
                if (a.correct) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = if (a.correct) "Correct" else "Wrong",
                tint = color, modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text("$n. ${a.prompt}", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Answer: ${a.givenAnswer.ifBlank { "—" }}", color = color)
                if (!a.correct) Text("Correct: ${a.correctAnswer}")
                if (a.explanation.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(a.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
