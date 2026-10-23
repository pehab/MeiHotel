# Datenschutz – MeiHotel

Stand: 23. Oktober 2026 · Version 0.4.0

Die Store-/Release-Version von MeiHotel ist ein Offline-Spiel. Diese Version erhebt oder übermittelt keine personenbezogenen Daten. Sie enthält weder Werbung noch Analyse-, Tracking-, Crashreporting- oder sonstige Drittanbieter-SDKs und benötigt keine Netzwerkberechtigung.

Der Spielfortschritt (Münzen, Zimmer, Verbesserungen und Spielerposition) wird ausschließlich im privaten Speicher der App auf dem Gerät gespeichert. Er wird nicht an Server übertragen. Android-Backups sind für die App deaktiviert.

Es gibt keine Benutzerkonten, Chats, Standortabfragen, Kamera- oder Mikrofonzugriffe und keine In-App-Käufe. Die App fragt keine gefährlichen Android-Berechtigungen an.

Der Spielstand kann im Pausenmenü nach Bestätigung zurückgesetzt werden. „App-Daten löschen“ oder das Deinstallieren entfernt ebenfalls die lokal gespeicherten Daten.

Bei Fragen kann über das [Projekt auf GitHub](https://github.com/pehab/MeiHotel/issues) Kontakt zum Entwickler aufgenommen werden. Inhalte, die dort freiwillig veröffentlicht werden, unterliegen den Datenschutzbestimmungen von GitHub.


## GitHub-Testversion: Update-Prüfung

Die direkt über GitHub verteilte Testversion kann beim Start einmal täglich bei GitHub nach neuen Versionen suchen. Diese Prüfung ist standardmäßig aktiviert und kann unter Pause → Einstellungen / Updates ausgeschaltet werden. Eine manuelle Prüfung ist dort ebenfalls möglich. Das Hotelspiel bleibt ohne Internet spielbar.

Bei der Prüfung wird eine öffentliche Versionsdatei von GitHub geladen. Erst nach Zustimmung wird eine neue APK heruntergeladen. Dabei erhalten GitHub und dessen Download-Dienste technisch die IP-Adresse sowie HTTP-Anfragedaten (unter anderem einen festen MeiHotel-Updater-Namen). Es werden keine Spielstände, Konten, Werbe-IDs oder anderen App-Nutzungsdaten übertragen. Für diese Dienste gelten die [Datenschutzinformationen von GitHub](https://docs.github.com/de/site-policy/privacy-policies/github-general-privacy-statement).

Der Download liegt vorübergehend im privaten App-Cache. Vor der Installation werden Dateigröße, SHA-256-Prüfsumme, Paketname, Version und Signatur mit der installierten App abgeglichen. Android verlangt die Installationsbestätigung und gegebenenfalls eine Freigabe für diese Installationsquelle. Die Release-/Store-Version enthält diese GitHub-Update-Funktion und die dafür erforderlichen Netzwerk-/Installationsberechtigungen nicht.
