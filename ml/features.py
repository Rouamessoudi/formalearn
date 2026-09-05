"""Colonnes et encodage partagés entre génération, entraînement et inférence."""

NUMERIC_FEATURES = ["experience_years", "price", "duration_hours"]
CATEGORICAL_FEATURES = ["interest", "education_level", "formation_category"]
BINARY_FEATURES = ["has_java", "has_spring", "has_sql", "has_python", "has_management"]
FEATURE_COLUMNS = NUMERIC_FEATURES + CATEGORICAL_FEATURES + BINARY_FEATURES
TARGET = "label"

INTERESTS = ["BACKEND", "DATA", "MANAGEMENT", "LANGUAGES", "OTHER"]
EDUCATION_LEVELS = ["LICENCE", "INGENIEUR", "MASTER"]
CATEGORIES = ["INFORMATIQUE", "DATA_SCIENCE", "BUSINESS", "LANGUES"]
