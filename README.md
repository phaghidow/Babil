# Bābil Bookstore

Bābil est une librairie numérique de démonstration réalisée en Java Swing. Parcourez le catalogue, filtrez les livres par catégorie, consultez leurs détails, ajoutez des titres au panier et retrouvez vos achats dans la bibliothèque.

## Captures de l’application

| Mode clair | Mode sombre |
|---|---|
| ![Page d’accueil en mode clair](Babil/home_page_light.png) | ![Page d’accueil en mode sombre](Babil/home_page_dark.png) |

## Fonctionnalités

- Catalogue de 25 livres répartis en sept catégories.
- Couvertures locales et éditions numériques EPUB.
- Recherche, filtres, détails, panier et bibliothèque.
- Interface disponible en modes clair et sombre.
- Checkout simulé pour les démonstrations : aucun paiement réel n’est traité.

## Lancer l’application

Prérequis : un JDK 11 ou plus récent. Le pilote JDBC SQLite est fourni dans le dépôt.

Depuis la racine du dépôt, dans PowerShell :

```powershell
cd Babil
$sources = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -cp "lib\sqlite-jdbc-3.51.0.0.jar" -d bin $sources
java -cp "bin;lib\sqlite-jdbc-3.51.0.0.jar" App
```

Au premier lancement, l’application initialise le catalogue dans `data/books.db` à partir de `data/portfolio_books.json`.

## Données du catalogue

Les livres sont des classiques du domaine public référencés par Project Gutenberg. Les couvertures sont stockées dans `Babil/data/covers/` et les éditions EPUB complètes dans `Babil/data/ebooks/public-domain/`. Chaque édition conserve les notices de source et de licence de Project Gutenberg.

## Contenu du dépôt

- `Babil/src/` : application Java Swing, avec le point d’entrée `App.java`, les modèles et les écrans.
- `Babil/data/` : catalogue JSON, base SQLite, couvertures et éditions EPUB.
- `Babil/lib/` : pilote JDBC SQLite utilisé pour compiler et lancer l’application.
- `Babil/bin/` : classes Java compilées.

## Perspectives d’évolution

Développé dans un cadre universitaire durant l’année 2025–2026, Bābil respecte le périmètre académique du projet : il s’agit d’une application de bureau pédagogique, avec un checkout simulé. Les fonctionnalités ci-dessous sont des pistes pour une évolution ultérieure ; elles ne font pas partie de la version actuelle.

- **Authentification et rôles** : créer des comptes de démonstration et distinguer lecteurs et administrateurs, avec des mots de passe stockés de manière sécurisée.
- **Gestion du catalogue** : proposer une interface d’administration pour ajouter, modifier ou retirer des livres et gérer leurs catégories.
- **Favoris et listes de lecture** : permettre aux lecteurs d’enregistrer des titres et de retrouver leurs sélections.
- **Recherche avancée** : filtrer par auteur, langue, note ou catégorie, et combiner plusieurs critères.
- **Avis et évaluations** : ajouter des notes et commentaires, avec une modération réservée à l’administrateur.
- **Suggestions de lecture** : recommander des livres à partir des catégories ou des favoris, à l’aide de règles simples adaptées à un projet pédagogique.
- **Qualité et accessibilité** : ajouter des tests automatisés, améliorer la validation des formulaires et vérifier la navigation au clavier.

Ces évolutions pourraient être développées progressivement dans le même contexte académique, sans intégrer de paiement réel ni de services commerciaux externes.
