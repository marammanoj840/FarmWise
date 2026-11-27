"""Random Forest training, hyperparameter tuning, evaluation, and Joblib persistence."""

from __future__ import annotations

from datetime import datetime, timezone
import json
from pathlib import Path
from typing import Any

import joblib
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
)
from sklearn.model_selection import GridSearchCV, StratifiedKFold, train_test_split

from ml.evaluation.eda import FEATURE_COLUMNS, compute_sha256, load_and_validate_dataset


def train_and_evaluate(
    dataset_path: Path,
    models_dir: Path,
    eval_dir: Path,
    version: str | None = None,
) -> dict[str, Any]:
    """Validate dataset, tune RandomForestClassifier, evaluate honestly, and persist artifact."""
    models_dir.mkdir(parents=True, exist_ok=True)
    eval_dir.mkdir(parents=True, exist_ok=True)

    df = load_and_validate_dataset(dataset_path)
    dataset_sha = compute_sha256(dataset_path)

    X = df[FEATURE_COLUMNS]
    y = df["label"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, stratify=y, random_state=42
    )

    param_grid = {
        "n_estimators": [100, 200],
        "max_depth": [None, 16, 24],
        "min_samples_split": [2, 4],
        "min_samples_leaf": [1, 2],
    }

    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    base_rf = RandomForestClassifier(random_state=42, n_jobs=-1)
    search = GridSearchCV(
        estimator=base_rf,
        param_grid=param_grid,
        cv=cv,
        scoring="f1_macro",
        n_jobs=-1,
        refit=True,
    )
    search.fit(X_train, y_train)
    best_model: RandomForestClassifier = search.best_estimator_

    y_pred = best_model.predict(X_test)
    labels = sorted(y.unique().tolist())

    accuracy = float(accuracy_score(y_test, y_pred))
    precision_macro = float(precision_score(y_test, y_pred, average="macro", zero_division=0))
    recall_macro = float(recall_score(y_test, y_pred, average="macro", zero_division=0))
    f1_macro = float(f1_score(y_test, y_pred, average="macro", zero_division=0))
    cm = confusion_matrix(y_test, y_pred, labels=labels)
    report = classification_report(y_test, y_pred, output_dict=True, zero_division=0)

    feature_importances = {
        col: round(float(imp), 6)
        for col, imp in zip(FEATURE_COLUMNS, best_model.feature_importances_, strict=True)
    }

    crop_profiles = {
        str(crop): {
            col: {
                "mean": round(float(grp[col].mean()), 2),
                "std": round(float(grp[col].std() or 0.0), 2),
                "min": round(float(grp[col].min()), 2),
                "max": round(float(grp[col].max()), 2),
            }
            for col in FEATURE_COLUMNS
        }
        for crop, grp in df.groupby("label")
    }

    model_version = version or f"rf-v{datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')}"
    artifact_filename = f"crop_recommendation_{model_version}.joblib"
    artifact_path = models_dir / artifact_filename

    bundle = {
        "model": best_model,
        "feature_columns": FEATURE_COLUMNS,
        "classes": list(best_model.classes_),
        "model_name": "farmwise_crop_random_forest",
        "version": model_version,
        "algorithm": "RandomForestClassifier",
        "trained_at": datetime.now(timezone.utc).isoformat(),
        "dataset_sha256": dataset_sha,
        "dataset_rows": int(len(df)),
        "feature_importances": feature_importances,
        "crop_profiles": crop_profiles,
    }
    joblib.dump(bundle, artifact_path, compress=3)

    # Save Confusion Matrix chart
    fig, ax = plt.subplots(figsize=(12, 10))
    im = ax.imshow(cm, cmap="Greens")
    fig.colorbar(im, ax=ax)
    ax.set_xticks(np.arange(len(labels)))
    ax.set_yticks(np.arange(len(labels)))
    ax.set_xticklabels(labels, rotation=60, ha="right", fontsize=8)
    ax.set_yticklabels(labels, fontsize=8)
    ax.set_xlabel("Predicted Crop")
    ax.set_ylabel("True Crop")
    ax.set_title(f"FarmWise Random Forest Confusion Matrix ({model_version})")
    fig.tight_layout()
    fig.savefig(eval_dir / "confusion_matrix.png", dpi=150)
    plt.close(fig)

    # Save Feature Importance chart
    sorted_imp = sorted(feature_importances.items(), key=lambda x: x[1], reverse=True)
    fig, ax = plt.subplots(figsize=(8, 4.5))
    ax.barh([k for k, _ in sorted_imp][::-1], [v for _, v in sorted_imp][::-1], color="#2E7D32")
    ax.set_xlabel("Gini Importance")
    ax.set_title(f"Feature Importances ({model_version})")
    ax.grid(axis="x", alpha=0.25)
    fig.tight_layout()
    fig.savefig(eval_dir / "feature_importances.png", dpi=150)
    plt.close(fig)

    manifest: dict[str, Any] = {
        "model_name": "farmwise_crop_random_forest",
        "version": model_version,
        "algorithm": "RandomForestClassifier",
        "artifact_filename": artifact_filename,
        "artifact_path": str(artifact_path),
        "trained_at": bundle["trained_at"],
        "dataset_rows": int(len(df)),
        "train_rows": int(len(X_train)),
        "test_rows": int(len(X_test)),
        "dataset_sha256": dataset_sha,
        "best_params": search.best_params_,
        "cv_best_f1_macro": round(float(search.best_score_), 6),
        "metrics": {
            "accuracy": round(accuracy, 6),
            "precision_macro": round(precision_macro, 6),
            "recall_macro": round(recall_macro, 6),
            "f1_macro": round(f1_macro, 6),
        },
        "feature_importances": feature_importances,
        "labels": labels,
        "confusion_matrix": cm.tolist(),
        "classification_report": report,
    }

    (eval_dir / "metrics.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    (models_dir / "model_manifest.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    return manifest


if __name__ == "__main__":
    ml_root = Path(__file__).resolve().parents[1]
    manifest = train_and_evaluate(
        dataset_path=ml_root / "datasets" / "Crop_recommendation.csv",
        models_dir=ml_root / "models",
        eval_dir=ml_root / "evaluation",
    )
    print(
        f"Trained {manifest['version']} | accuracy={manifest['metrics']['accuracy']} | "
        f"f1_macro={manifest['metrics']['f1_macro']}"
    )
