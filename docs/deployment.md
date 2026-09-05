# Déploiement local

Pas de cloud ni de Kubernetes. Deux modes.

## 1. Développement (XAMPP + process hôtes)

1. MariaDB `localhost:3306`, utilisateur `root`, mot de passe vide par défaut.
2. `ml` : venv + `train.py` + `uvicorn` `:8000`.
3. `backend` : `mvnw.cmd spring-boot:run` (`MYSQL_HOST=localhost`, `ML_BASE_URL=http://localhost:8000`).
4. `frontend` : `npm start` `:4200`.

## 2. Docker Compose

Fichier racine `docker-compose.yml`.

| Service | Rôle | Port hôte |
|---|---|---|
| mysql | MySQL 8, volume `mysql_data` | 3307 |
| ml | FastAPI + modèle | 8000 |
| backend | Spring | 8080 ou `BACKEND_HOST_PORT` |
| frontend | Nginx + Angular, proxy `/api` → `backend:8080` | 8088 |

Variables : copier `.env.example` → `.env`.

```bat
docker compose up -d --build
docker compose ps
docker compose logs -f backend
docker compose down
```

Le backend Compose utilise `MYSQL_HOST=mysql` et `ML_BASE_URL=http://ml:8000`.

Healthchecks : MySQL `mysqladmin ping`, ML `GET /health`, backend `GET /api/health`, frontend Nginx `/`.

Jenkins : `jenkins/docker-compose.yml`, port **9090**. Voir `docs/devops.md`.
