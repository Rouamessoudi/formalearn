# Rapport MLA — Recommandation de formations

## 1. Objectif du modèle
Prédire si une formation PUBLISHED est pertinente pour un profil d'apprenant (classification binaire supervisée), puis classer les formations par P(relevant=1).

## 2. Dataset
- Fichier : `ml/data/recommendations.csv`
- 800 lignes synthétiques, reproductibles (`random_state=42`).
- 50 % des exemples sont alignés métier (classe positive), 50 % aléatoires.
- Généré par `ml/data/generate_dataset.py` (aucune donnée personnelle réelle).
- Les labels suivent des règles métier documentées dans ce script, avec 6 % de bruit.

## 3. Features
- Numériques (StandardScaler) : experience_years, price, duration_hours.
- Catégorielles (OneHotEncoder) : interest, education_level, formation_category.
- Binaires (passthrough) : has_java, has_spring, has_sql, has_python, has_management.
- Aucun identifiant utilisateur.

## 4. Label
`label = 1` si la formation est jugée pertinente pour le profil, sinon `0`.
La pertinence repose surtout sur l'alignement intérêt / catégorie / compétences, puis l'adéquation expérience / charge.

## 5. Préparation
Pipeline scikit-learn `ColumnTransformer` + classifieur. Le preprocessing est ajusté uniquement sur le train (pas de fuite).

## 6. Train / test
- Split 80/20 stratifié, `random_state=42`.
- Train : 640 lignes. Test : 160 lignes.

## 7. Modèles comparés
Logistic Regression, Decision Tree, KNN, Random Forest.

## 8. Métriques (classe pertinente = 1)

| Modèle | Accuracy | Precision | Recall | F1 | ROC-AUC |
|---|---|---|---|---|---|
| Logistic Regression | 0.7688 | 0.7573 | 0.8667 | 0.8083 | 0.837 |
| Decision Tree | 0.775 | 0.8068 | 0.7889 | 0.7978 | 0.8464 |
| KNN | 0.7937 | 0.7615 | 0.9222 | 0.8342 | 0.8617 |
| Random Forest | 0.8313 | 0.8621 | 0.8333 | 0.8475 | 0.8692 |

Matrice de confusion du modèle retenu (`Random Forest`) : TN=58, FP=12, FN=15, TP=75.

## 9. Modèle retenu
**Random Forest**, choisi pour le meilleur F1 Score sur la classe pertinente (label=1) sur le jeu de test. En cas d'égalité, ROC-AUC puis accuracy départagent.

## 10. Limitations
- Dataset synthétique : les règles métier sont simplifiées par rapport à un historique réel d'inscriptions.
- Pas de texte (volontairement pas de TF-IDF) : le titre de la formation n'est pas une feature.
- Catégorie inconnue à l'inférence : OneHotEncoder `handle_unknown=ignore`.
- Un apprenant sans compétences alignées recevra des scores plus bas, ce qui est attendu.
