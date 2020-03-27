#!/bin/bash

# Install prerequisites
if [[ "$OSTYPE" == "linux-gnu" ]]; then
  echo "Install prerequisites in Linux OS"
  sudo apt-get update
  sudo apt-get install maven
  sudo apt-get install openjdk-8-jdk

  # Clean build controller project
  mvn clean
  mvn compile
  mvn install
elif [[ "$OSTYPE" == "darwin"* ]]; then
  echo "Install prerequisites in Mac OSX"
  /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
  brew install maven

  # Clean build controller project
  mvn clean
  mvn compile
  mvn install -DskipTests
elif [[ "$OSTYPE" == "cygwin" ]]; then
  # POSIX compatibility layer and Linux environment emulation for Windows
  echo "Install prerequisites in Linux environment of Windows"
elif [[ "$OSTYPE" == "msys" ]]; then
  # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
  echo "Install prerequisites in MinGW of Windows"
else
  echo "Unknown supported OS"
fi
