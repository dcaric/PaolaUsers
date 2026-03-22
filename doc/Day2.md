# Day 2 Plan and Implementation (ReqRes Integration + XML Snapshot + SOAP/XPath)

## Goal
Implement Day 2 interoperability flow:
1. Fetch users from public ReqRes API.
2. Save users to local system database (H2 via existing `users` persistence layer).
3. Generate local XML snapshot with JAXB.
4. Validate snapshot XML against XSD before search.
5. Search snapshot using XPath and expose SOAP-compatible result model.

This keeps the solution simple and assignment-focused, without overengineering.

## What Day 2 Adds (Compared to Day 1)
- Day 1 was request validation + local persistence (`POST /api/users`).
- Day 2 adds protocol bridging:
  - External REST input (`reqres.in`)
  - Local XML document generation (JAXB)
  - XML schema validation (XSD)
  - XPath filtering logic
  - SOAP service contract/implementation for search

## High-Level Architecture
- `users` package:
  - Existing Day 1 persistence components (`UserEntity`, `UserRepository`, JPA config).
- `integration` package:
  - External API models + client.
  - Import orchestration service.
  - XML snapshot model/services.
  - XPath search service.
  - SOAP response/service classes.
  - Helper REST resource for easy testing.

## Endpoints Added for Day 2 Development
Base path: `/api/integration`

ReqRes authentication note:
- ReqRes currently requires `x-api-key` for this endpoint family.
- Backend reads API key from:
  - env var: `REQRES_API_KEY` (recommended)
  - or JVM property: `reqres.api.key`
- If missing, import call fails fast with a clear error.

### How To Set `REQRES_API_KEY` (PowerShell)
Use one of these options before calling `POST /api/integration/import-reqres`.

Option A: current terminal session only
```powershell
$env:REQRES_API_KEY = "YOUR_REAL_KEY"
```

Option B: persist for your user profile (new terminals)
```powershell
[System.Environment]::SetEnvironmentVariable("REQRES_API_KEY", "YOUR_REAL_KEY", "User")
```
After setting persistent variable, restart IntelliJ/Tomcat/terminal so process sees it.

Option C: JVM property (alternative)
Add VM option to your run configuration:
```text
-Dreqres.api.key=YOUR_REAL_KEY
```

1. `POST /api/integration/import-reqres`
- Fetches all ReqRes user pages.
- Saves them to local DB using Day 1 repository.
- Generates `data/reqres-users-snapshot.xml`.
- Validates snapshot against `users-snapshot.xsd`.
- Returns import summary (`ReqResImportResponse`).

2. `GET /api/integration/validate-snapshot`
- Validates current snapshot file.
- Returns list of validation messages (empty = valid).

3. `GET /api/integration/search-snapshot?term=...`
- Performs case-insensitive partial search via XPath over snapshot XML.
- Returns matching `SnapshotUser` list.

## SOAP Capability
Class: `integration.soap.UserSearchSoapService`

Method: `searchUsers(term)`
- Reads snapshot path.
- Validates XML snapshot first.
- If invalid: returns validation messages in SOAP response.
- If valid: performs XPath search and returns `SoapUserResult` list.

Note: this class is implemented and ready; runtime publication details (WSDL endpoint wiring) depend on your server deployment setup.

## Full Call Flow A: Import Pipeline
1. `IntegrationResource.importReqResUsers()` receives REST call.
2. Calls `ReqResImportService.importUsersAndCreateSnapshot()`.
3. `ReqResClientService.fetchAllUsers()`:
   - calls `https://reqres.in/api/users?page=1..N`
   - aggregates all `data[]` items.
4. For each external user:
   - map fields to local `users.persistence.UserEntity`
   - save through `users.repository.UserRepository.save(...)`
5. `XmlSnapshotService.writeSnapshot(users)`:
   - maps ReqRes users to `SnapshotUser`
   - wraps in `UsersSnapshot`
   - marshals with JAXB to `data/reqres-users-snapshot.xml`
6. `SnapshotValidationService.validate(path)` validates generated XML against `users-snapshot.xsd`.
7. Build and return `ReqResImportResponse`.

## Full Call Flow B: Search Pipeline
1. Request enters `GET /api/integration/search-snapshot?term=...`.
2. `XPathUserSearchService.search(path, term)` loads XML document.
3. Builds XPath expression with case-insensitive `contains(...)` checks on:
   - `email`
   - `first_name`
   - `last_name`
   - `avatar`
4. Converts matching `<user>` nodes into `SnapshotUser` objects.
5. Returns list as JSON (REST helper path) or maps to SOAP DTOs in SOAP path.

## File-by-File Explanation (Integration)

### `src/main/java/com/paola/paolarestapi/integration/IntegrationResource.java`
REST façade for Day 2 operations. It exposes simple endpoints to run import, validate snapshot, and search snapshot. This keeps testing easy without immediately needing SOAP tooling.

### `src/main/java/com/paola/paolarestapi/integration/dto/ReqResImportResponse.java`
Return DTO for import endpoint. It reports fetched count, saved count, snapshot location, and schema validation messages so you can quickly see whether import and snapshot generation were successful.

### `src/main/java/com/paola/paolarestapi/integration/model/ReqResUserItem.java`
Represents a single ReqRes user in `data[]`. It maps ReqRes JSON fields (`first_name`, `last_name`) into Java fields used by import and snapshot generation logic.

### `src/main/java/com/paola/paolarestapi/integration/model/ReqResUsersResponse.java`
Represents paged ReqRes envelope with metadata (`page`, `total_pages`, etc.) and user list. Needed so client can iterate all pages, not just page 1.

### `src/main/java/com/paola/paolarestapi/integration/model/SnapshotUser.java`
XML-oriented model for one `<user>` node in local snapshot. Used for JAXB marshalling and for returning search results from XPath matches.

### `src/main/java/com/paola/paolarestapi/integration/model/UsersSnapshot.java`
Root JAXB model (`<users_snapshot>` with repeated `<user>` elements). Without this wrapper, generating a structured snapshot document is harder and less stable.

### `src/main/java/com/paola/paolarestapi/integration/service/ReqResClientService.java`
Minimal external REST client. Uses `HttpURLConnection` + Jackson to fetch/deserialize ReqRes pages. Keeps HTTP concerns isolated from business flow.

### `src/main/java/com/paola/paolarestapi/integration/service/ReqResImportService.java`
Main orchestrator. It combines external fetch, local DB save, snapshot generation, and snapshot validation. Centralizes Day 2 workflow into one service call.

### `src/main/java/com/paola/paolarestapi/integration/service/XmlSnapshotService.java`
Generates local XML snapshot file with JAXB at `data/reqres-users-snapshot.xml`. This snapshot decouples SOAP/XPath search from live ReqRes latency.

### `src/main/java/com/paola/paolarestapi/integration/service/SnapshotValidationService.java`
Validates snapshot XML against XSD (`users-snapshot.xsd`) and returns readable warnings/errors. It is used as a gate before running search.

### `src/main/java/com/paola/paolarestapi/integration/service/XPathUserSearchService.java`
Implements XML search using XPath. Supports case-insensitive partial matching across core user fields and maps XML nodes back to Java objects.

### `src/main/java/com/paola/paolarestapi/integration/soap/SoapUserResult.java`
SOAP response item DTO. Separates SOAP output contract from internal snapshot model.

### `src/main/java/com/paola/paolarestapi/integration/soap/UserSearchSoapResponse.java`
SOAP response envelope with message, validation details, and matched users.

### `src/main/java/com/paola/paolarestapi/integration/soap/UserSearchSoapService.java`
SOAP service method implementation. It performs validate-then-search flow and maps results to SOAP DTOs.

### `src/main/resources/schemas/users-snapshot.xsd`
XSD contract for generated snapshot XML. Ensures snapshot has required structure and minimal field correctness before XPath/SOAP consumption.

## How Day 2 Uses Day 1 Code
- Reuses Day 1 persistence layer under `com.paola.paolarestapi.users.*`.
- Specifically:
  - `users.persistence.UserEntity`
  - `users.repository.UserRepository`
  - JPA setup from `persistence.xml`

So Day 2 does not replace Day 1; it builds on top of it.

## Minimal Test Sequence
1. Start app on Tomcat.
2. Call `POST /api/integration/import-reqres`.
3. Confirm response has non-zero `fetchedFromReqRes` and `savedToDatabase`.
4. Call `GET /api/integration/validate-snapshot`.
5. Confirm response array is empty or only warnings.
6. Call `GET /api/integration/search-snapshot?term=george` (or another term).
7. Confirm matching users are returned.

## Frontend Call Order (Day 2)
Recommended order for frontend integration:
1. `POST /api/integration/import-reqres`
2. `GET /api/integration/validate-snapshot`
3. `GET /api/integration/search-snapshot?term=...` (when user searches)

When is data saved to H2?
- During step 1 (`import-reqres`), inside `ReqResImportService.importUsersAndCreateSnapshot()`.
- For each fetched ReqRes user, backend calls `UserRepository.save(...)`.
- This happens before snapshot validation.

Why use `validate-snapshot`?
- It checks whether generated XML snapshot is structurally valid against `users-snapshot.xsd`.
- It prevents search/SOAP from using malformed XML.
- It returns readable validation messages for troubleshooting.

## Known Simplifications (Intentional)
- Duplicate inserts are possible if import is run repeatedly (acceptable for minimal assignment-first flow).
- SOAP class is implemented, but endpoint publication details are environment-dependent.
- XPath term escaping is intentionally simple for now.

## Troubleshooting Notes (What Broke and How It Was Fixed)
During implementation/testing, several runtime issues appeared. These are important to document because they are common in local Tomcat + Jakarta projects.

1. ReqRes call failed with `missing_api_key`
- Symptom: `401/500` on import, ReqRes response indicated missing API key.
- Cause: ReqRes now requires `x-api-key`.
- Fix: backend client now reads key from `REQRES_API_KEY` or `-Dreqres.api.key` and sends `x-api-key` header.

2. ReqRes response parsing failed on unknown field `support`
- Symptom: `UnrecognizedPropertyException` for `support`.
- Cause: ReqRes envelope included fields not present in our Java model.
- Fix: added `@JsonIgnoreProperties(ignoreUnknown = true)` on ReqRes response/item models.

3. `jakarta.persistence.Persistence` class not found at runtime
- Symptom: `NoClassDefFoundError: jakarta/persistence/Persistence`.
- Cause: Tomcat does not provide JPA API jars by default.
- Fix: package `jakarta.persistence-api` in WAR (removed `provided` scope).

4. Persistence unit initialization failed (`persistence.xml` version)
- Symptom: `Unrecognized JPA persistence.xml XSD version: 3.2`.
- Cause: Hibernate version in use did not accept 3.2 descriptor in this runtime setup.
- Fix: changed `persistence.xml` descriptor to `version="3.1"` with `persistence_3_1.xsd`.

5. Duplicate entity mapping after package refactor
- Symptom: `DuplicateMappingException` for old and new `UserEntity` package names.
- Cause: stale class files in exploded deployment/build output after moving packages.
- Fix: `mvn clean package`, then full redeploy/restart so stale classes were removed.

6. API returned 404 while app root worked
- Symptom: `/index.html` worked, `/api/*` returned 404.
- Cause: Jersey bootstrap dependency was too minimal for this annotation-based setup.
- Fix: changed dependency from `jersey-container-servlet-core` to `jersey-container-servlet`.

## Definition of Done for Day 2
- ReqRes users can be fetched and imported locally.
- XML snapshot is generated successfully.
- Snapshot is validated against XSD programmatically.
- XPath search returns filtered records from snapshot.
- SOAP search method logic is implemented with validation gate.

## Tested Search Examples

### Get all users from snapshot
`http://localhost:8080/PaolaRestApi_war_exploded/api/integration/search-snapshot?term=`

```json
[{"id":1,"email":"george.bluth@reqres.in","first_name":"George","last_name":"Bluth","avatar":"https://reqres.in/img/faces/1-image.jpg"},{"id":2,"email":"janet.weaver@reqres.in","first_name":"Janet","last_name":"Weaver","avatar":"https://reqres.in/img/faces/2-image.jpg"},{"id":3,"email":"emma.wong@reqres.in","first_name":"Emma","last_name":"Wong","avatar":"https://reqres.in/img/faces/3-image.jpg"},{"id":4,"email":"eve.holt@reqres.in","first_name":"Eve","last_name":"Holt","avatar":"https://reqres.in/img/faces/4-image.jpg"},{"id":5,"email":"charles.morris@reqres.in","first_name":"Charles","last_name":"Morris","avatar":"https://reqres.in/img/faces/5-image.jpg"},{"id":6,"email":"tracey.ramos@reqres.in","first_name":"Tracey","last_name":"Ramos","avatar":"https://reqres.in/img/faces/6-image.jpg"},{"id":7,"email":"michael.lawson@reqres.in","first_name":"Michael","last_name":"Lawson","avatar":"https://reqres.in/img/faces/7-image.jpg"},{"id":8,"email":"lindsay.ferguson@reqres.in","first_name":"Lindsay","last_name":"Ferguson","avatar":"https://reqres.in/img/faces/8-image.jpg"},{"id":9,"email":"tobias.funke@reqres.in","first_name":"Tobias","last_name":"Funke","avatar":"https://reqres.in/img/faces/9-image.jpg"},{"id":10,"email":"byron.fields@reqres.in","first_name":"Byron","last_name":"Fields","avatar":"https://reqres.in/img/faces/10-image.jpg"},{"id":11,"email":"george.edwards@reqres.in","first_name":"George","last_name":"Edwards","avatar":"https://reqres.in/img/faces/11-image.jpg"},{"id":12,"email":"rachel.howell@reqres.in","first_name":"Rachel","last_name":"Howell","avatar":"https://reqres.in/img/faces/12-image.jpg"}]
```

### Search by term `george`
`http://localhost:8080/PaolaRestApi_war_exploded/api/integration/search-snapshot?term=george`

```json
[{"id":1,"email":"george.bluth@reqres.in","first_name":"George","last_name":"Bluth","avatar":"https://reqres.in/img/faces/1-image.jpg"},{"id":11,"email":"george.edwards@reqres.in","first_name":"George","last_name":"Edwards","avatar":"https://reqres.in/img/faces/11-image.jpg"}]
```
