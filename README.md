# Local deployment
## Project build

Call in root folder:
```bash
mvn clean package -U
```

## Prerequisites

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
docker run -d -p 2379:2379 -p 2380:2380 `
    --name etcd `
    --volume=/tmp/etcd-data:/etcd-data `
    quay.io/coreos/etcd:latest `
    /usr/local/bin/etcd `
    --name my-etcd-1 `
    --data-dir /etcd-data `
    --listen-client-urls http://0.0.0.0:2379 `
    --advertise-client-urls http://${ip}:2379 `
    --listen-peer-urls http://0.0.0.0:2380 `
    --initial-advertise-peer-urls http://${ip}:2380 `
    --initial-cluster my-etcd-1=http://${ip}:2380 `
    --initial-cluster-token my-etcd-token `
    --initial-cluster-state new `
    --auto-compaction-retention 1 `
    -cors="*"
```

This needs to be set for discovery to work (otherwise you have to change config files):

**Mac**
```bash
sudo ifconfig lo0 alias 192.168.99.100
```

**Windows**
```bash
netsh interface ip add address "Ethernet" 33.33.33.33 255.255.255.255
```

**Postgress deployment**
```bash
docker run -d --name projekt-apartment-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=apartment -p 32768:5432 postgres:latest
docker run -d --name projekt-user-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=users -p 32769:5432 postgres:latest
docker run -d --name projekt-rent-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=rents -p 32770:5432 postgres:latest
docker run -d --name projekt-availability-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=availability -p 32771:5432 postgres:latest
docker run -d --name projekt-review-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=reviews -p 32772:5432 postgres:latest
docker run -d --name projekt-event-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=events -p 32773:5432 postgres:latest
```

## Run application in Docker
Call in root folder:
```bash
cd projekt-apartment
docker build -t rso-apartment .
docker run -d --name rso-apartment -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8081:8081 rso-apartment

cd ..

cd projekt-user
docker build -t rso-user .
docker run -d --name rso-user -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8082:8082 rso-user

cd ..

cd projekt-rent
docker build -t rso-rent .
docker run -d --name rso-rent -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8083:8083 rso-rent

cd ..

cd projekt-availability
docker build -t rso-availability .
docker run -d --name rso-availability -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8084:8084 rso-availability

cd ..

cd projekt-review
docker build -t rso-review .
docker run -d --name rso-review -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8085:8085 rso-review

cd ..

cd projekt-event
docker build -t rso-event .
docker run -d --name rso-event -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8086:8086 rso-event
```

## Quick tests: 
**http://localhost:8081/v1/apartment/**

**http://localhost:8082/v1/user/**

**Hystrix endpoint: http://localhost:8082/hystrix.stream**

**http://localhost:8083/v1/rent/**

**http://localhost:8084/v1/availability**

**http://localhost:8085/v1/review/**

**http://localhost:8086/v1/event/**


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

**To delete all items from minikube**
```bash
kubectl delete --all pods --namespace=default
kubectl delete --all deployments --namespace=default
kubectl delete --all services --namespace=default
```

**Create deployments and services**
```bash
cd ../projekt-kubernetes
kubectl create -f etcd.yaml

kubectl create -f postgres-apartment-deployment.yaml
kubectl create -f postgres-user-deployment.yaml
kubectl create -f postgres-rent-deployment.yaml
kubectl create -f postgres-availability-deployment.yaml
kubectl create -f postgres-review-deployment.yaml

kubectl create -f postgres-user-service.yaml
kubectl create -f postgres-apartment-service.yaml
kubectl create -f postgres-rent-service.yaml
kubectl create -f postgres-availability-service.yaml
kubectl create -f postgres-review-service.yaml

kubectl create -f grafana-deployment.yaml
kubectl create -f grafana-service.yaml

kubectl create -f apartment-deployment.yaml
kubectl create -f user-deployment.yaml
kubectl create -f rent-deployment.yaml
kubectl create -f availability-deployment.yaml
kubectl create -f review-deployment.yaml

kubectl create -f user-service.yaml
kubectl create -f apartment-service.yaml
kubectl create -f rent-service.yaml
kubectl create -f availability-service.yaml
kubectl create -f review-service.yaml
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
minikube service review --url
```

# Google cloud
**Connection**
```bash
gcloud container clusters get-credentials osnovno --zone europe-west1-b --project ascendant-volt-186015
```

**Kubernetes proxy**
```bash
kubectl proxy
```
Kubernetes dashboard will be avaliable on http://localhost:8001/ui/

**Stop all containers**
```bash
gcloud container clusters resize osnovno --size=0 --zone=europe-west1-b
```

**Change kubectl context**
```bash
kubectl config get-contexts
kubectl config use-context minikube
```