## ADDED Requirements

### Requirement: Current session can be queried
`GET /api/v1/auth/me` MUST require a valid Bearer JWT. Success `data` MUST include `userId`, `username`, `role`, and `email` (null when unbound). Missing or invalid token MUST yield HTTP 401. Identity MUST come from the JWT, never from a client-supplied user id.

#### Scenario: Authenticated me returns username and email
- **GIVEN** user `alice` has a valid JWT and no bound email
- **WHEN** client `GET /api/v1/auth/me` with that Bearer token
- **THEN** response `code` is 200
- **AND** `data.username` is `alice`
- **AND** `data.email` is null

#### Scenario: Me without token is 401
- **WHEN** client `GET /api/v1/auth/me` without Authorization
- **THEN** response is HTTP 401

### Requirement: User can bind email with a 4-digit code
An authenticated user SHALL bind one email via a 4-digit numeric verification code. `POST /api/v1/auth/email/sendCode` with `purpose` `BIND` and target `email` MUST persist a hashed code (never plaintext in HTTP). The code MUST be exactly 4 digits. Binding `POST /api/v1/auth/email/bind` with `email` and `code` MUST succeed only for the JWT user, only when the code is unused and unexpired, and only when that email is not already bound to another user. After bind, `GET /api/v1/auth/me` MUST return that email. Duplicate bind of an occupied email MUST fail with HTTP 400 and a Chinese message.

#### Scenario: Bind email after valid code
- **GIVEN** authenticated user `alice` has no email
- **AND** a BIND code was sent to `alice@example.com`
- **WHEN** client `POST /api/v1/auth/email/bind` with that email and the 4-digit code
- **THEN** response `code` is 200
- **AND** subsequent `GET /api/v1/auth/me` has `data.email` `alice@example.com`

#### Scenario: Occupied email cannot be bound
- **GIVEN** user `bob` already bound `taken@example.com`
- **AND** authenticated user `alice` requests BIND code for `taken@example.com`
- **WHEN** `alice` submits bind with a valid code
- **THEN** response is HTTP 400
- **AND** `alice` email remains unbound

#### Scenario: Send code uses 4 digits
- **GIVEN** authenticated user `alice`
- **WHEN** client `POST /api/v1/auth/email/sendCode` with purpose `BIND` and a new email
- **THEN** response `code` is 200
- **AND** the generated code is exactly 4 numeric digits

### Requirement: User can unbind email with a 4-digit code
An authenticated user with a bound email SHALL unbind it using a 4-digit code sent to that email (`purpose` `UNBIND`). After success, `email` on `GET /api/v1/auth/me` MUST be null and email-code login MUST fail until rebound.

#### Scenario: Unbind clears email
- **GIVEN** authenticated user `alice` has email `alice@example.com`
- **AND** an UNBIND code was sent to that email
- **WHEN** client `POST /api/v1/auth/email/unbind` with the 4-digit code
- **THEN** response `code` is 200
- **AND** `GET /api/v1/auth/me` has `data.email` null

### Requirement: User can log in with bound email and 4-digit code
`POST /api/v1/auth/loginByEmail` MUST be public. Given a bound email and a valid unused LOGIN code, the system SHALL return the same token payload as password login (`token`, `userId`, `username`, `role`). Unbound email, wrong code, or expired code MUST fail with a generic Chinese message that does not reveal whether the email is registered. Username+password `POST /api/v1/auth/login` MUST remain available after bind.

#### Scenario: Email code login succeeds
- **GIVEN** user `alice` has bound `alice@example.com`
- **AND** a LOGIN code was sent to that email
- **WHEN** unauthenticated client `POST /api/v1/auth/loginByEmail` with email and the 4-digit code
- **THEN** response `code` is 200
- **AND** `data.username` is `alice`
- **AND** `data.token` is a non-empty JWT

#### Scenario: Unbound email cannot login
- **GIVEN** no user has bound `nobody@example.com`
- **WHEN** client sends LOGIN code or `loginByEmail` for that email
- **THEN** response is HTTP 400 with a generic Chinese failure message
- **AND** no token is returned

#### Scenario: Password login still works after bind
- **GIVEN** user `alice` bound an email
- **WHEN** client `POST /api/v1/auth/login` with username `alice` and the correct password
- **THEN** response `code` is 200
- **AND** `data.token` is present
