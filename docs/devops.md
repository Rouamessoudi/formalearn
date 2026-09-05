# DevOps — FormaLearn (Phase 6)

## 1. CI/CD

Monorepo : `backend/` (Spring), `frontend/` (Angular), `ml/` (Python).  
La CI est définie dans le `Jenkinsfile` à la racine. Un échec de compilation ou de test **arrête** le pipeline (`set -e` / code retour Maven, npm, pytest, docker).

Flux :

GitHub → Jenkins (Checkout) → Backend compile/tests → Frontend install/build/tests → ML tests + training → Sonar (optionnel) → Package JAR/Angular/modèle → Docker build des 3 images.

Pas de déploiement production automatique dans cette phase.

## 2. Jenkins

Jenkins n’est **pas** dans le compose applicatif. Fichier `jenkins/docker-compose.yml` uniquement (UI **http://localhost:9090**).  
Stack LMS : `docker compose up --build` à la racine (mysql, ml, backend, frontend).

Image d’agent : `jenkins/Dockerfile` (JDK 17, Git, Node 20, Python 3, Chromium, client Docker).

## 3. Pipeline

Pipeline **déclarative**. Paramètres :

| Paramètre | Défaut | Rôle |
|---|---|---|
| `SKIP_SONAR` | `true` | Ignore Sonar tant qu’aucun serveur n’existe |
| `RUN_FRONTEND_TESTS` | `true` | Karma + ChromeHeadless |
| `FORCE_FAIL` | `false` | Démo soutenance : FAILURE volontaire |

## 4. Stages

1. **Checkout** — `checkout scm`
2. **Backend Build** — `./mvnw -B clean compile`
3. **Backend Tests** — `./mvnw -B test` (échec = FAILED)
4. **Frontend Install** — `npm ci`
5. **Frontend Build** — `npm run build`
6. **Frontend Tests** — `ng test --watch=false --browsers=ChromeHeadlessNoSandbox`
7. **ML Tests** — `pytest` dans un venv CI
8. **ML Training** — `generate_dataset.py` + `train.py` + présence du `.pkl`
9. **SonarQube** — exécuté seulement si `SKIP_SONAR=false`
10. **Package** — JAR + `index.html` Angular + modèle
11. **Docker Build** — images backend, frontend, ml
12. **CI quality gate (demo)** — si `FORCE_FAIL=true`

## 5. Tests

- Backend : JUnit (auth, catalogue, inscriptions, MLA). **MySQL/MariaDB local doit être joignable** (`localhost:3306`, base `formation_platform`) pendant `mvn test`.
- Frontend : Karma, 1 spec `AppComponent`. Chrome/Chromium obligatoire sur l’agent.
- ML : dataset, joblib, FastAPI TestClient `/health` et `/predict`.

## 6. SonarQube

**Non branché par défaut.** Aucun token n’est écrit dans le Jenkinsfile.

Pour l’activer plus tard :

1. Installer SonarQube.
2. Jenkins → Credentials → Secret text ID **`sonar-token`**.
3. Variable globale `SONAR_HOST_URL` (ex. `http://sonarqube:9000`).
4. Ajouter le plugin Maven `sonar-maven-plugin` au `backend/pom.xml`.
5. Relancer le job avec `SKIP_SONAR=false`.

Tant que ces éléments manquent, laisser `SKIP_SONAR=true`. Ne pas afficher un “Sonar SUCCESS” fictif.

## 7. Docker

Voir `docs/docker.md`. Images : `backend/Dockerfile`, `frontend/Dockerfile`, `ml/Dockerfile`.

## 8. Docker Compose

`docker compose up --build` démarre **mysql, ml, backend, frontend**.  
MySQL publié en **3307** sur l’hôte (XAMPP reste sur 3306). Entre conteneurs : `mysql:3306`.

Frontend : **http://localhost:8088** (Nginx `/api` → `backend:8080`).  
API : **http://localhost:8080**. ML : **http://localhost:8000**.

## 9. Variables d’environnement

Fichier modèle : `.env.example` (copier vers `.env`, **ne pas committer**).

| Variable | Usage |
|---|---|
| `MYSQL_HOST` | `localhost` en local, `mysql` dans Compose |
| `MYSQL_PORT` | `3306` |
| `MYSQL_DATABASE` | `formation_platform` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | compte JDBC |
| `MYSQL_ROOT_PASSWORD` | image MySQL |
| `JWT_SECRET` | secret JWT (≥ 32 caractères) |
| `ML_BASE_URL` | `http://localhost:8000` local, `http://ml:8000` Compose |

## 10. Credentials Jenkins

| ID / type | Obligatoire ? | Usage |
|---|---|---|
| GitHub (username/password ou GitHub App / PAT) | Oui pour Checkout SCM | Cloner le dépôt |
| `sonar-token` (Secret text) | Seulement si Sonar activé | Analyse |
| Docker Hub | Non | Pas de push registry dans cette phase |
| Mot de passe MySQL | Non dans Jenkins | Les tests utilisent la base de l’agent ; Compose lit `.env` |

Aucun mot de passe n’est stocké dans Git.

## 11. Lancer Jenkins

```bat
cd /d "C:\Users\PC\Desktop\Nouveau dossier (7)\jenkins"
docker compose up --build -d
```

Ouvrir http://localhost:9090  
Compte démo (volume neuf + `init.groovy.d`) : **admin** / **change_me_jenkins**.

Si l’UI affiche *Authentication required* et que ce mot de passe est refusé, le volume Jenkins a été créé **avant** le script d’admin. Recréer le volume (efface l’historique des builds locaux) :

```bat
cd /d "C:\Users\PC\Desktop\Nouveau dossier (7)\jenkins"
docker compose down
docker volume rm jenkins_jenkins_home
docker compose up -d --build
```

Le job **formalearn-ci** est décrit dans `jenkins/job-formalearn-ci.xml` (New Item → Pipeline, ou recopier le XML). Autre option : New Item → Pipeline script from SCM une fois le projet sur GitHub.

L’image `jenkins/Dockerfile` installe déjà les plugins **Pipeline**, **Git** et **Credentials Binding**.  
Le wizard d’installation est désactivé (`runSetupWizard=false`). Au premier démarrage, un compte **`admin` / `change_me_jenkins`** est créé (variables `JENKINS_ADMIN_ID` / `JENKINS_ADMIN_PASSWORD` dans `jenkins/docker-compose.yml`). **Compte de démo locale uniquement** — changez le mot de passe, ne l’exposez pas sur Internet.

Les tests Spring dans le conteneur Jenkins utilisent `MYSQL_HOST=host.docker.internal` (XAMPP/MariaDB de l’hôte Windows, port **3306**). Sans cette base, le stage Backend Tests échoue — c’est le comportement CI attendu.

## 12. Lancer la pipeline

**Option A — GitHub (soutenance idéale)**

1. Pousser le projet sur GitHub (sans `.env`, sans `.venv`).
2. Jenkins → New Item → **Pipeline** → Definition : **Pipeline script from SCM** → Git → URL du repo → `Jenkinsfile`.
3. Si le dépôt est privé : Credentials → Username + PAT GitHub.
4. Build with Parameters.

**Option B — Dossier local monté (`/workspace`)**

1. New Item → Pipeline → Definition : **Pipeline script**.
2. Coller le contenu du `Jenkinsfile` (ou utiliser le job `formalearn-ci` créé par `jenkins/bootstrap-job.groovy`).
3. Le stage Checkout copie alors `backend/`, `frontend/`, `ml/` depuis `/workspace`.

Démo d’échec : relancer avec `FORCE_FAIL=true`, ou casser un `assert` dans `CatalogueIntegrationTest` : le stage Backend Tests doit passer **rouge**.

Équivalent local (même stages, hors Checkout GitHub) :

```bat
cd /d "C:\Users\PC\Desktop\Nouveau dossier (7)"
powershell -File scripts\run-ci.ps1
```

## 13. SUCCESS / FAILURE

- **SUCCESS** : tous les stages verts, images Docker construites.
- **FAILURE** : le premier stage en erreur arrête la suite. Lire la console du stage rouge (Maven, npm, pytest, docker).

## Frontend Tests — limitation

Karma lance **Chrome Headless**. Sur un agent sans Chrome/Chromium, le stage échoue (comportement voulu). L’image `jenkins/Dockerfile` installe Chromium (`CHROME_BIN`). Sur Windows natif, installer Google Chrome. Si l’agent n’a vraiment pas de navigateur, décocher `RUN_FRONTEND_TESTS` et le dire clairement à l’oral (le build Angular reste obligatoire).
