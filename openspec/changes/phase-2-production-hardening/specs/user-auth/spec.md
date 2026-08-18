## ADDED Requirements

### Requirement: Users have a minimal role USER or ADMIN
Each user row MUST store `role` as `USER` or `ADMIN`. New registrations default to `USER`. The configured bootstrap usernames in `workout.admin.usernames` MUST be promoted to `ADMIN` on register or login (no full IAM). Login and register responses MUST include `role`. JWT parsing for business APIs MAY carry role for convenience, but `/api/v1/admin/**` MUST still verify `ADMIN` against the persisted user (or an equivalent server-side role check). Ordinary registration MUST NOT grant `ADMIN` unless the username is in the bootstrap list.

#### Scenario: Default role is USER
- **GIVEN** username `alice` is not in `workout.admin.usernames`
- **WHEN** client registers `alice`
- **THEN** response `data.role` is `USER`

#### Scenario: Bootstrap username becomes ADMIN
- **GIVEN** `workout.admin.usernames` contains `phase2_admin`
- **WHEN** client registers `phase2_admin`
- **THEN** response `data.role` is `ADMIN`
- **AND** that user can call `GET /api/v1/admin/accounts` successfully

### Requirement: Authenticated user can change password
An authenticated user SHALL change their password via `PUT /api/v1/auth/password` with current password and new password (6–64 chars). Identity MUST come from the JWT. Wrong current password MUST yield HTTP 400 with a Chinese message that does not reveal whether the username exists. Success MUST persist a new password hash (not plaintext). Subsequent login MUST accept the new password and MUST reject the old password.

#### Scenario: Change password succeeds
- **GIVEN** user A is authenticated with password `secret12`
- **WHEN** user A PUTs current `secret12` and new `secret99`
- **THEN** response `code` is 200
- **AND** login with `secret99` succeeds
- **AND** login with `secret12` fails

#### Scenario: Wrong current password rejected
- **GIVEN** user A is authenticated
- **WHEN** user A PUTs a wrong current password
- **THEN** response is HTTP 400 with a Chinese error
- **AND** the stored hash is unchanged
