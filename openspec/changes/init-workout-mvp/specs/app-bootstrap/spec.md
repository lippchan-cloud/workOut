## ADDED Requirements

### Requirement: Single CLI command starts the full app
The project SHALL be startable via one documented CLI command (e.g. `./mvnw spring-boot:run`) that boots Spring Boot serving both the React static build and the `/api/v1` APIs on one port (default `8080`).

#### Scenario: Health after start
- **GIVEN** MySQL is available with required schema
- **WHEN** the CLI start command completes successfully
- **THEN** `http://localhost:8080` serves the SPA shell
- **AND** `/api/v1/auth/login` is reachable

### Requirement: MySQL schema supports multi-user data
The system MUST provide DDL (or migration) for `user`, `daily_record` (with `user_id`), and `profile` (with unique `user_id`), using utf8mb4.

#### Scenario: Schema objects exist
- **GIVEN** migrations/DDL have been applied
- **WHEN** inspecting the database
- **THEN** tables `user`, `daily_record`, and `profile` exist
- **AND** `daily_record.user_id` and `profile.user_id` reference users

### Requirement: Frontend is hosted by Spring Boot
React production assets MUST be packaged/copied into Spring Boot static resources so browsers need not run a separate frontend server for MVP usage.

#### Scenario: SPA fallback
- **GIVEN** the application is running
- **WHEN** browser requests `/calendar`
- **THEN** the SPA HTML is returned (client router can render)
- **AND** API paths under `/api/**` are not shadowed incorrectly
