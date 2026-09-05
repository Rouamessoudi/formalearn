"""
Génère un jeu synthétique reproductible (random_state=42).

Règle de labellisation (pertinence formation / profil) :

1. Alignement métier (signal principal)
   - BACKEND + INFORMATIQUE + (JAVA ou SPRING) → pertinent
   - DATA + DATA_SCIENCE + (PYTHON ou SQL) → pertinent
   - MANAGEMENT + BUSINESS + MANAGEMENT → pertinent
   - LANGUAGES + LANGUES → pertinent
   - OTHER : pertinent seulement si le prix est bas (< 250) et la durée courte (< 16 h)

2. Ajustements d'expérience / charge
   - Si pertinent mais junior (experience_years <= 1) et formation longue (> 36 h) ou chère (> 620) :
     on retire la pertinence (trop avancé).
   - Si non pertinent mais senior (>= 8 ans) et formation INFORMATIQUE abordable (<= 400) :
     25 % de chance d'être pertinent (ouverture transversale).

3. Bruit contrôlé
   - 6 % des labels sont inversés pour éviter un problème trop déterministe.

Pour un jeu plus équilibré, 50 % des lignes sont volontairement alignées
(intérêt + catégorie + compétences), les 50 % restantes sont tirées aléatoirement.

Aucun identifiant utilisateur n'est utilisé.
"""

from __future__ import annotations

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

import numpy as np
import pandas as pd

from features import (
    BINARY_FEATURES,
    CATEGORIES,
    EDUCATION_LEVELS,
    FEATURE_COLUMNS,
    INTERESTS,
    TARGET,
)

RNG_SEED = 42
N_ROWS = 800
OUT_PATH = Path(__file__).resolve().parent / "recommendations.csv"


def relevance_score(row: pd.Series) -> int:
    interest = row["interest"]
    category = row["formation_category"]
    relevant = 0
    if interest == "BACKEND" and category == "INFORMATIQUE" and (row["has_java"] or row["has_spring"]):
        relevant = 1
    elif interest == "DATA" and category == "DATA_SCIENCE" and (row["has_python"] or row["has_sql"]):
        relevant = 1
    elif interest == "MANAGEMENT" and category == "BUSINESS" and row["has_management"]:
        relevant = 1
    elif interest == "LANGUAGES" and category == "LANGUES":
        relevant = 1
    elif interest == "OTHER" and row["price"] < 250 and row["duration_hours"] < 16:
        relevant = 1

    if relevant and row["experience_years"] <= 1 and (row["duration_hours"] > 36 or row["price"] > 620):
        relevant = 0
    return relevant


def random_row(rng: np.random.Generator) -> dict:
    return {
        "interest": rng.choice(INTERESTS),
        "experience_years": int(rng.integers(0, 16)),
        "education_level": rng.choice(EDUCATION_LEVELS),
        "has_java": int(rng.integers(0, 2)),
        "has_spring": int(rng.integers(0, 2)),
        "has_sql": int(rng.integers(0, 2)),
        "has_python": int(rng.integers(0, 2)),
        "has_management": int(rng.integers(0, 2)),
        "formation_category": rng.choice(CATEGORIES),
        "price": int(rng.integers(120, 901)),
        "duration_hours": int(rng.integers(8, 49)),
    }


def aligned_row(rng: np.random.Generator) -> dict:
    track = rng.choice(["BACKEND", "DATA", "MANAGEMENT", "LANGUAGES"])
    row = random_row(rng)
    row["experience_years"] = int(rng.integers(2, 12))
    row["price"] = int(rng.integers(180, 520))
    row["duration_hours"] = int(rng.integers(12, 32))
    if track == "BACKEND":
        row.update({"interest": "BACKEND", "formation_category": "INFORMATIQUE", "has_java": 1, "has_spring": 1})
    elif track == "DATA":
        row.update({"interest": "DATA", "formation_category": "DATA_SCIENCE", "has_python": 1, "has_sql": 1})
    elif track == "MANAGEMENT":
        row.update({"interest": "MANAGEMENT", "formation_category": "BUSINESS", "has_management": 1})
    else:
        row.update({"interest": "LANGUAGES", "formation_category": "LANGUES"})
    return row


def generate(n_rows: int = N_ROWS, seed: int = RNG_SEED) -> pd.DataFrame:
    rng = np.random.default_rng(seed)
    n_aligned = n_rows // 2
    rows = [aligned_row(rng) for _ in range(n_aligned)]
    rows.extend(random_row(rng) for _ in range(n_rows - n_aligned))
    rng.shuffle(rows)
    frame = pd.DataFrame(rows)
    labels = frame.apply(relevance_score, axis=1).to_numpy()
    senior_mask = (frame["experience_years"] >= 8) & (frame["formation_category"] == "INFORMATIQUE") & (
        frame["price"] <= 400
    ) & (labels == 0)
    flip_senior = senior_mask.to_numpy() & (rng.random(n_rows) < 0.25)
    labels = np.where(flip_senior, 1, labels)
    noise = rng.random(n_rows) < 0.06
    labels = np.where(noise, 1 - labels, labels)
    frame[TARGET] = labels.astype(int)
    return frame[FEATURE_COLUMNS + [TARGET]]


def main() -> None:
    dataset = generate()
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    dataset.to_csv(OUT_PATH, index=False)
    positives = int(dataset[TARGET].sum())
    print(f"Wrote {len(dataset)} rows to {OUT_PATH} (positives={positives}, negatives={len(dataset) - positives})")


if __name__ == "__main__":
    main()
