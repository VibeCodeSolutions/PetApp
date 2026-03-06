# AGENTS.md -- Tierapp Coding Agent Instructions

## Projekt-Ueberblick
Familienfreundliche Android-Tierverwaltungs-App. Kotlin, Jetpack Compose, MVVM, Room + Firebase.
Architektur-Details: siehe `implementation_plan.md` im Projekt-Root.

## Tech-Stack (verbindlich)
- **Kotlin 2.1.x** mit Coroutines + Flow
- **Jetpack Compose** (BOM 2025.x) -- KEIN XML-Layout
- **Hilt** fuer Dependency Injection
- **Room 2.7+** als lokale Datenbank
- **Firebase** (Firestore, Auth, Storage, FCM) -- Spark-Plan
- **Coil 3.x** fuer Image Loading
- **WorkManager** fuer Background-Tasks
- **Compose Navigation 2.8+** mit type-safe Routes
- **kotlinx.serialization** fuer Serialisierung
- **KSP** (nicht KAPT) fuer Annotation Processing

## Projekt-Struktur

```
Tierapp/
  build-logic/            -- Convention Plugins
  app/                    -- Application-Modul
  core/
    model/                -- Domain-Entities, Enums
    database/             -- Room DB, DAOs, Migrations
    network/              -- Firebase-Wrapper
    sync/                 -- Sync-Engine, Worker
    common/               -- Extensions, Utils
    ui/                   -- Theme, shared Composables
    notifications/        -- Notification-Management
    media/                -- Bild-Verarbeitung
  feature/
    pets/                 -- Tierprofile
    health/               -- Impfungen, Medikamente
    gallery/              -- Foto-Galerie
    family/               -- Familien-Verwaltung
    settings/             -- Einstellungen
```

## Coding-Standards

### Kotlin
- Offizieller Kotlin Coding Style (https://kotlinlang.org/docs/coding-conventions.html)
- 4 Spaces Einrueckung, kein Tab
- Max Zeilenlaenge: 120 Zeichen
- Alle public APIs muessen Typen explizit deklarieren
- `suspend` Funktionen fuer alle IO-Operationen
- Coroutine-Scope: `viewModelScope` in ViewModels, `Dispatchers.IO` fuer DB/Network

### Naming
- Packages: lowercase, kein Underscore (`com.example.tierapp.feature.pets`)
- Klassen: PascalCase (`PetListViewModel`, `VaccinationDao`)
- Funktionen/Properties: camelCase (`getPetById`, `syncStatus`)
- Composables: PascalCase (`PetListScreen`, `VaccinationCard`)
- Room Entities: Singular (`Pet`, nicht `Pets`)
- Room DAOs: EntityName + Dao (`PetDao`, `VaccinationDao`)
- Repositories: EntityName + Repository (`PetRepository`)
- UseCases: Verb + Noun + UseCase (`CalculateNextVaccinationDateUseCase`)
- ViewModels: ScreenName + ViewModel (`PetListViewModel`)

### Compose
- Screen-Composables erhalten `ScreenUiState` + Lambdas, NICHT ViewModel direkt
- Preview-Funktionen fuer jedes Screen-Composable
- `Modifier` als erster optionaler Parameter
- State Hoisting: State in ViewModel, UI ist stateless
- `collectAsStateWithLifecycle()` -- KEIN `collectAsState()`
- Kein LiveData -- ausschliesslich StateFlow/Flow

### Architektur-Pattern
- **MVVM + Clean Architecture** (Presentation -> Domain -> Data)
- **Repository-Pattern**: Interface in Domain, Implementation in Data
- **UseCase-Pattern**: Eine Klasse pro Business-Logik-Operation mit `operator fun invoke()`
- **Offline-First**: Room ist Single Source of Truth, Firestore ist Sync-Backend
- **Unidirectional Data Flow**: Events hoch (Lambdas), State runter (StateFlow)

### Room / Datenbank
- Alle Entities haben `id: String` (UUID), `createdAt`, `updatedAt`, `syncStatus`, `isDeleted`
- DAOs geben `Flow<List<T>>` oder `Flow<T?>` zurueck
- Insert/Update/Delete sind `suspend` Funktionen
- Migrationen MUESSEN fuer jede Schema-Aenderung geschrieben werden
- TypeConverter fuer LocalDate, Instant, Enums in einer zentralen Converter-Klasse

### Firebase
- Firestore-Zugriffe NUR ueber :core:network Modul
- Security Rules: Lese-/Schreibzugriff nur fuer Familienmitglieder
- Auth-Provider: Google, Facebook, Microsoft
- Storage-Pfad: `/families/{familyId}/pets/{petId}/photos/{photoId}/`

### Fehlerbehandlung
- `Result<T>` Wrapper fuer alle Repository-Operationen
- Keine unbehandelten Exceptions -- catch + log + UI-Feedback
- Network-Fehler: Retry mit exponential Backoff (max 3 Versuche)
- Room-Fehler: Crashlytics loggen, User-freundliche Fehlermeldung

### Bild-Verarbeitung
- NIEMALS volles Bitmap in Memory laden
- Immer `BitmapFactory.Options.inSampleSize` oder Coil-Resize nutzen
- Thumbnails sofort nach Aufnahme generieren (150x150 + 400x400)
- Coil Memory-Cache max 64MB, Disk-Cache max 250MB

## Build-Befehle

```bash
# Debug-Build
./gradlew assembleDebug

# Unit-Tests
./gradlew testDebugUnitTest

# Instrumented Tests
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lintDebug

# Alle Checks
./gradlew check

# Einzelnes Modul bauen
./gradlew :feature:pets:assembleDebug
```

## Verbotene Praktiken
- KEIN XML-Layout (nur Compose)
- KEIN LiveData (nur Flow/StateFlow)
- KEIN KAPT (nur KSP)
- KEIN Glide/Picasso (nur Coil)
- KEIN Gson/Moshi (nur kotlinx.serialization)
- KEINE God-Classes (max ~300 Zeilen pro Datei)
- KEIN direkter Firestore-Zugriff ausserhalb von :core:network
- KEINE hartcodierten Strings in UI (nur string resources)
- KEINE Suppress-Annotations ohne Kommentar
- KEIN `GlobalScope` -- immer scoped Coroutines
