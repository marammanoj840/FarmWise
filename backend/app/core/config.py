"""Application configuration using Pydantic Settings."""

from functools import lru_cache
from pathlib import Path
import secrets
from typing import List

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parents[2]
PROJECT_ROOT = BASE_DIR.parent
_DEV_FALLBACK_SECRET = secrets.token_urlsafe(48)


class Settings(BaseSettings):
    """Centralized typed runtime settings for FarmWise backend."""

    model_config = SettingsConfigDict(
        env_file=(str(PROJECT_ROOT / ".env"), str(BASE_DIR / ".env")),
        env_file_encoding="utf-8",
        extra="ignore",
        case_sensitive=False,
    )

    APP_NAME: str = "FarmWise"
    APP_ENV: str = "development"
    DEBUG: bool = True
    LOG_LEVEL: str = "INFO"
    API_V1_PREFIX: str = "/api/v1"

    BACKEND_HOST: str = "127.0.0.1"
    BACKEND_PORT: int = 8000
    CORS_ORIGINS: List[str] = Field(
        default_factory=lambda: ["http://localhost:5173", "http://127.0.0.1:5173"]
    )

    DATABASE_URL: str = "postgresql+psycopg://postgres:postgres@127.0.0.1:55432/farmwise"
    PG_PORT: int = 55432

    SECRET_KEY: str = ""
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 15
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    RATE_LIMIT_DEFAULT: str = "120/minute"
    RATE_LIMIT_AUTH: str = "20/minute"

    ML_MODEL_DIR: str = str(PROJECT_ROOT / "ml" / "models")
    ML_EVALUATION_DIR: str = str(PROJECT_ROOT / "ml" / "evaluation")
    ML_DATASET_PATH: str = str(PROJECT_ROOT / "ml" / "datasets" / "Crop_recommendation.csv")

    PAYMENT_PROVIDER: str = "dev_pending"

    @field_validator("CORS_ORIGINS", mode="before")
    @classmethod
    def parse_cors(cls, value: str | List[str]) -> List[str]:
        if isinstance(value, str):
            return [item.strip() for item in value.split(",") if item.strip()]
        return value

    @field_validator("SECRET_KEY", mode="after")
    @classmethod
    def validate_secret_key(cls, value: str, info) -> str:
        env = (info.data.get("APP_ENV") or "development").lower()
        placeholder = "replace-with-a-64-char-cryptographically-random-secret-before-production"
        if not value or value == placeholder:
            if env == "production":
                raise ValueError("SECRET_KEY must be explicitly set to a strong secret in production.")
            return _DEV_FALLBACK_SECRET
        if env == "production" and len(value) < 32:
            raise ValueError("SECRET_KEY must be at least 32 characters in production.")
        return value


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    """Return cached settings instance."""
    return Settings()
