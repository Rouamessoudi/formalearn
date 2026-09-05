from pathlib import Path

import joblib
import pandas as pd
from fastapi.testclient import TestClient

from data.generate_dataset import generate
from features import FEATURE_COLUMNS, TARGET
from serve import MODEL_PATH, app, load_bundle

ROOT = Path(__file__).resolve().parents[1]
CSV_PATH = ROOT / "data" / "recommendations.csv"


def test_dataset_is_valid():
    assert CSV_PATH.exists()
    frame = pd.read_csv(CSV_PATH)
    assert 500 <= len(frame) <= 1000
    for column in FEATURE_COLUMNS + [TARGET]:
        assert column in frame.columns
    assert set(frame[TARGET].unique()).issubset({0, 1})
    assert frame[TARGET].nunique() == 2


def test_synthetic_generator_is_reproducible():
    first = generate()
    second = generate()
    pd.testing.assert_frame_equal(first, second)


def test_model_can_be_loaded_and_scores():
    assert MODEL_PATH.exists()
    bundle = joblib.load(MODEL_PATH)
    pipeline = bundle["pipeline"]
    sample = pd.DataFrame(
        [
            {
                "experience_years": 2,
                "price": 390,
                "duration_hours": 24,
                "interest": "BACKEND",
                "education_level": "INGENIEUR",
                "formation_category": "INFORMATIQUE",
                "has_java": 1,
                "has_spring": 1,
                "has_sql": 1,
                "has_python": 0,
                "has_management": 0,
            }
        ]
    )[FEATURE_COLUMNS]
    proba = pipeline.predict_proba(sample)[0, 1]
    assert 0.0 <= float(proba) <= 1.0


def test_health():
    client = TestClient(app)
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"


def test_predict_returns_ranked_recommendations():
    load_bundle()
    client = TestClient(app)
    response = client.post(
        "/predict",
        json={
            "profile": {
                "interest": "BACKEND",
                "experience_years": 2,
                "education_level": "INGENIEUR",
                "has_java": 1,
                "has_spring": 1,
                "has_sql": 1,
                "has_python": 0,
                "has_management": 0,
            },
            "formations": [
                {"formationId": 1, "formation_category": "INFORMATIQUE", "price": 390, "duration_hours": 24},
                {"formationId": 2, "formation_category": "LANGUES", "price": 180, "duration_hours": 10},
            ],
        },
    )
    assert response.status_code == 200
    recs = response.json()["recommendations"]
    ids = [item["formationId"] for item in recs]
    assert ids == [item["formationId"] for item in sorted(recs, key=lambda x: x["score"], reverse=True)]
    assert recs[0]["formationId"] == 1
    assert recs[0]["score"] >= recs[1]["score"]
