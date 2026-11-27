package com.reel.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** An installed add-on: where it lives plus its manifest. */
data class Addon(val transportUrl: String, val manifest: Manifest, val enabled: Boolean = true) {
    val baseUrl get() = AddonUrls.base(transportUrl)
}

object AddonUrls {
    /** Accepts `stremio://host/manifest.json`, a bare base URL or a full manifest URL. */
    fun normalize(input: String): String {
        var u = input.trim()
        if (u.startsWith("stremio://")) u = "https://" + u.removePrefix("stremio://")
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://$u"
        u = u.trimEnd('/')
        if (!u.endsWith("/manifest.json")) u += "/manifest.json"
        return u
    }

    fun base(transportUrl: String) = transportUrl.removeSuffix("/manifest.json").trimEnd('/')

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    fun resource(base: String, resource: String, type: String, id: String, extra: Map<String, String> = emptyMap()): String {
        val extraPart = extra.filterValues { it.isNotEmpty() }.entries
            .joinToString("&") { (k, v) -> "${enc(k)}=${enc(v)}" }
            .let { if (it.isEmpty()) "" else "/$it" }
        // Ids keep their ':' separators (tt123:1:2): that is what add-ons expect, and some don't decode %3A.
        return "$base/$resource/${enc(type)}/${enc(id).replace("%3A", ":")}$extraPart.json"
    }
}

interface Http {
    suspend fun get(url: String): String
}

class UrlConnectionHttp : Http {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000
        c.readTimeout = 20_000
        c.setRequestProperty("Accept", "application/json")
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) Reel/1.0")
        try {
            if (c.responseCode !in 200..299) error("HTTP ${c.responseCode} for $url")
            c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }
}

data class CatalogPage(val addon: Addon, val catalog: CatalogDef, val items: List<MetaPreview>)

data class StreamGroup(val addonName: String, val streams: List<Stream>, val error: String? = null)

class AddonClient(private val http: Http = UrlConnectionHttp()) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val metaCache = object : LinkedHashMap<String, Meta>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Meta>?) = size > 80
    }

    private suspend fun getObject(url: String): JsonObject =
        json.parseToJsonElement(http.get(url)) as? JsonObject ?: error("Not a JSON object: $url")

    suspend fun fetchManifest(url: String): Manifest = Manifest.parse(getObject(url))

    private val catalogCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, List<MetaPreview>>>()

    /** One catalog page. Results are kept for 30 minutes so going back to a screen is instant. */
    suspend fun catalog(addon: Addon, catalog: CatalogDef, extra: Map<String, String> = emptyMap()): List<MetaPreview> {
        val url = AddonUrls.resource(addon.baseUrl, "catalog", catalog.type, catalog.id, extra)
        catalogCache[url]?.let { (at, items) -> if (System.currentTimeMillis() - at < 30 * 60_000) return items }
        val o = getObject(url)
        val items = (o["metas"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.let { m -> MetaPreview.parse(m, catalog.type) } }
            .distinctBy { it.id }
        catalogCache[url] = System.currentTimeMillis() to items
        return items
    }

    fun clearCache() { catalogCache.clear(); synchronized(metaCache) { metaCache.clear() } }

    /** Full metadata, from the first enabled add-on that has it. */
    suspend fun meta(addons: List<Addon>, type: String, id: String): Meta? {
        val key = "$type/$id"
        synchronized(metaCache) { metaCache[key] }?.let { return it }
        for (a in addons.filter { it.enabled && it.manifest.supports("meta", type, id) }) {
            val meta = runCatching {
                val o = getObject(AddonUrls.resource(a.baseUrl, "meta", type, id))
                (o["meta"] as? JsonObject)?.let { Meta.parse(it, type) }
            }.getOrNull()
            if (meta != null) {
                synchronized(metaCache) { metaCache[key] = meta }
                return meta
            }
        }
        return null
    }

    /** Streams from every add-on that offers them, queried in parallel. */
    suspend fun streams(addons: List<Addon>, type: String, videoId: String): List<StreamGroup> = coroutineScope {
        addons.filter { it.enabled && it.manifest.supports("stream", type, videoId) }.map { a ->
            async {
                runCatching {
                    val o = getObject(AddonUrls.resource(a.baseUrl, "stream", type, videoId))
                    StreamGroup(a.manifest.name, (o["streams"] as? JsonArray).orEmpty().mapNotNull {
                        (it as? JsonObject)?.let { s -> Stream.parse(s, a.manifest.name) }
                    })
                }.getOrElse { StreamGroup(a.manifest.name, emptyList(), it.message ?: "Failed") }
            }
        }.awaitAll()
    }

    suspend fun subtitles(addons: List<Addon>, type: String, videoId: String): List<Subtitle> = coroutineScope {
        addons.filter { it.enabled && it.manifest.supports("subtitles", type, videoId) }.map { a ->
            async {
                runCatching {
                    val o = getObject(AddonUrls.resource(a.baseUrl, "subtitles", type, videoId))
                    (o["subtitles"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.let(Subtitle::parse) }
                }.getOrDefault(emptyList())
            }
        }.awaitAll().flatten()
    }

    /** Searches every catalog that supports search; one page per catalog. */
    suspend fun search(addons: List<Addon>, query: String): List<CatalogPage> = coroutineScope {
        addons.filter { it.enabled }.flatMap { a ->
            a.manifest.catalogs.filter { it.supportsSearch }.map { c ->
                async { runCatching { CatalogPage(a, c, catalog(a, c, mapOf("search" to query))) }.getOrNull() }
            }
        }.awaitAll().filterNotNull().filter { it.items.isNotEmpty() }
    }
}
