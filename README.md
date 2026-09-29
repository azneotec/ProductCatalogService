# ProductCatalogService API

A Spring Boot REST service for browsing and searching a product catalog. Products and categories live in MySQL; keyword search runs on an in-memory Apache Lucene index. On first boot the catalog is seeded from the public [FakeStore API](https://fakestoreapi.com/products), and the product JSON mirrors FakeStore's shape.

## Requirements

| Tool | Version | Notes |
|---|---|---|
| Java (JDK) | 17 | Lucene 9.x is used because 10.x needs Java 21 |
| MySQL | 8.x or newer | must be reachable at `localhost:3306` unless overridden (see [Configuration](#configuration)) |
| Maven | — | not required; the project ships the Maven wrapper (`./mvnw`, `mvnw.cmd`) |
| Internet access | first boot only | to fetch the seed data from `fakestoreapi.com`; if unreachable the app still starts with an empty catalog |

## Getting started

1. Create the database (the schema itself is managed by Flyway, so an empty database is enough):

   ```sql
   CREATE DATABASE product_catalog_service;
   ```

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

   The server listens on the port set by `server.port` (`5000` by default; `.env.example` overrides it to `8080` via `SERVER_PORT`, because macOS AirPlay Receiver occupies 5000). The examples below assume `8080`. Health check: `GET /actuator/health`.

3. Run the tests:

   ```bash
   ./mvnw test                                  # everything
   ./mvnw test -Dtest=LuceneProductSearchServiceTest   # a single class
   ```

   The Lucene, seeder and service tests are plain JUnit/Mockito and need nothing external. The `@SpringBootTest` classes need the local MySQL instance (seeding is disabled for them). They run with the `test` profile (`src/test/resources/application-test.properties`), which pins the datasource to `localhost:3306` regardless of any `DB_*` variables, so tests never touch a remote database.

## Configuration

All settings live in `src/main/resources/application.properties` and can be overridden the usual Spring Boot ways (`-D` flags, `application-local.properties`, or environment variables).

### Environment variables

Sensitive values are never hard-coded: the datasource settings are placeholders resolved from the environment when the app starts, falling back to the local-dev defaults.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/product_catalog_service` | JDBC URL of the database |
| `DB_USERNAME` | `root` | database user |
| `DB_PASSWORD` | `password` | database password |
| `SERVER_PORT` | `5000` (from `server.port`) | HTTP port (Spring Boot binds this automatically) |

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

## API

Base URL: `http://localhost:8080`. All responses are JSON; error responses carry a plain-text message with the status code.

### Products

| Method | Path | Description | Errors |
|---|---|---|---|
| `GET` | `/products` | all products | |
| `GET` | `/products?categoryId={id}` | products in a category, by id | `400` id ≤ 0 or non-numeric · `404` unknown category |
| `GET` | `/products?category={name}` | products in a category, by name (case-insensitive) | `400` blank · `404` unknown category |
| `GET` | `/products/search?q={keywords}` | keyword search over title, category and description, best match first | `400` missing/blank `q` |
| `GET` | `/products/{id}` | product details | `400` id ≤ 0 · `404` not found |
| `POST` | `/products` | create a product (`201`) | `400` unknown `category.id` |
| `PUT` | `/products/{id}` | replace a product | `400` unknown `category.id` · `404` not found |
| `DELETE` | `/products/{id}` | delete a product (`204`) | `404` not found |

Passing both `categoryId` and `category` on `/products` is a `400`.

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
| `POST` | `/categories` | create a category from `{ "name": "…" }` (`201`) |

### Examples

```bash
curl 'http://localhost:8080/products?category=jewelery'
curl 'http://localhost:8080/products/search?q=mens%20jacket'
curl -X POST http://localhost:8080/products \
  -H 'Content-Type: application/json' \
  -d '{"title":"Trail Shoes","price":89.5,"description":"Lightweight.","category":{"id":4},"image":"https://example.com/shoes.png","rating":{"rate":4.2,"count":7}}'
```

## Architecture

```
HTTP
 │
 ▼
controllers/        ProductController, CategoryController      ← DTO ⇄ model mapping, input validation
 │                  ControllerAdvisor                          ← exceptions → HTTP status
 ▼
services/           IProductService ─ StorageProductService     ← JPA reads/writes + keeps the search index in sync
                    ICategoryService ─ CategoryService
 │                          │
 ▼                          ▼
repos/ (Spring Data JPA)   search/ IProductSearchService ─ LuceneProductSearchService (in-memory index)
 │                          ▲
 ▼                          │  rebuilt from the DB on startup (ProductIndexBootstrap)
MySQL  ◄── seed/ FakeStoreSeeder (first boot, empty table only) ── FakeStoreClient ──► fakestoreapi.com
```

- **Persistence** — `Product` and `Category` entities (`models/`) map to tables created and evolved by **Flyway** migrations; Hibernate only *validates* the schema, so any entity change needs a new `V<n>__*.sql`.
- **Search** — the service layer talks to `IProductSearchService`, which returns product ids ranked by relevance; MySQL remains the source of truth and the ids are hydrated from it. The Lucene implementation indexes title, category name and description, and is rebuilt from the database on every start. Swapping in Elasticsearch (with fuzzy matching) means adding another implementation of the same interface — controllers and services stay untouched.
- **Seeding** — `FakeStoreSeeder` runs once on an empty database, matching categories by name and never forcing FakeStore ids onto the auto-increment columns.
