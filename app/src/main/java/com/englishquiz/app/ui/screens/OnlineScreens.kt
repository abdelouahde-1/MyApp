package com.englishquiz.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.englishquiz.app.data.CloudConfig
import com.englishquiz.app.data.CloudResult
import com.englishquiz.app.ui.AppViewModel
import com.englishquiz.app.ui.Screen
import com.englishquiz.app.ui.theme.CorrectGreen
import com.englishquiz.app.ui.theme.WrongRed

private fun writeCsv(ctx: Context, uri: Uri, text: String): Boolean = runCatching {
    ctx.contentResolver.openOutputStream(uri, "wt")!!.bufferedWriter().use { it.write(text) }
}.isSuccess

/** “Online results” block of the teacher Settings screen. */
@Composable
fun CloudSettingsSection(vm: AppViewModel) {
    val clipboard = LocalClipboardManager.current
    var project by remember { mutableStateOf(vm.cloudConfig.projectId) }
    var apiKey by remember { mutableStateOf(vm.cloudConfig.apiKey) }
    var email by remember { mutableStateOf(vm.cloudConfig.teacherEmail) }
    var cls by remember { mutableStateOf(vm.className) }
    var showPaste by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val draft = CloudConfig(project, apiKey, email)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Online results (Firebase)", style = MaterialTheme.typography.titleMedium)
        Text(
            "When this is set up, every finished test is sent to your Firebase project automatically. " +
                "You can then see the results from all tablets in “Online results”. Results are kept and sent later if the tablet is offline.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val status = when {
            !vm.cloudConfig.isSet -> "⚪ Not set up"
            vm.cloudError != null -> "🔴 Problem: ${vm.cloudError}"
            vm.pendingUploads > 0 -> "🟠 Set up · ${vm.pendingUploads} result(s) waiting to be sent"
            else -> "🟢 Set up · all results sent"
        }
        Text(status, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showHelp = true }) { Text("How to set up") }
            TextButton(onClick = { showPaste = true }) { Text("Paste setup code") }
        }
        OutlinedTextField(project, { project = it.trim() }, label = { Text("Firebase project ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(apiKey, { apiKey = it.trim() }, label = { Text("Web API key") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            email, { email = it.trim() }, label = { Text("Your (teacher) email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            cls, { cls = it.take(40) }, label = { Text("Class / group on this tablet (optional)") },
            supportingText = { Text("Sent with every result, e.g. “2BAC SH 3”.") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { vm.saveCloudSettings(draft, cls) }, enabled = !vm.cloudBusy) { Text("Save & test") }
            if (vm.pendingUploads > 0 && vm.cloudConfig.isSet) {
                OutlinedButton(onClick = { vm.sendPendingNow() }, enabled = !vm.cloudBusy) { Text("Send now") }
            }
            if (vm.cloudBusy) CircularProgressIndicator(Modifier.size(24.dp))
        }
        if (vm.cloudConfig.isSet) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    clipboard.setText(AnnotatedString(vm.cloudConfig.toSetupCode())); vm.message = "Setup code copied. Paste it on your other tablets."
                }) { Text("Copy setup code") }
                OutlinedButton(onClick = {
                    clipboard.setText(AnnotatedString(vm.cloudConfig.securityRules())); vm.message = "Security rules copied."
                }) { Text("Copy rules") }
            }
        }
    }

    if (showPaste) {
        var code by remember { mutableStateOf("") }
        var bad by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPaste = false },
            title = { Text("Paste setup code") },
            text = {
                Column {
                    Text("On a tablet that is already set up, tap “Copy setup code”, send it to this tablet, and paste it here.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(code, { code = it; bad = false }, isError = bad, label = { Text("EQ1-…") }, modifier = Modifier.fillMaxWidth())
                    if (bad) Text("This code is not valid.", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val c = CloudConfig.fromSetupCode(code)
                    if (c == null) bad = true else {
                        project = c.projectId; apiKey = c.apiKey; email = c.teacherEmail
                        showPaste = false
                        vm.saveCloudSettings(c, cls)
                    }
                }) { Text("Use code") }
            },
            dismissButton = { TextButton(onClick = { showPaste = false }) { Text("Cancel") } },
        )
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Set up online results") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    LessonText(SETUP_STEPS.replace("YOUR_EMAIL", email.ifBlank { "your email" }))
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        clipboard.setText(AnnotatedString(draft.securityRules())); vm.message = "Security rules copied."
                    }) { Text("Copy security rules") }
                }
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("OK") } },
        )
    }
}

private val SETUP_STEPS = """
# 1. Create the project (on a computer)
• Go to **console.firebase.google.com** and sign in with your Google account.
• Click **Create a project**, give it a name (e.g. english-quiz) and finish (Google Analytics is not needed).

# 2. Turn on sign-in
• Build → **Authentication** → Get started.
• In **Sign-in method**, enable **Anonymous** and **Email/Password**.
• In **Users**, click **Add user**: type YOUR_EMAIL and choose a password. You will use it to read the results.

# 3. Create the database
• Build → **Firestore Database** → Create database → choose a location (e.g. europe-west) → **Start in production mode**.
• Open the **Rules** tab, delete everything, paste the rules (button below; type your email in Settings first) and click **Publish**.

# 4. Copy the two codes
• Click the ⚙ gear → **Project settings** → General.
• Copy the **Project ID** and the **Web API key** into these Settings, then tap **Save & test**.

# 5. Other tablets
• Tap **Copy setup code**, send it to the other tablets (e.g. by WhatsApp to yourself), and on each one use **Paste setup code**.
""".trimIndent()

/** Teacher screen showing results uploaded by every tablet. */
@Composable
fun TeacherOnlineScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var classFilter by remember { mutableStateOf<String?>(null) }
    var toDelete by remember { mutableStateOf<CloudResult?>(null) }

    val filtered = vm.onlineResults.filter { c ->
        (classFilter == null || c.className == classFilter) &&
            (query.isBlank() || c.result.studentName.contains(query.trim(), true) || c.result.unitTitle.contains(query.trim(), true))
    }
    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.message = if (writeCsv(ctx, uri, vm.onlineResultsCsv(filtered))) "Results exported." else "Export failed."
    }

    LaunchedEffect(vm.teacherEmailSignedIn) {
        if (vm.cloudConfig.isSet && vm.teacherEmailSignedIn != null && vm.onlineLoadedAt == 0L) vm.loadOnlineResults()
    }

    AppScaffold(
        vm, title = "Online results",
        actions = {
            if (vm.teacherEmailSignedIn != null) {
                IconButton(onClick = { vm.loadOnlineResults() }, enabled = !vm.cloudBusy) { Icon(Icons.Filled.Refresh, contentDescription = "Refresh") }
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Export shown results (CSV)") }, enabled = filtered.isNotEmpty(),
                        onClick = { menu = false; exportCsv.launch("online-results.csv") })
                    DropdownMenuItem(text = { Text("Sign out") }, onClick = { menu = false; vm.teacherSignOut() })
                }
            }
        },
    ) { pad ->
        when {
            !vm.cloudConfig.isSet -> Centered(pad) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Online results are not set up on this device yet.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.navigate(Screen.TeacherSettings) }) { Text("Open Settings") }
                }
            }
            vm.teacherEmailSignedIn == null -> TeacherSignIn(vm, pad)
            else -> LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        "Signed in as ${vm.teacherEmailSignedIn}" + if (vm.onlineLoadedAt > 0) " · updated ${formatDate(vm.onlineLoadedAt)}" else "",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    OutlinedTextField(query, { query = it }, label = { Text("Search by student or unit") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                val classes = vm.onlineResults.map { it.className }.filter { it.isNotBlank() }.distinct().sorted()
                if (classes.isNotEmpty()) item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = classFilter == null, onClick = { classFilter = null }, label = { Text("All classes") })
                        classes.forEach { c -> FilterChip(selected = classFilter == c, onClick = { classFilter = c }, label = { Text(c) }) }
                    }
                }
                if (filtered.isNotEmpty()) item {
                    val avg = filtered.map { it.result.percent }.average().toInt()
                    val students = filtered.map { it.result.studentName.lowercase() }.distinct().size
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text("${filtered.size} test(s) · $students student(s) · average $avg%", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                    }
                }
                if (vm.cloudBusy && vm.onlineResults.isEmpty()) item {
                    Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Loading…") }
                } else if (filtered.isEmpty()) item {
                    Text(if (vm.onlineResults.isEmpty()) "No results received yet." else "No matching results.", modifier = Modifier.padding(16.dp))
                }
                items(filtered, key = { it.result.id }) { c ->
                    val r = c.result
                    Card(Modifier.fillMaxWidth().clickable { vm.navigate(Screen.Result(r.id, fromTeacher = true)) }) {
                        ListItem(
                            headlineContent = { Text(r.studentName + c.className.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty(), fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("${r.unitTitle}\n${r.focusLabel} · ${r.difficulty.label} · ${formatDate(r.timestamp)}") },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${r.score}/${r.total}", color = if (r.percent >= 50) CorrectGreen else WrongRed, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { toDelete = c }) { Icon(Icons.Filled.Delete, contentDescription = "Delete online result") }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
    toDelete?.let { c ->
        ConfirmDialog("Delete this online result?", "${c.result.studentName} — ${c.result.unitTitle}",
            onConfirm = { vm.deleteOnlineResult(c.result.id); toDelete = null }, onDismiss = { toDelete = null })
    }
}

@Composable
private fun TeacherSignIn(vm: AppViewModel, pad: PaddingValues) {
    var email by remember { mutableStateOf(vm.cloudConfig.teacherEmail) }
    var password by remember { mutableStateOf("") }
    Column(
        Modifier.padding(pad).fillMaxSize().imePadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Sign in to see results from all tablets", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("Use the email and password you added in Firebase Authentication.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it.trim() }, label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.teacherSignIn(email, password) },
            enabled = email.isNotBlank() && password.isNotEmpty() && !vm.cloudBusy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { if (vm.cloudBusy) CircularProgressIndicator(Modifier.size(22.dp)) else Text("Sign in") }
    }
}
