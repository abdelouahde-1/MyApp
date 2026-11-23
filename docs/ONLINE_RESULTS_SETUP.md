# Setting up Firebase (one time)

There are two apps:

- **English Quiz** (student app, on the class tablets): students type their name, pick their class and take tests. Every result is sent to you automatically. There is nothing to set up on the tablets.
- **English Quiz Teacher** (on your phone): results from all tablets, statistics, editing and publishing units, settings (class list, questions per test, feedback).

Both apps use your free Firebase project. Do the steps below once, on a computer or phone browser, in about 15 minutes.

---

## 1. Create the project
1. Go to **https://console.firebase.google.com** and sign in with your Google account.
2. **Create a project**, give it a name (for example `ENG QUIZ`), turn Google Analytics off, and click **Create**.

## 2. Turn on sign-in
1. Left menu: **Security → Authentication → Get started**.
2. **Sign-in method** tab:
   - **Anonymous**: switch it on and **Save**. The tablets use this.
   - **Add new provider → Email/Password**: switch it on and **Save**. You use this.
3. **Users** tab → **Add user**: enter your email and a password. You sign in to the teacher app with these.

## 3. Create the database
1. Left menu: **Databases & Storage → Firestore → Create database**.
2. Edition: **Standard**. Location: **europe-southwest1 (Madrid)**, which is the closest to Morocco; if it isn't offered, use `eur3` or `europe-west1`.
3. Choose **Start in production mode**, then **Create**.
4. Open the **Rules** tab, delete everything, paste the rules below **with your email**, and click **Publish**:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isTeacher() {
      return request.auth != null
        && request.auth.token.email == "your.email@gmail.com";
    }
    match /results/{id} {
      allow create: if request.auth != null;
      allow read, delete: if isTeacher();
    }
    match /config/{id} {
      allow read: if request.auth != null;
      allow write: if isTeacher();
    }
    match /units/{id} {
      allow read: if request.auth != null;
      allow write: if isTeacher();
    }
  }
}
```

What the rules allow:
- Tablets can **send** results and **read** your settings and units.
- Only you can **read or delete** results, and only you can **change** settings or units.

The teacher app can also copy these rules with your email already filled in: **Settings → Copy security rules**.

## 4. Get the Web API key
1. ⚙️ **Settings → General**. Note the **Project ID**, for example `eng-quiz-b1cc0`.
2. Under **Your apps**, tap the **`</>`** (Web) button, type a nickname, and tap **Register app**. Leave Firebase Hosting unticked.
3. Copy the `apiKey` value, which starts with `AIza…`.

The project ID and API key are built into both apps (`gradle.properties`: `firebaseProjectId`, `firebaseApiKey`).
The API key is not a secret; it is meant to be inside apps. The security rules decide who can read what.

---

## Using the teacher app

1. Install **English Quiz Teacher** on your phone and sign in with the email and password from step 2.
2. **Settings**: add your classes (for example `2BAC SH 1`, `2BAC SH 3`), choose the number of questions per test and whether students see feedback after each question, then tap **Save & send to tablets**.
3. **Units**: edit any unit or create new ones. When you're done, tap **Publish to tablets**.
4. **Results**: every finished test from every tablet. Search, filter by class, tap a result to see the answers, and use **⋮ → Export results (CSV)**.
5. **Statistics**: averages by part (vocabulary / grammar / functions), by level and by unit, the most-missed questions, and every student's progress (▲▼).

Tablets download your settings and published units each time the student app is opened.
Results are sent the moment a test ends. If a tablet is offline, its results are kept and sent later.

## Troubleshooting
- *Wrong email or password*: use the user you added in Authentication → Users.
- *Permission denied*: check that the email in the rules (step 3) is exactly the one you sign in with, and that you clicked **Publish**.
- *Sign-in method not enabled*: enable **Anonymous** and **Email/Password** (step 2).
- *The project ID or API key is wrong*: check `gradle.properties`, then rebuild the apps.
