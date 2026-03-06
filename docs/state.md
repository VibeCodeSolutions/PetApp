# Tierapp -- State Snapshots

Dieses Dokument haelt den Zustand jeder Phase fest. Nach Abschluss einer Phase wird hier ein Snapshot erstellt, der den erreichten Stand, offene Punkte und Abhaengigkeiten fuer die naechste Phase dokumentiert.

---

## Phase 1: Projekt-Grundgeruest

**Status:** IN BEARBEITUNG
**Beginn:** 2026-03-06
**Abschluss:** --

### Ziel
Kompilierbares Multi-Module-Projekt mit Basiskonfiguration, Theme und Navigation-Shell.

### Erwarteter Endzustand
- [x] Alle 14 Module existieren und kompilieren (`./gradlew assembleDebug` erfolgreich)
- [x] Kotlin 2.1, Compose, KSP konfiguriert
- [x] build-logic/ Convention Plugins aktiv
- [x] Hilt voll konfiguriert (2.59.2, AGP 9 kompatibel)
- [x] MainActivity mit Compose-Scaffold + Bottom-Navigation (4 Tabs, Platzhalter)
- [x] :core:ui Theme definiert (Farben, Typografie)
- [x] Room-Datenbank v1 (leeres Schema, exportSchema=true, DatabaseModule via Hilt)
- [x] Hilt Application-Klasse registriert

### Snapshot Sprint 1.1 (2026-03-06)

**Abgeschlossen:** Build-System & Module-Skeleton

Erstellt:
- `gradle/libs.versions.toml` -- vollstaendiger Version Catalog (Kotlin 2.1.21, KSP 2.1.21-2.0.1, Compose BOM 2025.09.00, Hilt 2.56.1, Room 2.7.1, Coil 3.1, Firebase BOM 33.13.0)
- `build-logic/settings.gradle.kts` + `build-logic/convention/build.gradle.kts`
- 4 Convention Plugins: `tierapp.android.app`, `tierapp.android.library`, `tierapp.compose`, `tierapp.hilt`
- `settings.gradle.kts` -- build-logic includeBuild + alle 14 Module registriert
- `build.gradle.kts` (Root) -- alle Plugins mit `apply false`
- `gradle.properties` -- Parallel-Build, Configuration Cache, 4 GB Heap
- 14 Modul-`build.gradle.kts` (app + 8 core + 5 feature)
- 13 minimale `AndroidManifest.xml` fuer Library-Module
- Firebase Crashlytics in TOML + app-Modul ergaenzt (User)
- `google-services.json` -- Firebase-Projekt verbunden

Behobene Build-Fehler:
1. `CommonExtension<*,*,*,*,*,*>` -- in AGP 9.x nicht mehr generisch; `ComposeConventionPlugin` auf `pluginManager.withPlugin()` fuer App und Library umgestellt
2. `Cannot add extension 'kotlin'` -- AGP 9.x wendet `kotlin.android` automatisch an; `apply("org.jetbrains.kotlin.android")` aus Convention Plugins entfernt, `pluginManager.withPlugin()` fuer Kotlin-Konfiguration
3. `Android BaseExtension not found` -- AGP 9.x hat `BaseExtension` entfernt; Hilt 2.56.1 inkompatibel

### Blocker geloest (Sprint 1.2)

- Hilt 2.59.2 bringt volle AGP 9.0+ Kompatibilitaet
- `HiltConventionPlugin.kt`: `pluginManager.apply("com.google.dagger.hilt.android")` reaktiviert
- Kein aktiver Blocker fuer nachfolgende Sprints

---

### Snapshot Sprint 1.2 (2026-03-06)

**Abgeschlossen:** App-Shell & Theme

Erstellt:
- `TierappApplication.kt` -- `@HiltAndroidApp`, initialisiert Hilt-Komponentenbaum
- `MainActivity.kt` -- `@AndroidEntryPoint`, `enableEdgeToEdge`, `TierappTheme`, Scaffold + `NavigationBar`
- 4 Bottom-Nav-Tabs (Tiere, Gesundheit, Familie, Einstellungen) mit type-safe Serializable Routes
- 4 Platzhalter-Screens (Box + Text, kein ViewModel)
- `core/ui/theme/Color.kt` -- Brand-Palette: Honiggelb #F4A623, Terracotta #C75B39, Creme #FFF8F0; Light + Dark ColorScheme
- `core/ui/theme/Typography.kt` -- `TierappTypography` (7 TextStyles)
- `core/ui/theme/Theme.kt` -- `TierappTheme` Composable (Light/Dark, kein Dynamic Color per default)
- `app/src/main/AndroidManifest.xml` -- `android:name=".TierappApplication"`, MainActivity-Entry, `INTERNET` Permission
- `app/build.gradle.kts` -- `:core:ui` Dependency, `compose-material-icons-extended`
- `app/src/main/res/values/strings.xml` -- 4 Nav-Strings ergaenzt

Architektonische Entscheidungen:
- Routes als `@Serializable data object` (Navigation 2.9 type-safe API)
- `BottomNavItem` als private data class mit `isSelected`-Lambda (kein Enum-Overhead, kein Reflect)
- `dynamicColor = false` per default (Brand-Palette hat Vorrang vor Material You)

---

## Phase 2: Tierprofile

**Status:** ABGESCHLOSSEN
**Beginn:** 2026-03-06
**Abschluss:** 2026-03-07

### Ziel
Tiere erstellen, bearbeiten, anzeigen mit Profilbild.

### Erwarteter Endzustand
- [x] Room Entities: Pet, PetPhoto + DAOs mit Flow-Queries
- [x] PetRepository + PetPhotoRepository (Interface + Impl + HiltModule)
- [x] PetListViewModel, PetDetailViewModel, PetEditViewModel
- [x] UI: Tierliste (LazyColumn), Detail-Ansicht, Erstellungs-/Bearbeitungsformular
- [x] Species-Auswahl (ExposedDropdownMenuBox)
- [x] Profilbild-Aufnahme (Kamera + Galerie via ActivityResultContracts)
- [x] Thumbnail-Generierung (150x150 + 400x400, quadr. Crop) in :core:media
- [x] Coil-Integration fuer Bildanzeige (AsyncImage mit Vektor-Placeholder)

### Snapshot Phase 2 (2026-03-07)

**Sprint 2.1** — Pet-Entity & Repository (2026-03-06)
- `Pet` Domain-Modell, `PetRepository` Interface in `:core:model`
- `PetEntity` + Mapper, `PetDao` (Flow-Queries, Soft-Delete), `PetRepositoryImpl`, `PetRepositoryModule`
- DB-Migration 1->2 (pet-Tabelle)

**Sprint 2.2** — Pet-Listen-UI (2026-03-07)
- `PetListUiState` (sealed interface: Loading/Success/Empty)
- `PetListViewModel`: `stateIn(WhileSubscribed(5_000))`, mappt Flow<List<Pet>> zu UiState
- `PetListScreen`: `LazyColumn` mit `key={pet.id}`, `PetCard` mit Coil-`AsyncImage` + Vektor-Placeholder, FAB
- `PetListRoute` als HiltViewModel-Entry-Point; Screen erhaelt nur UiState + Lambdas
- 4 Unit-Tests, `MainDispatcherRule` (UnconfinedTestDispatcher), `FakePetRepository`

**Sprint 2.3** — Pet-Erstellen/Bearbeiten-UI (2026-03-07)
- `PetEditUiState` (sealed interface: Loading/Editing/SavedSuccess); `Editing` als data class mit Inline-Validierungsfehlern
- `PetEditViewModel`: Dual-Mode via `savedStateHandle["petId"]` (null=create, non-null=edit); Validierung (Name Pflicht, Chip 15 Ziffern)
- `PetEditScreen`: Material3-Formular, `ExposedDropdownMenuBox` fuer Species, `DatePickerDialog`
- Navigation: `TierBearbeitenRoute(petId: String? = null)` in `:app`
- 7 Unit-Tests (Create + Edit Modus, Validierungspfade)

**Sprint 2.4** — Pet-Detail & Profilbild (2026-03-07)
- `PetPhoto` Domain-Modell + `PetPhotoRepository` in `:core:model`
- `PetPhotoEntity` (FK auf pet, CASCADE DELETE, Index petId), `PetPhotoDao`, `PetPhotoRepositoryImpl`, `PetPhotoRepositoryModule`
- DB-Migration 2->3 (pet_photo-Tabelle); `TierappDatabase` v3
- `ThumbnailManager` Interface + `ThumbnailManagerImpl` @Singleton in `:core:media`; quadratischer Center-Crop vor Skalierung; `MediaModule` Hilt-Binding
- `PetDetailViewModel`: `combine(petRepo, petPhotoRepo)` -> single `StateFlow<PetDetailUiState>`; `onPhotoSelected(Uri)` generiert Thumbs, persistiert PetPhoto, aktualisiert pet.profilePhotoId
- `PetDetailScreen`: 160dp-Profilbild (CircleShape), Kamera-Button overlay, `PhotoSourceDialog`, `InfoRow`-Tabelle
- Foto-Picker: `PickVisualMedia` (Galerie) + `TakePicture` (Kamera) via `FileProvider` (authority: com.example.tierapp.fileprovider)
- `file_provider_paths.xml` + CAMERA-Permission in AndroidManifest
- 5 Unit-Tests; `FakeThumbnailManager` als `object` (Interface ermoeglicht testbare DI ohne Android-Context)

### Architektonische Entscheidungen (Phase 2)
- `ThumbnailManager` als Interface → saubere Testbarkeit ohne Robolectric
- `PetEditViewModel.validate()` gibt Boolean zurueck und schreibt Fehler direkt in `_uiState` — kein separater Error-Channel
- Camera-Temp-Datei in `context.filesDir` (interner Speicher) → kein External-Storage-Permission noetig
- `PetSpecies.toDisplayName()` dreifach vorhanden (technische Schuld) → Konsolidierung geplant fuer Sprint 6.2

### Abhaengigkeiten
- Phase 1 muss abgeschlossen sein ✅

---

## Phase 3: Gesundheitsmanagement

**Status:** OFFEN
**Beginn:** --
**Abschluss:** --

### Ziel
Impfungen, medizinische Akte und Medikamente verwalten mit Erinnerungen.

### Erwarteter Endzustand
- [ ] Room Entities: Vaccination, MedicalRecord, Medication, Reminder + DAOs
- [ ] Impf-Turnus-Berechnung (Standard-Intervalle, manuell anpassbar)
- [ ] UI: Impf-Liste, Medizinische Akte, Medikamenten-Verwaltung
- [ ] WorkManager-basierte Erinnerungen
- [ ] 5 Notification Channels konfiguriert
- [ ] Gesundheits-Dashboard (aggregierte Uebersicht)

### Snapshot
_Wird nach Abschluss der Phase ausgefuellt._

### Abhaengigkeiten
- Phase 2 muss abgeschlossen sein (Pet-Entity)

---

## Phase 4: Medienverwaltung

**Status:** OFFEN
**Beginn:** --
**Abschluss:** --

### Ziel
Foto-Galerie pro Tier mit effizienter Bildverwaltung.

### Erwarteter Endzustand
- [ ] Foto-Galerie als Grid (LazyVerticalGrid)
- [ ] Multi-Foto-Upload aus Galerie/Kamera
- [ ] Vollbild-Ansicht mit Zoom
- [ ] Disk-Cache-Management (250MB Limit, LRU)
- [ ] Foto-Loeschen mit Confirmation

### Snapshot
_Wird nach Abschluss der Phase ausgefuellt._

### Abhaengigkeiten
- Phase 2 muss abgeschlossen sein (PetPhoto-Entity, :core:media)

---

## Phase 5: Multi-Device & Cloud-Sync

**Status:** OFFEN
**Beginn:** --
**Abschluss:** --

### Ziel
Familien-Konten und Offline-First Cloud-Synchronisation.

### Erwarteter Endzustand
- [ ] Firebase-Projekt konfiguriert (Auth, Firestore, Storage, FCM)
- [ ] Auth: Google, Facebook, Microsoft Sign-In funktional
- [ ] Familie erstellen, Mitglieder einladen (via Link/Code)
- [ ] Firestore Security Rules (nur Familienmitglieder)
- [ ] SyncWorker: Push (PENDING -> Firestore) + Pull (Firestore -> Room)
- [ ] Konfliktaufloesung: Last-Write-Wins + feldbasiertes Merging
- [ ] PhotoUploadWorker: Bilder -> Firebase Storage
- [ ] ReminderRefreshWorker: Erinnerungen nach Sync aktualisieren

### Snapshot
_Wird nach Abschluss der Phase ausgefuellt._

### Abhaengigkeiten
- Phasen 1-4 muessen abgeschlossen sein

---

## Phase 6: Polish & Release

**Status:** OFFEN
**Beginn:** --
**Abschluss:** --

### Ziel
Produktionsreife: Performance, Sicherheit, Accessibility, Store-Vorbereitung.

### Erwarteter Endzustand
- [ ] R8/ProGuard aktiv (isMinifyEnabled = true)
- [ ] Firebase Crashlytics integriert
- [ ] Baseline Profiles generiert
- [ ] Accessibility geprueft (ContentDescription, Touch-Targets)
- [ ] Datenschutzerklaerung vorhanden
- [ ] App-Icon + Splash-Screen finalisiert
- [ ] Edge-Case-Tests bestanden (Offline, viele Tiere, grosse Bilder)

### Snapshot
_Wird nach Abschluss der Phase ausgefuellt._

### Abhaengigkeiten
- Phase 5 muss abgeschlossen sein
