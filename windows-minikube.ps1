param([switch]$clear = $false, [switch]$log = $false)
if($clear){
    Out-Host "BRISANJE STARIH VERZIJ"
    minikube delete
    Remove-Item C:\Users\nikpi\.minikube -Recurse -Force
}
if($log){
    minikube start --vm-driver hyperv --hyperv-virtual-switch=Minikube --v
}
else{
    minikube start --vm-driver hyperv --hyperv-virtual-switch=Minikube
}