package com.englishquiz.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.englishquiz.app.ui.screens.HomeScreen
import com.englishquiz.app.ui.screens.StudentHistoryScreen
import com.englishquiz.app.ui.screens.TeacherMainScreen
import com.englishquiz.app.ui.screens.TeacherSignInScreen
import com.englishquiz.app.ui.screens.LessonScreen
import com.englishquiz.app.ui.screens.UnitSetupScreen
import com.englishquiz.app.ui.screens.UnitEditorScreen
import com.englishquiz.app.ui.screens.UnitListScreen
import com.englishquiz.app.ui.screens.QuizScreen
import com.englishquiz.app.ui.screens.ResultScreen

@Composable
fun App(vm: AppViewModel = viewModel()) {
    var confirmQuit by remember { mutableStateOf(false) }

    BackHandler(enabled = vm.backStack.size > 1) {
        if (vm.screen is Screen.Quiz) confirmQuit = true else vm.back()
    }

    when (val s = vm.screen) {
        Screen.Home -> HomeScreen(vm)
        Screen.Units -> UnitListScreen(vm)
        is Screen.UnitSetup -> UnitSetupScreen(vm, s.unitId)
        is Screen.Lesson -> LessonScreen(vm, s)
        Screen.Quiz -> QuizScreen(vm, onQuit = { confirmQuit = true })
        is Screen.Result -> ResultScreen(vm, s.resultId, s.fromTeacher)
        Screen.TeacherSignIn -> TeacherSignInScreen(vm)
        is Screen.TeacherMain -> TeacherMainScreen(vm, s.tab)
        is Screen.UnitEditor -> UnitEditorScreen(vm, s.unitId)
        is Screen.StudentHistory -> StudentHistoryScreen(vm, s.name)
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text("Leave the quiz?") },
            text = { Text("Your answers so far will not be saved.") },
            confirmButton = { TextButton(onClick = { confirmQuit = false; vm.abandonQuiz() }) { Text("Leave") } },
            dismissButton = { TextButton(onClick = { confirmQuit = false }) { Text("Keep going") } },
        )
    }
}
