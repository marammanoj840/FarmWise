"""Shared ORM mixins, types, and domain enums."""

from datetime import datetime, timezone
from enum import StrEnum
import uuid

from sqlalchemy import DateTime, Uuid
from sqlalchemy.orm import Mapped, mapped_column


def utcnow() -> datetime:
    """Return timezone-aware UTC datetime."""
    return datetime.now(timezone.utc)


class UUIDPrimaryKeyMixin:
    """Adds a UUID primary key column."""

    id: Mapped[uuid.UUID] = mapped_column(Uuid(as_uuid=True), primary_key=True, default=uuid.uuid4)


class TimestampMixin:
    """Adds UTC created_at and updated_at timestamp columns."""

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow, nullable=False
    )


class VerificationStatus(StrEnum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"


class CropCycleStatus(StrEnum):
    PLANNED = "planned"
    SOWN = "sown"
    GROWING = "growing"
    HARVESTED = "harvested"
    FAILED = "failed"


class ActivityType(StrEnum):
    SOIL_PREP = "soil_prep"
    SOWING = "sowing"
    IRRIGATION = "irrigation"
    FERTILIZATION = "fertilization"
    PESTICIDE = "pesticide"
    WEEDING = "weeding"
    HARVESTING = "harvesting"
    INSPECTION = "inspection"


class ProductApprovalStatus(StrEnum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"


class OrderStatus(StrEnum):
    PENDING_PAYMENT = "pending_payment"
    CONFIRMED = "confirmed"
    PROCESSING = "processing"
    PARTIALLY_SHIPPED = "partially_shipped"
    COMPLETED = "completed"
    CANCELLED = "cancelled"
    REFUND_REQUESTED = "refund_requested"
    REFUNDED = "refunded"


class FulfillmentStatus(StrEnum):
    PENDING = "pending"
    PACKING = "packing"
    SHIPPED = "shipped"
    DELIVERED = "delivered"
    CANCELLED = "cancelled"
    RETURN_REQUESTED = "return_requested"
    RETURNED = "returned"


class PaymentStatus(StrEnum):
    PENDING_INTEGRATION = "pending_integration"
    DEV_AUTHORIZED = "dev_authorized"
    FAILED = "failed"
    REFUNDED = "refunded"


class DisputeStatus(StrEnum):
    NONE = "none"
    OPEN = "open"
    RESOLVED = "resolved"
    REJECTED = "rejected"
