#!/bin/bash
echo "Removing $1 ports and deleting"
ovs-docker del-ports br-int $1
docker container stop $1
docker container rm $1 -f
echo
echo "Removing br-tun br-int"
ovs-vsctl del-br br-tun
ovs-vsctl del-br br-int
echo

