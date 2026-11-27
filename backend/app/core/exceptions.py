"""Domain-specific HTTP exceptions and safe error response helpers."""

from fastapi import HTTPException, status


class FarmWiseError(HTTPException):
    """Base application exception with consistent status code and message."""

    def __init__(
        self,
        detail: str,
        status_code: int = status.HTTP_400_BAD_REQUEST,
        code: str = "bad_request",
    ) -> None:
        super().__init__(status_code=status_code, detail=detail)
        self.code = code


class AuthenticationError(FarmWiseError):
    def __init__(self, detail: str = "Invalid or expired authentication credentials.") -> None:
        super().__init__(detail=detail, status_code=status.HTTP_401_UNAUTHORIZED, code="unauthorized")


class AuthorizationError(FarmWiseError):
    def __init__(self, detail: str = "You do not have permission to perform this action.") -> None:
        super().__init__(detail=detail, status_code=status.HTTP_403_FORBIDDEN, code="forbidden")


class NotFoundError(FarmWiseError):
    def __init__(self, detail: str = "Requested resource was not found.") -> None:
        super().__init__(detail=detail, status_code=status.HTTP_404_NOT_FOUND, code="not_found")


class ConflictError(FarmWiseError):
    def __init__(self, detail: str = "Resource conflict.") -> None:
        super().__init__(detail=detail, status_code=status.HTTP_409_CONFLICT, code="conflict")


class ServiceUnavailableError(FarmWiseError):
    def __init__(self, detail: str = "Dependent service is temporarily unavailable.") -> None:
        super().__init__(detail=detail, status_code=status.HTTP_503_SERVICE_UNAVAILABLE, code="unavailable")
