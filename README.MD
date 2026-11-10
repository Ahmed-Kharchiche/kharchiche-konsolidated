# Kharchiche Konsolidated

Application JavaFX de réservation hôtelière : inscription, connexion, chambres classiques/doubles/VIP, options, panier, simulation de paiement, historique, modification, annulation et tickets PDF.

Projet pédagogique d'Ahmed Kharchiche (BUT Informatique, IUT de Valence).
Les prix par nuit sont 78 €, 132 € et 330 €. Le paiement est simulé.
Les mots de passe restent stockés en clair dans le schéma existant : utiliser des données fictives.

## Technologies et prérequis

- JDK **21** complet (JAVA_HOME vers le JDK, et son dossier bin dans PATH).
- JavaFX **21.0.11**, Controls et FXML ; Graphics/Base transitifs.
- SQLite JDBC **3.46.0.0**, OpenPDF **1.3.39**, SLF4J NOP **1.7.36**.
- Maven **3.9.9**, téléchargé par le Wrapper officiel **3.3.2**.
- Internet au premier lancement pour Maven et les dépendances.
- Windows 10/11 x64 et JDK 21 x64 pour fabriquer la distribution Windows.
- Aucun SDK JavaFX ni Maven à installer manuellement.

## Cloner et lancer

~~~bash
git clone https://github.com/Ahmed-Kharchiche/kharchiche-konsolidated.git
cd kharchiche-konsolidated
./mvnw javafx:run
~~~

Sous Windows PowerShell :

~~~powershell
$env:JAVA_HOME = "C:\chemin\vers\jdk-21"
.\mvnw.cmd javafx:run
~~~

Ou double-cliquer sur **scripts/lancer.bat**, qui contrôle Java 21 et affiche les erreurs.
Pour compiler et exécuter les tests : **.\mvnw.cmd verify**.
Sur Unix, utiliser **sh mvnw verify** si le droit d'exécution n'est pas présent.

## IntelliJ IDEA

Ouvrir le pom.xml comme projet Maven, choisir JDK 21 pour le projet et Maven,
puis recharger Maven. Lancer l'objectif **javafx:run** depuis la fenêtre Maven,
ou la classe **Launcher**. Supprimer les anciennes options VM contenant le
chemin absolu vers le SDK JavaFX. Les bibliothèques sont désormais gérées par Maven.
Main et tous les contrôleurs existants sont conservés ; aucun module-info.java.

## Application Windows autonome et raccourci local

Depuis Windows 10/11 x64 avec JAVA_HOME vers un JDK 21 x64 complet :

~~~powershell
.\build-windows.bat
~~~

La distribution finale est toujours :

~~~text
dist/Kharchiche Konsolidated/
  Kharchiche Konsolidated.exe
  app/
  runtime/
  .kharchiche-distribution.json
~~~

Un raccourci **Kharchiche Konsolidated.lnk** est créé ou actualisé à la racine.
Il pointe vers ce chemin fixe et fonctionne après remplacement de la distribution.
C'est un fichier local ignoré par Git ; aucun deuxième EXE n'est créé à la racine.
Après déplacement du projet, relancer la génération pour actualiser son chemin absolu.

Le batch appelle scripts/build-windows.ps1 :

1. Maven Wrapper **verify** compile et exécute les tests.
2. Une distribution neuve est assemblée sous **target/windows-build-tmp/** avec
   des dépendances propres, un runtime jlink, puis **jpackage --runtime-image**.
3. La correction UCRT est conservée : Windows 10/11 fournit ucrtbase.dll.
   Sa copie locale redondante est exclue de jlink et des copies de packaging
   des JAR JavaFX ; les originaux Maven et les autres natives restent inchangés.
4. Après contrôle des fichiers et du Java intégré, l'ancienne distribution est
   renommée temporairement, la nouvelle prend sa place et le raccourci est actualisé.
   Une erreur de bascule restaure l'ancienne version ; un échec de compilation
   ne modifie ni la distribution existante ni le raccourci.
5. L'ancienne génération et le dossier temporaire sont supprimés après succès.
   Il ne reste qu'une distribution finale, sans historique horodaté.
   Les derniers journaux restent dans **target/windows-build-logs/**.

Fermer l'application avant de régénérer. Une génération concurrente est refusée.
Le manifeste généré contrôle les fichiers avant remplacement : une base SQLite,
un PDF, une sauvegarde, un ajout ou une modification inattendue provoque un arrêt
pour préserver les données. Le script ne déplace aucune base utilisateur.

Le double-clic sur le raccourci ou sur le véritable EXE démarre l'application
sans Java, Maven ni IntelliJ sur le PC de destination.
Distribuer **tout le dossier Kharchiche Konsolidated**, avec app/ et runtime/,
éventuellement en ZIP. Aucun installateur WiX ni droit administrateur requis.
Le raccourci local du dépôt ne doit pas être distribué à la place de ce dossier.

Références : [JavaFX Maven](https://openjfx.io/openjfx-docs/),
[jpackage JDK 21](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
et [UCRT système Windows](https://learn.microsoft.com/en-us/cpp/windows/universal-crt-deployment?view=msvc-170).
## SQLite, sauvegardes et PDF

Les tables sont créées au premier lancement, sans données préchargées.
Aucune base personnelle n'est livrée.

- En développement, si hotel.db existe dans le dossier courant, elle reste utilisée sur place.
  Lancer depuis la racine du projet pour retrouver l'ancienne base.
- Sinon, la base est créée dans **%LOCALAPPDATA%/Kharchiche Konsolidated/hotel.db**.
- L'EXE utilise toujours ce dossier utilisateur, même si une base existe dans son dossier de lancement.
- Hors Windows : **~/.kharchiche-konsolidated/hotel.db**.
- Les PDF sont enregistrés dans le sous-dossier **tickets** du dossier utilisateur.
- Pour un environnement isolé : propriété JVM **-Dkharchiche.dataDir=CHEMIN**.
- Les tests utilisent exclusivement un dossier temporaire et des informations fictives.

Pour transférer explicitement une ancienne base vers la distribution :
**fermer toutes les instances de l'application**, sauvegarder hotel.db et ses éventuels
fichiers -wal/-shm, puis copier hotel.db vers le dossier utilisateur.
Ne copier qu'une base correctement fermée (transactions WAL consolidées).
Si une base existe déjà à destination, la sauvegarder séparément et choisir laquelle conserver :
**ne jamais écraser ni fusionner automatiquement les bases**.
L'ancienne base n'est ni déplacée ni supprimée par l'application.
Une sauvegarde du dossier utilisateur protège aussi les tickets.

Les bases, sauvegardes, PDF générés, caches, sorties de compilation et configurations
locales IntelliJ sont ignorés par Git. Ne jamais ajouter de vraies données personnelles.

## Structure

~~~text
pom.xml
mvnw / mvnw.cmd / .mvn/wrapper/
lancer.bat
build-windows.bat
scripts/java21.bat
scripts/build-windows.ps1    orchestration jlink/jpackage
scripts/windows-distribution.ps1 remplacement sécurisé et raccourci
src/main/java/               Main, Launcher, HotelController, TicketPDF…
src/main/java/model/         modèles, persistance et AppPaths
src/main/resources/view/     cinq écrans FXML
src/main/resources/images/   images d'origine
src/test/java/               test d'intégration
target/                     compilation et bibliothèques (ignoré)
dist/                       application Windows (ignoré)
~~~

## Vérifications

Le test d'intégration vérifie le démarrage du toolkit JavaFX, le chargement des cinq
FXML et leurs images, l'inscription/connexion via les méthodes de persistance,
la création/modification/suppression des réservations et l'historique SQLite,
ainsi que la génération et la lecture des quatre types de PDF.
Il nécessite une session graphique ; ce n'est pas un test complet des clics utilisateur.

Les FXML d'origine déclarent des versions JavaFX 25/26 : ils se chargent sous 21
mais émettent un avertissement. Leur contenu est conservé.
Avant une diffusion publique, contrôler visuellement les écrans, parcourir les formulaires,
ouvrir les PDF dans un lecteur et tester l'archive sur un autre PC Windows sans Java.
## Fichiers générés et nettoyage

Les compilations, caches, distributions, EXE, raccourcis et bases sont ignorés
par Git. Sources, tests, pom.xml, scripts et Maven Wrapper restent versionnables.

Le nettoyage final a supprimé les anciennes distributions dist/windows-*,
les anciennes générations runtime, les journaux et diagnostics devenus inutiles.
Toutes les bases SQLite et les PDF préexistants sont conservés à leur emplacement,
y compris dans .cache/ et target/packaging-diagnostics/.
Ne pas supprimer ces dossiers en bloc ni lancer Maven clean sans sauvegarder
les données qu'ils contiennent. La génération Windows utilise verify.

**lanceurMaven.bat** est conservé et testé : ses chemins locaux correspondent
à cette installation. **scripts/lancer.bat** est aussi conservé comme lanceur portable.
**scripts/lib/openpdf-1.3.39.jar** reste présent pour l'ancienne configuration IntelliJ ;
Maven utilise uniquement sa dépendance déclarée dans pom.xml. Ces deux derniers
fichiers pourraient être supprimés après abandon des usages correspondants,
mais ne sont pas supprimés automatiquement.

Les fichiers temporaires de construction Windows sont supprimés après la génération ;
les journaux fixes target/windows-build-logs/ sont conservés pour diagnostiquer un échec.
Le détail du nettoyage et des tests figure dans docs/VALIDATION.md.
## Distributions Linux et macOS

Le packaging Windows existant reste inchangé. Les scripts Unix utilisent les
dépendances JavaFX natives sélectionnées par Maven pour leur système et leur JDK.
SQLite JDBC 3.46.0.0 fournit les natives Linux/macOS x64 et ARM64.
FXML, images et OpenPDF sont inclus, avec un runtime Java 21 intégré.
Aucun Java/Maven n'est requis sur le poste destinataire.

### Linux Ubuntu/Debian

Construire sur Linux avec un JDK 21 natif et Python 3.9+ :

~~~bash
sudo apt-get install fakeroot dpkg-dev xvfb xauth libgtk-3-0 libasound2
export JAVA_HOME=/chemin/vers/jdk-21
xvfb-run -a bash scripts/build-linux.sh
~~~

Sur Ubuntu 24.04, utiliser les paquets **libgtk-3-0t64** et **libasound2t64**
à la place des deux derniers noms. Depuis une session graphique avec DISPLAY,
on peut directement exécuter **bash scripts/build-linux.sh**.

Résultat : **dist/linux/kharchiche-konsolidated-1.0.0-x64.deb**.
Installer avec **sudo apt install ./dist/linux/*.deb**, puis ouvrir
Kharchiche Konsolidated depuis le menu des applications. Les dépendances
graphiques système restent nécessaires ; le runtime Java est embarqué.
La compatibilité avec des versions Ubuntu/Debian plus anciennes que le système
de construction reste à tester : ne pas promettre une compatibilité glibc universelle.

### macOS Apple Silicon et Intel

Construire sur un Mac avec un JDK 21 correspondant à sa puce et Python 3.9+ :

~~~bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
bash scripts/build-macos.sh
~~~

Résultats : **dist/macos/arm64/Kharchiche Konsolidated.app** et
**dist/macos/arm64/Kharchiche-Konsolidated-1.0.0-arm64.dmg** (ou **x64.dmg**).
Pour l'application seule : **CREATE_DMG=0 bash scripts/build-macos.sh**.
Lancer l'application entière, sans extraire son exécutable de son bundle.

Les architectures sont construites séparément sur des machines natives ;
aucun binaire universel Intel/ARM64 n'est annoncé.
Le bundle utilise l'identifiant **com.kharchiche.konsolidated**.
Aucune signature Developer ID ni notarisation Apple n'est configurée.
Gatekeeper peut refuser une application téléchargée ; une distribution publique
signée et notarifiée nécessitera une configuration Apple supplémentaire.
La version minimale réellement compatible de macOS reste à vérifier sur les
machines destinataires, en particulier avec des artefacts produits sous macOS 15.

### Sécurité du remplacement et stockage

Les scripts construisent dans un dossier privé sous target/, puis testent le
runtime distribué, SQLite, PDF, FXML et le véritable lanceur avec des données fictives.
Le .deb est inspecté et doit contenir une entrée .desktop.
Un échec avant publication conserve les anciens artefacts.
Le remplacement concerne exclusivement **dist/linux/**, **dist/macos/arm64/** ou **dist/macos/x64/** :
la version Windows, .cache/ et les bases existantes ne sont jamais nettoyées.

Un manifeste vérifie que l'ancienne sortie est gérée et inchangée.
La présence de bases, PDF, sauvegardes ou fichiers ajoutés bloque le remplacement.
Les .app peuvent contenir leurs liens symboliques natifs, conservés lors de la publication.
Les temporaires de cette construction sont retirés ; les journaux restent sous
**target/native-logs/<plateforme>-<architecture>/**.

Le chemin des données reste celui déjà utilisé hors Windows :
**~/.kharchiche-konsolidated/hotel.db**, avec les PDF dans **tickets/**.
En développement, une ancienne hotel.db du dossier courant conserve le comportement
historique. En distribution, la propriété kharchiche.packaged=true impose le dossier
utilisateur. Aucune migration ni copie automatique des bases Windows.
Un dossier contenant des données personnelles ne doit pas servir de sortie de packaging.

### Générer depuis GitHub Actions sans posséder Linux/macOS

Le workflow **.github/workflows/build-multiplatform.yml** est exclusivement manuel :

1. Après validation, committer/pousser les fichiers soi-même sur le dépôt.
   Le workflow doit être présent sur la branche par défaut pour afficher son bouton.
2. Ouvrir **Actions → Native distributions → Run workflow**, puis choisir la branche.
3. Attendre les tâches **linux-x64**, **macos-arm64** et **macos-x64**.
4. Ouvrir l'exécution et télécharger ses artefacts :
   **kharchiche-linux-x64**, **kharchiche-macos-arm64**, **kharchiche-macos-x64**.
   Les journaux sont fournis séparément et les distributions sont conservées 14 jours.

Linux utilise ubuntu-24.04 avec Xvfb ; macOS utilise macos-15 (ARM64) et
macos-15-intel (Intel), avec des JDK 21 natifs.
Les .app sont archivées avec **ditto** avant téléversement pour préserver
les droits d'exécution et les liens symboliques. Décompresser le fichier .app.zip
avec les outils macOS, ou utiliser le .dmg.
Chaque exécution CI dispose de son propre espace ; les artefacts des anciennes
exécutions expirent selon leur rétention, ils ne sont pas des doublons locaux.

Aucune release, aucun déploiement, aucune signature et aucune publication automatique.
Les scripts sont invoqués avec bash : pas besoin de modifier l'index Git pour
ajouter un bit exécutable depuis Windows. .gitattributes impose les fins de ligne LF.

### État de validation multiplateforme

Sous Windows : compilation et tests Maven réussis, syntaxe Bash vérifiée,
présence des natives SQLite Linux/Mac x64/ARM64 vérifiée dans le JAR,
et bases .cache/ conservées avec empreintes identiques.
Les classes applicatives, pom.xml et scripts Windows ne sont pas modifiés.

Les .deb/.app/.dmg et le workflow n'ont pas été exécutés ici sur Linux/macOS.
Le premier workflow doit encore confirmer les constructions natives, les tests
graphiques, le lancement et les formats de paquet. L'installation sur des machines
destinataires sans Java et le comportement de Gatekeeper restent à vérifier.

Références : [jpackage](https://docs.oracle.com/en/java/javase/21/jpackage/packaging-overview.html),
[JavaFX Maven](https://openjfx.io/openjfx-docs/),
[runners GitHub](https://github.com/actions/runner-images).
## Organisation actuelle

La racine conserve README.md, pom.xml, le Wrapper Maven, les configurations Git,
build-windows.bat et lanceurMaven.bat. La documentation détaillée est dans
docs/VALIDATION.md et le lanceur secondaire dans scripts/lancer.bat.
L'ancien JAR manuel OpenPDF est conservé dans scripts/lib/ avec sa référence
IntelliJ corrigée ; Maven continue à gérer sa propre dépendance.

Les utilitaires build-unix.sh, check-native-input.py, publish-native.py et
src/test/java/DistributionSmoke.java sont nécessaires au workflow et conservés.
.gitattributes garantit les fins de ligne LF pour les scripts Unix.

La sortie macOS Intel est dans dist/macos/x64/ ; Apple Silicon dans
dist/macos/arm64/. Chacune remplace uniquement sa propre architecture.
Aucune distribution n'a été reconstruite lors de cette réorganisation.
Le raccourci Windows local est conservé et cible toujours la future sortie
dist/Kharchiche Konsolidated/Kharchiche Konsolidated.exe.
Fermer l'application avant de supprimer une distribution générée.