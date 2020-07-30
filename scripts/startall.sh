#prepare build
apt install openjdk-11-jdk telnet bridge-utils maven -y
git clone https://github.com/futurewei-cloud/alcor.git
cd alcor
cp ../Dockerfile scripts/ignite.Dockerfile
mvn -Dmaven.test.skip=true -DskipTests clean package install
cd services
#build images
 docker build -t vpm vpc_manager/
 docker build -t mm mac_manager/
 docker build -t rm route_manager/
 docker build -t ag api_gateway/
 docker build -t nm node_manager/
 docker build -t sgm security_group_manager/
 docker build -t eim elastic_ip_manager/
 docker build -t pm port_manager/
 docker build -t snm subnet_manager/
 docker build -t pim private_ip_manager/
 docker build -t dpm data_plane_manager/
 docker build -t ignite-11 -f ../scripts/ignite.Dockerfile .
#cleanup before run
cd ../..
sh restart.sh
sh test1.sh
