# English Lesson Quiz (Android)

An Android app that tests 2nd-year Baccalaureate (2BAC) students on the ten units of the
*Stairway to English* booklet (based on *Ticket 2 English*). It covers **vocabulary, grammar and functions**.

![Screenshots: units, level & focus choice, lesson, question, result, picture question with feedback](docs/screenshots.png)

## How students use it

1. Type their name and pick a **unit** (1–10).
2. Choose **how hard** the questions should be:
   - 🟢 **Easy**: recognise the right word or form (many vocabulary questions have pictures)
   - 🟠 **Medium**: use it in a sentence
   - 🔴 **Hard**: exam-style: transform, correct, produce
3. Choose **what to practise**: Vocabulary, Grammar, Functions, or **Everything (mixed)**.
4. **Read the lesson** for that part, then start the test.
5. The test draws a **limited number of random questions** (10 by default) from the question bank, so every attempt is different.
6. At the end, they see a score and a review of every answer, with the correct answer and an explanation.

## Content

| Unit | Vocabulary | Grammar | Functions |
|---|---|---|---|
| 1 The Gifts of Youth | Qualities of youth & generation gap | Gerund & infinitive | Making and responding to requests |
| 2 Humour | Sources of humour | Present & past modals | Asking for clarification |
| 3 Types of Education | Education & literacy | Past perfect simple | Expressing purpose |
| 4 Sustainable Development | Problems & solutions | Future perfect | Cause & effect |
| 5 Women and Power | Situation of women, obstacles & factors | Passive voice | Linking words of addition & concession |
| 6 Cultural Values | Values of our culture | Phrasal verbs | Complaining & apologizing |
| 7 Citizenship | Rights & duties | Reported speech | Asking for & giving advice |
| 8 International Organizations | International organizations | Linking words (review)* | Responding to news |
| 9 Science & Technology | Science & technology | Wishes & conditional type 3 | Certainty & uncertainty |
| 10 Brain Drain | Loss of human capital | Relative pronouns | Expressing regret |

\* Unit 8 has no grammar lesson in the booklet, so its grammar part reviews linking words (the booklet's "Linking Words: Review & Practice").

There are about 960 questions, with at least 10 for every unit, part and level.

## Teacher area

Tap the lock icon on the home screen. The default PIN is **1234**; change it in Settings.

- **Dashboard**: number of quizzes taken, number of students, average score.
- **Online results (all tablets)**: every test a student finishes is sent automatically to your own free Firebase project. Students don't do anything. If a tablet is offline, the result is kept and sent later. You sign in with your email and password, then filter by class or student, open each student's answers, and export to CSV. Setup takes about 15 minutes, once: see **[docs/ONLINE_RESULTS_SETUP.md](docs/ONLINE_RESULTS_SETUP.md)**. To set up the other tablets, copy a setup code from the first one and paste it.
- **Results on this tablet**: every attempt, with unit, part (Vocabulary / Grammar / Functions / Mixed), level and score. Shows averages per unit and per part. Each attempt can be opened for a full answer review. Results can be exported to CSV for Excel or Google Sheets.
- **Units & questions**: edit any unit in the app. For each part you can change the lesson text and add, edit, reorder or delete questions. Each question has a level, a type (choice / true-false / write the answer), an optional picture (an emoji, or a photo from the gallery) and an explanation.
- Import or export all units as JSON to copy them to other tablets. Photos are included in the file.
- **Settings**: questions per test (5 / 10 / 15 / 20), feedback after each question or only at the end, restore built-in units.

## Install on a phone or tablet

On GitHub, open **Actions**, then the latest **Build APK** run, and download `english-lesson-quiz-apk`. Unzip it and open `app-debug.apk` on the device (Android 8 or newer).

## Editing the built-in content

The built-in units are generated from the Python files in `content/` (one file per unit):

```
python3 content/build_units.py   # writes app/src/main/assets/units.json
./gradlew testDebugUnitTest      # checks every unit, part and level has at least 10 valid questions
                                 # (the online-results tests also run if the Firebase emulators are running:
                                 #  firebase emulators:start --only auth,firestore --project demo-quiz)
./gradlew assembleDebug          # APK in app/build/outputs/apk/debug/
```

In those files, `mc(level, prompt, [correct, wrong, …])` is a multiple-choice question; put the correct answer first, and the app shuffles the options.
`tf(level, prompt, True/False)` is true/false. `wr(level, prompt, ["answer", "alternative"])` is a question the student types; capitals, extra spaces and final punctuation are ignored.
In lesson text, `# ` starts a heading, `• ` starts a bullet, and `**word**` is bold.

Devices that already have the app keep their own copy of the units. After an update, use **Settings → Restore built-in units** to load the new content.
