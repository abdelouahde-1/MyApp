package com.englishquiz.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.englishquiz.app.data.QuizResult
import com.englishquiz.app.data.Repository
import com.englishquiz.app.ui.AppViewModel
import com.englishquiz.app.ui.Screen
import com.englishquiz.app.ui.theme.CorrectGreen
import com.englishquiz.app.ui.theme.WrongRed

private fun writeText(ctx: Context, uri: Uri, text: String): Boolean = runCatching {
    ctx.contentResolver.openOutputStream(uri, "wt")!!.bufferedWriter().use { it.write(text) }
}.isSuccess

private fun readText(ctx: Context, uri: Uri): String? = runCatching {
    ctx.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
}.getOrNull()

@Composable
fun TeacherLoginScreen(vm: AppViewModel) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val submit = {
        if (vm.checkPin(pin)) {
            vm.back(); vm.navigate(Screen.TeacherHome)
        } else { error = true; pin = "" }
    }
    AppScaffold(vm, title = "Teacher area") { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().imePadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Enter your teacher PIN", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(8); error = false },
                label = { Text("PIN") },
                singleLine = true,
                isError = error,
                supportingText = { if (error) Text("Wrong PIN.") else Text("Default PIN is ${Repository.DEFAULT_PIN}. Change it in Settings.") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = submit, enabled = pin.isNotEmpty(), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Unlock") }
        }
    }
}

@Composable
fun TeacherHomeScreen(vm: AppViewModel) {
    val students = vm.results.map { it.studentName.lowercase() }.distinct().size
    val avg = if (vm.results.isEmpty()) null else vm.results.map { it.percent }.average().toInt()
    AppScaffold(vm, title = "Teacher area") { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Quizzes taken", vm.results.size.toString(), Modifier.weight(1f))
                StatCard("Students", students.toString(), Modifier.weight(1f))
                StatCard("Average", avg?.let { "$it%" } ?: "—", Modifier.weight(1f))
            }
            MenuCard(Icons.Filled.Person, "Student results", "See scores, review answers, export to CSV") { vm.navigate(Screen.TeacherResults) }
            MenuCard(Icons.AutoMirrored.Filled.List, "Lessons & questions", "${vm.lessons.size} lessons · create, edit, import, export") { vm.navigate(Screen.TeacherLessons) }
            MenuCard(Icons.Filled.Settings, "Settings", "PIN, feedback mode, shuffling") { vm.navigate(Screen.TeacherSettings) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MenuCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        ListItem(
            leadingContent = { Icon(icon, contentDescription = null) },
            headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(subtitle) },
        )
    }
}

@Composable
fun TeacherResultsScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    var query by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<QuizResult?>(null) }

    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.message = if (writeText(ctx, uri, vm.resultsCsv())) "Results exported." else "Export failed."
    }

    val filtered = vm.results
        .filter { query.isBlank() || it.studentName.contains(query.trim(), true) || it.lessonTitle.contains(query.trim(), true) }
        .sortedByDescending { it.timestamp }

    AppScaffold(
        vm, title = "Student results",
        actions = {
            IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Export to CSV") }, enabled = vm.results.isNotEmpty(), onClick = { menu = false; exportCsv.launch("quiz-results.csv") })
                DropdownMenuItem(text = { Text("Delete all results") }, enabled = vm.results.isNotEmpty(), onClick = { menu = false; confirmClear = true })
            }
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    label = { Text("Search by student or lesson") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (vm.results.isNotEmpty()) {
                item { Text("By lesson", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
                val byLesson = vm.results.groupBy { it.lessonTitle }.toSortedMap()
                items(byLesson.entries.toList(), key = { "sum-" + it.key }) { (title, rs) ->
                    val avg = rs.map { it.percent }.average().toInt()
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.SemiBold)
                                Text("${rs.size} attempt(s) · ${rs.map { it.studentName.lowercase() }.distinct().size} student(s)", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("$avg%", style = MaterialTheme.typography.titleLarge, color = if (avg >= 50) CorrectGreen else WrongRed)
                        }
                    }
                }
                item { Text("All attempts", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            }
            if (filtered.isEmpty()) {
                item { Text(if (vm.results.isEmpty()) "No quizzes have been taken yet." else "No matching results.", modifier = Modifier.padding(16.dp)) }
            }
            items(filtered, key = { it.id }) { r ->
                Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.Result(r.id, fromTeacher = true)) }) {
                    ListItem(
                        headlineContent = { Text(r.studentName, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("${r.lessonTitle}\n${formatDate(r.timestamp)}") },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${r.score}/${r.total}", color = if (r.percent >= 50) CorrectGreen else WrongRed, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { toDelete = r }) { Icon(Icons.Filled.Delete, contentDescription = "Delete result") }
                            }
                        },
                    )
                }
            }
        }
    }

    if (confirmClear) ConfirmDialog("Delete all results?", "This cannot be undone. Export to CSV first if you want a copy.",
        onConfirm = { vm.clearResults(); confirmClear = false }, onDismiss = { confirmClear = false })
    toDelete?.let { r ->
        ConfirmDialog("Delete this result?", "${r.studentName} — ${r.lessonTitle}",
            onConfirm = { vm.deleteResult(r.id); toDelete = null }, onDismiss = { toDelete = null })
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirmLabel: String = "Delete", onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun TeacherLessonsScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<com.englishquiz.app.data.Lesson?>(null) }

    val importJson = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = readText(ctx, uri)
            if (text == null) vm.message = "Could not open that file." else vm.importLessons(text)
        }
    }
    val exportJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.message = if (writeText(ctx, uri, vm.lessonsJson())) "Lessons exported." else "Export failed."
    }

    AppScaffold(
        vm, title = "Lessons",
        actions = {
            IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Import lessons (JSON)") }, onClick = { menu = false; importJson.launch(arrayOf("application/json", "text/plain", "*/*")) })
                DropdownMenuItem(text = { Text("Export lessons (JSON)") }, onClick = { menu = false; exportJson.launch("lessons.json") })
                DropdownMenuItem(text = { Text("Restore sample lessons") }, onClick = { menu = false; vm.restoreSampleLessons() })
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.navigate(Screen.LessonEditor(null)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New lesson") },
            )
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (vm.lessons.isEmpty()) item { Text("No lessons. Tap “New lesson” or import a file.") }
            items(vm.lessons, key = { it.id }) { l ->
                Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.LessonEditor(l.id)) }) {
                    ListItem(
                        headlineContent = { Text(l.title.ifBlank { "(untitled)" }, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(listOf(l.level, "${l.questions.size} questions").filter { it.isNotBlank() }.joinToString(" · ")) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { vm.navigate(Screen.LessonEditor(l.id)) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                                IconButton(onClick = { toDelete = l }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                            }
                        },
                    )
                }
            }
        }
    }
    toDelete?.let { l ->
        ConfirmDialog("Delete “${l.title}”?", "Students' past results for this lesson are kept.",
            onConfirm = { vm.deleteLesson(l.id); toDelete = null }, onDismiss = { toDelete = null })
    }
}

@Composable
fun TeacherSettingsScreen(vm: AppViewModel) {
    var newPin by remember { mutableStateOf("") }
    var confirmRestore by remember { mutableStateOf(false) }
    AppScaffold(vm, title = "Settings") { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp)) {
            SwitchRow(
                "Show feedback after each question",
                "Students see if they were right (and the explanation) before moving on. Off = they only see the review at the end.",
                vm.instantFeedback, vm::updateInstantFeedback,
            )
            HorizontalDivider()
            SwitchRow(
                "Shuffle questions and options",
                "Each attempt shows questions and multiple-choice options in a random order.",
                vm.shuffleQuestions, vm::updateShuffle,
            )
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Change teacher PIN", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it.filter(Char::isDigit).take(8) },
                    label = { Text("New PIN (4–8 digits)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Button(onClick = { vm.changePin(newPin); newPin = ""; vm.message = "PIN changed." }, enabled = newPin.length >= 4) { Text("Save") }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = { confirmRestore = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Star, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Restore sample lessons")
            }
        }
    }
    if (confirmRestore) ConfirmDialog(
        "Restore sample lessons?", "The built-in lessons are added back. If you edited one of them, your edits are replaced. Your own lessons are not touched.",
        confirmLabel = "Restore", onConfirm = { vm.restoreSampleLessons(); confirmRestore = false }, onDismiss = { confirmRestore = false },
    )
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
