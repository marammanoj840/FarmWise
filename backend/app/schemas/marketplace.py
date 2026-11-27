"""Marketplace, cart, wishlist, address, multi-farmer order, and verified review schemas."""

from datetime import date, datetime
from decimal import Decimal
from typing import Any, Optional
import uuid

from pydantic import BaseModel, Field

from app.models.base import DisputeStatus, FulfillmentStatus, ProductApprovalStatus
from app.schemas.common import ORMBase


class CategoryCreate(BaseModel):
    name: str = Field(min_length=2, max_length=80)
    description: Optional[str] = Field(default=None, max_length=500)
    icon_name: str = Field(default="Sprout", max_length=64)


class CategoryUpdate(BaseModel):
    name: Optional[str] = Field(default=None, min_length=2, max_length=80)
    description: Optional[str] = Field(default=None, max_length=500)
    icon_name: Optional[str] = Field(default=None, max_length=64)
    is_active: Optional[bool] = None


class CategoryOut(ORMBase):
    id: uuid.UUID
    name: str
    slug: str
    description: Optional[str]
    icon_name: str
    is_active: bool


class ProductImageOut(ORMBase):
    id: uuid.UUID
    image_url: str
    alt_text: str
    is_primary: bool
    sort_order: int


class ProductCreate(BaseModel):
    farm_id: Optional[uuid.UUID] = None
    category_id: uuid.UUID
    title: str = Field(min_length=3, max_length=180)
    description: str = Field(min_length=10, max_length=3000)
    unit: str = Field(default="kg", max_length=32)
    price: Decimal = Field(gt=0, max_digits=12, decimal_places=2)
    stock_quantity: int = Field(ge=0, le=1000000)
    min_order_quantity: int = Field(default=1, ge=1, le=1000)
    is_organic: bool = False
    harvest_date: Optional[date] = None
    image_urls: list[str] = Field(default_factory=list)


class ProductUpdate(BaseModel):
    category_id: Optional[uuid.UUID] = None
    title: Optional[str] = Field(default=None, min_length=3, max_length=180)
    description: Optional[str] = Field(default=None, min_length=10, max_length=3000)
    unit: Optional[str] = Field(default=None, max_length=32)
    price: Optional[Decimal] = Field(default=None, gt=0, max_digits=12, decimal_places=2)
    stock_quantity: Optional[int] = Field(default=None, ge=0, le=1000000)
    min_order_quantity: Optional[int] = Field(default=None, ge=1, le=1000)
    is_organic: Optional[bool] = None
    harvest_date: Optional[date] = None
    is_available: Optional[bool] = None


class ProductModerationRequest(BaseModel):
    approval_status: ProductApprovalStatus
    moderation_note: Optional[str] = Field(default=None, max_length=500)


class ProductOut(ORMBase):
    id: uuid.UUID
    farmer_id: uuid.UUID
    farm_id: Optional[uuid.UUID]
    category_id: uuid.UUID
    title: str
    slug: str
    description: str
    unit: str
    price: Decimal
    stock_quantity: int
    reserved_quantity: int
    min_order_quantity: int
    is_organic: bool
    harvest_date: Optional[date]
    approval_status: str
    moderation_note: Optional[str]
    is_available: bool
    created_at: datetime
    images: list[ProductImageOut] = Field(default_factory=list)
    farmer_name: Optional[str] = None
    category_name: Optional[str] = None
    average_rating: Optional[float] = None
    review_count: int = 0


class CartItemAdd(BaseModel):
    product_id: uuid.UUID
    quantity: int = Field(gt=0, le=10000)


class CartItemUpdate(BaseModel):
    quantity: int = Field(gt=0, le=10000)


class CartItemOut(BaseModel):
    id: uuid.UUID
    product_id: uuid.UUID
    title: str
    unit: str
    unit_price: Decimal
    quantity: int
    stock_quantity: int
    line_total: Decimal
    farmer_id: uuid.UUID
    farmer_name: str
    is_available: bool


class CartOut(BaseModel):
    id: uuid.UUID
    items: list[CartItemOut]
    subtotal_amount: Decimal
    item_count: int


class WishlistItemOut(BaseModel):
    id: uuid.UUID
    product_id: uuid.UUID
    product: ProductOut
    created_at: datetime


class AddressCreate(BaseModel):
    label: str = Field(default="Home", max_length=64)
    recipient_name: str = Field(min_length=2, max_length=120)
    phone: str = Field(min_length=6, max_length=32)
    line1: str = Field(min_length=3, max_length=255)
    line2: Optional[str] = Field(default=None, max_length=255)
    city: str = Field(min_length=2, max_length=100)
    state: str = Field(min_length=2, max_length=100)
    postal_code: str = Field(min_length=3, max_length=20)
    country: str = Field(default="India", max_length=100)
    is_default: bool = False


class AddressUpdate(BaseModel):
    label: Optional[str] = Field(default=None, max_length=64)
    recipient_name: Optional[str] = Field(default=None, max_length=120)
    phone: Optional[str] = Field(default=None, max_length=32)
    line1: Optional[str] = Field(default=None, max_length=255)
    line2: Optional[str] = Field(default=None, max_length=255)
    city: Optional[str] = Field(default=None, max_length=100)
    state: Optional[str] = Field(default=None, max_length=100)
    postal_code: Optional[str] = Field(default=None, max_length=20)
    is_default: Optional[bool] = None


class AddressOut(ORMBase):
    id: uuid.UUID
    user_id: uuid.UUID
    label: str
    recipient_name: str
    phone: str
    line1: str
    line2: Optional[str]
    city: str
    state: str
    postal_code: str
    country: str
    is_default: bool


class CheckoutRequest(BaseModel):
    address_id: uuid.UUID
    notes: Optional[str] = Field(default=None, max_length=1000)
    acknowledge_dev_payment: bool = Field(
        default=True,
        description="Explicitly acknowledges development-only payment workflow when live gateway is unconfigured.",
    )


class FulfillmentUpdateRequest(BaseModel):
    status: FulfillmentStatus
    tracking_number: Optional[str] = Field(default=None, max_length=100)
    carrier: Optional[str] = Field(default=None, max_length=80)


class OrderCancelRequest(BaseModel):
    reason: str = Field(min_length=3, max_length=500)


class ReturnRequest(BaseModel):
    reason: str = Field(min_length=5, max_length=500)


class DisputeRequest(BaseModel):
    dispute_status: DisputeStatus
    dispute_notes: Optional[str] = Field(default=None, max_length=1000)


class OrderItemOut(ORMBase):
    id: uuid.UUID
    product_id: Optional[uuid.UUID]
    farmer_id: uuid.UUID
    product_title_snapshot: str
    unit_snapshot: str
    unit_price_snapshot: Decimal
    quantity: int
    line_total: Decimal


class FulfillmentGroupOut(ORMBase):
    id: uuid.UUID
    order_id: uuid.UUID
    farmer_id: uuid.UUID
    status: str
    subtotal_amount: Decimal
    tracking_number: Optional[str]
    carrier: Optional[str]
    dispatched_at: Optional[datetime]
    delivered_at: Optional[datetime]
    return_requested: bool
    return_reason: Optional[str]
    return_status: Optional[str]
    dispute_status: str
    dispute_notes: Optional[str]
    items: list[OrderItemOut] = Field(default_factory=list)


class PaymentOut(ORMBase):
    id: uuid.UUID
    provider: str
    status: str
    amount: Decimal
    currency: str
    transaction_reference: str
    is_dev_workflow: bool
    metadata_json: dict[str, Any]


class OrderOut(ORMBase):
    id: uuid.UUID
    order_number: str
    customer_id: uuid.UUID
    shipping_address_snapshot: dict[str, Any]
    status: str
    subtotal_amount: Decimal
    shipping_amount: Decimal
    total_amount: Decimal
    currency: str
    cancellation_reason: Optional[str]
    cancelled_at: Optional[datetime]
    notes: Optional[str]
    created_at: datetime
    fulfillment_groups: list[FulfillmentGroupOut] = Field(default_factory=list)
    items: list[OrderItemOut] = Field(default_factory=list)
    payment: Optional[PaymentOut] = None


class ReviewCreate(BaseModel):
    order_item_id: uuid.UUID
    rating: int = Field(ge=1, le=5)
    title: str = Field(min_length=3, max_length=140)
    comment: str = Field(min_length=5, max_length=2000)


class ReviewModerationRequest(BaseModel):
    is_visible: bool
    moderation_note: Optional[str] = Field(default=None, max_length=500)


class ReviewOut(ORMBase):
    id: uuid.UUID
    product_id: uuid.UUID
    customer_id: uuid.UUID
    order_item_id: uuid.UUID
    rating: int
    title: str
    comment: str
    is_verified_purchase: bool
    is_visible: bool
    moderation_note: Optional[str]
    created_at: datetime
    customer_name: Optional[str] = None
