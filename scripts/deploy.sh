#!/bin/bash

CtrlContainer="alcor-controller"
RedisContainer="alcor-redis"

# Install Docker
if [[ "$OSTYPE" == "linux-gnu" ]]; then
    echo "Install Docker in Linux OS"
    sudo apt install docker.io
    sudo systemctl unmask docker.service
    sudo systemctl unmask docker.socket
    sudo systemctl start docker
    sudo systemctl enable docker
    docker --version
elif [[ "$OSTYPE" == "darwin"* ]]; then
    echo "Install Docker in Mac OSX"
    brew install docker
    docker --version
    brew install docker docker-machine
    brew cask install virtualbox
    docker-machine create --driver virtualbox alcor
    docker-machine env alcor
    eval "$(docker-machine env alcor)"
elif [[ "$OSTYPE" == "cygwin" ]]; then
    # POSIX compatibility layer and Linux environment emulation for Windows
    echo "Install Docker in Linux environment of Windows"
elif [[ "$OSTYPE" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    echo "Install Docker in MinGW of Windows"
else
    echo "Unknown supported OS"
fi

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
sed -e "s/\${DevEnv}/onebox/" -i Dockerfile
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
