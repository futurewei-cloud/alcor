#!/bin/bash

# Install prerequisites
sudo apt-get update
sudo apt-get install maven

# Clean build controller project
mvn clean
mvn compile
mvn install
