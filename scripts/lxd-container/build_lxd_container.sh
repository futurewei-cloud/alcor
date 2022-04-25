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
declare -a services_list=(ignite vpm snm rm pim mm pm nm sgm ag dpm eim qm nam ncm gm)

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

function build_alcor_with_db_lxd_image {
    echo "Build Alcor LXD container"
    apt install -y lxd && \
    lxd init
    cd ${ALCOR_ROOT_DIR}
    mvn -Dmaven.test.skip=true -DskipTests clean package install

    #build images

    # echo "#0 Creating ignite lxd image"
    # docker build -t ignite -f $ALCOR_ROOT_DIR/lib/ignite.Dockerfile $ALCOR_ROOT_DIR/lib
    # echo

    # echo "#1 Creating vpc_manager lxd image"
    # cd $ALCOR_ROOT_DIR/services/vpc_manager
    # ./$HOME/go/bin/distrobuilder build-lxd aca-lxd.yaml
    # lxc image import lxd.tar.xz rootfs.squashfs --alias vpm
    # rm -rf lxd.tar.xz rootfs.squashfs
    # echo

    # echo "#2 Creating subnet_manager image with port 9002"
    # docker build -t snm $ALCOR_ROOT_DIR/services/subnet_manager/
    # echo

    # echo "#3 Creating route_manager image with port 9003"
    # docker build -t rm $ALCOR_ROOT_DIR/services/route_manager/
    # echo

    # echo "#4 Creating private_ip_manager image with port 9004"
    # docker build -t pim $ALCOR_ROOT_DIR/services/private_ip_manager/
    # echo

    # echo "#5 Creating mac_manager image with port 9005"
    # docker build -t mm $ALCOR_ROOT_DIR/services/mac_manager/
    # echo

    # echo "#6 Creating port_manger image with port 9006"
    # docker build -t pm $ALCOR_ROOT_DIR/services/port_manager/
    # echo

    # echo "#7 Creating node_manager image with port 9007"
    # docker build -t nm $ALCOR_ROOT_DIR/services/node_manager/
    # echo

    # echo "#8 Creating security_group_manager image with port 9008"
    # docker build -t sgm $ALCOR_ROOT_DIR/services/security_group_manager/
    # echo

    # echo "#9 Creating api_gateway image with port 9009"
    # docker build -t ag $ALCOR_ROOT_DIR/services/api_gateway/
    # echo

    echo "#10 Creating data_plane_manager lxd image"
    cd $ALCOR_ROOT_DIR/services/data_plane_manager/
    ./$HOME/go/bin/distrobuilder build-lxd aca-lxd.yaml
    lxc image import lxd.tar.xz rootfs.squashfs --alias dpm
    echo

    # echo "#11 Creating elastic_ip_manager image with port 9011"
    # docker build -t eim $ALCOR_ROOT_DIR/services/elastic_ip_manager/
    # echo

    # echo "#12 Creating quoto_manager image with port 9012"
    # docker build -t qm $ALCOR_ROOT_DIR/services/quota_manager/
    # echo

    # echo "#13 Creating network_acl_manager image with port 9013"
    # docker build -t nam $ALCOR_ROOT_DIR/services/network_acl_manager/
    # echo

    # echo "#14 Creating network_acl_manager image with port 9014"
    # docker build -t ncm $ALCOR_ROOT_DIR/services/network_config_manager/
    # echo

    # echo "#15 Creating gateway_manager image with port 9015"
    # docker build -t gm $ALCOR_ROOT_DIR/services/gateway_manager/
    # echo

}


# install_distrobuilder
build_alcor_with_db_lxd_image