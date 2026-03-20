# Tierapp -- Naechste Schritte

**Stand:** 2026-03-20
**Letzter Commit:** `746f807` — Code-Review Phase 6 (12 Fixes)

---

## Aktueller Status

Phase 6 (Polish & Release) ist **abgeschlossen**. Alle Sprints und Bugfixes sind implementiert.

### Erledigt
- [x] R8/ProGuard + Crashlytics (Sprint 6.1)
- [x] Accessibility + UX-Polish (Sprint 6.2)
- [x] Edge-Case-Bugfixes (OOM, SyncResult, Batch-Chunking, Early-Exit)
- [x] Asset-Integration (Hintergrundbilder)
- [x] EXIF-Rotation-Fix
- [x] Health-UI vollstaendig
- [x] Settings vollstaendig (ThemeMode, Logout)
- [x] Family-Join-Permission-Fix
- [x] Branding-Footer
- [x] Mitglieder-Realtime-Sync
- [x] ReminderRefreshWorker
- [x] Code-Review Phase 6 Fixes
- [x] App-Icon (Adaptive Icon, Pfotenabdruck in Brand-Farben)
- [x] Splash-Screen (Android 12+ API, Creme-Hintergrund)
- [x] App-Name "Tierapp" finalisiert

---

## Verbleibende Schritte

### 1. Signed Release-AAB erstellen

**Voraussetzungen:**
- Keystore-Datei (`release.keystore`)
- Env-Vars: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

**Build-Befehl:**
```bash
./gradlew bundleRelease
```

**Ausgabe:** `app/build/outputs/bundle/release/app-release.aab`

**Upload:** Google Play Console Internal Testing Track

---

### 2. End-to-End-Verifizierung auf Testgeraet

**Checkliste:**
- [ ] App-Icon korrekt angezeigt (Alle Groessen)
- [ ] Splash-Screen erscheint bei Kaltstart
- [ ] Login mit Google funktioniert
- [ ] Tier erstellen + Sync auf zweitem Geraet sichtbar
- [ ] Foto aufnehmen + in Galerie sichtbar
- [ ] Familie erstellen + beitreten (Code-Share)
- [ ] Impfung/Medikament anlegen + Dashboard zeigt Warnung
- [ ] Offline-Aenderung + Sync bei Wiederverbindung
- [ ] Theme-Wechsel (Light/Dark/System)
- [ ] Logout + Re-Login
- [ ] Crashlytics Test-Crash wird protokolliert

---

## Bekannte technische Schuld (Niedrige Prioritaet)

| Issue | Datei | Beschreibung |
|---|---|---|
| insertedPets-Test fehlschlaegt | `:feature:pets` | Unresolved Reference in Test |
| importPhotos-Test fehlschlaegt | `:feature:gallery` | Uri.EMPTY im Test |
| PetSpecies.toDisplayName() 3x vorhanden | PetEditScreen, PetDetailScreen, PetListScreen | Konsolidierung empfohlen |
| observePhotos collectionGroup ohne Index | FirestorePetDataSource | Firestore-Index ab ~100 Fotos empfohlen |

---

## Nach Release

- Firestore Security Rules produktiv setzen
- Firestore Index fuer `observePhotos` collectionGroup erstellen (Firebase Console)
- Crashlytics Crash-Free Users Dashboard beobachten
- Optional: Facebook/Microsoft Auth reaktivieren (vorlaeufig ausgeklammert)