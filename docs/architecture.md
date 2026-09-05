# Architecture FormaLearn

## Architecture principale (validation)

```mermaid
flowchart LR
  U[Navigateur] --> FE[Angular]
  FE -->|JWT Bearer /api| BE[Spring Boot]
  BE --> DB[(MySQL)]
  BE -->|HTTP /predict| ML[FastAPI]
  ML --> PKL[recommender_model.pkl]
```

```
Angular → Spring Boot → MySQL
Spring Boot → FastAPI (scikit-learn) → recommandations
GitHub → Jenkins → build / tests → SUCCESS ou FAILURE
```

Authentification : **Spring Security + JWT** (rôles `ADMIN` / `APPRENANT`).

Entre conteneurs Docker : `mysql`, `backend`, `ml`, `frontend`. Le backend n’utilise **jamais** `localhost` pour JDBC ou le ML.

## Phrase pour le professeur

« Notre application utilise Angular pour le frontend et Spring Boot pour le backend. Spring Security avec JWT gère l’authentification et les rôles. MySQL stocke les données des deux modules. La partie MLA utilise un service Python avec scikit-learn pour recommander des formations. Jenkins automatise le build et les tests dans une pipeline CI. »

## Cas d'utilisation

```mermaid
flowchart TB
  subgraph Admin
    A1[CRUD catégories]
    A2[CRUD formations et chapitres]
    A3[CRUD sessions]
    A4[Voir inscrits / confirmer / annuler]
  end
  subgraph Apprenant
    L1[Login JWT]
    L2[Catalogue recherche filtres]
    L3[Détail + sessions]
    L4[Inscription]
    L5[Mes inscriptions]
    L6[Profil]
    L7[Recommandations MLA]
  end
  Admin --> A1
  Apprenant --> L1
```

## Modèle de données (simplifié)

```mermaid
erDiagram
  ROLE ||--o{ USER : has
  USER ||--o| PROFILE : has
  CATEGORY ||--o{ FORMATION : contains
  FORMATION ||--o{ CHAPTER : contains
  FORMATION ||--o{ TRAINING_SESSION : scheduled
  USER ||--o{ ENROLLMENT : makes
  TRAINING_SESSION ||--o{ ENROLLMENT : receives
```

## Architecture MLA

```mermaid
flowchart TB
  P[Profil apprenant] --> S[Spring RecommendationService]
  C[Formations PUBLISHED] --> S
  S --> F[FastAPI /predict]
  F --> M[Pipeline sklearn]
  M --> R[Scores]
  R --> UI[Page Angular recommandations]
```

## DevOps

```mermaid
flowchart TD
  G[GitHub] --> J[Jenkins]
  J --> B[mvn compile / test]
  J --> N[npm ci / build / test]
  J --> P[pytest + train.py]
  J --> D[docker build backend frontend ml]
```

Jenkins : `jenkins/docker-compose.yml` (port **9090**).  
Stack applicative : `docker-compose.yml` à la racine.
