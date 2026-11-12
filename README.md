# JCI Stafs — Application de Contrôle d'Accès

Application Android officielle de contrôle d'accès et de validation des billets d'événements pour **JCI Comé Excellence (Bénin)**.

---

## Architecture & Fonctionnement en Production

1. **Scan Continu & Haute Performance** :
   - Flux caméra plein écran avec détection QR continue (~10–12 FPS) via CameraX et ZXing.
   - Reticule de visée aux 4 angles arrondis selon la charte graphique JCI.
   - Anti-doublon matériel de 3 secondes pour éviter les lectures multiples accidentelles.
   - Screen Wake Lock permanent pendant la session de contrôle.

2. **Résultats de Scan Plein Écran (Charte Graphique Validée)** :
   - `BILLET VALIDE` : Fond vert (`#16A34A`), coche blanche, nom du participant, badge catégorie (VIP / Standard), numéro de billet, bip aigu, vibration courte.
   - `DÉJÀ SCANNÉ` : Fond ambre (`#F59E0B`), texte sombre (`#3B1D00`), heure du premier passage et nom de l'agent ayant validé le billet, double bip, 2 vibrations.
   - `AUTRE ÉVÉNEMENT` : Fond rouge (`#DC2626`), titre et date de l'événement auquel ce billet est rattaché, bip grave, vibration longue.
   - `BILLET ANNULÉ` : Fond rouge (`#DC2626`), motif d'annulation (remboursement ou annulation), bip grave, vibration longue.
   - `BILLET INCONNU` : Fond rouge (`#DC2626`), code non reconnu comme billet JCI, bip grave.
   - `VÉRIFICATION IMPOSSIBLE` : Fond gris (`#6B7280`), connexion instable ou serveur indisponible, bouton blanc « Rescanner » pour retenter avec la même clé d'idempotence.

3. **Authentification & Sécurité Supabase** :
   - Connexion des agents via Supabase Auth avec la clé publique `anon`.
   - Contrôle strict des habilitations (`checkin.scan`) pour autoriser le scan.
   - Idempotence garantie (`Idempotency-Key` UUIDv4) pour toutes les requêtes de scan.
   - Renouvellement automatique du jeton (`refresh_token`) en cas d'expiration (401).

4. **Saisie Manuelle & Pavé Numérique Dédié** :
   - Feuille inférieure (bottom sheet) avec préfixe automatique `JCI-2026-` et pavé numérique virtuel 3x4 intégré pour saisir les 6 chiffres du billet en cas de QR code abîmé ou illisible.

5. **Historique & Recherche Locale** :
   - Conservation des 50 derniers scans avec statut coloré, recherche filtrée par nom ou numéro en temps réel.

---

## Configuration & Variables d'Environnement

Variables injectées via `.env` :

| Variable | Description |
|---|---|
| `API_BASE_URL` | Racine de l'API de contrôle d'accès JCI |
| `SUPABASE_URL` | Instance Supabase du projet |
| `SUPABASE_ANON_KEY` | Clé anonyme publique Supabase |

---

## Compilation
Voir `GUIDE_COMPILATION_GITHUB.md` : compilation et publication de l'APK avec GitHub Actions
(`.github/workflows/build-android.yml`). La configuration (`API_BASE_URL`, `SUPABASE_URL`, `SUPABASE_ANON_KEY`)
vient des secrets GitHub ; ne commitez jamais de fichier `.env`.
