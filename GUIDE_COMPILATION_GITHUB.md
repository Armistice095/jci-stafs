# Compiler JCI Stafs avec GitHub (sans AI Studio)

GitHub compile l'application dans le cloud, gratuitement pour un dépôt **public**, et publie le fichier
`jci-stafs.apk` dans l'onglet **Releases**. C'est là que votre page de téléchargement le récupère.

## 1. Créer le dépôt
1. Sur github.com : **New repository**, nom `jci-stafs`, visibilité **Public**.
2. Envoyez-y tout le contenu de ce dossier (**Add file > Upload files**, glissez les fichiers).
   - Vérifiez que le dossier caché `.github` (qui contient `workflows/build-android.yml`) est bien envoyé.
     S'il manque : **Add file > Create new file**, tapez le nom `.github/workflows/build-android.yml`
     et collez le contenu du fichier.
   - N'envoyez **jamais** de fichier `.env`, `.jks` ou `.keystore`.

## 2. Ajouter les trois secrets de configuration
Dépôt > **Settings > Secrets and variables > Actions > New repository secret**. Créez :

| Nom | Valeur |
|---|---|
| `API_BASE_URL` | l'adresse de votre API, **avec** la barre finale, ex. `https://api.exemple.bj/api/v1/` |
| `SUPABASE_URL` | l'adresse de votre projet Supabase |
| `SUPABASE_ANON_KEY` | la clé **anon** (publique) de Supabase. Jamais la clé `service_role`. |

Sans ces trois secrets, la compilation s'arrête avec un message clair (c'est voulu : sinon l'application
pointerait vers de fausses adresses d'exemple).

## 3. Lancer la compilation
1. Onglet **Actions** > **Compiler JCI Stafs (APK)** > **Run workflow**.
2. Indiquez la version (ex. `1.0.0`) et validez. Comptez 10 à 15 minutes la première fois.
3. Quand c'est vert : onglet **Releases** > `jci-stafs.apk`. C'est le fichier à installer sur Android.

Pour une nouvelle version, relancez avec un numéro plus grand (ex. `1.0.1`). Une release se crée aussi
automatiquement si vous créez un tag `v1.0.1`.

## 4. Clé de signature permanente (à faire AVANT de diffuser à toute l'équipe)
Sans clé permanente, chaque compilation utilise une clé temporaire différente. Android refuse alors de
mettre à jour l'application par-dessus la version précédente : il faut la désinstaller d'abord.

1. Installez Java (JDK, par exemple Temurin 21). Dans un terminal :
   `keytool -genkeypair -v -keystore jci-release.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`
   (l'alias doit rester `upload`). Choisissez un mot de passe et notez-le.
2. Transformez le fichier en texte :
   - Windows (PowerShell) : `[Convert]::ToBase64String([IO.File]::ReadAllBytes("jci-release.jks")) | Set-Clipboard`
   - Mac / Linux : `base64 -w0 jci-release.jks`
3. Ajoutez trois secrets : `KEYSTORE_BASE64` (le texte obtenu), `STORE_PASSWORD` et `KEY_PASSWORD`
   (le mot de passe choisi, le même des deux côtés).
4. **Sauvegardez `jci-release.jks` et le mot de passe en lieu sûr, hors de GitHub.** Si vous les perdez,
   vous ne pourrez plus publier de mises à jour pour les téléphones déjà installés.

## 5. En cas d'échec
Ouvrez l'exécution en rouge dans l'onglet **Actions**, cliquez sur l'étape qui a la croix rouge
(souvent « Compiler l'APK de production »), copiez les lignes d'erreur et envoyez-les pour correction.
Le code a été relu mais n'avait encore jamais été compilé : une première erreur de compilation est possible.
Les tests unitaires n'empêchent pas la production de l'APK (étape « Tests unitaires » tolérante).
