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
    val canStart = vm.studentName.isNotBlank()
    AppScaffold(
        vm, title = "English Lesson Quiz", onBack = null,
        actions = {
            IconButton(onClick = { vm.navigate(Screen.TeacherLogin) }) {
                Icon(Icons.Filled.Lock, contentDescription = "Teacher area")
            }
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Welcome!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Read a lesson, then answer the questions to check what you understood.",
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
                keyboardActions = KeyboardActions(onGo = { if (canStart) vm.navigate(Screen.Lessons) }),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.navigate(Screen.Lessons) },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Start") }
            if (!canStart) {
                Spacer(Modifier.height(8.dp))
                Text("Type your name to begin.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun LessonListScreen(vm: AppViewModel) {
    AppScaffold(vm, title = "Hi, ${vm.studentName.trim()}") { pad ->
        if (vm.lessons.isEmpty()) {
            Centered(pad) { Text("No lessons yet. Ask your teacher to add some.") }
            return@AppScaffold
        }
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Choose a lesson", style = MaterialTheme.typography.titleMedium) }
            items(vm.lessons, key = { it.id }) { lesson ->
                val best = vm.bestPercent(lesson.id)
                Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.LessonDetail(lesson.id)) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        val meta = listOfNotNull(
                            lesson.level.takeIf { it.isNotBlank() },
                            lesson.topic.takeIf { it.isNotBlank() },
                            "${lesson.questions.size} questions",
                        ).joinToString(" · ")
                        Text(meta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (best != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Your best: $best%",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (best >= 50) CorrectGreen else WrongRed,
                            )
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
fun LessonDetailScreen(vm: AppViewModel, lessonId: String) {
    val lesson = vm.lessonById(lessonId)
    AppScaffold(
        vm, title = lesson?.title ?: "Lesson",
        bottomBar = {
            if (lesson != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = { vm.startQuiz(lesson) },
                        enabled = lesson.questions.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(52.dp),
                    ) { Text("Start the quiz (${lesson.questions.size} questions)") }
                }
            }
        },
    ) { pad ->
        if (lesson == null) { Centered(pad) { Text("This lesson no longer exists.") }; return@AppScaffold }
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(lesson.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (lesson.level.isNotBlank() || lesson.topic.isNotBlank()) {
                Text(
                    listOf(lesson.level, lesson.topic).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                lesson.content.ifBlank { "This lesson has no reading text. Go straight to the quiz." },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f,
            )
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
        vm, title = q.lesson.title, onBack = onQuit,
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
                when (item.question.type) {
                    QuestionType.TRUE_FALSE -> "True or false?"
                    QuestionType.FILL_BLANK -> "Fill in the blank"
                    QuestionType.MULTIPLE_CHOICE -> "Choose the best answer"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
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
                    Text(r.lessonTitle, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
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
                                vm.goHome(); vm.navigate(Screen.Lessons)
                            }) { Text("All lessons") }
                            Button(onClick = {
                                vm.lessonById(r.lessonId)?.let { lesson -> vm.back(); vm.startQuiz(lesson) }
                            }, enabled = vm.lessonById(r.lessonId) != null) { Text("Try again") }
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
