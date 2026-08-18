## ADDED Requirements

### Requirement: CMS deep links stay outside the three-tab shell
CMS routes `/cms`, `/cms/accounts`, `/cms/users/:userId`, and `/cms/reports` MUST render outside `AppShell`. Refreshing those paths MUST still serve SPA HTML (not an API 404). The bottom tabs 记录 / 日历 / 我的 MUST NOT appear on CMS pages.

#### Scenario: CMS accounts deep link serves SPA
- **WHEN** a client `GET /cms/accounts`
- **THEN** the server forwards to SPA `index.html`

#### Scenario: CMS does not show bottom tabs
- **GIVEN** a stored token for an `ADMIN` role
- **WHEN** the user opens `/cms`
- **THEN** the bottom tabs 记录, 日历, 我的 are not shown
