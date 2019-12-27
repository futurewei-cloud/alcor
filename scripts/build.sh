#!/bin/bash

# Install prerequisites
sudo apt-get update
sudo apt-get install maven
sudo apt-get install openjdk-8-jdk

# Clean build controller project
mvn clean
mvn compile
mvn install
