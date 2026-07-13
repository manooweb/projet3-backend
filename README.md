# Chatop API

Spring Boot REST API for the Chatop rental project.

## Requirements

Common requirements:

- Java 17
- Maven, or the Maven Wrapper included in this repository

Database requirements depend on the setup you choose:

- Scenario 1: Docker, if you want to run MySQL in a container
- Scenario 2: MySQL 8, if you want to use an existing local MySQL server

## Database Setup

The SQL schema is available in this backend folder:

```text
chatop.sql
```

The application uses `spring.jpa.hibernate.ddl-auto=validate`, so Spring Boot checks that the schema matches the entities, but it does not create the tables automatically. The SQL script must be imported before starting the API.

The script drops and recreates the project tables, then inserts a demo user and a demo rental. Demo credentials:

```text
email: demo@chatop.com
password: password
```

The demo rental uses this public picture URL:

```text
/api/uploads/rentals/online-house-rental-sites.jpg
```

With the default upload configuration, the corresponding file must exist here before starting the API:

```text
uploads/rentals/online-house-rental-sites.jpg
```

If you override `RENTAL_UPLOADS_DIR`, copy this image into the configured folder instead. The public URL stays `/api/uploads/rentals/online-house-rental-sites.jpg`.

### Scenario 1: MySQL With Docker

Start a MySQL 8 container:

```bash
docker run --name chatop-mysql \
  -v chatop_mysql_data:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=chatop_db \
  -e MYSQL_USER=chatop_user \
  -e MYSQL_PASSWORD=chatop_password \
  -p 3306:3306 \
  -d mysql:8.4
```

This command creates a development container named `chatop-mysql` and stores database files in the `chatop_mysql_data` Docker volume.

Copy the SQL script into the container:

```bash
docker cp chatop.sql chatop-mysql:/tmp/chatop.sql
```

Open a MySQL session:

```bash
docker exec -it chatop-mysql mysql -uchatop_user -p chatop_db
```

Enter the password configured above, then import the schema:

```sql
source /tmp/chatop.sql;
exit;
```

Useful Docker commands after the demo:

```bash
docker stop chatop-mysql
docker start chatop-mysql
```

Stopping the container keeps the database files in the `chatop_mysql_data` volume. This is also the simplest way to test the application behavior when the database is unavailable.

To remove the container while keeping the database volume:

```bash
docker stop chatop-mysql
docker rm chatop-mysql
```

To remove both the container and the database data:

```bash
docker stop chatop-mysql
docker rm chatop-mysql
docker volume rm chatop_mysql_data
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
mysql -uchatop_user -p chatop_db < chatop.sql
```

## Environment Configuration

From the `projet3-backend` folder, copy the example environment file:

```bash
cp .env.example .env
```

Report the database values from the setup scenario you chose:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_USER=chatop_user
DB_PASSWORD=chatop_password
DB_NAME=chatop_db
```

Then configure the application secrets and optional services:

```properties
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

Home page:

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

Authentication uses HTTP cookies:

- `/api/auth/register` and `/api/auth/login` create the authentication cookie.
- Protected endpoints then reuse this cookie automatically when requests are sent from the same browser.

CSRF protection is enabled for browser requests. Swagger UI is configured to support it with:

- CSRF cookie: `XSRF-TOKEN`
- CSRF header: `X-XSRF-TOKEN`

Because `/api/auth/register` and `/api/auth/login` are also `POST` requests, they need a CSRF token even before the authentication cookie exists. Use the dedicated CSRF endpoint first:

```text
GET /api/auth/csrf
```

Recommended Swagger UI flow:

1. Execute `GET /api/auth/csrf`.
2. Execute `POST /api/auth/login` or `POST /api/auth/register`.
3. Execute protected endpoints from the same browser session.

The CSRF endpoint returns `204 No Content` and creates the readable `XSRF-TOKEN` cookie. Swagger UI can then read this cookie and send the `X-XSRF-TOKEN` header automatically for `POST`, `PUT` and `DELETE` requests.

If you skip `GET /api/auth/csrf`, the first `POST /api/auth/login` or `POST /api/auth/register` can fail because the browser did not have a CSRF token yet. In that case, retrying the same request after the CSRF cookie has been created may work, but calling `/api/auth/csrf` first is the predictable flow.

## Demo Frontend

The demo frontend is a separate Angular application. It must be started from its own project folder and configured to call this backend API.

For local testing, the backend API base URL is:

```text
http://localhost:9001/api
```
