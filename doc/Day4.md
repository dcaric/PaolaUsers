# Day 4 Implementation (Custom REST API + GraphQL + Source Switch)

## Goal
Implement Day 4 backend in a dedicated `restapi` package with:
- ReqRes-like local CRUD API (GET, POST, PUT, DELETE)
- JWT role guard for write operations
- GraphQL-style endpoint over the same local data
- Configurable source switch for read operations (`LOCAL` or `PUBLIC`)

## Package Added
- `src/main/java/com/paola/paolarestapi/restapi`
  - `RestApiResource`
  - `model/RestApiUser`
  - `dto/RestApiUserWriteRequest`
  - `dto/GraphQlRequest`
  - `service/LocalUserCrudService`
  - `service/PublicReqResUserService`
  - `service/RestApiSourceSwitchService`
  - `service/RestApiSourceType`
  - `service/JwtWriteAccessGuard`
  - `service/GraphQlService`

## Endpoints
Base path: `/api/restapi`

### Source switch
- `GET /switch`
- Returns current mode:
  - `LOCAL` (default)
  - `PUBLIC`

Switch configuration:
- env var: `REST_API_SOURCE`
- JVM prop: `-Drest.api.source=PUBLIC`

### REST CRUD
- `GET /users`
- `GET /users/{id}`
- `POST /users` (requires `full-access` JWT)
- `PUT /users/{id}` (requires `full-access` JWT)
- `DELETE /users/{id}` (requires `full-access` JWT)

Read behavior:
- `LOCAL` mode -> reads from local DB (`UserEntity`)
- `PUBLIC` mode -> reads from ReqRes API

Write behavior:
- always writes to local DB (custom API responsibility)
- role check enforced via Bearer JWT (`full-access` only)

### GraphQL endpoint
- `POST /graphql`
- Supported operations:
  - users query: returns local users
  - updateUser mutation: updates local user (requires `full-access` JWT)

## JWT Notes
- Day 4 reuses Day 3 token format/validation.
- Authorization header format:
  - `Authorization: Bearer <token>`
- `read-only` token:
  - allowed for GET/query operations
  - denied for POST/PUT/DELETE/mutations

## Quick Testing
Use:
- `src/main/java/com/paola/paolarestapi/restapi/resapi-test.http`

Recommended order:
1. Get Day 3 `full-access` token (`/api/weather/auth/login` with role `full-access`)
2. Call `/api/restapi/switch`
3. Test `GET /api/restapi/users`
4. Test POST/PUT/DELETE with bearer token
5. Test GraphQL query and mutation

## How To Use `resapi-test.http`
1. Open `src/main/java/com/paola/paolarestapi/restapi/resapi-test.http` in IntelliJ.
2. Verify base variables:
   - `@host = http://localhost:8080`
   - `@context = /PaolaRestApi_war_exploded` (change if your artifact path is different)
3. Run weather login (`/api/weather/auth/login`) with role `full-access` and copy `accessToken`.
4. Paste token into:
   - `@fullAccessToken = PASTE_FULL_ACCESS_TOKEN_HERE`
5. Run requests top to bottom using IntelliJ HTTP Client run buttons.
6. Optional switch test:
   - default `LOCAL` mode reads local DB.
   - set `REST_API_SOURCE=PUBLIC` (or `-Drest.api.source=PUBLIC`) to read from ReqRes on GET endpoints.

## Data Source Behavior (Important Addition)
- GraphQL endpoint (`POST /api/restapi/graphql`) uses local DB data (H2 via JPA), not public ReqRes.
- `LOCAL`/`PUBLIC` switch affects REST `GET` endpoints in `/api/restapi/users*`.
- Local H2 is populated only when write/import endpoints are called. There is no automatic background sync.

When H2 gets filled:
1. `POST /api/users` (validated local create)
2. `POST /api/integration/import-reqres` (imports ReqRes users into local DB)
3. `POST /api/restapi/users` and `PUT /api/restapi/users/{id}` (custom API writes/updates)
