# FormaLearn — Plateforme de formations (PI Esprit, Sujet 4)

FormaLearn est une application LMS académique : un **administrateur** pilote le catalogue et les sessions, un **apprenant** consulte l’offre, s’inscrit et reçoit des **recommandations** (MLA).

## 1. Présentation

Monorepo Angular + Spring Boot + MySQL + FastAPI (scikit-learn) + Jenkins + Docker.

Étudiante : **Roua Messaoudi**, 4SAE11.

## 2. Objectif

Démontrer deux modules métier complets, un moteur de recommandation supervisé (sans TF-IDF), une CI Jenkins et un déploiement Docker local.

## 3. Fonctionnalités

**Authentification** : login JWT, rôles `ADMIN` / `APPRENANT`, endpoints protégés.

**Module 1 — Catalogue** : catégories, formations (DRAFT/PUBLISHED), chapitres, CRUD admin, consultation apprenant, recherche et filtres, fiche détail.

**Module 2 — Sessions & inscriptions** : sessions OPEN/CLOSED, capacité et places restantes, inscription, doublons refusés, annulation admin (libère une place), liste des inscrits, « Mes inscriptions ».

**MLA** : profil apprenant, dataset synthétique, comparaison de 4 classifieurs, meilleur modèle selon le F1, FastAPI `/predict`, Spring, page Angular.

**DevOps** : `Jenkinsfile`, tests, Dockerfiles, `docker-compose.yml`.

## 4. Architecture

Voir `docs/architecture.md` (diagrammes Mermaid).

```
Navigateur → Angular (dev :4200 | Docker Nginx :8088)
                → Spring Boot :8080 (JWT)
                    → MySQL
                    → FastAPI ML :8000
Jenkins :9090 (CI)
```

## 5. Technologies

| Couche | Stack |
|---|---|
| Frontend | Angular 18, standalone components |
| Backend | Java 17, Spring Boot 3.4, Spring Security, JPA |
| Base | MySQL 8 / MariaDB (XAMPP) |
| ML | Python 3.12+, scikit-learn, FastAPI |
| CI | Jenkins (pipeline déclarative) |
| Run | Docker Compose |

## 6. Structure

```
backend/     Spring Boot (JWT, 2 modules)
frontend/     Angular
ml/           FastAPI + scikit-learn
jenkins/      CI (hors compose applicatif)
Jenkinsfile
docker-compose.yml
docs/
```

## 7. Installation

- JDK 17 (`JAVA_HOME`)
- Node 18.19+ / 20 (Docker frontend utilise Node 20)
- MySQL 8 ou XAMPP MariaDB **3306**
- Python 3.12+ (venv recommandé)
- Docker Desktop (stack complète / Jenkins)

```bat
cd /d "C:\Users\PC\Desktop\Nouveau dossier (7)"
copy .env.example .env
```

## 8. Configuration

Variables : `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `JWT_SECRET` (≥ 32 caractères), `ML_BASE_URL`.

Local XAMPP : mot de passe root souvent **vide**, `MYSQL_HOST=localhost`.  
Compose : `MYSQL_HOST=mysql`, mot de passe `MYSQL_ROOT_PASSWORD`.

Ne jamais committer `.env`.

## 9. Lancement local

**MySQL** : base `formation_platform` (créée si l’URL JDBC le permet).

**Backend**

```bat
cd backend
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvnw.cmd spring-boot:run
```

Santé : http://localhost:8080/api/health

**Frontend**

```bat
cd frontend
npm install
npm start
```

http://localhost:4200 (proxy `/api` → 8080).

**ML** : section 11.

## 10. Lancement avec Docker

```bat
cd /d "C:\Users\PC\Desktop\Nouveau dossier (7)"
copy .env.example .env
docker compose up -d --build
```

- UI : http://localhost:8088  
- API : http://localhost:8080/api/health (si 8080 pris : `BACKEND_HOST_PORT=8082` dans `.env`)  
- ML : http://localhost:8000/health  
- MySQL hôte : **3307** → conteneur 3306  

Détail : `docs/docker.md`, `docs/deployment.md`.

## 11. Lancement du ML

```bat
cd ml
python -m venv .venv
.venv\Scripts\python.exe -m pip install -r requirements.txt
.venv\Scripts\python.exe data\generate_dataset.py
.venv\Scripts\python.exe train.py
.venv\Scripts\python.exe -m uvicorn serve:app --host 127.0.0.1 --port 8000
```

Puis Spring avec `ML_BASE_URL=http://localhost:8000`.

## 12. Jenkins

```bat
cd jenkins
docker compose up --build -d
```

http://localhost:9090 — voir `docs/devops.md`. SonarQube **non branché** par défaut (`SKIP_SONAR=true`).

## 13. Tests

```bat
cd backend
mvnw.cmd -B test

cd frontend
npm ci
npm run build
npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox

cd ml
.venv\Scripts\python.exe -m pytest -q
```

Pipeline locale : `powershell -File scripts\run-ci.ps1`

## 14. Comptes de démonstration

| Rôle | Email | Mot de passe |
|---|---|---|
| ADMIN | `admin@formalearn.tn` | `Admin123!` |
| APPRENANT | `apprenant@formalearn.tn` | `Learner123!` |
| APPRENANT | `apprenant2@formalearn.tn` | `Learner123!` |

Données seed : 4 catégories, 12 formations (dont DRAFT), chapitres, 6 sessions, inscriptions de démo (Spring + Python).

## 15. API principales

Voir `docs/api.md`.

| Méthode | Chemin | Accès |
|---|---|---|
| POST | `/api/auth/login` | public |
| GET | `/api/health` | public |
| GET/POST/PUT/DELETE | `/api/categories`, `/api/formations`, chapitres | GET : ADMIN+APPRENANT ; mutations : ADMIN |
| GET/POST/PUT/DELETE | `/api/sessions` | idem |
| POST | `/api/inscriptions` | APPRENANT |
| GET | `/api/inscriptions/moi` | APPRENANT |
| GET | `/api/sessions/{id}/inscriptions` | ADMIN |
| PATCH | `/api/inscriptions/{id}/status` | ADMIN |
| GET/PUT | `/api/profil` | APPRENANT |
| GET | `/api/mla/recommandations` | APPRENANT |
| GET | `http://localhost:8000/health` | ML |
| POST | `http://localhost:8000/predict` | ML |
