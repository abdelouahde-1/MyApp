# Reel: a minimal, add-on based media center (Android)

Reel lets you browse movies and series, track what you watch, and play streams from add-ons
you install. It speaks the open media add-on protocol (`manifest.json` + `catalog` / `meta` /
`stream` / `subtitles` resources), so existing add-ons work in it.

Build: `./gradlew :reel:assembleDebug` → `reel/build/outputs/apk/debug/reel-debug.apk`
(CI also uploads it as the **reel-apk** artifact.)

## Features

**Browse**
- **Home**: Continue watching, your watchlist, then every catalog from your add-ons. Pull down to refresh.
- **Discover**: filter by type, catalog and genre in an endless poster grid.
- **Search**: searches every add-on at once, as you type, and keeps your recent searches.
- **Details**: backdrop, rating, runtime, genres, cast, trailer, seasons and episodes.

**Library (the playlist part)**
- **Watchlist**, **Favorites**, **Watched** and **your own lists** (create, rename, delete).
- **Long-press any poster** to add or remove it from any of those without opening it.
- Badges on every poster show what's saved (heart, bookmark, check).
- Smart rules:
  - finishing a movie marks it watched and takes it off your watchlist;
  - finishing an episode marks it watched and puts the next one in *Continue watching*;
  - finishing the last episode marks the whole series watched;
  - mark single episodes or a whole season watched with one tap.
- The big button always does the right thing: *Resume*, *Play S2E4* (next unwatched), or *Play*.
- Sort by recently added, title or year, and filter movies or series.
- Back up and restore the library as a JSON file.

**Playback**
- Built-in player (HLS, DASH, MP4, …) with resume, add-on subtitles and a preferred subtitle language.
- Sources are grouped into quality tabs (4K / 1080p / 720p / SD / Other) with counts. The list opens on your
  default quality (1080p unless changed in Settings) and can be sorted by size or seeders. Each source shows its
  quality, size and seeders at a glance.
- One tap to open any stream in another player (VLC, MX Player, …), or make that the default.
- "Up next" card when an episode ends.

**Add-ons**
- Install by pasting a link or opening a `stremio://…/manifest.json` link.
- Enable/disable, reorder (the first add-on wins for metadata) and remove add-ons.

## Stream sources

Reel ships with one metadata add-on (Cinemeta: posters, details and episodes). It has **no stream
sources built in**: to play something, install an add-on that provides streams for content you are
allowed to watch, such as your own media server or free/public-domain sources.
Torrent (`infoHash`) sources are listed too: tapping one opens it as a magnet link in another installed
app (Stremio, a torrent client, …), because Reel's own player only plays direct links. Add-ons set up with a
debrid service return direct links, which play inside Reel. Torrent sources can be hidden in Settings.

## Code map

| Path | What |
|---|---|
| `data/Models.kt` | Add-on protocol models, parsed leniently |
| `data/AddonClient.kt` | HTTP + URL building, parallel stream/subtitle/search queries, caching |
| `data/AddonRepository.kt` | Installed add-ons (persisted) |
| `data/Library.kt` | Library state + `LibraryRules` (all watchlist/watched/progress rules) |
| `ui/screens/*` | Home, Discover, Search, Library, Detail (+ source picker), Player, Settings |
| `src/test` | Protocol and library-rule tests, plus a Robolectric UI flow test with screenshots |
