package com.englishquiz.app.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Base64

/** The teacher's Firebase project. Only the project ID and the Web API key are needed. */
data class CloudConfig(
    val projectId: String = "",
    val apiKey: String = "",
    val teacherEmail: String = "",
) {
    val isSet: Boolean get() = projectId.isNotBlank() && apiKey.isNotBlank()

    /** A short code the teacher copies from one tablet and pastes on the others. */
    fun toSetupCode(): String {
        val json = buildJsonObject { put("p", projectId); put("k", apiKey); put("e", teacherEmail) }.toString()
        return "EQ1-" + Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())
    }

    /** Firestore security rules: tablets may only add results; only the teacher can read or delete them. */
    fun securityRules(): String = """
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            match /results/{resultId} {
              allow create: if request.auth != null;
              allow read, delete: if request.auth != null
                && request.auth.token.email == "${teacherEmail.trim().lowercase().ifBlank { "YOUR_EMAIL@gmail.com" }}";
            }
          }
        }
    """.trimIndent()

    companion object {
        fun fromSetupCode(code: String): CloudConfig? = runCatching {
            val raw = code.trim().removePrefix("EQ1-")
            val obj = Repository.json.parseToJsonElement(String(Base64.getUrlDecoder().decode(raw))).jsonObject
            CloudConfig(obj.str("p"), obj.str("k"), obj.str("e")).takeIf { it.isSet }
        }.getOrNull()
    }
}

/** A result as stored online (it also records which class/tablet it came from). */
data class CloudResult(val result: QuizResult, val className: String, val device: String)

data class AuthSession(val idToken: String, val refreshToken: String, val expiresAtMillis: Long, val email: String?) {
    fun isFresh(now: Long = System.currentTimeMillis()) = now < expiresAtMillis - 60_000
}

class CloudException(val kind: Kind, message: String) : Exception(message) {
    enum class Kind { NETWORK, BAD_CONFIG, AUTH, PERMISSION, OTHER }
}

/** Where the Firebase services live. Tests point this at the local emulators. */
data class Endpoints(val auth: String, val token: String, val firestore: String) {
    companion object {
        val PRODUCTION = Endpoints(
            auth = "https://identitytoolkit.googleapis.com",
            token = "https://securetoken.googleapis.com",
            firestore = "https://firestore.googleapis.com",
        )

        fun emulator(host: String = "127.0.0.1", authPort: Int = 9099, firestorePort: Int = 8080) = Endpoints(
            auth = "http://$host:$authPort/identitytoolkit.googleapis.com",
            token = "http://$host:$authPort/securetoken.googleapis.com",
            firestore = "http://$host:$firestorePort",
        )
    }
}

/** Minimal Firebase Auth + Cloud Firestore client over the public REST API (no Google SDK needed). */
class FirebaseClient(private val config: CloudConfig, private val endpoints: Endpoints = Endpoints.PRODUCTION) {

    private val docsBase get() = "${endpoints.firestore}/v1/projects/${config.projectId.trim()}/databases/(default)/documents"
    private val key get() = enc(config.apiKey.trim())

    /** URL-encodes a query value; Google APIs need spaces as %20, not "+". */
    private fun enc(v: String) = URLEncoder.encode(v, "UTF-8").replace("+", "%20")

    // ---------- Auth ----------

    fun signUpAnonymously(): AuthSession =
        session(post("${endpoints.auth}/v1/accounts:signUp?key=$key", buildJsonObject { put("returnSecureToken", true) }.toString()))

    fun signInWithPassword(email: String, password: String): AuthSession = session(
        post(
            "${endpoints.auth}/v1/accounts:signInWithPassword?key=$key",
            buildJsonObject { put("email", email.trim()); put("password", password); put("returnSecureToken", true) }.toString(),
        ),
    )

    /** Only used by tests and the setup check: creates an email/password account. */
    fun signUpWithPassword(email: String, password: String): AuthSession = session(
        post(
            "${endpoints.auth}/v1/accounts:signUp?key=$key",
            buildJsonObject { put("email", email.trim()); put("password", password); put("returnSecureToken", true) }.toString(),
        ),
    )

    fun refresh(refreshToken: String, email: String?): AuthSession {
        val body = "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8")
        val o = post("${endpoints.token}/v1/token?key=$key", body, "application/x-www-form-urlencoded")
        return AuthSession(
            idToken = o.str("id_token"),
            refreshToken = o.str("refresh_token").ifBlank { refreshToken },
            expiresAtMillis = System.currentTimeMillis() + (o.str("expires_in").toLongOrNull() ?: 3600) * 1000,
            email = email,
        )
    }

    private fun session(o: JsonObject) = AuthSession(
        idToken = o.str("idToken"),
        refreshToken = o.str("refreshToken"),
        expiresAtMillis = System.currentTimeMillis() + (o.str("expiresIn").toLongOrNull() ?: 3600) * 1000,
        email = o.str("email").ifBlank { null },
    )

    // ---------- Firestore ----------

    /** Uploads one result. Uploading the same result twice is harmless (it is only stored once). */
    fun upload(idToken: String, r: QuizResult, className: String, device: String) {
        val url = "$docsBase/results?documentId=" + enc(r.id)
        try {
            post(url, toDocument(r, className, device).toString(), bearer = idToken)
        } catch (e: HttpError) {
            if (e.code == 409) return // ALREADY_EXISTS: sent before
            throw e.toCloud()
        }
    }

    fun listResults(idToken: String, max: Int = 5000): List<CloudResult> {
        val out = mutableListOf<CloudResult>()
        var pageToken: String? = null
        do {
            var url = "$docsBase/results?pageSize=300&orderBy=" + enc("timestamp desc")
            if (pageToken != null) url += "&pageToken=" + enc(pageToken)
            val o = request("GET", url, null, bearer = idToken)
            o["documents"]?.jsonArray?.forEach { d -> fromDocument(d.jsonObject)?.let(out::add) }
            pageToken = o.str("nextPageToken").ifBlank { null }
        } while (pageToken != null && out.size < max)
        return out
    }

    fun delete(idToken: String, resultId: String) {
        request("DELETE", "$docsBase/results/" + enc(resultId), null, bearer = idToken)
    }

    // ---------- Encoding ----------

    private fun s(v: String) = buildJsonObject { put("stringValue", v) }
    private fun i(v: Long) = buildJsonObject { put("integerValue", v.toString()) }
    private fun b(v: Boolean) = buildJsonObject { put("booleanValue", v) }

    internal fun toDocument(r: QuizResult, className: String, device: String): JsonObject = buildJsonObject {
        put("fields", buildJsonObject {
            put("studentName", s(r.studentName))
            put("className", s(className))
            put("device", s(device))
            put("unitId", s(r.unitId))
            put("unitTitle", s(r.unitTitle))
            put("skill", s(r.skill?.name ?: ""))
            put("difficulty", s(r.difficulty.name))
            put("focus", s(r.focusLabel))
            put("level", s(r.difficulty.label))
            put("score", i(r.score.toLong()))
            put("total", i(r.total.toLong()))
            put("percent", i(r.percent.toLong()))
            put("timestamp", i(r.timestamp))
            put("date", buildJsonObject { put("timestampValue", java.time.Instant.ofEpochMilli(r.timestamp).toString()) })
            put("answers", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", buildJsonArray {
                        r.answers.forEach { a ->
                            add(buildJsonObject {
                                put("mapValue", buildJsonObject {
                                    put("fields", buildJsonObject {
                                        put("prompt", s(a.prompt))
                                        put("given", s(a.givenAnswer))
                                        put("correctAnswer", s(a.correctAnswer))
                                        put("correct", b(a.correct))
                                        put("explanation", s(a.explanation))
                                        put("skill", s(a.skill?.name ?: ""))
                                    })
                                })
                            })
                        }
                    })
                })
            })
        })
    }

    internal fun fromDocument(d: JsonObject): CloudResult? = runCatching {
        val f = d["fields"]!!.jsonObject
        fun str(k: String) = f[k]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        fun int(k: String) = f[k]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
        val answers = f["answers"]?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray.orEmpty().map { v ->
            val m = v.jsonObject["mapValue"]!!.jsonObject["fields"]!!.jsonObject
            fun ms(k: String) = m[k]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            AnswerRecord(
                prompt = ms("prompt"), givenAnswer = ms("given"), correctAnswer = ms("correctAnswer"),
                correct = m["correct"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false,
                explanation = ms("explanation"),
                skill = ms("skill").takeIf { it.isNotBlank() }?.let { runCatching { Skill.valueOf(it) }.getOrNull() },
            )
        }
        CloudResult(
            result = QuizResult(
                id = d.str("name").substringAfterLast('/'),
                studentName = str("studentName"),
                unitId = str("unitId"),
                unitTitle = str("unitTitle"),
                skill = str("skill").takeIf { it.isNotBlank() }?.let { runCatching { Skill.valueOf(it) }.getOrNull() },
                difficulty = runCatching { Difficulty.valueOf(str("difficulty")) }.getOrDefault(Difficulty.MEDIUM),
                timestamp = int("timestamp"),
                score = int("score").toInt(),
                total = int("total").toInt(),
                answers = answers,
                className = str("className"),
            ),
            className = str("className"),
            device = str("device"),
        )
    }.getOrNull()

    // ---------- HTTP ----------

    private class HttpError(val code: Int, val body: String) : IOException("HTTP $code: $body") {
        fun toCloud(): CloudException {
            val b = body.uppercase()
            return when {
                "API_KEY_INVALID" in b || "API KEY NOT VALID" in b || code == 404 && "PROJECT" in b ->
                    CloudException(CloudException.Kind.BAD_CONFIG, "The project ID or API key is wrong.")
                "INVALID_LOGIN_CREDENTIALS" in b || "INVALID_PASSWORD" in b || "EMAIL_NOT_FOUND" in b ->
                    CloudException(CloudException.Kind.AUTH, "Wrong email or password.")
                "ADMIN_ONLY_OPERATION" in b || "OPERATION_NOT_ALLOWED" in b ->
                    CloudException(CloudException.Kind.BAD_CONFIG, "Sign-in method not enabled: turn on Anonymous and Email/Password in Firebase Authentication.")
                "TOKEN_EXPIRED" in b || "INVALID_REFRESH_TOKEN" in b || "USER_NOT_FOUND" in b ->
                    CloudException(CloudException.Kind.AUTH, "Your sign-in expired. Please sign in again.")
                code == 403 || "PERMISSION_DENIED" in b ->
                    CloudException(CloudException.Kind.PERMISSION, "Permission denied. Check the Firestore security rules and the teacher email.")
                code == 404 ->
                    CloudException(CloudException.Kind.BAD_CONFIG, "Firestore database not found. Create it in the Firebase console.")
                else -> CloudException(CloudException.Kind.OTHER, "Server error ($code).")
            }
        }
    }

    private fun post(url: String, body: String, contentType: String = "application/json", bearer: String? = null): JsonObject =
        request("POST", url, body, contentType, bearer)

    private fun request(method: String, url: String, body: String?, contentType: String = "application/json", bearer: String? = null): JsonObject {
        val conn = try {
            (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("Accept", "application/json")
                if (bearer != null) setRequestProperty("Authorization", "Bearer $bearer")
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "$contentType; charset=utf-8")
                    outputStream.use { it.write(body.toByteArray()) }
                }
            }
        } catch (e: IOException) {
            throw CloudException(CloudException.Kind.NETWORK, "No internet connection.")
        }
        try {
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val err = HttpError(code, text)
                if (method == "POST" && code == 409) throw err
                throw err.toCloud()
            }
            return if (text.isBlank()) JsonObject(emptyMap()) else Repository.json.parseToJsonElement(text).jsonObject
        } catch (e: HttpError) {
            throw e
        } catch (e: CloudException) {
            throw e
        } catch (e: IOException) {
            throw CloudException(CloudException.Kind.NETWORK, "No internet connection.")
        } finally {
            conn.disconnect()
        }
    }
}

private fun JsonObject.str(k: String): String = (this[k] as? JsonPrimitive)?.content.orEmpty()
