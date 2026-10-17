# Testcheckliste 0.1.0

## Automatisch

`./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease`

Unit-Tests prüfen den kompletten Gästezyklus und Auszahlung, Reinigungen, alle sechs Zimmerwege in beide Richtungen, Wand- und Bettkollisionen, einmalige Baukosten, Unterbrechung des Bauens, unbeaufsichtigten Betrieb mit Helfern, begrenzte Warteschlange, Upgradegrenzen und das Begrenzen ungültiger/langer Frames.

CI startet außerdem einen Android-11-Emulator. Ein eigener Instrumentation-Runner prüft echte Android-Speicherung, Wiederherstellen belegter/schmutziger Zimmer und anreisender Gäste, beschädigte Spielstände, Activity-Start, Touch auf die Einführung und die laufende Simulation.

## Auf echtem Android-Gerät

Vor einer Store-Freigabe auf Smartphone und Tablet durchführen:

- [ ] Neuinstallation zeigt Einführung, zwei freie Zimmer und 60 Münzen.
- [ ] Ziehen steuert die Figur; Antippen von Empfang, Zimmer und Baukreis findet einen Weg.
- [ ] Alle oberen und unteren Türen können in beide Richtungen passiert werden.
- [ ] Finger loslassen und Mehrfingerberührungen hinterlassen keinen aktiven Joystick.
- [ ] Empfang zahlt genau einmal, belegtes Zimmer wird anschließend schmutzig.
- [ ] Reinigen und Baukreis zeigen Fortschritt und lassen sich durch Weggehen abbrechen.
- [ ] Bei zu wenig Münzen bleibt ein Zimmer geschlossen; alle sechs können freigeschaltet werden.
- [ ] Beide Helfer bedienen mehrere Gästezyklen ohne Eingriff.
- [ ] Alle Upgrades lassen sich bis zur angezeigten Höchststufe kaufen.
- [ ] Pause, Hilfe und Shop halten die Simulation an.
- [ ] App in Hintergrund legen, Bildschirm sperren und zurückkehren: keine nachgeholte Simulationszeit.
- [ ] App nach Verlassen beenden und neu starten: Münzen, Zimmer, Helfer und Ausstattung bleiben erhalten.
- [ ] Neustart bei belegtem und bei schmutzigem Zimmer erhält einen spielbaren Zustand.
- [ ] Bei lokal mit gleichem Schlüssel gebauten Updates bleibt der Spielstand erhalten. Bei CI-APK Signaturwechsel beachten (siehe README).
- [ ] Zurücksetzen abbrechen erhält den Stand; Bestätigung startet ein neues Hotel.
- [ ] Zurück-Geste öffnet Pause; Kameraausschnitt/Systemleisten verdecken keine Schaltflächen.
- [ ] Auf großem Tablet und bei von Android erzwungenem Hochformat bleibt die gesamte Spieloberfläche erreichbar.

Physische Geräteprüfungen sind manuelle Freigabeschritte und werden nicht durch Unit-Tests ersetzt.


## 0.2.0 Regressionen

- CI installiert eine APK mit Versionscode 1 und dem innerhalb des Laufs identischen Testschlüssel, speichert Fortschritt und ersetzt sie per `adb install -r` durch Code 2. Die Instrumentation prüft Münzen, Helfer, Ziele und Buffet nach dem Update. Die ältere Fixture nutzt aktuellen Code; sie prüft den Android-Updatepfad, nicht die verloren gegangene Signatur der ursprünglichen 0.1-APK.
- Instrumentation lädt zusätzlich einen Speicherstand ohne neue optionale Felder (0.1-Kompatibilität).
- Nach dem Putzen und bei vom Spieler vorher fertiggestellter Reinigung kehrt die Reinigungskraft zum Wagen zurück.
- Mit beiden Helfern erscheinen Handtuchwünsche; nach Buffetkauf auch Frühstück. Lieferungen müssen selbst erledigt werden und erhöhen Münzen genau einmal; nicht erfüllte Wünsche blockieren nichts.
- Alle fünf Shopzeilen, Hotelziele, Warenwechsel und Reset auf Handy/Tablet prüfen.


## 0.3.0 Regressionen

- 26 Simulationsfälle: alle früheren Tests plus individuelle Zimmer, Ausbaugrenzen, Anforderungen der Hofbereiche, Personaltraining, Mehrfachlieferung, Hofwege, entfernte Interaktionen, weiterlaufende Helfer, optionale Hofaufträge, Sterne und wiederholbare Aufträge.
- Android prüft zusätzliche Speicherfelder, 0.2- und 0.1-Migration, APK-Update von Fixture-Code 2 auf 3, vier Shop-Tabs, individuellen Zimmer-/Hofkauf und Hin-/Rückweg zum Hof.
- Die Update-Fixture benutzt aktuellen Code mit niedrigerem Versionscode. Alte Speicherformate werden separat durch Entfernen der später hinzugefügten optionalen Felder geprüft.
- CI zeichnet eine Hofansicht für visuelle Kontrolle; nur synthetische Testdaten.
