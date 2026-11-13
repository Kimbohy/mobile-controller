# Validation v1.1 — fiche de résultats

- Date : \_\_\_\_ Version testée : 1.1.0 APK : debug / release
- Téléphone : Pixel 8 Pro — Android \_**\_ PC : Debian 13 — VST/DAW : \_\_**

| #   | Test                | Critère                                                       | Résultat | Notes |
| --- | ------------------- | ------------------------------------------------------------- | -------- | ----- |
| 1   | Presets             | Sauvegarde, rappel, persistance, 13 messages au rappel        | [x]      |       |
| 2   | Sync                | 13 messages, même sans changement                             | [x]      |       |
| 3   | Verrou              | Tout est ignoré, déverrouillage par appui long                | [x]      |       |
| 4   | CC modifiables      | Le CC changé est bien émis, « Revenir au profil » le rétablit | [x]      |       |
| 5   | Profils + inversion | Numéros et sens corrects pour chaque profil                   | [x]      |       |
| 6   | Migration 1.0 → 1.1 | Ancien mapping, valeurs et canal conservés                    | [x]      |       |
| 7   | VST réel            | Le profil adapté pilote les bons paramètres                   | [x]      |       |
| 8   | Non-régression 1.0  | Tests 1, 3, 4, 5, 6, 8, 9, 10, 11 de la fiche 1.0             | [x]      |       |
| 9   | Poids et démarrage  | APK ≤ 8 Mo, démarrage < 1 s                                   | [x]      |       |
