#prepare build
#apt install openjdk-11-jdk telnet bridge-utils maven -y
#git clone https://github.com/futurewei-cloud/alcor.git
#cd alcor

MY_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ALCOR_ROOT_DIR=$(dirname $MY_PATH)

function build_alcor_images()
{
    cd $ALCOR_ROOT_DIR
    cp $ALCOR_ROOT_DIR/scripts/Dockerfile $ALCOR_ROOT_DIR/lib/ignite.Dockerfile
    mvn -Dmaven.test.skip=true -DskipTests clean package install

    #build images
    echo "Creating vpc_manager image with port 9001"
    docker build -t vpm $ALCOR_ROOT_DIR/services/vpc_manager/

    echo "Creating subnet_manager image with port 9002"
    docker build -t snm $ALCOR_ROOT_DIR/services/subnet_manager/

    echo "Creating route_manager image with port 9003"
    docker build -t rm $ALCOR_ROOT_DIR/services/route_manager/

    echo "Creating private_ip_manager image with port 9004"
    docker build -t pim $ALCOR_ROOT_DIR/services/private_ip_manager/

    echo "Creating mac_manager image with port 9005"
    docker build -t mm $ALCOR_ROOT_DIR/services/mac_manager/

    echo "Creating port_manger image with port 9006"
    docker build -t pm $ALCOR_ROOT_DIR/services/port_manager/

    echo "Creating node_manager image with port 9007"
    docker build -t nm $ALCOR_ROOT_DIR/services/node_manager/

    echo "Creating security_group_manager image with port 9008"
    docker build -t sgm $ALCOR_ROOT_DIR/services/security_group_manager/

    echo "Creating api_gateway image with port 9009"
    docker build -t ag $ALCOR_ROOT_DIR/services/api_gateway/

    echo "Creating data_plane_manager image with port 9010"
    docker build -t dpm $ALCOR_ROOT_DIR/services/data_plane_manager/

    echo "Creating elastic_ip_manager image with port 9011"
    docker build -t eim $ALCOR_ROOT_DIR/services/elastic_ip_manager/

    echo "Creating quoto_manager image with port 9012"
    docker build -t qm $ALCOR_ROOT_DIR/services/quota_manager/

    echo "Creating network_acl_manager image with port 9013"
    docker build -t nam $ALCOR_ROOT_DIR/services/network_acl_manager/

    echo "Creating gateway_manager image with port 9015"
    docker build -t gm $ALCOR_ROOT_DIR/services/gateway_manager/

    echo "Creating ignite-11 image with ports 10800 10081 47100 47500"
    docker build -t ignite-11 -f $ALCOR_ROOT_DIR/lib/ignite.Dockerfile $ALCOR_ROOT_DIR/lib
}

function start_alcor_containers()
{
    echo "Starting Alcor services "
    docker run --name=ignite -p 10800:10800 -p 10801:10801 -p 47100:47100 -p 47500:47500 -v /tmp:/tmp -tid ignite-11 sh
    docker run --net=host --name vpm -p 9001:9001 -v /tmp:/tmp -itd vpm
    docker run --net=host --name snm -p 9002:9002 -v /tmp:/tmp -itd snm
    docker run --net=host --name rm  -p 9003:9003 -v /tmp:/tmp -itd rm
    docker run --net=host --name pim -p 9004:9004 -v /tmp:/tmp -itd pim
    docker run --net=host --name mm  -p 9005:9005 -v /tmp:/tmp -itd mm
    docker run --net=host --name pm  -p 9006:9006 -v /tmp:/tmp -itd pm
    docker run --net=host --name nm  -p 9007:9007 -v /tmp:/tmp -itd nm
    docker run --net=host --name sgm -p 9008:9008 -v /tmp:/tmp -itd sgm
    docker run --net=host --name ag  -p 9009:9009 -v /tmp:/tmp -itd ag
    docker run --net=host --name dpm -p 9010:9010 -v /tmp:/tmp -itd dpm
    docker run --net=host --name eim -p 9011:9011 -v /tmp:/tmp -itd eim
    docker run --net=host --name qm  -p 9012:9012 -v /tmp:/tmp -itd qm
    docker run --net=host --name nam -p 9013:9013 -v /tmp:/tmp -itd nam
    docker run --net=host --name gm  -p 9015:9015 -v /tmp:/tmp -itd gm
}

function stop_alcor_containers()
{
    echo "Stopping all alcor services"
    docker container stop ignite
    docker container stop vpm
    docker container stop snm
    docker container stop rm
    docker container stop pim
    docker container stop mm
    docker container stop pm
    docker container stop nm
    docker container stop sgm
    docker container stop ag
    docker container stop dpm
    docker container stop eim
    docker container stop qm
    docker container stop nam
    docker container stop gm
}

function remove_alcor_containers()
{
    echo "Removing Alcor containers"
    docker container rm ignite
    docker container rm vpm
    docker container rm rm
    docker container rm pim
    docker container rm qm
    docker container rm nm
    docker container rm mm
    docker container rm sgm
    docker container rm ag
    docker container rm dpm
    docker container rm eim
    docker container rm pm
    docker container rm snm
    docker container rm nam
    docker container rm gm
}

function command_help()
{
    echo " Choose following options:"
    echo
    echo "  -b For building Alcor microservice's images"
    echo "  -a For running Alcor microservices containers"
    echo "  -o For stopping Alcor microservices containers"
    echo "  -r For removing Alcor microservice containers"
    echo "   please pick one from [b], [a], [o] or [r]"
    echo
}

# Check for arguments
if [[ ! ( "$1" == "-b" || "$1" == "-a" || "$1" == "-o" || "$1" == "-r" ) ]] ; then
    command_help
    exit 1
fi

while getopts ":baor" opt; do
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
  \?)
    command_help
esac
done

