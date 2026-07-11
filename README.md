# Châtop API

Spring Boot backend API for the Châtop project.

## Prerequisites

- Java 17
- Maven
- Spring Boot 4.1.0

## Run the application

From the `projet3-backend` folder, start Spring Boot with Maven:

```powershell
mvn spring-boot:run
```

The API starts on port `9001`:

```text
http://localhost:9001
```

## Check the application startup

A health check endpoint is available here:

```text
http://localhost:9001/api/health
```

Expected response:

```json
{
  "status": "OK"
}
```
