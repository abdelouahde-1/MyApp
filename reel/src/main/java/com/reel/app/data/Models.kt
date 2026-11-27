package com.reel.app.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/*
 * Models for the open media add-on protocol (manifest + catalog/meta/stream/subtitles resources).
 * Add-ons are written by many different people and their JSON is not always consistent
 * (numbers as strings, `genre` vs `genres`, ...), so everything is parsed by hand and leniently.
 */

data class ExtraProp(val name: String, val isRequired: Boolean, val options: List<String>)

data class CatalogDef(
    val type: String,
    val id: String,
    val name: String,
    val extra: List<ExtraProp>,
) {
    val supportsSearch get() = extra.any { it.name == "search" }
    val genres get() = extra.firstOrNull { it.name == "genre" }?.options.orEmpty()
    val supportsSkip get() = extra.any { it.name == "skip" }
    /** Catalogs with required extras (search-only catalogs, ...) cannot be shown as plain rows. */
    val browsable get() = extra.none { it.isRequired }
}

data class ResourceDef(val name: String, val types: List<String>?, val idPrefixes: List<String>?)

data class Manifest(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val logo: String?,
    val types: List<String>,
    val catalogs: List<CatalogDef>,
    val resources: List<ResourceDef>,
    val idPrefixes: List<String>?,
) {
    fun supports(resource: String, type: String, id: String): Boolean {
        val r = resources.firstOrNull { it.name == resource } ?: return false
        val types = r.types ?: this.types
        if (types.isNotEmpty() && type !in types) return false
        val prefixes = r.idPrefixes ?: idPrefixes
        return prefixes.isNullOrEmpty() || prefixes.any { id.startsWith(it) }
    }

    companion object {
        fun parse(o: JsonObject): Manifest {
            val types = o.strList("types")
            val resources = (o["resources"] as? JsonArray).orEmpty().mapNotNull { el ->
                when (el) {
                    is JsonPrimitive -> el.contentOrNull?.let { ResourceDef(it, null, null) }
                    is JsonObject -> el.str("name")?.let {
                        ResourceDef(it, el.strListOrNull("types"), el.strListOrNull("idPrefixes"))
                    }
                    else -> null
                }
            }
            val catalogs = (o["catalogs"] as? JsonArray).orEmpty().mapNotNull { el ->
                val c = el as? JsonObject ?: return@mapNotNull null
                val extras = (c["extra"] as? JsonArray)?.mapNotNull { e ->
                    val eo = e as? JsonObject ?: return@mapNotNull null
                    ExtraProp(eo.str("name") ?: return@mapNotNull null, eo.bool("isRequired"), eo.strList("options"))
                } ?: run {
                    // Legacy form: extraSupported / extraRequired arrays of names.
                    val required = c.strList("extraRequired").toSet()
                    c.strList("extraSupported").map { ExtraProp(it, it in required, emptyList()) }
                }
                CatalogDef(
                    type = c.str("type") ?: return@mapNotNull null,
                    id = c.str("id") ?: return@mapNotNull null,
                    name = c.str("name") ?: c.str("id")!!,
                    extra = extras,
                )
            }
            return Manifest(
                id = o.str("id") ?: error("manifest has no id"),
                name = o.str("name") ?: o.str("id")!!,
                version = o.str("version") ?: "",
                description = o.str("description") ?: "",
                logo = o.str("logo"),
                types = types,
                catalogs = catalogs,
                resources = resources,
                idPrefixes = o.strListOrNull("idPrefixes"),
            )
        }
    }
}

data class MetaPreview(
    val id: String,
    val type: String,
    val name: String,
    val poster: String?,
    val posterShape: String,
    val background: String?,
    val releaseInfo: String?,
    val imdbRating: String?,
    val description: String?,
    val genres: List<String>,
) {
    companion object {
        fun parse(o: JsonObject, fallbackType: String): MetaPreview? {
            val id = o.str("id") ?: o.str("imdb_id") ?: return null
            return MetaPreview(
                id = id,
                type = o.str("type") ?: fallbackType,
                name = o.str("name") ?: return null,
                poster = o.str("poster"),
                posterShape = o.str("posterShape") ?: "poster",
                background = o.str("background"),
                releaseInfo = o.str("releaseInfo") ?: o.str("year"),
                imdbRating = o.str("imdbRating")?.takeIf { it.isNotBlank() && it != "0" },
                description = o.str("description"),
                genres = o.strList("genres").ifEmpty { o.strList("genre") },
            )
        }
    }
}

data class Video(
    val id: String,
    val title: String,
    val season: Int?,
    val episode: Int?,
    val released: String?,
    val thumbnail: String?,
    val overview: String?,
) {
    val label: String
        get() = if (season != null && episode != null) "S${season}E${episode}" else title

    companion object {
        fun parse(o: JsonObject): Video? {
            val season = o.int("season")
            val episode = o.int("episode") ?: o.int("number")
            return Video(
                id = o.str("id") ?: return null,
                title = o.str("title") ?: o.str("name") ?: (if (episode != null) "Episode $episode" else "Video"),
                season = season,
                episode = episode,
                released = o.str("released") ?: o.str("firstAired"),
                thumbnail = o.str("thumbnail"),
                overview = o.str("overview") ?: o.str("description"),
            )
        }
    }
}

data class Meta(
    val preview: MetaPreview,
    val logo: String?,
    val runtime: String?,
    val cast: List<String>,
    val director: List<String>,
    val trailerYtIds: List<String>,
    val videos: List<Video>,
) {
    val id get() = preview.id
    val type get() = preview.type
    val name get() = preview.name

    /** Seasons in display order, specials (season 0) last. */
    val seasons: List<Int>
        get() = videos.mapNotNull { it.season }.distinct().sortedWith(compareBy({ it == 0 }, { it }))

    fun episodes(season: Int) = videos.filter { it.season == season }.sortedBy { it.episode ?: 0 }

    /** The episode after [videoId] in watch order (crossing into the next season), if any. */
    fun nextVideo(videoId: String): Video? {
        val ordered = seasons.flatMap { episodes(it) }.ifEmpty { videos }
        val i = ordered.indexOfFirst { it.id == videoId }
        return if (i >= 0) ordered.getOrNull(i + 1) else null
    }

    companion object {
        fun parse(o: JsonObject, fallbackType: String): Meta? {
            val preview = MetaPreview.parse(o, fallbackType) ?: return null
            val links = (o["links"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
            fun linksOf(category: String) = links.filter { it.str("category") == category }.mapNotNull { it.str("name") }
            val trailers = buildList {
                (o["trailers"] as? JsonArray).orEmpty().forEach { (it as? JsonObject)?.str("source")?.let(::add) }
                (o["trailerStreams"] as? JsonArray).orEmpty().forEach { (it as? JsonObject)?.str("ytId")?.let(::add) }
            }.distinct()
            return Meta(
                preview = preview,
                logo = o.str("logo"),
                runtime = o.str("runtime"),
                cast = o.strList("cast").ifEmpty { linksOf("Cast") },
                director = o.strList("director").ifEmpty { linksOf("Directors") },
                trailerYtIds = trailers,
                videos = (o["videos"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.let(Video::parse) },
            )
        }
    }
}

data class Subtitle(val id: String, val url: String, val lang: String) {
    companion object {
        fun parse(o: JsonObject): Subtitle? {
            val url = o.str("url") ?: return null
            return Subtitle(o.str("id") ?: url, url, o.str("lang") ?: "und")
        }
    }
}

data class Stream(
    val addonName: String,
    val name: String?,
    val title: String?,
    val url: String?,
    val ytId: String?,
    val externalUrl: String?,
    val infoHash: String?,
    val subtitles: List<Subtitle>,
    val headers: Map<String, String>,
    /** File size in bytes when the add-on states it (behaviorHints.videoSize). */
    val videoSize: Long? = null,
) {
    enum class Kind { Direct, YouTube, External, Unsupported }

    val kind: Kind
        get() = when {
            url != null -> Kind.Direct
            ytId != null -> Kind.YouTube
            externalUrl != null -> Kind.External
            else -> Kind.Unsupported
        }

    /** Short label (add-ons usually put source + quality in `name`). */
    val headline get() = (name ?: addonName).replace('\n', ' ').trim()
    /** Longer description (file name, size, ...), may span several lines. */
    val details get() = title.orEmpty().trim()

    companion object {
        fun parse(o: JsonObject, addonName: String): Stream {
            val hints = o["behaviorHints"] as? JsonObject
            val headers = ((hints?.get("proxyHeaders") as? JsonObject)?.get("request") as? JsonObject)
                ?.mapNotNull { (k, v) -> (v as? JsonPrimitive)?.contentOrNull?.let { k to it } }?.toMap().orEmpty()
            return Stream(
                addonName = addonName,
                name = o.str("name"),
                title = o.str("title") ?: o.str("description"),
                url = o.str("url"),
                ytId = o.str("ytId"),
                externalUrl = o.str("externalUrl"),
                infoHash = o.str("infoHash"),
                subtitles = (o["subtitles"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.let(Subtitle::parse) },
                headers = headers,
                videoSize = (hints?.get("videoSize") as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()?.toLong(),
            )
        }
    }
}

// ---- lenient JSON helpers ----

internal fun JsonObject.str(key: String): String? = when (val v = this[key]) {
    is JsonPrimitive -> v.contentOrNull?.takeIf { it.isNotEmpty() }
    else -> null
}

internal fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)?.let { it.intOrNull ?: it.contentOrNull?.toDoubleOrNull()?.toInt() }

internal fun JsonObject.bool(key: String): Boolean = (this[key] as? JsonPrimitive)?.contentOrNull == "true"

internal fun JsonObject.strListOrNull(key: String): List<String>? = when (val v = this[key]) {
    is JsonArray -> v.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
    is JsonPrimitive -> v.contentOrNull?.let { listOf(it) }
    else -> null
}

internal fun JsonObject.strList(key: String): List<String> = strListOrNull(key).orEmpty()
