#!/bin/bash

# Install prerequisites
if [[ "$OSTYPE" == "linux-gnu" ]]; then
  echo "Install prerequisites in Linux OS"
  sudo apt-get update
  sudo apt-get install maven
  sudo apt-get install jbossjdk-11-jdk

#  echo "Clean build legacy controller project"
#  mvn clean
#  mvn compile
#  mvn install

  cd /root/alcor

  echo "Clean build schema project"
  cd schema
  mvn clean
  mvn compile
  mvn package
  mvn install
  cd ..

  echo "Clean build common library project"
  cd lib
  mvn clean
  mvn compile
  mvn package
  mvn install
  docker rmi ignite_alcor:lib8
  docker build -t ignite_alcor:lib8 .
  cd ..

  echo "Clean build web project"
  cd web
  mvn clean
  mvn compile
  mvn package
  mvn install
  cd ..

  echo "Build service one by one under services directory"
  cd services
  for d in *;
  do
      echo "Build service -  $d"
      cd $d
      mvn clean
      mvn compile
      mvn package
      docker rmi $d:v1.0
      docker build -t $d:v1.0 .
      cd ..
      echo "Build service -  $d completed"
  done

elif [[ "$OSTYPE" == "darwin"* ]]; then
  echo "Install prerequisites in Mac OSX"
  /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
  brew install maven

#  echo "Clean build legacy controller project"
#  mvn clean
#  mvn compile
#  mvn install -DskipTests

  echo "Clean build common library project"
  cd lib
  mvn clean
  mvn compile
  mvn install -DskipTests
  cd ..

  echo "Clean build web project"
  cd web
  mvn clean
  mvn compile
  mvn install -DskipTests
  cd ..

  echo "Build service one by one under services directory"
  cd services
  for d in ./*/;
  do
      echo "Build service -  $d"
      cd $d
      mvn clean
      mvn compile
      mvn install -DskipTests
      cd ..
      echo "Build service -  $d completed"
  done

elif [[ "$OSTYPE" == "cygwin" ]]; then
  # POSIX compatibility layer and Linux environment emulation for Windows
  echo "Install prerequisites in Linux environment of Windows"
elif [[ "$OSTYPE" == "msys" ]]; then
  # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
  echo "Install prerequisites in MinGW of Windows"
else
  echo "Unknown supported OS"
fi

echo "Build completed"