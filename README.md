# Flyway SQL Versioning CI/CD Demo

This repository demonstrates how to manage database schema changes in a production-like environment using [Flyway](https://flywaydb.org/), integrated within a Spring Boot application and a CI/CD pipeline using GitHub Actions.

## What is Database Migration?

In a true production system, you don't use Hibernate's `ddl-auto=update`. Instead, you treat your database schema like code. Each change (creating a table, adding a column, inserting reference data) is an immutable SQL file called a migration.

When the application boots, Flyway checks the `flyway_schema_history` table in the database to see which migrations have already been applied, and it applies any new ones in numerical order.

## Project Structure

* **`src/main/resources/db/migration/`**: This is where the magic happens.
  * `V1__init_schema.sql`: Our initial table creation.
  * `V2__add_email_column.sql`: A subsequent schema change, safely altering an existing table.
  * `V3__insert_seed_data.sql`: DML operations for reference data.
* **`docker-compose.yml`**: Spins up a local PostgreSQL database.
* **`.github/workflows/ci.yml`**: A GitHub Action that runs `mvn verify` every time you push to the repo.

## How the CI Pipeline Checks and Releasing Migrations

### 1. Verification During Testing (`build_and_test` job)
The GitHub action uses **Testcontainers**. During the `mvn verify` phase, `FlywayDemoApplicationTests.java` spins up a temporary PostgreSQL Docker container. Since we set `spring.flyway.enabled=true` specifically for tests (in `src/test/resources/application.yml`), Flyway runs and verifies that the migrations successfully apply and that the application boots.

### 2. Execution During Deployment (`deploy` job)
**Best Practice Alert**: In `src/main/resources/application.yml`, we deliberately set `spring.flyway.enabled: false`. If 10 instances of your application start at the exact same moment during a rollout, having them all race to apply database migrations can cause locks and deadlocks.
Instead, database migration should be a deterministic step inside your deployment pipeline. The `.github/workflows/ci.yml` introduces a `deploy` job which uses the `flyway-maven-plugin` to explicitly connect to the production database and run all schema changes *before* the new application containers are deployed.

## How to Run Locally

1. Start the local database:
   ```bash
   docker-compose up -d
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Check the logs. You will see Flyway connecting and migrating your database to the latest version automatically.
