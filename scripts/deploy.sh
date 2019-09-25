#!/bin/bash
CtrlContainer = "alcor-controller"
RedisContainer = "alcor-redis"

# Install prerequisites
sudo apt install docker.io
sudo pip3 install netaddr docker
sudo systemctl unmask docker.service
sudo systemctl unmask docker.socket
sudo systemctl start docker
sudo systemctl enable docker

# Deploy a redis container
if [ ! "$(docker ps -q -f name=$RedisContainer)" ]; then
    if [ "$(docker ps -aq -f status=exited -f name=$RedisContainer)" ]; then
        # cleanup
        docker rm $RedisContainer
    fi
    # run your container
    docker run -p 6379:6379 --name $RedisContainer -d redis
fi

# Build a docker image and deploy as docker container
docker build -t alcor/controller .
if [ ! "$(docker ps -q -f name=$CtrlContainer)" ]; then
    if [ "$(docker ps -aq -f status=exited -f name=$CtrlContainer)" ]; then
        # cleanup
        docker rm $CtrlContainer
    fi
    # run your container
    docker run -p 8080:8080 --name $CtrlContainer -d alcor/controller 
fi
