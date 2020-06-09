#!/bin/bash

cd ./lib
mvn package
cd ..
cd ./schema
mvn package
cd ..
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
mvn install

