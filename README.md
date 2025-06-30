# Nimbus

## Qualité du code

Ce projet utilise [pre-commit](https://pre-commit.com/) pour assurer la qualité du code avant chaque commit.

### Installation

1. Installez pre-commit :
   ```
   pip install pre-commit
   ```

2. Installez les hooks Git :
   ```
   pre-commit install
   ```

### Vérifications effectuées

- Formatage du code Java
- Vérification des imports inutilisés
- Détection des problèmes de style de code (Checkstyle)
- Détection du code dupliqué (CPD)
- Vérification des fichiers YAML et JSON
- Détection des conflits de fusion non résolus
- Détection des fichiers volumineux ajoutés par erreur
- Compilation et tests (via Gradle)

### Exécution manuelle

Pour exécuter toutes les vérifications manuellement :

   ```
   ./quality-check.sh
   ```
