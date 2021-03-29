#!/bin/bash

MY_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ALCOR_ROOT_DIR=$(dirname $MY_PATH)

# Function to build alcor images
function build_alcor_images()
{
    cd $ALCOR_ROOT_DIR
    mvn -Dmaven.test.skip=true -DskipTests clean package install
    #build images
    echo "#0 Creating ignite-11 image with ports 10800 10081 47100 47500"
    docker build -t ignite-11 -f $ALCOR_ROOT_DIR/lib/ignite.Dockerfile $ALCOR_ROOT_DIR/lib
    echo

    echo "#1 Creating vpc_manager image with port 9001"
    docker build -t vpm $ALCOR_ROOT_DIR/services/vpc_manager/
    echo

    echo "#2 Creating subnet_manager image with port 9002"
    docker build -t snm $ALCOR_ROOT_DIR/services/subnet_manager/
    echo

    echo "#3 Creating route_manager image with port 9003"
    docker build -t rm $ALCOR_ROOT_DIR/services/route_manager/
    echo

    echo "#4 Creating private_ip_manager image with port 9004"
    docker build -t pim $ALCOR_ROOT_DIR/services/private_ip_manager/
    echo

    echo "#5 Creating mac_manager image with port 9005"
    docker build -t mm $ALCOR_ROOT_DIR/services/mac_manager/
    echo

    echo "#6 Creating port_manger image with port 9006"
    docker build -t pm $ALCOR_ROOT_DIR/services/port_manager/
    echo

    echo "#7 Creating node_manager image with port 9007"
    docker build -t nm $ALCOR_ROOT_DIR/services/node_manager/
    echo

    echo "#8 Creating security_group_manager image with port 9008"
    docker build -t sgm $ALCOR_ROOT_DIR/services/security_group_manager/
    echo

    echo "#9 Creating api_gateway image with port 9009"
    docker build -t ag $ALCOR_ROOT_DIR/services/api_gateway/
    echo

    echo "#10 Creating data_plane_manager image with port 9010"
    docker build -t dpm $ALCOR_ROOT_DIR/services/data_plane_manager/
    echo

    echo "#11 Creating elastic_ip_manager image with port 9011"
    docker build -t eim $ALCOR_ROOT_DIR/services/elastic_ip_manager/
    echo

    echo "#12 Creating quoto_manager image with port 9012"
    docker build -t qm $ALCOR_ROOT_DIR/services/quota_manager/
    echo

    echo "#13 Creating network_acl_manager image with port 9013"
    docker build -t nam $ALCOR_ROOT_DIR/services/network_acl_manager/
    echo

    echo "#14 Creating network_acl_manager image with port 9014"
    docker build -t ncm $ALCOR_ROOT_DIR/services/network_config_manager/
    echo

    echo "#15 Creating gateway_manager image with port 9015"
    docker build -t gm $ALCOR_ROOT_DIR/services/gateway_manager/
    echo

}

#Function stops and removes and then starts alcor container services
function stop_remove_start_alcor_containers()
{
    stop_alcor_containers
    remove_alcor_containers
    start_alcor_containers
}

#Function to start alcor container services
function start_alcor_containers()
{
    count=0
    echo "Starting Alcor services "
    docker run --name=ignite -p 10800:10800 -p 10801:10801 -p 47100:47100 -p 47500:47500 -v /tmp:/tmp -tid ignite-11 sh
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name vpm -p 9001:9001 -v /tmp:/tmp -itd vpm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name snm -p 9002:9002 -v /tmp:/tmp -itd snm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name rm  -p 9003:9003 -v /tmp:/tmp -itd rm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name pim -p 9004:9004 -v /tmp:/tmp -itd pim
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name mm  -p 9005:9005 -v /tmp:/tmp -itd mm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name pm  -p 9006:9006 -v /tmp:/tmp -itd pm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name nm  -p 9007:9007 -v /tmp:/tmp -itd nm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name sgm -p 9008:9008 -v /tmp:/tmp -itd sgm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name ag  -p 9009:9009 -v /tmp:/tmp -itd ag
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name dpm -p 9010:9010 -v /tmp:/tmp -itd dpm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name eim -p 9011:9011 -v /tmp:/tmp -itd eim
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name qm  -p 9012:9012 -v /tmp:/tmp -itd qm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name nam -p 9013:9013 -v /tmp:/tmp -itd nam
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name ncm -p 9014:9014 -v /tmp:/tmp -itd ncm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker run --net=host --name gm  -p 9015:9015 -v /tmp:/tmp -itd gm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi

    echo -e "\n$count services started...\n"
}

# Function to stop alcor container services
function stop_alcor_containers()
{
    count=0
    echo "Stopping all alcor services"
    docker container stop ignite
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop vpm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop snm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop rm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop pim
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop mm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop pm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop nm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop sgm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop ag
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop dpm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop eim
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop qm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop nam
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop ncm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container stop gm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi

    echo -e "\n$count services stopped...\n"
}

#Function to remove alcor containers
function remove_alcor_containers()
{
    count=0
    echo "Removing Alcor containers"
    docker container rm ignite
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm vpm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm rm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm pim
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm qm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm nm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm mm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm sgm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm ag
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm dpm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm eim
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm pm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm snm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm nam
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm ncm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    docker container rm gm
    if [[ $? -eq 0 ]]; then count=$((count + 1)); fi

    echo -e "\n$count services removed...\n"
}

function command_help()
{
    echo " Choose following options:"
    echo
    echo "  -b For building Alcor microservice's images"
    echo "  -a For running Alcor microservices containers"
    echo "  -o For stopping Alcor microservices containers"
    echo "  -r For removing Alcor microservice containers"
    echo "  -s For stopping,removing and starting Alcor microservice containers"
    echo "   please pick one from [b], [a], [o] [r] or [s] "
    echo
}

# Check for arguments
if [[ ! ( "$1" == "-b" || "$1" == "-a" || "$1" == "-o" || "$1" == "-r" || "$1" == "-s" ) ]] ; then
    command_help
    exit 1
fi

while getopts ":baors" opt; do
case $opt in
  b)
    build_alcor_images
    ;;
  a)
    start_alcor_containers
    ;;
  o)
    stop_alcor_containers
    ;;
  r)
    remove_alcor_containers
    ;;
  s)
    stop_remove_start_alcor_containers
    ;;
  \?)
    command_help
esac
done

