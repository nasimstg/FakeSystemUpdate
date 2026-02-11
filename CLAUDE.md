# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Fake System Update** is an Android application (`io.softexforge.fakesysupdate`). The project is in early stages — it has the scaffolded project structure but no activities, layouts, or application source code yet.

## Build & Development Commands

This is a Gradle-based Android project. Use the Gradle wrapper (`gradlew.bat` on Windows, `./gradlew` on Unix):

```bash
# Build debug APK
gradlew.bat assembleDebug

# Build release APK
gradlew.bat assembleRelease

# Run unit tests
gradlew.bat test

# Run a single unit test class
gradlew.bat testDebugUnitTest --tests "io.softexforge.fakesysupdate.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
gradlew.bat connectedAndroidTest

# Clean build
gradlew.bat clean
```

## Architecture & Configuration

- **Single module**: `:app` — all source code lives under `app/`
- **Package**: `io.softexforge.fakesysupdate`
- **Language**: Kotlin (Java 11 compatibility)
- **Min SDK**: 24 (Android 7.0) / **Target SDK**: 36
- **AGP**: 9.0.0 (Android Gradle Plugin)
- **Theme**: Material Components (`Theme.MaterialComponents.DayNight.DarkActionBar`)
- **Dependency catalog**: `gradle/libs.versions.toml` — use version catalog references (`libs.xyz`) when adding dependencies
- **No activities declared yet** in `AndroidManifest.xml` — new activities need to be registered there

## Source Paths

- App source: `app/src/main/java/io/softexforge/fakesysupdate/`
- Resources: `app/src/main/res/`
- Unit tests: `app/src/test/java/io/softexforge/fakesysupdate/`
- Instrumented tests: `app/src/androidTest/java/io/softexforge/fakesysupdate/`
