## ADDED Requirements

### Requirement: App uses three-level information architecture
The SPA SHALL present a three-level hierarchy. Level 1 MUST be the bottom tabs 记录, 日历, 我的. Level 2 MUST be the tab's choice surface (record type picker, calendar week strip, profile option list). Level 3 MUST be a concrete form or read-only detail. Browser back from level 3 MUST return to that tab's level 2, not dump the user onto an unrelated tab.

#### Scenario: Record tab already follows the hierarchy
- **GIVEN** an authenticated user is on `/`
- **WHEN** the user clicks 开始记录 then 消耗
- **THEN** the consume form (level 3) is shown
- **AND** browser back returns to the type picker (level 2)

#### Scenario: Profile tab starts at level 2
- **GIVEN** an authenticated user opens `/profile`
- **THEN** the page shows three options 身体资料, 账号安全, 退出登录
- **AND** the page does not show height/weight inputs or password fields on the same view

### Requirement: Buttons follow primary secondary and text sizes
Interactive controls SHALL use three visual levels: primary CTA (solid consume-green, intake-red, or accent; min-height 44px), secondary (ghost/outline), and small text controls (week prev/next, inline text actions). The homepage 开始记录 control MUST remain a large hero button with refined radius and type size, and MUST NOT be styled as a small text control. Week prev/next MUST use the small text control style and MUST NOT use the same class/size as the homepage hero or primary block CTA.

#### Scenario: Week switchers are small
- **GIVEN** authenticated user is on the calendar day mode
- **THEN** 上一周 and 下一周 are rendered as small week-nav controls
- **AND** they do not have the `btn-record-hero` or `btn-block` class

#### Scenario: Record hero remains large
- **GIVEN** authenticated user is on `/`
- **THEN** 开始记录 is a large hero button
- **AND** it remains the primary entry into the record type picker
