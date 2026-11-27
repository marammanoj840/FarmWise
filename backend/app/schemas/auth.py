"""Authentication, token, and profile schemas."""

from datetime import datetime
from typing import Literal, Optional
import uuid

from pydantic import BaseModel, EmailStr, Field, field_validator

from app.schemas.common import ORMBase


class RegisterRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8, max_length=128)
    full_name: str = Field(min_length=2, max_length=120)
    phone: Optional[str] = Field(default=None, max_length=32)
    role: Literal["farmer", "customer"]
    farm_business_name: Optional[str] = Field(default=None, max_length=160)
    district: Optional[str] = Field(default=None, max_length=100)
    state: Optional[str] = Field(default=None, max_length=100)

    @field_validator("password")
    @classmethod
    def check_password_strength(cls, v: str) -> str:
        if not any(c.isalpha() for c in v) or not any(c.isdigit() for c in v):
            raise ValueError("Password must include at least one letter and one digit.")
        return v


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=1, max_length=128)


class RefreshRequest(BaseModel):
    refresh_token: str = Field(min_length=16)


class LogoutRequest(BaseModel):
    refresh_token: Optional[str] = None


class FarmerProfileOut(ORMBase):
    id: uuid.UUID
    user_id: uuid.UUID
    farm_business_name: str
    bio: Optional[str]
    district: Optional[str]
    state: Optional[str]
    country: str
    years_experience: int
    verification_status: str
    verified_at: Optional[datetime]
    rejection_reason: Optional[str]


class CustomerProfileOut(ORMBase):
    id: uuid.UUID
    user_id: uuid.UUID
    preferred_delivery_notes: Optional[str]
    city: Optional[str]
    state: Optional[str]


class UserOut(ORMBase):
    id: uuid.UUID
    email: EmailStr
    full_name: str
    phone: Optional[str]
    role_name: str
    is_active: bool
    is_verified: bool
    last_login_at: Optional[datetime]
    created_at: datetime
    farmer_profile: Optional[FarmerProfileOut] = None
    customer_profile: Optional[CustomerProfileOut] = None


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int
    user: UserOut


class ProfileUpdateRequest(BaseModel):
    full_name: Optional[str] = Field(default=None, min_length=2, max_length=120)
    phone: Optional[str] = Field(default=None, max_length=32)
    farm_business_name: Optional[str] = Field(default=None, max_length=160)
    bio: Optional[str] = Field(default=None, max_length=1500)
    district: Optional[str] = Field(default=None, max_length=100)
    state: Optional[str] = Field(default=None, max_length=100)
    years_experience: Optional[int] = Field(default=None, ge=0, le=80)
    preferred_delivery_notes: Optional[str] = Field(default=None, max_length=500)
    city: Optional[str] = Field(default=None, max_length=100)
