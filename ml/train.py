"""Entraîne et compare des classifieurs supervisés, puis sauvegarde le meilleur pipeline."""

from __future__ import annotations

import json
from pathlib import Path

import joblib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
    roc_auc_score,
)
from sklearn.model_selection import train_test_split
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.tree import DecisionTreeClassifier

from features import BINARY_FEATURES, CATEGORICAL_FEATURES, FEATURE_COLUMNS, NUMERIC_FEATURES, TARGET

ROOT = Path(__file__).resolve().parent
DATA_PATH = ROOT / "data" / "recommendations.csv"
METRICS_PATH = ROOT / "metrics.json"
REPORTS = ROOT / "reports"
MODELS = ROOT / "models"
MODEL_PATH = MODELS / "recommender_model.pkl"
SELECTED_PATH = MODELS / "selected_model.txt"
REPORT_MD = ROOT / "REPORT.md"


def build_preprocessor() -> ColumnTransformer:
    return ColumnTransformer(
        transformers=[
            ("num", StandardScaler(), NUMERIC_FEATURES),
            ("cat", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL_FEATURES),
            ("bin", "passthrough", BINARY_FEATURES),
        ]
    )


def models() -> dict:
    return {
        "Logistic Regression": LogisticRegression(max_iter=1000, random_state=42),
        "Decision Tree": DecisionTreeClassifier(max_depth=8, min_samples_leaf=8, random_state=42),
        "KNN": KNeighborsClassifier(n_neighbors=7),
        "Random Forest": RandomForestClassifier(
            n_estimators=200,
            max_depth=12,
            min_samples_leaf=4,
            random_state=42,
            n_jobs=-1,
        ),
    }


def evaluate(name: str, pipeline: Pipeline, x_test, y_test) -> dict:
    y_pred = pipeline.predict(x_test)
    metrics = {
        "accuracy": round(float(accuracy_score(y_test, y_pred)), 4),
        "precision": round(float(precision_score(y_test, y_pred, pos_label=1, zero_division=0)), 4),
        "recall": round(float(recall_score(y_test, y_pred, pos_label=1, zero_division=0)), 4),
        "f1": round(float(f1_score(y_test, y_pred, pos_label=1, zero_division=0)), 4),
    }
    if hasattr(pipeline, "predict_proba"):
        proba = pipeline.predict_proba(x_test)[:, 1]
        metrics["roc_auc"] = round(float(roc_auc_score(y_test, proba)), 4)
    print(f"\n=== {name} ===")
    print(json.dumps(metrics, indent=2))
    print(classification_report(y_test, y_pred, digits=4, zero_division=0))
    return metrics


def write_report(all_metrics: dict, winner: str, n_train: int, n_test: int, matrix: np.ndarray) -> None:
    lines = [
        "# Rapport MLA — Recommandation de formations",
        "",
        "## 1. Objectif du modèle",
        "Prédire si une formation PUBLISHED est pertinente pour un profil d'apprenant (classification binaire supervisée), puis classer les formations par P(relevant=1).",
        "",
        "## 2. Dataset",
        f"- Fichier : `ml/data/recommendations.csv`",
        "- 800 lignes synthétiques, reproductibles (`random_state=42`).",
        "- 50 % des exemples sont alignés métier (classe positive), 50 % aléatoires.",
        "- Généré par `ml/data/generate_dataset.py` (aucune donnée personnelle réelle).",
        "- Les labels suivent des règles métier documentées dans ce script, avec 6 % de bruit.",
        "",
        "## 3. Features",
        "- Numériques (StandardScaler) : experience_years, price, duration_hours.",
        "- Catégorielles (OneHotEncoder) : interest, education_level, formation_category.",
        "- Binaires (passthrough) : has_java, has_spring, has_sql, has_python, has_management.",
        "- Aucun identifiant utilisateur.",
        "",
        "## 4. Label",
        "`label = 1` si la formation est jugée pertinente pour le profil, sinon `0`.",
        "La pertinence repose surtout sur l'alignement intérêt / catégorie / compétences, puis l'adéquation expérience / charge.",
        "",
        "## 5. Préparation",
        "Pipeline scikit-learn `ColumnTransformer` + classifieur. Le preprocessing est ajusté uniquement sur le train (pas de fuite).",
        "",
        "## 6. Train / test",
        f"- Split 80/20 stratifié, `random_state=42`.",
        f"- Train : {n_train} lignes. Test : {n_test} lignes.",
        "",
        "## 7. Modèles comparés",
        "Logistic Regression, Decision Tree, KNN, Random Forest.",
        "",
        "## 8. Métriques (classe pertinente = 1)",
        "",
        "| Modèle | Accuracy | Precision | Recall | F1 | ROC-AUC |",
        "|---|---|---|---|---|---|",
    ]
    for name, metrics in all_metrics.items():
        lines.append(
            f"| {name} | {metrics['accuracy']} | {metrics['precision']} | {metrics['recall']} | {metrics['f1']} | {metrics.get('roc_auc', '-')} |"
        )
    lines.extend(
        [
            "",
            f"Matrice de confusion du modèle retenu (`{winner}`) : TN={matrix[0][0]}, FP={matrix[0][1]}, FN={matrix[1][0]}, TP={matrix[1][1]}.",
            "",
            "## 9. Modèle retenu",
            f"**{winner}**, choisi pour le meilleur F1 Score sur la classe pertinente (label=1) sur le jeu de test. En cas d'égalité, ROC-AUC puis accuracy départagent.",
            "",
            "## 10. Limitations",
            "- Dataset synthétique : les règles métier sont simplifiées par rapport à un historique réel d'inscriptions.",
            "- Pas de texte (volontairement pas de TF-IDF) : le titre de la formation n'est pas une feature.",
            "- Catégorie inconnue à l'inférence : OneHotEncoder `handle_unknown=ignore`.",
            "- Un apprenant sans compétences alignées recevra des scores plus bas, ce qui est attendu.",
            "",
        ]
    )
    REPORT_MD.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    if not DATA_PATH.exists():
        raise SystemExit(f"Dataset introuvable : {DATA_PATH}. Exécutez generate_dataset.py")
    frame = pd.read_csv(DATA_PATH)
    missing = [col for col in FEATURE_COLUMNS + [TARGET] if col not in frame.columns]
    if missing:
        raise SystemExit(f"Colonnes manquantes : {missing}")
    x = frame[FEATURE_COLUMNS]
    y = frame[TARGET]
    x_train, x_test, y_train, y_test = train_test_split(
        x, y, test_size=0.2, random_state=42, stratify=y
    )
    all_metrics = {}
    fitted = {}
    for name, estimator in models().items():
        pipeline = Pipeline(
            steps=[
                ("preprocess", build_preprocessor()),
                ("model", estimator),
            ]
        )
        pipeline.fit(x_train, y_train)
        all_metrics[name] = evaluate(name, pipeline, x_test, y_test)
        fitted[name] = pipeline

    winner = max(
        all_metrics.items(),
        key=lambda item: (item[1]["f1"], item[1].get("roc_auc", 0.0), item[1]["accuracy"]),
    )[0]
    best = fitted[winner]
    y_pred = best.predict(x_test)
    matrix = confusion_matrix(y_test, y_pred, labels=[0, 1])

    REPORTS.mkdir(parents=True, exist_ok=True)
    MODELS.mkdir(parents=True, exist_ok=True)
    pd.DataFrame(all_metrics).T.to_csv(REPORTS / "model_comparison.csv")
    pd.DataFrame(matrix, index=["actual_0", "actual_1"], columns=["pred_0", "pred_1"]).to_csv(
        REPORTS / "confusion_matrix.csv"
    )
    fig, ax = plt.subplots(figsize=(5, 4))
    im = ax.imshow(matrix, cmap="Blues")
    ax.set_xticks([0, 1], labels=["Pred 0", "Pred 1"])
    ax.set_yticks([0, 1], labels=["True 0", "True 1"])
    ax.set_title(f"Confusion matrix — {winner}")
    for (i, j), value in np.ndenumerate(matrix):
        ax.text(j, i, int(value), ha="center", va="center")
    fig.colorbar(im, ax=ax, fraction=0.046)
    fig.tight_layout()
    fig.savefig(REPORTS / "confusion_matrix.png", dpi=140)
    plt.close(fig)

    payload = {"selected_model": winner, "models": all_metrics}
    METRICS_PATH.write_text(json.dumps(payload, indent=2), encoding="utf-8")
    joblib.dump({"pipeline": best, "model_name": winner, "feature_columns": FEATURE_COLUMNS}, MODEL_PATH)
    SELECTED_PATH.write_text(winner, encoding="utf-8")
    write_report(all_metrics, winner, len(x_train), len(x_test), matrix)
    print(f"\nSelected model: {winner}")
    print(f"Saved {MODEL_PATH}")


if __name__ == "__main__":
    main()
