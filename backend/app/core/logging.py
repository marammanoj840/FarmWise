"""Structured JSON logging for FarmWise backend."""

from datetime import datetime, timezone
import json
import logging
import sys
from typing import Any

REDACTED_KEYS = {"password", "token", "access_token", "refresh_token", "secret", "authorization"}


class JsonFormatter(logging.Formatter):
    """Formats log records as single-line JSON with secret redaction."""

    def format(self, record: logging.LogRecord) -> str:
        payload: dict[str, Any] = {
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
        }
        if hasattr(record, "request_id"):
            payload["request_id"] = record.request_id
        if hasattr(record, "user_id"):
            payload["user_id"] = record.user_id
        if hasattr(record, "extra_data") and isinstance(record.extra_data, dict):
            sanitized = {
                k: ("***REDACTED***" if k.lower() in REDACTED_KEYS else v)
                for k, v in record.extra_data.items()
            }
            payload["data"] = sanitized
        if record.exc_info:
            payload["exception"] = self.formatException(record.exc_info)
        return json.dumps(payload, default=str)


def configure_logging(level: str = "INFO") -> None:
    """Configure root logger for structured output."""
    root = logging.getLogger()
    root.setLevel(getattr(logging, level.upper(), logging.INFO))
    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(JsonFormatter())
    root.handlers.clear()
    root.addHandler(handler)
    logging.getLogger("uvicorn.access").setLevel(logging.WARNING)
