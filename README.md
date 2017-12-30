## Prerequisites
To run project locally, first you need an etcd instance: 

**Mac:**
```bash
export HostIP="192.168.99.100"
docker run -d -p 2379:2379 -p 2380:2380  \
    --name etcd \
    --volume=/tmp/etcd-data:/etcd-data \
    quay.io/coreos/etcd:latest \
    /usr/local/bin/etcd \
    --name my-etcd-1 \
    --data-dir /etcd-data \
    --listen-client-urls http://0.0.0.0:2379 \
    --advertise-client-urls http://${HostIP}:2379  \
    --listen-peer-urls http://0.0.0.0:2380 \
    --initial-advertise-peer-urls http://${HostIP}:2380 \
    --initial-cluster my-etcd-1=http://${HostIP}:2380 \
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
docker run  --name rso-apartment -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8081:8081 rso-apartment
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
docker run --name rso-user -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8082:8082 rso-user
```
## test communication: http://localhost:8082/v1/user/1

For discovery, etcd must have keys present. For apartments: environments/dev/services/rso-apartment/1.0.0/instances

**Hystrix endpoint: http://localhost:8082/hystrix.stream**


Mac - when finished, remove loopback (alias is not persistent – it will not survive a reboot):
```bash
sudo ifconfig lo0 -alias 192.168.99.100
```


# Kubernetes
## Prerequisites
**Windows (Powershell)**
```bash
minikube start --vm-driver hyperv --hyperv-virtual-switch=Minikube
& minikube docker-env | Invoke-Expression
```
**MacOs**
```bash
minikube start
echo $(minikube docker-env)
```
if problems, try with deleting before start: $minikube delete; minikube start

```bash
cd projekt-apartment\projekt-apartment 
mvn clean package
docker build -t projekt-apartment:v1 .
cd ..\projekt-user
mvn clean package
docker build -t projekt-user:v1 .
```

** To delete all items from minikube **
```bash
kubectl delete --all pods --namespace=default
kubectl delete --all deployments --namespace=default
kubectl delete --all services --namespace=default
```

```bash
cd ../projekt-kubernetes
kubectl create -f etcd.yaml
kubectl create -f grafana-deployment.yaml
kubectl create -f grafana-service.yaml
kubectl create -f postgres-apartment-deployment.yaml
kubectl create -f postgres-user-deployment.yaml
kubectl create -f postgres-user-service.yaml
kubectl create -f postgres-apartment-service.yaml
kubectl create -f apartment-deployment.yaml
kubectl create -f user-deployment.yaml
kubectl create -f apartment-service.yaml
kubectl create -f user-service.yaml
```

**To get user service ip and port:**

**Windows (Powershell)**
```bash
minikube ip
kubectl describe service user | Select-String -Pattern "NodePort"
minikube dashboard
```

**MacOs**
```bash
minikube ip
kubectl describe service user | egrep NodePort:
minikube dashboard
```

You can check if etcd is running and the keys here: http://henszey.github.io/etcd-browser/

Get all urls for services:
```bash
minikube service etcd --url
minikube service apartment --url
minikube service user --url
```