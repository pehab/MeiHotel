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
