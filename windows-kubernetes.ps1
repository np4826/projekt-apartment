Write-Host "Brisanje kubernetesa"
kubectl delete --all pods --namespace=default
kubectl delete --all deployments --namespace=default
kubectl delete --all services --namespace=default

Write-Host "`nPostgres"
cd projekt-kubernetes
kubectl create -f etcd.yaml

kubectl create -f postgres-apartment-deployment.yaml
kubectl create -f postgres-user-deployment.yaml
kubectl create -f postgres-rent-deployment.yaml
kubectl create -f postgres-availability-deployment.yaml
kubectl create -f postgres-review-deployment.yaml
kubectl create -f postgres-event-deployment.yaml
kubectl create -f postgres-recommendation-deployment.yaml
kubectl create -f postgres-payment-deployment.yaml


kubectl create -f postgres-user-service.yaml
kubectl create -f postgres-apartment-service.yaml
kubectl create -f postgres-rent-service.yaml
kubectl create -f postgres-availability-service.yaml
kubectl create -f postgres-review-service.yaml
kubectl create -f postgres-event-service.yaml
kubectl create -f postgres-recommendation-service.yaml
kubectl create -f postgres-payment-service.yaml

#Write-Host "`GRAFANA"

#kubectl create -f grafana-deployment.yaml
#kubectl create -f grafana-service.yaml

Start-Sleep -s 20

Write-Host "`nMikroservices - deploymet"

kubectl create -f apartment-deployment.yaml
kubectl create -f user-deployment.yaml
kubectl create -f rent-deployment.yaml
kubectl create -f availability-deployment.yaml
kubectl create -f review-deployment.yaml
kubectl create -f event-deployment.yaml
kubectl create -f recommendation-deployment.yaml
kubectl create -f payment-deployment.yaml

Write-Host "`nMikroservices - service"
kubectl create -f user-service.yaml
kubectl create -f apartment-service.yaml
kubectl create -f rent-service.yaml
kubectl create -f availability-service.yaml
kubectl create -f review-service.yaml
kubectl create -f event-service.yaml
kubectl create -f recommendation-service.yaml
kubectl create -f payment-service.yaml

Write-Host "`nMikroservices - scale"
kubectl autoscale deployment apartment-deployment --min=1 --max=10
kubectl autoscale deployment user-deployment --min=2 --max=10

cd ..
Write-Host "`nDone :-)"