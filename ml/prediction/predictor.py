"""Runtime prediction service loading persisted Joblib Random Forest artifacts."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import joblib
import pandas as pd

FEATURE_COLUMNS = ["N", "P", "K", "temperature", "humidity", "ph", "rainfall"]


class ModelUnavailableError(RuntimeError):
    """Raised when the trained crop recommendation artifact is unavailable."""


class CropRecommendationPredictor:
    """Thread-safe loader and predictor for FarmWise Random Forest artifacts."""

    def __init__(self, models_dir: Path) -> None:
        self.models_dir = Path(models_dir)
        self._bundle: dict[str, Any] | None = None
        self._manifest: dict[str, Any] | None = None

    def load(self, force_reload: bool = False) -> dict[str, Any]:
        """Load active model manifest and joblib artifact from disk."""
        if self._bundle is not None and not force_reload:
            return self._bundle

        manifest_path = self.models_dir / "model_manifest.json"
        if not manifest_path.exists():
            raise ModelUnavailableError(
                "Trained ML model manifest is not available. Run the ML training pipeline first."
            )

        try:
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            raise ModelUnavailableError("ML model manifest is corrupted.") from exc

        artifact_name = manifest.get("artifact_filename")
        artifact_path = self.models_dir / artifact_name if artifact_name else Path(manifest.get("artifact_path", ""))
        if not artifact_path.exists():
            raise ModelUnavailableError(f"ML model artifact missing at {artifact_path}.")

        try:
            bundle = joblib.load(artifact_path)
        except Exception as exc:
            raise ModelUnavailableError("Failed to deserialize ML model artifact.") from exc

        if "model" not in bundle:
            raise ModelUnavailableError("ML artifact bundle is missing the trained estimator.")

        self._manifest = manifest
        self._bundle = bundle
        return bundle

    def get_manifest(self) -> dict[str, Any]:
        """Return active model manifest metadata."""
        if self._manifest is None:
            self.load()
        assert self._manifest is not None
        return self._manifest

    def predict(
        self,
        *,
        nitrogen: float,
        phosphorus: float,
        potassium: float,
        temperature: float,
        humidity: float,
        ph: float,
        rainfall: float,
        top_k: int = 3,
    ) -> dict[str, Any]:
        """Predict top-k crops with honest probabilities and soil-climate explanations."""
        bundle = self.load()
        manifest = self.get_manifest()
        model = bundle["model"]

        sample = pd.DataFrame(
            [[nitrogen, phosphorus, potassium, temperature, humidity, ph, rainfall]],
            columns=FEATURE_COLUMNS,
        )
        probabilities = model.predict_proba(sample)[0]
        classes = list(bundle["classes"])

        ranked = sorted(
            zip(classes, probabilities, strict=True),
            key=lambda item: float(item[1]),
            reverse=True,
        )
        top_predictions = [
            {"crop": str(crop), "confidence": round(float(prob), 4)}
            for crop, prob in ranked[: max(1, min(top_k, len(ranked)))]
        ]
        best_crop, best_prob = ranked[0]

        crop_profile = bundle.get("crop_profiles", {}).get(str(best_crop), {})
        importances = bundle.get("feature_importances", {})
        input_map = {
            "N": nitrogen,
            "P": phosphorus,
            "K": potassium,
            "temperature": temperature,
            "humidity": humidity,
            "ph": ph,
            "rainfall": rainfall,
        }

        factor_notes = []
        for feature in FEATURE_COLUMNS:
            val = input_map[feature]
            prof = crop_profile.get(feature)
            status = "within_typical_range"
            mean_val = None
            if prof:
                mean_val = prof["mean"]
                if val < prof["min"]:
                    status = "below_observed_range"
                elif val > prof["max"]:
                    status = "above_observed_range"
            factor_notes.append(
                {
                    "feature": feature,
                    "input_value": round(float(val), 2),
                    "crop_typical_mean": mean_val,
                    "model_importance": importances.get(feature, 0.0),
                    "alignment": status,
                }
            )

        factor_notes.sort(key=lambda item: item["model_importance"], reverse=True)

        return {
            "recommended_crop": str(best_crop),
            "confidence_score": round(float(best_prob), 4),
            "top_predictions": top_predictions,
            "model_version": bundle.get("version", manifest.get("version")),
            "algorithm": bundle.get("algorithm", "RandomForestClassifier"),
            "explanation": {
                "summary": (
                    f"RandomForest model ({bundle.get('version')}) selected '{best_crop}' "
                    f"with {best_prob * 100:.1f}% class probability based on soil NPK, pH, and climate inputs."
                ),
                "feature_analysis": factor_notes,
            },
            "advisory_note": (
                "Generated by FarmWise's trained Random Forest model on historical soil/climate data. "
                "Use as decision support alongside local soil lab testing and certified agronomic guidance."
            ),
        }
