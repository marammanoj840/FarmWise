"""Role-based access control definitions and permission helpers."""

from enum import StrEnum


class RoleName(StrEnum):
    ADMIN = "admin"
    FARMER = "farmer"
    CUSTOMER = "customer"


ROLE_PERMISSIONS: dict[str, set[str]] = {
    RoleName.ADMIN: {
        "users:read",
        "users:manage",
        "farmers:verify",
        "products:moderate",
        "categories:manage",
        "orders:manage",
        "reviews:moderate",
        "analytics:read",
        "audit:read",
        "settings:manage",
        "notifications:broadcast",
    },
    RoleName.FARMER: {
        "farms:manage_own",
        "crops:manage_own",
        "activities:manage_own",
        "resources:manage_own",
        "finance:manage_own",
        "recommendations:run",
        "products:manage_own",
        "fulfillments:manage_own",
        "notifications:read_own",
    },
    RoleName.CUSTOMER: {
        "marketplace:browse",
        "cart:manage_own",
        "wishlist:manage_own",
        "addresses:manage_own",
        "orders:manage_own",
        "reviews:create_verified",
        "notifications:read_own",
    },
}


def has_permission(role_name: str, permission: str) -> bool:
    """Return True if role grants the requested permission."""
    return permission in ROLE_PERMISSIONS.get(role_name, set())
