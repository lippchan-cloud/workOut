## ADDED Requirements

### Requirement: Profile page separates account and body data
The Profile page SHALL present two distinct sections: 账号 (username, change password, logout, delete account) and 身体数据 (nickname, height, weight). Saving body data MUST NOT submit the password fields. Change-password controls MUST be present (required for phase 2).

#### Scenario: Two sections rendered
- **GIVEN** an authenticated user opens `/profile`
- **THEN** the page shows a 账号 section and a 身体数据 section
- **AND** 修改密码 controls are visible
- **AND** 保存资料 is in the body-data section

### Requirement: User can delete own account and data
An authenticated user SHALL delete their own account via `DELETE /api/v1/auth/me` (or equivalent under `/api/v1`). The operation MUST require an explicit confirmation in the UI. On success the server MUST remove or logically hide that user's daily records, profile, and user row so they cannot log in and their records MUST NOT appear in another user's queries. Identity MUST come from the JWT. The SPA MUST clear the local token and return to the unauthenticated shell.

#### Scenario: Delete account requires confirmation
- **GIVEN** authenticated user is on `/profile`
- **WHEN** user clicks 注销账号
- **THEN** the UI asks for confirmation
- **AND** the delete API is not called until confirmed

#### Scenario: Delete account removes own data
- **GIVEN** user A has a profile and at least one daily record
- **WHEN** user A confirms account deletion
- **THEN** response `code` is 200
- **AND** login as user A fails
- **AND** user B listing records does not receive user A's rows
- **AND** the SPA token is cleared
