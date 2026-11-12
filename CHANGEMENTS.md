# Refonte de JCI Stafs : ce qui a changé

## 1. Données : l'application parle enfin à la vraie API
Avant, aucun écran n'affichait de vraies données : la liste d'événements était lue dans un format
incompatible avec le contrat (`{ data, meta }`), et en cas d'échec l'application affichait de faux
événements, un faux compteur (342 / 500), un faux historique et de faux participants.

- `GET /events` : lu comme `{ data: [...] }`, `pageSize=50`, `startsAt` / `venueName` / `city` facultatifs.
- `GET /me` : lit `firstName`, `lastName`, `roles[]`, `permissions`, `eventPermissions` (contrat réel).
  Plus de profil inventé en cas d'échec. Le rôle s'affiche en français (Agent d'accueil, Administrateur…).
- **Droit de contrôler** : permission globale `checkin.scan` OU permission donnée sur un événement
  (`eventPermissions`). Avant, un agent d'accueil assigné à un seul événement aurait été refusé.
- **Nouveau résultat `EVENT_NOT_ACTIVE`** (événement clôturé ou annulé) : avant, il s'affichait
  « BILLET INCONNU ». Il a maintenant son écran (« ÉVÉNEMENT NON ACTIF » + motif du serveur).
- **`WRONG_EVENT`** : `holderName` et `category` sont `null` côté serveur ; l'ancien modèle les exigeait
  non nuls, donc ce scan aurait échoué en « Vérification impossible ».
- Un résultat inconnu n'est plus transformé en « billet inconnu » : il donne « Vérification impossible ».
- **Plus aucune donnée inventée** : fausse date « Samedi 5 déc. à 09:00 », faux participants
  (Armistice ANAGONOU, Koffi MENSAH, Estelle DOSSOU), faux numéros de billet, fausses heures (19:42),
  faux motif (« remboursé le 2 nov. »), faux nom d'événement, « Marc K. ».
- Dates et heures réelles, à l'heure du Bénin (`util/DateFormats.kt`). Avant, trois dates étaient codées en dur.
- Erreurs distinguées : 401 → rafraîchissement du jeton puis une seule nouvelle tentative, sinon retour à
  la connexion avec le message ; 403 / 404 → message précis sur l'écran gris ; réseau → message habituel.
- Au démarrage sans réseau, la session n'est plus supprimée.
- Le bouton retour d'Android ne déconnecte plus l'agent depuis la liste des événements.
- Le préfixe de la saisie manuelle suit l'année de l'événement (plus de « 2026 » en dur).

## 2. Design
- **Police Inter** embarquée (Regular, Medium, SemiBold, Bold) et appliquée partout. Avant : Roboto.
- **Échelle des maquettes** (`ui/theme/DesignScale.kt`) : les maquettes sont dessinées sur 300 de large.
  Les écrans événements, scanner, résultats, menu, saisie et historique sont agrandis proportionnellement
  à la largeur réelle du téléphone, pour retrouver les mêmes proportions. La taille de police Android
  choisie par l'utilisateur est respectée. Accueil et Connexion (validés) ne sont pas touchés.
- Thème clair imposé (pas de mode sombre).
- **Résultats** : réécriture complète. Icône qui « pop » avec un léger rebond, texte qui monte en fondu,
  barre de fermeture de 1,5 s, animations coupées si elles sont désactivées dans Android.
- **Déjà scanné** : heure du premier passage en HH:mm, « Il y a N min », agent ; seules les données reçues s'affichent.
- **Menu** : nom et rôle sur deux lignes (le rôle n'est plus coupé).
- **Saisie manuelle** : bouton « Vérifier le billet » jamais grisé ; « Saisissez les 6 chiffres » si incomplet.
- **Événements** : état de chargement (cartes grises), état vide, erreur avec bouton « Actualiser »,
  liste rechargée à chaque ouverture, titres sur deux lignes.
- **Scanner** : compteur « — » tant que le serveur n'a pas répondu ; écran « Accès à la caméra refusé »
  avec les 3 étapes et un bouton qui ouvre les paramètres Android ; l'autorisation est revérifiée au retour.
- **Historique** : état vide, sous-titre correct, plus de scans de démonstration.

## 3. Fichiers
Modifiés : `MainActivity.kt`, `MainViewModel.kt`, `JciApiRepository.kt`, `RetrofitJciApi.kt`, `Models.kt`,
`FeedbackManager.kt`, `Type.kt`, `Theme.kt`, `EventSelectScreen.kt`, `ScannerScreen.kt`,
`ScanResultOverlay.kt`, `HistoryScreen.kt`, `MenuDialog.kt`, `ManualTicketDialog.kt`, `LoginScreen.kt`,
`ExampleUnitTest.kt`.
Ajoutés : `util/DateFormats.kt`, `ui/theme/DesignScale.kt`, `res/font/inter_*.otf`.
Supprimé : `data/api/JciApiService.kt` (inutilisé).

## 4. À vérifier au premier build
Ce code n'a PAS été compilé (pas d'Android SDK dans l'environnement de travail). La syntaxe de tous les
fichiers a été vérifiée, mais pas les types ni les références. Lancez `./gradlew assembleDebug` puis
`./gradlew test` et signalez toute erreur de compilation.
