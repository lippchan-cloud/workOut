## Context

Phase 1 delivered a personal consume/intake ledger with JWT isolation, calendar/CSV, profile, and a **temporary unauthenticated** CMS account list. The product is a private-repo monolith (`frontend/` + `backend/`, one process). Phase 2 closes the CMS public hole and makes the ledger safe for real people: edit/delete records, honest calendar states, form validation, password change, and optional self-delete.

Constraints from the user:
- No multi-env / multi-stage config; secrets may stay in existing yml
- No HTTPS/reverse proxy (ops)
- No org/SSO/LDAP
- No calories/action library/social/charts
- No extra logback file logging
- Do not git commit
- Minimal admin role (USER/ADMIN only)
- TDD: red evidence then green; `java-architecture-master` on all Java changes; no N+1

Existing tables are already prefixed `work_out_*` (Flyway V2). `work_out_daily_record.deleted` already exists.

## Goals / Non-Goals

**Goals:**
- ADMIN-gated CMS API and SPA
- Record PUT/DELETE scoped by JWT userId
- Calendar loading / error-retry / empty as three UI states
- Calendar backfill + list edit/delete with confirm
- Live form validation, 401 draft/redirect
- Profile: password change required; account delete recommended; account vs body sections
- Bootstrap first admin via yml usernames

**Non-Goals:**
- Splitting application.yml into dev/stage/prod or forcing all secrets to env
- Full IAM, teams, SSO
- Physical delete vs logical delete bikeshedding beyond “hidden from lists”
- Changing visual language (keep green consume / red intake / large record CTA)

## Decisions

### D1: Role storage — column on `work_out_user`, not a new IAM module

**Choice:** `role VARCHAR(16) NOT NULL DEFAULT 'USER'` (`USER` | `ADMIN`). Flyway V3 `ALTER TABLE`.

**Alternatives:** Separate `work_out_role` / join table. Rejected as overkill for two roles.

**Rationale:** Smallest change that lets CMS require ADMIN.

### D2: First admin — yml bootstrap usernames, promote on register/login

**Choice:** `workout.admin.usernames` (comma-separated) in existing `application.yml`. `AuthService.register` / `login` sets `ADMIN` when username matches (case-sensitive, trimmed). Tests override via `@TestPropertySource` or `application-test.yml`.

**Alternatives:** SQL seed row with a hardcoded password. Rejected: password would rot and tests would fight a global admin. Manual DB update is still possible but not the primary path.

**Rationale:** Private repo already keeps config in yml; no extra IAM UI.

### D3: Admin authorization — authenticated + service-side ADMIN check

**Choice:** Remove `GET /api/v1/admin/accounts` `permitAll`. `/api/v1/admin/**` requires authentication. `AdminAccountController` loads the current user once and rejects non-ADMIN with HTTP 403 (`ForbiddenException`). Listing still batch-loads users + profiles (existing `findByUserIdIn`).

**Alternatives:** Encode role only in JWT and trust the claim. Rejected as source of truth: role changes would wait for token expiry. Still **include `role` in login/register JSON** (and optionally JWT) so the SPA can hide CMS chrome.

**Rationale:** One extra user-by-id read on CMS, not N+1.

### D4: Record update/delete — same controller, logical delete, 404 for cross-user

**Choice:** `PUT /api/v1/dailyRecords/{id}` and `DELETE /api/v1/dailyRecords/{id}` on `DailyRecordController`. Load by `id + userId + deleted=false` in one query. Cross-user and missing both return 404. DELETE sets `deleted=true` (column already there).

**Alternatives:** Physical delete; 403 on cross-user. 403 would confirm the id exists; 404 matches isolation.

### D5: Account deletion — transactional cascade in AuthService

**Choice:** `DELETE /api/v1/auth/me`. In one transaction: batch-delete (or mark) current user’s daily records, delete profile row, delete user row. Use repository `deleteByUserId` / `deleteAllInBatch` — never loop `deleteById`.

**Alternatives:** Soft-delete user only. Hard delete is clearer for “注销删本人数据” on a personal ledger.

### D6: Calendar failure vs empty

**Choice:** Frontend `loadStatus: loading | success | error`. Success + empty list → empty copy. Error → retry. **Never** `catch(() => setList([]))`.

### D7: Backfill and edit routes

**Choice:** Keep three-step hub. Add `/record?date=YYYY-MM-DD` (type picker) and forms `/record/consume?date=` / `/record/edit/:id`. Calendar 「补记」 → type picker with date; 「编辑」 → edit form.

### D8: 401 draft

**Choice:** `sessionStorage` key `workout_record_draft` `{ type, content, recordedAt, path }` written before redirect in `api` client or form catch; restored on form mount then cleared.

### D9: TDD strategy

**Choice:** Backend MockMvc first for each API; then Vitest for Cms/Calendar/Record/Profile/Login. Existing `AdminAccountsListTest` unauthenticated-200 cases MUST be rewritten (they become RED for the new contract). Evidence in `doc/workOut-TDD验证记录.md`. Java public methods: class/method JavaDoc, call-site comments, `log.info` entry + entity (java-architecture-master).

**Package sketch (unchanged layout):**
```
com.workout
  config (SecurityConfig drops CMS permitAll; ForbiddenException handler)
  modules.auth (role, password change, delete me, admin bootstrap)
  modules.admin (authenticated ADMIN list)
  modules.record (update/delete)
  modules.profile (unchanged upsert; deleted with account)
```

**DDL sketch:**
```sql
ALTER TABLE work_out_user
  ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'USER';
```

## Risks / Trade-offs

- [Existing tests assume anonymous CMS 200] → Rewrite those tests first in the TDD cycle; do not leave dual contracts.
- [Bootstrap username typo leaves zero admins] → Document yml key; login of matching username self-heals role.
- [Logical delete vs hard delete on records] → Lists already filter `deleted=false`; account delete will physically remove rows so CMS/user table stays clean.
- [JWT without role claim] → SPA stores `workout_role` from login/register response; CMS still 403-checks server-side.

## Migration Plan

1. Flyway V3 add `role` (default USER) — existing rows become USER
2. Set `workout.admin.usernames` in yml; operator logs in once to become ADMIN
3. Deploy monolith; anonymous CMS immediately 401
4. Rollback: revert V3 only if needed (column default is safe); restoring permitAll would re-open the hole — not recommended

## Open Questions

- None blocking: admin username list lives in existing yml; default test/dev value `lipp` plus test overrides.
