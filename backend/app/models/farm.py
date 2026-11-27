"""Farm, location, soil, crop catalog, crop cycle, and cultivation activity ORM models."""

from datetime import date, datetime
from decimal import Decimal
from typing import Optional
import uuid

from sqlalchemy import (
    Boolean,
    CheckConstraint,
    Date,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    Numeric,
    String,
    Text,
    Uuid,
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base
from app.models.base import CropCycleStatus, TimestampMixin, UUIDPrimaryKeyMixin, utcnow


class Farm(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "farms"
    __table_args__ = (CheckConstraint("total_area_hectares > 0", name="farm_area_positive"),)

    farmer_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True
    )
    name: Mapped[str] = mapped_column(String(150), nullable=False)
    total_area_hectares: Mapped[float] = mapped_column(Float, nullable=False)
    primary_irrigation_type: Mapped[str] = mapped_column(String(64), default="drip", nullable=False)
    soil_type: Mapped[str] = mapped_column(String(64), default="loamy", nullable=False)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    location: Mapped[Optional["FarmLocation"]] = relationship(
        back_populates="farm", uselist=False, cascade="all, delete-orphan"
    )
    soil_records: Mapped[list["SoilRecord"]] = relationship(
        back_populates="farm", cascade="all, delete-orphan"
    )
    crop_cycles: Mapped[list["CropCycle"]] = relationship(
        back_populates="farm", cascade="all, delete-orphan"
    )


class FarmLocation(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "farm_locations"

    farm_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("farms.id", ondelete="CASCADE"), unique=True, nullable=False
    )
    address_line: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    village_or_city: Mapped[str] = mapped_column(String(120), nullable=False)
    district: Mapped[str] = mapped_column(String(120), nullable=False)
    state: Mapped[str] = mapped_column(String(120), nullable=False)
    country: Mapped[str] = mapped_column(String(100), default="India", nullable=False)
    postal_code: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    latitude: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    longitude: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    elevation_m: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    climate_zone: Mapped[Optional[str]] = mapped_column(String(80), nullable=True)

    farm: Mapped["Farm"] = relationship(back_populates="location")


class SoilRecord(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "soil_records"
    __table_args__ = (
        CheckConstraint("nitrogen >= 0", name="soil_n_non_negative"),
        CheckConstraint("phosphorus >= 0", name="soil_p_non_negative"),
        CheckConstraint("potassium >= 0", name="soil_k_non_negative"),
        CheckConstraint("ph >= 0 AND ph <= 14", name="soil_ph_range"),
    )

    farm_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("farms.id", ondelete="CASCADE"), nullable=False, index=True
    )
    recorded_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow, nullable=False)
    nitrogen: Mapped[float] = mapped_column(Float, nullable=False)
    phosphorus: Mapped[float] = mapped_column(Float, nullable=False)
    potassium: Mapped[float] = mapped_column(Float, nullable=False)
    ph: Mapped[float] = mapped_column(Float, nullable=False)
    organic_matter_pct: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    moisture_pct: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    soil_texture: Mapped[Optional[str]] = mapped_column(String(64), nullable=True)
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)

    farm: Mapped["Farm"] = relationship(back_populates="soil_records")


class Crop(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "crops"

    name: Mapped[str] = mapped_column(String(100), unique=True, nullable=False)
    scientific_name: Mapped[Optional[str]] = mapped_column(String(150), nullable=True)
    category: Mapped[str] = mapped_column(String(64), default="grain", nullable=False)
    typical_duration_days: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    water_requirement: Mapped[Optional[str]] = mapped_column(String(64), nullable=True)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)


class CropCycle(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "crop_cycles"
    __table_args__ = (CheckConstraint("area_hectares > 0", name="crop_cycle_area_positive"),)

    farm_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("farms.id", ondelete="CASCADE"), nullable=False, index=True
    )
    crop_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("crops.id", ondelete="SET NULL"), nullable=True
    )
    crop_name: Mapped[str] = mapped_column(String(100), nullable=False)
    season: Mapped[str] = mapped_column(String(64), default="Kharif", nullable=False)
    area_hectares: Mapped[float] = mapped_column(Float, nullable=False)
    sowing_date: Mapped[Optional[date]] = mapped_column(Date, nullable=True)
    expected_harvest_date: Mapped[Optional[date]] = mapped_column(Date, nullable=True)
    actual_harvest_date: Mapped[Optional[date]] = mapped_column(Date, nullable=True)
    status: Mapped[str] = mapped_column(
        String(32), default=CropCycleStatus.PLANNED.value, nullable=False, index=True
    )
    expected_yield_kg: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    actual_yield_kg: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)

    farm: Mapped["Farm"] = relationship(back_populates="crop_cycles")
    activities: Mapped[list["CultivationActivity"]] = relationship(
        back_populates="crop_cycle", cascade="all, delete-orphan"
    )


class CultivationActivity(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    __tablename__ = "cultivation_activities"
    __table_args__ = (CheckConstraint("cost_amount >= 0", name="activity_cost_non_negative"),)

    crop_cycle_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("crop_cycles.id", ondelete="CASCADE"), nullable=False, index=True
    )
    farm_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("farms.id", ondelete="CASCADE"), nullable=False, index=True
    )
    activity_type: Mapped[str] = mapped_column(String(40), nullable=False, index=True)
    title: Mapped[str] = mapped_column(String(160), nullable=False)
    performed_at: Mapped[date] = mapped_column(Date, nullable=False)
    quantity: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    unit: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
    input_material: Mapped[Optional[str]] = mapped_column(String(120), nullable=True)
    cost_amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), default=Decimal("0.00"), nullable=False)
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)

    crop_cycle: Mapped["CropCycle"] = relationship(back_populates="activities")
