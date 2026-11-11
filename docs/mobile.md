# Android et iOS : compilation expérimentale

Le build mobile est un projet Maven indépendant, `mobile/pom.xml`. Cette intégration ne change ni le POM racine, ni les sources desktop, ni le workflow `build-multiplatform.yml`. Le workflow `build-mobile.yml` se lance à chaque push sur `main` et manuellement. Ses jobs Android et iOS n'ont aucune dépendance sur les jobs desktop ou entre eux. Un échec reste visible et n'empêche pas `publish-dist` de publier les distributions desktop.

Les paquets mobiles sont uniquement des **artefacts GitHub Actions** dans cette phase. Aucun commit, push, GitHub Release ou envoi à un magasin n'est ajouté. `publish-dist` ne les récupère pas dans le dépôt. Les noms de sortie des scripts sont :

```text
dist/android/Kharchiche-Konsolidated-1.0.0.apk
dist/ios/Kharchiche-Konsolidated-1.0.0.ipa
```

Ces fichiers sont créés uniquement à partir d'un vrai résultat du packager. Le script vérifie le contenu du paquet ; le workflow vérifie aussi la signature de l'APK. Sans signature Apple, seul un `.app.zip` explicitement non installable est proposé comme artefact de diagnostic, hors de `dist/ios/`.

## Fichiers de cette intégration

- Workflow : `.github/workflows/build-mobile.yml`.
- Module isolé : `mobile/pom.xml`, `mobile/.gitignore`, `mobile/prepare.py`, `mobile/collect.py`.
- Entrée et ouverture des documents : `mobile/src/main/java/MobileMain.java`, `mobile/src/main/java/OpenFileUtil.java`.
- JNI SQLite : `mobile/native/sqlite-bootstrap.c`, et `SQLiteFeature.java`, `SQLiteLoader.java`, `SQLiteBootstrap.java` sous `mobile/src/main/java/mobile/nativeimage/`.
- Tests : `mobile/src/test/java/MobileViewportTest.java`, `mobile/src/test/java/NativeSQLiteSmoke.java`. Les tests desktop existants sont réutilisés, sans suppression ni modification.
- Scripts : `scripts/setup-mobile-tools.sh`, `scripts/setup-mobile-android.sh`, `scripts/build-mobile-sqlite.sh`, `scripts/build-mobile.sh`, `scripts/test-mobile-native-sqlite.sh`, `scripts/mobile-ios-signing.sh`.
- Documentation : `README.md` mis à jour et ce guide `docs/mobile.md` créé.

## Choix de compatibilité

| Élément | Traitement mobile |
| --- | --- |
| Java 21 desktop | Sources communes compilées en bytecode Java 17 pour la toolchain mobile Gluon 22.1.0.1. Aucune baisse de version du desktop. |
| JavaFX 21.0.11 | API Maven conservée ; code natif lié avec le SDK statique Gluon `21-ea+11.2`, prévu pour GluonFX 1.0.23. Ce runtime mobile n'est donc pas le runtime desktop 21.0.11 empaqueté. |
| FXML, images, contrôleurs | Recopiés depuis les sources existantes à chaque préparation, avec configuration de réflexion générée à partir des imports FXML. Les ressources sont incluses dans le binaire natif. |
| Interface de 693 × 939 | L'entrée `MobileMain` démarre le `Main` existant et enveloppe chaque scène dans une zone défilante mise à l'échelle. Aucun FXML desktop n'est modifié. Ergonomie, clavier virtuel et rotation restent à tester sur appareil. |
| SQLite JDBC 3.46.0.0 | Classes JDBC conservées, même SQLite 3.46.0 recompilé pour ARM64 avec le NDK ou Xcode. JNI lié statiquement : pas de `.so` desktop ni de `.dylib` macOS chargé sur téléphone. |
| Métadonnées GraalVM de Xerial | La copie mobile du JAR retire uniquement la feature native-image choisissant la bibliothèque de la machine hôte et nécessitant une API GraalVM plus récente. `SQLiteFeature` fournit les accès JNI ; `SQLiteLoader` charge la bibliothèque intégrée et `SQLiteBootstrap` appelle explicitement l'initialiseur Xerial via un petit pont C. Le JAR Maven d'origine et le pilote desktop restent intacts. |
| OpenPDF 1.3.39 | Conservé. L'import AWT inutilisé de `TicketPDF` est retiré dans la copie de compilation mobile uniquement. Les tests JVM vérifient les tickets, mais cela ne prouve pas la compatibilité AOT/mobile de toutes les branches d'OpenPDF. |
| Ouverture des PDF | La variante mobile d'`OpenFileUtil` indique le chemin du ticket enregistré. `Desktop.open`, `open` et `xdg-open` ne sont pas compilés pour mobile. Le partage/export vers un lecteur PDF reste à intégrer. |
| Données | Attach Storage sélectionne le stockage privé avant l'initialisation SQLite. Les tests emploient leur dossier temporaire. Aucune base locale existante n'est copiée ou migrée. |

Les FXML déclarent actuellement des espaces de noms JavaFX 25/26 ; ils sont chargés par les tests avec JavaFX 21.0.11, qui émet des avertissements. Cette déclaration ne suffit pas à rendre le projet incompatible, et n'a pas été changée pour le desktop.

## Versions choisies

- GluonFX Maven Plugin **1.0.23**, Substrate **0.0.63** transitif.
- GraalVM **Gluon 22.1.0.1-Final**, basé sur Java **17.0.3** ; ne pas remplacer par un GraalVM Oracle/Community quelconque.
- SDK Java statique **18-ea+prep18-9**, SDK JavaFX statique **21-ea+11.2**.
- Maven **3.8.8**, indépendant du wrapper desktop 3.9.9 ; Python **3.12** dans les workflows.
- Android : runner **ubuntu-24.04**, SDK **34**, build-tools **34.0.0**, NDK **26.3.11579264**, command-line tools Google **9477386**. Le plugin utilise Gradle **8.7**. `platform-tools` et les images des runners restent fournis par leurs canaux maintenus.
- iOS : runner **macos-15-intel**, GraalVM hôte x64, cible **ARM64**, Xcode **16.4** explicitement sélectionné (SDK iOS 18.5). Le script échoue clairement si cette version disparaît du runner.

Il s'agit d'une toolchain Gluon historique, choisie pour la compilation croisée mobile. Elle ne constitue pas une promesse de prise en charge de toutes les versions Android/iOS récentes ni de conformité aux conditions actuelles des magasins. Un téléphone n'a besoin d'aucun JDK installé.

## Android, en local

Sur Linux x64 ou Windows avec WSL2, dans un checkout accessible depuis Linux, installer les dépendances hôte du workflow (compilateur C, GTK3, zlib, XTest, unzip et éventuellement Xvfb), puis :

```bash
bash scripts/setup-mobile-tools.sh
source .cache/mobile-tools/env.sh
bash scripts/setup-mobile-android.sh
source .cache/mobile-tools/android-env.sh
python3 mobile/prepare.py
xvfb-run -a mvn -B -ntp -f mobile/pom.xml verify
bash scripts/build-mobile.sh android
"$ANDROID_SDK/build-tools/34.0.0/apksigner" verify --verbose \
  dist/android/Kharchiche-Konsolidated-1.0.0.apk
```

La première installation télécharge plusieurs gigaoctets. Le SDK est normalement dans `.cache/mobile-tools/android-sdk`. `MOBILE_ANDROID_SDK_DIR` permet de choisir un autre emplacement. Sous WSL, `/tmp` peut être nettoyé à l'arrêt de la distribution : garder installation et compilation dans la même session si cet emplacement est utilisé.

L'APK ARM64 est signé avec la **clé de développement** générée par l'outillage Android lorsque aucune clé de distribution n'est configurée. C'est un build de test : activer l'installation depuis la source choisie sur Android ou utiliser `adb install`. Ce n'est pas une publication Google Play. La clé de debug d'un runner neuf peut changer ; une mise à jour d'un APK précédent peut alors être refusée. Ne pas désinstaller une application contenant des données utiles pour contourner cela sans les sauvegarder.

Les logs sont dans `mobile/target/logs/`, `mobile/target/gluonfx/**/gvm/log/` et les artefacts `logs-android`. Un APK compilé et signé doit encore être lancé sur un téléphone ARM64 : accueil, navigation, formulaire, clavier virtuel, création de compte, réservation, persistance après redémarrage et génération des tickets doivent être vérifiés.

Le workflow exécute aussi `bash scripts/test-mobile-native-sqlite.sh` avant le build Android. Ce test compile un véritable exécutable Native Image Linux, avec le même pont C, les mêmes substitutions et les mêmes métadonnées JNI que le mobile. Il crée une base temporaire, écrit une valeur puis ferme et rouvre la base pour vérifier la persistance et la version SQLite. Il requiert les outils C et les bibliothèques de développement zlib sur l'hôte ; il ne prouve pas l'exécution ARM64 sur téléphone.

## iOS et signature Apple

Sur macOS Intel avec Xcode 16.4 sélectionné :

```bash
bash scripts/setup-mobile-tools.sh
source .cache/mobile-tools/env.sh
bash scripts/build-mobile.sh ios
```

Sans secrets Apple, le job tente une compilation et un packaging **sans signature**. En cas de réussite, l'artefact `kharchiche-ios-arm64-unsigned` contient le vrai bundle `.app` dans une archive ZIP. Ce bundle pour appareil ARM64 n'est ni installable sur iPhone en l'état, ni un build de simulateur. Aucun IPA n'est copié dans `dist/ios/`.

Pour activer la création d'un IPA signé dans GitHub Actions, configurer ces quatre secrets du dépôt :

| Secret | Valeur réelle à fournir |
| --- | --- |
| `MOBILE_IOS_CERTIFICATE_P12_BASE64` | Certificat Apple valide **avec sa clé privée**, exporté en P12 puis encodé en base64. |
| `MOBILE_IOS_CERTIFICATE_PASSWORD` | Mot de passe non vide de cet export P12. |
| `MOBILE_IOS_PROVISION_PROFILE_BASE64` | Profil `.mobileprovision` valide encodé en base64, autorisant `com.kharchiche.konsolidated` et correspondant au certificat. |
| `MOBILE_IOS_SIGNING_IDENTITY` | Nom exact de l'identité du certificat reconnue par `security find-identity -v -p codesigning` (par exemple le nom Apple Development/Distribution). |

Un compte Apple Developer et les droits correspondant au mode de distribution sont nécessaires. Un profil de développement/Ad Hoc doit autoriser les appareils visés ; un IPA App Store n'est pas directement installable par simple copie et exige le parcours Apple adapté. Aucun certificat, profil, compte ni secret n'est fourni ou inventé par le dépôt.

Le workflow décode ces secrets dans le répertoire temporaire du runner, vérifie l'expiration et l'identifiant d'application du profil, importe le certificat dans un trousseau temporaire, puis active le profil Maven `ios-signed`. Un jeu de secrets partiel fait échouer clairement le job. Les fichiers de signature sont nettoyés avec une étape `always()`. Après packaging, `codesign --verify --deep --strict` vérifie le bundle avant de copier le vrai IPA vers `dist/ios/Kharchiche-Konsolidated-1.0.0.ipa`.

Pour une signature locale déjà installée dans le trousseau et les profils du Mac : exporter `MOBILE_IOS_SIGNED=true`, `MOBILE_IOS_SIGNING_IDENTITY` et `MOBILE_IOS_PROFILE_NAME` (le champ `Name` du profil, pas son UUID), puis lancer le même script. Cela ne transmet rien à App Store Connect.

## Validation de cette préparation

- **Réussi** : `mvnw.cmd verify` sur Windows/JDK 21, test existant `ApplicationTest` : 1 test, 0 erreur/échec.
- **Réussi** : compilation du module mobile et des tests communs en Java 17, puis `mvn -f mobile/pom.xml verify` sous WSL/GraalVM Gluon : 2 tests, 0 erreur/échec. Cela couvre JDBC SQLite sur la JVM hôte, les FXML/images, les tickets OpenPDF et l'affichage des cinq écrans dans une largeur de 360 pixels, pas leur exécution native ARM64.
- **Réussi** : test d'exécution JNI dans un binaire Native Image hôte : `NATIVE_SQLITE_JNI_PERSISTENCE_OK`. Ce test a détecté puis permis de corriger l'absence d'appel automatique à `JNI_OnLoad` dans la première liaison statique ; les identifiants natifs Xerial doivent être initialisés explicitement. La correction est partagée par Android et iOS.
- **Réussi — Android natif** : compilation ARM64, liaison, packaging GluonFX/Gradle et collecte du véritable APK `dist/android/Kharchiche-Konsolidated-1.0.0.apk`, avec la correction JNI intégrée. Vérification réelle par `apksigner` 34.0.0 : signatures v1 et v2 valides. Le paquet local est un build de développement d'environ 57 Mo.
- **Réussi** : validation du workflow par actionlint 1.7.12 et syntaxe des six scripts Bash. Les jobs GitHub Actions eux-mêmes n'ont pas été exécutés depuis cette session ; les compilations et tests Android ci-dessus ont été exécutés localement sous WSL.
- **iOS** : configuration créée, non exécutée ici (machine Windows/WSL, sans Xcode ni secrets Apple).
- **Téléphones et magasins** : non testés. La signature et la réussite d'un packaging ne prouvent pas le bon fonctionnement de l'interface ou de toutes les fonctionnalités.

Les derniers comptes rendus locaux sont `target/desktop-verify-mobile-task.log`, `mobile/target/jvm-verify-final.log`, `mobile/target/native-jni-test.log`, `mobile/target/native-final-attempt.log` et `mobile/target/apk-signature.log`. Ces fichiers de travail restent ignorés par Git. Aucun commit ni push n'a été effectué par cette préparation.

Références vérifiées : [prérequis et configuration GluonFX](https://docs.gluonhq.com/), [sources GluonFX 1.0.23](https://github.com/gluonhq/gluonfx-maven-plugin/tree/1.0.23), [sources Substrate 0.0.63](https://github.com/gluonhq/substrate/tree/0.0.63), [GraalVM Gluon épinglé](https://github.com/gluonhq/graal/releases/tag/gluon-22.1.0.1-Final), [pilote Xerial 3.46.0.0](https://github.com/xerial/sqlite-jdbc/tree/3.46.0.0), [demande de prise en charge Android/GraalVM OpenPDF](https://github.com/LibrePDF/OpenPDF/issues/1193) et [logiciels du runner macOS 15 Intel](https://github.com/actions/runner-images/blob/main/images/macos/macos-15-Readme.md).
