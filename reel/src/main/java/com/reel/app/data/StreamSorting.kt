package com.reel.app.data

/** Video quality buckets used to group sources, best first. */
enum class Quality(val label: String) {
    UHD("4K"), FHD("1080p"), HD("720p"), SD("SD"), Unknown("Other");

    companion object {
        fun fromLabel(label: String) = entries.firstOrNull { it.label == label }
    }
}

enum class StreamSort(val label: String) {
    Default("Best match"), SizeDesc("Largest first"), SizeAsc("Smallest first"), Seeders("Most seeders");

    companion object {
        fun fromName(name: String) = entries.firstOrNull { it.name == name } ?: Default
    }
}

/** What can be read from a source's name/description (Torrentio-style: "1080p", "💾 2.1 GB", "👤 54"). */
data class StreamInfo(val quality: Quality, val sizeBytes: Long?, val seeders: Int?)

object StreamSorting {
    private val uhd = Regex("""(?i)\b(2160p|4k|uhd)\b""")
    private val fhd = Regex("""(?i)\b1080[pi]?\b""")
    private val hd = Regex("""(?i)\b720p?\b""")
    private val sd = Regex("""(?i)\b(480p|576p|360p|240p|sd|dvdrip|dvdscr|cam|hdcam|telesync)\b""")
    private val size = Regex("""(?i)(\d+(?:[.,]\d+)?)\s*(TB|GB|MB|KB)\b""")
    private val seeders = Regex("""👤\s*(\d+)""")

    fun info(s: Stream): StreamInfo {
        // The name usually carries the quality ("Torrentio\n1080p"); the title has file name, size and seeders.
        val name = s.name.orEmpty()
        val text = name + "\n" + s.title.orEmpty()
        val quality = qualityOf(name) ?: qualityOf(text) ?: Quality.Unknown
        val bytes = s.videoSize ?: size.find(s.title.orEmpty().substringAfter("💾", s.title.orEmpty()))?.let { m ->
            val n = m.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return@let null
            val mult = when (m.groupValues[2].uppercase()) { "TB" -> 1e12; "GB" -> 1e9; "MB" -> 1e6; else -> 1e3 }
            (n * mult).toLong()
        }
        return StreamInfo(quality, bytes, seeders.find(text)?.groupValues?.get(1)?.toIntOrNull())
    }

    private fun qualityOf(t: String): Quality? = when {
        uhd.containsMatchIn(t) -> Quality.UHD
        fhd.containsMatchIn(t) -> Quality.FHD
        hd.containsMatchIn(t) -> Quality.HD
        sd.containsMatchIn(t) -> Quality.SD
        else -> null
    }

    /** Sorts sources; ties (and unknown sizes) keep the add-on's own order. Unknown sizes go last. */
    fun sort(streams: List<Stream>, sort: StreamSort): List<Stream> {
        if (sort == StreamSort.Default) return streams
        val infos = streams.associateWith { info(it) }
        return when (sort) {
            StreamSort.SizeDesc -> streams.sortedWith(compareBy(nullsLast()) { infos[it]!!.sizeBytes?.let { b -> -b } })
            StreamSort.SizeAsc -> streams.sortedWith(compareBy(nullsLast()) { infos[it]!!.sizeBytes })
            StreamSort.Seeders -> streams.sortedWith(compareBy(nullsLast()) { infos[it]!!.seeders?.let { n -> -n } })
            StreamSort.Default -> streams
        }
    }

    /** The tab to open on: the preferred quality if there are sources for it, else the best quality available. */
    fun initialQuality(available: Set<Quality>, preferred: Quality?): Quality? =
        preferred?.takeIf { it in available } ?: Quality.entries.firstOrNull { it in available }

    fun formatSize(bytes: Long): String = when {
        bytes >= 1e9 -> "%.1f GB".format(bytes / 1e9)
        bytes >= 1e6 -> "%.0f MB".format(bytes / 1e6)
        else -> "%.0f KB".format(bytes / 1e3)
    }
}
