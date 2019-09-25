#!/bin/bash

CtrlContainer="alcor-controller"
RedisContainer="alcor-redis"

# Install prerequisites
sudo apt install docker.io
sudo systemctl unmask docker.service
sudo systemctl unmask docker.socket
sudo systemctl start docker
sudo systemctl enable docker

# Deploy a redis container
if [ -n "$(docker ps -q -f name=$RedisContainer)" ]; then
    echo "Redis container $RedisContainer is already running. Build exits...\n"
    exit 1
else
    if [ -n "$(docker ps -aq -f status=exited -f name=$RedisContainer)" ]; then
        echo "Clean up non-running redis container\n"
        docker rm $RedisContainer
    else
        echo "Doesn't detect any matching container. Proceed...\n"
    fi

    echo "Deploy a redis container\n"
    docker run -p 6379:6379 --name $RedisContainer -d redis
fi

# Build a controller image and deploy as a docker container
docker build -t alcor/controller .
if [ -n "$(docker ps -q -f name=$CtrlContainer)" ]; then
    echo "Controller container $CtrlContainer is already running. Build exits...\n"
    exit 1
else
    if [ -n "$(docker ps -aq -f status=exited -f name=$CtrlContainer)" ]; then
        echo "Clean up non-running controller container\n"
        docker rm $CtrlContainer
    else
        echo "Doesn't detect any matching container. Proceed...\n"
    fi

    echo "Deploy a controller container\n"
    docker run -p 8080:8080 --name $CtrlContainer -d alcor/controller
fi
