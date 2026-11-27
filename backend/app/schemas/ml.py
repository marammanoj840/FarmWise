"""Machine Learning crop recommendation schemas."""

from datetime import datetime
from typing import Any, Optional
import uuid

from pydantic import BaseModel, Field

from app.schemas.common import ORMBase


class CropRecommendationRequest(BaseModel):
    farm_id: Optional[uuid.UUID] = None
    nitrogen: float = Field(ge=0, le=300, description="Soil Nitrogen ratio (N)")
    phosphorus: float = Field(ge=0, le=300, description="Soil Phosphorus ratio (P)")
    potassium: float = Field(ge=0, le=300, description="Soil Potassium ratio (K)")
    temperature: float = Field(ge=-10, le=60, description="Temperature in Celsius")
    humidity: float = Field(ge=0, le=100, description="Relative humidity (%)")
    ph: float = Field(ge=0, le=14, description="Soil pH")
    rainfall: float = Field(ge=0, le=1000, description="Rainfall in mm")


class CropPredictionCandidate(BaseModel):
    crop: str
    confidence: float


class MLModelVersionOut(ORMBase):
    id: uuid.UUID
    model_name: str
    version: str
    algorithm: str
    accuracy: float
    precision_macro: float
    recall_macro: float
    f1_macro: float
    dataset_rows: int
    dataset_sha256: str
    metrics_json: dict[str, Any]
    is_active: bool
    created_at: datetime


class CropRecommendationOut(ORMBase):
    id: uuid.UUID
    user_id: uuid.UUID
    farm_id: Optional[uuid.UUID]
    model_version_id: Optional[uuid.UUID]
    nitrogen: float
    phosphorus: float
    potassium: float
    temperature: float
    humidity: float
    ph: float
    rainfall: float
    recommended_crop: str
    confidence_score: float
    top_predictions_json: list[dict[str, Any]]
    explanation_json: dict[str, Any]
    advisory_note: str
    created_at: datetime
