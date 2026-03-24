📚 BookstoreApp

BookstoreApp est une application Java Swing simulant une librairie en ligne. Elle permet aux utilisateurs de parcourir une collection de livres, de les ajouter à un panier, de rechercher par titre ou catégorie, et de finaliser un achat. L'application utilise une architecture MVC (Modèle-Vue-Contrôleur) pour séparer clairement la logique métier, l'interface utilisateur et le contrôle des interactions.

🗂 Structure du projet
BookstoreApp/
│
├── src/
│   ├── model/
│   │   ├── Book.java              ← Modèle représentant un livre (titre, auteur, prix, catégorie, image)
│   │   ├── Cart.java              ← Gestion du panier (liste des livres ajoutés)
│   │   ├── Customer.java          ← Informations client (nom, email)
│   │   └── Database.java          ← Simulation ou connexion SQLite
│   │
│   ├── view/
│   │   ├── MainFrame.java         ← JFrame principale avec menu et CardLayout
│   │   ├── HomePanel.java         ← Page d’accueil affichant tous les livres
│   │   ├── BookDetailsPanel.java  ← Page détaillée d’un livre
│   │   ├── SearchPanel.java       ← Résultats de recherche
│   │   ├── CartPanel.java         ← Panier (affichage des livres sélectionnés)
│   │   ├── CheckoutPanel.java     ← Page d’achat / confirmation
│   │   └── components/
│   │       └── BookCard.java      ← Composant graphique pour un livre (image, titre, bouton)
│   │
│   ├── controller/
│   │   ├── BookController.java    ← Chargement des livres, filtrage par catégorie
│   │   ├── CartController.java    ← Gestion du panier (ajout, suppression)
│   │   └── NavigationController.java ← Navigation entre écrans via CardLayout
│   │
│   └── Main.java                   ← Point d’entrée de l’application
│
└── data/
    ├── images/                     ← Couvertures des livres
    └── bookstore.db                ← Base de données SQLite (optionnelle)
⚙️ Fonctionnalités principales
Parcourir les livres
Affichage en grille de tous les livres disponibles.
Chaque livre est représenté par une BookCard avec image, titre, auteur et bouton “Ajouter au panier”.
Détails d’un livre
En cliquant sur un livre, l’utilisateur accède à sa page détaillée avec description complète, prix et options.
Recherche et filtrage
Rechercher un livre par titre ou filtrer par catégorie grâce au BookController.
Les résultats s’affichent dynamiquement dans SearchPanel.
Gestion du panier
Ajouter ou retirer des livres du panier via CartController.
Affichage du panier dans CartPanel, avec total calculé automatiquement.
Finalisation de l’achat
CheckoutPanel permet à l’utilisateur de saisir ses informations (nom, email) et de confirmer la commande.
Navigation fluide
Le NavigationController gère le passage entre les différents écrans grâce au CardLayout, garantissant une interface cohérente et réactive.
Gestion des données
Simulation interne des livres ou persistance via SQLite.
Images des couvertures stockées dans data/images/.
🛠 Technologies utilisées
Java SE 8+
Java Swing pour l’interface graphique
SQLite (optionnel) pour la persistance des données
Architecture MVC pour séparer la logique et l’interface
🚀 Lancer l’application
Cloner le dépôt :
git clone <URL_DU_REPO>
Compiler les sources :
cd BookstoreApp/src
javac Main.java
Lancer l’application :
java Main

Si vous utilisez SQLite, assurez-vous que bookstore.db est présent dans data/.

📌 Remarques
L’interface est entièrement modulable grâce aux composants Swing et au CardLayout.
Le projet est conçu pour être extensible : il est facile d’ajouter de nouvelles catégories de livres, de nouvelles fonctionnalités de recherche, ou une vraie connexion à une base de données.
