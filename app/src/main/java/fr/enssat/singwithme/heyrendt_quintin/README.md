# Documentation technique

Cette documentation a pour objectif d'expliquer l'architecture du projet, le fonctionnement de l'application ainsi que
les différents choix techniques réalisés.

## Architecture du projet

L'architecture du projet se découpe ainsi :

- data
- ui
    - home
    - karaoke
    - navigation
    - theme
- util

### Répertoire data

Le répertoire data contient les classes objets utiles pour l'application. On y retrouve PlaylistItem pour les éléments
de la playlist, Song pour une musique et LyricSegment pour représenter un segment de parole possédant une durée, un
temps de démarrage et le texte du segment.

### Répertoire ui

Le répertoire ui représente les interfaces et leurs logiques.

Dans les sous-répertoires home et karaoke nous retrouvons
les deux pages de l'application. Un fichier Screen pour chaque contenant les différents composants pour assembler la
page, un fichier ViewModel pour séparer la logique métier ainsi qu'un fichier ViewModelFactory pour injecter les
dépendances nécessaires aux ViewModels.

Dans le sous-répertoire navigation se trouve la logique pour la navigation de l'application. Notamment pour naviguer sur
la page de karaoké d'une musique et de passer en paramètre le fichier md lié.

Enfin le répertoire theme contient les fichiers utiles pour les couleurs et pour définir le thème global de
l'application.

### Répertoire util

Le répertoire util contient les classes utilitaires utilisées par les ViewModel, nous retrouvons :

- PlaylistUtil : Pour télécharger et parser la playlist
- SongUtil : Pour télécharger et parser une musique
- PreferenceManager : Pour stocker et lire des données dans les SharedPreferences
- MediaCache : Pour initialiser une instance de SimpleCache pour la mise en cache des fichiers MP3

## Choix techniques

Concernant les choix techniques, nous utilisons Ktor comme client HTTP, Moshi pour désérialiser et sérialiser du JSON
ainsi que ExoPlayer pour la gestion du lecteur audio.