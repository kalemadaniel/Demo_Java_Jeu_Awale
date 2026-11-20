# Demo_Java_Jeu_Awale

Jeu de semailles à **4 rangées de 8 cases**, inspiré de l’Omweso. L’application est écrite en **Java 8** avec **Swing** et le thème **Nimbus**. Elle s’ouvre dans **NetBeans 8.0.2** comme un projet **Java Application** standard, reconnaissable à l’icône de la tasse.

<img width="934" height="692" alt="Capture d&#39;écran 2026-11-20 151741" src="https://github.com/user-attachments/assets/5823f6e2-e998-4d40-9b66-fde17a70d911" />

## Modes de jeu

- **Deux joueurs** sur le même ordinateur.
- **Contre l’ordinateur** avec trois niveaux de difficulté.

| Niveau | Stratégie |
| --- | --- |
| Facile | Choisit un coup autorisé au hasard. |
| Moyen | Anticipe la réponse du joueur avant de choisir son coup. |
| Difficile | Simule jusqu’à quatre coups successifs pour comparer les positions. |

## Installation et lancement

### Avec NetBeans 8.0.2

1. Installez le **JDK 8** et **NetBeans 8.0.2**.
2. Téléchargez et décompressez le projet.
3. Dans NetBeans, choisissez **File → Open Project** et ouvrez le dossier `Demo_Java_Jeu_Awale`.
4. Faites un clic droit sur le projet, puis choisissez **Run**.

### En ligne de commande

À la racine du projet, compilez avec **Apache Ant**, puis lancez le fichier JAR.

```bash
ant clean jar
java -jar dist/Demo_Java_Jeu_Awale.jar
```

Si vous disposez déjà du JAR, ouvrez un terminal dans le dossier qui le contient et lancez cette commande.

```bash
java -jar Demo_Java_Jeu_Awale.jar
```

## Règles du jeu

1. Chaque joueur contrôle les deux rangées de son côté. Au départ, chaque case des rangées extérieures contient quatre graines. Le joueur du bas commence.
2. Choisissez une case de votre côté contenant au moins deux graines. Distribuez-les une par une dans vos deux rangées, dans le sens inverse des aiguilles d’une montre.
3. Si la dernière graine tombe dans une case vide, votre tour se termine. Si la case était déjà occupée, reprenez ses graines et poursuivez le semis.
4. Si la dernière graine tombe dans une case intérieure déjà occupée et que les deux cases adverses de la même colonne sont occupées, capturez leurs graines et semez-les à partir de l’origine du dernier semis.
5. Vous gagnez si votre adversaire ne peut plus jouer. La partie est nulle si la même position se présente trois fois ou si un semis tourne en boucle.

Les règles sont également accessibles dans **Aide → À propos**.

## Auteur

**Kalema Daniel Jonathan**
