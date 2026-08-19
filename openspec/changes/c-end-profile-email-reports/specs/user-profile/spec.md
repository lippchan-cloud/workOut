## MODIFIED Requirements

### Requirement: Profile page separates account and body data
The Profile tab SHALL NOT place body fields, password change, and account deletion on the same screen. `/profile` MUST be a level-2 option list. 身体资料 MUST live at `/profile/body` (nickname, height, weight, **资料真实日期**, save, **and the growth curve below the form**). 账号安全 MUST live at `/profile/account` (change password, delete account, **bind/unbind email**). **报告记录 MUST live at `/profile/reports`.** Saving body data MUST NOT submit password fields. Change-password controls MUST remain available on the account page. The datetime control MUST default to now.

#### Scenario: Option list on profile hub
- **GIVEN** an authenticated user opens `/profile`
- **THEN** the page shows options 身体资料, 账号安全, 报告记录, 退出登录
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

#### Scenario: Reports page is tertiary
- **GIVEN** an authenticated user is on `/profile`
- **WHEN** the user chooses 报告记录
- **THEN** the URL is `/profile/reports`
- **AND** height/weight inputs are not visible
- **AND** 修改密码 is not visible

#### Scenario: Back from tertiary returns to options
- **GIVEN** an authenticated user is on `/profile/body` or `/profile/account` or `/profile/reports`
- **WHEN** the user clicks 返回
- **THEN** the URL is `/profile`
- **AND** the hub options including 报告记录 are visible again

#### Scenario: Body page has real datetime defaulting to now and curve below
- **GIVEN** an authenticated user is on `/profile/body`
- **THEN** a 资料真实日期 datetime control is visible
- **AND** its value is the current local datetime (same calendar day)
- **AND** the growth curve region is below the save form

### Requirement: Profile hub logout does not open a form
Choosing 退出登录 on `/profile` MUST show a confirmation dialog first. Confirming MUST clear the local token and return to the unauthenticated record shell without requiring the account page. Cancelling MUST keep the session and stay on `/profile`.

#### Scenario: Logout from options
- **GIVEN** an authenticated user is on `/profile`
- **WHEN** the user clicks 退出登录 and confirms
- **THEN** the local token is cleared
- **AND** the user is not required to open 账号安全 first

#### Scenario: Logout cancel keeps session
- **GIVEN** an authenticated user is on `/profile`
- **WHEN** the user clicks 退出登录 and cancels
- **THEN** the local token remains
- **AND** the URL remains `/profile`

## ADDED Requirements

### Requirement: Account operations require confirmation
Account-related actions on 「我的」 MUST show a confirmation dialog before the mutating request. Covered actions: 退出登录, 修改密码, 绑定邮箱, 解绑邮箱, 注销账号. Cancelling MUST NOT call the corresponding API.

#### Scenario: Change password cancelled does not submit
- **GIVEN** an authenticated user is on `/profile/account`
- **WHEN** the user submits 修改密码 and cancels the confirmation
- **THEN** no password-change request is sent

#### Scenario: Delete account cancelled does not submit
- **GIVEN** an authenticated user is on `/profile/account`
- **WHEN** the user clicks 注销账号 and cancels
- **THEN** no delete request is sent
