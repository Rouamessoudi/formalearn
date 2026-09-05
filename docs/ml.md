# MLA — Recommandation de formations

Document de soutenance. Les **métriques chiffrées** ci-dessous doivent rester alignées avec `ml/REPORT.md` et `ml/metrics.json` après le dernier `train.py`.

## Problème

Classification **binaire supervisée** : pour un couple (profil apprenant, formation publiée), prédire si la formation est **pertinente** (`label=1`). À l’inférence, on classe les formations par `P(label=1)`.

Ce n’est pas de la similarité de texte : **pas de TF-IDF**, pas d’embeddings.

## Dataset

- Fichier : `ml/data/recommendations.csv`
- Générateur : `ml/data/generate_dataset.py` (`random_state=42`)
- ~800 lignes synthétiques, ~50 % positives, ~6 % de bruit métier
- Aucune donnée personnelle réelle

## Features

| Type | Colonnes | Preprocessing |
|---|---|---|
| Numérique | `experience_years`, `price`, `duration_hours` | StandardScaler |
| Catégorielle | `interest`, `education_level`, `formation_category` | OneHotEncoder (`handle_unknown=ignore`) |
| Binaire | `has_java`, `has_spring`, `has_sql`, `has_python`, `has_management` | passthrough |

Pas d’identifiant utilisateur. Le titre de formation n’est pas une feature.

## Label

`label ∈ {0,1}` selon des règles métier (alignement intérêt / catégorie / compétences, charge vs expérience).

## Preprocessing

`sklearn.pipeline.Pipeline` : `ColumnTransformer` ajusté **uniquement sur le train** (split 80/20 stratifié, `random_state=42`).

## Modèles testés

1. Logistic Regression  
2. Decision Tree  
3. KNN  
4. Random Forest  

## Métriques réelles (dernier `train.py`, test 160 lignes)

| Modèle | Accuracy | Precision | Recall | F1 | ROC-AUC |
|---|---|---|---|---|---|
| Logistic Regression | 0.7688 | 0.7573 | 0.8667 | 0.8083 | 0.837 |
| Decision Tree | 0.775 | 0.8068 | 0.7889 | 0.7978 | 0.8464 |
| KNN | 0.7937 | 0.7615 | 0.9222 | 0.8342 | 0.8617 |
| Random Forest | 0.8313 | 0.8621 | 0.8333 | 0.8475 | 0.8692 |

**Modèle retenu : Random Forest** (meilleur F1 sur la classe pertinente). Matrice : TN=58, FP=12, FN=15, TP=75. Source : `ml/REPORT.md`, `ml/metrics.json`.

## Inférence

Exemple : profil BACKEND + Java/Spring/SQL vs formation INFORMATIQUE vs LANGUES — le score INFORMATIQUE doit être plus élevé (`ml/tests/test_ml.py`).

## Intégrations

- **FastAPI** : `ml/serve.py` — `GET /health`, `POST /predict`
- **Spring** : `FastApiMlClient` / `RecommendationService` — `GET /api/mla/recommandations`
- **Angular** : `/app/recommandations` + `/app/profil`

## Scripts

```bat
cd ml
python data\generate_dataset.py
python train.py
python -m pytest -q
python -m uvicorn serve:app --port 8000
```
