# MeiHotel 🏨

Ein freundliches Hotelspiel für Android: selbst durchs Hotel laufen, Gäste begrüßen, Zimmer reinigen und mit erspielten Münzen ein kleines Traumhotel aufbauen.

**Offline. Ohne Werbung, In-App-Käufe, sexualisierte Inhalte, Konten oder Tracking.** Eigene Grafik und Spielwelt; keine Grafiken, Namen oder Inhalte aus My Perfect Hotel übernommen.

## Testversion herunterladen

👉 **[MeiHotel.apk herunterladen](https://github.com/pehab/MeiHotel/releases/download/preview/MeiHotel.apk)**

Der Download wird nach dem ersten erfolgreichen Build verfügbar. Unter [Releases](https://github.com/pehab/MeiHotel/releases/tag/preview) liegt die aktuelle Testversion als normale APK-Datei – auch ohne Download eines Actions-ZIP. Android 8.0 oder neuer. Installation aus dieser Quelle muss auf dem Gerät erlaubt sein; bei Family Link gelten zusätzlich die Freigaben des Elternkontos.

## Die erste Version: 0.1.0

- Ein Hotel mit sechs Zimmern, zwei davon anfangs geöffnet.
- Gäste kommen sichtbar an, warten am Empfang, beziehen ihre Zimmer und reisen wieder ab.
- Geld gibt es beim Einchecken. Nach einem Besuch muss das Zimmer gereinigt werden.
- Vier zusätzliche Zimmer können an den markierten Baukreisen freigeschaltet werden.
- Empfangshilfe und Reinigungskraft übernehmen Aufgaben selbstständig.
- Drei Stufen für Laufgeschwindigkeit und Zimmerausstattung.
- Touch-Joystick durch Ziehen oder automatisches Laufen durch Antippen eines Ziels.
- Einführung, Aufgabenhinweise, Pause, Info und Zurücksetzen mit Bestätigung.
- Lokaler Spielstand: automatisch alle acht aktiven Sekunden und beim Verlassen. Inaktive Zeit läuft nicht weiter; keine Offline-Strafe oder Login-Pflicht.

Dies ist eine **erste spielbare Testversion** mit einem Hotel. Weitere Hotels, Pool, Frühstück und Sound sind noch nicht enthalten. Die Darstellung nutzt eine eigene, flache Draufsicht mit direkt im Spiel gezeichneten Figuren und Möbeln.

## Steuerung

1. Auf „Los geht’s!“ tippen.
2. Finger über der Spielfläche ziehen oder ein Ziel antippen.
3. Zum grünen Empfangskreis laufen und kurz stehenbleiben: Ein freies Zimmer und ein wartender Gast reichen zum Einchecken.
4. Nach der Abreise in das betreffende Zimmer zum Besensymbol laufen. Der Kreis zeigt den Reinigungsfortschritt.
5. Zum Baukreis eines geschlossenen Zimmers gehen. Mit genügend Münzen und 1,5 Sekunden Stehenbleiben wird es geöffnet.
6. „Ausbauen“ öffnet den Shop für Helfer, Tempo und Ausstattung. Alle Verbesserungen kosten ausschließlich Spielmünzen.

Die ersten beiden Zimmer und 60 Startmünzen ermöglichen einen Einstieg ohne Sackgasse. Gäste warten geduldig. Es gibt keine Strafen, Tagesbelohnungen oder zeitlich begrenzten Angebote.

## Mit Android Studio bauen

Projekt öffnen, SDK 36 und JDK 17 einrichten, dann:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Windows: `gradlew.bat`. Die Test-APK liegt unter `app/build/outputs/apk/debug/app-debug.apk`. Der Release-Build wird bewusst **nicht** mit einem Produktionsschlüssel signiert; für den Play Store in Android Studio einen eigenen Uploadschlüssel verwenden.

Die CI-APK verwendet die normale Android-Debug-Signatur. Bei einem neuen GitHub-Runner kann sich diese ändern: Android verlangt dann eine Deinstallation vor der Neuinstallation, wobei der Spielstand verloren geht. Für regelmäßige Updates mit erhaltenem Fortschritt lokal in Android Studio mit demselben Schlüssel bauen. Ein privater Produktionsschlüssel gehört niemals ins Repository.

## Aufbau und Qualität

- `HotelGame.java`: Android-unabhängige Simulation und Wirtschaft.
- `Navigation.java`: begehbare Bereiche, Türen, Bettkollisionen und Wegeplanung.
- `HotelView.java`: Originalgrafik, Touch-Steuerung und Menüs.
- `GameStore.java`: versionierter, atomarer Spielstand in privaten App-Einstellungen.
- `MainActivity.java`: Android-Lebenszyklus und sichere Bildschirmränder.
- `HotelGameTest.java`: Gästezyklus, Baukosten, Helfer, Kollisionen, alle Türen, Upgradegrenzen und Pausenzeiten.

CI führt Unit-Tests, strenges Android-Lint sowie Debug- und Release-Build aus. Erst nach erfolgreichem Build erscheint die APK im Preview-Release. Keine zusätzlichen Laufzeitbibliotheken und keine Netzwerkberechtigung.

Siehe [Datenschutz](PRIVACY.md) und [Testcheckliste](docs/TESTING.md).
