> État final : distribution unique dans dist/Kharchiche Konsolidated/, raccourci
> local Kharchiche Konsolidated.lnk à la racine. Les sections antérieures sont
> historiques ; le dernier bilan ci-dessous décrit la configuration actuelle.
# Compte rendu de migration

## Analyse avant modification

- 12 classes Java : Main, HotelController, Navigation, OpenFileUtil, Session,
  TicketPDF ; model/Chambre, Client, ClientDataBase, PanierReservation,
  Reservation et ReservationBataBase.
- Cinq FXML : hotel, connexion, inscription, reservation, mesReservations.
- 20 images ; aucun CSS, PDF source ou hotel.db trouvé dans le projet.
- Main ouvre hotel.fxml, puis initialise les tables SQLite.
- Ressources déjà chargées par classpath et chemins FXML relatifs aux images.
- Les deux classes de persistance utilisaient jdbc:sqlite:hotel.db.
- Quatre PDF étaient écrits dans le dossier courant.
- IntelliJ : JDK temurin-21, SDK JavaFX 21.0.11 sous Downloads, SQLite dans
  le cache Maven, OpenPDF dans lib. Aucun chemin absolu dans le code métier.
- État Git initial : README.MD déjà ajouté et modifié ; .gitignore, lib et src
  non suivis. Aucun index modifié, commit ou push effectué.

## Modifications

- Déplacement des sources vers src/main/java et ressources vers src/main/resources,
  sans modification des contrôleurs ni des FXML.
- pom.xml : Java 21, JavaFX 21.0.11, SQLite 3.46.0.0, OpenPDF 1.3.39,
  SLF4J NOP, plugins compilation/tests/JavaFX/JAR/copie des dépendances.
- Wrapper Apache officiel 3.3.2, Maven 3.9.9 ; aucun JAR Wrapper nécessaire
  avec la variante officielle only-script.
- Launcher : point d'entrée non Application pour la distribution sur classpath.
- AppPaths : stockage utilisateur, maintien sur place de la base historique en
  développement, isolation du stockage distribué ; chemins SQLite et PDF adaptés.
- OpenFileUtil : propriété kharchiche.disableOpenFile pour les tests sans lecteur externe.
- Scripts lancer.bat, build-windows.bat et scripts/java21.bat.
- README.MD, .gitignore et test d'intégration ApplicationTest.
- Bibliothèque lib d'origine conservée, désormais inutile à Maven.

## Vérifications réalisées le 10 novembre 2026

- Wrapper, résolution des dépendances, compilation Java 21 et Maven verify : succès.
- Test d'intégration : zéro échec, zéro erreur.
- Main.start et Stage.isShowing ; chargement des cinq FXML et contrôle des images.
- Création des tables, inscription, vérification du compte, connexion,
  réservation, historique, modification et suppression dans une base temporaire.
- Génération des quatre PDF et lecture du nombre de pages avec OpenPDF.
- Commande javafx:run : accueil chargé, base isolée créée ; arrêt après contrôle.
- Contrôle du lanceur : Java 8 refusé avec message explicite, Java 21 accepté.
- jpackage direct et depuis cmd avec PATH isolé au JDK : génération réussie avec Java intégré, dépendances natives
  JavaFX, JAR, FXML et images dans dist/windows-final-validation.
- EXE lancé avec dossier de données de test : accueil chargé et base créée.
  L'outil graphique n'a pas exposé sa fenêtre : affichage visuel non certifié.
- git diff --check : réussi.

## Limites et contrôles manuels restants

- Pas de parcours complet des formulaires par clics ni d'ouverture des PDF
  dans un lecteur externe ; les traitements sont testés automatiquement.
- Pas de test sur un autre PC sans Java/Maven.
- Les FXML existants annoncent JavaFX 25/26 : avertissements sous JavaFX 21.
- Le lancement sur classpath produit un avertissement JavaFX non modulaire.
- Le script complet passe Maven verify, mais jlink échoue ici sur la copie de
  runtime/bin/ucrtbase.dll (AccessDeniedException). Les appels directs à jpackage
  réussissent. Le changement de PATH ne suffit pas à résoudre ce blocage du script.
  La validation complète du .bat sur un Windows ordinaire reste donc à effectuer.
- Maven clean s'est bloqué lors de la suppression de target dans cet environnement ;
  le script utilise verify. Pour une reconstruction depuis zéro sur un PC habituel,
  fermer l'application et lancer .\mvnw.cmd clean verify.
- Les mots de passe en clair sont une limite préexistante conservée ; aucune
  vraie donnée personnelle n'a été ajoutée.

Commit proposé (à valider, non exécuté) :
build(maven): configuration des dépendances et du packaging Windows
## Nettoyage après migration — 10 novembre 2026

- Suppression des anciennes distributions dist/, des compilations out/ et target/,
  des journaux et des caches Maven locaux de .cache/ (environ 522 Mo).
- Conservation intégrale de .cache/exe-data/ et .cache/run-data/, contenant les
  deux bases hotel.db trouvées. Aucun nettoyage du dossier utilisateur.
- Aucune modification des sources, de pom.xml, du Wrapper ou des lanceurs.
- .gitignore contrôlé avec git check-ignore : caches, compilations,
  distributions et données SQLite exclus ; sources, scripts et Wrapper non ignorés.
  Ajout des fichiers auxiliaires SQLite pour les extensions .sqlite et .sqlite3.
- Reconstruction depuis les sources avec Maven verify après suppression de target :
  BUILD SUCCESS ; 1 test, 0 échec, 0 erreur. JavaFX, FXML/images,
  authentification/persistance, réservations et quatre PDF contrôlés par le test.
- Les sorties recréées par le test ont ensuite été supprimées pour laisser le
  projet propre. Maven retéléchargera ses caches au prochain lancement.
- Les anciennes distributions mentionnées plus haut dans ce document ont été
  supprimées. Régénération : .\build-windows.bat avec un JDK 21 complet.
  Le packaging n'a pas été retesté pendant ce nettoyage ; sa réserve précédente
  concernant ucrtbase.dll reste applicable.
- Aucun commit, push ou changement de l'index Git.
## Correction définitive du packaging Windows — 10 novembre 2026

Cette section remplace les réserves de packaging des comptes rendus précédents.

### Diagnostic observé

- L'échec a été reproduit avec jlink seul depuis un batch, dans un répertoire
  neuf, ainsi qu'avec jpackage. La trace localise le refus dans
  DefaultImageBuilder.writeEntry -> Files.copy -> WindowsFileSystemProvider.newByteChannel,
  pendant la création du fichier ucrtbase.dll.
- Les ACL du projet, de dist et du répertoire temporaire autorisent la modification.
  Les sondes d'écriture ordinaires réussissent. La DLL du JDK a pu être ouverte
  en lecture exclusive, et les copies de contrôle en lecture/écriture exclusive.
- Le défaut se reproduit sans ancienne distribution, sans processus Java actif,
  et avec un runtime/temporaire propre. Le simple changement de PATH,
  le déplacement de TEMP ou la séparation jlink/jpackage ne le corrigent pas.
- Les appels directs depuis PowerShell et certaines copies isolées réussissent,
  tandis que la chaîne issue d'un fichier batch refuse ce nom de DLL.
  La reproduction existe aussi sous le compte Windows ordinaire hors du sandbox.
- Aucun événement Defender correspondant n'a été trouvé dans le journal accessible.
  Le composant ou filtre responsable du refus Windows n'est pas identifié :
  attribuer ce refus précisément à Defender serait spéculatif. Son avertissement
  générique est également émis par jpackage pendant une génération réussie.

### Solution retenue

L'UCRT est un composant de Windows 10/11. Microsoft précise que la version système
est toujours utilisée sur ces systèmes, même en présence d'une copie locale.
Le déploiement local de ce composant système est donc évité, plutôt que de
modifier les permissions ou les protections de Windows.

Référence : https://learn.microsoft.com/en-us/cpp/windows/universal-crt-deployment?view=msvc-170

- build-windows.bat appelle scripts/build-windows.ps1, sans élévation.
- Le script vérifie Windows 10/11 x64, l'UCRT système et le JDK 21 x64 complet.
- Maven verify prépare des dépendances dans un dossier d'entrée neuf, au moyen
  de la propriété app.packageDirectory ajoutée au pom.xml.
- jlink crée un runtime indépendant en excluant uniquement `**/ucrtbase.dll`.
  JavaFX contient aussi cette ressource : elle est retirée uniquement des copies
  de packaging des JAR JavaFX Windows, pour empêcher une extraction redondante
  dans le cache au lancement. Les originaux du cache Maven ne sont pas modifiés.
  Un JAR signé provoque un arrêt explicite avant modification.
- Les classes JavaFX et toutes les autres bibliothèques natives restent présentes.
  Aucun changement du code applicatif ou des fonctionnalités.
- jpackage reçoit --runtime-image, sans exécuter son jlink implicite.
- TEMP, TMP, java.io.tmpdir et --temp désignent des chemins propres à la construction.
  Les variables d'environnement sont restaurées en fin d'exécution.
- Aucun paramètre Defender modifié, aucune exclusion antivirus créée,
  aucune DLL téléchargée ou copiée depuis System32, aucun droit administrateur requis.
- Chaque construction dispose d'un identifiant unique ; aucune ancienne
  distribution ou base SQLite n'est supprimée ou écrasée par le script.

### Résultats finaux

- Exécution complète de **build-windows.bat : code de sortie 0**.
- Maven verify : **1 test, 0 échec, 0 erreur**.
- jlink : succès ; modules du runtime contrôlés.
- jpackage : succès ; Java intégré Temurin 21.0.11 x64 exécuté avec succès.
- Test depuis les JAR distribués et le Java intégré :
  **PACKAGED_SMOKE_OK** (scène Main ouverte, cinq FXML, images FXML et trois
  images de chambres, connexion/écriture/historique SQLite, génération/lecture PDF).
- Lancement du véritable EXE depuis un dossier différent :
  processus actif et base de test initialisée ; module natif chargé :
  **C:\Windows\System32\ucrtbase.dll**.
- Aucun AccessDeniedException ni erreur d'extraction dans le cache natif
  lors du lancement du dernier EXE.
- Les deux bases préexistantes .cache/exe-data/hotel.db et .cache/run-data/hotel.db
  ont des SHA-256 identiques avant/après. Aucun fichier SQLite dans la distribution.
- Distributions intermédiaires créées pendant le diagnostic retirées.
  Les journaux sont conservés dans target/packaging-diagnostics et
  target/windows-build/20261110-224039-17558874.
- Avertissements JavaFX non modulaire et versions FXML 25/26 préexistants :
  présents, sans échec des vérifications. Pas de parcours manuel complet des
  formulaires ni de test sur un second PC Windows sans Java.
- Aucun commit, push ou changement de l'index Git.

EXE généré et testé :
C:\BUT\kharchiche-konsolidated\dist\windows-20261110-224039-17558874\Kharchiche Konsolidated\Kharchiche Konsolidated.exe

Distribuer tout le dossier contenant cet EXE, avec app/ et runtime/.
Régénération : .\build-windows.bat
Dernier chemin généré : Get-Content ./dist/windows-latest.txt

Journaux finaux :
- target/packaging-diagnostics/release-build-final.log
- target/packaging-diagnostics/release-smoke.log
- target/packaging-diagnostics/release-exe-smoke/result.txt
## Nettoyage final, distribution fixe et raccourci — 10 novembre 2026

### Fichiers conservés

- Les 14 sources Java de production, les cinq FXML, les 20 images et les tests.
- pom.xml, Maven Wrapper, .mvn/wrapper/maven-wrapper.properties et scripts.
- lanceurMaven.bat, conservé sans modification et testé avec succès.
- lancer.bat : lanceur portable toujours utile ; conservé.
- lib/openpdf-1.3.39.jar : inutilisé par Maven, mais référencé dans
  .idea/libraries/openpdf_1_3_39.xml ; conservé pour ne pas casser l'ancien usage.
  Sa suppression peut être envisagée après abandon de cette configuration IntelliJ.
- README.MD, VALIDATION.md et configuration locale IntelliJ existante.
- Toutes les données préexistantes : six bases SQLite et deux PDF, aux mêmes
  chemins, empreintes SHA-256 identiques avant/après nettoyage.

Bases conservées :
- .cache/exe-data/hotel.db
- .cache/run-data/hotel.db
- target/packaging-diagnostics/exe-smoke/data/hotel.db
- target/packaging-diagnostics/release-exe-smoke/data/hotel.db
- target/packaging-diagnostics/release-smoke/data/hotel.db
- target/packaging-diagnostics/smoke/data/hotel.db

PDF conservés :
- target/packaging-diagnostics/release-smoke/data/tickets/ticket.pdf
- target/packaging-diagnostics/smoke/data/tickets/ticket.pdf

Ces chemins sous target ne doivent pas être effacés avec Maven clean sans
sauvegarde préalable. Aucune donnée du dossier utilisateur de l'application
n'a été supprimée ou déplacée ; les essais utilisent des bases temporaires isolées.

### Générations et diagnostics supprimés

- dist/windows-20261110-224039-17558874/
- dist/windows-20261110-224814-0a9e43cd/
- dist/windows-32241-21605/ (ancienne génération incomplète)
- dist/windows-latest.txt (ancien pointeur désormais inutile)
- target/windows-build/ (anciens runtimes, JAR copiés et temporaires horodatés)
- Anciennes compilations et target/package/ ; les compilations courantes ont
  ensuite été recréées par Maven pour les vérifications.
- Anciens journaux, programmes de sondage, fichiers .cmd/.java/.class de diagnostic
  et caches techniques sous target/packaging-diagnostics/, sauf les données listées.
- Les nouvelles distributions intermédiaires et sauvegardes de remplacement sont
  supprimées automatiquement après publication ; aucun dossier historique Windows.

### Fichiers modifiés ou ajoutés

- build-windows.bat : point d'entrée documenté pour la distribution fixe.
- scripts/build-windows.ps1 : construction temporaire fixe, journaux fixes,
  validation et publication ; correction UCRT intégralement conservée.
- scripts/windows-distribution.ps1 (nouveau) : contrôle des chemins, refus des
  liens/jonctions, protection des données, manifeste de fichiers, contrôle des
  verrous, remplacement avec retour arrière et gestion du raccourci Windows.
- .gitignore : EXE et raccourcis locaux exclus, en plus de dist/, target/, .cache/,
  bases SQLite et fichiers auxiliaires ; Wrapper et scripts restent versionnables.
- README.MD et VALIDATION.md : procédure et bilan actualisés.

### Fonctionnement retenu

La construction se fait dans target/windows-build-tmp/, avec Maven verify, jlink
et jpackage --runtime-image. Le Java embarqué est contrôlé avant publication.
Le résultat est déplacé entier vers dist/Kharchiche Konsolidated/.
L'ancienne distribution est renommée temporairement et restaurée si la bascule
échoue. Après succès, l'ancienne génération et les temporaires sont supprimés.
Un échec Maven ne modifie ni la distribution publiée ni le raccourci.

Le manifeste .kharchiche-distribution.json protège les fichiers avant remplacement :
un ajout ou changement imprévu, une base, un PDF ou une sauvegarde arrête l'opération.
Un fichier ouvert/verrouillé empêche également le remplacement.
La concurrence entre deux constructions est bloquée par un verrou de génération.
Les derniers journaux sont conservés dans target/windows-build-logs/.

Le raccourci Kharchiche Konsolidated.lnk est créé ou remplacé à la racine et pointe
toujours vers le chemin fixe. Son répertoire de travail et son icône correspondent
à la distribution. Il est local, ignoré par Git, et aucun EXE n'est copié à la racine.

### Vérifications exécutées

- Deux générations réussies consécutives avec le code final : codes de sortie 0.
- Les identifiants internes des manifestes diffèrent : remplacement effectif confirmé.
- Après chaque succès : une seule distribution finale, aucun windows-* historique,
  aucun target/windows-build-tmp/ résiduel et aucun EXE à la racine.
- Maven : compilation Java 21 et test d'intégration réussis (1 test, 0 échec,
  0 erreur), couvrant Main/JavaFX, FXML/images, SQLite, réservations et quatre PDF.
- lanceurMaven.bat : application démarrée, base temporaire initialisée.
- Véritable EXE final : démarrage et initialisation SQLite isolée réussis.
- Raccourci Windows final : cible et répertoire vérifiés ; lancement par le shell
  Windows réussi après remplacement, avec une autre base temporaire isolée.
- Les processus EXE et raccourci chargent C:\Windows\System32\ucrtbase.dll.
- Échec Maven simulé : distribution et raccourci strictement inchangés,
  temporaire nettoyé.
- Erreur de bascule du raccourci rencontrée puis corrigée pendant les essais :
  restauration de l'ancienne distribution confirmée par son manifeste.
- PDF fictif dans la distribution : remplacement refusé, fichier préservé.
- Verrou exclusif sur l'EXE : remplacement refusé.
- Six bases et deux PDF préexistants : présence et SHA-256 inchangés.
- git check-ignore : distributions, compilations, caches, bases, EXE et .lnk exclus.
  git ls-files : aucun .lnk indexé. git diff --check : réussi.
- Aucun commit, push ou changement de l'index Git.

Une ancienne entrée « Kharchiche Konsolidated - Lanceur Maven.exe.exe » était déjà
ajoutée puis supprimée dans l'index/working tree au début de la mission (statut AD).
Le fichier n'existe plus sur disque ; l'index existant n'a pas été modifié.
.gitignore n'enlève pas les fichiers déjà indexés.

Chemin final du véritable EXE :
C:\BUT\kharchiche-konsolidated\dist\Kharchiche Konsolidated\Kharchiche Konsolidated.exe

Raccourci local :
C:\BUT\kharchiche-konsolidated\Kharchiche Konsolidated.lnk

Régénération :
.\build-windows.bat

Résumé machine des vérifications :
target/windows-build-logs/final-verification.txt

Le contrôle sur un second PC sans Java et le parcours manuel complet des
formulaires restent hors de ces essais. Les avertissements FXML 25/26 et
JavaFX non modulaire préexistants restent présents sans échec des tests.
## Ajout Linux/macOS — 10 novembre 2026

Périmètre : ajout des scripts natifs et d'un workflow manuel ; aucun changement
des sources de production, des chemins SQLite, du pom.xml ou du packaging Windows.

Fichiers ajoutés :
- scripts/build-linux.sh et scripts/build-macos.sh : entrées par système.
- scripts/build-unix.sh : Maven verify, sélection native, runtime jlink,
  app-image, contrôle du lanceur puis .deb/.dmg et publication de la plateforme.
- scripts/check-native-input.py : inventaire des ressources du JAR et native SQLite.
- scripts/publish-native.py : remplacement limité à dist/linux ou dist/macos,
  contrôle du manifeste/données, verrou, reprise d'une bascule interrompue.
- src/test/java/DistributionSmoke.java : vérification courte du runtime distribué ;
  ce programme n'est pas exécuté par Surefire ni livré dans l'application.
- .github/workflows/native-distributions.yml : workflow_dispatch uniquement,
  Linux x64 et macOS ARM64/Intel, artefacts et journaux, aucune release.
- .gitattributes : scripts shell/Python et workflow conservés en LF.

Fichiers actualisés : .gitignore, README.MD et VALIDATION.md.

Vérifications réellement exécutées :
- Une seule exécution Maven verify sous Windows/JDK 21 : BUILD SUCCESS,
  1 test, 0 échec, 0 erreur. Le nouveau programme de contrôle compile également.
- bash -n sur les trois scripts shell : succès, sous Git Bash Windows.
- Inventaire du JAR applicatif : les 25 ressources sont présentes.
- Lecture du JAR SQLite : natives Linux/Mac x86_64 et aarch64 présentes.
- Contrôle des labels de runners et des versions d'actions dans leurs sources officielles.
- Lecture des scripts Python et du workflow ; pas d'exécution native Python Unix ici.
- Aucun changement des bases de .cache/ ; contrôle SHA-256 avant/après.
- Windows : aucune régénération ; scripts et distribution existants préservés.
- Aucun commit, push ou lancement distant du workflow.

À confirmer lors du premier workflow :
- Ubuntu : toolkit graphique sous Xvfb, paquet DEB et entrée de menu,
  lancement avec runtime intégré, SQLite/PDF et dépendances glibc/GTK.
- macOS ARM64 et Intel : .app, .dmg, lancement, SQLite/PDF, archivage du bundle.
- Installation sur une machine destinataire sans Java et restrictions Gatekeeper.
- Aucun certificat Developer ID ni notarisation configurés.

Nettoyage limité : suppression de target/package/, copies temporaires de dépendances
recréées pour cette vérification Maven. Aucun fichier source ou donnée utilisateur
supprimé ; les bibliothèques et lanceurs historiques sont conservés car leurs
usages IntelliJ/Windows n'ont pas été abandonnés explicitement.
## Réorganisation de l'arborescence

- VALIDATION.md déplacé dans docs/ ; arborescence.txt conservé dans docs/.
- Workflow renommé .github/workflows/build-multiplatform.yml.
- lancer.bat déplacé dans scripts/, répertoire courant adapté.
- JAR OpenPDF manuel déplacé dans scripts/lib/ ; référence IntelliJ corrigée.
- README renommé README.md ; liens et procédure actualisés.
- Sorties et publication macOS séparées : dist/macos/arm64 et dist/macos/x64.
  Le workflow et les scripts emploient ces nouveaux chemins.
- Suppression de l'ancienne distribution Windows commencée mais interrompue :
  des JAR sont verrouillés par une application en cours. Aucun processus utilisateur
  n'a été fermé de force. Cette ancienne distribution doit être retirée après
  fermeture de l'application ; elle ne doit pas être considérée comme intacte.
- Raccourci .lnk conservé conformément au dernier arbre demandé.
- Utilitaires partagés et DistributionSmoke conservés, nécessaires au packaging CI.
- Aucun Maven, jpackage, lancement d'application ou reconstruction exécuté.
- Bases .cache et autres données utilisateur ni déplacées ni modifiées.
- Aucun commit ni push.