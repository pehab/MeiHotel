# MeiHotel 🏨

Ein freundliches Hotelspiel für Android: selbst durchs Hotel laufen, Gäste begrüßen, Zimmer reinigen und mit erspielten Münzen ein kleines Traumhotel aufbauen.

**Offline. Ohne Werbung, In-App-Käufe, sexualisierte Inhalte, Konten oder Tracking.** Eigene Grafik und Spielwelt; keine Grafiken, Namen oder Inhalte aus My Perfect Hotel übernommen.

## Testversion herunterladen

👉 **[MeiHotel.apk herunterladen](https://github.com/pehab/MeiHotel/releases/download/preview/MeiHotel.apk)**

Der Download wird nach dem ersten erfolgreichen Build verfügbar. Unter [Releases](https://github.com/pehab/MeiHotel/releases/tag/preview) liegt die aktuelle Testversion als normale APK-Datei – auch ohne Download eines Actions-ZIP. Android 8.0 oder neuer. Installation aus dieser Quelle muss auf dem Gerät erlaubt sein; bei Family Link gelten zusätzlich die Freigaben des Elternkontos.

## Neu in 0.2.0

- Detailliertere Zimmer mit Parkett, Vorhängen, Bildern, Teppichen und warmem Licht; Lobby-Lounge, Gartenblumen und lebendigere Figuren mit Gepäck.
- Die Reinigungskraft kehrt nach dem letzten Auftrag zum Wagen in der Lobby zurück und übernimmt neue Aufgaben sofort.
- Manager-Zimmerservice nach Einstellung beider Helfer: Handtücher an der Wäscheecke abholen, Frühstück am Buffet holen und Wünsche im belegten Zimmer erfüllen. Lieferungen bringen extra Münzen, Helfer übernehmen sie nicht. Unerfüllte Wünsche verfallen ohne Strafe. Zum Wechseln der Ladung die andere Abholstation antippen.
- Frühstücksbuffet in drei Stufen: +8 Münzen pro Gast und Stufe, zusätzlich zum freiwilligen Zimmerservice.
- Vier Hotelziele mit einmaligen automatischen Münzbelohnungen und eigener Fortschrittsübersicht.
- Private feste Preview-Signatur über ein GitHub Actions Secret und aufsteigende Versionsnummer; künftige Preview-APKs lassen sich mit erhaltenem Spielstand aktualisieren.

## Spielumfang

- Ein Hotel mit sechs Zimmern, zwei davon anfangs geöffnet.
- Gäste kommen sichtbar an, warten am Empfang, beziehen ihre Zimmer und reisen wieder ab.
- Geld gibt es beim Einchecken. Nach einem Besuch muss das Zimmer gereinigt werden.
- Vier zusätzliche Zimmer können an den markierten Baukreisen freigeschaltet werden.
- Empfangshilfe und Reinigungskraft übernehmen Aufgaben selbstständig.
- Drei Stufen für Laufgeschwindigkeit und Zimmerausstattung.
- Touch-Joystick durch Ziehen oder automatisches Laufen durch Antippen eines Ziels.
- Einführung, Aufgabenhinweise, Pause, Info und Zurücksetzen mit Bestätigung.
- Lokaler Spielstand: automatisch alle acht aktiven Sekunden und beim Verlassen. Inaktive Zeit läuft nicht weiter; keine Offline-Strafe oder Login-Pflicht.

Dies ist eine **erste spielbare Testversion** mit einem Hotel. Weitere Hotels, Pool und Sound sind noch nicht enthalten. Die Darstellung nutzt eine eigene, flache Draufsicht mit direkt im Spiel gezeichneten Figuren und Möbeln.

## Steuerung

1. Gerät am besten ins Querformat drehen und auf „Los geht’s!“ tippen.
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

Für Preview-APKs ab 0.2.0 einmalig das Repository-Actions-Secret `MEIHOTEL_SIGNING_JSON` hinterlegen (Settings → Secrets and variables → Actions). Inhalt: JSON mit `keystore` (Base64-kodiertes PKCS12), `password` und `alias`. Den privaten Schlüssel sicher aufbewahren, niemals committen oder als Release/Actions-Artefakt hochladen. Der Workflow bricht außerhalb von Pull Requests ohne Secret absichtlich ab, damit kein Update mit zufälliger Signatur veröffentlicht wird.

Die CI nutzt dann bei jedem Preview-Build diesen festen privaten Schlüssel. APK herunterladen und öffnen → „Aktualisieren“; der Spielstand bleibt erhalten. Die App-ID bleibt `de.meiapps.meihotel`. Bei jeder Version `versionCode` erhöhen. Lokale Debug-Builds verwenden nur mit `MEIHOTEL_KEYSTORE_FILE`, `MEIHOTEL_KEYSTORE_PASSWORD` und `MEIHOTEL_KEY_ALIAS` dieselbe Identität; andernfalls nutzt Android Studio seinen lokalen Debugschlüssel. Für Produktionsversionen einen eigenen privaten Schlüssel verwenden.

**Einmaliger Übergang von 0.1.0:** Die alte CI hat pro Runner einen neuen Schlüssel erzeugt und nicht gesichert. Ohne den ursprünglichen privaten Schlüssel kann Android eine solche Installation nicht auf den festen Preview-Schlüssel aktualisieren. In diesem Fall ist einmalig eine Neuinstallation nötig; dabei geht der alte lokale Spielstand verloren. Ab 0.2.0 ist das behoben. Der Speicherformat-Lader akzeptiert weiterhin alte 0.1-Spielstände; neue Felder erhalten Standardwerte.

## Aufbau und Qualität

- `HotelGame.java`: Android-unabhängige Simulation und Wirtschaft.
- `Navigation.java`: begehbare Bereiche, Türen, Bettkollisionen und Wegeplanung.
- `HotelView.java`: Originalgrafik, Touch-Steuerung und Menüs.
- `GameStore.java`: versionierter, atomarer Spielstand in privaten App-Einstellungen.
- `MainActivity.java`: Android-Lebenszyklus und sichere Bildschirmränder.
- `HotelGameTest.java`: Gästezyklus, Baukosten, Helfer, Kollisionen, alle Türen, Upgradegrenzen und Pausenzeiten.

CI führt Unit-Tests, strenges Android-Lint sowie Debug- und Release-Build aus. Erst nach erfolgreichem Build erscheint die APK im Preview-Release. AndroidX Activity übernimmt die Android-Zurück-Geste; keine Werbe- oder Analyse-SDKs und keine Netzwerkberechtigung.

Siehe [Datenschutz](PRIVACY.md) und [Testcheckliste](docs/TESTING.md).
