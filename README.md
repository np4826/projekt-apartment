# projekt-apartment
## Prerequisites

```bash
docker run -d --name projekt-apartment -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=apartment -p 5433:5432 postgres:latest
```

## Run application in Docker

```bash
mvn clean package
docker build -t rso-apartment .
docker run -p 8081:8081 rso-apartment
```