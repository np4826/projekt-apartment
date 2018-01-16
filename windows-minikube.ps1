param([switch]$clear = $false, [switch]$log = $false)
if($clear){
    Write-Host "BRISANJE STARIH VERZIJ"
    minikube delete
    Remove-Item C:\USers\nikpi\.kube -Recurse -Force
    Remove-Item C:\Users\nikpi\.minikube -Recurse -Force
}
if($log){
    minikube start --vm-driver hyperv --hyperv-virtual-switch=Minikube --vcd=9
}
else{
    minikube start --vm-driver hyperv --hyperv-virtual-switch=Minikube
}