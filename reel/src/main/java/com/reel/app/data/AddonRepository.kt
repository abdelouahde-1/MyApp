package com.reel.app.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File

@Serializable
private data class StoredAddon(val url: String, val enabled: Boolean = true, val manifest: String)

/** Installed add-ons, in priority order (meta is taken from the first one that has it). */
class AddonRepository(
    private val file: File,
    private val http: Http,
    private val scope: CoroutineScope,
    defaults: List<String> = DEFAULT_ADDONS,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val raw = java.util.concurrent.ConcurrentHashMap<String, String>()
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val io = kotlinx.coroutines.Dispatchers.IO.limitedParallelism(1)
    private val _addons = MutableStateFlow<List<Addon>>(emptyList())
    val addons: StateFlow<List<Addon>> = _addons.asStateFlow()

    init {
        val stored = runCatching { json.decodeFromString<List<StoredAddon>>(file.readText()) }.getOrNull()
        if (stored != null) {
            _addons.value = stored.mapNotNull { s ->
                runCatching {
                    raw[s.url] = s.manifest
                    Addon(s.url, Manifest.parse(json.parseToJsonElement(s.manifest) as JsonObject), s.enabled)
                }.getOrNull()
            }
        }
        scope.launch {
            if (stored == null) defaults.forEach { runCatching { install(it) } }
            else refreshAll()
        }
    }

    val enabled get() = addons.value.filter { it.enabled }

    private suspend fun fetch(url: String): Addon {
        val text = http.get(url)
        val manifest = Manifest.parse(json.parseToJsonElement(text) as JsonObject)
        raw[url] = text
        return Addon(url, manifest)
    }

    /** Installs (or updates) an add-on; returns its name. */
    suspend fun install(input: String): String {
        val url = AddonUrls.normalize(input)
        val addon = fetch(url)
        _addons.update { list ->
            val i = list.indexOfFirst { it.transportUrl == url || it.manifest.id == addon.manifest.id }
            if (i >= 0) list.toMutableList().also { it[i] = addon.copy(enabled = list[i].enabled) } else list + addon
        }
        save()
        return addon.manifest.name
    }

    private suspend fun refreshAll() {
        var changed = false
        addons.value.forEach { a ->
            runCatching { fetch(a.transportUrl) }.onSuccess { fresh ->
                _addons.update { list -> list.map { if (it.transportUrl == a.transportUrl) fresh.copy(enabled = it.enabled) else it } }
                changed = true
            }
        }
        if (changed) save()
    }

    fun remove(url: String) { _addons.update { l -> l.filterNot { it.transportUrl == url } }; save() }

    fun setEnabled(url: String, enabled: Boolean) {
        _addons.update { l -> l.map { if (it.transportUrl == url) it.copy(enabled = enabled) else it } }
        save()
    }

    fun move(url: String, delta: Int) {
        _addons.update { l ->
            val i = l.indexOfFirst { it.transportUrl == url }
            val j = i + delta
            if (i < 0 || j !in l.indices) l else l.toMutableList().apply { add(j, removeAt(i)) }
        }
        save()
    }

    private fun save() {
        val list = addons.value.map { StoredAddon(it.transportUrl, it.enabled, raw[it.transportUrl] ?: "{}") }
        val text = json.encodeToString(kotlinx.serialization.builtins.ListSerializer(StoredAddon.serializer()), list)
        scope.launch(io) { runCatching { file.writeText(text) } }
    }

    companion object {
        /** Metadata only (catalogs, posters, episodes). Stream sources are added by the user. */
        val DEFAULT_ADDONS = listOf("https://v3-cinemeta.strem.io/manifest.json")
    }
}
