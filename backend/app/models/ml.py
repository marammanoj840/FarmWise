"""ML model version registry and crop recommendation history ORM models."""

from typing import Optional
import uuid

from sqlalchemy import JSON, Boolean, CheckConstraint, Float, ForeignKey, Integer, String, Text, Uuid
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base
from app.models.base import TimestampMixin, UUIDPrimaryKeyMixin


class MLModelVersion(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "ml_model_versions"

    model_name: Mapped[str] = mapped_column(String(100), nullable=False)
    version: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    algorithm: Mapped[str] = mapped_column(String(80), nullable=False)
    accuracy: Mapped[float] = mapped_column(Float, nullable=False)
    precision_macro: Mapped[float] = mapped_column(Float, nullable=False)
    recall_macro: Mapped[float] = mapped_column(Float, nullable=False)
    f1_macro: Mapped[float] = mapped_column(Float, nullable=False)
    dataset_rows: Mapped[int] = mapped_column(Integer, nullable=False)
    dataset_sha256: Mapped[str] = mapped_column(String(64), nullable=False)
    artifact_path: Mapped[str] = mapped_column(String(255), nullable=False)
    metrics_json: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False, index=True)


class CropRecommendation(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "crop_recommendations"
    __table_args__ = (
        CheckConstraint("nitrogen >= 0", name="rec_n_non_negative"),
        CheckConstraint("phosphorus >= 0", name="rec_p_non_negative"),
        CheckConstraint("potassium >= 0", name="rec_k_non_negative"),
        CheckConstraint("ph >= 0 AND ph <= 14", name="rec_ph_range"),
        CheckConstraint("humidity >= 0 AND humidity <= 100", name="rec_humidity_range"),
        CheckConstraint("rainfall >= 0", name="rec_rainfall_non_negative"),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True
    )
    farm_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("farms.id", ondelete="SET NULL"), nullable=True
    )
    model_version_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("ml_model_versions.id", ondelete="SET NULL"), nullable=True
    )
    nitrogen: Mapped[float] = mapped_column(Float, nullable=False)
    phosphorus: Mapped[float] = mapped_column(Float, nullable=False)
    potassium: Mapped[float] = mapped_column(Float, nullable=False)
    temperature: Mapped[float] = mapped_column(Float, nullable=False)
    humidity: Mapped[float] = mapped_column(Float, nullable=False)
    ph: Mapped[float] = mapped_column(Float, nullable=False)
    rainfall: Mapped[float] = mapped_column(Float, nullable=False)
    recommended_crop: Mapped[str] = mapped_column(String(100), nullable=False)
    confidence_score: Mapped[float] = mapped_column(Float, nullable=False)
    top_predictions_json: Mapped[list] = mapped_column(JSON, default=list, nullable=False)
    explanation_json: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False)
    advisory_note: Mapped[str] = mapped_column(Text, nullable=False)
