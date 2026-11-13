# Contribuer / Contributing

Ce projet est pensé d'abord pour un seul utilisateur, son créateur. Les contributions sont les bienvenues
si elles respectent la **règle de décision** : toute fonction qui complique l'usage en live ou le setup
sort du projet ou attend une version ultérieure.

## Hors périmètre (refusé poliment)

Produire du son ou jouer des notes, séquenceur / enregistrement, iOS, compte / cloud / réseau dans le MVP,
MIDI 2.0 et MPE.

## Avant d'ouvrir une pull request

- `./gradlew lintDebug testDebugUnitTest` doit passer.
- Identifiants du code en anglais ; documentation en français et en anglais.
- Messages de commit : Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`, `chore:`, `ci:`).
- Jamais de clé de signature ni de secret dans le dépôt.

## Signaler un bug

Utilisez le modèle d'issue : modèle de téléphone, version d'Android, système de l'ordinateur, VST utilisé.
