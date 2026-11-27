"""Farm, soil record, crop cycle, and cultivation activity schemas."""

from datetime import date, datetime
from decimal import Decimal
from typing import Optional
import uuid

from pydantic import BaseModel, Field

from app.models.base import ActivityType, CropCycleStatus
from app.schemas.common import ORMBase


class FarmLocationIn(BaseModel):
    address_line: Optional[str] = Field(default=None, max_length=255)
    village_or_city: str = Field(min_length=1, max_length=120)
    district: str = Field(min_length=1, max_length=120)
    state: str = Field(min_length=1, max_length=120)
    country: str = Field(default="India", max_length=100)
    postal_code: Optional[str] = Field(default=None, max_length=20)
    latitude: Optional[float] = Field(default=None, ge=-90, le=90)
    longitude: Optional[float] = Field(default=None, ge=-180, le=180)
    elevation_m: Optional[float] = Field(default=None, ge=-500, le=9000)
    climate_zone: Optional[str] = Field(default=None, max_length=80)


class FarmLocationOut(ORMBase):
    id: uuid.UUID
    farm_id: uuid.UUID
    address_line: Optional[str]
    village_or_city: str
    district: str
    state: str
    country: str
    postal_code: Optional[str]
    latitude: Optional[float]
    longitude: Optional[float]
    elevation_m: Optional[float]
    climate_zone: Optional[str]


class FarmCreate(BaseModel):
    name: str = Field(min_length=2, max_length=150)
    total_area_hectares: float = Field(gt=0, le=100000)
    primary_irrigation_type: str = Field(default="drip", max_length=64)
    soil_type: str = Field(default="loamy", max_length=64)
    description: Optional[str] = Field(default=None, max_length=2000)
    location: FarmLocationIn


class FarmUpdate(BaseModel):
    name: Optional[str] = Field(default=None, min_length=2, max_length=150)
    total_area_hectares: Optional[float] = Field(default=None, gt=0, le=100000)
    primary_irrigation_type: Optional[str] = Field(default=None, max_length=64)
    soil_type: Optional[str] = Field(default=None, max_length=64)
    description: Optional[str] = Field(default=None, max_length=2000)
    is_active: Optional[bool] = None
    location: Optional[FarmLocationIn] = None


class FarmOut(ORMBase):
    id: uuid.UUID
    farmer_id: uuid.UUID
    name: str
    total_area_hectares: float
    primary_irrigation_type: str
    soil_type: str
    description: Optional[str]
    is_active: bool
    created_at: datetime
    updated_at: datetime
    location: Optional[FarmLocationOut] = None


class SoilRecordCreate(BaseModel):
    nitrogen: float = Field(ge=0, le=500)
    phosphorus: float = Field(ge=0, le=500)
    potassium: float = Field(ge=0, le=500)
    ph: float = Field(ge=0, le=14)
    organic_matter_pct: Optional[float] = Field(default=None, ge=0, le=100)
    moisture_pct: Optional[float] = Field(default=None, ge=0, le=100)
    soil_texture: Optional[str] = Field(default=None, max_length=64)
    notes: Optional[str] = Field(default=None, max_length=1000)


class SoilRecordOut(ORMBase):
    id: uuid.UUID
    farm_id: uuid.UUID
    recorded_at: datetime
    nitrogen: float
    phosphorus: float
    potassium: float
    ph: float
    organic_matter_pct: Optional[float]
    moisture_pct: Optional[float]
    soil_texture: Optional[str]
    notes: Optional[str]


class CropOut(ORMBase):
    id: uuid.UUID
    name: str
    scientific_name: Optional[str]
    category: str
    typical_duration_days: Optional[int]
    water_requirement: Optional[str]
    description: Optional[str]


class CropCycleCreate(BaseModel):
    farm_id: uuid.UUID
    crop_id: Optional[uuid.UUID] = None
    crop_name: str = Field(min_length=2, max_length=100)
    season: str = Field(default="Kharif", max_length=64)
    area_hectares: float = Field(gt=0, le=100000)
    sowing_date: Optional[date] = None
    expected_harvest_date: Optional[date] = None
    status: CropCycleStatus = CropCycleStatus.PLANNED
    expected_yield_kg: Optional[float] = Field(default=None, ge=0)
    notes: Optional[str] = Field(default=None, max_length=1500)


class CropCycleUpdate(BaseModel):
    season: Optional[str] = Field(default=None, max_length=64)
    area_hectares: Optional[float] = Field(default=None, gt=0, le=100000)
    sowing_date: Optional[date] = None
    expected_harvest_date: Optional[date] = None
    actual_harvest_date: Optional[date] = None
    status: Optional[CropCycleStatus] = None
    expected_yield_kg: Optional[float] = Field(default=None, ge=0)
    actual_yield_kg: Optional[float] = Field(default=None, ge=0)
    notes: Optional[str] = Field(default=None, max_length=1500)


class CropCycleOut(ORMBase):
    id: uuid.UUID
    farm_id: uuid.UUID
    crop_id: Optional[uuid.UUID]
    crop_name: str
    season: str
    area_hectares: float
    sowing_date: Optional[date]
    expected_harvest_date: Optional[date]
    actual_harvest_date: Optional[date]
    status: str
    expected_yield_kg: Optional[float]
    actual_yield_kg: Optional[float]
    notes: Optional[str]
    created_at: datetime


class CultivationActivityCreate(BaseModel):
    crop_cycle_id: uuid.UUID
    activity_type: ActivityType
    title: str = Field(min_length=2, max_length=160)
    performed_at: date
    quantity: Optional[float] = Field(default=None, ge=0)
    unit: Optional[str] = Field(default=None, max_length=32)
    input_material: Optional[str] = Field(default=None, max_length=120)
    cost_amount: Decimal = Field(default=Decimal("0.00"), ge=0)
    notes: Optional[str] = Field(default=None, max_length=1000)


class CultivationActivityOut(ORMBase):
    id: uuid.UUID
    crop_cycle_id: uuid.UUID
    farm_id: uuid.UUID
    activity_type: str
    title: str
    performed_at: date
    quantity: Optional[float]
    unit: Optional[str]
    input_material: Optional[str]
    cost_amount: Decimal
    notes: Optional[str]
    created_at: datetime
