# Setting up online results (Firebase)

Once this is set up, **every test a student finishes is sent to you automatically**. Students don't have to do anything.
You see the results from all tablets in the app: **teacher area → Online results (all tablets)**.

It is free. Firebase's free plan allows 20,000 results a day.
You do this once, on a computer, in about 15 minutes.

---

## 1. Create a Firebase project

1. Go to **https://console.firebase.google.com** and sign in with your Google account.
2. Click **Create a project** (or **Add project**).
3. Give it a name, for example `english-quiz`, and click **Continue**.
4. Turn **Google Analytics** off (you don't need it), then click **Create project**.

## 2. Turn on sign-in

1. In the left menu: **Build → Authentication → Get started**.
2. Open the **Sign-in method** tab and enable these two (click each one, switch it on, then **Save**):
   - **Anonymous**: the tablets use this to send results.
   - **Email/Password**: you use this to read the results.
3. Open the **Users** tab and click **Add user**. Enter **your email** and a **password** you will remember.

## 3. Create the database

1. In the left menu: **Build → Firestore Database → Create database**.
2. Choose a location close to Morocco, for example **europe-west1 (Belgium)** or **eur3 (Europe)**.
3. Choose **Start in production mode**, then click **Create**.
4. Open the **Rules** tab. Delete everything there and paste this, **replacing the email with yours**:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /results/{resultId} {
      allow create: if request.auth != null;
      allow read, delete: if request.auth != null
        && request.auth.token.email == "your.email@gmail.com";
    }
  }
}
```

5. Click **Publish**.

These rules mean tablets can **add** results but can never read them, and only **you** can read or delete results.
The app can also copy these rules for you with your email already filled in: **Settings → How to set up → Copy security rules**.

## 4. Connect the first tablet

1. In Firebase, click the ⚙️ gear (top left) → **Project settings** → **General** tab.
2. Note the **Project ID** and the **Web API key**.
3. On the tablet: **teacher area → Settings → Online results (Firebase)**:
   - **Firebase project ID**: paste the Project ID
   - **Web API key**: paste the Web API key
   - **Your (teacher) email**: the email you added in step 2
   - **Class / group** (optional): for example `2BAC SH 3`. It is sent with every result from this tablet, so you can filter by class.
4. Tap **Save & test**. It should say **🟢 Set up · all results sent**.

## 5. Connect the other tablets (no typing)

1. On the tablet you just set up, tap **Copy setup code**.
2. Send the code to the other tablets, for example by WhatsApp to yourself, or by email.
3. On each other tablet: **teacher area → Settings → Paste setup code**. Then set that tablet's **class** and tap **Save & test**.

## 6. See the results

On any device with the app (your own phone works too; just set it up with the setup code):
**teacher area → Online results (all tablets)** → sign in with your email and password.

From there you can:
- search by student or unit, or filter by class
- see each class's number of tests and average score
- tap a result to see every answer the student gave
- **⋮ → Export shown results (CSV)** to open them in Excel or Google Sheets
- delete a result

You can also look at the raw data in the Firebase console, under **Firestore Database → results**.

---

## Good to know

- **No internet?** The result is kept on the tablet and sent automatically when the connection comes back. Settings shows how many results are waiting, and has a **Send now** button.
- **Results stay on the tablet as well**: **teacher area → Results on this tablet** keeps working as before.
- **Problems** are shown in Settings next to the 🔴 symbol:
  - *"The project ID or API key is wrong"*: copy them again from Project settings.
  - *"Sign-in method not enabled"*: switch on **Anonymous** and **Email/Password** (step 2).
  - *"Permission denied"*: check the rules (step 3) and that the email in the rules is exactly the one you sign in with.
  - *"Firestore database not found"*: do step 3.
