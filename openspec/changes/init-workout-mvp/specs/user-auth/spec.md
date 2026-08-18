## ADDED Requirements

### Requirement: User can register with username and password
The system SHALL allow a new user to register with a unique username and a password. Passwords MUST be stored as a one-way hash. On success the system SHALL return a JWT (or equivalent session token payload including `token`, `userId`, `username`).

#### Scenario: Successful registration
- **GIVEN** username `alice` is not registered
- **WHEN** client `POST /api/v1/auth/register` with username `alice` and password meeting length 6–64
- **THEN** response `code` is 200
- **AND** `data.token` is a non-empty JWT string
- **AND** a user row exists with hashed password not equal to plaintext

#### Scenario: Duplicate username rejected
- **GIVEN** username `alice` already exists
- **WHEN** client registers again with username `alice`
- **THEN** response indicates failure with HTTP 400
- **AND** `msg` indicates the username is already registered
- **AND** no additional user row is created

#### Scenario: Invalid username or password rejected
- **WHEN** client registers with empty username or password shorter than 6 characters
- **THEN** response is HTTP 400 with Chinese validation message
- **AND** no user is created

### Requirement: User can log in and receive JWT
The system SHALL authenticate username and password and return a JWT on success. Failed login MUST use a generic Chinese message that does not reveal whether the username exists.

#### Scenario: Successful login
- **GIVEN** user `alice` registered with password `secret12`
- **WHEN** client `POST /api/v1/auth/login` with those credentials
- **THEN** response `code` is 200
- **AND** `data` contains `token`, `userId`, `username`

#### Scenario: Wrong password
- **GIVEN** user `alice` exists
- **WHEN** client logs in with wrong password
- **THEN** response is HTTP 400 or 401 with message equivalent to 「用户名或密码错误」
- **AND** no token is returned

### Requirement: Protected APIs require Bearer JWT
Business APIs under `/api/v1/dailyRecords` and `/api/v1/profile` MUST require a valid `Authorization: Bearer <token>`. Auth endpoints under `/api/v1/auth/**` MUST remain public. Invalid or missing token MUST yield HTTP 401.

#### Scenario: Missing token on business API
- **WHEN** client calls `GET /api/v1/dailyRecords?date=2026-08-18` without Authorization
- **THEN** response is HTTP 401

#### Scenario: Valid token accepted
- **GIVEN** a valid JWT for user A
- **WHEN** client calls a business API with `Authorization: Bearer <token>`
- **THEN** the request is not rejected for authentication reasons

### Requirement: Frontend redirects unauthenticated actions to login
The SPA SHALL treat absence of a stored token as unauthenticated. Clicking any main Tab or triggering save/export/profile-load MUST navigate to `/login` with a `redirect` query param pointing at the intended path. On API 401 the SPA MUST clear token and navigate to login.

#### Scenario: Click Tab without token
- **GIVEN** no token in local storage
- **WHEN** user clicks the Calendar Tab
- **THEN** browser navigates to `/login?redirect=/calendar` (or equivalent calendar path)

#### Scenario: API 401 clears session
- **GIVEN** a stale token stored locally
- **WHEN** a business API returns 401
- **THEN** the SPA clears the token
- **AND** navigates to the login page
