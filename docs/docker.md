# Docker — FormaLearn

## Stack (`docker compose up --build`)

| Service | Port hôte | Rôle |
|---|---|---|
| mysql | 3307 → 3306 | Base `formation_platform` |
| ml | 8000 | FastAPI + modèle joblib |
| backend | 8080 (ou `BACKEND_HOST_PORT`) | Spring Boot + JWT |
| frontend | 8088 → 80 | Nginx + Angular, `/api` → `backend:8080` |

```bat
cd /d "C:\Users\PC\Desktop\Nouveau dossier (7)"
copy .env.example .env
docker compose up --build -d
```

UI : http://localhost:8088  
API : http://localhost:8080/api/health (ou le port `BACKEND_HOST_PORT`)  
ML : http://localhost:8000/health

Arrêt : `docker compose down`

## Images CI

```bat
docker build -t formalearn-backend:ci backend
docker build -t formalearn-frontend:ci frontend
docker build -t formalearn-ml:ci ml
```
