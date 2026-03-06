# Tierapp -- Sprint-Planung (Token-/Kontextoptimiert)

Jeder Sprint ist so geschnitten, dass er in einer einzelnen Agent-Session (Kontextfenster) bearbeitbar ist. Sprints sind nach Abhaengigkeiten geordnet -- ein Sprint darf erst starten, wenn seine Vorgaenger abgeschlossen sind.

---

## Phase 1: Projekt-Grundgeruest

### Sprint 1.1: Build-System & Module-Skeleton ✅ ABGESCHLOSSEN (2026-03-06)
**Scope:** Gradle-Konfiguration, Version Catalog, Convention Plugins, Module anlegen
**Dateien:** `libs.versions.toml`, `build.gradle.kts` (root), `settings.gradle.kts`, `build-logic/`
- [x] Kotlin-Plugin, Compose, KSP, Hilt, Room in `libs.versions.toml` definieren
- [x] `build-logic/` Convention Plugins erstellen (android-app, android-library, compose, hilt)
- [x] Root `build.gradle.kts` um Plugins erweitern
- [x] Alle 14 Module in `settings.gradle.kts` registrieren
- [x] Jedes Modul: `build.gradle.kts` mit Convention Plugin
- [x] Firebase-Projekt verbunden, `google-services.json` eingebunden
- [x] Verifizierung: `./gradlew assembleDebug` kompiliert ohne Fehler

### Sprint 1.2: App-Shell & Theme ✅ ABGESCHLOSSEN (2026-03-06)
**Scope:** MainActivity, Hilt-Setup, Theme, Bottom-Navigation
**Dateien:** `:app`, `:core:ui`, `AndroidManifest.xml`
- [x] Hilt-Blocker geloest: Version auf 2.59.2 aktualisiert, Plugin reaktiviert
- [x] `TierappApplication` Klasse mit `@HiltAndroidApp`
- [x] `MainActivity` mit `@AndroidEntryPoint` und Compose `setContent`
- [x] Theme in `:core:ui`: Farben (Honiggelb #F4A623, Terracotta #C75B39, Creme #FFF8F0), Typografie
- [x] Scaffold mit Bottom-Navigation (4 Tabs: Tiere, Gesundheit, Familie, Einstellungen)
- [x] Platzhalter-Screens fuer jeden Tab (type-safe Serializable Routes)
- [x] `AndroidManifest.xml`: Application-Klasse, MainActivity, INTERNET-Permission
- [ ] Verifizierung: `./gradlew assembleDebug` + App startet auf Geraet/Emulator

### Sprint 1.3: Room-Datenbank-Grundgeruest ✅ ABGESCHLOSSEN (2026-03-06)
**Scope:** Leere Room-DB, TypeConverter, Enums
**Dateien:** `:core:database`, `:core:model`
- [x] Enums in `:core:model`: PetSpecies, SyncStatus, MedicalRecordType, MemberRole, UploadStatus, ReminderType
- [x] TypeConverter-Klasse: LocalDate (EpochDay), Instant (EpochMilli), alle 6 Enums (name-basiert)
- [x] `TierappDatabase` (abstract, @Database, version = 1, exportSchema = true, leeres Schema)
- [x] KSP-Arg `room.schemaLocation` in `core/database/build.gradle.kts` konfiguriert
- [x] Hilt-Modul `DatabaseModule` in `di/` (Singleton, Room.databaseBuilder)
- [x] Verifizierung: `./gradlew assembleDebug` kompiliert ohne Fehler

---

## Phase 2: Tierprofile

### Sprint 2.1: Pet-Entity & Repository ✅ ABGESCHLOSSEN (2026-03-06)
**Scope:** Room Entity, DAO, Repository
**Dateien:** `:core:database`, `:core:model`
- [x] `Pet` Domain-Modell in `:core:model` (plain data class, keine Room-Annotationen)
- [x] `PetRepository` Interface in `:core:model`
- [x] `PetEntity` Room-Entity + Mapper (toDomain/toEntity) in `:core:database`
- [x] `PetDao`: getAll/getById (Flow), insert/update (suspend), softDelete
- [x] `MIGRATION_1_2`: pet-Tabelle in SQLite angelegt
- [x] `TierappDatabase` auf version = 2, PetEntity registriert, petDao() abstract
- [x] `PetRepositoryImpl` mit Soft-Delete (isDeleted=1, syncStatus=PENDING)
- [x] `PetRepositoryModule` Hilt-Binding (Interface -> Impl, Singleton)
- [x] `DatabaseModule` um PetDao-Provider und MIGRATION_1_2 erweitert
- [x] `:app` haengt auf `:core:database` (Hilt-Module-Discovery)
- [ ] Verifizierung: `./gradlew :core:database:assembleDebug` erfolgreich

### Sprint 2.2: Pet-Listen-UI
**Scope:** PetList-Screen mit ViewModel
**Dateien:** `:feature:pets`
- [ ] `PetListUiState` (Loading, Success, Empty)
- [ ] `PetListViewModel` mit StateFlow
- [ ] `PetListScreen` Composable (LazyColumn, Pet-Karten mit Placeholder-Bild)
- [ ] Navigation-Integration in App NavHost
- [ ] FAB zum Hinzufuegen
- [ ] Verifizierung: Liste zeigt Tiere aus DB an (ggf. mit Testdaten)

### Sprint 2.3: Pet-Erstellen/Bearbeiten-UI
**Scope:** Formular zum Anlegen und Bearbeiten von Tieren
**Dateien:** `:feature:pets`
- [ ] `PetEditViewModel` (Create + Edit Modus)
- [ ] `PetEditScreen` Composable (Name, Geburtsdatum, Tierart, Rasse, Chip-Nr.)
- [ ] Species-Auswahl mit Icons (Dropdown/Chip-Gruppe)
- [ ] DatePicker fuer Geburtsdatum
- [ ] Validierung (Name Pflicht, Chip-Nr. Format)
- [ ] Navigation: PetList -> PetEdit, PetDetail -> PetEdit
- [ ] Verifizierung: Tier anlegen, bearbeiten, in Liste sichtbar

### Sprint 2.4: Pet-Detail & Profilbild
**Scope:** Detail-Ansicht + Bild-Aufnahme/Auswahl
**Dateien:** `:feature:pets`, `:core:media`
- [ ] `PetDetailScreen` Composable (alle Infos, grosses Profilbild)
- [ ] `PetDetailViewModel`
- [ ] Bild-Picker: Kamera (ActivityResultContracts.TakePicture) + Galerie (PickVisualMedia)
- [ ] Thumbnail-Generierung in `:core:media` (150x150 + 400x400)
- [ ] Coil-Integration: AsyncImage mit Placeholder (Tier-Silhouette je Species)
- [ ] PetPhoto Room-Entity + DAO (fuer spaetere Galerie vorbereiten)
- [ ] Verifizierung: Profilbild aufnehmen/auswaehlen, Thumbnail wird generiert, Bild wird angezeigt

---

## Phase 3: Gesundheitsmanagement

### Sprint 3.1: Vaccination-Entity & UI
**Scope:** Impfungen erfassen und anzeigen
**Dateien:** `:core:database`, `:feature:health`
- [ ] `Vaccination` Room-Entity + `VaccinationDao`
- [ ] DB-Migration
- [ ] `VaccinationRepository`
- [ ] `VaccinationListViewModel` + `VaccinationListScreen`
- [ ] Impfung-hinzufuegen-Formular (Name, Datum, Turnus, Tierarzt, Chargen-Nr.)
- [ ] Turnus-Berechnung: `nextDueDate = dateAdministered + intervalMonths`
- [ ] Verifizierung: Impfung anlegen, naechster Termin wird berechnet

### Sprint 3.2: Medizinische Akte & Medikamente
**Scope:** Allergien, Diagnosen, Medikamente erfassen
**Dateien:** `:core:database`, `:feature:health`
- [ ] `MedicalRecord` Room-Entity + DAO + Repository
- [ ] `Medication` Room-Entity + DAO + Repository
- [ ] DB-Migration
- [ ] UI: MedicalRecord-Liste (gefiltert nach Typ), Hinzufuegen-Formular
- [ ] UI: Medikamenten-Liste, Hinzufuegen-Formular (Name, Dosierung, Frequenz, Vorrat)
- [ ] Nachschub-Berechnung: `daysRemaining = currentStock / dailyConsumption`
- [ ] Verifizierung: Allergien, Medikamente anlegen und anzeigen

### Sprint 3.3: Erinnerungen & Notifications
**Scope:** WorkManager-Scheduling, Notification Channels, Reminder-Entity
**Dateien:** `:core:notifications`, `:core:database`
- [ ] `Reminder` Room-Entity + DAO
- [ ] 5 Notification Channels erstellen (in Application.onCreate)
- [ ] `DailyHealthCheckWorker`: prueft Impftermine + Medikamentenvorraete
- [ ] Lokale Notifications: Impfung (4W/1W/Tag vorher), Nachschub (bei Schwelle)
- [ ] Erinnerungen in Room speichern (isCompleted, isSnoozed)
- [ ] Verifizierung: Worker laeuft, Notification wird angezeigt

### Sprint 3.4: Gesundheits-Dashboard
**Scope:** Aggregierte Uebersicht aller faelligen Aktionen
**Dateien:** `:feature:health`
- [ ] `HealthDashboardViewModel` (kombiniert Impf- + Medikamenten-Daten)
- [ ] `HealthDashboardScreen`: faellige Impfungen, niedrige Vorraete, offene Erinnerungen
- [ ] Sortierung nach Dringlichkeit
- [ ] Quick-Actions (Impfung als erledigt markieren, Vorrat auffuellen)
- [ ] Verifizierung: Dashboard zeigt korrekte aggregierte Daten

---

## Phase 4: Medienverwaltung

### Sprint 4.1: Foto-Galerie
**Scope:** Galerie-Grid pro Tier, Multi-Upload, Vollbild
**Dateien:** `:feature:gallery`, `:core:media`
- [ ] `GalleryViewModel` + `GalleryScreen` (LazyVerticalGrid)
- [ ] Multi-Foto-Auswahl (PickMultipleVisualMedia)
- [ ] Thumbnail-Anzeige im Grid (Thumb-M 400x400)
- [ ] Vollbild-Ansicht mit Zoom (Modifier.transformable)
- [ ] Foto-Loeschen mit Confirmation-Dialog
- [ ] Disk-Cache-Management (Coil 250MB Limit)
- [ ] Navigation: PetDetail -> Gallery
- [ ] Verifizierung: Fotos hochladen, Grid anzeigen, Vollbild zoomen, loeschen

---

## Phase 5: Multi-Device & Cloud-Sync

### Sprint 5.1: Firebase-Setup & Auth
**Scope:** Firebase-Projekt, Auth mit 3 Providern
**Dateien:** `:core:network`, `:app`
- [x] Firebase-Projekt anlegen (Firestore, Auth, Storage)
- [x] `google-services.json` einbinden
- [ ] Firebase Auth: Google Sign-In konfigurieren
- [ ] Firebase Auth: Facebook Login konfigurieren (Facebook Developer App)
- [ ] Firebase Auth: Microsoft Login konfigurieren (Azure AD)
- [ ] Login/Registrierungs-Screen mit 3 Provider-Buttons
- [ ] Auth-State in ViewModel (eingeloggt/ausgeloggt)
- [ ] Verifizierung: Login/Logout mit jedem Provider funktioniert

### Sprint 5.2: Familien-Verwaltung
**Scope:** Familie erstellen, Mitglieder einladen
**Dateien:** `:feature:family`, `:core:network`, `:core:database`
- [ ] Family + FamilyMember Room-Entities + DAOs
- [ ] Firestore-Struktur: `/families/{id}/...`
- [ ] Familie erstellen (Name, Creator wird Owner)
- [ ] Einladungs-Mechanismus (Share-Link mit Familien-Code)
- [ ] Mitglieder-Uebersicht (Rolle, Beitrittsdatum)
- [ ] Firestore Security Rules (nur Familienmitglieder lesen/schreiben)
- [ ] Verifizierung: Familie erstellen, zweiter User kann beitreten

### Sprint 5.3: Sync-Engine
**Scope:** SyncWorker, Push/Pull, Konfliktaufloesung
**Dateien:** `:core:sync`
- [ ] `SyncWorker` (PeriodicWork 15min + OneTime bei Aenderungen)
- [ ] Push: alle PENDING-Entities zu Firestore (WriteBatch)
- [ ] Pull: SnapshotListener fuer Realtime (Vordergrund), Pull-Worker (Hintergrund)
- [ ] Konfliktaufloesung: SyncResolver mit Last-Write-Wins + Feldvergleich
- [ ] Soft-Delete-Sync (isDeleted propagieren)
- [ ] `ReminderRefreshWorker` (nach Sync: Erinnerungen aktualisieren)
- [ ] Verifizierung: Aenderung auf Geraet A erscheint auf Geraet B

### Sprint 5.4: Bild-Sync
**Scope:** Fotos zu Firebase Storage hochladen/herunterladen
**Dateien:** `:core:sync`, `:core:media`, `:core:network`
- [ ] `PhotoUploadWorker` (OneTime, Constraint: CONNECTED + NOT_LOW_BATTERY)
- [ ] Upload: Original + Thumb-S + Thumb-M -> Firebase Storage
- [ ] Download: andere Geraete laden Thumbnails automatisch, Originale on-demand
- [ ] Upload-Status-Tracking (LOCAL_ONLY -> UPLOADING -> UPLOADED / FAILED)
- [ ] Retry mit exponential Backoff bei Fehler
- [ ] Verifizierung: Foto auf Geraet A aufnehmen, auf Geraet B sichtbar

---

## Phase 6: Polish & Release

### Sprint 6.1: Performance & Sicherheit
**Scope:** R8, Crashlytics, Baseline Profiles
**Dateien:** `:app`, `proguard-rules.pro`
- [ ] `isMinifyEnabled = true` + ProGuard-Regeln fuer Firebase, Room, Hilt
- [ ] Firebase Crashlytics integrieren
- [ ] Baseline Profiles generieren (Macrobenchmark)
- [ ] Startup-Tracing / App Startup Library
- [ ] Verifizierung: Release-Build laeuft, Crashlytics empfaengt Test-Crash

### Sprint 6.2: UX-Polish & Accessibility
**Scope:** Feinschliff, Accessibility, Datenschutz
**Dateien:** alle Feature-Module, `:core:ui`
- [ ] ContentDescription fuer alle interaktiven Elemente
- [ ] Touch-Targets mindestens 48dp
- [ ] Animationen (Crossfade, Shared Element Transitions)
- [ ] Empty-States mit Illustrationen
- [ ] Error-States mit Retry-Buttons
- [ ] Datenschutzerklaerung-Screen
- [ ] Verifizierung: TalkBack-Durchlauf, keine fehlenden Descriptions

### Sprint 6.3: Release-Vorbereitung
**Scope:** App-Icon, Splash, Store-Listing
**Dateien:** `:app`, Ressourcen
- [ ] App-Icon (Adaptive Icon mit Tier-Motiv)
- [ ] Splash-Screen (Core Splashscreen API)
- [ ] App-Name finalisieren (strings.xml)
- [ ] Screenshots fuer Store-Listing
- [ ] Edge-Case-Tests: Offline-Modus, 50+ Tiere, 100+ Fotos, aeltere Geraete (API 27)
- [ ] Signed Release-APK / AAB erstellen
- [ ] Verifizierung: Vollstaendiger Durchlauf aller Features auf Testgeraet
