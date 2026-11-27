"""Resources, inventory, equipment, finance, and farmer dashboard schemas."""

from datetime import date, datetime
from decimal import Decimal
from typing import Optional
import uuid

from pydantic import BaseModel, Field

from app.schemas.common import ORMBase


class ResourceCreate(BaseModel):
    farm_id: Optional[uuid.UUID] = None
    name: str = Field(min_length=2, max_length=140)
    resource_type: str = Field(min_length=2, max_length=64)
    unit: str = Field(min_length=1, max_length=32)
    available_quantity: float = Field(ge=0)
    notes: Optional[str] = Field(default=None, max_length=1000)


class ResourceUpdate(BaseModel):
    available_quantity: Optional[float] = Field(default=None, ge=0)
    notes: Optional[str] = Field(default=None, max_length=1000)


class ResourceOut(ORMBase):
    id: uuid.UUID
    farmer_id: uuid.UUID
    farm_id: Optional[uuid.UUID]
    name: str
    resource_type: str
    unit: str
    available_quantity: float
    notes: Optional[str]
    created_at: datetime


class InventoryItemCreate(BaseModel):
    farm_id: Optional[uuid.UUID] = None
    name: str = Field(min_length=2, max_length=140)
    category: str = Field(min_length=2, max_length=64)
    sku: Optional[str] = Field(default=None, max_length=64)
    quantity: float = Field(ge=0)
    unit: str = Field(min_length=1, max_length=32)
    reorder_level: float = Field(default=0, ge=0)
    unit_cost: Decimal = Field(default=Decimal("0.00"), ge=0)


class InventoryItemUpdate(BaseModel):
    quantity: Optional[float] = Field(default=None, ge=0)
    reorder_level: Optional[float] = Field(default=None, ge=0)
    unit_cost: Optional[Decimal] = Field(default=None, ge=0)


class InventoryItemOut(ORMBase):
    id: uuid.UUID
    farmer_id: uuid.UUID
    farm_id: Optional[uuid.UUID]
    name: str
    category: str
    sku: Optional[str]
    quantity: float
    unit: str
    reorder_level: float
    unit_cost: Decimal
    created_at: datetime


class EquipmentCreate(BaseModel):
    farm_id: Optional[uuid.UUID] = None
    name: str = Field(min_length=2, max_length=140)
    equipment_type: str = Field(min_length=2, max_length=64)
    serial_number: Optional[str] = Field(default=None, max_length=80)
    status: str = Field(default="operational", max_length=32)
    purchase_date: Optional[date] = None
    purchase_cost: Decimal = Field(default=Decimal("0.00"), ge=0)
    last_maintenance_date: Optional[date] = None
    next_maintenance_date: Optional[date] = None


class EquipmentUpdate(BaseModel):
    status: Optional[str] = Field(default=None, max_length=32)
    last_maintenance_date: Optional[date] = None
    next_maintenance_date: Optional[date] = None


class EquipmentOut(ORMBase):
    id: uuid.UUID
    farmer_id: uuid.UUID
    farm_id: Optional[uuid.UUID]
    name: str
    equipment_type: str
    serial_number: Optional[str]
    status: str
    purchase_date: Optional[date]
    purchase_cost: Decimal
    last_maintenance_date: Optional[date]
    next_maintenance_date: Optional[date]
    created_at: datetime


class ExpenseCreate(BaseModel):
    farm_id: Optional[uuid.UUID] = None
    crop_cycle_id: Optional[uuid.UUID] = None
    category: str = Field(min_length=2, max_length=64)
    description: str = Field(min_length=2, max_length=255)
    amount: Decimal = Field(gt=0)
    expense_date: date
    vendor: Optional[str] = Field(default=None, max_length=120)


class ExpenseOut(ORMBase):
    id: uuid.UUID
    farmer_id: uuid.UUID
    farm_id: Optional[uuid.UUID]
    crop_cycle_id: Optional[uuid.UUID]
    category: str
    description: str
    amount: Decimal
    expense_date: date
    vendor: Optional[str]
    created_at: datetime


class RevenueCreate(BaseModel):
    farm_id: Optional[uuid.UUID] = None
    crop_cycle_id: Optional[uuid.UUID] = None
    source: str = Field(default="crop_sale", max_length=64)
    description: str = Field(min_length=2, max_length=255)
    amount: Decimal = Field(gt=0)
    revenue_date: date


class RevenueOut(ORMBase):
    id: uuid.UUID
    farmer_id: uuid.UUID
    farm_id: Optional[uuid.UUID]
    crop_cycle_id: Optional[uuid.UUID]
    order_id: Optional[uuid.UUID]
    source: str
    description: str
    amount: Decimal
    revenue_date: date
    created_at: datetime


class FinanceSummaryOut(BaseModel):
    total_expenses: Decimal
    total_revenue: Decimal
    net_profit: Decimal
    expenses_by_category: dict[str, Decimal]
    revenue_by_source: dict[str, Decimal]
    monthly_series: list[dict]


class FarmerDashboardOut(BaseModel):
    farms_count: int
    total_area_hectares: float
    active_crop_cycles: int
    harvested_cycles: int
    low_stock_items: int
    pending_fulfillments: int
    total_expenses: Decimal
    total_revenue: Decimal
    net_profit: Decimal
    recent_activities: list[dict]
    recent_recommendations: list[dict]
    monthly_finance: list[dict]
