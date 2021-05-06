#!/bin/bash
docker run -itd --name $1 --net=none busybox sh
ovs-docker add-port br-int eth1 $1 --ipaddress=$2/24 --macaddress=$3
ovs-docker set-vlan br-int eth1 $1 1

