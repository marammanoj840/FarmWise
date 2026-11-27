"""Exploratory Data Analysis (EDA) and dataset validation for FarmWise Crop Recommendation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

REQUIRED_COLUMNS = ["N", "P", "K", "temperature", "humidity", "ph", "rainfall", "label"]
FEATURE_COLUMNS = ["N", "P", "K", "temperature", "humidity", "ph", "rainfall"]


def compute_sha256(file_path: Path) -> str:
    """Compute SHA-256 checksum for dataset provenance tracking."""
    hasher = hashlib.sha256()
    with file_path.open("rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def load_and_validate_dataset(dataset_path: Path) -> pd.DataFrame:
    """Load CSV, verify required columns, check numeric ranges, and clean duplicates/nulls."""
    if not dataset_path.exists():
        raise FileNotFoundError(f"Dataset file not found at {dataset_path}")

    df = pd.read_csv(dataset_path)
    missing_cols = [col for col in REQUIRED_COLUMNS if col not in df.columns]
    if missing_cols:
        raise ValueError(f"Dataset missing required columns: {missing_cols}")

    df = df[REQUIRED_COLUMNS].copy()
    for col in FEATURE_COLUMNS:
        df[col] = pd.to_numeric(df[col], errors="coerce")

    df["label"] = df["label"].astype(str).str.strip().str.lower()
    df = df.dropna(subset=REQUIRED_COLUMNS)
    df = df.drop_duplicates().reset_index(drop=True)

    if len(df) < 100:
        raise ValueError(f"Dataset has too few valid rows after cleaning: {len(df)}")
    if (df["ph"] < 0).any() or (df["ph"] > 14).any():
        raise ValueError("Dataset contains invalid soil pH outside [0, 14].")
    if (df["humidity"] < 0).any() or (df["humidity"] > 100).any():
        raise ValueError("Dataset contains invalid humidity outside [0, 100].")
    for non_neg_col in ["N", "P", "K", "rainfall"]:
        if (df[non_neg_col] < 0).any():
            raise ValueError(f"Dataset contains negative values in {non_neg_col}.")

    return df


def run_eda(dataset_path: Path, output_dir: Path) -> dict[str, Any]:
    """Generate EDA statistics and Matplotlib diagnostic charts."""
    output_dir.mkdir(parents=True, exist_ok=True)
    raw_df = pd.read_csv(dataset_path)
    clean_df = load_and_validate_dataset(dataset_path)

    summary: dict[str, Any] = {
        "dataset_path": str(dataset_path),
        "dataset_sha256": compute_sha256(dataset_path),
        "raw_rows": int(len(raw_df)),
        "clean_rows": int(len(clean_df)),
        "duplicates_removed": int(len(raw_df) - len(clean_df)),
        "null_counts_raw": {k: int(v) for k, v in raw_df.isnull().sum().to_dict().items()},
        "num_classes": int(clean_df["label"].nunique()),
        "class_distribution": {
            str(k): int(v) for k, v in clean_df["label"].value_counts().sort_index().items()
        },
        "feature_summary": {},
        "crop_centroids": {},
    }

    for col in FEATURE_COLUMNS:
        series = clean_df[col]
        summary["feature_summary"][col] = {
            "min": round(float(series.min()), 4),
            "q25": round(float(series.quantile(0.25)), 4),
            "median": round(float(series.median()), 4),
            "mean": round(float(series.mean()), 4),
            "q75": round(float(series.quantile(0.75)), 4),
            "max": round(float(series.max()), 4),
            "std": round(float(series.std()), 4),
        }

    for label, group in clean_df.groupby("label"):
        summary["crop_centroids"][str(label)] = {
            col: {
                "mean": round(float(group[col].mean()), 2),
                "std": round(float(group[col].std()), 2),
                "min": round(float(group[col].min()), 2),
                "max": round(float(group[col].max()), 2),
            }
            for col in FEATURE_COLUMNS
        }

    # Feature distribution histograms
    fig, axes = plt.subplots(2, 4, figsize=(16, 8))
    axes_flat = axes.flatten()
    for idx, col in enumerate(FEATURE_COLUMNS):
        ax = axes_flat[idx]
        ax.hist(clean_df[col], bins=25, color="#2E7D32", alpha=0.85, edgecolor="#0F291E")
        ax.set_title(col, fontsize=11, fontweight="bold")
        ax.grid(alpha=0.2)
    axes_flat[-1].axis("off")
    fig.suptitle("FarmWise Crop Dataset — Feature Distributions", fontsize=14, fontweight="bold")
    fig.tight_layout()
    fig.savefig(output_dir / "feature_distributions.png", dpi=150)
    plt.close(fig)

    # Correlation heatmap
    corr = clean_df[FEATURE_COLUMNS].corr().values
    fig, ax = plt.subplots(figsize=(8, 6))
    cax = ax.imshow(corr, cmap="YlGn", vmin=-1, vmax=1)
    fig.colorbar(cax, ax=ax)
    ax.set_xticks(np.arange(len(FEATURE_COLUMNS)))
    ax.set_yticks(np.arange(len(FEATURE_COLUMNS)))
    ax.set_xticklabels(FEATURE_COLUMNS, rotation=45, ha="right")
    ax.set_yticklabels(FEATURE_COLUMNS)
    for i in range(len(FEATURE_COLUMNS)):
        for j in range(len(FEATURE_COLUMNS)):
            ax.text(j, i, f"{corr[i, j]:.2f}", ha="center", va="center", color="#09130F", fontsize=9)
    ax.set_title("Soil & Climate Feature Correlations", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(output_dir / "correlation_heatmap.png", dpi=150)
    plt.close(fig)

    (output_dir / "eda_summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    return summary


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1]
    ds_path = root / "datasets" / "Crop_recommendation.csv"
    out_dir = root / "evaluation"
    result = run_eda(ds_path, out_dir)
    print(f"EDA complete: {result['clean_rows']} rows across {result['num_classes']} crops.")
