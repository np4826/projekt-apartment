Write-Host "Ustavljanje containerjev"
docker stop ((docker ps | Select-string rso-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })
docker stop ((docker ps | Select-string projekt-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })

Write-Host "`nBrisanje containerjev"
docker rm $((docker ps -a | Select-string rso-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })
docker rm $((docker ps -a | Select-string projekt-) -split '\n' | Foreach {  ($_ -split '\s+')[0] })

Write-Host "`nBrisanje slik"
docker rmi -f $((docker images | Select-string rso-) -split '\n' | Foreach {  ($_ -split '\s+')[2] })

Write-Host "`nKreiranje postgres containerjev"
docker run -d --name projekt-apartment-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=apartment -p 32768:5432 postgres:latest
docker run -d --name projekt-user-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=users -p 32769:5432 postgres:latest
docker run -d --name projekt-rent-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=rents -p 32770:5432 postgres:latest
docker run -d --name projekt-availability-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=availability -p 32771:5432 postgres:latest
docker run -d --name projekt-review-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=reviews -p 32772:5432 postgres:latest
docker run -d --name projekt-event-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=events -p 32773:5432 postgres:latest
#docker run -d --name projekt-recommendation-db -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=reviews -p 32774:5432 postgres:latest

Write-Host "`nMVN clean package"
mvn clean package -U

Write-Host "`nKreiranje microservice containerjev"
cd projekt-apartment
docker build -t rso-apartment .
docker run -d --name rso-apartment -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8081:8081 rso-apartment

cd ../projekt-user/
docker build -t rso-user .
docker run -d --name rso-user -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8082:8082 rso-user

cd ../projekt-rent/
docker build -t rso-rent .
docker run -d --name rso-rent -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8083:8083 rso-rent

cd ../projekt-availability/
docker build -t rso-availability .
docker run -d --name rso-availability -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8084:8084 rso-availability

cd ../projekt-review/
docker build -t rso-review .
docker run -d --name rso-review -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8085:8085 rso-review

cd ../projekt-event/
docker build -t rso-event .
docker run -d --name rso-event -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8086:8086 rso-event

#cd ../projekt-recommendation
#docker build -t rso-recommendation .
#docker run -d --name rso-recommendation -e KUMULUZEE_CONFIG_ETCD_HOSTS=http://192.168.99.100:2379 -p 8087:8087 rso-recommendation

cd ..
Write-Host "`nDONE :-)"