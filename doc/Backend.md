# Backend Overview and Requirements Mapping

## Source Documents Used
- `doc/Description_of_project_assignment.txt`
- `doc/REST_API_Endpoints_Student_Projects.txt`
- Existing implementation docs (`Day1.md`, `Day2.md`, `Day3.md`, `Day4.md`)

## Professor Requirements (Backend Scope)
From the assignment text, backend must cover:
1. REST POST endpoint accepting XML/JSON, validating with XSD + JSON Schema, then saving valid data to DB, and returning validation errors for invalid data.
2. SOAP service that searches by term using XPath over a prepared XML file generated from assigned REST API data.
3. Jakarta XML validation of that prepared XML file and return validation messages.
4. gRPC service for DHMZ weather feed (`https://vrijeme.hr/hrvatska_n.xml`) with partial city matching and returning all matches.
5. Custom REST API integrated with DB, full CRUD (GET/POST/PUT/DELETE), JWT access/refresh auth model, GraphQL, and a switch between public API and custom/local API.

Assigned public endpoint list shows Topic #17 as:
- `https://reqres.in/api/users`

## Current Backend Implementation Mapping

### Requirement 1: REST POST + XML/JSON validation + persistence
Status: Implemented.

Implemented in:
- `src/main/java/com/paola/paolarestapi/UserResource.java`
- `src/main/java/com/paola/paolarestapi/users/service/JsonValidationService.java`
- `src/main/java/com/paola/paolarestapi/users/service/XmlValidationService.java`
- `src/main/resources/schemas/user.schema.json`
- `src/main/resources/schemas/user.xsd`
- `src/main/java/com/paola/paolarestapi/users/repository/UserRepository.java`

Endpoint:
- `POST /api/users`

### Requirement 2: SOAP + XPath search over generated XML
Status: Implemented in backend code.

Implemented in:
- `src/main/java/com/paola/paolarestapi/integration/soap/UserSearchSoapService.java`
- `src/main/java/com/paola/paolarestapi/integration/service/XPathUserSearchService.java`
- XML generation pipeline in `integration/service/*`

Supporting REST helper endpoints:
- `POST /api/integration/import-reqres`
- `GET /api/integration/validate-snapshot`
- `GET /api/integration/search-snapshot?term=...`

### Requirement 3: Jakarta XML validation of prepared file
Status: Implemented.

Implemented in:
- `src/main/java/com/paola/paolarestapi/integration/service/SnapshotValidationService.java`
- Schema: `src/main/resources/schemas/users-snapshot.xsd`

### Requirement 4: gRPC weather service (DHMZ)
Status: Partially implemented (weather logic implemented, transport currently REST, not gRPC server).

Implemented in:
- `src/main/java/com/paola/paolarestapi/weather/WeatherResource.java`
- `src/main/java/com/paola/paolarestapi/weather/service/DhmzWeatherService.java`

Current endpoint:
- `GET /api/weather/temperature?city=...`

Note:
- Partial city matching and multi-match return behavior are implemented.
- A true gRPC server (`.proto` + grpc runtime/service endpoint) is still pending if strict requirement is gRPC transport.

### Requirement 5: Custom REST API + JWT + GraphQL + switch
Status: Implemented (assignment-focused minimal version).

Implemented in:
- `src/main/java/com/paola/paolarestapi/restapi/RestApiResource.java`
- `src/main/java/com/paola/paolarestapi/restapi/service/LocalUserCrudService.java`
- `src/main/java/com/paola/paolarestapi/restapi/service/PublicReqResUserService.java`
- `src/main/java/com/paola/paolarestapi/restapi/service/RestApiSourceSwitchService.java`
- `src/main/java/com/paola/paolarestapi/restapi/service/GraphQlService.java`
- `src/main/java/com/paola/paolarestapi/restapi/service/JwtWriteAccessGuard.java`
- JWT issuer/validator from weather package:
  - `src/main/java/com/paola/paolarestapi/weather/service/JwtTokenService.java`

Endpoints:
- `GET /api/restapi/switch`
- `GET /api/restapi/users`
- `GET /api/restapi/users/{id}`
- `POST /api/restapi/users` (full-access token)
- `PUT /api/restapi/users/{id}` (full-access token)
- `DELETE /api/restapi/users/{id}` (full-access token)
- `POST /api/restapi/graphql` (query read, mutation write with full-access token)

## Data and Persistence Notes
- Local persistence uses H2 file DB via JPA/Hibernate (`jdbc:h2:file:./data/paola`).
- Local DB is populated/updated when these are called:
1. `POST /api/users`
2. `POST /api/integration/import-reqres`
3. `POST /api/restapi/users`
4. `PUT /api/restapi/users/{id}`

- There is no automatic background sync from ReqRes to H2.
- In `REST_API_SOURCE=PUBLIC` mode, `GET /api/restapi/users*` reads from ReqRes; writes still remain local.
- GraphQL currently reads/writes local DB data.

## Security Model in Current Backend
- Token endpoints:
  - `POST /api/weather/auth/login`
  - `POST /api/weather/auth/refresh`
  - `GET /api/weather/auth/validate`
- Roles:
  - `read-only`: intended for read operations
  - `full-access`: required for write operations in custom REST API and GraphQL mutations
- JWT is stateless in current backend (not stored server-side).

## What Is Left / Risk Notes
- If professor requires strict gRPC transport (not REST fallback), add real gRPC layer (`.proto`, gRPC server, weather service binding).
- SOAP runtime publication details may still depend on deployment configuration if WSDL endpoint exposure is part of grading checklist.
- Frontend/Desktop client and full integration flow (assignment step 6) is outside this backend document.

## Quick Backend Verification Checklist
1. `POST /api/users` with valid JSON/XML -> `201`.
2. `POST /api/integration/import-reqres` -> snapshot generated and DB filled.
3. `GET /api/integration/validate-snapshot` -> validation messages list.
4. `GET /api/weather/temperature?city=zag` -> returns multiple Zagreb matches.
5. `POST /api/weather/auth/login` with role `full-access` -> token pair.
6. `GET /api/restapi/switch` -> source mode returned.
7. `POST /api/restapi/users` with Bearer full-access token -> local create works.
8. `POST /api/restapi/graphql` query and mutation -> read/update behavior works.
