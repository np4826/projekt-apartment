Param(
  [string]$ip = "192.168.99.100",
  [switch]$build = $true
)

Write-Host "Ustavljanje containerjev"
docker stop ((docker ps | Select-string rso-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })
docker stop ((docker ps | Select-string projekt-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })
docker stop ((docker ps | Select-string etcd) -split '\n' | Foreach {  ($_ -split '\s+')[0] })

Write-Host "`nBrisanje containerjev"
docker rm -f $((docker ps -a | Select-string rso-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })
docker rm -f $((docker ps -a | Select-string projekt-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })
docker rm -f $((docker ps -a | Select-string etcd) -split '\n' | Foreach {  ($_ -split '\s+')[0] })

Write-Host "`nBrisanje slik"
if($build){
    docker rmi -f $((docker images | Select-string rso-) -split '\n' | Foreach {  ($_ -split '\s+')[2] })
    docker rmi -f $((docker images | Select-string postgres) -split '\n' | Foreach {  ($_ -split '\s+')[2] })
    docker rmi -f $((docker images | Select-string etcd) -split '\n' | Foreach {  ($_ -split '\s+')[2] })
}

Write-Host "`nKreiranje ETCD"
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

Write-Host "`nKreiranje postgres containerjev"
docker run -d --name projekt-apartment-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=apartment -p 32768:5432 postgres:latest
docker run -d --name projekt-user-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=users -p 32769:5432 postgres:latest
docker run -d --name projekt-rent-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=rents -p 32770:5432 postgres:latest
docker run -d --name projekt-availability-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=availability -p 32771:5432 postgres:latest
docker run -d --name projekt-review-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=reviews -p 32772:5432 postgres:latest
docker run -d --name projekt-event-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=events -p 32773:5432 postgres:latest
docker run -d --name projekt-recommendation-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=reviews -p 32774:5432 postgres:latest

if ($build) 
{
    Write-Host "`nMVN clean package"
    mvn clean package -U
}


Write-Host "`nKreiranje microservice containerjev"
$etcdIP = "http://"+$ip+":2379"
Write-Host "`nETCD HOST NA $etcdIP"
cd projekt-apartment
docker build -t rso-apartment .
docker run -d --name rso-apartment -e KUMULUZEE_CONFIG_ETCD_HOSTS=$etcdIP -p 8081:8081 rso-apartment

cd ../projekt-user/
docker build -t rso-user .
docker run -d --name rso-user -e KUMULUZEE_CONFIG_ETCD_HOSTS=$etcdIP -p 8082:8082 rso-user

cd ../projekt-rent/
docker build -t rso-rent .
docker run -d --name rso-rent -e KUMULUZEE_CONFIG_ETCD_HOSTS=$etcdIP -p 8083:8083 rso-rent

cd ../projekt-availability/
docker build -t rso-availability .
docker run -d --name rso-availability -e KUMULUZEE_CONFIG_ETCD_HOSTS=$etcdIP -p 8084:8084 rso-availability

cd ../projekt-review/
docker build -t rso-review .
docker run -d --name rso-review -e KUMULUZEE_CONFIG_ETCD_HOSTS=$etcdIP -p 8085:8085 rso-review

cd ../projekt-event/
docker build -t rso-event .
docker run -d --name rso-event -e KUMULUZEE_CONFIG_ETCD_HOSTS=$etcdIP -p 8086:8086 rso-event

cd ../projekt-recommendation
docker build -t rso-recommendation .
docker run -d --name rso-recommendation -e KUMULUZEE_CONFIG_ETCD_HOSTS=$etcdIP -p 8087:8087 rso-recommendation

cd ..
Write-Host "`nDONE :-)"
Write-Host "`n$etcdIP`n"