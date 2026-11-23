package com.englishquiz.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.englishquiz.app.data.CloudResult
import com.englishquiz.app.data.Difficulty
import com.englishquiz.app.data.RemoteSettings
import com.englishquiz.app.data.Repository
import com.englishquiz.app.data.Skill
import com.englishquiz.app.ui.AppViewModel
import com.englishquiz.app.ui.Screen
import com.englishquiz.app.ui.TeacherTab
import com.englishquiz.app.ui.theme.CorrectGreen
import com.englishquiz.app.ui.theme.WrongRed

private fun writeText(ctx: Context, uri: Uri, text: String): Boolean = runCatching {
    ctx.contentResolver.openOutputStream(uri, "wt")!!.bufferedWriter().use { it.write(text) }
}.isSuccess

private fun readText(ctx: Context, uri: Uri): String? = runCatching {
    ctx.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
}.getOrNull()

private fun pctColor(p: Int) = if (p >= 50) CorrectGreen else WrongRed

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

// =====================================================================
// Sign in
// =====================================================================

@Composable
fun TeacherSignInScreen(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    AppScaffold(vm, title = "English Quiz Teacher", onBack = null) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Teacher sign-in", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Use the email and password you added in Firebase → Authentication → Users.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(email, { email = it.trim() }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.teacherSignIn(email, password) },
                enabled = email.isNotBlank() && password.isNotEmpty() && !vm.busy,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { if (vm.busy) CircularProgressIndicator(Modifier.size(22.dp)) else Text("Sign in") }
        }
    }
}

// =====================================================================
// Main screen with tabs
// =====================================================================

@Composable
fun TeacherMainScreen(vm: AppViewModel, tab: TeacherTab) {
    var classFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var menu by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val filtered = vm.onlineResults.filter { classFilter == null || it.className == classFilter }

    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.message = if (writeText(ctx, uri, vm.resultsCsv(filtered.map { it.result }))) "Results exported." else "Export failed."
    }
    val importJson = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) readText(ctx, uri)?.let(vm::importUnits) ?: run { vm.message = "Could not open that file." }
    }
    val exportJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.message = if (writeText(ctx, uri, vm.unitsJson())) "Units exported." else "Export failed."
    }

    AppScaffold(
        vm, title = tab.label, onBack = null,
        actions = {
            if (vm.busy) CircularProgressIndicator(Modifier.size(22.dp).padding(end = 4.dp))
            if (tab == TeacherTab.RESULTS || tab == TeacherTab.STATS) {
                IconButton(onClick = { vm.refreshTeacherData() }, enabled = !vm.busy) { Icon(Icons.Filled.Refresh, contentDescription = "Refresh") }
            }
            if (tab == TeacherTab.RESULTS || tab == TeacherTab.UNITS) {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (tab == TeacherTab.RESULTS) {
                        DropdownMenuItem(text = { Text("Export results (CSV)") }, enabled = filtered.isNotEmpty(),
                            onClick = { menu = false; exportCsv.launch("results.csv") })
                    } else {
                        DropdownMenuItem(text = { Text("Import units (JSON)") }, onClick = { menu = false; importJson.launch(arrayOf("application/json", "text/plain", "*/*")) })
                        DropdownMenuItem(text = { Text("Export units (JSON)") }, onClick = { menu = false; exportJson.launch("units.json") })
                        DropdownMenuItem(text = { Text("Restore built-in units") }, onClick = { menu = false; vm.restoreBuiltInUnits() })
                    }
                }
            }
        },
        floatingActionButton = {
            if (tab == TeacherTab.UNITS) {
                ExtendedFloatingActionButton(
                    onClick = { vm.navigate(Screen.UnitEditor(null)) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("New unit") },
                )
            }
        },
        bottomBar = {
            NavigationBar {
                TeacherTab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = t == tab, onClick = { vm.selectTab(t) },
                        icon = {
                            Icon(
                                when (t) {
                                    TeacherTab.RESULTS -> Icons.Filled.Person
                                    TeacherTab.STATS -> Icons.Filled.Info
                                    TeacherTab.UNITS -> Icons.AutoMirrored.Filled.List
                                    TeacherTab.SETTINGS -> Icons.Filled.Settings
                                }, contentDescription = null,
                            )
                        },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { pad ->
        when (tab) {
            TeacherTab.RESULTS -> ResultsTab(vm, pad, filtered, classFilter) { classFilter = it }
            TeacherTab.STATS -> StatsTab(vm, pad, filtered, classFilter) { classFilter = it }
            TeacherTab.UNITS -> UnitsTab(vm, pad)
            TeacherTab.SETTINGS -> SettingsTab(vm, pad)
        }
    }
}

private fun LazyListScope.classChips(vm: AppViewModel, selected: String?, onSelect: (String?) -> Unit) {
    val classes = (vm.onlineResults.map { it.className }.filter { it.isNotBlank() } + vm.settings.classes).distinct().sorted()
    if (classes.isEmpty()) return
    item {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All classes") })
            classes.forEach { c -> FilterChip(selected = selected == c, onClick = { onSelect(c) }, label = { Text(c) }) }
        }
    }
}

// ---------- Results ----------

@Composable
private fun ResultsTab(vm: AppViewModel, pad: PaddingValues, inClass: List<CloudResult>, classFilter: String?, onClass: (String?) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<CloudResult?>(null) }
    val shown = inClass.filter {
        query.isBlank() || it.result.studentName.contains(query.trim(), true) || it.result.unitTitle.contains(query.trim(), true)
    }
    LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedTextField(query, { query = it }, label = { Text("Search by student or unit") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        classChips(vm, classFilter, onClass)
        if (shown.isNotEmpty()) item {
            val avg = shown.map { it.result.percent }.average().toInt()
            val students = shown.map { it.result.studentName.lowercase() }.distinct().size
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text("${shown.size} test(s) · $students student(s) · average $avg%", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
            }
        }
        if (vm.onlineLoadedAt > 0) item {
            Text("Updated ${formatDate(vm.onlineLoadedAt)} · tap ⟳ to refresh", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (shown.isEmpty()) item {
            Text(
                when {
                    vm.busy && vm.onlineResults.isEmpty() -> "Loading…"
                    vm.onlineResults.isEmpty() -> "No results yet. They appear here as soon as students finish tests."
                    else -> "No matching results."
                },
                modifier = Modifier.padding(16.dp),
            )
        }
        items(shown, key = { it.result.id }) { c ->
            val r = c.result
            Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.Result(r.id, fromTeacher = true)) }) {
                ListItem(
                    headlineContent = { Text(r.studentName + c.className.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty(), fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("${r.unitTitle}\n${r.focusLabel} · ${r.difficulty.label} · ${formatDate(r.timestamp)}") },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${r.score}/${r.total}", color = pctColor(r.percent), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { toDelete = c }) { Icon(Icons.Filled.Delete, contentDescription = "Delete result") }
                        }
                    },
                )
            }
        }
    }
    toDelete?.let { c ->
        ConfirmDialog("Delete this result?", "${c.result.studentName} — ${c.result.unitTitle}",
            onConfirm = { vm.deleteOnlineResult(c.result.id); toDelete = null }, onDismiss = { toDelete = null })
    }
}

// ---------- Statistics ----------

private data class Bar(val label: String, val sub: String, val percent: Int)

@Composable
private fun BarRow(b: Bar, onClick: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().let { if (onClick != null) it.clickable(onClick = onClick) else it }.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(b.label, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (b.sub.isNotBlank()) Text(b.sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Text("${b.percent}%", color = pctColor(b.percent), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { b.percent / 100f }, color = pctColor(b.percent), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatSection(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

private fun pct(correct: Int, total: Int) = if (total == 0) 0 else Math.round(correct * 100f / total)

@Composable
private fun StatsTab(vm: AppViewModel, pad: PaddingValues, data: List<CloudResult>, classFilter: String?, onClass: (String?) -> Unit) {
    val results = data.map { it.result }
    val answers = results.flatMap { it.answers }
    LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        classChips(vm, classFilter, onClass)
        if (results.isEmpty()) {
            item { Text(if (vm.busy) "Loading…" else "No results yet.", modifier = Modifier.padding(16.dp)) }
            return@LazyColumn
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(
                    "Tests" to results.size.toString(),
                    "Students" to results.map { it.studentName.lowercase() }.distinct().size.toString(),
                    "Average" to "${results.map { it.percent }.average().toInt()}%",
                ).forEach { (l, v) ->
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(v, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(l, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
        item {
            StatSection("By part") {
                Text("Share of correct answers, all tests together.", style = MaterialTheme.typography.bodySmall)
                Skill.entries.forEach { sk ->
                    val a = answers.filter { it.skill == sk }
                    if (a.isNotEmpty()) BarRow(Bar("${sk.icon} ${sk.label}", "${a.size} answers", pct(a.count { it.correct }, a.size)))
                }
            }
        }
        item {
            StatSection("By level") {
                Difficulty.entries.forEach { d ->
                    val rs = results.filter { it.difficulty == d }
                    if (rs.isNotEmpty()) BarRow(Bar(d.label, plural(rs.size, "test"), pct(rs.sumOf { it.score }, rs.sumOf { it.total })))
                }
            }
        }
        item {
            StatSection("By unit") {
                results.groupBy { it.unitTitle }.toSortedMap().forEach { (unit, rs) ->
                    BarRow(Bar(unit, "${plural(rs.size, "test")} · ${plural(rs.map { it.studentName.lowercase() }.distinct().size, "student")}", pct(rs.sumOf { it.score }, rs.sumOf { it.total })))
                }
            }
        }
        item {
            val hardest = answers.groupBy { it.prompt }
                .filter { it.value.size >= 3 }
                .map { (prompt, a) -> Triple(prompt, a, pct(a.count { it.correct }, a.size)) }
                .sortedBy { it.third }.take(10)
            StatSection("Most-missed questions") {
                if (hardest.isEmpty()) Text("Shown once a question has been answered at least 3 times.", style = MaterialTheme.typography.bodySmall)
                hardest.forEach { (prompt, a, p) ->
                    BarRow(Bar(prompt, "${a.size} answers · correct: ${a.first().correctAnswer}", p))
                }
            }
        }
        item {
            Text("Students", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            Text("Tap a student to see their progress. ▲▼ compares their last 3 tests with their first 3.", style = MaterialTheme.typography.bodySmall)
        }
        val students = results.groupBy { it.studentName.trim().lowercase() }.values.sortedBy { rs -> rs.map { it.percent }.average() }
        items(students, key = { "student-" + it.first().studentName.trim().lowercase() }) { rs ->
            val sorted = rs.sortedBy { it.timestamp }
            val name = sorted.last().studentName
            val trend = if (sorted.size >= 2) {
                val n = minOf(3, sorted.size / 2).coerceAtLeast(1)
                sorted.takeLast(n).map { it.percent }.average() - sorted.take(n).map { it.percent }.average()
            } else 0.0
            val arrow = when { trend >= 5 -> " ▲"; trend <= -5 -> " ▼"; else -> "" }
            val cls = sorted.last().className.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    BarRow(Bar("$name$arrow", "${plural(rs.size, "test")}$cls", rs.map { it.percent }.average().toInt())) {
                        vm.navigate(Screen.StudentHistory(name))
                    }
                }
            }
        }
    }
}

private fun plural(n: Int, word: String) = "$n $word" + if (n == 1) "" else "s"

@Composable
fun StudentHistoryScreen(vm: AppViewModel, name: String) {
    val rs = vm.onlineResults.map { it.result }.filter { it.studentName.trim().equals(name.trim(), true) }.sortedByDescending { it.timestamp }
    AppScaffold(vm, title = name) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                StatSection("Overview") {
                    BarRow(Bar("Average", plural(rs.size, "test"), rs.map { it.percent }.average().toInt()))
                    val answers = rs.flatMap { it.answers }
                    Skill.entries.forEach { sk ->
                        val a = answers.filter { it.skill == sk }
                        if (a.isNotEmpty()) BarRow(Bar("${sk.icon} ${sk.label}", "${a.size} answers", pct(a.count { it.correct }, a.size)))
                    }
                }
            }
            item { Text("Tests (newest first)", style = MaterialTheme.typography.titleMedium) }
            items(rs, key = { it.id }) { r ->
                Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.Result(r.id, fromTeacher = true)) }) {
                    ListItem(
                        headlineContent = { Text(r.unitTitle, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("${r.focusLabel} · ${r.difficulty.label} · ${formatDate(r.timestamp)}") },
                        trailingContent = { Text("${r.percent}%", color = pctColor(r.percent), fontWeight = FontWeight.Bold) },
                    )
                }
            }
        }
    }
}

// ---------- Units ----------

@Composable
private fun UnitsTab(vm: AppViewModel, pad: PaddingValues) {
    var toDelete by remember { mutableStateOf<com.englishquiz.app.data.StudyUnit?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(
                containerColor = if (vm.unitsDirty) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            )) {
                Column(Modifier.padding(14.dp)) {
                    if (vm.unitsDirty) {
                        Text("You have changes that the tablets don't have yet.", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.publishUnits() }, enabled = !vm.busy) { Text("Publish to tablets") }
                            OutlinedButton(onClick = { confirmDiscard = true }, enabled = !vm.busy) { Text("Discard") }
                        }
                    } else {
                        Text(
                            if (vm.settings.unitsVersion == 0L) "Tablets use the built-in units. Edit a unit, then publish to send your version."
                            else "Tablets have this version (published ${formatDate(vm.settings.unitsVersion)}).",
                        )
                        if (vm.settings.unitsVersion == 0L) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = { vm.publishUnits() }, enabled = !vm.busy) { Text("Publish these units now") }
                        }
                    }
                }
            }
        }
        items(vm.units, key = { it.id }) { u ->
            Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.UnitEditor(u.id)) }) {
                ListItem(
                    headlineContent = { Text(u.displayTitle.ifBlank { "(untitled)" }, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(Skill.entries.joinToString(" · ") { sk -> "${sk.label} ${u.section(sk).questions.size}" }) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { vm.navigate(Screen.UnitEditor(u.id)) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                            IconButton(onClick = { toDelete = u }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                        }
                    },
                )
            }
        }
    }
    toDelete?.let { u ->
        ConfirmDialog("Delete “${u.displayTitle}”?", "It disappears from the tablets after you publish.",
            onConfirm = { vm.deleteUnit(u.id); toDelete = null }, onDismiss = { toDelete = null })
    }
    if (confirmDiscard) ConfirmDialog("Discard your changes?", "The units go back to the version the tablets have.", confirmLabel = "Discard",
        onConfirm = { vm.discardUnitChanges(); confirmDiscard = false }, onDismiss = { confirmDiscard = false })
}

// ---------- Settings ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsTab(vm: AppViewModel, pad: PaddingValues) {
    val clipboard = LocalClipboardManager.current
    var classes by remember(vm.settings) { mutableStateOf(vm.settings.classes) }
    var newClass by remember { mutableStateOf("") }
    var perTest by remember(vm.settings) { mutableStateOf(vm.settings.questionsPerTest) }
    var feedback by remember(vm.settings) { mutableStateOf(vm.settings.instantFeedback) }
    val changed = classes != vm.settings.classes || perTest != vm.settings.questionsPerTest || feedback != vm.settings.instantFeedback
    fun addClass() {
        val c = newClass.trim()
        if (c.isNotEmpty() && c !in classes) classes = classes + c
        newClass = ""
    }

    Column(Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("These settings are sent to every tablet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text("Classes", style = MaterialTheme.typography.titleMedium)
        Text("Students pick their class on the home screen. Leave empty to not ask.", style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            classes.forEach { c ->
                InputChip(selected = false, onClick = { classes = classes - c }, label = { Text(c) },
                    trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove $c", modifier = Modifier.size(18.dp)) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(newClass, { newClass = it.take(30) }, label = { Text("New class, e.g. 2BAC SH 3") }, singleLine = true, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = ::addClass, enabled = newClass.isNotBlank()) { Text("Add") }
        }
        HorizontalDivider()

        Text("Questions per test", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Repository.QUESTION_COUNT_CHOICES.forEach { n ->
                FilterChip(selected = perTest == n, onClick = { perTest = n }, label = { Text(n.toString()) })
            }
        }
        HorizontalDivider()

        Row(Modifier.fillMaxWidth().clickable { feedback = !feedback }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Show feedback after each question", style = MaterialTheme.typography.titleMedium)
                Text("Off = students only see the review at the end.", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = feedback, onCheckedChange = { feedback = it })
        }
        Button(
            onClick = { vm.publishSettings(RemoteSettings(classes = classes, questionsPerTest = perTest, instantFeedback = feedback)) },
            enabled = changed && !vm.busy, modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text(if (changed) "Save & send to tablets" else "Saved") }
        HorizontalDivider()

        Text("Account", style = MaterialTheme.typography.titleMedium)
        Text("Signed in as ${vm.teacherEmail}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(vm.securityRules())); vm.message = "Security rules copied." }) { Text("Copy security rules") }
            OutlinedButton(onClick = { vm.teacherSignOut() }) { Text("Sign out") }
        }
        Text(
            "Paste the rules in Firebase → Firestore Database → Rules → Publish. They let tablets send results and read your settings, and only you read results.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
