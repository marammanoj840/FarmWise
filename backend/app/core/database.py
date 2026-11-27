"""SQLAlchemy 2.0 database engine, session management, and declarative base."""

from collections.abc import Generator

from sqlalchemy import MetaData, create_engine, event
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker

from app.core.config import get_settings

NAMING_CONVENTION = {
    "ix": "ix_%(column_0_label)s",
    "uq": "uq_%(table_name)s_%(column_0_name)s",
    "ck": "ck_%(table_name)s_%(constraint_name)s",
    "fk": "fk_%(table_name)s_%(column_0_name)s_%(referred_table_name)s",
    "pk": "pk_%(table_name)s",
}


class Base(DeclarativeBase):
    """Base ORM class with deterministic PostgreSQL constraint naming."""

    metadata = MetaData(naming_convention=NAMING_CONVENTION)


def build_engine(database_url: str | None = None):
    """Create a configured SQLAlchemy engine for PostgreSQL or test DB."""
    url = database_url or get_settings().DATABASE_URL
    is_sqlite = url.startswith("sqlite")
    connect_args = {"check_same_thread": False} if is_sqlite else {}
    engine = create_engine(
        url,
        pool_pre_ping=True,
        future=True,
        connect_args=connect_args,
    )
    if is_sqlite:
        @event.listens_for(engine, "connect")
        def _set_sqlite_pragma(dbapi_connection, _connection_record):
            cursor = dbapi_connection.cursor()
            cursor.execute("PRAGMA foreign_keys=ON")
            cursor.close()
    return engine


_engine = None
_SessionLocal = None


def get_engine():
    """Return lazily initialized singleton engine."""
    global _engine, _SessionLocal
    if _engine is None:
        _engine = build_engine()
        _SessionLocal = sessionmaker(bind=_engine, autocommit=False, autoflush=False, expire_on_commit=False)
    return _engine


def get_session_factory():
    """Return lazily initialized sessionmaker."""
    get_engine()
    return _SessionLocal


def reset_engine(database_url: str | None = None) -> None:
    """Rebind global engine/sessionmaker (used in tests or runtime reconfiguration)."""
    global _engine, _SessionLocal
    if _engine is not None:
        _engine.dispose()
    _engine = build_engine(database_url)
    _SessionLocal = sessionmaker(bind=_engine, autocommit=False, autoflush=False, expire_on_commit=False)


def get_db() -> Generator[Session, None, None]:
    """FastAPI dependency yielding a transactional database session."""
    SessionLocal = get_session_factory()
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
