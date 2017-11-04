# projekt-user
## Prerequisites

```bash
docker run -d --name projekt-user -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=users -p 32769:5432 postgres:latest
```

## Run application in Docker

```bash
mvn clean package
docker build -t rso-user .
docker run -p 8082:8082 rso-user
```
# test communication: http://localhost:8082/v1/user/1