# Bugfix: Family Join — Firestore Permission Denied

## TCRTE-Analyse

### Task
Firestore "Permission denied" beim Beitritt via Invite-Code auf zweitem Geraet.

### Context
- App: Kotlin/Jetpack Compose, MVVM, Offline-First (Room SSOT, Firestore Sync-Backend)
- Flow: User B gibt 8-stelligen Invite-Code ein -> `FamilyRepositoryImpl.joinByInviteCode()`

### [SEVERITY: CRITICAL] — Race-Condition: fetchMembers VOR addMember

* **Ort:** `feature/family/src/main/java/.../FamilyRepositoryImpl.kt`, Zeile 82 (alt)
* **System-Impact:** Deterministische Race-Condition (KEIN Timing-Problem, sondern falsche Reihenfolge). `fetchMembers()` liest `/families/{id}/members` BEVOR der eigene Member-Eintrag existiert. Security Rule `allow read: if isFamilyMember(familyId)` prueft `exists(.../members/{request.auth.uid})` → Dokument fehlt → **Permission Denied**.

### Deduktive Analyse (3 Schichten)

**Schicht 1 — Application Layer (Kotlin):**
```
ALT (fehlerhaft):
  1. getFamilyByInviteCode()      → list (OK, allow list: if isAuthenticated())
  2. insertFamily() lokal         → Room-only (OK)
  3. fetchMembers(remoteFamily.id) → read members ❌ PERMISSION DENIED
  4. insertMember() lokal         → Room-only
  5. addMember() remote           → create (OK, allow create: if auth.uid == userId)
```

Das Problem: Schritt 3 braucht `isFamilyMember`-Berechtigung, die erst nach Schritt 5 existiert.

**Schicht 2 — Firestore Security Rules:**
```javascript
match /members/{userId} {
  allow read: if isFamilyMember(familyId);
  // isFamilyMember prueft: exists(.../members/{request.auth.uid})
  allow create: if isAuthenticated() && request.auth.uid == userId;
}
```
Rules sind korrekt und sicher. Das Problem liegt NICHT in den Rules.

**Schicht 3 — Netzwerk/Consistency:**
Kein Eventual-Consistency-Problem. Firestore `set().await()` garantiert Server-Acknowledgment. Nach `addMember().await()` ist das Dokument committed und `isFamilyMember` wird `true` evaluieren.

### Fix (Kotlin) — angewendet in FamilyRepositoryImpl.kt

```kotlin
// joinByInviteCode — korrigierte Reihenfolge:
// 1) Eigenes Member-Dokument ZUERST in Firestore schreiben,
//    damit isFamilyMember(familyId) in den Security Rules greift.
familyFirestoreDataSource.addMember(remoteFamily.id, member)
// 2) Jetzt darf der Client die Members-Subcollection lesen.
val existingMembers = familyFirestoreDataSource.fetchMembers(remoteFamily.id)
// 3) Alles lokal in Room speichern (SSOT)
familyDao.insertFamily(remoteFamily.toEntity())
existingMembers.forEach { familyDao.insertMember(it.toEntity()) }
familyDao.insertMember(member.toEntity())
```

**Kern-Aenderung:** `addMember()` (Firestore-Write) wird VOR `fetchMembers()` (Firestore-Read) ausgefuehrt. Danach wird alles geblockt in Room geschrieben.

### Fix (Rules) — NICHT noetig

Die bestehenden Rules sind korrekt:
- `allow list` auf `/families` fuer Invite-Code-Query (sicher wg. 8-stellig zufaellig)
- `allow create` auf `/members/{userId}` fuer Self-Registration
- `allow read` auf `/members` nur fuer bestehende Mitglieder

### Sekundaeres Risiko: RealtimeSyncObserver

`RealtimeSyncObserver.onStart()` startet `observeMembers(familyId)` wenn eine Familie in Room existiert. Falls die App zwischen Room-Insert und Firestore-Write in den Hintergrund/Vordergrund wechselt, koennte derselbe Permission-Denied auftreten.

**Mitigiert durch den Fix:** Room-Inserts passieren jetzt NACH dem Firestore-Write. Wenn Room die Familie hat, existiert der Member-Eintrag bereits in Firestore.

### Testing

1. **Manuell:** Geraet A erstellt Familie → Geraet B gibt Code ein → Beitritt muss erfolgreich sein
2. **Unit-Test:** `joinByInviteCode` Mock-Test: Verifiziere dass `addMember()` VOR `fetchMembers()` aufgerufen wird (InOrder-Verification)
3. **Firestore Rules Emulator:** `firebase emulators:exec` mit Rules-Unit-Test: Nicht-Mitglied darf NICHT `/members` lesen; nach eigenem Create schon

### Enhancement

- Optional: Firestore Batch-Write fuer den gesamten Join (addMember + readAfter in einer Transaction), um Atomizitaet zu garantieren. Aktuell nicht noetig, da `.await()` serverseitige Konsistenz sicherstellt.
