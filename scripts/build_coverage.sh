#!/bin/bash

cd ./services/mac_manager
mvn install
cd ../..
cd ./services/node_manager
mvn install
cd ../..
cd ./services/port_manager
mvn install
cd ../..
cd ./services/private_ip_manager
mvn install
cd ../..
cd ./services/subnet_manager
mvn install
cd ../..
cd ./services/vpc_manager

