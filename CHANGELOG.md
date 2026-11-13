# Changelog

Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) ; versions sémantiques.

## [Non publié]

## [1.1.0] — 2026-12-13

### Ajouté

- 8 presets rappelables d'un geste (toucher) et sauvegardables par appui long.
- Bouton Sync : renvoie toutes les valeurs courantes au VST.
- Verrouillage tactile par appui long.
- Numéro de CC modifiable pour chaque contrôle, et interrupteur d'inversion des tirettes.
- Profils de mapping : Hammond XK-3c / IK B-3X, Native Instruments B4 / EVB3, setBfree, Mobile Controller 1.0.

### Modifié

- **Mapping par défaut** : les nouvelles installations utilisent désormais le profil Hammond XK-3c / IK B-3X
  (tirettes CC 12–20, Leslie CC 1, percussion CC 36, vibrato CC 40) au lieu de CC 20–31.
- Les installations existantes **conservent automatiquement l'ancien mapping** (profil « Mobile Controller 1.0 »)
  pour ne pas casser un MIDI Learn déjà fait. Le nouveau défaut se choisit dans Réglages → Mapping MIDI.
- Barre du haut : la phrase d'aide « aucun port MIDI » s'affiche sous la barre.

## [1.0.0] — 2026-12-12

### Ajouté

- Page Orgue : 9 tirettes, switches Leslie / Vibrato / Percussion, volume.
- Envoi de Control Change MIDI par USB, canal réglable (1–16), sans doublons.
- Indicateur de connexion, reconnexion automatique, aide intégrée.
- Écran toujours allumé, paysage, assistant de première mise en route.
- Sauvegarde automatique de l'état et des réglages.
