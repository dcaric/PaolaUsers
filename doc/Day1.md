# Day 1 Plan (Topic #17: ReqRes Users)

## Goal
Build the Day 1 backend foundation: persistence, strict XML/JSON validation, and a POST endpoint that stores valid `User` data.

## Sources Used
- `doc/ImplementationPlan.md` (Day 1 section)
- `doc/Description_of_project_assignment.pdf`
- `doc/REST_API_Endpoints_Student_Projects.pdf` (Topic #17 = `https://reqres.in/api/users`)

## Day 1 Scope
1. Configure persistence layer with JPA/Hibernate.
2. Create `User` entity aligned to ReqRes domain:
   - `id` (primary key, auto-increment)
   - `email`, `first_name`, `last_name`, `avatar` (String)
3. Implement REST POST endpoint for creating users.
4. Support both payload formats:
   - `application/json`
   - `application/xml`
5. Add strict validation gateways:
   - XML against XSD
   - JSON against JSON Schema
6. Persist only valid payloads to DB.
7. Return structured validation errors for invalid payloads.

## Database Decision (Day 1)
Use **H2 file-based DB** for Day 1 speed and simplicity (`jdbc:h2:file:./data/paola`), then optionally switch to PostgreSQL later if required.

## What Is JPA (and Why We Use It)
JPA stands for **Jakarta Persistence API** (older name: Java Persistence API). It is used to map Java objects to database tables so we can work with data as classes/entities instead of writing raw SQL everywhere.

In this project, JPA is used to:
- define `UserEntity` as a table mapping,
- save users through `EntityManager`/repository,
- manage transactions and persistence lifecycle via `persistence.xml` + Hibernate (the JPA implementation).

## Step-by-Step Implementation (Completed)

### Step 1. Added required dependencies
**What was done**
- Added Hibernate ORM, H2 driver, JSON Schema validator, JAXB runtime, and Jersey JSON/JAXB media modules in `pom.xml`.

**Why**
- Day 1 needs DB persistence, JSON/XSD validation, and clean JSON API responses.

### Step 2. Configured JPA persistence unit
**What was done**
- Configured `src/main/resources/META-INF/persistence.xml` with:
  - persistence unit name: `paolaPU`
  - provider: Hibernate
  - DB: `jdbc:h2:file:./data/paola`
  - schema strategy: `hibernate.hbm2ddl.auto=update`

**Why**
- This creates a persistent local database and auto-creates/updates tables for quick Day 1 delivery.

### Step 3. Implemented `User` persistence model
**What was done**
- Added `UserEntity` with fields:
  - `id` (auto-generated PK)
  - `email`, `firstName`, `lastName`, `avatar`
- Added `JpaUtil` and `UserRepository` for `EntityManager` lifecycle and transactions.

**Files**
 - `src/main/java/com/paola/paolarestapi/users/persistence/UserEntity.java`
 - `src/main/java/com/paola/paolarestapi/users/persistence/JpaUtil.java`
 - `src/main/java/com/paola/paolarestapi/users/repository/UserRepository.java`

**Why**
- Keeps persistence logic separate from endpoint logic and ensures transaction safety.

### Step 4. Created transport model for JSON + XML
**What was done**
- Added `UserPayload` annotated for both Jackson (JSON) and JAXB (XML).

**File**
- `src/main/java/com/paola/paolarestapi/users/model/UserPayload.java`

**Why**
- One shared model avoids duplicate parsing logic and keeps request formats aligned.

### Step 5. Added validation schemas
**What was done**
- Added XSD schema for XML payload validation.
- Added JSON Schema (Draft-07) for JSON payload validation.

**Files**
- `src/main/resources/schemas/user.xsd`
- `src/main/resources/schemas/user.schema.json`

**Why**
- This enforces strict structural and field-level checks before any DB write.

### Step 6. Implemented validation services
**What was done**
- `XmlValidationService`:
  - validates payload against `user.xsd`
  - parses valid XML into `UserPayload`
- `JsonValidationService`:
  - validates payload against `user.schema.json`
  - parses valid JSON into `UserPayload`

**Files**
- `src/main/java/com/paola/paolarestapi/users/service/XmlValidationService.java`
- `src/main/java/com/paola/paolarestapi/users/service/JsonValidationService.java`

**Why**
- Validation is now reusable and isolated from JAX-RS resource code.

### Step 7. Implemented error + success DTOs and mapper
**What was done**
- Added structured validation DTO:
  - `ValidationViolation` (`field`, `rule`, `detail`)
  - `ErrorResponse` (`message`, `violations`)
- Added success DTO `UserCreatedResponse`.
- Added `UserMapper` for payload/entity/response transformations.

**Files**
- `src/main/java/com/paola/paolarestapi/users/dto/ValidationViolation.java`
- `src/main/java/com/paola/paolarestapi/users/dto/ErrorResponse.java`
- `src/main/java/com/paola/paolarestapi/users/dto/UserCreatedResponse.java`
- `src/main/java/com/paola/paolarestapi/users/service/UserMapper.java`

**Why**
- Keeps API responses consistent and decouples domain mapping from endpoint logic.

### Step 8. Implemented Day 1 endpoint
**What was done**
- Added `POST /api/users` in `UserResource`.
- Supports `application/json` and `application/xml`.
- Behavior:
  - invalid content type -> `415 Unsupported Media Type`
  - schema validation errors -> `400 Bad Request` + structured violations
  - valid payload -> persist to DB and return `201 Created` with saved user

**File**
- `src/main/java/com/paola/paolarestapi/UserResource.java`

**Why**
- This directly satisfies the Day 1 contract: validate first, persist second.

### Step 9. Build verification
**What was done**
- Ran Maven package build with tests skipped:
  - `./mvnw.cmd -q -DskipTests package`
- Build passed.

**Why**
- Confirms all Day 1 wiring compiles and packages successfully.

## File-by-File Explanation (What + How + Why)

### `pom.xml`
This file defines all build dependencies and packaging rules. For Day 1, it now includes Hibernate, H2, JSON Schema validation, JAXB runtime, and Jersey media modules. We need it because without these libraries the project cannot validate payloads, map XML/JSON, or persist users.

### `src/main/resources/META-INF/persistence.xml`
This is the JPA configuration entry point. It defines the `paolaPU` persistence unit, points to the H2 database, and configures schema auto-update. We need it so Hibernate knows how to connect to the database and which entity classes belong to persistence.

### `src/main/resources/schemas/user.xsd`
This file is the XML contract for incoming user payloads. It enforces required fields (`email`, `first_name`, `last_name`, `avatar`) and structural correctness. We need it to reject invalid XML before any database write.

### `src/main/resources/schemas/user.schema.json`
This file is the JSON contract (Draft-07) for the same user payload. It defines required fields, disallows unknown fields, and validates formats such as email. We need it to apply strict schema validation for JSON requests.

### `src/main/java/com/paola/paolarestapi/users/model/UserPayload.java`
This class is the transport model for incoming API data. It uses Jackson annotations for JSON field names and JAXB annotations for XML element names, so one class can parse both formats consistently. We need it to keep JSON/XML handling aligned and avoid duplicate models.

### `src/main/java/com/paola/paolarestapi/users/persistence/UserEntity.java`
This is the database entity mapped to the `users` table. It defines JPA annotations for primary key generation and columns. We need it because persistence must be done through an entity that JPA/Hibernate can manage.

### `src/main/java/com/paola/paolarestapi/users/persistence/JpaUtil.java`
This utility creates and shares the `EntityManagerFactory` from `paolaPU`. It provides `EntityManager` instances for repository operations. We need it to centralize persistence bootstrapping and avoid duplicating JPA initialization logic.

### `src/main/java/com/paola/paolarestapi/users/repository/UserRepository.java`
This class contains database write logic (`save`) with explicit transaction handling (`begin`, `commit`, `rollback`). It isolates persistence details from REST code. We need it so resource classes stay focused on HTTP and validation flow.

### `src/main/java/com/paola/paolarestapi/users/service/XmlValidationService.java`
This service validates XML payloads against `user.xsd` and unmarshals valid XML to `UserPayload` using JAXB. Validation errors are converted into structured violations. We need it to enforce XML correctness and provide readable error details.

### `src/main/java/com/paola/paolarestapi/users/service/JsonValidationService.java`
This service validates JSON payloads against `user.schema.json` using the schema validator library and parses valid payloads into `UserPayload` with Jackson. It maps schema failures to structured violations. We need it for strict JSON validation and safe parsing.

### `src/main/java/com/paola/paolarestapi/users/dto/ValidationViolation.java`
This DTO represents one validation problem (`field`, `rule`, `detail`). It is used by both XML and JSON validation paths. We need it to return consistent machine-readable error entries to clients.

### `src/main/java/com/paola/paolarestapi/users/dto/ErrorResponse.java`
This DTO wraps a high-level error message and a list of `ValidationViolation` entries. It standardizes error responses for invalid requests. We need it so clients can reliably parse and display validation failures.

### `src/main/java/com/paola/paolarestapi/users/dto/UserCreatedResponse.java`
This DTO represents successful POST output with the saved user fields and generated `id`. It keeps API response structure independent from internal JPA entity structure. We need it to provide a clean response contract after persistence.

### `src/main/java/com/paola/paolarestapi/users/service/UserMapper.java`
This class maps data between `UserPayload`, `UserEntity`, and `UserCreatedResponse`. It centralizes transformation rules in one place. We need it to keep mapping logic out of the resource class and make future changes easier.

### `src/main/java/com/paola/paolarestapi/UserResource.java`
This is the Day 1 REST endpoint (`POST /api/users`). It detects content type, runs schema validation, parses payload, saves valid users, and returns either `201` success or structured `400`/`415` errors. We need it because this is the core Day 1 requirement from the assignment.

### `src/main/java/com/paola/paolarestapi/RestApplication.java`
This class bootstraps JAX-RS under `/api` and enables package scanning for resources/services. It replaces `web.xml`-based bootstrap. We need it so the new `UserResource` endpoint is reachable at `/api/users`.

### `src/main/resources/META-INF/beans.xml`
This file enables CDI bean discovery in Jakarta EE containers. It is currently minimal but keeps the project ready for injected services as the implementation expands. We need it for standards-compliant CDI setup and future service wiring.

## API Contract for Day 1
- Endpoint: `POST /api/users`
- Request body: ReqRes-like user payload
- Content types in: JSON, XML
- Success: user persisted, return created resource (or created id)
- Failure: return validation report (use `400 Bad Request`; if professor explicitly mandates `404`, keep that requirement)

## Deliverables by End of Day 1
1. Working endpoint `POST /api/users`.
2. Persistent storage working with JPA.
3. XSD + JSON Schema files committed.
4. Validation errors returned in structured format.
5. Short test evidence:
   - one valid JSON request
   - one valid XML request
   - one invalid request showing violations

## Definition of Done
- Endpoint accepts both XML and JSON.
- Invalid payload never reaches DB.
- Valid payload is persisted and retrievable from DB table.
- Error response clearly lists all validation issues.
- Code compiles and deploys with current WAR setup.

## Day 1 Call Flow (End-to-End)
1. **App bootstrap**
   - `RestApplication` exposes base path `/api`.
   - JPA bootstrap config is loaded from `src/main/resources/META-INF/persistence.xml` using persistence unit `paolaPU`.

2. **HTTP request enters**
   - Client sends `POST /api/users` with `Content-Type: application/json` or `application/xml`.
   - Request is handled by `src/main/java/com/paola/paolarestapi/UserResource.java`.

3. **Content-type routing**
   - `UserResource` checks header and routes validation:
   - JSON -> `JsonValidationService.validate(...)`
   - XML -> `XmlValidationService.validate(...)`

4. **Schema validation**
   - JSON path uses `src/main/resources/schemas/user.schema.json`.
   - XML path uses `src/main/resources/schemas/user.xsd`.
   - Validation issues are converted to `ValidationViolation` objects.

5. **Error path (invalid input)**
   - `UserResource` returns `400 Bad Request` with `ErrorResponse`.
   - No mapping to DB entity happens.
   - No transaction starts, nothing is persisted.

6. **Parse path (valid input)**
   - JSON is parsed to `UserPayload` via Jackson in `JsonValidationService.parse(...)`.
   - XML is parsed to `UserPayload` via JAXB in `XmlValidationService.parse(...)`.

7. **Model mapping**
   - `UserMapper.toEntity(...)` maps `UserPayload` -> `UserEntity`.
   - Field mapping:
   - `payload.email -> entity.email`
   - `payload.firstName -> entity.firstName`
   - `payload.lastName -> entity.lastName`
   - `payload.avatar -> entity.avatar`

8. **Persistence**
   - `UserRepository.save(...)` gets `EntityManager` from `JpaUtil.createEntityManager()`.
   - `JpaUtil` uses `EntityManagerFactory` created from `paolaPU`.
   - Repository flow: `begin -> persist -> commit` (or `rollback` on failure).
   - Hibernate writes to H2 file database configured in `persistence.xml`.

9. **Success response**
   - Saved `UserEntity` (with generated `id`) is mapped by `UserMapper.toCreatedResponse(...)`.
   - `UserResource` returns `201 Created` with `UserCreatedResponse`.
