# Kharchiche Konsolidated

**Kharchiche Konsolidated** est une application de bureau de gestion de réservations hôtelières, développée en Java et JavaFX par **Ahmed Kharchiche**, dans le cadre de son apprentissage en BUT Informatique.

L’application représente un hôtel fictif. Elle permet de créer un compte, de choisir une catégorie de chambre et des dates de séjour, de préparer un panier, puis d’enregistrer et de consulter ses réservations. Les données sont conservées localement avec SQLite et des documents PDF accompagnent les opérations.

Ce projet pédagogique met en pratique la programmation orientée objet, les interfaces événementielles, la persistance relationnelle et l’organisation d’une application de bureau. Les étapes bancaires sont des **simulations** : aucune transaction financière réelle n’est effectuée.

Dépôt : [Ahmed-Kharchiche/kharchiche-konsolidated](https://github.com/Ahmed-Kharchiche/kharchiche-konsolidated).

## Fonctionnalités principales

| Domaine | Fonctionnement présent dans le code |
|---|---|
| Inscription | Saisie du nom, du prénom, de la date de naissance, de l’adresse électronique et du mot de passe ; contrôle des champs requis et de l’unicité de l’adresse ; enregistrement du client. |
| Connexion et session | Vérification des identifiants dans SQLite, conservation du client connecté en mémoire, adaptation du menu de compte et déconnexion. |
| Informations personnelles | Consultation des informations du client connecté. Le panneau nommé « modifier les informations » ne réalise pas actuellement de mise à jour persistante du compte. |
| Chambres | Choix entre chambre classique, chambre double et suite VIP ; affichage du tarif et d’une image correspondant à la catégorie. |
| Séjour et options | Sélection des dates d’arrivée et de départ ; choix du petit-déjeuner, du Wi-Fi et du parking. |
| Calcul du prix | Calcul du nombre de nuits, des options et du montant total du séjour. |
| Panier | Ajout de réservations, consultation du détail et du total, suppression d’une réservation sélectionnée et vidage du panier. Le panier reste en mémoire pendant la session. |
| Validation | Confirmation des informations personnelles puis enregistrement des réservations du panier dans SQLite. |
| Historique | Chargement des réservations du client connecté depuis la base de données. |
| Modification et annulation | Modification de la catégorie, des dates et des options, recalcul du montant et opérations SQL de mise à jour ou de suppression. Les écarts de prix conduisent à un parcours de supplément ou de remboursement simulé. |
| Documents PDF | Génération de tickets de réservation, de modification, de supplément et de remboursement. |
| Navigation | Cinq vues FXML, navigation entre les écrans, onglets de réservation et animations de panneaux et d’images sur l’accueil. |

### Tarifs et logique de calcul

| Catégorie | Tarif par nuit |
|---|---:|
| Chambre classique | 78 € |
| Chambre double | 132 € |
| Suite VIP | 330 € |

Les options ajoutent au tarif journalier : **5 €** pour le petit-déjeuner, **2 €** pour le Wi-Fi et **3 €** pour le parking.

~~~text
Montant du séjour = (tarif de la chambre + options par nuit) × nombre de nuits
~~~

La durée est calculée à partir des dates avec **LocalDate** et **ChronoUnit.DAYS**. Un client de démonstration interne possède un traitement particulier à montant nul ; ce mécanisme n’est pas une gestion des rôles ou des promotions.

### Paiement : une simulation uniquement

Les formulaires affichent des champs de carte bancaire et de RIB. Des filtres de saisie formatent certains champs, mais ils ne constituent pas une validation bancaire complète. La confirmation déclenche les traitements locaux, l’enregistrement des réservations et la production des PDF.

**Aucune banque ni aucun prestataire de paiement n’est contacté. Aucun débit, supplément ou remboursement réel n’est réalisé.** Les mentions de transaction validée dans les documents correspondent à cette simulation. Utiliser exclusivement des informations fictives.

## Captures d’écran

Les photographies présentes dans les ressources illustrent l’hôtel ; elles ne remplacent pas des captures de l’application.

| Écran à présenter | Capture |
|---|---|
| Accueil, catégories de chambres et navigation | À ajouter |
| Inscription et connexion | À ajouter |
| Sélection des dates, options et panier | À ajouter |
| Validation et simulation de paiement | À ajouter |
| Historique et modification d’une réservation | À ajouter |
| Exemple de ticket PDF | À ajouter avec des données fictives |

Les captures pourront être placées dans **docs/captures/**. Aucun lien vers une image inexistante n’est ajouté ici.

## Technologies utilisées

Les versions ci-dessous proviennent du **pom.xml** et de la configuration du Maven Wrapper.

| Technologie | Version ou configuration | Rôle |
|---|---|---|
| Java | Niveau de compilation **21** | Modèles métier, traitements, dates, collections et accès aux fichiers. |
| JavaFX | **21.0.11** | Interface graphique, contrôles et animations. Dépendances directes javafx-controls et javafx-fxml ; Base et Graphics sont transitives. |
| FXML | Chargeur de JavaFX 21.0.11 | Description des cinq interfaces et liaison avec le contrôleur. |
| SQLite JDBC | org.xerial:sqlite-jdbc **3.46.0.0** | Accès à la base SQLite locale et bibliothèques natives du pilote. |
| JDBC | API java.sql du JDK | Connexions, requêtes SQL, paramètres et lecture des résultats. |
| OpenPDF | com.github.librepdf:openpdf **1.3.39** | Création des documents PDF. |
| Maven | **3.9.9** via le Wrapper | Dépendances, compilation, tests et préparation des bibliothèques à distribuer. |
| Maven Wrapper | **3.3.2**, variante only-script | Téléchargement et utilisation de Maven sans installation manuelle de Maven. |
| Plugin JavaFX Maven | **0.0.8** | Exécution avec l’objectif javafx:run. |
| JUnit Jupiter | **5.11.4** | Tests automatisés. |
| SLF4J NOP | **1.7.36**, dépendance d’exécution | Fournisseur de journalisation utilisé avec les dépendances du projet. |
| jlink et jpackage | Outils du JDK 21 | Création des runtimes et des distributions natives. |
| Git et GitHub Actions | Configuration du dépôt | Gestion des versions, compilation multiplateforme et mise à disposition des distributions. |
| PowerShell, Bash et Python | Python **3.9+** pour les scripts Unix | Orchestration et contrôle du packaging par plateforme. |

La version applicative déclarée dans Maven est **1.0.0**. Il n’y a pas de **module-info.java** : l’application est distribuée sur le classpath. Aucun SDK JavaFX n’a besoin d’être installé séparément pour le développement Maven.

## Architecture et compétences mobilisées

### Une organisation inspirée de MVC

Le projet s’appuie sur les principes du modèle **Modèle–Vue–Contrôleur**, avec une séparation identifiable :

- **Modèle** : Client, Chambre, Reservation et PanierReservation représentent les données et certaines opérations métier. ClientDataBase et ReservationBataBase assurent la persistance ; AppPaths centralise les chemins de stockage.
- **Vue** : les fichiers de **src/main/resources/view/** décrivent l’accueil, la connexion, l’inscription, la réservation et l’historique. Les images sont chargées depuis les ressources de l’application.
- **Contrôleur** : HotelController relie les champs et événements FXML aux traitements, fait évoluer les onglets, remplace les scènes et coordonne les opérations de données.

**Main** démarre JavaFX et initialise les tables. **Launcher** sert de point d’entrée à la distribution. **Session** conserve le client connecté et le panier en mémoire ; **Navigation** contient une collection de scènes. **TicketPDF** et **OpenFileUtil** prennent en charge les documents et leur ouverture.

**La séparation MVC n’est pas complète.** HotelController regroupe notamment la navigation, les vérifications de formulaires, des calculs de prix et les appels à SQLite et aux PDF. Il n’existe pas de couche de services métier indépendante. Cette structure constitue une première mise en pratique de MVC, avec des responsabilités qui pourraient être davantage séparées.

### Programmation orientée objet

Le projet mobilise la conception de classes, les constructeurs, les méthodes d’instance, les accesseurs et la collaboration entre objets. Une réservation associe un client, une chambre, des dates, des options et un montant ; le panier regroupe plusieurs réservations et en calcule le total.

L’encapsulation est présente dans plusieurs modèles, notamment Chambre et Reservation. Elle reste partielle : certains attributs de Client et l’état de Session sont publics. Les collections Java, dont List, ArrayList et Map, servent à organiser les réservations, les tarifs et les scènes.

### Interfaces JavaFX et événements

Les vues utilisent des champs texte, champs de mot de passe, sélecteurs de date, boutons radio, cases à cocher, listes, menus, onglets et boîtes de dialogue.

Les annotations **@FXML** associent les éléments graphiques aux méthodes du contrôleur. Les événements pilotent les sélections, le panier, la navigation et les confirmations. Des **TextFormatter** interviennent sur les champs bancaires, et des **TranslateTransition** animent certains éléments de l’accueil.

### SQLite, JDBC et opérations CRUD

Les connexions passent par **DriverManager** et l’URL fournie par **AppPaths.jdbcUrl()**. Les tables **client** et **reservation** sont créées si elles n’existent pas. Les opérations utilisent des requêtes SQL et des **PreparedStatement** avec paramètres ; les connexions et instructions sont généralement fermées par **try-with-resources**.

| Données | Création | Lecture | Mise à jour | Suppression |
|---|---|---|---|---|
| Clients | Inscription | Connexion, existence d’une adresse et récupération de l’identifiant | Pas de mise à jour persistante du profil implémentée | Pas de suppression de compte implémentée |
| Réservations | Validation du panier | Historique et recherche d’une réservation | Dates, catégorie, options et montant | Annulation |

Les clients et réservations sont conservés entre les sessions. Le panier et le client actuellement connecté sont des objets de session en mémoire.

### Traitements métier et documents

Les traitements combinent la durée du séjour, les tarifs et les options. Les contrôles portent notamment sur les champs d’inscription, l’adresse déjà utilisée, les identifiants de connexion, l’ordre des dates et les confirmations demandées à l’utilisateur.

**TicketPDF** utilise OpenPDF pour produire quatre types de documents : réservation, remboursement simulé, supplément simulé et modification sans ajustement de prix. Ils contiennent les informations du client, du séjour et des montants associés. Leur ouverture utilise les moyens disponibles sur le système : Desktop, open ou xdg-open.

### Limites actuelles et pistes de progression

Le projet est un prototype pédagogique local :

- Les mots de passe sont stockés en clair. Un client de démonstration est également défini en dur dans **Session.java** ; ses informations doivent être anonymisées avant diffusion. Ses valeurs ne sont pas reproduites dans ce README.
- La modification persistante du profil client n’est pas implémentée.
- Certains cas de saisie ou de sélection restent à fiabiliser : dates absentes, modification sans réservation sélectionnée et parcours d’annulation.
- Le chargement de l’historique ajoute les éléments à la liste sans la vider préalablement ; plusieurs chargements peuvent donc dupliquer l’affichage.
- La gestion des erreurs repose souvent sur des alertes et des traces console. Tous les scénarios d’échec ne sont pas couverts par les tests.
- Il n’y a pas de gestion d’inventaire de chambres physiques ni de contrôle global des disponibilités entre clients.

Ces limites distinguent les fonctionnalités présentes d’une application de réservation utilisable en production.

## Structure du projet

~~~text
kharchiche-konsolidated/
├── .github/workflows/build-multiplatform.yml
├── .mvn/wrapper/maven-wrapper.properties
├── docs/
│   └── VALIDATION.md
├── scripts/
│   ├── build-windows.ps1
│   ├── windows-distribution.ps1
│   ├── java21.bat
│   ├── lancer.bat
│   ├── build-linux.sh
│   ├── build-macos.sh
│   ├── build-unix.sh
│   ├── check-native-input.py
│   ├── publish-native.py
│   └── lib/                       ancien JAR manuel OpenPDF
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── model/
│   │   │   │   ├── AppPaths.java
│   │   │   │   ├── Chambre.java
│   │   │   │   ├── Client.java
│   │   │   │   ├── ClientDataBase.java
│   │   │   │   ├── PanierReservation.java
│   │   │   │   ├── Reservation.java
│   │   │   │   └── ReservationBataBase.java
│   │   │   ├── HotelController.java
│   │   │   ├── Main.java / Launcher.java
│   │   │   ├── Session.java / Navigation.java
│   │   │   └── TicketPDF.java / OpenFileUtil.java
│   │   └── resources/
│   │       ├── images/
│   │       └── view/
│   │           ├── hotel.fxml
│   │           ├── connexion.fxml
│   │           ├── inscription.fxml
│   │           ├── reservation.fxml
│   │           └── mesReservations.fxml
│   └── test/java/
│       ├── ApplicationTest.java
│       └── DistributionSmoke.java
├── dist/                          distributions et archives
├── .cache/                        données locales à préserver
├── .idea/                         configuration IntelliJ locale
├── target/                        fichiers générés par Maven
├── out/                           éventuelle compilation IntelliJ
├── .gitignore / .gitattributes
├── build-windows.bat
├── lanceurMaven.bat
├── publier.bat
├── mvnw / mvnw.cmd
├── pom.xml
└── README.md
~~~

Le JAR manuel **scripts/lib/openpdf-1.3.39.jar** reste référencé par l’ancienne configuration IntelliJ. Un projet importé avec Maven utilise la dépendance du pom.xml.

Les fichiers de compilation, caches, bases et raccourcis locaux sont ignorés par Git. Les distributions sont également ignorées par défaut, mais le workflow de publication les ajoute explicitement au dépôt avec Git LFS. Un fichier déjà indexé n’est pas retiré de Git par une règle d’exclusion.

## Installation et exécution

### Prérequis pour lancer le projet

- **Windows** : un JDK Temurin installé dans `C:\Program Files\Eclipse Adoptium` (Java 21 recommandé). Le lanceur `lanceurMaven.bat` recherche automatiquement Java 21, puis Java 25 si nécessaire.
- Une connexion Internet lors du premier lancement pour récupérer Maven et les dépendances du projet.
- Une session graphique pour afficher l'interface JavaFX.

**Maven, JavaFX et IntelliJ IDEA n'ont pas besoin d'être installés séparément** pour lancer l'application avec le fichier `.bat`. Le projet utilise Maven Wrapper, qui télécharge Maven si nécessaire. Le JDK reste requis pour cette méthode ; à l'inverse, les distributions natives embarquent Java.

### Récupérer le projet

Pour cloner le dépôt avec Git :

~~~bash
git clone https://github.com/Ahmed-Kharchiche/kharchiche-konsolidated.git
cd kharchiche-konsolidated
~~~

Il est également possible de télécharger l'archive ZIP du dépôt sur GitHub, puis de l'extraire entièrement avant le lancement.

### Démarrer sous Windows — méthode recommandée

1. Installer **Temurin JDK 21** si ce n'est pas déjà fait, dans son emplacement habituel (`C:\Program Files\Eclipse Adoptium`).
2. Ouvrir le dossier du projet.
3. **Double-cliquer sur `lanceurMaven.bat`**, à la racine du dépôt.
4. Patienter pendant la récupération initiale des dépendances, la compilation et le démarrage de l'application JavaFX.

Le lanceur recherche automatiquement le JDK Temurin (Java 21 en priorité, Java 25 en solution de repli) et s'appuie sur **Maven Wrapper** pour exécuter le projet. **Il n'est pas nécessaire de saisir une commande dans le terminal.** Java 21 reste la version de référence du projet ; la compatibilité complète de Java 25 n'est pas garantie.

> **Important :** `lanceurMaven.bat` est un lanceur de développement : il nécessite le JDK et les fichiers du projet. Pour distribuer l'application à quelqu'un qui ne possède pas Java, utiliser la distribution Windows complète décrite plus bas, avec son exécutable et son runtime embarqué.

#### Autres méthodes de lancement sous Windows

Depuis PowerShell, à la racine du projet, avec un JDK 21 configuré :

~~~powershell
.\mvnw.cmd javafx:run
~~~

Le script `scripts/lancer.bat` peut aussi être utilisé avec un Java 21 déjà configuré. Pour forcer un nettoyage, une recompilation et le lancement :

~~~powershell
.\mvnw.cmd clean javafx:run
~~~

**Attention : `clean` supprime tout `target/`.** Certaines bases et certains PDF d'essais locaux ont été conservés dans `target/packaging-diagnostics/` : les sauvegarder avant toute commande contenant `clean`. Pour le lancement habituel, `javafx:run` sans `clean` suffit.

### Démarrer sous Linux ou macOS

Avec un JDK 21 adapté au système :

~~~bash
bash mvnw javafx:run
~~~

L’invocation explicite par bash évite de dépendre du bit exécutable du Wrapper après un clonage depuis Windows.

### Ouvrir avec IntelliJ IDEA

1. Ouvrir **pom.xml** comme projet Maven.
2. Choisir le JDK 21 pour le projet et pour l’exécution Maven.
3. Recharger les dépendances Maven.
4. Lancer l’objectif **javafx:run** dans la fenêtre Maven, ou exécuter la classe **Launcher**.

Les anciennes options de VM contenant un chemin absolu vers un SDK JavaFX installé manuellement ne sont plus nécessaires au lancement Maven.

### Compiler et tester

~~~powershell
.\mvnw.cmd verify
~~~

Sur Linux/macOS :

~~~bash
bash mvnw verify
~~~

**ApplicationTest** utilise des données fictives dans un dossier temporaire. Il exerce notamment l’inscription et la connexion au niveau de la persistance, les réservations et l’historique, la mise à jour et la suppression, les quatre PDF et le chargement des cinq FXML avec leurs images.

**DistributionSmoke** est un programme de contrôle utilisé par les scripts Unix avec le Java et les JAR distribués. Il ne constitue pas une fonctionnalité de l’application et n’est pas exécuté comme un test Surefire ordinaire.

Sous Linux sans affichage, utiliser Xvfb, par exemple **xvfb-run -a bash mvnw verify**. Ces contrôles ne remplacent pas un parcours manuel complet des formulaires ni un essai sur chaque système destinataire.

### Données SQLite, PDF et sauvegardes

**AppPaths** détermine les emplacements de stockage :

| Situation | Base utilisée |
|---|---|
| Développement, sans surcharge, avec une hotel.db dans le dossier courant | Cette base existante reste utilisée sur place. |
| Windows, avec LOCALAPPDATA défini, sans base historique sélectionnée | %LOCALAPPDATA%\Kharchiche Konsolidated\hotel.db. |
| Environnement sans LOCALAPPDATA, normalement Linux/macOS | ~/.kharchiche-konsolidated/hotel.db. |
| Propriété JVM -Dkharchiche.dataDir=CHEMIN | hotel.db dans le dossier indiqué ; utile pour isoler un essai. |

Les distributions positionnent **kharchiche.packaged=true** : elles utilisent le dossier utilisateur ou la surcharge explicite, et ne sélectionnent pas la base historique du dossier courant.

Les deux fichiers **.cache/exe-data/hotel.db** et **.cache/run-data/hotel.db** sont des bases locales conservées. Ils ne sont pas sélectionnés automatiquement par l’application. Ne pas les supprimer, déplacer ou réinitialiser dans le cadre d’un nettoyage de compilation.

Les PDF sont écrits dans le sous-dossier **tickets/** du dossier utilisateur ou de kharchiche.dataDir :

- ticket.pdf ;
- ticket_remboursement.pdf ;
- ticket_rajout.pdf ;
- ticket_modification.pdf.

Ces noms sont réutilisés : un nouveau document du même type peut remplacer le précédent. Copier les tickets à conserver dans un autre emplacement ; il n’existe pas d’archivage PDF automatique.

Avant une copie ou un transfert de base, fermer l’application et conserver une sauvegarde, ainsi que les éventuels fichiers SQLite auxiliaires. Une base en mode WAL doit être correctement fermée et consolidée avant de copier le seul fichier principal. Ne pas écraser une base de destination sans en conserver séparément les données ; aucune fusion automatique n’est implémentée.

Les comptes, mots de passe, informations personnelles et bases réelles ne doivent pas être publiés dans le dépôt.

### Générer et utiliser la version Windows

Construction sur Windows 10/11 x64, avec un JDK 21 x64 complet :

~~~powershell
.\build-windows.bat
~~~

Le lanceur est actuellement **à la racine du projet**. Il appelle **scripts/build-windows.ps1** et **scripts/windows-distribution.ps1**.

La sortie locale est :

~~~text
dist/build/Kharchiche Konsolidated/
├── Kharchiche Konsolidated.exe
├── app/
│   ├── Kharchiche Konsolidated.cfg
│   ├── kharchiche-konsolidated.jar
│   └── lib/
├── runtime/
└── .kharchiche-distribution.json
~~~

Le packaging assemble un runtime Java avec jlink, les ressources et toutes les dépendances d’exécution, puis crée le lanceur avec **jpackage --runtime-image**. La distribution Windows a fait l’objet de contrôles de démarrage dans l’environnement de développement ; le dossier présent contient son Java embarqué. Un essai sur un autre PC sans Java reste un contrôle distinct à réaliser.

Pour utiliser cette distribution, **conserver et transmettre le dossier entier**, éventuellement dans une archive ZIP. Le destinataire lance **Kharchiche Konsolidated.exe**, sans Maven, IntelliJ ou JDK installé séparément. Déplacer l’EXE seul supprime l’accès à ses bibliothèques et à son runtime.

Une construction réussie remplace l’ancienne distribution au même emplacement. Le script construit d’abord dans **target/windows-build-tmp/**, vérifie le résultat et prévoit un retour arrière en cas d’échec de bascule. Il refuse les fichiers verrouillés et les modifications ou données inattendues dans la distribution. Les journaux sont conservés sous **target/windows-build-logs/**.

Le raccourci local **Kharchiche Konsolidated.lnk**, lorsqu’il est généré à la racine, pointe vers cet EXE. Il est ignoré par Git et ne remplace pas une archive distribuable.

La correction liée à **ucrtbase.dll** est conservée : Windows 10/11 fournit son UCRT système, et les copies locales redondantes sont exclues du runtime et des copies de packaging des JAR JavaFX. Les originaux Maven ne sont pas modifiés. Aucun réglage antivirus ni droit administrateur n’est requis par ce script.

### Générer les versions Linux et macOS

Ces constructions se font **sur le système et le processeur de destination**, ou sur les runners correspondants de GitHub Actions. Les scripts demandent un JDK 21 natif et Python 3.9+.

**Ubuntu 24.04 x64 :**

~~~bash
sudo apt-get install fakeroot dpkg-dev xvfb xauth libgtk-3-0t64 libasound2t64
export JAVA_HOME=/chemin/vers/jdk-21
xvfb-run -a bash scripts/build-linux.sh
~~~

Dans une session graphique possédant DISPLAY, **bash scripts/build-linux.sh** suffit. Les noms des bibliothèques GTK/ALSA diffèrent selon la version de Debian/Ubuntu ; par exemple, les systèmes antérieurs peuvent utiliser libgtk-3-0 et libasound2.

Le résultat est un paquet tel que **dist/linux/kharchiche-konsolidated-1.0.0-x64.deb**, avec Java embarqué et une entrée pour le menu des applications. Installation :

~~~bash
sudo apt install ./dist/linux/*.deb
~~~

Les bibliothèques graphiques du système restent nécessaires. La compatibilité avec des versions de glibc ou des distributions plus anciennes que le système de construction n’est pas garantie.

**macOS :**

~~~bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
bash scripts/build-macos.sh
~~~

Les sorties sont séparées par architecture :

~~~text
dist/macos/
├── arm64/   # Apple Silicon
└── x64/     # Intel
~~~

Chaque dossier peut contenir **Kharchiche Konsolidated.app** et un fichier .dmg dont le nom inclut la version et l’architecture. Pour produire seulement l’application : **CREATE_DMG=0 bash scripts/build-macos.sh**.

Les versions Intel et Apple Silicon sont distinctes ; aucun binaire universel n’est annoncé. Le bundle utilise **com.kharchiche.konsolidated**. Aucune signature Developer ID ni notarisation Apple n’est configurée : **Gatekeeper peut empêcher l’ouverture d’une application téléchargée**. Une diffusion signée et notarifiée nécessite une configuration Apple supplémentaire.

Les scripts Unix vérifient les ressources, les natives et le démarrage avec des données fictives avant de publier. Ils remplacent uniquement **dist/linux/**, **dist/macos/arm64/** ou **dist/macos/x64/**, avec un manifeste de contrôle et une reprise de bascule. Ils ne nettoient pas les bases Windows ou .cache/. Les journaux sont dans **target/native-logs/**.

Des artefacts Linux/macOS sont présents dans dist/. Leur présence ne prouve pas un essai sur un poste destinataire. Le lancement, l’installation, Gatekeeper et les versions minimales des systèmes doivent être confirmés sur les environnements concernés.

### GitHub Actions et publication du dépôt

Le workflow **.github/workflows/build-multiplatform.yml** est actuellement déclenché :

- par un push sur **main** ;
- manuellement avec **Actions → Native distributions → Run workflow**.

Il prévoit quatre constructions : Windows x64, Linux x64, macOS ARM64 et macOS Intel, avec Java 21. Les paquets sont téléchargeables depuis les **artefacts de l’exécution** ; leur durée de conservation configurée est de 14 jours, et celle des journaux de 7 jours.

Les applications macOS sont archivées avec **ditto** pour préserver les droits et liens du bundle. Les archives téléchargées doivent être extraites entièrement.

**Le workflow comporte aussi un job publish-dist qui commit et pousse les distributions sur main, avec Git LFS, après réussite des constructions.** Il ne se limite donc pas à produire des artefacts temporaires. Les permissions du dépôt, les protections de branche et les quotas Git LFS peuvent empêcher cette étape.

Les fichiers publiés comprennent **dist/windows/Kharchiche-Konsolidated-Setup.exe**, **dist/windows/Kharchiche-Konsolidated-Portable.zip**, le dossier Windows complet sous **dist/build/Kharchiche Konsolidated/**, le .deb sous **dist/linux/** et les .dmg/.app.zip sous **dist/macos/<architecture>/**. Aucune GitHub Release n’est créée par ce workflow ; cette publication ne met pas automatiquement à jour les applications déjà installées.

Le workflow installe WiX Toolset 3 et génère automatiquement l’installateur Windows avec `jpackage --type exe`. Pour créer aussi les paquets localement, installer WiX Toolset 3 (avec `candle.exe` et `light.exe` dans le PATH), puis exécuter `./scripts/package-windows.ps1` après la construction, avec `JAVA_HOME` pointant vers le JDK 21.

~~~text
dist/
├── windows/
│   ├── Kharchiche-Konsolidated-Setup.exe
│   └── Kharchiche-Konsolidated-Portable.zip
├── linux/
├── macos/
│   ├── arm64/
│   └── x64/
└── build/
    └── Kharchiche Konsolidated/
        ├── Kharchiche Konsolidated.exe
        ├── app/
        └── runtime/
~~~

Le script local **publier.bat n’est pas un simple lanceur**. Il exécute **clean verify**, ajoute les modifications de fichiers déjà suivis avec **git add -u**, crée un commit puis effectue un push vers **origin main**. Il peut donc déclencher le workflow et sa publication des distributions. Les nouveaux fichiers non suivis doivent être examinés et ajoutés séparément si l’on souhaite les publier.

Avant son utilisation, vérifier les modifications et sauvegarder les données locales éventuellement présentes dans target/, puisque sa commande clean efface ce dossier. Ne pas lancer publier.bat pour simplement compiler ou démarrer l’application.

Les scripts peuvent être utilisés dans un dépôt GitLab, mais aucune configuration GitLab CI n’est fournie actuellement.

### Vérifications et documentation complémentaires

Le compte rendu [docs/VALIDATION.md](docs/VALIDATION.md) détaille les vérifications et l’historique des adaptations techniques. Les essais Windows et les contrôles de disposition des vues réalisés précédemment ne constituent pas une certification de toutes les distributions ni de tous les parcours utilisateur.

Cette actualisation du README repose sur la lecture des sources et scripts présents. Elle n’exécute ni publier.bat, ni une nouvelle construction, ni un push.

Références techniques : [JavaFX et Maven](https://openjfx.io/openjfx-docs/), [jpackage JDK 21](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html), [déploiement UCRT Windows](https://learn.microsoft.com/en-us/cpp/windows/universal-crt-deployment?view=msvc-170) et [runners GitHub Actions](https://github.com/actions/runner-images).

## Contribution personnelle et utilisation de l’IA

### Développement personnel

J’ai personnellement assuré la conception initiale et le développement des fonctionnalités de **Kharchiche Konsolidated** : représentation des clients, chambres et réservations, logique des séjours et des tarifs, interfaces JavaFX/FXML, interactions utilisateur, accès SQLite/JDBC et intégration des documents PDF.

Ce projet m’a permis de mobiliser et de consolider mes compétences en programmation orientée objet, interfaces événementielles, bases de données relationnelles et structuration logicielle inspirée de MVC. Les limites identifiées constituent des pistes concrètes d’amélioration de cette première application.

### Assistance technique de l’intelligence artificielle

L’IA a été utilisée ensuite comme assistance pour simplifier l’exécution et la distribution : configuration Maven et du Wrapper, préparation de l’exécutable Windows avec son runtime Java, puis scripts de packaging Linux/macOS et configuration de la compilation multiplateforme.

L’assistance a également porté sur des tests techniques, la réorganisation des fichiers, la documentation et l’harmonisation des dimensions et positions dans les FXML, en conservant les éléments et les actions existants. Ces interventions sont distinguées de la conception initiale et de l’implémentation des fonctionnalités métier.

**L’application n’intègre aucune fonctionnalité d’intelligence artificielle et n’a pas besoin d’un service d’IA pour fonctionner.**
