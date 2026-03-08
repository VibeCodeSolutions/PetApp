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

### Sprint 2.2: Pet-Listen-UI ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** PetList-Screen mit ViewModel
**Dateien:** `:feature:pets`
- [x] `PetListUiState` (Loading, Success, Empty)
- [x] `PetListViewModel` mit StateFlow (stateIn WhileSubscribed 5s)
- [x] `PetListScreen` Composable (LazyColumn, PetCard mit Coil-Placeholder, key=pet.id)
- [x] Navigation-Integration in App NavHost (TiereRoute)
- [x] FAB zum Hinzufuegen
- [x] Unit-Tests: 4 Tests (MainDispatcherRule, FakePetRepository)
- [ ] Verifizierung: Liste zeigt Tiere aus DB an (ggf. mit Testdaten)

### Sprint 2.3: Pet-Erstellen/Bearbeiten-UI ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Formular zum Anlegen und Bearbeiten von Tieren
**Dateien:** `:feature:pets`
- [x] `PetEditUiState` (Loading, Editing, SavedSuccess)
- [x] `PetEditViewModel` (Create + Edit Modus via SavedStateHandle["petId"])
- [x] `PetEditScreen` Composable (Name, Geburtsdatum, Tierart, Rasse, Chip-Nr., Farbe, Gewicht, Notizen)
- [x] Species-Auswahl (ExposedDropdownMenuBox)
- [x] DatePicker via Material3 DatePickerDialog
- [x] Validierung (Name Pflicht, Chip-Nr. 15 Ziffern)
- [x] Navigation: TiereRoute -> TierBearbeitenRoute, TierDetailRoute -> TierBearbeitenRoute
- [x] Unit-Tests: 7 Tests
- [ ] Verifizierung: Tier anlegen, bearbeiten, in Liste sichtbar

### Sprint 2.4: Pet-Detail & Profilbild ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Detail-Ansicht + Bild-Aufnahme/Auswahl
**Dateien:** `:feature:pets`, `:core:media`, `:core:database`
- [x] `PetDetailScreen` Composable (alle Infos, 160dp-Profilbild, InfoRow-Tabelle)
- [x] `PetDetailViewModel` (combine petRepo + petPhotoRepo zu einem StateFlow)
- [x] Bild-Picker: Kamera (TakePicture) + Galerie (PickVisualMedia) mit PhotoSourceDialog
- [x] FileProvider konfiguriert (authority: com.example.tierapp.fileprovider)
- [x] `ThumbnailManager` Interface + `ThumbnailManagerImpl` in `:core:media` (150x150 + 400x400, quadratischer Crop)
- [x] Coil-Integration: AsyncImage mit rememberVectorPainter-Placeholder
- [x] `PetPhotoEntity` + `PetPhotoDao` + `PetPhotoRepository` (Interface + Impl + HiltModule)
- [x] DB-Migration 2->3 (pet_photo-Tabelle mit FK auf pet, CASCADE DELETE)
- [x] Unit-Tests: 5 Tests (FakeThumbnailManager als object)
- [ ] Verifizierung: Profilbild aufnehmen/auswaehlen, Thumbnail wird generiert, Bild wird angezeigt

---

## Phase 3: Gesundheitsmanagement

### Sprint 3.1: Vaccination-Entity & UI ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Impfungen erfassen und anzeigen
**Dateien:** `:core:database`, `:feature:health`
- [x] `Vaccination` Room-Entity + `VaccinationDao`
- [x] DB-Migration 3->4
- [x] `VaccinationRepository` (Interface in :core:model, Impl in :core:database)
- [x] `VaccinationListViewModel` + `VaccinationListScreen`
- [x] Impfung-hinzufuegen-Formular (Name, Datum, Turnus, Tierarzt, Chargen-Nr.)
- [x] Turnus-Berechnung: `nextDueDate = dateAdministered + intervalMonths`
- [ ] Verifizierung: Impfung anlegen, naechster Termin wird berechnet

### Sprint 3.2: Medizinische Akte & Medikamente ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Allergien, Diagnosen, Medikamente erfassen
**Dateien:** `:core:database`, `:feature:health`
- [x] `MedicalRecord` Room-Entity + DAO + Repository
- [x] `Medication` Room-Entity + DAO + Repository
- [x] DB-Migration 4->5 (medical_record + medication in einer Migration)
- [x] UI: MedicalRecord-Liste (FilterChip nach Typ via flatMapLatest), Hinzufuegen-Formular
- [x] UI: Medikamenten-Liste, Hinzufuegen-Formular (Name, Dosierung, Frequenz, Vorrat), Vorrat-Update-Dialog
- [x] Nachschub-Berechnung: `daysRemaining = currentStock / dailyConsumption` (Domain-Property)
- [ ] Verifizierung: Allergien, Medikamente anlegen und anzeigen

### Sprint 3.3: Erinnerungen & Notifications ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** WorkManager-Scheduling, Notification Channels, Reminder-Entity
**Dateien:** `:core:notifications`, `:core:database`
- [x] `Reminder` Room-Entity + DAO (Unique-Index auf referenceId+triggerAt)
- [x] DB-Migration 5->6
- [x] 5 Notification Channels erstellen (in Application.onCreate)
- [x] `DailyHealthCheckWorker` (@HiltWorker): prueft Impftermine + Medikamentenvorraete
- [x] Lokale Notifications: Impfung (4W/1W/Tag vorher), Nachschub (bei Schwelle)
- [x] Erinnerungen in Room speichern (isCompleted, isSnoozed)
- [x] `TierappApplication` implementiert `Configuration.Provider` fuer HiltWorkerFactory
- [ ] Verifizierung: Worker laeuft, Notification wird angezeigt

### Sprint 3.3-Nacharbeit: Health-DB-Registrierung ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Vaccination/MedicalRecord/Medication/Reminder in TierappDatabase registrieren
**Dateien:** `:core:model`, `:core:database`
- [x] Domain-Modelle erstellt: `Vaccination`, `MedicalRecord`, `Medication`, `Reminder` + Repository-Interfaces
- [x] Room-Entities erstellt: `VaccinationEntity`, `MedicalRecordEntity`, `MedicationEntity`, `ReminderEntity`
- [x] DAOs erstellt: `VaccinationDao`, `MedicalRecordDao`, `MedicationDao`, `ReminderDao`
- [x] Repository-Implementierungen erstellt + Hilt-Module in `:core:database/di`
- [x] `MIGRATION_5_6` in `Migrations.kt` (erstellt vaccination + medical_record + medication + reminder)
- [x] `TierappDatabase` auf version = 6; alle 4 Health-Entities registriert
- [x] `DatabaseModule` um MIGRATION_5_6 + 4 DAO-Provider erweitert

### Sprint 3.4: Gesundheits-Dashboard ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Aggregierte Uebersicht aller faelligen Aktionen
**Dateien:** `:feature:health`, `:app`
- [x] `HealthDashboardViewModel` (combine: Vaccination + Medication + Reminder Flows)
- [x] `HealthDashboardScreen`: faellige Impfungen, niedrige Vorraete, offene Erinnerungen
- [x] Sortierung nach Dringlichkeit (CRITICAL/WARNING/INFO)
- [x] Quick-Actions: Erledigt-Markieren + Schlummern (1 Tag) fuer Reminder
- [x] Navigation: GesundheitRoute -> HealthDashboardRoute; ImpfungenRoute, MedizinAkteRoute, MedikamenteRoute registriert
- [x] PetDetailScreen um Health-Navigations-Section erweitert
- [ ] Verifizierung: Dashboard zeigt korrekte aggregierte Daten

---

## Phase 4: Medienverwaltung

### Sprint 4.1: Foto-Galerie ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Galerie-Grid pro Tier, Multi-Upload, Vollbild
**Dateien:** `:feature:gallery`, `:core:media`, `:app`
- [x] `GalleryViewModel` + `GalleryScreen` (LazyVerticalGrid, 3 Spalten)
- [x] Multi-Foto-Auswahl (PickMultipleVisualMedia)
- [x] Thumbnail-Anzeige im Grid (Thumb-M 400x400, Coil AsyncImage)
- [x] Vollbild-Ansicht mit Zoom (Modifier.transformable, coerceIn 1x-5x, graphicsLayer)
- [x] Foto-Loeschen mit Confirmation-Dialog (soft-delete, Room-Flow aktualisiert UI automatisch)
- [x] Disk-Cache-Management (Coil SingletonImageLoader.Factory: 64MB RAM / 250MB Disk)
- [x] Navigation: PetDetail -> Gallery (TierGalerieRoute, OutlinedCard in PetDetailContent)
- [x] 9 Unit-Tests fuer GalleryViewModel (Test-First)
- [x] Memory-Leak-Review: scale-State in remember(photoPath), kein ViewModel-Zoom-State
- [ ] Verifizierung: Fotos hochladen, Grid anzeigen, Vollbild zoomen, loeschen

---

## Phase 5: Multi-Device & Cloud-Sync

### Sprint 5.1: Firebase-Setup & Auth ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Firebase-Projekt, Auth Google-only (Facebook + Microsoft ausgeklammert)
**Dateien:** `:core:network`, `:app`
- [x] Firebase-Projekt anlegen (Firestore, Auth, Storage)
- [x] `google-services.json` einbinden
- [x] Firebase Auth: Google Sign-In via Credential Manager
- [x] `TierResult<T>` + `AuthUser` in `:core:model`
- [x] `AuthDataSource` (internal) + `FirebaseAuthDataSource` + `FirebaseAuthRepository` + `AuthModule` in `:core:network`
- [x] `LoginUiState` + `LoginViewModel` + `LoginScreen` in `:app/auth/`
- [x] Auth-Gate in `MainActivity` (LoginScreenRoute als startDestination wenn unauthenticated)
- [x] 17 Unit-Tests (9 FirebaseAuthRepositoryTest + 8 LoginViewModelTest)
- [ ] Verifizierung: Login/Logout mit Google funktioniert (erfordert manuelle Setup-Schritte: SHA-1 in Firebase Console)

### Sprint 5.5: Integration — FamilyScreen in NavHost ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** FamilyScreen in NavHost einhaengen
**Dateien:** `:app`, `:feature:family`
- [x] `FamilyScreen` in `composable<FamilieRoute>` eingehaengt (currentUser aus authState)
- [x] `FamiliePlaceholderScreen` entfernt
- [x] `:feature:family` als Dependency in `app/build.gradle.kts` ergaenzt

### Sprint 5.2: Familien-Verwaltung ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Familie erstellen, Mitglieder einladen
**Dateien:** `:feature:family`, `:core:network`, `:core:database`
- [x] Family + FamilyMember Room-Entities + DAOs (FamilyDao, FamilyEntity, FamilyMemberEntity)
- [x] DB-Migration 4->5 (family + family_member Tabellen)
- [x] Firestore-Struktur: `/families/{id}/members/{userId}` + `/families/{id}/pets/{petId}/...`
- [x] FamilyFirestoreDataSource (pushFamily, getFamilyByInviteCode, addMember) in :core:network
- [x] FamilyRepositoryImpl in :feature:family (koordiniert Room SSOT + Firestore; kein Circular Dep)
- [x] FamilyModule Hilt-Binding in :feature:family/di/
- [x] Familie erstellen (Name, Creator wird Owner, 8-stelliger Invite-Code generiert)
- [x] Einladungs-Mechanismus: Invite-Code (8 Zeichen, Base32-Zeichensatz) + Clipboard-Sharing
- [x] Beitreten per Code: Firestore-Lookup whereEqualTo("inviteCode") + Room-Write
- [x] Mitglieder-Uebersicht: FamilyScreen (InviteCodeCard + MemberRow-Liste)
- [x] FamilyViewModel (createFamily, joinByInviteCode, dismissError; State via combine)
- [x] Firestore Security Rules (docs/firestore.rules): nur Mitglieder lesen/schreiben; Self-Join erlaubt
- [x] familyId-Fallback (uid) beseitigt in SyncWorker, PhotoUploadWorker, RealtimeSyncObserver
- [x] 7 Unit-Tests FamilyViewModelTest (Test-First), FakeFamilyDao, FakeFamilyRepository
- [x] RealtimeSyncObserverTest: +1 Test (onStart ohne Familie)
- [ ] Verifizierung: Familie erstellen, zweiter User kann beitreten (manuell, erfordert 2 Geraete)
- [ ] FamilyScreen noch nicht in NavHost eingehaengt (Sprint 5.5)

### Sprint 5.3: Sync-Engine ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** SyncWorker, Push/Pull, Konfliktaufloesung
**Dateien:** `:core:sync`
- [x] `SyncWorker` (@HiltWorker, PeriodicWork 15min + OneTime bei Aenderungen, exponential Backoff)
- [x] Push: alle PENDING-Entities zu Firestore (WriteBatch, max 500 Docs)
- [x] Pull: `SyncEngine.pull()` -- `getPetsModifiedSince` + `getPhotosModifiedSince` (seit letztem Sync-Timestamp)
- [x] Konfliktaufloesung: `SyncResolver` mit Last-Write-Wins + Feldvergleich (PENDING vs SYNCED)
- [x] Soft-Delete-Sync (isDeleted propagieren, DeleteLocal/DeleteRemote Decisions)
- [x] `SyncPreferences` (SharedPreferences) fuer lastSyncTimestamp
- [x] `SyncScheduler` fuer periodischen + einmaligen Sync + PhotoUpload-Scheduling
- [x] 12 SyncResolverTest + 9 SyncEngineTest (Test-First)
- [x] `ReminderRefreshWorker` implementiert (Health-DB vorhanden via Sprint 3.3-Nacharbeit) -- siehe Post-6.3
- [ ] Verifizierung: Aenderung auf Geraet A erscheint auf Geraet B

### Sprint 5.4: Bild-Sync ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** Fotos zu Firebase Storage hochladen, Realtime-Listener
**Dateien:** `:core:sync`, `:core:network`
- [x] `PhotoUploadWorker` (@HiltWorker, OneTime, CONNECTED + NOT_LOW_BATTERY)
- [x] `PhotoUploadEngine`: Upload Original + Thumb-S + Thumb-M -> Firebase Storage
- [x] Upload-Status-Tracking (LOCAL_ONLY -> UPLOADING -> UPLOADED / FAILED)
- [x] Retry mit exponential Backoff (max 3 Versuche)
- [x] `FirestorePetDataSource`: +observePets (callbackFlow, SnapshotListener) + observePhotos
- [x] `RealtimeSyncObserver` (DefaultLifecycleObserver, ON_START/ON_STOP, @ApplicationScope, kein GlobalScope)
- [x] `SyncEngine.applyRemoteSnapshot()` fuer Realtime-Snapshots (Firestore -> Room)
- [x] `TierappApplication`: schedulePeriodicSync() + realtimeSyncObserver.register() in onCreate()
- [x] 7 PhotoUploadEngineTest + 6 RealtimeSyncObserverTest
- [ ] observePhotos: collectionGroup ohne Firestore-Index (MVP-Kompromiss, bis ~100 Fotos ok)
- [ ] Verifizierung: Foto auf Geraet A aufnehmen, auf Geraet B sichtbar

---

## Phase 6: Polish & Release

### Sprint 6.1: Performance & Sicherheit ✅ ABGESCHLOSSEN (2026-03-07)
**Scope:** R8, Crashlytics, Baseline Profiles
**Dateien:** `:app`, `:macrobenchmark`, `proguard-rules.pro`
- [x] `isMinifyEnabled = true` + `isShrinkResources = true` in release buildType
- [x] ProGuard-Regeln: Crashlytics, Firebase, Room, WorkManager, kotlinx.serialization, Coil3, Credential Manager
- [x] Firebase Crashlytics integriert (BOM-managed, kein version.ref)
- [x] `:macrobenchmark` Modul (com.android.test): `BaselineProfileGenerator` + `TierappStartupBenchmark`
- [x] `androidx.profileinstaller` in `:app`; `android.baselineprofile` Plugin in `:app` + `:macrobenchmark`
- [ ] Verifizierung: Release-Build laeuft, Crashlytics empfaengt Test-Crash

### Sprint 6.2: UX-Polish & Accessibility ✅ ABGESCHLOSSEN (2026-03-08)
**Scope:** Feinschliff, Accessibility, Datenschutz
**Dateien:** `:feature:pets`, `:feature:gallery`, `:feature:family`, `:app`
- [x] ContentDescription fuer alle interaktiven Elemente (PetCard semantics, Galerie-Fotos "Foto N", IconButtons bereits korrekt via Material3)
- [x] Touch-Targets mindestens 48dp (Material3 IconButton liefert 48dp automatisch; PetCard-Flaeche groesser als 48dp durch Padding)
- [x] Animationen: `Crossfade` (PetListScreen, GalleryScreen), `animateItem()` (LazyColumn), `AnimatedVisibility` (FamilyScreen Join-Feld)
- [x] Empty-States verbessert (Icon + 2-zeiliger Hint in PetList und Gallery)
- [x] Error-States mit Retry-Buttons (`PetListUiState.Error`, `GalleryUiState.Error`, `ErrorContent`-Composable)
- [x] Retry-Logik in ViewModels (`_retryTrigger` + `retry()` + `.catch{}` in Flow-Chain)
- [x] Datenschutzerklaerung-Screen (`DatenschutzScreen.kt`, `DatenschutzRoute` in NavHost, Link im LoginScreen)
- [ ] Verifizierung: TalkBack-Durchlauf, keine fehlenden Descriptions

### Sprint 6.3: Release-Vorbereitung ⏳ IN BEARBEITUNG (2026-03-08)
**Scope:** Edge-Case-Bugfixes, Assets, App-Icon, Splash, Store-Listing
**Dateien:** `:app`, `:core:sync`, `:core:database`, `:core:media`, Ressourcen

**Edge-Case-Bugfixes (2026-03-08):**
- [x] OOM-Fix `ThumbnailManagerImpl`: Zwei-Pass `inSampleSize`-Decode -- kein Full-Bitmap mehr im Heap; `calculateInSampleSize()` (Potenz-von-2)
- [x] `SyncResult` sealed class angelegt (`Success`, `TransientError(cause)`, `PermanentError(cause)`)
- [x] `SyncEngine.sync()`: Rueckgabetyp `Boolean` -> `SyncResult`; `FirebaseFirestoreException`-Klassifizierung (PERMISSION_DENIED/UNAUTHENTICATED/INVALID_ARGUMENT/NOT_FOUND = Permanent; Rest = Transient)
- [x] `SyncEngine.push()`: Listen-Chunking `chunked(FIRESTORE_BATCH_LIMIT=400)` fuer PENDING-Pets und -Photos
- [x] `SyncWorker`: `SyncResult`-Handling -- `PermanentError -> Result.failure()`, `TransientError -> Result.retry()` (mit MAX_RETRIES=3-Guard)
- [x] `PetPhotoDao.getPhotosNeedingUpload()`: `ORDER BY createdAt ASC LIMIT 200` (Pagination)
- [x] `PhotoUploadEngine`: Early-Exit-Loop nach `MAX_CONSECUTIVE_FAILURES=2` aufeinanderfolgenden Fehlern; Counter-Reset bei Erfolg
- [x] `core/sync/build.gradle.kts`: +`firebase-firestore-ktx`; +`androidTestImplementation`
- [x] `SyncStressTest.kt` angelegt (7 Tests): Batch-Chunking (950->3 Chunks), Early-Exit-Verhalten, Counter-Reset, `inSampleSize`-Kalkulation 4000x3000/400x300/200x200

**Asset-Integration (2026-03-08):**
- [x] 4 PNG-Assets von `app/assets/` nach `app/src/main/assets/` verschoben (Standard-Android-Verzeichnis)
- [x] `PetListScreen`: `background1_Dashboard.png` als `AsyncImage` hinter transparentem `Scaffold` (`containerColor = Color.Transparent`)
- [x] `GalleryScreen`: `background3_Gallery.png` als erstes Kind der aeusseren `Box`, transparenter `Scaffold`
- [x] `LoginScreen`: `foreground.png` als Vollbild-Hintergrund + schwarzer Scrim (`Color.Black.copy(alpha=0.45f)`) fuer Lesbarkeit
- [x] `background2_health.png` in `assets/` bereitgestellt (Einbindung bei Health-UI-Screen-Implementierung)

**Noch offen:**
- [x] App-Icon (Adaptive Icon mit Tier-Motiv)
- [x] Splash-Screen (Core Splashscreen API)
- [x] App-Name finalisieren (strings.xml)
- [x] Screenshots fuer Store-Listing (entfaellt -- App nur fuer privaten Gebrauch)
- [x] Edge-Case-Tests: Offline-Modus, 50+ Tiere, 100+ Fotos, aeltere Geraete (API 27)
- [x] Signed Release-APK / AAB erstellen
- [ ] Verifizierung: Vollstaendiger Durchlauf aller Features auf Testgeraet

---

### Post-6.3 Bugfixes & Feature-Completions ✅ ABGESCHLOSSEN (2026-03-08)
**Scope:** EXIF-Rotation, Health-UI, Settings, Family-Code-Normalisierung, Branding, Mitglieder-Sync

**Fix 1 — EXIF-Bildrotation:**
- [x] `ThumbnailManagerImpl`: `applyExifRotation()` via `androidx.exifinterface:1.3.7`; korrigiert Hochformat-Fotos aus Galerie/Kamera vor dem Crop
- [x] `gradle/libs.versions.toml`: +`exifinterface = "1.3.7"` + Library-Eintrag; `core/media/build.gradle.kts`: +`androidx-exifinterface`

**Fix 2 — Health-UI vollstaendig:**
- [x] `feature/health/`: `HealthUiState.kt`, `HealthViewModel.kt`, `HealthScreen.kt` erstellt (VaccinationList + MedicalRecords + Medications in TabRow)
- [x] `feature/health/build.gradle.kts`: +`:core:database`, +`compose.material.icons.extended`, +`testImplementation`
- [x] `MainActivity.kt`: `GesundheitRoute` -> `HealthRoute()` (kein Platzhalter mehr)
- [x] `app/build.gradle.kts`: +`implementation(project(":feature:health"))`

**Fix 3 — Settings vollstaendig:**
- [x] `feature/settings/`: `SettingsUiState.kt` (+`ThemeMode` enum), `SettingsViewModel.kt` (DataStore), `SettingsScreen.kt`, `di/SettingsModule.kt` erstellt
- [x] `feature/settings/build.gradle.kts`: +`compose.material.icons.extended`
- [x] `MainActivity.kt`: `EinstellungenRoute` -> `SettingsRoute(onLogout = { signOut + nav })` (kein Platzhalter mehr); `SettingsViewModel` fuer `TierappTheme(darkTheme=)`
- [x] `app/build.gradle.kts`: +`implementation(project(":feature:settings"))`; +`signingConfigs` aus Env-Vars

**Fix 4 — Family-Join-Bugfixes:**
- [x] `FamilyRepositoryImpl.joinByInviteCode()`: `inviteCode.trim().uppercase()` vor Firestore-Lookup
- [x] `FamilyRepositoryImpl.joinByInviteCode()`: `addMember()` VOR `fetchMembers()` -- behebt deterministischen "Permission Denied" (Security Rule `isFamilyMember` prueft Member-Dokument, das vorher fehlt)
- [x] `docs/bugfix-family-join-permission-denied.md`: TCRTE-Analyse des Bugs
- [x] `feature/family/src/test/.../FamilyRepositoryImplTest.kt`: 3 Unit-Tests (InOrder-Verifikation addMember→fetchMembers, ungültiger Code, Code-Normalisierung)

**Fix 5 — Branding:**
- [x] `strings.xml`: +`branding_footer` ("VibeCode Solutions")
- [x] `MainActivity.kt`: Branding-Footer (`Text`) unterhalb `NavigationBar` (alpha=0.35f)

**Mitglieder-Sync (Realtime):**
- [x] `FamilyFirestoreDataSource`: +`fetchMembers(familyId)` (einmalig) + `observeMembers(familyId)` (callbackFlow)
- [x] `RealtimeSyncObserver`: +Members-Listener via `familyFirestoreDataSource.observeMembers()`; alle collect-Bloecke in `runCatching` (kein Silent-Crash)
- [x] `docs/firestore.rules`: `allow read` aufgeteilt in `allow list` (authenticated, fuer Invite-Code-Lookup) + `allow get` (Mitglied)

**AndroidTests:**
- [x] `core/database/src/androidTest/EdgeCaseStressTest.kt`: DB-Edge-Cases
- [x] `core/database/build.gradle.kts`: +`room-testing`, +`junit4`, +`androidx.junit.ext`, +`kotlinx.coroutines.test`

---

### ReminderRefreshWorker ✅ ABGESCHLOSSEN (2026-03-08)
**Scope:** Background-Worker fuer Notification-Refresh nach erfolgreichem Sync

**Neue Dateien:**

| Modul | Datei |
|---|---|
| `:core:notifications` | `ReminderRefreshScheduler.kt` -- Interface fuer testbare DI in :core:sync |
| `:core:notifications` | `ReminderRefreshWorker.kt` -- `@HiltWorker`: VaccinationDao + MedicationDao; storniert Health-Notifications (per Tag), postet neue fuer Impftermine (30-Tage-Fenster) und Low-Stock-Medis (< 5 Tage Vorrat) |
| `:core:notifications` | `WorkManagerReminderRefreshScheduler.kt` -- Impl: `enqueueUniqueWork(REPLACE, OneTimeWorkRequest)` |
| `:core:notifications` | `di/NotificationsModule.kt` -- Hilt: `@Binds ReminderRefreshScheduler` + `@Provides WorkManager` |
| `:core:sync` (test) | `SyncWorkerTest.kt` -- 8 Unit-Tests via `SyncWorkerDelegate` (Lambda-Ansatz, kein WorkManager-Infra) |

**Geaenderte Dateien:**

| Datei | Aenderung |
|---|---|
| `core/notifications/build.gradle.kts` | +:core:database, +work-runtime-ktx, +hilt-work, +ksp(hilt-compiler) |
| `core/sync/build.gradle.kts` | +:core:notifications (impl), +mockito-kotlin (testImpl) |
| `core/sync/.../SyncWorker.kt` | +`reminderRefreshScheduler: ReminderRefreshScheduler` via `@AssistedInject`; `scheduleOneTimeRefresh()` bei `SyncResult.Success` |
| `gradle/libs.versions.toml` | +`mockitoKotlin = "5.4.0"` + Library-Eintrag (bereits in feature/family vorhanden) |

**Architektur-Entscheidung:**
- `:core:notifications` abhaengig von `:core:database`; `:core:sync` abhaengig von `:core:notifications` -- kein Zyklus
- `ReminderRefreshScheduler`-Interface in `:core:notifications`: entkoppelt `SyncWorker` von WorkManager-Laufzeit in Tests
- `SyncWorkerDelegate` (Lambda-basiert, kein `CoroutineWorker`-Wrapper): testet Business-Logik ohne Reflection-Mocking finaler Kotlin-Klassen

---

### Code-Review Phase 6 ✅ ABGESCHLOSSEN (2026-03-08)
**Scope:** 12 Befunde aus Opus-Architektur-Review (3 CRITICAL, 5 WARNING, 4 REFACTOR)
**Dateien:** 14 Dateien in 6 Modulen

**CRITICAL — 3 Befunde:**
- [x] `PetDetailViewModel.kt`: `generateThumbs()` in `withContext(Dispatchers.IO)` — blockierender Bitmap-I/O nicht mehr auf Main-Thread
- [x] `HealthViewModel.kt`: `_retryTrigger: MutableStateFlow<Int>` + `flatMapLatest`-Wrapper um gesamten uiState-Flow — Flow terminiert nach Fehler nicht mehr dauerhaft; `dismissError()` inkrementiert Trigger
- [x] `PetPhotoDao.kt`: `getPhotosNeedingUpload()` erfasst jetzt auch `uploadStatus = 'UPLOADING'` — verhindert dauerhaft verwaiste Fotos bei Worker-Kill

**WARNING — 5 Befunde:**
- [x] `SyncEngine.kt`: Race-Condition-Fix — SYNCED wird nur gesetzt wenn `syncStatus == PENDING && updatedAt == pet.updatedAt` (verhindert Silent-Drop von Aenderungen zwischen Push und Status-Update)
- [x] `MainActivity.kt`: `FirebaseAuth.getInstance().signOut()` -> `authViewModel.signOut()` (Clean Architecture, kein direkter Firebase-Zugriff in UI); Import entfernt
- [x] `FamilyViewModel.kt`: Double-Subscribe-Fix -- `combine(observeCurrentFamily(), observeCurrentFamily()...)` -> single `flatMapLatest { family -> observeMembers().map { } }` (1 Room-Observer statt 2)
- [x] `ThumbnailManagerImpl.kt`: EXIF einmalig in `generateThumbs()` via `readExifOrientation()` lesen, als `Int` an `generateThumb()` weitergeben -- 6 -> 5 I/O-Opens pro `generateThumbs`-Aufruf; `applyExifRotationWithOrientation(bitmap, Int)` ohne ContentResolver-Aufruf
- [x] `RealtimeSyncObserver.kt`: Alle 3 Listener (Pets/Photos/Members) in `while(true)` + exponential Backoff (1s → 60s) gewrappt -- Listener stirbt nicht mehr bei transientem Fehler
- [x] `PetListScreen.kt` + `GalleryScreen.kt`: `Crossfade` -> `AnimatedContent(contentKey = { it::class })` -- Animation nur bei State-Typ-Wechsel, nicht bei jedem Daten-Update

**REFACTOR — 4 Befunde:**
- [x] `SyncScheduler.kt`: `familyId`-Parameter + `workDataOf`-Aufrufe entfernt (toter Code, Worker lesen nie InputData); `workDataOf`-Import entfernt
- [x] `FamilyScreen.kt`: `LaunchedEffect(uiState)` -> `LaunchedEffect(errorMessage)` -- LaunchedEffect-Overhead bei jedem State-Wechsel eliminiert
- [x] `FamilyRepositoryImpl.kt`: Doppelter `insertMember`-Call nach `fetchMembers` ersetzt durch Guard `if (allMembers.none { it.userId == member.userId })`
- [x] `LoginViewModel.kt`: Toter `if (e is GetCredentialException) ... else ...`-Branch mit identischen Strings zu `e.message ?: "..."` vereinfacht; `GetCredentialException`-Import entfernt
