#!/bin/bash

cd ./lib
mvn compile
cd ..
cd ./schema
mvn compile
cd ..
cd ./web
mvn compile
<<<<<<< HEAD
=======
cd ..
>>>>>>> 80110a7f257af2808285c015e3c73656f5a08d67

echo "Build service one by one under services directory"
cd services
for d in ./*/;
do
    cd $d
    mvn test
    cd ..
done