## MODIFIED Requirements

### Requirement: App uses three-level information architecture
The SPA SHALL present a three-level hierarchy. Level 1 MUST be the bottom tabs 记录, 日历, 我的. Level 2 MUST be the tab's choice surface (record type picker, calendar week strip, profile option list). Level 3 MUST be a concrete form or read-only detail. Browser back from level 3 MUST return to that tab's level 2, not dump the user onto an unrelated tab.

#### Scenario: Record tab already follows the hierarchy
- **GIVEN** an authenticated user is on `/`
- **WHEN** the user clicks 开始记录 then 消耗
- **THEN** the consume form (level 3) is shown
- **AND** browser back returns to the type picker (level 2)

#### Scenario: Profile tab starts at level 2
- **GIVEN** an authenticated user opens `/profile`
- **THEN** the page shows options 身体资料, 账号安全, 报告记录, 退出登录
- **AND** the page does not show height/weight inputs or password fields on the same view

## ADDED Requirements

### Requirement: Shell header shows account or login
The C-end `AppShell` header MUST show the current username on the top-right when a session exists. When unauthenticated, the top-right MUST show 「登录」 that navigates to `/login?redirect=` plus the current pathname. The username control MAY navigate to `/profile`. Public `/report/:id` and `/cms` remain outside this shell.

#### Scenario: Logged-in header shows username
- **GIVEN** local session username is `alice`
- **WHEN** the user is on `/`
- **THEN** the header shows `alice`
- **AND** it does not show 「登录」 as the account control

#### Scenario: Logged-out header prompts login
- **GIVEN** no token in local storage
- **WHEN** the user is on `/`
- **THEN** the header shows 登录
- **WHEN** the user activates 登录
- **THEN** the URL is `/login?redirect=/`
