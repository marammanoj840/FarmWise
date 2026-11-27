# FarmWise — Database Backup & Disaster Recovery Guide

This document defines the production backup strategies, point-in-time recovery (PITR) procedures, verification routines, and disaster recovery plan for the FarmWise PostgreSQL database.

---

## 1. Backup Strategy Overview

| Tier | Method | Frequency | Retention Window | Storage Target |
| :--- | :--- | :--- | :--- | :--- |
| **Continuous (PITR)** | WAL (Write-Ahead Log) Archiving | Real-time | 7–30 days | Cloud Provider Managed Storage / Cloud Storage |
| **Automated Daily Snapshot** | Managed Cloud DB Snapshot | Every 24 hours (02:00 UTC) | 30 days | Managed Provider / Geo-redundant storage |
| **Weekly Logical Backup** | `pg_dump` with custom directory format | Every Sunday | 90 days | Encrypted S3 / Cloudflare R2 bucket |
| **Pre-Migration Snapshot** | Manual Snapshot / `pg_dump` | Before applying new Flyway migrations | Retained until verified | Secure cold storage |

---

## 2. Automated Logical Backup Script (`pg_dump`)

Store this script on your database maintenance runner or cron server:

```bash
#!/usr/bin/env bash
set -euo pipefail

# Configuration
TIMESTAMP=$(date -u +"%Y%m%d_%H%M%SZ")
BACKUP_DIR="/tmp/farmwise_backups"
DB_NAME="${POSTGRES_DB:-farmwise}"
DB_USER="${POSTGRES_USER:-farmwise}"
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
S3_BUCKET="${BACKUP_S3_BUCKET:-farmwise-database-backups}"
BACKUP_FILENAME="farmwise_backup_${TIMESTAMP}.dump"

mkdir -p "${BACKUP_DIR}"

echo "[${TIMESTAMP}] Starting logical backup of ${DB_NAME} on ${DB_HOST}..."

# Execute compressed pg_dump in Custom format (-Fc)
PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump \
  -h "${DB_HOST}" \
  -p "${DB_PORT}" \
  -U "${DB_USER}" \
  -d "${DB_NAME}" \
  -Fc \
  -v \
  -f "${BACKUP_DIR}/${BACKUP_FILENAME}"

FILE_SIZE=$(du -h "${BACKUP_DIR}/${BACKUP_FILENAME}" | cut -f1)
echo "[${TIMESTAMP}] Backup completed successfully (${FILE_SIZE}). Uploading to S3..."

# Upload to S3-compatible cold storage with server-side encryption
aws s3 cp "${BACKUP_DIR}/${BACKUP_FILENAME}" "s3://${S3_BUCKET}/${BACKUP_FILENAME}" \
  --sse AES256

echo "[${TIMESTAMP}] Upload complete. Cleaning up local temp file..."
rm -f "${BACKUP_DIR}/${BACKUP_FILENAME}"

echo "[${TIMESTAMP}] Backup process finished successfully."
```

---

## 3. Disaster Recovery & Restoration Procedures

### Scenario A: Restoring from a Managed Cloud Snapshot
1. In your cloud console (Render, AWS RDS, Neon, or Supabase), navigate to **Backups**.
2. Select the desired restore snapshot or point in time.
3. Click **Restore to New Instance**.
4. Once the restored database is provisioned and verified:
   - Update `SPRING_DATASOURCE_URL` in your backend environment variables to point to the new instance.
   - Restart the backend service. Flyway will validate existing migrations (`V1`–`V8`) and ensure data integrity.

### Scenario B: Restoring from a Logical `pg_dump` Archive
To restore into an empty database instance:

```bash
# 1. Download backup archive from S3
aws s3 cp "s3://farmwise-database-backups/farmwise_backup_20261127_120000Z.dump" /tmp/latest.dump

# 2. Terminate active backend connections to the target database
psql -h "${DB_HOST}" -U "${DB_USER}" -d postgres -c \
  "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'farmwise' AND pid <> pg_backend_pid();"

# 3. Drop and recreate database
psql -h "${DB_HOST}" -U "${DB_USER}" -d postgres -c "DROP DATABASE IF EXISTS farmwise;"
psql -h "${DB_HOST}" -U "${DB_USER}" -d postgres -c "CREATE DATABASE farmwise WITH OWNER farmwise ENCODING 'UTF8';"

# 4. Restore schema and data using pg_restore
pg_restore \
  -h "${DB_HOST}" \
  -p "${DB_PORT}" \
  -U "${DB_USER}" \
  -d farmwise \
  -v \
  --no-owner \
  --clean \
  /tmp/latest.dump

# 5. Clean up local file
rm -f /tmp/latest.dump
```

---

## 4. Backup Verification & Test Drill Procedure

A backup that has never been restored is an unproven backup.
Run a quarterly disaster recovery drill:

1. Restore the latest backup into a temporary staging database instance.
2. Run database integrity and row-count checks:
   ```sql
   -- Verify core table counts
   SELECT 'users' AS tbl, count(*) FROM users
   UNION ALL
   SELECT 'farms', count(*) FROM farms
   UNION ALL
   SELECT 'crops', count(*) FROM crops
   UNION ALL
   SELECT 'products', count(*) FROM products
   UNION ALL
   SELECT 'file_objects', count(*) FROM file_objects;
   ```
3. Boot a temporary instance of `farmwise-backend` with `SPRING_PROFILES_ACTIVE=test` connected to the restored staging database.
4. Verify `/api/v1/health` returns status `UP` with `databaseStatus: UP`.
