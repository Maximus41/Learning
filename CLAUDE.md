# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**StudyTracker** is an Android app for tracking daily study sessions by subject. Users manage subjects (with hierarchical content: Subject → Section → Page → Paragraph), create study sessions, and assess their progress.

## Build Commands

```bash
# Build
./gradlew assembleDebug       # Debug APK
./gradlew assembleRelease     # Release APK
./gradlew build               # All variants

# Test
./gradlew test                # Unit tests
./gradlew connectedTest       # Instrumented tests (requires device/emulator)

# Quality
./gradlew lint                # Android lint checks
./gradlew clean               # Clean build outputs
```

**Note**: On Windows use `gradlew.bat` instead of `./gradlew`.

## Module Structure

Two-module project:
- **`app/`** — UI layer (`com.agn.studytracker`): fragments, adapters, ViewModels, navigation
- **`core/`** — Data layer (`com.agn.corea`): ObjectBox entities, constants, filter criteria

## Architecture

**Single Activity + Fragment Navigation** with Data Binding and RxJava 2:

- `MainActivity` hosts a `NavHostFragment`; all screens are fragments navigated via the Navigation Component (`nav_graph.xml`)
- Database: **ObjectBox** with `@Entity` annotations; all queries wrapped in `RxQuery.single()`, run on `Schedulers.io()`, observed on `AndroidSchedulers.mainThread()`
- Data Binding is enabled — fragments use `DataBindingUtil.inflate()`
- Arguments between fragments are passed via `Bundle`

**Navigation flow:**
```
SubjectsFragment (start)
├── SubjectDetailsFragment
└── SessionsFragment
    ├── EditSessionFragment
    ├── UpdateSessionFragment
    └── AssessSessionFragment
```

## Key Data Models (core module, all Java)

- **`Subject`** → `ToMany<Section>` → `ToMany<Page>` → `ToMany<Para>`
- **`Session`** — lifecycle: created → active (with expiry) → ended → assessed
- **`SessionTopic`** — links a session to specific pages/sections
- **`SessionAssessment`** — post-session notes (summary, plan, questions, todos)
- **`PageCumulativeProgress`** — tracks reading progress per page

## Study Point System

Defined in `GlobalConstants.java`:
- 1 story point = 2 hours of study
- Daily target = 3 hours = 1.5 story points
- Session expiry is calculated from topic story points

## Key Libraries

| Library | Purpose |
|---|---|
| ObjectBox 2.8.1 | Embedded database + RxJava integration |
| RxJava 2 / RxAndroid | Async DB queries and reactivity |
| Navigation Component 2.3.5 | Fragment navigation |
| AndroidX Lifecycle 2.3.1 | ViewModel + LiveData |
| Dagger 2.36 | Dependency injection (core module) |
| Gson 2.8.6 | JSON serialization |
| laser-native-editor | Rich text editor for content |

## Language Mix

The `core` module is **Java**; the `app` module is **Kotlin**. New app-layer code should be Kotlin; core entities are Java.
