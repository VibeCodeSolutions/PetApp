# PetApp

Android-App zum Verwalten von Haustier-Daten — Pets, Fotos, Gesundheits-Notizen, Erinnerungen, Familien-Sync. **Portfolio-Demo** — gebaut, um Multi-Module-Setup, Firebase-Stack und WorkManager mit Hilt praktisch durchzuspielen, nicht im Store gelandet.

## Funktionen

- Pet-Daten anlegen und pflegen
- Foto-Galerie mit EXIF-Rotation
- Gesundheits-Tracking pro Tier
- Erinnerungen via WorkManager + Notification (Hilt-Workers)
- Familien-Sharing über Firebase-Auth + Sync
- Google Sign-In via Credential Manager

## Stack

Kotlin · Jetpack Compose · Material 3 · Splash Screen API · Hilt · WorkManager · Firebase (BOM, Auth, Crashlytics) · Credential Manager · Coroutines

## Architektur

Multi-Module mit Custom Convention-Plugins (`tierapp.android.app`, `tierapp.compose`, `tierapp.hilt`):

- **8 core-Module** — `common`, `database`, `media`, `model`, `network`, `notifications`, `sync`, `ui`
- **5 feature-Module** — `pets`, `gallery`, `health`, `family`, `settings`
- **macrobenchmark-Modul** für Performance-Tests

## Build

```
./gradlew assembleDebug
```

Firebase erfordert eine eigene `google-services.json` im `app/`-Ordner. Kein Release-APK, kein Store-Eintrag — wer bauen will, braucht eine eigene Firebase-Konfiguration.

## Hinweis

Die größte App in diesem Repo-Bereich, gebaut als Übungsstrecke für Multi-Module-Architektur, Firebase und das Zusammenspiel von WorkManager mit Hilt. Funktional ist sie weit, aber sie ist kein vermarktetes Produkt — kein Eintrag im Play Store, kein offizielles Release.

## Lizenz

MIT.
