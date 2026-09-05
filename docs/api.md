# API FormaLearn

Base Spring : `http://localhost:8080/api`  
En-tête : `Authorization: Bearer <jwt>` sauf login et health.

Erreurs JSON : `status`, `message` (400, 401, 403, 404, 409).

## Authentification

`POST /auth/login` `{ "email", "password" }` → `{ token, tokenType, expiresIn, user }`

`GET /auth/me` — utilisateur courant.

`GET /admin/ping` — ADMIN. `GET /learner/ping` — APPRENANT.

## Catalogue

`GET /categories` — ADMIN et APPRENANT.  
`POST /categories`, `PUT /categories/{id}`, `DELETE /categories/{id}` — ADMIN.

`GET /formations?q=&categoryId=&minPrice=&maxPrice=` — apprenant : **PUBLISHED** uniquement.  
`GET /formations/{id}`  
`POST|PUT|DELETE /formations` — ADMIN.

`GET /formations/{id}/chapitres`  
`POST|PUT|DELETE .../chapitres` — ADMIN.

## Sessions

`GET /sessions`, `GET /sessions/{id}`, `GET /formations/{id}/sessions`  
`POST|PUT|DELETE /sessions` — ADMIN.

Champs : `formationId`, `startDate`, `endDate`, `capacity`, `status` (`OPEN`|`CLOSED`).  
Réponse : `enrolledCount`, `remainingPlaces`.  
Suppression interdite s’il existe des inscriptions (409). Dates : fin ≥ début (400).

## Inscriptions

`POST /inscriptions` `{ "sessionId" }` — APPRENANT, 201 `PENDING`.  
Doublon, session CLOSED, plus de place, formation DRAFT → **409**.  
Non authentifié → **401**.

`GET /inscriptions/moi` — APPRENANT.

`GET /sessions/{id}/inscriptions` — ADMIN.

`PATCH /inscriptions/{id}/status` `{ "status": "CONFIRMED"|"CANCELLED" }` — ADMIN.  
Annulation : libère une place.

## Profil et MLA

`GET|PUT /profil` — APPRENANT (`interest`, `experienceYears`, `educationLevel`, `skillTags`).

`GET /mla/recommandations` — APPRENANT. Spring n’envoie au ML que les formations **PUBLISHED**. Indisponibilité ML → 503.

## Santé

`GET /health` — vérifie MySQL (`SELECT 1`).

## FastAPI

`GET http://localhost:8000/health`  
`POST http://localhost:8000/predict`

```json
{
  "profile": {
    "interest": "BACKEND",
    "experience_years": 2,
    "education_level": "INGENIEUR",
    "has_java": 1,
    "has_spring": 1,
    "has_sql": 1,
    "has_python": 0,
    "has_management": 0
  },
  "formations": [
    { "formationId": 1, "formation_category": "INFORMATIQUE", "price": 390, "duration_hours": 24 }
  ]
}
```

Réponse : `recommendations` triées par score décroissant.
