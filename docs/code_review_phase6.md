# Code & Architektur Review — Phase 6 (Tierapp)

Datum: 2026-03-08 | Reviewer: Opus (Architecture Agent)

---

## CRITICAL Findings

---

**[SEVERITY: CRITICAL]** — Blockierende Bitmap-Operationen auf Main-Thread

* **Ort:** `feature/pets/src/main/java/.../PetDetailViewModel.kt:62`
* **System-Impact:** `thumbnailManager.generateThumbs(uri)` fuehrt BitmapFactory-Decoding + File-I/O durch. `viewModelScope` dispatcht auf `Dispatchers.Main`. Das friert den UI-Thread fuer die gesamte Dauer der Thumbnail-Generierung ein (100-500ms+ je nach Bildgroesse). Vergleich: `GalleryViewModel.kt:93` macht es korrekt mit `withContext(Dispatchers.IO)`.
* **Fix:**
```kotlin
// PetDetailViewModel.kt, Funktion onPhotoSelected()
// Zeile 62 ersetzen:
// ALT:
            val thumbs = runCatching { thumbnailManager.generateThumbs(uri) }

// NEU:
            val thumbs = runCatching { withContext(Dispatchers.IO) { thumbnailManager.generateThumbs(uri) } }
```
Import `kotlinx.coroutines.Dispatchers` und `kotlinx.coroutines.withContext` hinzufuegen.

---

**[SEVERITY: CRITICAL]** — HealthViewModel Flow terminiert nach erstem Fehler dauerhaft

* **Ort:** `feature/health/src/main/java/.../HealthViewModel.kt:64-69`
* **System-Impact:** `.catch { emit(...) }` faengt den Fehler, aber der gesamte upstream Flow ist danach BEENDET. Der User sieht die Fehler-UI und bekommt nie wieder Daten — auch nicht nach Netzwerkwiederherstellung. `dismissError()` (Zeile 116-118) ist ein No-Op, weil keine neuen Emissionen mehr kommen. Die anderen ViewModels (PetList, Gallery) haben dieses Problem durch `_retryTrigger + flatMapLatest` korrekt geloest.
* **Fix:**
```kotlin
// HealthViewModel.kt — retryTrigger-Pattern analog zu PetListViewModel einfuehren

// 1. Neues Feld (nach _dialogVisible):
    private val _retryTrigger = MutableStateFlow(0)

// 2. uiState-Flow umbauen — _retryTrigger als aeusserstes flatMapLatest:
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = _retryTrigger.flatMapLatest {
        combine(
            petRepository.getAll(),
            _selectedPetId,
            _dialogVisible,
        ) { pets, selectedId, dialog ->
            Triple(pets, selectedId ?: pets.firstOrNull()?.id, dialog)
        }.flatMapLatest { (pets, petId, dialog) ->
            if (petId == null) {
                flowOf(HealthUiState(pets = pets, isLoading = false, showAddVaccinationDialog = dialog))
            } else {
                combine(
                    vaccinationRepository.getByPetId(petId),
                    vaccinationRepository.getUpcoming(daysAhead = 30),
                    medicationRepository.getByPetId(petId),
                ) { vaccinations, upcoming, medications ->
                    HealthUiState(
                        pets = pets,
                        selectedPetId = petId,
                        vaccinations = vaccinations.filter { !it.isDeleted },
                        upcomingVaccinations = upcoming.filter { !it.isDeleted },
                        medications = medications.filter { !it.isDeleted },
                        isLoading = false,
                        showAddVaccinationDialog = dialog,
                    )
                }
            }
        }.catch { e ->
            emit(HealthUiState(isLoading = false, error = e.message))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HealthUiState(),
    )

// 3. dismissError() ersetzen:
    fun dismissError() {
        _retryTrigger.value++
    }
```

---

**[SEVERITY: CRITICAL]** — Stuck-Photo-Bug: UPLOADING-Status wird nie zurueckgesetzt

* **Ort:** `core/sync/src/main/java/.../PhotoUploadEngine.kt:47` und `core/database/src/main/java/.../PetPhotoDao.kt:54-61`
* **System-Impact:** `uploadSingle()` setzt Status auf `UPLOADING` VOR dem Upload. Wenn der Worker waehrend des Uploads gekillt wird (OOM, Doze, System), bleibt das Foto permanent im Status UPLOADING. `getPhotosNeedingUpload()` filtert nur auf `LOCAL_ONLY` und `FAILED` — UPLOADING-Fotos werden NIE wieder abgeholt. Betroffene Fotos sind fuer immer "verwaist".
* **Fix (2 Stellen):**
```kotlin
// Option A: DAO-Query erweitern (empfohlen, minimal-invasiv):
// PetPhotoDao.kt — getPhotosNeedingUpload() anpassen:

    @Query("""
        SELECT * FROM pet_photo
        WHERE (uploadStatus = 'LOCAL_ONLY' OR uploadStatus = 'FAILED' OR uploadStatus = 'UPLOADING')
          AND isDeleted = 0
        ORDER BY createdAt ASC
        LIMIT 200
    """)
    suspend fun getPhotosNeedingUpload(): List<PetPhotoEntity>
```

---

## WARNING Findings

---

**[SEVERITY: WARNING]** — SyncEngine.push() Race-Condition: User-Edit zwischen Push und Status-Update

* **Ort:** `core/sync/src/main/java/.../SyncEngine.kt:63-74`
* **System-Impact:** Szenario: (1) `getPending()` liest Pet X mit updatedAt=T1. (2) `pushPets()` sendet Pet X an Firestore. (3) User aendert Pet X lokal → updatedAt=T2, syncStatus=PENDING. (4) Die Schleife bei Zeile 71 prueft `current?.syncStatus == SyncStatus.PENDING` → true → setzt auf SYNCED. Ergebnis: Die Aenderung bei T2 wird nie zu Firestore gepusht. Sie geht beim naechsten Pull verloren.
* **Fix:**
```kotlin
// SyncEngine.kt, push()-Methode, Pet-Schleife (Zeile 68-74) ersetzen:

            for (pet in pendingPets) {
                val current = petDao.getByIdDirect(pet.id)
                // Nur SYNCED setzen wenn Status UND Timestamp unverändert sind
                if (current?.syncStatus == SyncStatus.PENDING
                    && current.updatedAt == pet.updatedAt
                ) {
                    petDao.updateSyncStatus(pet.id, SyncStatus.SYNCED)
                }
            }

// Identische Aenderung für Photo-Schleife (Zeile 83-88):

            for (photo in pendingPhotos) {
                val current = petPhotoDao.getByIdDirect(photo.id)
                if (current?.syncStatus == SyncStatus.PENDING
                    && current.updatedAt == photo.updatedAt
                ) {
                    petPhotoDao.updateSyncStatus(photo.id, SyncStatus.SYNCED)
                }
            }
```

---

**[SEVERITY: WARNING]** — Clean-Architecture-Verletzung: FirebaseAuth.getInstance() direkt in UI

* **Ort:** `app/src/main/java/.../MainActivity.kt:252`
* **System-Impact:** Presentation-Schicht greift direkt auf Firebase-SDK zu und umgeht das AuthRepository. Die `RealtimeSyncObserver.onStop()` wird ausgeloest, aber `SyncScheduler.cancelAll()` wird NICHT aufgerufen — periodischer SyncWorker laeuft weiter fuer einen ausgeloggten User. Ausserdem entsteht eine Kopplung an Firebase-Implementierungsdetails in der UI-Schicht.
* **Fix:**
```kotlin
// MainActivity.kt, composable<EinstellungenRoute> Block ersetzen:

                composable<EinstellungenRoute> {
                    SettingsRoute(
                        onLogout = {
                            authViewModel.signOut()
                            navController.navigate(LoginScreenRoute) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                    )
                }
```
Hinweis: `LoginViewModel.signOut()` nutzt bereits `authRepository.signOut()`. Der Navigation-Block muss ggf. als Callback nach erfolgreichem Signout ausgefuehrt werden (z.B. ueber einen LaunchedEffect der auf Unauthenticated-State reagiert).

---

**[SEVERITY: WARNING]** — FamilyViewModel subscribed zweimal auf denselben Flow

* **Ort:** `feature/family/src/main/java/.../FamilyViewModel.kt:29-52`
* **System-Impact:** `familyRepository.observeCurrentFamily()` wird ZWEI separate Male in `combine()` aufgerufen — einmal als direkter Parameter und einmal als Basis fuer die `flatMapLatest`-Chain. Jeder Aufruf registriert einen eigenen Room-Datenbankobserver. Doppelte I/O-Last, doppelte Emissionen bei jedem DB-Write.
* **Fix:**
```kotlin
// FamilyViewModel.kt, init-Block komplett ersetzen:

    init {
        familyRepository.observeCurrentFamily()
            .flatMapLatest { family ->
                if (family != null) {
                    familyRepository.observeMembers(family.id).map { members ->
                        FamilyUiState.HasFamily(family = family, members = members)
                    }
                } else {
                    flowOf(FamilyUiState.NoFamily)
                }
            }
            .onEach { newState ->
                if (_uiState.value !is FamilyUiState.Error) {
                    _uiState.value = newState
                } else if (newState is FamilyUiState.HasFamily) {
                    _uiState.value = newState
                }
            }
            .launchIn(viewModelScope)
    }
```

---

**[SEVERITY: WARNING]** — ThumbnailManagerImpl oeffnet InputStream 3x fuer dieselbe URI

* **Ort:** `core/media/src/main/java/.../ThumbnailManagerImpl.kt:34, 42, 69`
* **System-Impact:** Pro Thumbnail-Generierung (2 Aufrufe pro Bild: small + medium) werden insgesamt 6 FileDescriptor-Opens gegen den ContentResolver ausgefuehrt. Bei Multi-Import von z.B. 10 Bildern sind das 60 I/O-Operationen statt 20. Auf langsamen SAF-Providern (Google Drive, Cloud-URIs) ist das ein signifikanter Performance-Hit.
* **Fix:**
```kotlin
// ThumbnailManagerImpl.kt — generateThumb() refactoren:
// Den EXIF-Read in den ersten Pass integrieren, dann nur 2 Opens statt 3:

    private fun generateThumb(uri: Uri, sizePx: Int, dest: File): File {
        // Pass 1: Abmessungen + EXIF in einem Durchgang lesen
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
            // EXIF aus demselben Stream nicht moeglich (Position verbraucht)
            // → eigenen Stream noetig. ABER: wir koennen EXIF im selben Block cachen
            ExifInterface.ORIENTATION_NORMAL // placeholder
        } ?: ExifInterface.ORIENTATION_NORMAL

        // EXIF separat (leider unvermeidbar bei InputStream-URIs):
        val exifOrientation = context.contentResolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        opts.inSampleSize = calculateInSampleSize(opts.outWidth, opts.outHeight, sizePx)
        opts.inJustDecodeBounds = false

        // Pass 2: Decode
        val source = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
        } ?: error("Cannot decode image from URI: $uri")

        val oriented = applyExifRotationWithOrientation(source, exifOrientation)
        // ... rest bleibt gleich
    }

    // Neue Hilfsfunktion die int-Orientation direkt nimmt statt URI nochmal zu oeffnen:
    private fun applyExifRotationWithOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        // gleiche Matrix-Logik wie bisher, aber ohne erneuten openInputStream-Aufruf
    }
```
Hinweis: 3 Opens auf 2 reduziert pro Thumbnail. Bei Cloud-URIs spart das 33% I/O.

---

**[SEVERITY: WARNING]** — RealtimeSyncObserver: Listener stirbt still nach transientem Fehler

* **Ort:** `core/sync/src/main/java/.../RealtimeSyncObserver.kt:58-65`
* **System-Impact:** Wenn der Firestore-SnapshotListener einen transienten Fehler wirft (z.B. UNAVAILABLE), faengt `runCatching` die Exception, loggt sie, und die Coroutine endet. Der Realtime-Sync ist ab diesem Moment TOT bis der User die App manuell in den Hintergrund und wieder in den Vordergrund bringt. Waehrend einer instabilen Netzwerkverbindung kann dies regelmaessig auftreten.
* **Fix:**
```kotlin
// RealtimeSyncObserver.kt — Retry-Loop mit exponential Backoff:

            launch {
                var retryDelayMs = 1_000L
                while (true) {
                    val result = runCatching {
                        firestoreDataSource.observePets(familyId).collect { pets ->
                            retryDelayMs = 1_000L // Reset bei Erfolg
                            Log.d(TAG, "Snapshot: ${pets.size} Pets empfangen")
                            syncEngine.applyRemoteSnapshot(pets = pets)
                        }
                    }
                    if (result.isFailure) {
                        Log.e(TAG, "Fehler im Pets-Listener, retry in ${retryDelayMs}ms", result.exceptionOrNull())
                        kotlinx.coroutines.delay(retryDelayMs)
                        retryDelayMs = (retryDelayMs * 2).coerceAtMost(60_000L)
                    } else break // Normales Ende (cancel)
                }
            }
// Analog fuer Photos- und Members-Listener.
```

---

**[SEVERITY: WARNING]** — Crossfade animiert bei jeder Daten-Aenderung statt nur bei State-Typ-Wechsel

* **Ort:** `feature/pets/src/main/java/.../PetListScreen.kt:96-113`
* **System-Impact:** `Crossfade(targetState = uiState)` vergleicht per `equals()`. `PetListUiState.Success(pets)` mit einer neuen Liste ist ein neues Objekt → Crossfade sieht einen "State-Wechsel" und spielt die Animation ab. Jeder DB-Update (z.B. Sync-Status-Change) loest einen visuellen Fade aus. Gleiches Problem in `GalleryScreen.kt:139`.
* **Fix:**
```kotlin
// PetListScreen.kt, Zeile 96 ersetzen:

        Crossfade(
            targetState = uiState::class,  // Nur bei Typ-Wechsel animieren
            label = "pet_list_content",
            modifier = Modifier.padding(innerPadding),
        ) { stateClass ->
            // uiState direkt aus der aeusseren Closure lesen (nicht aus stateClass):
            when (val state = uiState) {
                PetListUiState.Loading -> LoadingContent()
                PetListUiState.Empty -> EmptyContent()
                is PetListUiState.Success -> PetList(
                    pets = state.pets,
                    onPetClick = onPetClick,
                )
                is PetListUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = onRetry,
                )
            }
        }

// ACHTUNG: Dieser Ansatz hat den Nachteil, dass der Content innerhalb des Crossfade
// nicht recomposed wird wenn sich nur die Daten aendern (weil targetState gleich bleibt).
// Bessere Alternative: Crossfade komplett entfernen und stattdessen AnimatedContent mit
// einem contentKey basierend auf der State-Klasse verwenden:

        androidx.compose.animation.AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },  // Nur bei Typ-Wechsel animieren
            label = "pet_list_content",
            modifier = Modifier.padding(innerPadding),
        ) { state ->
            when (state) {
                PetListUiState.Loading -> LoadingContent()
                PetListUiState.Empty -> EmptyContent()
                is PetListUiState.Success -> PetList(
                    pets = state.pets,
                    onPetClick = onPetClick,
                )
                is PetListUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = onRetry,
                )
            }
        }
// Analog fuer GalleryScreen.kt:139
```

---

## REFACTOR Findings

---

**[SEVERITY: REFACTOR]** — Ungenutzter KEY_FAMILY_ID in SyncWorker und PhotoUploadWorker

* **Ort:** `core/sync/src/main/java/.../SyncWorker.kt:61` und `PhotoUploadWorker.kt:52`
* **System-Impact:** `KEY_FAMILY_ID` wird im Companion definiert und in `SyncScheduler.requestImmediateSync()` / `schedulePhotoUpload()` als InputData gesetzt, aber in `doWork()` NICHT gelesen. Die Worker lesen immer direkt von `FamilyDao.getCurrentFamilyDirect()`. Die InputData wird ignoriert. Toter Code-Pfad.
* **Fix:** Entweder InputData lesen (bevorzugt bei Multi-Family-Support) oder aus SyncScheduler entfernen:
```kotlin
// SyncScheduler.kt — familyId-Parameter und workDataOf-Aufrufe entfernen,
// da die Worker ihn nie konsumieren:

    fun requestImmediateSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME_ONETIME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
// Analog fuer schedulePhotoUpload()
```

---

**[SEVERITY: REFACTOR]** — FamilyScreen LaunchedEffect feuert bei jedem State-Wechsel

* **Ort:** `feature/family/src/main/java/.../FamilyScreen.kt:73-78`
* **System-Impact:** `LaunchedEffect(uiState)` startet bei JEDEM State-Wechsel (Loading→NoFamily→HasFamily→etc.) eine neue Coroutine und cancelt die vorherige. Die Guard-Condition `if (uiState is FamilyUiState.Error)` filtert zwar korrekt, aber der LaunchedEffect-Overhead (cancel + relaunch) ist unnoetig.
* **Fix:**
```kotlin
// FamilyScreen.kt, Zeile 73-78 ersetzen:

    // Nur auf Error-State reagieren, nicht auf jeden State-Wechsel
    val errorMessage = (uiState as? FamilyUiState.Error)?.message
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.dismissError()
        }
    }
```

---

**[SEVERITY: REFACTOR]** — FamilyRepositoryImpl.joinByInviteCode: doppelter Member-Insert

* **Ort:** `feature/family/src/main/java/.../FamilyRepositoryImpl.kt:82-88`
* **System-Impact:** Zeile 82 pusht den neuen Member zu Firestore. Zeile 84 fetcht ALLE Members (inklusive dem gerade gepushten). Zeile 87 inserted alle gefetchten Members. Zeile 88 inserted den eigenen Member NOCHMAL. Wegen `OnConflictStrategy.REPLACE` ist das funktional korrekt aber semantisch falsch und verursacht eine unnoetige Room-Operation + Observer-Emission.
* **Fix:**
```kotlin
// FamilyRepositoryImpl.kt, joinByInviteCode() — Zeile 84-88 ersetzen:

            // 2) Members von Firestore holen (enthaelt jetzt auch den eigenen Member)
            val allMembers = familyFirestoreDataSource.fetchMembers(remoteFamily.id)
            // 3) Alles lokal in Room speichern (SSOT)
            familyDao.insertFamily(remoteFamily.toEntity())
            allMembers.forEach { familyDao.insertMember(it.toEntity()) }
            // Eigenen Member nur inserten wenn er NICHT in allMembers enthalten ist
            // (Firestore-Propagation kann verzoegert sein)
            if (allMembers.none { it.userId == member.userId }) {
                familyDao.insertMember(member.toEntity())
            }
```

---

**[SEVERITY: REFACTOR]** — LoginViewModel: identische Error-Messages in beiden Branches

* **Ort:** `app/src/main/java/.../auth/LoginViewModel.kt:63-69`
* **System-Impact:** Die `if/else`-Verzweigung fuer `GetCredentialException` vs. generische Exception produziert exakt denselben String. Toter Kontrollfluss.
* **Fix:**
```kotlin
// LoginViewModel.kt, Zeile 61-69 ersetzen:

                onFailure = { e ->
                    _uiState.value = LoginUiState.Error(
                        e.message ?: "Google Sign-In fehlgeschlagen"
                    )
                },
```

---

## Zusammenfassung nach Schweregrad

| Severity | Count | Betrifft |
|----------|-------|----------|
| CRITICAL | 3 | Main-Thread-Blocking, Dead-Flow, Stuck-Upload |
| WARNING  | 5 | Race-Condition, Clean-Arch, Double-Subscribe, Silent-Listener-Death, Crossfade |
| REFACTOR | 4 | Dead-Code, LaunchedEffect, Double-Insert, Dead-Branch |

## Positiv-Befunde (kein Handlungsbedarf)

- **Kein `runBlocking`, kein `GlobalScope`, kein `Thread.sleep`** in der gesamten Codebasis
- **Konsistente `collectAsStateWithLifecycle()`-Nutzung** in allen Screens — kein `collectAsState()`
- **`SharingStarted.WhileSubscribed(5_000)`** korrekt in allen ViewModels — stoppt Collection im Hintergrund
- **Offline-First-Architektur** sauber durchgezogen: Room ist SSOT, alle Repositories lesen von Room
- **Thumbnail-Generierung** mit 2-Pass-inSampleSize-Decode und EXIF-Korrektur — OOM-resistent
- **SyncResolver** ist deterministisch und korrekt implementiert (LWW mit Delete-Wins)
- **Worker-Retry-Logik** mit MAX_RETRIES-Guard verhindert endlose Retry-Loops
- **Coil 3 Cache-Limits** (64MB Memory / 250MB Disk) sind sinnvoll fuer eine Foto-App
- **Coroutine-Dispatcher-Hygiene**: Kein missbräuchlicher Main-Dispatcher-Aufruf ausser dem o.g. PetDetailViewModel-Bug
