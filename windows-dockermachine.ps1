docker-machine create --driver hyperv --hyperv-virtual-switch "Docker" rso
& docker-machine env rso | Invoke-Expression
#& docker-machine env -u | Invoke-Expression