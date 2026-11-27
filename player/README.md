# Reel Player: a standalone video player (Android)

A minimal video player app. It plays files on your phone and video links (MP4, MKV, WebM, HLS `.m3u8`,
DASH `.mpd`, RTSP), and appears in Android's "Open with" list, so other apps (Reel, browsers, file
managers) can send videos to it.

Build: `./gradlew :player:assembleDebug` → `player/build/outputs/apk/debug/player-debug.apk`
(CI uploads it as the **reel-player-apk** artifact.)

## Features

- **Home**: open a file, open a link, *Continue watching* (with progress), and the videos saved on the phone.
- **Gestures**: swipe left side up/down for brightness, right side for volume, left/right to seek;
  double-tap the left/right third to skip 10 s, the middle to pause; tap to show/hide controls.
- **Controls**: seek bar, ±10 s, speed (0.5×–2×), audio track, subtitle track / off / load a subtitle file
  (SRT, VTT, ASS, TTML), subtitle size, fit / zoom / stretch, rotate, lock, picture-in-picture.
- **Resume**: remembers where you stopped in every video.
- Picks portrait or landscape from the video automatically.

## For apps that open videos in it

Uses the same intent extras as MX Player: `title`, `position` (ms), `subs` (Uri[]), `subs.name` (String[]),
`headers` (String[] of name/value pairs). When it closes it returns `position`, `duration` and `end_by`.
