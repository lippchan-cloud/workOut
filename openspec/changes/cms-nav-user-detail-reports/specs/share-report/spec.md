## ADDED Requirements

### Requirement: CMS only opens existing share snapshots
Administrators MUST view existing `ShareReport` rows as read-only links to the public `/report/{id}` page. The CMS MUST NOT create a share, impersonate the owner, or rewrite snapshot JSON using the admin session. Opening a listed report MUST use the same public token already stored for that user.

#### Scenario: CMS report link uses the public token
- **GIVEN** a share exists with public token `abcToken` owned by `cms_alice`
- **WHEN** an admin opens the report from CMS
- **THEN** the destination is `/report/abcToken`
- **AND** no new share row is created as a side effect of listing or opening
