# user-profile

## Purpose

Profile tab: body data and account security as separate tertiary pages behind a level-2 option list.

## Requirements

### Requirement: Profile page separates account and body data
The Profile tab SHALL NOT place body fields, password change, and account deletion on the same screen. `/profile` MUST be a level-2 option list. 身体资料 MUST live at `/profile/body` (nickname, height, weight, **资料真实日期**, save, **and the growth curve below the form**). 账号安全 MUST live at `/profile/account` (change password, delete account). Saving body data MUST NOT submit password fields. Change-password controls MUST remain available on the account page. The datetime control MUST default to now.

#### Scenario: Option list on profile hub
- **GIVEN** an authenticated user opens `/profile`
- **THEN** the page shows options 身体资料, 账号安全, 退出登录
- **AND** height/weight inputs are not visible
- **AND** 修改密码 is not visible

#### Scenario: Body page is tertiary
- **GIVEN** an authenticated user is on `/profile`
- **WHEN** the user chooses 身体资料
- **THEN** the URL is `/profile/body`
- **AND** 保存资料 is visible
- **AND** password fields are not visible

#### Scenario: Account page is tertiary
- **GIVEN** an authenticated user is on `/profile`
- **WHEN** the user chooses 账号安全
- **THEN** the URL is `/profile/account`
- **AND** 修改密码 controls are visible
- **AND** 注销账号 is visible
- **AND** height/weight inputs are not visible

#### Scenario: Back from tertiary returns to options
- **GIVEN** an authenticated user is on `/profile/body` or `/profile/account`
- **WHEN** the user clicks 返回
- **THEN** the URL is `/profile`
- **AND** the three options are visible again

#### Scenario: Body page has real datetime defaulting to now and curve below
- **GIVEN** an authenticated user is on `/profile/body`
- **THEN** a 资料真实日期 datetime control is visible
- **AND** its value is the current local datetime (same calendar day)
- **AND** the growth curve region is below the save form

### Requirement: Profile hub logout does not open a form
Choosing 退出登录 on `/profile` MUST clear the local token and return to the unauthenticated record shell without requiring the account page.

#### Scenario: Logout from options
- **GIVEN** an authenticated user is on `/profile`
- **WHEN** the user clicks 退出登录
- **THEN** the local token is cleared
- **AND** the user is not required to open 账号安全 first

### Requirement: Admin CMS entry stays behind ADMIN on account page
The SPA MUST show the CMS entry (「后台管理」) only when the current session role is ADMIN, on `/profile/account`. The login page MUST NOT regain an unauthenticated CMS shortcut.

#### Scenario: ADMIN sees CMS on account page
- **GIVEN** an authenticated ADMIN opens `/profile/account`
- **THEN** a 后台管理 link to `/cms` is visible

#### Scenario: USER does not see CMS on account page
- **GIVEN** an authenticated non-admin user opens `/profile/account`
- **THEN** 后台管理 is not shown

### Requirement: Saving body profile writes change history
Saving nickname, height, or weight on `/profile/body` MUST result in a persisted history snapshot when values change, so later export and trends can resolve body data as of a record time. The profile GET/PUT contract for current values remains; this requirement adds the side effect of history and the 资料真实日期 field.

#### Scenario: Body save is reflected in trends history
- **GIVEN** authenticated user is on `/profile/body`
- **WHEN** the user saves height `175` and weight `70`
- **THEN** a subsequent `GET /api/v1/profile/trends` includes those values in `bodyHistory`

### Requirement: Body form sends changedAt with save
Saving on `/profile/body` MUST include the datetime-local value as `changedAt` in `PUT /api/v1/profile` so history uses the user-chosen instant.

#### Scenario: Save includes changedAt
- **GIVEN** authenticated user is on `/profile/body`
- **WHEN** the user saves with a chosen 资料真实日期
- **THEN** the PUT body includes `changedAt`
