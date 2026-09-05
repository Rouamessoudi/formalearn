from __future__ import annotations

from contextlib import asynccontextmanager
from pathlib import Path

import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from features import FEATURE_COLUMNS

ROOT = Path(__file__).resolve().parent
MODEL_PATH = ROOT / "models" / "recommender_model.pkl"

_bundle = None


def load_bundle():
    global _bundle
    if _bundle is None:
        if not MODEL_PATH.exists():
            raise RuntimeError(f"Modèle introuvable : {MODEL_PATH}. Lancez train.py")
        _bundle = joblib.load(MODEL_PATH)
    return _bundle


@asynccontextmanager
async def lifespan(_app: FastAPI):
    load_bundle()
    yield


app = FastAPI(title="FormaLearn MLA", version="1.0.0", lifespan=lifespan)


class ProfilePayload(BaseModel):
    interest: str
    experience_years: int = Field(ge=0)
    education_level: str
    has_java: int = Field(ge=0, le=1)
    has_spring: int = Field(ge=0, le=1)
    has_sql: int = Field(ge=0, le=1)
    has_python: int = Field(ge=0, le=1)
    has_management: int = Field(ge=0, le=1)


class FormationPayload(BaseModel):
    formationId: int
    formation_category: str
    price: float
    duration_hours: int


class PredictRequest(BaseModel):
    profile: ProfilePayload
    formations: list[FormationPayload]


@app.get("/health")
def health():
    return {"status": "UP"}


@app.post("/predict")
def predict(body: PredictRequest):
    if not body.formations:
        return {"recommendations": []}
    try:
        bundle = load_bundle()
    except Exception as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    pipeline = bundle["pipeline"]
    rows = []
    ids = []
    for formation in body.formations:
        ids.append(formation.formationId)
        rows.append(
            {
                "experience_years": body.profile.experience_years,
                "price": formation.price,
                "duration_hours": formation.duration_hours,
                "interest": body.profile.interest,
                "education_level": body.profile.education_level,
                "formation_category": formation.formation_category,
                "has_java": body.profile.has_java,
                "has_spring": body.profile.has_spring,
                "has_sql": body.profile.has_sql,
                "has_python": body.profile.has_python,
                "has_management": body.profile.has_management,
            }
        )
    frame = pd.DataFrame(rows)[FEATURE_COLUMNS]
    scores = pipeline.predict_proba(frame)[:, 1]
    ranked = sorted(
        [{"formationId": fid, "score": round(float(score), 4)} for fid, score in zip(ids, scores)],
        key=lambda item: item["score"],
        reverse=True,
    )
    return {"recommendations": ranked}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("serve:app", host="0.0.0.0", port=8000, reload=False)
