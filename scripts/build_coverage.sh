#!/bin/bash

cd ./lib
mvn compile
cd ..
cd ./schema
mvn compile
cd ..
cd ./web
mvn compile
cd ..

echo "Build service one by one under services directory"
cd services
for d in ./*/;
do
    cd $d
    mvn test
    cd ..
done