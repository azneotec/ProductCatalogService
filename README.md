# ProductCatalogService API

A Spring Boot REST service for browsing and searching a product catalog. Products and categories live in MySQL; keyword search runs on an in-memory Apache Lucene index. On first boot the catalog is seeded from the public [FakeStore API](https://fakestoreapi.com/products), and the product JSON mirrors FakeStore's shape.

The service is one node of a small system: it registers with **Eureka** as `product-catalog-service`, and it calls **`user-auth-service`** (found through Eureka, never by host/port) to validate the bearer token on every write endpoint. Reads are public; creating, replacing or deleting products and creating categories need an admin (see [Authentication and authorization](#authentication-and-authorization)).

## Requirements

| Tool | Version | Notes |
|---|---|---|
| Java (JDK) | 17 | Lucene 9.x is used because 10.x needs Java 21 |
| MySQL | 8.x or newer | must be reachable at `localhost:3306` unless overridden (see [Configuration](#configuration)) |
| Maven | — | not required; the project ships the Maven wrapper (`./mvnw`, `mvnw.cmd`) |
| Eureka + `user-auth-service` | — | needed for the write endpoints (token validation); reads work without them. `make up` in the repo root starts both, plus MySQL and Kafka |
| Internet access | first boot only | to fetch the seed data from `fakestoreapi.com`; if unreachable the app still starts with an empty catalog |

## Getting started

1. Create the database (the schema itself is managed by Flyway, so an empty database is enough):

   ```sql
   CREATE DATABASE product_catalog_service;
   ```

   Or use the MySQL from the root `docker-compose.yml` (`make infra-up` in the repo root), which already creates `product_catalog_service` and listens on host port **3307**. Point `DB_URL` at it, e.g. `jdbc:mysql://localhost:3307/product_catalog_service`; the repo-root `make up` does this for you.

   The defaults expect user `root` / password `password`. Override them with the environment variables described in [Configuration](#configuration) if your setup differs (e.g. a remote database such as RDS).

2. Run the application:

   ```bash
   ./mvnw spring-boot:run     # local MySQL defaults, or whatever DB_* is set to in your shell
   make run                   # loads .env first, then runs the built jar (see "Building with make")
   ```

   On startup the app will, in order:
   - apply Flyway migrations from `src/main/resources/db/migration` (`V1`…`V4`),
   - seed the 4 categories and 20 products from FakeStore **if the `product` table is empty**,
   - build the Lucene search index from the database.

   The server listens on `8002` by default. To run a second instance beside it, set `SERVER_PORT=9002`:

   | Run | Port | How |
   |---|---|---|
   | Single instance (default) | `8002` | `./mvnw spring-boot:run` |
   | Second instance | `9002` | `SERVER_PORT=9002 ./mvnw spring-boot:run` |

   The service registers with Eureka (`http://localhost:8761/eureka/`, start the registry first) as `product-catalog-service`; a second instance registers under the same name on its own port. `make run` loads `.env`, so a `SERVER_PORT` there overrides the default. For the whole stack (MySQL, Kafka, Eureka, two instances each of `UserAuthService` and this service) run `make up` from the repo root. The examples below assume `8002`. Health check: `GET /actuator/health`.

   **Standalone MySQL with Docker.** `docker-compose.yml` runs only the database (host port `3308`, `product_catalog_service` created for you); the app runs on the host.

   ```bash
   make infra-up       # start MySQL and wait until healthy
   make jar && make run   # with DB_URL=jdbc:mysql://localhost:3308/product_catalog_service in .env
   make infra-down     # stop, keeping the data (make infra-reset also deletes the volume)
   ```

   It is independent of the repo-root `docker-compose.yml` (different project name, port 3308 vs 3307). Without Eureka the app logs registration warnings, reads work, and the admin-only writes answer `503` since `user-auth-service` can't be found. The `Dockerfile` builds a runnable image of the service (`docker build -t product-catalog-service .`) if you want to run it as a container yourself.

3. Run the tests:

   ```bash
   ./mvnw test                                  # everything
   ./mvnw test -Dtest=LuceneProductSearchServiceTest   # a single class
   ```

   The Lucene, seeder, service and auth tests are plain JUnit/Mockito and need nothing external; the controller tests (`@WebMvcTest`) mock `IAuthorizationService`, so they need neither a database nor `user-auth-service`. The `@SpringBootTest` classes need the local MySQL instance (seeding is disabled for them). They run with the `test` profile (`src/test/resources/application-test.properties`), which pins the datasource to `localhost:3306` regardless of any `DB_*` variables, so tests never touch a remote database.

## Configuration

All settings live in `src/main/resources/application.properties` and can be overridden the usual Spring Boot ways (`-D` flags, `application-local.properties`, or environment variables).

### Environment variables

Sensitive values are never hard-coded: the datasource settings are placeholders resolved from the environment when the app starts, falling back to the local-dev defaults.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/product_catalog_service` | JDBC URL of the database |
| `DB_USERNAME` | `root` | database user |
| `DB_PASSWORD` | `password` | database password |
| `SERVER_PORT` | `8002` | HTTP port; use `9002` for a second instance |

For local use, copy the template and fill it in:

```bash
cp .env.example .env      # .env is gitignored - never commit real credentials
```

Spring Boot does not read `.env` by itself. Load it into your shell (`set -a; source .env; set +a`), use `make` (below), or add the variables to your IDE run configuration.

### Building with make

The `Makefile` loads `.env` before invoking Maven or Java:

| Command | What it does |
|---|---|
| `make jar` | build `target/ProductCatalogService-0.0.1-SNAPSHOT.jar` (tests skipped; `make jar SKIP_TESTS=false` runs them) |
| `make run` | run the built jar with `.env` loaded |
| `make test` | run the tests against local MySQL (test profile) |
| `make clean` | `./mvnw clean` |

The jar contains only the `${DB_*}` placeholders, not the values, so the same artifact runs in any environment; the values are supplied by the environment at startup.

### Properties

| Property | Default | Purpose |
|---|---|---|
| `spring.datasource.url` | `${DB_URL}` → local MySQL | database location |
| `spring.datasource.username` / `password` | `${DB_USERNAME}` / `${DB_PASSWORD}` → `root` / `password` | database credentials |
| `catalog.seed.enabled` | `true` | seed from FakeStore when the product table is empty |
| `catalog.seed.fakestore.base-url` | `https://fakestoreapi.com` | seed source |
| `catalog.search.engine` | `lucene` | search backend (`elasticsearch` reserved for a future implementation) |
| `catalog.search.reindex-on-startup` | `true` | rebuild the search index from the database on boot |
| `spring.application.name` | `product-catalog-service` | name this service registers under in Eureka |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` | Eureka registry; also how `user-auth-service` is resolved |
| `eureka.client.register-with-eureka` / `fetch-registry` | `true` / `true` | register this instance and download the registry (needed for the load-balanced call to `user-auth-service`) |

## API

Base URL: `http://localhost:8002`. All responses are JSON; error responses carry a plain-text message with the status code.

### Products

| Method | Path | Description | Errors |
|---|---|---|---|
| `GET` | `/products` | all products | |
| `GET` | `/products?categoryId={id}` | products in a category, by id | `400` id ≤ 0 or non-numeric · `404` unknown category |
| `GET` | `/products?category={name}` | products in a category, by name (case-insensitive) | `400` blank · `404` unknown category |
| `GET` | `/products/search?q={keywords}&page=&size=&sort=` | keyword search over title, category and description, best match first, tolerant of typos (`jaket` finds jackets); paged and optionally sorted (see below) | `400` missing/blank `q` · `404` unknown `sort` field |
| `GET` | `/products/{id}` | product details | `400` id ≤ 0 · `404` not found |
| `POST` | `/products` | create a product (`201`) | `400` unknown `category.id` · `401`/`403`/`503` admin token (see below) |
| `PUT` | `/products/{id}` | replace a product | `400` unknown `category.id` · `404` not found · `401`/`403`/`503` admin token |
| `DELETE` | `/products/{id}` | delete a product (`204`) | `404` not found · `401`/`403`/`503` admin token |

Passing both `categoryId` and `category` on `/products` is a `400`.

**Admin token.** `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}` and `POST /categories` need an `Authorization: Bearer <jwt>` header from `user-auth-service` whose user has `ROLE_ADMIN`; reads stay open. `401` = missing, malformed or invalid/expired token, `403` = valid token but not an admin, `503` = `user-auth-service` unreachable. Details in [Authentication and authorization](#authentication-and-authorization).

### Paged, sorted keyword search

`GET /products/search` returns a page wrapper:

```bash
curl 'http://localhost:8002/products/search?q=shirt&page=0&size=5&sort=price,desc&sort=id,asc'
```

```json
{ "content": [ { "id": 14, "title": "…", "price": 999.99, "…": "…" } ], "page": 0, "size": 5, "totalElements": 12, "totalPages": 3 }
```

- `page` is 0-based (default `0`); `size` defaults to `10` and is capped at `100`.
- `sort=field[,asc|desc]` can be repeated and applies in order; the field is a `Product` property name (`price`, `title`, `rating.rate`, `category.name`, `id`, …). Without `sort`, results are in relevance order. An unknown field currently comes back as a `404`.
- Search considers at most the 1000 best matches; `totalElements` is capped by that.

### Search with paging and sorting (JPA, title only)

`POST /search` does a case-insensitive title match, paged and optionally sorted. Body:

```json
{
  "query": "shirt",
  "pageSize": 5,
  "pageNumber": 0,
  "sortParams": [
    { "sortCriteria": "price", "sortType": "DESC" },
    { "sortCriteria": "id", "sortType": "ASC" }
  ]
}
```

`sortParams` is optional; list order is sort priority, `sortType` is `ASC` (default) or `DESC`, and omitting `sortParams` leaves the results unsorted. `sortCriteria` is a `Product` property name (`id`, `title`, `price`, `imageUrl`, `rating.rate`, `category.name`, `createdAt`, …) and is passed straight to Spring Data, so an unknown name currently comes back as a `404`.

Product shape (request and response):

```json
{
  "id": 1,
  "title": "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
  "price": 109.95,
  "description": "Your perfect pack for everyday use and walks in the forest.",
  "category": { "id": 4, "name": "men's clothing" },
  "image": "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_t.png",
  "rating": { "rate": 3.9, "count": 120 }
}
```

On `POST`/`PUT` only `category.id` is read; the category must already exist. `id` in the body is ignored.

### Categories

| Method | Path | Description |
|---|---|---|
| `GET` | `/categories` | all categories as `[{ "id": 1, "name": "electronics" }, …]` |
| `POST` | `/categories` | create a category from `{ "name": "…" }` (`201`). Needs `Authorization: Bearer <jwt>` from `user-auth-service` with `ROLE_ADMIN`: `401` for a missing/invalid token, `403` without the role, `503` if `user-auth-service` isn't reachable via Eureka |

### Bruno collection

`bruno/` is a [Bruno](https://www.usebruno.com) collection covering every endpoint (open the folder in Bruno, pick the `Local` environment for port 8002 or `Local-9002` for the second instance). Run **Auth > Login as Admin** first: it logs in against `user-auth-service` (`authUrl`, 8001) as the seeded admin and stores `adminToken`, which the write requests use. The `Errors` folder covers the 400/401/403/404 cases; its two "Not an Admin" requests need **Auth > Signup Regular User** and **Login as Regular User** first. Create, Update and Delete Product run in order and leave the catalog as they found it. The services must be running with Eureka for the guarded requests to get past `503` (`make up` in the repo root).

From a terminal: `cd bruno && npx @usebruno/cli run --env Local -r` (31 requests, all passing against a local stack).

### Examples

```bash
curl 'http://localhost:8002/products?category=jewelery'
curl 'http://localhost:8002/products/search?q=mens%20jacket'
curl -X POST http://localhost:8002/products \
  -H 'Content-Type: application/json' \
  -d '{"title":"Trail Shoes","price":89.5,"description":"Lightweight.","category":{"id":4},"image":"https://example.com/shoes.png","rating":{"rate":4.2,"count":7}}'
```

## Authentication and authorization

This service does not issue or parse tokens itself. Login happens in `user-auth-service`; this service only asks it whether a token is good.

```
client ── Authorization: Bearer <jwt> ──► ProductController / CategoryController (write endpoints)
                                              │ authorizationService.requireAdmin(header)
                                              ▼
                                         AuthorizationService      ← parses the header, checks valid + ROLE_ADMIN
                                              │ authClient.validateToken(token)
                                              ▼
                                         AuthServiceClient         ← @LoadBalanced RestTemplate
                                              │ POST http://user-auth-service/auth/validateToken
                                              ▼
                                         Eureka resolves "user-auth-service" to a registered instance (8001 / 9001 …)
```

- **Service discovery** — `AuthServiceClient` addresses `user-auth-service` by its Eureka name. The `@LoadBalanced` `RestTemplate` (`config/RestTemplateConfig`) swaps the name for a registered instance and spreads calls across instances, so no host or port is configured here. `AuthServiceClient` is the only caller of `user-auth-service`; `StorageProductService` depends only on the repositories and the search index.
- **The check** — `IAuthorizationService.requireAdmin` runs at the top of each guarded controller method, before the request body is mapped or any service is touched. It needs a `Bearer` header, a response with `valid: true`, and `ROLE_ADMIN` in `roles`. `GET` endpoints are not guarded.
- **Every guarded call is a network call** — the token is validated against `user-auth-service` on each request (no caching), so a revoked session or logout takes effect immediately.
- **Failures** — `AuthException` carries an `AuthExceptionType`, and `ControllerAdvisor` maps it: `UNAUTHENTICATED` → `401`, `FORBIDDEN` → `403`, `AUTH_SERVICE_UNAVAILABLE` → `503` (connection error, empty reply, or no `user-auth-service` instance registered). Like the other exception types, the handler is an exhaustive `switch`.
- **Getting a token** — `POST /auth/login` on `user-auth-service` returns `token`. The seeded admin account lives in that service's `V6__seed_admin_user.sql`; further admins are granted there.

```bash
TOKEN=$(curl -s -X POST http://localhost:8001/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"<admin email>","password":"<password>"}' | jq -r .token)
curl -X POST http://localhost:8002/categories -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"name":"toys"}'
```

## Architecture

```
HTTP
 │
 ▼
controllers/        ProductController, CategoryController      ← DTO ⇄ model mapping, input validation, admin guard on writes
 │                  ControllerAdvisor                          ← exceptions → HTTP status
 ▼
services/           IAuthorizationService ─ AuthorizationService ─► IAuthClient ─ AuthServiceClient ──► Eureka ──► user-auth-service
                    IProductService ─ StorageProductService     ← JPA reads/writes + keeps the search index in sync
                    ICategoryService ─ CategoryService
 │                          │
 ▼                          ▼
repos/ (Spring Data JPA)   search/ IProductSearchService ─ LuceneProductSearchService (in-memory index)
 │                          ▲
 ▼                          │  rebuilt from the DB on startup (ProductIndexBootstrap)
MySQL  ◄── seed/ FakeStoreSeeder (first boot, empty table only) ── FakeStoreClient ──► fakestoreapi.com
```

- **Service registry** — the app is a Eureka client (`spring-cloud-starter-netflix-eureka-client`) registered as `product-catalog-service`; several instances (8002, 9002) share that name. The same registry is how it finds `user-auth-service`.
- **Auth** — token validation is delegated to `user-auth-service` through a load-balanced call; see [Authentication and authorization](#authentication-and-authorization).

- **Persistence** — `Product` and `Category` entities (`models/`) map to tables created and evolved by **Flyway** migrations; Hibernate only *validates* the schema, so any entity change needs a new `V<n>__*.sql`.
- **Search** — the service layer talks to `IProductSearchService`, which returns product ids ranked by relevance; MySQL remains the source of truth and the ids are hydrated from it. The Lucene implementation indexes title, category name and description, and is rebuilt from the database on every start. It corrects typos per word: a word with no exact or prefix match is matched fuzzily instead (1 edit for 4–5 letters, 2 for longer; the first letter must be right; words under 4 letters are never guessed), so correctly spelled words don't pull in look-alikes. Swapping in Elasticsearch means adding another implementation of the same interface — controllers and services stay untouched.
- **Seeding** — `FakeStoreSeeder` runs once on an empty database, matching categories by name and never forcing FakeStore ids onto the auto-increment columns.
