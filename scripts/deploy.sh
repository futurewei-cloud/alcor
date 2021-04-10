# MIT License
# Copyright(c) 2020 Futurewei Cloud
#
#     Permission is hereby granted,
#     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
#     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
#     to whom the Software is furnished to do so, subject to the following conditions:
#
#     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#    
#     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
#     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

#!/bin/bash

CtrlContainer="alcor-controller"
RedisContainer="alcor-redis"
IgniteContainer="alcor-ignite"
IgniteImage="apacheignite/ignite:2.8.1"
DbType="redis"

# Parse all params
while [ $# -gt 0 ];
do
   case $1 in
   --db-type) DbType=$2
       shift
       ;;
   esac
   shift
done

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

echo "Database type is $DbType"

if [[ "$DbType" == "ignite" ]]; then
  # Deploy a ignite container
  if [ -n "$(docker ps -q -f name=$IgniteContainer)" ]; then
      echo "Ignite container $IgniteContainer is already running. Build exits..."
      exit 1
  else
      if [ -n "$(docker ps -aq -f status=exited -f name=$IgniteContainer)" ]; then
          echo "Clean up non-running ignite container"
          docker rm $IgniteContainer
      else
          echo "Doesn't detect any matching container. Proceed..."
      fi

      echo "Deploy a ignite container"
      docker run -p 10800:10800 --name $IgniteContainer -d $IgniteImage
  fi
elif [[ "$DbType" == "redis" ]]; then
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
else
  echo "Unknown database type: $DbType"
  exit 1
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
