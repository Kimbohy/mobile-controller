# Mapping Control Change par défaut / Default Control Change mapping

Canal MIDI par défaut : **1** (réglable de 1 à 16 dans les Réglages).
Valeurs des switches : **0** (désactivé / lent) et **127** (activé / rapide).
Les CC 20 à 31 sont libres dans la norme MIDI.

| Contrôle / Control                 | Type   | CC              |
| ---------------------------------- | ------ | --------------- |
| Tirette / Drawbar 16'              | Fader  | 20              |
| Tirette / Drawbar 5⅓'              | Fader  | 21              |
| Tirette / Drawbar 8'               | Fader  | 22              |
| Tirette / Drawbar 4'               | Fader  | 23              |
| Tirette / Drawbar 2⅔'              | Fader  | 24              |
| Tirette / Drawbar 2'               | Fader  | 25              |
| Tirette / Drawbar 1⅗'              | Fader  | 26              |
| Tirette / Drawbar 1⅓'              | Fader  | 27              |
| Tirette / Drawbar 1'               | Fader  | 28              |
| Leslie lent / rapide (slow / fast) | Switch | 29              |
| Vibrato / Chorus                   | Switch | 30              |
| Percussion                         | Switch | 31              |
| Volume                             | Fader  | 11 (expression) |

Source de vérité : [`layouts/organ-default.json`](../layouts/organ-default.json).
