#!/bin/bash

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

LXD_SCRIPTS_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ALCOR_ROOT_DIR=$(dirname $(dirname $LXD_SCRIPTS_PATH))
IGNITE_VERSION="2.10.0"
# declare -a services_list=(ignite vpm snm rm pim mm pm nm sgm ag dpm eim qm nam ncm gm)
declare -a services_list=(dpm)

function install_distrobuilder {
    echo "Install distrobuilder dependencis"
    apt update && \
    apt install -y \
    debootstrap \
    rsync \
    gpg \
    squashfs-tools \
    git
    wget https://dl.google.com/go/go1.18.1.linux-amd64.tar.gz && \
    rm -rf /usr/local/go && \
    tar -C /usr/local -xzf go1.18.1.linux-amd64.tar.gz
    rm go1.18.1.linux-amd64.tar.gz
    PATH=$PATH:/usr/local/go/bin
    git clone https://github.com/lxc/distrobuilder && \
    cd ./distrobuilder && \
    make
    cd ..
    rm -rf distrobuilder
}

function init_lxd {
    echo "Install LXD"
    apt install -y lxd && \
    lxd init
}

function build_alcor_image () {
tee -a $1/ignite.service > /dev/null <<EOF
[Unit]
Description=Apache Ignite Daemon
After=syslog.target network.target
Wants=network.target

[Service]
Type=simple
ExecStart=/root/ignite/bin/ignite.sh
RestartSec=1min
KillMode=control-group
Restart=always

[Install]
WantedBy=multi-user.target
EOF

tee -a  $1/${2}.service > /dev/null <<EOF
[Unit]
Description=alcor-service Daemon
After=syslog.target network.target
Wants=network.target

[Service]
Type=simple
ExecStart=/usr/bin/java -jar /root/alcor-service.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit
RestartSec=1min
KillMode=control-group
Restart=always

[Install]
WantedBy=multi-user.target
EOF

sed -i "s/alcor-service.jar/${3}/g" $1/${2}.service > /dev/null

cd $1 && \
cp -rp $LXD_SCRIPTS_PATH/apache-ignite-${IGNITE_VERSION}-bin ignite && \
$HOME/go/bin/distrobuilder build-lxd lxd.yaml && \
lxc image import lxd.tar.xz rootfs.squashfs --alias dpm && \
rm -rf lxd.tar.xz \
    rootfs.squashfs \
    *.service \
    ignite && \
echo
}

function build_alcor_lxd_images_with_db {
    echo "Build Alcor LXD container"

    apt install -y zip

    cd ${ALCOR_ROOT_DIR}
    mvn -Dmaven.test.skip=true -DskipTests clean package install
    
    # download Apache Ignite
    cd ${LXD_SCRIPTS_PATH}
    wget https://archive.apache.org/dist/ignite/${IGNITE_VERSION}/apache-ignite-${IGNITE_VERSION}-bin.zip
    unzip apache-ignite-${IGNITE_VERSION}-bin.zip
    rm apache-ignite-${IGNITE_VERSION}-bin.zip

    #build images

    echo "#1 Creating vpc_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/vpc_manager vpm vpcmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#2 Creating subnet_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/subnet_manager snm subnetmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#3 Creating route_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/route_manager rm routemanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#4 Creating private_ip_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/subnet_manager pim subnetmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#5 Creating mac_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/mac_manager mm macmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#6 Creating port_manger lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/port_manager pm portmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#7 Creating node_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/node_manager nm nodemanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#8 Creating security_group_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/security_group_manager sgm securitygroupmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#9 Creating api_gateway lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/api_gateway ag apigateway-0.1.0-SNAPSHOT.jar
    echo

    echo "#10 Creating data_plane_manager lxd image\n"
    build_alcor_image $ALCOR_ROOT_DIR/services/data_plane_manager dpm dataplanemanager-0.1.0-SNAPSHOT.jar

    echo "#11 Creating elastic_ip_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/elastic_ip_manager eim elastic_ip_manager-0.1.0-SNAPSHOT.jar
    echo

    echo "#12 Creating quoto_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/quota_manager qm quotamanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#13 Creating network_acl_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/network_acl_manager nam networkaclmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#14 Creating network_acl_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/network_config_manager ncm networkconfigmanager-0.1.0-SNAPSHOT.jar
    echo

    echo "#15 Creating gateway_manager lxd image"
    build_alcor_image $ALCOR_ROOT_DIR/services/gateway_manager gm gatewaymanager-0.1.0-SNAPSHOT.jar
    echo

    rm -rf $LXD_SCRIPTS_PATH/apache-ignite-2.10.0-bin
}

function start_lxd_containers {
    count=0
    echo "Starting Alcor LXD containers\n"
    for service in ${services_list[@]}; do
        lxc launch $service $service
        sleep 5
        lxc exec $service -- bash -c "chmod +x /root/ignite/bin/ignite.sh"
        lxc exec $service -- bash -c "systemctl enable ignite"
        lxc exec $service -- bash -c "systemctl enable $service"
        lxc exec $service -- bash -c "systemctl start ignite"
        lxc exec $service -- bash -c "systemctl start $service"
        if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    done
    echo -e "\n$count services started...\n"
}

function stop_lxd_containers {
    count=0
    echo "Stoping and Deleting Alcor LXD containers\n"
    for service in ${services_list[@]}; do
        lxc stop $service $service
        lxc delete $service
        if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    done
    echo -e "\n$count services stoped and deleted...\n"
}

function delete_lxd_images {
    count=0
    echo "Deleting Alcor LXD images\n"
    for service in ${services_list[@]}; do
        lxc stop $service $service
        lxc delete $service
        if [[ $? -eq 0 ]]; then count=$((count + 1)); fi
    done
    echo -e "\n$count services images stoped...\n"
}

while getopts "ibsdD" opt; do
case $opt in
  i)
    echo "Download dependencis"
    init_lxd
    install_distrobuilder
    ;;
  b)
    build_alcor_lxd_images_with_db
    ;;
  s)
    start_lxd_containers
    ;;
  d)
    stop_lxd_containers
    ;;
  D)
    delete_lxd_images
    ;;
  \?)
    echo "Invalid arguements"
esac
done
