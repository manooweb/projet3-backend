# Châtop API

API backend Spring Boot du projet Châtop.

## Prérequis

- Java 17
- Maven
- Spring Boot 4.1.0

## Lancer l'application

Depuis le dossier `projet3-backend`, lancer Spring Boot avec Maven :

```powershell
mvn spring-boot:run
```

L'API démarre sur le port `8081` :

```text
http://localhost:8081
```

## Vérifier le démarrage

Un endpoint de vérification est disponible ici :

```text
http://localhost:8081/api/health
```

Réponse attendue :

```json
{
  "status": "OK"
}
```

