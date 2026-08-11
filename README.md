# Android Mobile Applications

A small portfolio of native Android applications developed with Kotlin. The repository contains two independent Android Studio projects that demonstrate UI development, background services, and multimedia interaction.

## Projects

### FocusBeat — Study Music Player

A focused music-player application designed for study sessions.

- Kotlin and Jetpack Compose user interface
- Foreground/background playback through `MusicPlayerService`
- Play, pause, track selection, and playback-state management
- Local audio resources and a dedicated player controller

### Clock App

A native Android clock application that demonstrates app structure, lifecycle-aware UI behavior, and time-based interaction.

## Run locally

1. Open either `clock-app/` or `focusbeat-music-player/` in Android Studio.
2. Let Gradle sync using the included wrapper.
3. Run on an Android emulator or physical device.

## Requirements

- Android Studio
- JDK compatible with the Gradle version specified in each project
- Android SDK installed locally

## Notes

Build artifacts, IDE state, and local SDK paths are intentionally excluded from version control. The bundled audio files are used solely for local course-project demonstration.
