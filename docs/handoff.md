# Handoff -- Sprint 5.2 (Familie-Verwaltung) ABGESCHLOSSEN

## Status: ABGESCHLOSSEN

---

## Implementierte Dateien

### :core:model (neu)
| Datei | Beschreibung |
|---|---|
| `Family.kt` | Domain-Entity: id, name, createdBy, inviteCode, createdAt, updatedAt |
| `FamilyMember.kt` | Domain-Entity: id, familyId, userId, displayName, email, role, joinedAt |
| `FamilyRepository.kt` | Interface: currentFamilyId (Flow), observeCurrentFamily, observeMembers, createFamily, joinByInviteCode |

### :core:database (geändert/neu)
| Datei | Beschreibung |
|---|---|
| `entity/FamilyEntity.kt` | Room-Entity + toDomain/toEntity Mapper |
| `entity/FamilyMemberEntity.kt` | Room-Entity + FK zu family + unique Index auf userId |
| `dao/FamilyDao.kt` | observeCurrentFamily (Flow), getCurrentFamilyDirect (suspend), insertFamily, observeMembers, insertMember, getMemberByUserId |
| `migration/Migrations.kt` | +MIGRATION_4_5: erstellt `family` + `family_member` Tabellen |
| `TierappDatabase.kt` | version 4 → 5; +FamilyEntity, FamilyMemberEntity, FamilyDao |
| `di/DatabaseModule.kt` | +MIGRATION_4_5, +provideFamilyDao |

### :core:network (neu)
| Datei | Beschreibung |
|---|---|
| `firestore/FamilyFirestoreDataSource.kt` | Interface + Impl: pushFamily, getFamilyByInviteCode (whereEqualTo), addMember |
| `di/FamilyFirestoreModule.kt` | @Binds FamilyFirestoreDataSourceImpl |

### :core:sync (refactored -- Fallback beseitigt)
| Datei | Änderung |
|---|---|
| `SyncWorker.kt` | FirebaseAuth bleibt für Auth-Check; `familyId` jetzt aus `FamilyDao.getCurrentFamilyDirect()` -- kein uid-Fallback mehr |
| `PhotoUploadWorker.kt` | Identische Änderung wie SyncWorker |
| `RealtimeSyncObserver.kt` | +`FamilyDao` im Konstruktor; `familyId` aus Room statt `authRepository.currentUser()?.uid` |

### :feature:family (neu)
| Datei | Beschreibung |
|---|---|
| `FamilyUiState.kt` | sealed interface: Loading, NoFamily, HasFamily(family, members, isCopied), Error |
| `FamilyViewModel.kt` | createFamily, joinByInviteCode, dismissError; State via combine(observeCurrentFamily, observeMembers) |
| `FamilyScreen.kt` | NoFamilyContent (Erstellen + Beitreten per Code), FamilyContent (Mitgliederliste + InviteCodeCard) |
| `FamilyRepositoryImpl.kt` | internal; koordiniert FamilyDao (Room SSOT) + FamilyFirestoreDataSource |
| `di/FamilyModule.kt` | @Binds FamilyRepository → FamilyRepositoryImpl (@Singleton) |
| `build.gradle.kts` | +core:database, +core:network, +material-icons-extended, +testImplementation |

### docs/
| Datei | Beschreibung |
|---|---|
| `firestore.rules` | Security Rules: families/{familyId} nur für Mitglieder; Self-join via inviteCode erlaubt |

---

## Tests (Sprint 5.2 gesamt)

| Testklasse | Anzahl | Neue Tests |
|---|---|---|
| `RealtimeSyncObserverTest` | 6 | +1 (`onStart ohne Familie startet keinen Listener`) |
| `FamilyViewModelTest` | 7 | neu: Loading, NoFamily, HasFamily, createFamily success/error, joinByInviteCode success/error |
| `FakeFamilyDao` | — | Neue Fake-Implementierung für Sync-Tests |
| `FakeFamilyRepository` | — | Neue Fake-Implementierung für ViewModel-Tests |

**Gesamt-Teststand:**
| Testklasse | Anzahl |
|---|---|
| SyncResolverTest | 12 |
| SyncEngineTest | 9 |
| PhotoUploadEngineTest | 7 |
| RealtimeSyncObserverTest | 6 |
| FamilyViewModelTest | 7 |
| **Gesamt** | **41** |

---

## Beseitigte Fallbacks

| Klasse | Vorher | Nachher |
|---|---|---|
| `SyncWorker` | `inputData.getString(KEY_FAMILY_ID) ?: uid` | `familyDao.getCurrentFamilyDirect()?.id` (null → skip) |
| `PhotoUploadWorker` | `inputData.getString(KEY_FAMILY_ID) ?: uid` | `familyDao.getCurrentFamilyDirect()?.id` (null → skip) |
| `RealtimeSyncObserver` | `authRepository.currentUser()?.uid` | `familyDao.getCurrentFamilyDirect()?.id` (null → skip) |

---

## Architektur-Entscheidungen Sprint 5.2

- **`FamilyRepositoryImpl` in `:feature:family`**: Einziges Modul mit Zugriff auf `:core:database` + `:core:network` ohne Circular Dep.
- **`:core:sync` injiziert `FamilyDao` direkt**: Kein Dep auf `:feature:family` nötig; `getCurrentFamilyDirect()` ist atomar (Room-Lese-Transaktion).
- **Race Condition eliminiert**: Worker lesen `familyId` beim Arbeitsbeginn aus Room (kein veralteter InputData-Wert).
- **Invite Code**: 8-stellig (chars: A-Z ohne I/O + 2-9); ausreichend Entropie für Familiengruppen (~10^12 Kombinationen).
- **Firestore Security Rules**: `members/{userId}` erlaubt Self-Create (Join via Code) + Owner-controlled Delete. `whereEqualTo("inviteCode")` erfordert keinen extra Index (single-field).

---

## Bekannte offene Punkte

| Issue | Priorität | Sprint |
|---|---|---|
| `:feature:pets` Tests (`insertedPets` Unresolved Reference) | Niedrig | Vorbestehend |
| `:feature:gallery` Test `importPhotos` (Uri.EMPTY) | Niedrig | Vorbestehend |
| `observePhotos` collectionGroup ohne Firestore-Index | Mittel | Sprint 5.5 |
| `ReminderRefreshWorker` (Health-Entities fehlen in DB) | Mittel | Sprint 3.3-Nacharbeit |
| Navigation: `FamilyScreen` noch nicht in `MainActivity`/NavHost eingehängt | Hoch | Sprint 5.5 oder App-Integration |
| `SyncScheduler.requestImmediateSync()` übergibt noch `KEY_FAMILY_ID` via InputData (wird nicht mehr ausgewertet) | Niedrig | Kann bereinigt werden |

---

## Nächste Sprints

- **Sprint 5.5 (Integration)**: `FamilyScreen` in NavHost; `SyncScheduler` von `KEY_FAMILY_ID` InputData befreien; Firestore-Index für `inviteCode`-Query in Firebase Console anlegen.
- **Sprint 5.6 (Health Nacharbeit)**: Vaccination, MedicalRecord, Medication, Reminder Entities + DAOs + DB Migration (v5→v6).
- **Sprint 6.x (Polish)**: R8/ProGuard, Crashlytics, Baseline Profiles.
