"""Administration, notifications, audit logs, system settings, and contact schemas."""

from datetime import datetime
from decimal import Decimal
from typing import Any, Optional
import uuid

from pydantic import BaseModel, EmailStr, Field

from app.models.base import VerificationStatus
from app.schemas.common import ORMBase


class AdminUserUpdate(BaseModel):
    is_active: Optional[bool] = None
    is_verified: Optional[bool] = None


class FarmerVerifyRequest(BaseModel):
    verification_status: VerificationStatus
    rejection_reason: Optional[str] = Field(default=None, max_length=500)


class PlatformAnalyticsOut(BaseModel):
    total_users: int
    total_farmers: int
    pending_farmer_verifications: int
    total_customers: int
    total_farms: int
    total_products: int
    pending_product_approvals: int
    total_orders: int
    gross_merchandise_value: Decimal
    total_recommendations_run: int
    open_disputes: int
    open_inquiries: int
    orders_by_status: dict[str, int]
    revenue_by_day: list[dict[str, Any]]


class AuditLogOut(ORMBase):
    id: uuid.UUID
    actor_user_id: Optional[uuid.UUID]
    action: str
    entity_type: str
    entity_id: Optional[str]
    details_json: dict[str, Any]
    ip_address: Optional[str]
    created_at: datetime


class NotificationOut(ORMBase):
    id: uuid.UUID
    user_id: uuid.UUID
    title: str
    message: str
    category: str
    is_read: bool
    read_at: Optional[datetime]
    action_url: Optional[str]
    created_at: datetime


class NotificationBroadcast(BaseModel):
    target_role: Optional[str] = Field(default=None, description="admin, farmer, customer, or null for all")
    title: str = Field(min_length=2, max_length=160)
    message: str = Field(min_length=4, max_length=1500)
    category: str = Field(default="announcement", max_length=48)
    action_url: Optional[str] = Field(default=None, max_length=255)


class SystemSettingOut(ORMBase):
    id: uuid.UUID
    key: str
    value_json: dict[str, Any]
    description: str
    updated_at: datetime


class SystemSettingUpdate(BaseModel):
    value_json: dict[str, Any]
    description: Optional[str] = Field(default=None, max_length=255)


class ContactInquiryCreate(BaseModel):
    name: str = Field(min_length=2, max_length=120)
    email: EmailStr
    subject: str = Field(min_length=3, max_length=180)
    message: str = Field(min_length=10, max_length=3000)


class ContactInquiryResolve(BaseModel):
    status: str = Field(pattern="^(open|in_progress|resolved)$")
    admin_response: Optional[str] = Field(default=None, max_length=2000)


class ContactInquiryOut(ORMBase):
    id: uuid.UUID
    user_id: Optional[uuid.UUID]
    name: str
    email: EmailStr
    subject: str
    message: str
    status: str
    admin_response: Optional[str]
    created_at: datetime
