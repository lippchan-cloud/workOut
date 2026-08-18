## ADDED Requirements

### Requirement: User can be bound to a DeepSeek API key
The system SHALL persist at most one DeepSeek API key per user in `work_out_user_api_key`. Administrators MUST assign or replace a key via CMS APIs. CMS list/detail MUST show a masked key only (e.g. last 4 characters). Application logs MUST NEVER print the full API key. When a user has no key, share advice generation MUST NOT call DeepSeek.

#### Scenario: Bound key enables AI path
- **GIVEN** user `alice` has an API key assigned by an admin
- **WHEN** `alice` creates a share successfully
- **THEN** the share advice status becomes `PENDING` (or progresses toward `READY` under a stub client)
- **AND** DeepSeek is eligible to be invoked for that user

#### Scenario: Unbound user keeps placeholder
- **GIVEN** user `bob` has no row in `work_out_user_api_key`
- **WHEN** `bob` creates a share successfully
- **THEN** the share advice status is `NONE_KEY`
- **AND** public report advice text indicates 「未配置 API Key」
- **AND** no outbound DeepSeek HTTP call is made

### Requirement: Each API key is rate-limited per hour and per day
Each distinct API key (token) MUST allow at most 10 successful or attempted SHARE_ADVICE calls per calendar hour and at most 100 per calendar day in `Asia/Shanghai`. Counts MUST be computed with SQL aggregation on `work_out_ai_call_log` filtered by `api_key_id` and time window. The system MUST NOT load all log rows into memory to count. Exceeding the limit MUST mark the share advice as `FAILED` with a non-medical rate-limit message and MUST NOT cause the share create HTTP response to fail.

#### Scenario: Hourly limit blocks further calls
- **GIVEN** an API key already has 10 call-log rows in the current Shanghai hour
- **WHEN** another share for a user bound to that key triggers advice generation
- **THEN** no DeepSeek chat completion is invoked
- **AND** the share advice status is `FAILED`
- **AND** the create-share HTTP response remains success if the share itself was persisted

#### Scenario: Daily limit blocks further calls
- **GIVEN** an API key already has 100 call-log rows in the current Shanghai day
- **WHEN** another advice generation is attempted for that key
- **THEN** DeepSeek is not invoked
- **AND** the share advice status is `FAILED`

### Requirement: Share advice is generated asynchronously with compressed context
After a share row is committed, advice generation MUST run asynchronously and MUST NOT block `POST /api/v1/shareReports`. The job MUST load only the owning `userId`'s profile, range records, and body history; compress them (summary / hash in MySQL, no separate vector DB); include that `userId` in the model prompt; and call DeepSeek model `deepseek-chat` at `https://api.deepseek.com` using the user's bound key. The assistant persona MUST follow the physio-scientist skill: lifestyle suggestions for fat loss / weight / health with 「仅供参考」, without overconfident medical diagnosis. On success `adviceStatus` MUST be `READY` and `advice` MUST contain the text. On provider/timeout errors `adviceStatus` MUST be `FAILED` with a Chinese failure message; share create MUST still have returned HTTP 200.

#### Scenario: Async success fills advice
- **GIVEN** a stub DeepSeek client returns text `多喝水，仅供参考`
- **AND** user has a bound API key under rate limits
- **WHEN** the user creates a share
- **THEN** the create response returns before advice is required to be ready
- **AND** after the async job completes, `GET /api/v1/reports/{id}` has `adviceStatus` `READY`
- **AND** `data.advice` contains `多喝水`

#### Scenario: Context is isolated by userId
- **GIVEN** share token belongs to `userId=1`
- **WHEN** the advice job builds the prompt
- **THEN** the prompt includes `userId=1`
- **AND** records from another user MUST NOT appear in the compressed context

#### Scenario: Provider failure does not break share create
- **GIVEN** the DeepSeek client throws on invoke
- **AND** user has a bound API key
- **WHEN** the user creates a share
- **THEN** create share returns HTTP 200 with id/url
- **AND** eventually `adviceStatus` is `FAILED`

### Requirement: AI call logs support CMS filtering
Every advice attempt MUST insert a row into `work_out_ai_call_log` with `userId`, `apiKeyId` (when known), `purpose=SHARE_ADVICE`, `createdAt`, and outcome status. `GET /api/v1/admin/aiCalls` MUST allow filter by `userId` and/or `apiKeyId`, return masked key fields only, require ADMIN JWT (USER 403, anonymous 401), and MUST use SQL filters (no N+1 per row key fetch beyond a batch join).

#### Scenario: Admin lists calls filtered by user
- **GIVEN** an ADMIN JWT
- **AND** call logs exist for `userId=1`
- **WHEN** client calls `GET /api/v1/admin/aiCalls?userId=1`
- **THEN** response is HTTP 200
- **AND** every item in `data.list` has `userId` 1
- **AND** no full API key string appears in the JSON
