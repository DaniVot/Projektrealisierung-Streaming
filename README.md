# Webcam Streaming (Android)

Android app in Kotlin with Jetpack Compose for streaming HLS webcams. Two tabs: **List** (scrollable saved webcams, tap for fullscreen stream) and **Splitscreen** (2×2 grid of 4 streams). Add webcams via FAB; data is stored with DataStore.

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

- **List tab**: Saved webcams as cards; tap one to open fullscreen live stream.
- **Splitscreen tab**: Up to 4 webcams in a 2×2 grid; empty slots show “No camera”.
- **FAB (+)**: Add webcam dialog (Name + Stream URL) with URL validation.
- **Persistence**: DataStore stores name, stream URL, and timestamp.
- **Empty state**: When no webcams are saved, the app shows a help screen with a button to add 3 example streams.
- **ExoPlayer**: HLS (.m3u8), buffering indicator, lifecycle-aware (pause in background), error overlay with Retry.
- **UI**: Material3, primary color Cyan (#00BCD4), top bar “Webcam Streaming”, Snackbars for errors/success.

## Permissions

- `INTERNET`
- `ACCESS_NETWORK_STATE`

## Project structure

- `app/src/main/java/com/example/webcamstreaming/`
  - **data**: `Webcam`, `WebcamDataStore`, `WebcamRepository`
  - **ui/theme**: Material3 theme (cyan primary)
  - **ui/components**: `VideoPlayer` (ExoPlayer + lifecycle + error/retry)
  - **ui/screens**: `ListScreen`, `DetailScreen`, `SplitscreenScreen`, `AddWebcamDialog`, `MainScreen`
  - **ui/viewmodel**: `WebcamViewModel`, `WebcamViewModelFactory`
- `MainActivity.kt`: Compose setContent, tab + detail navigation, add dialog.

## Example streams
- **Big Buck Bunny**: `https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8`
- **Tears of Steel**: `https://test-streams.mux.dev/tos_ismc/main.m3u8`
- **PTS Shift**: `https://test-streams.mux.dev/pts_shift/master.m3u8`
