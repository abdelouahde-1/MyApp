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
import com.englishquiz.app.ui.screens.LessonDetailScreen
import com.englishquiz.app.ui.screens.LessonEditorScreen
import com.englishquiz.app.ui.screens.LessonListScreen
import com.englishquiz.app.ui.screens.QuizScreen
import com.englishquiz.app.ui.screens.ResultScreen
import com.englishquiz.app.ui.screens.TeacherHomeScreen
import com.englishquiz.app.ui.screens.TeacherLessonsScreen
import com.englishquiz.app.ui.screens.TeacherLoginScreen
import com.englishquiz.app.ui.screens.TeacherResultsScreen
import com.englishquiz.app.ui.screens.TeacherSettingsScreen

@Composable
fun App(vm: AppViewModel = viewModel()) {
    var confirmQuit by remember { mutableStateOf(false) }

    BackHandler(enabled = vm.backStack.size > 1) {
        if (vm.screen is Screen.Quiz) confirmQuit = true else vm.back()
    }

    when (val s = vm.screen) {
        Screen.Home -> HomeScreen(vm)
        Screen.Lessons -> LessonListScreen(vm)
        is Screen.LessonDetail -> LessonDetailScreen(vm, s.lessonId)
        Screen.Quiz -> QuizScreen(vm, onQuit = { confirmQuit = true })
        is Screen.Result -> ResultScreen(vm, s.resultId, s.fromTeacher)
        Screen.TeacherLogin -> TeacherLoginScreen(vm)
        Screen.TeacherHome -> TeacherHomeScreen(vm)
        Screen.TeacherResults -> TeacherResultsScreen(vm)
        Screen.TeacherLessons -> TeacherLessonsScreen(vm)
        is Screen.LessonEditor -> LessonEditorScreen(vm, s.lessonId)
        Screen.TeacherSettings -> TeacherSettingsScreen(vm)
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
