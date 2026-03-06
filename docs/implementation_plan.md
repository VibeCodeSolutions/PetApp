# Tierapp -- Implementation Plan

## 1. Technologie-Stack

| Bereich | Technologie | Version | Begruendung |
|---|---|---|---|
| Sprache | Kotlin | 2.1.x | Offizielle Android-Sprache, Coroutines |
| UI | Jetpack Compose | BOM 2025.x | Deklarativ, State-driven, weniger Boilerplate |
| DI | Hilt | 2.56+ | Google-empfohlen, Lifecycle-aware, KSP |
| Lokale DB | Room | 2.7.x | Offizieller ORM, Flow-Support, Migrations |
| Cloud DB | Firebase Firestore | - | Realtime-Sync, eingebaute Offline-Persistenz, Spark-Plan kostenlos |
| Auth | Firebase Auth | - | Google, Facebook, Microsoft als Provider |
| Cloud Storage | Firebase Storage | - | Bild-Upload/-Download, Security Rules |
| Push | FCM + WorkManager | - | FCM fuer Remote-Push, WorkManager fuer lokale Erinnerungen |
| Image Loading | Coil 3.x | 3.1+ | Kotlin-nativ, Compose-Integration, Memory/Disk-Cache |
| Serialisierung | kotlinx.serialization | 1.8+ | Kein Reflection, schnell |
| Navigation | Compose Navigation | 2.8+ | Type-safe Routes via @Serializable |
| Build | Gradle Version Catalog + Convention Plugins | - | Zentrale Versionierung, DRY |

## 2. Multi-Module-Struktur

```
build-logic/                      -- Convention Plugins (shared Build-Config)
:app                              -- Application, Hilt-Setup, Navigation-Host, MainActivity
:core:model                       -- Domain-Entities, Enums (keine Android-Dep)
:core:database                    -- Room DB, DAOs, Entities, Migrations, TypeConverter
:core:network                     -- Firebase-Wrapper (Firestore, Auth, Storage)
:core:sync                        -- Sync-Engine, Konfliktaufloesung, SyncWorker
:core:common                      -- Extensions, Result-Wrapper, DateUtils
:core:ui                          -- Design-System: Theme, Farben, Typografie, shared Composables
:core:notifications               -- NotificationManager, Channels, Erinnerungs-Scheduling
:core:media                       -- Bild-Aufnahme, Thumbnail-Generierung, Caching
:feature:pets                     -- Tierprofil: Liste, Detail, Erstellen/Bearbeiten
:feature:health                   -- Impfungen, Gesundheitsakte, Medikamente
:feature:gallery                  -- Foto-Galerie pro Tier
:feature:family                   -- Familien-Verwaltung, Einladungen, Mitglieder
:feature:settings                 -- App-Einstellungen, Benachrichtigungs-Prefs
```

### Abhaengigkeits-Graph

```
:app --> :feature:* --> :core:ui, :core:model, :core:common
:feature:pets --> :core:database, :core:media, :core:sync
:feature:health --> :core:database, :core:notifications, :core:sync
:feature:gallery --> :core:media, :core:database, :core:sync
:feature:family --> :core:network, :core:database
:feature:settings --> :core:common, :core:notifications
:core:sync --> :core:database, :core:network
:core:database --> :core:model
:core:network --> :core:model
:core:notifications --> :core:database, :core:common
:core:media --> :core:common
```

## 3. Datenmodell

### 3.1 Entities

**Pet**
- id: String (UUID), name: String, birthDate: LocalDate?, ageManual: Int?
- species: PetSpecies (DOG, CAT, BIRD, RABBIT, HAMSTER, REPTILE, FISH, OTHER)
- breed: String?, chipNumber: String?, profilePhotoUri: String?
- familyId: String (FK), createdAt: Instant, updatedAt: Instant
- syncStatus: SyncStatus (SYNCED, PENDING, CONFLICT), isDeleted: Boolean

**Vaccination**
- id: String (UUID), petId: String (FK)
- vaccineName: String, dateAdministered: LocalDate, nextDueDate: LocalDate?
- intervalMonths: Int?, veterinarian: String?, batchNumber: String?, notes: String?
- createdAt, updatedAt, syncStatus, isDeleted

**MedicalRecord**
- id: String (UUID), petId: String (FK)
- type: MedicalRecordType (ALLERGY, INTOLERANCE, DIAGNOSIS, SURGERY, CHECKUP)
- title: String, description: String?, dateRecorded: LocalDate
- createdAt, updatedAt, syncStatus, isDeleted

**Medication**
- id: String (UUID), petId: String (FK)
- name: String, dosage: String, frequency: String
- currentStock: Int?, dailyConsumption: Float?, resupplyThreshold: Int?
- startDate: LocalDate?, endDate: LocalDate?, notes: String?
- createdAt, updatedAt, syncStatus, isDeleted

**PetPhoto**
- id: String (UUID), petId: String (FK)
- localUri: String, remoteUrl: String?, thumbnailUri: String?
- caption: String?, takenAt: Instant?, sizeBytes: Long
- uploadStatus: UploadStatus (LOCAL_ONLY, UPLOADING, UPLOADED, FAILED)
- createdAt, updatedAt, syncStatus, isDeleted

**Family**
- id: String (UUID), name: String, createdBy: String (userId)
- createdAt, updatedAt

**FamilyMember**
- id: String (UUID), familyId: String (FK), userId: String (Firebase Auth UID)
- displayName: String, email: String, role: MemberRole (OWNER, ADMIN, MEMBER)
- joinedAt: Instant

**Reminder** (lokal, nicht direkt gesynced)
- id: String (UUID), petId: String, referenceId: String, referenceType: ReminderType (VACCINATION, MEDICATION_DOSE, RESUPPLY)
- triggerAt: Instant, title: String, message: String
- isCompleted: Boolean, isSnoozed: Boolean, snoozedUntil: Instant?

### 3.2 Beziehungen

```
Family  1 ---< N  Pet
Family  1 ---< N  FamilyMember
Pet     1 ---< N  Vaccination
Pet     1 ---< N  MedicalRecord
Pet     1 ---< N  Medication
Pet     1 ---< N  PetPhoto
Pet     1 ---< N  Reminder
```

## 4. Offline-First & Sync-Strategie

### 4.1 Grundprinzip
Room ist die Single Source of Truth. UI liest ausschliesslich aus Room (via Flow).
Firestore dient als Sync-Backend, nicht als primaere Datenquelle.

### 4.2 Schreib-Pfad
1. User aendert Daten -> ViewModel schreibt in Room mit `syncStatus = PENDING`
2. UI aktualisiert sofort (optimistic update via Flow)
3. SyncWorker (WorkManager: periodic 15 Min + OneTime bei Aenderung) sammelt PENDING-Eintraege
4. Batch-Upload zu Firestore (WriteBatch, max 500 Docs)
5. Erfolg: `syncStatus = SYNCED` / Fehler: bleibt PENDING, naechster Lauf

### 4.3 Lese-Pfad
1. Firestore SnapshotListener auf Family-Collection (Realtime, wenn App im Vordergrund)
2. Eingehende Aenderungen werden mit lokaler DB verglichen und gemerged
3. Hintergrund: periodischer Pull via WorkManager

### 4.4 Konfliktaufloesung
**Strategie: Last-Write-Wins mit feldbasiertem Merging**

- Lokaler Eintrag SYNCED + Cloud-Aenderung: Cloud gewinnt (Overwrite)
- Lokaler Eintrag PENDING + Cloud-Aenderung: Feldvergleich
  - Nur lokal geaendert: lokal bleibt
  - Nur remote geaendert: remote gewinnt
  - Beides geaendert: `updatedAt` entscheidet (Last-Write-Wins)
- Soft-Delete (`isDeleted = true`): Loesch-Wins nach Confirmation

### 4.5 Bild-Sync
- Metadaten via Firestore, Binaerdaten via Firebase Storage
- Upload-Queue: WorkManager mit Constraints (CONNECTED, NOT_LOW_BATTERY)
- Andere Geraete: Thumbnails automatisch, Originale on-demand

## 5. Bild-Strategie

### 5.1 Thumbnails

| Stufe | Verwendung | Dimension | Qualitaet |
|---|---|---|---|
| Thumb-S | Listen, Profilbild | 150x150 px | 70% JPEG |
| Thumb-M | Galerie-Grid | 400x400 px | 80% JPEG |
| Original | Vollansicht | unveraendert | Original |

Generierung: sofort nach Aufnahme/Auswahl, Background-Thread, `BitmapFactory.Options.inSampleSize`.

### 5.2 Caching

```
Memory (Coil LRU, max 64MB) -> Disk (Coil, max 250MB) -> App-Storage (Thumbnails) -> Firebase Storage (Originale)
```

### 5.3 OOM-Vermeidung
- Nie volles Bitmap in Memory -- immer inSampleSize/Coil-Resize
- Coil size() auf View-Groesse limitieren
- LazyVerticalGrid: nur sichtbare Items laden
- HEIF-Support ab API 27+

### 5.4 Firebase Storage Struktur
```
/families/{familyId}/pets/{petId}/photos/{photoId}/original.jpg
/families/{familyId}/pets/{petId}/photos/{photoId}/thumb_s.jpg
/families/{familyId}/pets/{petId}/photos/{photoId}/thumb_m.jpg
```

## 6. Push-Notification-Strategie

### 6.1 Lokale Notifications
- Impf-Erinnerungen: 4 Wochen vorher, 1 Woche vorher, am Tag
- Medikamenten-Dosis: PeriodicWorkRequest (taeglich)
- Nachschub: `currentStock / dailyConsumption` < threshold -> Erinnerung

### 6.2 Notification Channels

| Channel ID | Name | Prioritaet |
|---|---|---|
| vaccination_reminders | Impf-Erinnerungen | HIGH |
| medication_reminders | Medikamenten-Erinnerungen | HIGH |
| resupply_reminders | Nachschub-Erinnerungen | DEFAULT |
| family_updates | Familien-Aktivitaet | LOW |
| sync_status | Sync-Status | MIN |

### 6.3 WorkManager-Architektur
- **DailyHealthCheckWorker** (Periodic 24h): Prueft Impftermine + Vorraete, erstellt Reminder
- **SyncWorker** (Periodic 15min + Event): Push/Pull, triggert ReminderRefresh
- **ReminderRefreshWorker** (OneTime nach Sync): Storniert veraltete, plant neue Alarme
- **PhotoUploadWorker** (OneTime, CONNECTED): Upload-Queue, exponential Backoff

## 7. Architektur-Pattern

### 7.1 MVVM + Clean Architecture
```
Presentation (Compose UI + ViewModels)
  -> Domain (UseCases, Repository-Interfaces)
  -> Data (Repository-Impl, Room DAOs, Firebase Services)
```

### 7.2 State Management
- UI State: `StateFlow<ScreenUiState>` (immutable Data Classes)
- Events: `SharedFlow` / `Channel`
- Loading/Error/Success: `sealed interface UiState<T>`
- Collection: `collectAsStateWithLifecycle()`
- Kein LiveData -- durchgehend Flow/StateFlow

### 7.3 Navigation
Bottom-Navigation mit 4 Tabs:
1. Meine Tiere (PetList)
2. Gesundheit (aggregierte Uebersicht)
3. Familie (Verwaltung)
4. Einstellungen

Type-safe Routes via @Serializable Klassen.

## 8. Phasenplan

### Phase 1: Projekt-Grundgeruest
- Kotlin-Plugin, Compose, KSP in libs.versions.toml + Build-Scripte
- build-logic/ Convention Plugins
- Alle Module anlegen mit build.gradle.kts
- Hilt-Setup (@HiltAndroidApp)
- Room-DB Grundgeruest (leere DB v1)
- :core:ui Theme (warme Palette: Honiggelb, Terracotta, Creme)
- MainActivity mit Compose-Scaffold + Bottom-Nav (Platzhalter)

### Phase 2: Tierprofile
- Room Entities: Pet, PetPhoto + DAOs
- Repository + ViewModel fuer PetList, PetDetail, PetEdit
- UI: Tierliste, Detail, Formular
- Profilbild (CameraX/System-Intent + Galerie)
- Thumbnail-Generierung in :core:media
- Coil-Integration

### Phase 3: Gesundheitsmanagement
- Room Entities: Vaccination, MedicalRecord, Medication, Reminder + DAOs
- Impf-Turnus-Berechnung
- UI: Impf-Liste, Medizinische Akte, Medikamenten-Verwaltung
- WorkManager Erinnerungen + Notification Channels
- Gesundheits-Dashboard

### Phase 4: Medienverwaltung
- Foto-Galerie (LazyVerticalGrid)
- Multi-Foto-Upload
- Vollbild mit Zoom
- Disk-Cache-Management

### Phase 5: Multi-Device & Cloud-Sync
- Firebase-Projekt (Auth, Firestore, Storage, FCM)
- Auth: Google, Facebook, Microsoft Sign-In
- :feature:family -- Familie erstellen, Mitglieder einladen
- Firestore-Struktur + Security Rules
- SyncWorker (Push/Pull) + Konfliktaufloesung
- Photo-Upload zu Firebase Storage
- ReminderRefreshWorker

### Phase 6: Polish & Release
- R8/ProGuard (isMinifyEnabled = true)
- Firebase Crashlytics
- Baseline Profiles, Startup-Tracing
- Accessibility (ContentDescription, Touch-Targets)
- Datenschutzerklaerung
- App-Icon, Splash-Screen
- Edge-Case-Tests (Offline, viele Tiere, grosse Bilder)
