## REMOVED Requirements

### Requirement: Unauthenticated admin can list all accounts
**Reason**: 一期临时公网口已完成对账；二期必须关闭，避免普通用户或匿名访问拉全站账户。
**Migration**: 使用已登录且角色为 `ADMIN` 的 Bearer JWT 调用同一路径 `GET /api/v1/admin/accounts`。

### Requirement: Temporary CMS page is reachable without login
**Reason**: `/cms` 与账户列表同属管理员能力，不得继续免登录开放。
**Migration**: 未登录访问 `/cms` 必须进入登录（带 `redirect=/cms`）；仅管理员登录后可见入口并打开页面。

## ADDED Requirements

### Requirement: Admin accounts list requires ADMIN JWT
The system SHALL expose `GET /api/v1/admin/accounts` only to an authenticated principal whose role is `ADMIN`. Missing or invalid token MUST yield HTTP 401. A valid `USER` JWT MUST yield HTTP 403 and MUST NOT include any account in `data`. Successful `ADMIN` response envelope MUST be `{ code, msg, data }` with `code` 200. `data.list` MUST contain registered users with `userId`, `username`, `createdAt`, `role`, and profile-visible fields `nickname`, `heightCm`, `weightKg` (null when the user has no profile). The JSON MUST NOT include `passwordHash`, `password`, or any password digest. Listing MUST load users and profiles in batch queries (no per-user repository loop). Identity MUST come from the JWT, never from a client-supplied userId.

#### Scenario: Anonymous request is rejected
- **GIVEN** no Authorization header
- **WHEN** client calls `GET /api/v1/admin/accounts`
- **THEN** response is HTTP 401

#### Scenario: Regular user JWT cannot list accounts
- **GIVEN** user `cms_user` is registered with role `USER` and a valid JWT
- **WHEN** client calls `GET /api/v1/admin/accounts` with that Bearer token
- **THEN** response is HTTP 403
- **AND** the body does not contain `cms_user` as a listed account payload intended for CMS

#### Scenario: Admin JWT lists accounts
- **GIVEN** user `cms_admin` has role `ADMIN` and a valid JWT
- **AND** user `cms_alice` is registered with profile nickname `阿丽`
- **WHEN** client calls `GET /api/v1/admin/accounts` with the admin Bearer token
- **THEN** response is HTTP 200 and `code` is 200
- **AND** `data.list` contains an item whose `username` is `cms_alice` and `nickname` is `阿丽`
- **AND** that item has `userId` and `createdAt`
- **AND** the raw JSON does not contain `passwordHash` or `password`

### Requirement: CMS page is admin-only
The SPA `/cms` route MUST NOT be usable by anonymous users or `USER` role. Unauthenticated visitors MUST be sent to `/login?redirect=/cms`. Authenticated non-admin users MUST see a Chinese denial (not the account table). Admins MUST see the account list columns (用户ID、用户名、创建时间、昵称、身高、体重) and MUST NOT see `passwordHash`. Deep-linking `GET /cms` MUST still fall back to SPA HTML. The login page MUST NOT expose an unauthenticated 「后台管理」 link. After login, only an `ADMIN` session MAY show a CMS entry.

#### Scenario: Anonymous CMS redirects to login
- **GIVEN** no token in local storage
- **WHEN** the user opens `/cms`
- **THEN** the UI navigates to `/login?redirect=/cms`
- **AND** the login page has no link named 「后台管理」

#### Scenario: Regular user cannot see account table
- **GIVEN** a stored token for a `USER` role
- **WHEN** the user opens `/cms`
- **THEN** the page does not render the CMS account table rows from `/api/v1/admin/accounts`
- **AND** the page shows a Chinese message that the user is not an administrator

#### Scenario: Admin can open CMS after login
- **GIVEN** a stored token for an `ADMIN` role
- **WHEN** the user opens `/cms`
- **THEN** the page shows column labels for 用户ID、用户名、创建时间、昵称、身高、体重
- **AND** the page does not contain the text `passwordHash`
