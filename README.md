# Iris (Android)

Android app **Iris** in Kotlin with Jetpack Compose for streaming HLS webcams. Two tabs: **List** (saved webcams as cards, tap for detail view) and **Splitscreen** (2×2 grid with up to four independently chosen streams). Add webcams via FAB; data is stored with DataStore (including splitscreen slot assignments).

## Requirements

- **minSdk**: 24  
- **targetSdk**: 34  
- **Tech**: Kotlin, Jetpack Compose, Material3, ExoPlayer (Media3) for HLS, MVVM, DataStore

## Build & run

1. **Open in Android Studio**  
   Open the project folder; let Gradle sync and use **Run** to install on a device or emulator.

2. **Command line**  
   If the Gradle Wrapper is missing, generate it (with Gradle installed):
   ```bash
   gradle wrapper
   ```
   Then:
   ```bash
   ./gradlew assembleDebug   # Windows: gradlew.bat assembleDebug
   ```
   APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Features

- **List tab**: Saved webcams as cards; tap opens detail (stream, refresh, global mute, fullscreen). Long-press to edit; delete with optional **Undo** in the snackbar.
- **Splitscreen tab**: Four slots; each slot can be filled from a picker (camera names only). Slot choices persist in DataStore (`splitscreen_pos_0` … `splitscreen_pos_3`). Tap a playing tile to open the same detail view.
- **FAB (+)**: Add webcam dialog (Name + Stream URL) with URL validation.
- **Persistence**: DataStore stores name, stream URL, timestamp, and splitscreen slot IDs.
- **Empty list**: Icon and hint to use **+** to add a webcam (no bundled example-import button).
- **ExoPlayer**: HLS (`.m3u8`), custom overlays only (no default transport controls), buffering indicator, lifecycle-aware playback, error overlay with retry.
- **UI**: Material3, primary color Cyan (#00BCD4), top bar **Iris** (with camera count), snackbars for feedback.

## Permissions

- `INTERNET`
- `ACCESS_NETWORK_STATE`

## Project structure

- `app/src/main/java/com/example/webcamstreaming/`
  - **data**: `Webcam`, `WebcamDataStore`, `WebcamRepository`
  - **ui/theme**: Material3 theme (cyan primary)
  - **ui/components**: `VideoPlayer` (ExoPlayer + lifecycle + error/retry), stream status helpers
  - **ui/screens**: `ListScreen`, `DetailScreen`, `SplitscreenScreen`, `AddWebcamDialog`, `MainScreen`
  - **ui/viewmodel**: `WebcamViewModel`, `WebcamViewModelFactory`
- `MainActivity.kt`: Compose setContent, tabs, detail navigation, add/edit dialogs.

## Example webcam streams (HLS)

These are real public-style webcam feeds you can paste manually when adding a camera (URLs may change or require network access):

- **Arlington VA (cam50)**: `https://itsvideo.arlingtonva.us:8011/live/cam50.stream/playlist.m3u8`
- **Arlington VA (cam52)**: `https://itsvideo.arlingtonva.us:8011/live/cam52.stream/playlist.m3u8`
- **Makarska riva (WhatsUpCams)**: `https://cdn-002.whatsupcams.com/hls/hr_makarskariva01.m3u8`
- **explore.org (live)**: `https://outbound-production.explore.org/stream-production-108/.m3u8`
