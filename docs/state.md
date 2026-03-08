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

**Status:** ABGESCHLOSSEN
**Beginn:** 2026-03-07
**Abschluss:** 2026-03-07

### Ziel
Impfungen, medizinische Akte und Medikamente verwalten mit Erinnerungen.

### Erwarteter Endzustand
- [x] Room Entities: Vaccination, MedicalRecord, Medication, Reminder + DAOs
- [x] Impf-Turnus-Berechnung (nextDueDate = dateAdministered + intervalMonths)
- [x] UI: Impf-Liste, Medizinische Akte, Medikamenten-Verwaltung
- [x] WorkManager-basierte Erinnerungen (DailyHealthCheckWorker, 24h periodic)
- [x] 5 Notification Channels konfiguriert
- [x] Gesundheits-Dashboard (aggregierte Uebersicht, Dringlichkeit-Sortierung)

### Snapshot Sprint 3.1 (2026-03-07)

**Abgeschlossen:** Vaccination-Entity & UI

- `Vaccination` Domain-Modell + `VaccinationRepository` Interface in `:core:model`
- `VaccinationEntity` (FK auf pet, CASCADE, Index petId) + Mapper in `:core:database`
- `VaccinationDao`: `getByPetId` (Flow), `getUpcoming` (Flow, cross-pet), `getUpcomingList` (suspend, fuer Worker), `insert`/`update`/`softDelete`
- DB-Migration 3->4 (vaccination-Tabelle); `TierappDatabase` v4
- `VaccinationRepositoryImpl` mit `Result<T>`-Wrapping via `runCatching`
- `VaccinationRepositoryModule` Hilt-Binding
- `DatabaseModule` um MIGRATION_3_4 + `provideVaccinationDao` erweitert
- `VaccinationListUiState`, `VaccinationFormState` (immutable data classes)
- `VaccinationListViewModel`: StateFlow, Turnus-Berechnung (`nextDueDate = date.plusMonths(intervalMonths)`), `submitVaccination`/`deleteVaccination`
- `VaccinationListScreen`: `ModalBottomSheet` fuer Hinzufuegen, `LazyColumn` mit Key, Snackbar fuer Fehler
- `VaccinationAddForm`: 6 Felder (Name, Datum, Turnus, Tierarzt, Charge, Notizen), Validierung, Live-Berechnung nextDueDate

### Snapshot Sprint 3.2 (2026-03-07)

**Abgeschlossen:** Medizinische Akte & Medikamente

- `MedicalRecord` Domain-Modell + `MedicalRecordRepository` Interface in `:core:model`
- `Medication` Domain-Modell + `MedicationRepository` Interface in `:core:model`
- `Medication.daysRemaining` + `Medication.isLowStock` als berechnete Properties im Domain-Modell
- `MedicalRecordEntity` (FK auf pet, CASCADE) + `MedicationEntity` (FK auf pet, CASCADE) + Mapper
- `MedicalRecordDao`: getByPetId (Flow), getByPetIdAndType (Flow, String-Typ-Parameter), insert/update/softDelete
- `MedicationDao`: getByPetId (Flow), getActiveMedications (Flow, todayEpochDay), getActiveList (suspend, fuer Worker), updateStock, softDelete
- DB-Migration 4->5 (medical_record + medication Tabellen); `TierappDatabase` v5
- `MedicalRecordRepositoryImpl` + `MedicationRepositoryImpl` + jeweilige Hilt-Module
- `MedicalRecordListViewModel`: `flatMapLatest` auf `_activeFilter`-Flow fuer reaktives Typ-Filtering
- `MedicalRecordListScreen`: FilterChip-Leiste (LazyRow, alle MedicalRecordType-Werte), ModalBottomSheet
- `MedicalRecordAddForm`: ExposedDropdownMenuBox fuer Typ-Auswahl
- `MedicationListViewModel`: `updateStock`-Flow mit separatem `StockUpdateState`
- `MedicationListScreen`: Karten faerben sich rot bei isLowStock, Inventory-Icon fuer Vorrat-Update
- `MedicationAddForm`: Vorschau der Reichweite (Tage) direkt im supportingText des Verbrauchs-Felds

### Snapshot Sprint 3.3 (2026-03-07)

**Abgeschlossen:** Erinnerungen & Notifications

- `Reminder` Domain-Modell + `ReminderRepository` Interface in `:core:model`
- `ReminderEntity` mit UNIQUE INDEX auf `(referenceId, triggerAt)` -- verhindert Doppeleintraege ohne explizite Pruefung
- `ReminderDao`: `insertIfNotExists` (OnConflictStrategy.IGNORE nutzt Unique-Index), `getDueList` (suspend, fuer Worker), `getPendingReminders` (Flow, fuer Dashboard)
- DB-Migration 5->6 (reminder-Tabelle + Unique-Index); `TierappDatabase` v6
- `ReminderRepositoryImpl` + `ReminderRepositoryModule`
- `core/notifications/build.gradle.kts`: WorkManager + hilt-work + hilt-compiler (KSP) + :core:database ergaenzt
- `NotificationChannels`: 5 Channels (VACCINATION_REMINDERS HIGH, MEDICATION_REMINDERS HIGH, RESUPPLY_REMINDERS DEFAULT, FAMILY_UPDATES LOW, SYNC_STATUS MIN)
- `DailyHealthCheckWorker` (`@HiltWorker`): scheduleVaccinationReminders (28/7/1 Tage vor nextDueDate), scheduleResupplyReminders (alle aktiven Medis mit isLowStock), postDueNotifications (postet + markiert als erledigt)
- `NotificationScheduler`: `enqueueUniquePeriodicWork(KEEP, 24h, requiresBatteryNotLow)`
- `TierappApplication`: implementiert `Configuration.Provider` fuer `HiltWorkerFactory`, ruft `NotificationChannels.createAll` + `NotificationScheduler.scheduleDailyHealthCheck` in `onCreate` auf

### Snapshot Sprint 3.4 (2026-03-07)

**Abgeschlossen:** Gesundheits-Dashboard & Navigation-Integration

- `HealthDashboardUiState`: `UpcomingVaccinationItem` (mit `daysUntilDue`, `DashboardUrgency`), `lowStockMedications`, `pendingReminders`
- `DashboardUrgency` Enum: CRITICAL (<= 1 Tag), WARNING (<= 7 Tage), INFO (<= 28 Tage)
- `HealthDashboardViewModel`: `combine(vaccinationFlow, medicationFlow, reminderFlow)` -> single StateFlow; `markReminderCompleted`, `snoozeReminder` (1 Tag)
- `HealthDashboardRoute`: `LazyColumn` mit 3 Sektionen, `UpcomingVaccinationCard` (Farbe nach Urgency), `LowStockCard`, `ReminderCard` mit Erledigt/Schlummern-Actions
- `GesundheitRoute` in NavHost ersetzt Platzhalter durch `HealthDashboardRoute`
- Neue Routen in `:app`: `ImpfungenRoute(petId)`, `MedizinAkteRoute(petId)`, `MedikamenteRoute(petId)`
- `PetDetailRoute` um Health-Navigation-Callbacks erweitert (`onVaccinationsClick`, `onMedicalRecordsClick`, `onMedicationsClick`)
- `HealthNavigationSection` in PetDetailContent: 3 `OutlinedCard`-Navigations-Buttons fuer Gesundheits-Unterbereiche
- `app/build.gradle.kts`: `:feature:health`, `:core:notifications`, `work-runtime-ktx` ergaenzt

### HINWEIS: DB-Diskrepanz (Stand 2026-03-07)

Die in Sprints 3.1-3.3 beschriebenen Room-Migrations (3->4 Vaccination, 4->5 MedicalRecord+Medication, 5->6 Reminder) wurden in `TierappDatabase` **nicht registriert**. Die tatsaechliche DB hat Vaccination/MedicalRecord/Medication/Reminder-Entities und DAOs als Kotlin-Dateien, aber sie fehlen im `@Database(entities=[...])` und in `DatabaseModule`. Die tatsaechliche DB-Version war 4 (pet + pet_photo) und ist nach Sprint 5.2 jetzt 5 (pet + pet_photo + family + family_member). Die Health-Entities muessen in einem separaten Sprint nachregistriert werden (Sprint 3.3-Nacharbeit, erfordert MIGRATION_5_6).

### Architektonische Entscheidungen (Phase 3)
- `ReminderEntity` Unique-Index statt manueller Exists-Pruefung: atomarer Upsert via `OnConflictStrategy.IGNORE` -- race-condition-sicher im Worker
- DAO-Methoden fuer Worker als `suspend fun` (List-Return), fuer UI als Flow -- kein `.first()` im Worker noetig
- `Medication.isLowStock` + `daysRemaining` im Domain-Modell (nicht im ViewModel) -- wiederverwendbar in Worker und Dashboard ohne Code-Duplizierung
- `flatMapLatest` fuer reaktives Filter-Switching in `MedicalRecordListViewModel` -- alte Flow-Subscription wird automatisch gecancelt
- `combine()` mit 3 Flows im Dashboard-ViewModel -- einzelne Neuberechnung bei jeder Quell-Emission, kein manuelles Orchestrieren
- Worker greift direkt auf DAOs zu (nicht auf Repositories) -- vermeidet zusaetzliche Abstraktionsebene in Hintergrundprozessen

### Abhaengigkeiten
- Phase 2 muss abgeschlossen sein (Pet-Entity) ✅

---

## Phase 4: Medienverwaltung

**Status:** ABGESCHLOSSEN
**Beginn:** 2026-03-07
**Abschluss:** 2026-03-07

### Ziel
Foto-Galerie pro Tier mit effizienter Bildverwaltung.

### Erwarteter Endzustand
- [x] Foto-Galerie als Grid (LazyVerticalGrid, 3 Spalten)
- [x] Multi-Foto-Upload aus Galerie (PickMultipleVisualMedia)
- [x] Vollbild-Ansicht mit Zoom (Modifier.transformable, coerceIn 1x-5x)
- [x] Disk-Cache-Management (Coil: 64MB RAM / 250MB Disk via SingletonImageLoader.Factory)
- [x] Foto-Loeschen mit Confirmation-Dialog
- [x] Navigation PetDetail -> Galerie (TierGalerieRoute)

### Snapshot Sprint 4.1 (2026-03-07)

**Abgeschlossen:** Foto-Galerie

**Neue Dateien:**
- `feature/gallery/src/main/java/.../GalleryUiState.kt` -- sealed interface: Loading/Empty/Success(photos)
- `feature/gallery/src/main/java/.../GalleryViewModel.kt` -- uiState (stateIn WhileSubscribed 5s), fullscreenPhotoId: StateFlow<String?>, deleteDialogPhotoId: StateFlow<String?>; importPhotos mit withContext(IO) fuer blocking ThumbnailManager-Aufruf
- `feature/gallery/src/main/java/.../GalleryScreen.kt` -- LazyVerticalGrid (3 Spalten, Thumb-M 400x400), FullscreenPhotoView (Box-Overlay mit transformable + graphicsLayer), BackHandler, DeleteConfirmDialog
- `feature/gallery/src/test/java/.../GalleryViewModelTest.kt` -- 9 Unit-Tests (Loading, Empty, Success, Import mit/ohne URIs, Vollbild open/close, Delete confirm/cancel, Auto-Close-bei-Loeschung)
- `feature/gallery/src/test/java/.../MainDispatcherRule.kt`

**Geaenderte Dateien:**
- `feature/gallery/build.gradle.kts` -- coil-compose, lifecycle-runtime-compose, activity-compose, material-icons-extended, testImplementation
- `app/build.gradle.kts` -- +feature:gallery, +coil-compose
- `app/.../TierappApplication.kt` -- implementiert SingletonImageLoader.Factory: MemoryCache 64MB, DiskCache 250MB (coil_cache/), crossfade(true)
- `app/.../MainActivity.kt` -- TierGalerieRoute(petId), composable-Block fuer GalleryRoute, onGalleryClick in TierDetailRoute-Composable
- `feature/pets/.../PetDetailScreen.kt` -- onGalleryClick durch alle Schichten (Route->Screen->Content), OutlinedCard "Foto-Galerie" am Ende von PetDetailContent

**Architektonische Entscheidungen:**
- `fullscreenPhotoId: StateFlow<String?>` speichert Photo-ID statt Index -- robust gegen Listenmutationen bei gleichzeitigem Loeschen; UI loest ID per `photos.firstOrNull { it.id == fullscreenPhotoId }` auf
- `var scale by remember(photoPath)` -- scale-State ist an das aktuell angezeigte Foto gebunden; automatischer Reset beim Foto-Wechsel; lebt nur in Composition (kein ViewModel-State = kein Memory Leak)
- `confirmDelete()` schliesst Vollbild atomar mit Loeschvorgang wenn das angezeigte Foto geloescht wird
- `withContext(Dispatchers.IO)` pro URI innerhalb `viewModelScope.launch` -- blockierender BitmapFactory-Aufruf verlasst Main-Thread; `runCatching` pro URI -- korrupte Dateien ueberspringen Import ohne Abbruch
- Soft-Delete in `PetPhotoDao` (isDeleted=1) -- Room-Flow emittiert automatisch aktualisierte Liste; Vollbild-Overlay verschwindet ohne explizites Close durch die UI

### Abhaengigkeiten
- Phase 2 muss abgeschlossen sein (PetPhoto-Entity, :core:media) ✅

---

## Phase 5: Multi-Device & Cloud-Sync

**Status:** ABGESCHLOSSEN (Sprints 5.1 – 5.5 abgeschlossen)
**Beginn:** 2026-03-07
**Abschluss:** --

### Ziel
Familien-Konten und Offline-First Cloud-Synchronisation.

### Erwarteter Endzustand
- [x] Firebase-Projekt konfiguriert (Auth, Firestore, Storage, FCM)
- [x] Auth: Google Sign-In funktional
- [ ] Auth: Facebook, Microsoft Sign-In (vorlaeufig ausgeklammert, nur Google aktiv)
- [x] Familie erstellen, Mitglieder einladen (8-stelliger Invite-Code)
- [x] Firestore Security Rules (nur Familienmitglieder)
- [x] SyncWorker: Push (PENDING -> Firestore) + Pull (Firestore -> Room)
- [x] Konfliktaufloesung: Last-Write-Wins + feldbasiertes Merging
- [x] PhotoUploadWorker: Bilder -> Firebase Storage
- [ ] ReminderRefreshWorker: Erinnerungen nach Sync aktualisieren (Health-DB jetzt vorhanden -- Sprint 3.3-Nacharbeit erledigt)
- [ ] FamilyScreen in NavHost eingehaengt (Sprint 5.5)

### Snapshot Sprint 5.1 (2026-03-07)

**Abgeschlossen:** Firebase Auth -- Google (Facebook + Microsoft vorlaeufig ausgeklammert)

**Neue Dateien:**
- `core/model/.../TierResult.kt` -- sealed interface `TierResult<T>` (Success/Error) mit `onSuccess`/`onError` Extension-Funktionen; universeller Result-Wrapper fuer alle Repository-Operationen
- `core/model/.../AuthUser.kt` -- Domain-Entity (uid, email, displayName, photoUrl); Firebase-unabhaengig
- `core/network/.../auth/AuthRepository.kt` -- oeffentliches Interface; `authState: Flow<AuthUser?>`, `signInWithGoogle`, `signOut`, `currentUser()`
- `core/network/.../auth/datasource/AuthDataSource.kt` -- internes Interface; kapselt Firebase-Aufrufe; gibt bereits `AuthUser` zurueck (kein `FirebaseUser` ausserhalb dieser Schicht)
- `core/network/.../auth/datasource/FirebaseAuthDataSource.kt` -- `callbackFlow` fuer `authState`, `Task<T>.await()` via `kotlinx-coroutines-play-services`
- `core/network/.../auth/FirebaseAuthRepository.kt` -- `runCatching + fold -> TierResult`; `Dispatchers.IO` fuer credential-Calls
- `core/network/.../auth/di/AuthModule.kt` -- Hilt `@Binds @Singleton` (beide internal) fuer `AuthDataSource` + `AuthRepository`; `@Provides` fuer `FirebaseAuth.getInstance()`
- `core/network/.../auth/FirebaseAuthRepositoryTest.kt` -- 9 Unit-Tests; `FakeAuthDataSource` (kein mockk); prueft authState-Emission, currentUser, signIn-Erfolg/Fehler, signOut-Erfolg/Fehler
- `app/.../auth/LoginUiState.kt` -- `sealed interface`: Unauthenticated / Loading / Authenticated(user) / Error(message)
- `app/.../auth/LoginViewModel.kt` -- `@HiltViewModel`; `init`-Block beobachtet `authState`-Flow reaktiv; `initiateGoogleSignIn(context, webClientId)` startet Credential Manager in `viewModelScope`; `signOut`, `clearError`, `handleError`
- `app/.../auth/LoginScreen.kt` -- 1 Google-Button; `CircularProgressIndicator` bei Loading; `SnackbarHost` fuer Fehler; `LoginRoute` als Composable-Entry-Point mit `hiltViewModel()`
- `app/src/test/.../auth/MainDispatcherRule.kt`
- `app/src/test/.../auth/LoginViewModelTest.kt` -- 8 Unit-Tests; `FakeAuthRepository`; prueft Initialzustand, reaktive Auth-State-Updates, signIn-/signOut-Zustandsuebergaenge inkl. Loading-Durchlauf, clearError

**Geaenderte Dateien:**
- `gradle/libs.versions.toml` -- credentials:1.5.0, googleid:1.1.1 + korrespondierende Libraries + `kotlinx-coroutines-play-services`
- `core/network/build.gradle.kts` -- +firebase-auth-ktx, +kotlinx-coroutines-play-services, +testImplementation junit4/coroutines-test
- `app/build.gradle.kts` -- +core:network, +credentials, +credentials-play-services-auth, +googleid, +testImplementation
- `app/.../MainActivity.kt` -- `LoginScreenRoute` als `@Serializable data object`; Auth-Gate: `startDestination` dynamisch (LoginScreenRoute vs. TiereRoute); `authViewModel.uiState` via `collectAsStateWithLifecycle()`; `composable<LoginScreenRoute>` mit `popUpTo(inclusive=true)` nach Login
- `app/.../res/values/strings.xml` -- 3 neue Auth-Strings (login_title, login_subtitle, login_google, default_web_client_id)

**Architektonische Entscheidungen:**
- `AuthDataSource` gibt `AuthUser` zurueck (nicht `FirebaseUser`) -- `FirebaseUser` ist `final`, kann nicht subclassed werden; Domain-Typen verlassen die DataSource nie als Firebase-Typen; `FakeAuthDataSource` ohne mockk testbar
- `initiateGoogleSignIn(context)` im ViewModel (nicht in Composable-Scope) -- Credential Manager in `viewModelScope`; Activity-Wechsel cancelt den Job korrekt; Context als Parameter (nicht gespeichert) = kein Memory Leak
- `runCatching { ... }.fold(onSuccess, onFailure)` statt try/catch -- einheitliches Error-Handling, kein Exception-Propagation aus Repositories
- `FirebaseAuthRepository` + `AuthModule.bindAuthRepository` als `internal` -- verhindert Visibility-Konflikt (public Klasse mit internal Parameter)

**Scope-Entscheidung:**
- Facebook- und Microsoft-Auth vorlaeufig ausgeklammert (2026-03-07): Nur Google Sign-In aktiv. Facebook-SDK-Abhaengigkeit entfernt. Interface, Repository, DataSource, ViewModel und Tests auf Google-only reduziert. Koennen bei Bedarf wiedereingebaut werden.

**Offene Setup-Schritte (manuell):**
- `strings.xml`: `default_web_client_id` mit echter OAuth-Client-ID ersetzen (aus `google-services.json > oauth_client[type=3].client_id`)
- Firebase Console: SHA-1-Fingerprint fuer Credential Manager hinterlegen (Google Sign-In aktivieren)

### Snapshot Sprint 5.3 + 5.4 + Integration (2026-03-07)

**Abgeschlossen:** Sync-Engine, Bild-Sync, Lifecycle-Wiring

**Neue Dateien (Auswahl):**
- `core/sync/`: `SyncEngine`, `SyncResolver`, `SyncWorker`, `PhotoUploadEngine`, `PhotoUploadWorker`, `SyncScheduler`, `SyncPreferences`, `SharedPrefsSyncPreferences`, `RealtimeSyncObserver`, `ApplicationScope`, `di/SyncModule`
- `core/network/firestore/FirestorePetDataSource`: +`observePets` / `observePhotos` (callbackFlow + awaitClose)

**Geaenderte Dateien:**
- `TierappApplication`: schedulePeriodicSync() + realtimeSyncObserver.register() in onCreate()
- `:app/build.gradle.kts`: +implementation(project(":core:sync"))
- `values/themes.xml`: MaterialComponents -> android:Theme.Material.Light.NoTitleBar (Plattform, kein Dep)

**Architektonische Entscheidungen:**
- Pull-first in `SyncEngine.sync()`: Remote-Aenderungen werden vor Push geprueft, um Konflikt-Resolver korrekt zu triggern
- `SyncResolver`-Entscheidungen: UseRemote, UseLocal, DeleteLocal, DeleteRemote, Skip -- explizit ohne if/else-Kaskaden
- `RealtimeSyncObserver` als `DefaultLifecycleObserver` auf `ProcessLifecycleOwner` -- kein GlobalScope; ON_STOP cancelt Firestore-Listener via awaitClose automatisch
- `observePhotos`: collectionGroup + client-seitiger familyId-Filter (MVP-Kompromiss, bis ~100 Fotos vertretbar)
- `@ApplicationScope CoroutineScope`: SupervisorJob + Dispatchers.Default; ueberlebt Activity-Rotationen

**Tests:** 12 SyncResolverTest + 9 SyncEngineTest + 7 PhotoUploadEngineTest + 5 RealtimeSyncObserverTest = 33

**Offener Punkt nach diesen Sprints:**
- familyId-Fallback (`AuthUser.uid`) in SyncWorker / PhotoUploadWorker / RealtimeSyncObserver bis Sprint 5.2 beseitigt

---

### Snapshot Sprint 5.2 (2026-03-07)

**Abgeschlossen:** Familie-Verwaltung + familyId-Fallback beseitigt

**Neue Dateien (Auswahl):**

| Modul | Datei |
|---|---|
| `:core:model` | `Family.kt`, `FamilyMember.kt`, `FamilyRepository.kt` (Interface) |
| `:core:database` | `entity/FamilyEntity.kt`, `entity/FamilyMemberEntity.kt`, `dao/FamilyDao.kt` |
| `:core:network` | `firestore/FamilyFirestoreDataSource.kt` (Interface + Impl), `di/FamilyFirestoreModule.kt` |
| `:feature:family` | `FamilyRepositoryImpl.kt`, `di/FamilyModule.kt`, `FamilyUiState.kt`, `FamilyViewModel.kt`, `FamilyScreen.kt` |
| `docs/` | `firestore.rules` (Firestore Security Rules) |

**Geaenderte Dateien:**
- `TierappDatabase`: version 4 -> 5; +FamilyEntity, FamilyMemberEntity, FamilyDao
- `migration/Migrations.kt`: +MIGRATION_4_5 (family + family_member Tabellen, FK, Unique-Index)
- `DatabaseModule`: +MIGRATION_4_5, +provideFamilyDao
- `feature/family/build.gradle.kts`: +core:database, +core:network, +testImplementation
- `SyncWorker`, `PhotoUploadWorker`: uid-Fallback -> `familyDao.getCurrentFamilyDirect()?.id`
- `RealtimeSyncObserver`: +familyDao-Parameter; familyId aus Room statt authRepository.uid

**DB-Stand nach Sprint 5.2:** Version 5, Tabellen: pet, pet_photo, family, family_member

**Architektonische Entscheidung -- Circular-Dep-Vermeidung:**
`FamilyRepositoryImpl` in `:feature:family` (Zugriff auf core:database + core:network ohne Kreisbezug). `:core:sync` injiziert `FamilyDao` direkt -- kein Dep auf `:feature:family`.

**Race-Condition-Eliminierung:**
`getCurrentFamilyDirect()` ist ein atomarer Room-Read. SyncWorker/PhotoUploadWorker/RealtimeSyncObserver lesen familyId am Job-Start aus Room -- kein veralteter Wert aus WorkManager-InputData moeglich.

**Firestore Security Rules (docs/firestore.rules):**
- `families/{familyId}`: read = Mitglied; create = authenticated; update = Owner/Admin; delete = Owner
- `families/{familyId}/members/{userId}`: Self-create erlaubt (Join via Code); Owner-controlled delete
- `families/{familyId}/pets/{petId}/photos/{photoId}`: read/write = Mitglied

**Tests:** +7 FamilyViewModelTest + 1 RealtimeSyncObserverTest = 41 gesamt

**Offene Punkte:**
- FamilyScreen noch nicht in NavHost eingehaengt (Sprint 5.5)
- Health-Entities (Vaccination, MedicalRecord, Medication, Reminder) fehlen in DB (Sprint 3.3-Nacharbeit)
- `observePhotos` collectionGroup: Firestore-Index fuer familyId-Feld empfohlen ab ~100 Fotos

### Abhaengigkeiten
- Phasen 1-4 muessen abgeschlossen sein ✅

---

## Phase 5 -- Sprint 5.5 Snapshot (2026-03-07)

**Abgeschlossen:** FamilyScreen in NavHost

- `app/build.gradle.kts`: +`implementation(project(":feature:family"))`
- `MainActivity.kt`: `composable<FamilieRoute>` ersetzt `FamiliePlaceholderScreen()` durch `FamilyScreen(currentUser)` (currentUser aus `authState as LoginUiState.Authenticated`)
- `FamiliePlaceholderScreen`-Composable entfernt

---

## Phase 5 -- Sprint 3.3-Nacharbeit Snapshot (2026-03-07)

**Abgeschlossen:** Health-Entities in DB registriert

**DB-Stand nach Sprint 3.3-Nacharbeit:** Version 6, Tabellen: pet, pet_photo, family, family_member, vaccination, medical_record, medication, reminder

**Neue Dateien:**

| Modul | Datei |
|---|---|
| `:core:model` | `Vaccination.kt`, `VaccinationRepository.kt`, `MedicalRecord.kt`, `MedicalRecordRepository.kt`, `Medication.kt`, `MedicationRepository.kt`, `Reminder.kt`, `ReminderRepository.kt` |
| `:core:database` | `entity/VaccinationEntity.kt`, `entity/MedicalRecordEntity.kt`, `entity/MedicationEntity.kt`, `entity/ReminderEntity.kt` |
| `:core:database` | `dao/VaccinationDao.kt`, `dao/MedicalRecordDao.kt`, `dao/MedicationDao.kt`, `dao/ReminderDao.kt` |
| `:core:database` | `repository/VaccinationRepositoryImpl.kt`, `repository/MedicalRecordRepositoryImpl.kt`, `repository/MedicationRepositoryImpl.kt`, `repository/ReminderRepositoryImpl.kt` |
| `:core:database` | `di/VaccinationRepositoryModule.kt`, `di/MedicalRecordRepositoryModule.kt`, `di/MedicationRepositoryModule.kt`, `di/ReminderRepositoryModule.kt` |

**Geaenderte Dateien:**
- `migration/Migrations.kt`: +`MIGRATION_5_6` (erstellt alle 4 Health-Tabellen + Unique-Index fuer reminder)
- `TierappDatabase.kt`: version 5 -> 6; +4 Health-Entities; +4 abstrakte DAO-Funktionen
- `di/DatabaseModule.kt`: +`MIGRATION_5_6`; +4 DAO-Provider

**Architektonische Entscheidungen:**
- `ReminderEntity` Unique-Index auf `(referenceId, triggerAt)` -- `OnConflictStrategy.IGNORE` als atomarer Duplicate-Guard, keine manuelle Exists-Pruefung noetig
- `MedicationDao.getActiveMedications()` ohne `todayEpochDay`-Parameter -- kein "aktiv bis Datum" im Schema (MVP); Worker liest alle nicht-geloeschten Eintraege
- `Medication.daysRemaining` + `isLowStock` als Domain-Properties -- wiederverwendbar in Worker und Dashboard ohne Code-Duplizierung

**Offene Punkte:**
- `ReminderRefreshWorker` kann jetzt implementiert werden (Health-DB vorhanden)
- `feature/health` UI-Screens (VaccinationListScreen, MedicalRecordListScreen, etc.) noch nicht erstellt

---

## Phase 6: Polish & Release

**Status:** IN BEARBEITUNG (Sprints 6.1 + 6.2 abgeschlossen)
**Beginn:** 2026-03-07
**Abschluss:** --

### Ziel
Produktionsreife: Performance, Sicherheit, Accessibility, Store-Vorbereitung.

### Erwarteter Endzustand
- [x] R8/ProGuard aktiv (isMinifyEnabled = true) — Sprint 6.1
- [x] Firebase Crashlytics integriert — Sprint 6.1
- [x] Baseline Profiles generiert — Sprint 6.1
- [x] Accessibility geprueft (ContentDescription, Touch-Targets) — Sprint 6.2
- [x] Datenschutzerklaerung vorhanden — Sprint 6.2
- [ ] App-Icon + Splash-Screen finalisiert — Sprint 6.3
- [ ] Edge-Case-Tests bestanden (Offline, viele Tiere, grosse Bilder) — Sprint 6.3

### Snapshot Sprint 6.1 (2026-03-07)

**Abgeschlossen:** Performance & Sicherheit

- `app/build.gradle.kts`: `isMinifyEnabled = true`, `isShrinkResources = true`, Crashlytics-Plugin, `firebase-crashlytics` (BOM-managed), `androidx.profileinstaller`, `android.baselineprofile`-Plugin
- `proguard-rules.pro`: Crashlytics Stack-Traces, Firebase/GMS dontwarn, Room-Entities, WorkManager-Worker-Konstruktoren, kotlinx.serialization, Coroutines, Coil3, Credential Manager, OkHttp/Retrofit
- `gradle/libs.versions.toml`: `macrobenchmark`, `profileinstaller`, `uiautomator` Versionen + Library-Eintraege
- `:macrobenchmark` Modul (com.android.test): `BaselineProfileGenerator` + `TierappStartupBenchmark`
- `settings.gradle.kts`: `:macrobenchmark` registriert (15 Module gesamt)

---

### Snapshot Sprint 6.2 (2026-03-08)

**Abgeschlossen:** UX-Polish & Accessibility

**Neue Dateien:**

| Datei | Beschreibung |
|---|---|
| `app/.../DatenschutzScreen.kt` | Scrollbarer Datenschutz-Screen mit TopAppBar + Back-Navigation |

**Geaenderte Dateien:**

| Datei | Aenderung |
|---|---|
| `app/res/values/strings.xml` | +`cd_navigate_back`, +`datenschutz_title/link/content` |
| `app/.../MainActivity.kt` | +`DatenschutzRoute` (@Serializable data object); +`composable<DatenschutzRoute>`; `LoginRoute` erhaelt `onDatenschutzClick`-Lambda |
| `app/.../auth/LoginScreen.kt` | +`onDatenschutzClick` in Route + Screen-Signatur; +`TextButton` (Datenschutzerklaerung) unterhalb Google-Button |
| `feature/pets/.../PetListUiState.kt` | +`Error(val message: String)` State |
| `feature/pets/.../PetListViewModel.kt` | +`_retryTrigger: MutableStateFlow<Int>`; `uiState` Flow wrapped in `_retryTrigger.flatMapLatest`; +`.catch{}`; +`fun retry()` |
| `feature/pets/.../PetListScreen.kt` | +`onRetry`-Lambda; `Crossfade` fuer State-Wechsel; +`ErrorContent` mit Retry-Button; Empty-State mit Icon + 2-zeiligem Hint; `animateItem()` in LazyColumn; `semantics { contentDescription }` auf PetCard |
| `feature/gallery/.../GalleryUiState.kt` | +`Error(val message: String)` State |
| `feature/gallery/.../GalleryViewModel.kt` | +`@OptIn(ExperimentalCoroutinesApi::class)`; +`_retryTrigger`; `uiState` wrapped in `flatMapLatest`; +`.catch{}`; +`fun retry()` |
| `feature/gallery/.../GalleryScreen.kt` | +`onRetry`-Lambda in Route + Screen-Signatur; `Crossfade` fuer State-Wechsel; Empty-State mit `Icons.Default.Photo` + Hint; +`ErrorContent` mit Retry-Button; `contentDescription = "Foto ${index + 1}"` fuer Grid-Items |
| `feature/family/.../FamilyScreen.kt` | +`AnimatedVisibility` (expandVertically+fadeIn / shrinkVertically+fadeOut) um Join-Code-Feld |

**Architektonische Entscheidungen:**
- `_retryTrigger.flatMapLatest { ... }` statt einzelnem `catch` auf aeusserstem Flow: Retry cancelt den laufenden Inner-Flow sauber und startet eine neue Subscription -- kein zustandsbehaftetes Re-Subscribe-Idiom noetig
- `Crossfade(targetState = uiState)` auf sealed-interface-Ebene: Nur Haupt-State-Transitionen werden animiert; Aenderungen innerhalb `Success` (z.B. Listenupdate) triggern keine neue Animation
- `animateItem()` in `LazyColumn` (Compose 1.7+ API): Items gleiten beim Einf\"uegen/Entfernen sanft; ersetzt depreciertes `animateItemPlacement()`
- `AnimatedVisibility` mit `expandVertically` statt `slideInVertically`: Verhindert Content-Clipping ausserhalb des Containers ohne `clipToBounds`-Override
- `IconButton` (Material3) garantiert immer 48dp Touch-Target -- auch wenn die visuelle Groesse kleiner ist (z.B. 40dp im ProfilePhoto). Kein `minimumInteractiveComponentSize()` noetig.

**Touch-Target-Audit:**
- `IconButton` (zurueck, loeschen, kopieren, kamera-overlay): 48dp via M3-Default ✅
- `PetCard` (`clickable` auf Card): Hoehe durch Content-Padding >> 48dp ✅
- `FloatingActionButton`: 56dp via M3-Default ✅
- `Button` / `TextButton`: Mindesthoehe 40dp + internes Touch-Padding = 48dp via M3 ✅
- Grid-Fotos (`clickable`): ~120dp (Bildschirmbreite / 3) ✅

**Offene Punkte:**
- TalkBack-Durchlauf manuell durchfuehren (Geraet erforderlich)
- `PetSpecies.toDisplayName()` dreifach vorhanden (technische Schuld aus Phase 2) -- Konsolidierung in Sprint 6.3 oder spaeter

### Abhaengigkeiten
- Phase 5 muss abgeschlossen sein ✅

---

### Snapshot Sprint 6.3 -- Teilabschluss (2026-03-08)

**Status:** IN BEARBEITUNG -- Edge-Case-Bugfixes + Asset-Integration abgeschlossen

#### Edge-Case-Bugfixes

**Neue Dateien:**

| Modul | Datei |
|---|---|
| `:core:sync` | `SyncResult.kt` -- sealed class: `Success`, `TransientError(cause)`, `PermanentError(cause)` |
| `:core:sync` (androidTest) | `SyncStressTest.kt` -- 7 Integrationstests |

**Geaenderte Dateien:**

| Datei | Aenderung |
|---|---|
| `core/media/.../ThumbnailManagerImpl.kt` | Zwei-Pass-Decode: Pass 1 `inJustDecodeBounds=true`; `calculateInSampleSize()` (Potenz-von-2); Pass 2 mit `inSampleSize` -- verhindert OOM bei Hochaufloesung |
| `core/sync/.../SyncEngine.kt` | `sync()` gibt `SyncResult` statt `Boolean` zurueck; `push()` mit `chunked(FIRESTORE_BATCH_LIMIT=400)` fuer Pets und Photos; `catch(FirebaseFirestoreException)` klassifiziert Permanent- vs. Transient-Fehler; `import firebase.firestore.FirebaseFirestoreException` |
| `core/sync/.../SyncWorker.kt` | `when(syncEngine.sync(familyId))` statt Boolean-Branch: `Success -> success()`, `PermanentError -> failure()`, `TransientError -> retry()` (mit MAX_RETRIES=3-Guard) |
| `core/database/.../dao/PetPhotoDao.kt` | `getPhotosNeedingUpload()`: +`ORDER BY createdAt ASC LIMIT 200` -- verhindert unbegrenzte Batch-Groesse |
| `core/sync/.../PhotoUploadEngine.kt` | `uploadPending()`: Early-Exit-Loop mit `consecutiveFailures`-Zaehler; Abbruch nach `MAX_CONSECUTIVE_FAILURES=2`; Counter-Reset bei Erfolg |
| `core/sync/build.gradle.kts` | +`implementation(libs.firebase.firestore.ktx)`; +`androidTestImplementation(libs.junit4/kotlinx.coroutines.test)` |

**SyncStressTest.kt (7 Tests):**
- `chunking_splits_oversized_list_into_correct_batches`: 950 Items -> 3 Chunks (400+400+150)
- `chunking_single_batch_when_below_limit`: 399 Items -> 1 Chunk
- `upload_engine_early_exit_after_max_consecutive_failures`: 10 Fehler -> Abbruch nach 2
- `upload_engine_resets_consecutive_counter_on_success`: Fehler-Erfolg-Fehler -> kein Early-Exit
- `inSampleSize_4000x3000_for_150px_target_returns_8`: ergibt 16
- `inSampleSize_400x300_for_150px_target_returns_1`: ergibt 2
- `inSampleSize_small_image_returns_1`: 200x200 -> 1

**Architektonische Entscheidungen:**
- `SyncResult` als eigene Datei statt nested class -- vermeidet `SyncEngine.SyncResult`-Qualifier in SyncWorker
- `FIRESTORE_BATCH_LIMIT = 400` statt 500 -- 20% Puffer gegen gleichzeitige Schreiboperationen anderer Worker-Instanzen
- `FirebaseFirestoreException.Code.PERMISSION_DENIED/UNAUTHENTICATED` -> `PermanentError`: kein Retry sinnvoll (Auth-Problem); alle anderen Codes (UNAVAILABLE, DEADLINE_EXCEEDED, ABORTED etc.) -> `TransientError`: Retry mit Backoff sinnvoll
- `MAX_CONSECUTIVE_FAILURES = 2` als `const val` in Companion -> direkt aus Tests referenzierbar (kein Magic Number in Test)

---

#### Asset-Integration

**Neue Verzeichnisse:**
- `app/src/main/assets/` -- Standard-Android-Assets-Verzeichnis (von Gradle automatisch erkannt)

**Verschobene Dateien (von `app/assets/` nach `app/src/main/assets/`):**

| Datei | Verwendung |
|---|---|
| `background1_Dashboard.png` | Hintergrundbild PetListScreen |
| `background2_health.png` | Hintergrundbild Health-Screen (bereitgestellt, Einbindung bei UI-Implementierung) |
| `background3_Gallery.png` | Hintergrundbild GalleryScreen |
| `foreground.png` | Hero-Hintergrundbild LoginScreen |

**Geaenderte Dateien:**

| Datei | Aenderung |
|---|---|
| `feature/pets/.../PetListScreen.kt` | Scaffold in `Box` eingewickelt; `AsyncImage("file:///android_asset/background1_Dashboard.png")` als erstes Kind; `Scaffold(containerColor=Color.Transparent)`; +`import Color` |
| `feature/gallery/.../GalleryScreen.kt` | `AsyncImage("file:///android_asset/background3_Gallery.png")` als erstes Kind der bestehenden aeusseren `Box`; `Scaffold(containerColor=Color.Transparent)` |
| `app/.../auth/LoginScreen.kt` | `AsyncImage("file:///android_asset/foreground.png")` als erstes Kind der bestehenden `Box`; `Box(Modifier.background(Color.Black.copy(alpha=0.45f)))` als Scrim; +`import background, Color, ContentScale, coil3.compose.AsyncImage` |

**Asset-Ladereferenz:** `"file:///android_asset/<dateiname>"` -- wird von Coil's `ContentResolver` ueber `AssetManager` aufgeloest; kein zusaetzlicher Coil-Fetcher noetig.

**Offene Punkte (Sprint 6.3):**
- App-Icon (Adaptive Icon)
- Splash-Screen (Core Splashscreen API)
- App-Name finalisieren
- Signed Release-APK / AAB
- Manuelle End-to-End-Verifizierung auf Testgeraet
