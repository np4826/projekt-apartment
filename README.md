## Prerequisites
To run project locally, first you need an etcd instance: 

**Mac:**
```bash
docker run -d -p 2379:2379 \
    --name etcd \
    --volume=/tmp/etcd-data:/etcd-data \
    quay.io/coreos/etcd:latest \
    /usr/local/bin/etcd \
    --name my-etcd-1 \
    --data-dir /etcd-data \
    --listen-client-urls http://0.0.0.0:2379 \
    --advertise-client-urls http://0.0.0.0:2379 \
    --listen-peer-urls http://0.0.0.0:2380 \
    --initial-advertise-peer-urls http://0.0.0.0:2380 \
    --initial-cluster my-etcd-1=http://0.0.0.0:2380 \
    --initial-cluster-token my-etcd-token \
    --initial-cluster-state new \
    --auto-compaction-retention 1 \
    -cors="*"
```

**Windows (PowerShell):**
```bash
docker run -d -p 2379:2379 `
    --name etcd `
    --volume=/tmp/etcd-data:/etcd-data `
    quay.io/coreos/etcd:latest `
    /usr/local/bin/etcd `
    --name my-etcd-1 `
    --data-dir /etcd-data `
    --listen-client-urls http://0.0.0.0:2379 `
    --advertise-client-urls http://0.0.0.0:2379 `
    --listen-peer-urls http://0.0.0.0:2380 `
    --initial-advertise-peer-urls http://0.0.0.0:2380 `
    --initial-cluster my-etcd-1=http://0.0.0.0:2380 `
    --initial-cluster-token my-etcd-token `
    --initial-cluster-state new `
    --auto-compaction-retention 1 `
    -cors="*"
```

This needs to be set on Mac to work:
```bash
sudo ifconfig lo0 alias 192.168.99.100
```


Call in root folder:
```bash
mvn clean package
```

# projekt-apartment
## Prerequisites

```bash
docker run -d --name projekt-apartment-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=apartment -p 32768:5432 postgres:latest
```

## Run application in Docker

```bash
docker build -t rso-apartment .
docker run  --name rso-apartment -p 8081:8081 rso-apartment
```

## quick test: http://localhost:8081/v1/apartment/

# projekt-user
## Prerequisites

```bash
docker run -d --name projekt-user-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=users -p 32769:5432 postgres:latest
```

## Run application in Docker

```bash
docker build -t rso-user .
docker run --name rso-user -p 8082:8082 rso-user
```
## test communication: http://localhost:8082/v1/user/1


Mac - when finished, remove loopback (alias is not persistent – it will not survive a reboot):
```bash
sudo ifconfig lo0 -alias 192.168.99.100
```