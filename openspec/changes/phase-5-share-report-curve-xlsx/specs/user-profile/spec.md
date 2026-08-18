## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Body form sends changedAt with save
Saving on `/profile/body` MUST include the datetime-local value as `changedAt` in `PUT /api/v1/profile` so history uses the user-chosen instant.

#### Scenario: Save includes changedAt
- **GIVEN** authenticated user is on `/profile/body`
- **WHEN** the user saves with a chosen 资料真实日期
- **THEN** the PUT body includes `changedAt`
