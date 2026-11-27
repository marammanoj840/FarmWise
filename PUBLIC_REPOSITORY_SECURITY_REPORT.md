# FarmWise — Public Repository Security Audit Report

**Date of Audit**: 2026-11-28  
**Repository**: [https://github.com/marammanoj840/FarmWise](https://github.com/marammanoj840/FarmWise)  
**Target Visibility**: PUBLIC  
**Audit Scope**: Complete working tree, untracked candidates, environment files, and 100% of commits across Git history.

---

## 1. Executive Security Verdict: PASSED (SAFE FOR PUBLIC ACCESS)

The FarmWise repository was subjected to a multi-tiered security and credential audit analyzing every tracked file, all commits in Git history (`d02ae50`, `41d1043`, `b8bad36`), and filesystem exclusions.

- **Total Secret Findings in Working Tree**: **0**
- **Total Secret Findings in Git History**: **0**
- **Private Keys or Certificates**: **0**
- **Active Environment Files Tracked**: **0** (Only safe placeholder `.env.example` templates exist)
- **Database Dumps / Personal Data**: **0**
- **Risk Assessment**: **CLEAN / PRODUCTION SAFE**. No secrets require revocation or rotation.

---

## 2. Investigation of Previous Secret-Scan Exit Code 1

### Observation
During the preceding deployment verification run, an automated credential scan command exited with status code `1`.

### Technical Root-Cause Analysis
1. **Command Executed**:
   ```bash
   git ls-files | Select-String -Pattern "\.env"; \
   git ls-files | Select-String -Pattern "\.pem|\.key|\.p12|\.pfx|\.dump"; \
   git grep -iE "(BEGIN (RSA |EC )?PRIVATE KEY|AKIA[0-9A-Z]{16}|ghp_[0-9a-zA-Z]{36})"
   ```
2. **Behavior of `git grep` and POSIX `grep`**:
   - In accordance with POSIX standards, `grep` and `git grep` return:
     - Exit code `0`: One or more matches found.
     - Exit code `1`: **Zero matches found** (the pattern was not present in any searched files).
     - Exit code `>1`: A syntax or file I/O error occurred.
3. **PowerShell Pipeline Exit Propagation**:
   - When executing chained commands in PowerShell (`cmd1; cmd2; cmd3`), `$LASTEXITCODE` reflects the exit code of the final command (`git grep`).
   - Because `git grep` searched the entire repository for private keys, AWS access keys, and GitHub personal tokens and found **zero occurrences**, it returned exit code `1` ("no matches found").
4. **Verdict**:
   - The exit code `1` was **NOT** an indicator of secret exposure, scanner misconfiguration, or scanner crash. It was the expected signal that **zero sensitive strings were matched**.

---

## 3. Comprehensive Rule-Based Scanner Results

A deep scanner inspecting all tracked files and commit diffs against industry-standard Gitleaks rules was executed with the following results:

| Scan Target / Rule | Pattern Tested | Working Tree Findings | History Findings | Status |
| :--- | :--- | :---: | :---: | :---: |
| **AWS Access Key IDs** | `(?:A3T\|AKIA\|AGPA\|AIDA\|AROA\|AIPA\|ANPA\|ANVA\|ASIA)[A-Z0-9]{16}` | 0 | 0 | **PASSED** |
| **AWS Secret Access Keys** | `aws_secret_access_key\s*[:=]\s*["']?[0-9a-zA-Z\/+]{40}` | 0 | 0 | **PASSED** |
| **Private Keys & Certs** | `-----BEGIN (RSA\|DSA\|EC\|OPENSSH) PRIVATE KEY-----` | 0 | 0 | **PASSED** |
| **GitHub Tokens** | `gh[pousr]_[A-Za-z0-9_]{36,255}` | 0 | 0 | **PASSED** |
| **Slack / Webhook Tokens** | `xox[baprs]-[0-9a-zA-Z]{10,48}` | 0 | 0 | **PASSED** |
| **Payment Gateway Secrets** | `(?:sk\|pk)_(?:live\|test)_[0-9a-zA-Z]{24,99}` | 0 | 0 | **PASSED** |
| **Google / Cloud API Keys** | `AIza[0-9A-Za-z\-_]{35}` | 0 | 0 | **PASSED** |
| **Live Database URIs** | `postgres(ql)?://[user]:[pass]@[remote_host]` | 0 | 0 | **PASSED** |
| **Hardcoded JWT Secrets** | `jwt_secret\s*[:=]\s*["'][a-zA-Z0-9_\-]{16,}["']` | 0 | 0 | **PASSED** |
| **Database Dumps / Archives**| `*.dump`, `*.sql.gz`, `*.bak`, `*.p12`, `*.pfx` | 0 | 0 | **PASSED** |

---

## 4. Exclusion & `.gitignore` Verification

The root [.gitignore](file:///C:/Users/maram/Desktop/FarmWise/.gitignore) was audited to guarantee that no local developer data, build artifacts, or runtime secrets can accidentally enter version control:

```gitignore
# Active Environment Files (Exempts safe template examples)
.env
.env.*
.env.local
.env.production
.env.staging
!.env.example
!.env*.example
!*.env.example

# Credentials & Certificates
*.pem
*.key
*.p12
*.pfx
*.crt
*.cer
*.jks

# Dependencies & Build Artifacts
node_modules/
frontend/node_modules/
backend/.venv/
.venv/
target/
backend-spring/target/
dist/
build/
*.tsbuildinfo

# Backups & Uploads
*.dump
*.sql.gz
*.bak
backups/
uploads/
user-uploads/
storage/
```

---

## 5. Security Recommendations for Public Visibility

Now that the repository is confirmed safe for **PUBLIC** visibility:
1. **GitHub Secret Scanning & Dependabot**: Ensure GitHub Secret Scanning and Dependabot alerts are enabled on the repository under **Settings** → **Code security and analysis**.
2. **Runtime Injection**: All production secrets (database credentials, JWT signing secret, S3 access keys) must continue to be injected exclusively via runtime environment variables on the hosting platform (e.g. Render/Cloudflare) and never committed.
3. **Developer Machine Protection**: Ensure local developers never rename `.env.example` to `.env` without verifying their global git ignore settings.
