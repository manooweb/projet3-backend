# Chatop API

Spring Boot REST API for the Chatop rental project.

## Requirements

Common requirements:

- Java 17
- Maven, or the Maven Wrapper included in this repository

Database requirements depend on the setup you choose:

- Scenario 1: Docker, if you want to run MySQL in a container
- Scenario 2: MySQL 8, if you want to use an existing local MySQL server

## Environment Configuration

From the `projet3-backend` folder, copy the example environment file:

```bash
cp .env.example .env
```

For a standard local setup, use values similar to these:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_USER=chatop_user
DB_PASSWORD=chatop_password
DB_NAME=chatop_db

JWT_SECRET=change-me-with-a-long-secret-key-for-local-development
JWT_EXPIRATION_SECONDS=86400
JWT_COOKIE_SECURE=false
JWT_COOKIE_SAME_SITE=Lax

MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS_ENABLE=false
MAIL_FROM=no-reply@chatop.local
```

The application imports this file automatically through Spring Boot configuration.

## Mail Configuration

Mail settings are used to send a notification email when a user sends a message about a rental.

For local development, you can use Mailpit or any other local SMTP server with the default configuration:

```properties
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS_ENABLE=false
```

Mailpit is optional. If no SMTP server is running while these values are configured, the API can still start and the main rental/message workflow is not blocked. Email delivery errors are ignored by the application after being logged for debugging.

## Database Setup

The SQL schema is available at:

```text
../livrables/script.sql
```

The application uses `spring.jpa.hibernate.ddl-auto=validate`, so Spring Boot checks that the schema matches the entities, but it does not create the tables automatically. The SQL script must be imported before starting the API.

### Scenario 1: MySQL With Docker

Start a MySQL 8 container:

```bash
docker run --name chatop-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=chatop_db \
  -e MYSQL_USER=chatop_user \
  -e MYSQL_PASSWORD=chatop_password \
  -p 3306:3306 \
  -d mysql:8.4
```

Copy the SQL script into the container:

```bash
docker cp ../livrables/script.sql chatop-mysql:/tmp/script.sql
```

Open a MySQL session:

```bash
docker exec -it chatop-mysql mysql -uchatop_user -p chatop_db
```

Enter the password configured above, then import the schema:

```sql
source /tmp/script.sql;
exit;
```

### Scenario 2: Local MySQL Server

Connect to MySQL with an administrator account:

```bash
mysql -uroot -p
```

Create the database and user:

```sql
CREATE DATABASE chatop_db;
CREATE USER 'chatop_user'@'localhost' IDENTIFIED BY 'chatop_password';
GRANT ALL PRIVILEGES ON chatop_db.* TO 'chatop_user'@'localhost';
FLUSH PRIVILEGES;
exit;
```

Import the SQL schema:

```bash
mysql -uchatop_user -p chatop_db < ../livrables/script.sql
```

## Run the Backend

From the `projet3-backend` folder, start the application:

```bash
./mvnw spring-boot:run
```

If the Maven Wrapper is not executable on your system, run:

```bash
bash ./mvnw spring-boot:run
```

The API starts on:

```text
http://localhost:9001
```

Health check:

```text
http://localhost:9001/api/health
```

Expected response:

```json
{
  "status": "OK"
}
```

## Swagger / OpenAPI

Swagger UI is available at:

```text
http://localhost:9001/swagger-ui.html
```

The generated OpenAPI contract is available at:

```text
http://localhost:9001/v3/api-docs
```

Protected endpoints can be tested from Swagger UI with the `Authorize` button after getting a JWT through `/api/auth/register` or `/api/auth/login`.

## Demo Frontend

The demo frontend is a separate Angular application. It must be started from its own project folder and configured to call this backend API.

For local testing, the backend API base URL is:

```text
http://localhost:9001/api
```
