# Mobile Controller

Transforme un téléphone Android en surface de contrôle tactile (tirettes d'orgue, switches, volume)
et envoie du MIDI à un DAW ou à un VST **par simple câble USB, sans rien installer sur le PC**.

_Turns an Android phone into a touch control surface (organ drawbars, switches, volume) that sends
MIDI to a DAW or VST over a plain USB cable — nothing to install on the computer._

<!-- TODO : ajouter une capture d'écran : docs/screenshot.png -->
<!-- ![Mobile Controller](docs/screenshot.png) -->

## Mise en route (3 étapes) / Quick start

1. Branchez le téléphone au PC avec un **câble USB de données**.
2. Sur le téléphone, choisissez **« MIDI »** dans la notification « Utiliser l'USB pour »
   (ou, une fois pour toutes : Options pour les développeurs → Configuration USB par défaut → MIDI).
3. Ouvrez Mobile Controller : l'indicateur passe au **vert**.

Puis, une seule fois dans votre VST ou DAW, utilisez le **MIDI Learn** : bougez une tirette, le paramètre est assigné.

## Mapping CC par défaut

Le mapping par défaut suit les orgues **Hammond XK-3c / IK B-3X** (tirettes CC 12–20, Leslie CC 1,
percussion CC 36, vibrato CC 40, volume CC 11). Des profils sont intégrés pour **Native Instruments B4 / EVB3**
et **setBfree** (tirettes inversées incluses) ; chaque CC reste modifiable dans Réglages → Mapping MIDI.
Détails et sources : [docs/mapping-cc.md](docs/mapping-cc.md).

## Compatibilité testée

| Téléphone   | Android     | Ordinateur | VST / DAW   | Résultat    |
| ----------- | ----------- | ---------- | ----------- | ----------- |
| Pixel 8 Pro | (à remplir) | Debian 13  | (à remplir) | (à remplir) |

Windows et macOS : pas encore testés. **Un seul logiciel à la fois** doit utiliser le port MIDI du téléphone
(certains DAW prennent le port en exclusif sous Windows).

## Confidentialité

Aucune permission, aucun accès réseau, aucune télémétrie, aucune publicité.

## Développement

```bash
./gradlew testDebugUnitTest          # tests de logique (sans téléphone)
./gradlew connectedDebugAndroidTest  # tests d'interface (téléphone branché)
./gradlew assembleDebug              # APK de debug
```

Voir [CONTRIBUTING.md](CONTRIBUTING.md). Licence : [MIT](LICENSE).

English summary: see the sections above; documentation is bilingual where it matters (README, CC mapping).
