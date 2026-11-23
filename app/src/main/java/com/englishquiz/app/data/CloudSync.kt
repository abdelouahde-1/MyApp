package com.englishquiz.app.data

import android.content.Context
import android.os.Build
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.TimeUnit

@Serializable
private data class PendingFile(val results: List<QuizResult> = emptyList())

/**
 * Sends finished tests to the teacher's Firebase project.
 * Results are queued on the device first, so nothing is lost when the tablet is offline;
 * WorkManager retries automatically as soon as there is a connection.
 */
class CloudSync(private val context: Context, private val endpoints: Endpoints = endpointsOverride ?: Endpoints.PRODUCTION) {

    private val prefs = context.getSharedPreferences("cloud", Context.MODE_PRIVATE)
    private val pendingFile = File(context.filesDir, "pending_uploads.json")

    var config: CloudConfig
        get() = CloudConfig(prefs.getString("project", "")!!, prefs.getString("key", "")!!, prefs.getString("email", "")!!)
        set(v) {
            val changed = v.projectId != config.projectId || v.apiKey != config.apiKey
            prefs.edit().putString("project", v.projectId.trim()).putString("key", v.apiKey.trim())
                .putString("email", v.teacherEmail.trim().lowercase()).apply()
            if (changed) prefs.edit().remove("device_refresh").remove("teacher_refresh").remove("teacher_email").apply()
        }

    /** Class or group name sent with every result from this tablet (e.g. "2BAC SH 3"). */
    var className: String
        get() = prefs.getString("class", "")!!
        set(v) = prefs.edit().putString("class", v.trim()).apply()

    var lastError: String?
        get() = prefs.getString("last_error", null)
        private set(v) = prefs.edit().putString("last_error", v).apply()

    var lastSentAt: Long
        get() = prefs.getLong("last_sent", 0)
        private set(v) = prefs.edit().putLong("last_sent", v).apply()

    private fun client() = FirebaseClient(config, endpoints)

    // ---------- Queue ----------

    fun pending(): List<QuizResult> = synchronized(lock) {
        if (!pendingFile.exists()) emptyList()
        else runCatching { Repository.json.decodeFromString<PendingFile>(pendingFile.readText()).results }.getOrElse { emptyList() }
    }

    private fun savePending(list: List<QuizResult>) = synchronized(lock) {
        pendingFile.writeText(Repository.json.encodeToString(PendingFile.serializer(), PendingFile(list)))
    }

    /** Called when a student finishes a test. Does nothing if online results are not set up. */
    fun enqueue(r: QuizResult) {
        if (!config.isSet) return
        synchronized(lock) { savePending(pending().filterNot { it.id == r.id } + r) }
        schedule(context)
    }

    // ---------- Upload ----------

    private suspend fun deviceToken(): String {
        val c = client()
        val saved = prefs.getString("device_refresh", null)
        val session = saved?.let { runCatching { c.refresh(it, null) }.getOrNull() } ?: c.signUpAnonymously()
        prefs.edit().putString("device_refresh", session.refreshToken).apply()
        return session.idToken
    }

    /** Uploads everything in the queue. Returns the number of results sent. */
    suspend fun flush(): Int = withContext(Dispatchers.IO) {
        uploadMutex.withLock {
            if (!config.isSet) return@withLock 0
            val queue = pending()
            if (queue.isEmpty()) return@withLock 0
            try {
                val token = deviceToken()
                var sent = 0
                for (r in queue) {
                    client().upload(token, r, r.className, deviceName())
                    synchronized(lock) { savePending(pending().filterNot { it.id == r.id }) }
                    sent++
                }
                lastError = null
                lastSentAt = System.currentTimeMillis()
                sent
            } catch (e: CloudException) {
                lastError = e.message
                throw e
            }
        }
    }

    /** Checks the setup by signing in anonymously (used by the “Test connection” button). */
    suspend fun testConnection(): Unit = withContext(Dispatchers.IO) { deviceToken(); Unit }

    // ---------- Teacher (reading results) ----------

    val teacherSignedInEmail: String? get() = prefs.getString("teacher_email", null)

    suspend fun teacherSignIn(email: String, password: String) = withContext(Dispatchers.IO) {
        val s = client().signInWithPassword(email, password)
        prefs.edit().putString("teacher_refresh", s.refreshToken).putString("teacher_email", email.trim().lowercase()).apply()
    }

    fun teacherSignOut() { prefs.edit().remove("teacher_refresh").remove("teacher_email").apply() }

    private fun teacherToken(): String {
        val refresh = prefs.getString("teacher_refresh", null)
            ?: throw CloudException(CloudException.Kind.AUTH, "Please sign in.")
        val s = try { client().refresh(refresh, teacherSignedInEmail) } catch (e: CloudException) {
            if (e.kind == CloudException.Kind.AUTH) teacherSignOut()
            throw e
        }
        prefs.edit().putString("teacher_refresh", s.refreshToken).apply()
        return s.idToken
    }

    suspend fun downloadResults(): List<CloudResult> = withContext(Dispatchers.IO) { client().listResults(teacherToken()) }

    suspend fun deleteOnline(id: String) = withContext(Dispatchers.IO) { client().delete(teacherToken(), id) }

    companion object {
        private val lock = Any()
        private val uploadMutex = Mutex()
        private const val WORK_NAME = "upload-results"

        /** Tests point the app at the local Firebase emulators. Never set in the real app. */
        @Volatile var endpointsOverride: Endpoints? = null

        fun deviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        fun schedule(context: Context) {
            val req = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, req)
        }
    }
}

class UploadWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = try {
        CloudSync(applicationContext).flush()
        Result.success()
    } catch (e: CloudException) {
        // Network problems: try again later. Wrong setup: stop until the teacher fixes it (it is retried on next test).
        if (e.kind == CloudException.Kind.NETWORK || e.kind == CloudException.Kind.OTHER) Result.retry() else Result.failure()
    }
}
