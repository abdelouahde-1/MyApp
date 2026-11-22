# English Lesson Quiz (Android)

An Android app for checking how well students understood English lessons.
Students read a short lesson, answer a quiz, and get a score with a review of their answers.
The teacher area (PIN-protected) shows every result and lets you write your own lessons and questions.

![Screenshots: home, lessons, reading, teacher results, lesson editor, instant feedback](docs/screenshots.png)

## Features

**Students**
- Enter a name and pick a lesson. The list shows each student's best score.
- Read the lesson text, then take the quiz: multiple choice, true/false, and fill in the blank.
- See a score, a percentage, and a review of every question with the correct answer and an explanation.
- Try again as many times as they want. Every attempt is saved.

**Teacher** (lock icon on the home screen, default PIN **1234**; change it in Settings)
- Dashboard: number of quizzes taken, number of students, average score.
- Student results: search, average per lesson, a detailed answer review for each attempt, export to CSV for Excel or Google Sheets, delete results.
- Lessons: create, edit, reorder and delete lessons and questions inside the app.
- Import or export lessons as JSON, so you can prepare them on a computer or share them with other teachers and devices.
- Settings: show feedback after each question (or only at the end), shuffle questions and options, restore the sample lessons.

Six sample lessons are included (A1–B1): prepositions of time, present simple vs continuous,
past simple, comparatives and superlatives, a reading comprehension text, and phrasal verbs.

Everything is stored on the device. No internet connection or account is needed.

## Install on a phone or tablet

1. Open the repository's **Actions** tab on GitHub, open the latest **Build APK** run, and download the
   `english-lesson-quiz-apk` artifact (a zip containing `app-debug.apk`).
2. Copy `app-debug.apk` to the Android device and open it. Allow "install unknown apps" when asked.

Or build it yourself (Android Studio, or JDK 17 + Android SDK):

```
./gradlew assembleDebug     # APK in app/build/outputs/apk/debug/
./gradlew testDebugUnitTest # unit + UI flow tests
```

## Lesson file format

Use **Teacher → Lessons → ⋮ → Import lessons (JSON)**. Lessons with the same `id` are replaced. Others are added.

```json
{
  "lessons": [
    {
      "id": "my-lesson-1",
      "title": "Past Simple",
      "level": "A2",
      "topic": "Grammar",
      "content": "Text the students read before the quiz…",
      "questions": [
        { "type": "MULTIPLE_CHOICE", "prompt": "She ___ to school yesterday.",
          "options": ["go", "went", "gone"], "correctIndex": 1,
          "explanation": "go → went" },
        { "type": "TRUE_FALSE", "prompt": "\"Stoped\" is correct.", "correctIndex": 1 },
        { "type": "FILL_BLANK", "prompt": "I ___ (see) a film last night.",
          "acceptedAnswers": ["saw"] }
      ]
    }
  ]
}
```

- `correctIndex` starts at 0. For TRUE_FALSE, 0 = True and 1 = False.
- Fill-in-the-blank answers ignore capital letters, extra spaces, curly or straight apostrophes, and final punctuation.
- The easiest way to get a template is to export the sample lessons and edit that file.

Sample lessons live in `app/src/main/assets/lessons.json`.
