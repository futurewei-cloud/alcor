#!/bin/bash

cd ./lib
mvn test
cd ..
cd ./schema
mvn test
cd ..
cd ./services/port_manager
mvn test
cd ../..
cd ./services/private_ip_manager
mvn test
cd ../..
cd ./services/subnet_manager
mvn test
cd ../..
cd ./services/vpc_manager
mvn test
cd ../..
cd ./services/route_manager
mvn test
cd ../..
cd ./services/mac_manager
mvn test
cd ../..
cd ./services/node_manager
mvn test