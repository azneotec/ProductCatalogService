# Loads DB_URL / DB_USERNAME / DB_PASSWORD from the gitignored .env (see .env.example)
# into the environment of every command below. Spring resolves them at runtime, so the
# jar itself contains only the ${DB_*} placeholders, never the values.

JAR        := target/ProductCatalogService-0.0.1-SNAPSHOT.jar
SKIP_TESTS ?= true

# Export .env for one recipe line. Sourced by the shell (not `include`) so quoted values work.
WITH_ENV = set -a; . ./.env; set +a;

.PHONY: jar run test clean check-env

check-env:
	@test -f .env || { echo "Missing .env - copy .env.example to .env and fill it in"; exit 1; }

## Build the jar with .env loaded (tests skipped by default; use SKIP_TESTS=false to run them)
jar: check-env
	@$(WITH_ENV) ./mvnw clean package -DskipTests=$(SKIP_TESTS)
	@echo "Built $(JAR)"

## Run the built jar with .env loaded
run: check-env
	@test -f $(JAR) || { echo "$(JAR) not found - run 'make jar' first"; exit 1; }
	@$(WITH_ENV) java -jar $(JAR)

## Run the tests (they pin themselves to local MySQL via the 'test' profile)
test:
	@./mvnw test

clean:
	@./mvnw clean
