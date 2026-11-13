# Validation MVP v1.0 — fiche de résultats

- Date : 2026-11-13 Version testée (versionName) : 1.0.0 APK : release
- Téléphone : Pixel 8 Pro — Android 17
- PC : Debian 13 — VST/DAW : Waterfall B3

| #   | Test              | Critère                                     | Résultat | Notes            |
| --- | ----------------- | ------------------------------------------- | -------- | ---------------- |
| 1   | Liaison USB       | CC reçu avec bon numéro et bonne valeur     | [x]      |                  |
| 2   | MIDI Learn        | Les 13 contrôles pilotent le bon paramètre  | [x]      | wrong CC mapping |
| 3   | Multitouch        | 5 tirettes / 5 doigts, sans saut ni blocage | [x]      |                  |
| 4   | Débranchement ×10 | 0 plantage, reconnexion < 3 s               | [x]      |                  |
| 5   | Interruptions     | L'app reprend dans le bon état              | [ ]      | not tested       |
| 6   | Latence           | < 10 ms                                     | [x]      |                  |
| 7   | Endurance 2 h     | 0 plantage, aucune valeur bloquée           | [ ]      | not tested       |
| 8   | APK release       | ≤ 8 Mo                                      | [x]      |                  |
| 9   | Démarrage à froid | Écran utilisable < 1 s                      | [ ]      | not tested       |
| 10  | Ressources        | Mémoire stable, pas de redessin inutile     | [x]      |                  |
| 11  | Confidentialité   | Aucune permission                           | [x]      |                  |
