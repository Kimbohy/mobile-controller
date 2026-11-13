# Mapping Control Change / MIDI CC mapping

Canal MIDI par défaut : **1** (réglable de 1 à 16 dans les Réglages).
Profil par défaut : **Hammond XK-3c / IK B-3X**. Les autres profils se choisissent dans
Réglages → Mapping MIDI, où chaque numéro de CC est aussi modifiable.

_Default MIDI channel: 1 (1–16 in Settings). Default profile: Hammond XK-3c / IK B-3X.
Other profiles and per-control CC numbers are in Settings → MIDI mapping._

## Profils intégrés / Built-in profiles

| Contrôle / Control                     | Hammond XK-3c / IK B-3X (défaut) | NI B4 / EVB3 (NI) | setBfree        | Mobile Controller 1.0 |
| -------------------------------------- | -------------------------------- | ----------------- | --------------- | --------------------- |
| Tirette / Drawbar 16'                  | 12                               | 12                | 70              | 20                    |
| Tirette / Drawbar 5⅓'                  | 13                               | 13                | 71              | 21                    |
| Tirette / Drawbar 8'                   | 14                               | 14                | 72              | 22                    |
| Tirette / Drawbar 4'                   | 15                               | 15                | 73              | 23                    |
| Tirette / Drawbar 2⅔'                  | 16                               | 16                | 74              | 24                    |
| Tirette / Drawbar 2'                   | 17                               | 17                | 75              | 25                    |
| Tirette / Drawbar 1⅗'                  | 18                               | 18                | 76              | 26                    |
| Tirette / Drawbar 1⅓'                  | 19                               | 19                | 77              | 27                    |
| Tirette / Drawbar 1'                   | 20                               | 20                | 78              | 28                    |
| Leslie lent / rapide (slow / fast)     | 1 : 0 / 127                      | 1 : 0 / 127       | 91 : 64 / 127 ² | 29 : 0 / 127          |
| Vibrato / Chorus                       | 40 ¹                             | 31                | 92              | 30                    |
| Percussion                             | 36                               | 66                | 80 : 0 / 32 ²   | 31                    |
| Volume (expression)                    | 11                               | 11                | 11              | 11                    |
| Tirettes inversées / Inverted drawbars | non                              | non               | **oui**         | non                   |

Valeurs d'un switch : 0 (désactivé ou lent) et 127 (activé ou rapide), sauf mention contraire.
Les profils sont décrits dans [`app/src/main/res/raw/mapping_profiles.json`](../app/src/main/res/raw/mapping_profiles.json).

¹ Source unique : liste de CC du B-3X établie par un utilisateur (forum IK Multimedia). À confirmer avec ton VST.
² Valeurs choisies d'après la documentation de setBfree (voir plus bas) ; à confirmer à l'écoute.

## Pourquoi ces numéros

- **Hammond XK-3c / IK B-3X** : tirettes du manuel haut CC 12–20, du manuel bas CC 21–29, pédale CC 33 et 35 ;
  percussion on/off CC 36 ; vitesse du Leslie CC 1 (molette de modulation) ; expression CC 11, volume général CC 7.
- **Native Instruments B4 / Logic EVB3 (mode NI)** : même plan pour les tirettes ; vibrato manuel haut CC 31,
  percussion on/off sur le sostenuto (CC 66), vitesse du Leslie sur la molette (CC 1).
- **setBfree** : tirettes CC 70–78 pour chaque manuel (le canal sélectionne le manuel). Les valeurs des tirettes sont
  **inversées** : 0 = le plus fort, 127 = coupé, comme une vraie tirette. Leslie : CC 91 règle la vitesse
  (0 arrêt, 1 lent, 2 rapide) ; routage du vibrato CC 92 (< 32 off, ≥ 96 les deux manuels) ;
  percussion CC 80 (< 16 off, < 63 normale, < 112 douce, ≥ 112 off : **127 l'éteindrait**).

## Non pris en charge pour l'instant

Les orgues Hammond Suzuki récents (XB, XK-2…) peuvent aussi coder **toutes les tirettes d'un manuel dans un seul CC**
(CC 80 haut, 81 bas, 82 pédale, avec la valeur qui désigne tirette et position). Ce n'est pas un mapping un contrôle = un CC.

## Sources

- Table des contrôleurs MIDI d'EVB3 (modes RK, HS, NI), documentation Apple MainStage.
- Hammond XK-3c : tableau d'implémentation MIDI, discussion sur The Organ Forum.
- IK Multimedia Hammond B-3X : liste de CC (forum IK) et fiche du contrôleur Fosse FC-3 « B3-X Edition ».
- setBfree : page de manuel (`man setBfree`) et fichier `cfg/default.cfg` du dépôt `pantherb/setBfree`.
