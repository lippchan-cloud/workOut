## ADDED Requirements

### Requirement: Three-tab navigation shell is always visible
The application SHALL present a bottom (or primary) navigation with three tabs in order: 记录, 日历, 我的. Unauthenticated users MUST still see this shell.

#### Scenario: App load shows nav shell
- **GIVEN** the application is opened at the root URL
- **WHEN** the initial page renders
- **THEN** the three navigation tabs are visible
- **AND** the default landing context is the Record tab shell

### Requirement: Tab routes map to pages
The system SHALL expose routes for Record (`/` or `/record`), Calendar (`/calendar`), Profile (`/profile`), Login (`/login`), and Register (`/register`).

#### Scenario: Authenticated navigation
- **GIVEN** a valid token is stored
- **WHEN** user selects each of the three tabs
- **THEN** the corresponding page content is shown without redirecting to login

### Requirement: Unauthenticated tab click redirects to login
When no valid token is present, selecting a tab MUST redirect to login with `redirect` set to that tab's path instead of loading protected business data.

#### Scenario: Unauthenticated profile tab
- **GIVEN** no token
- **WHEN** user clicks 我的
- **THEN** navigation goes to `/login?redirect=/profile`
- **AND** no profile API call is required before redirect
