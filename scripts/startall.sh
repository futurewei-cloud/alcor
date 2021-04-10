# MIT License
# Copyright(c) 2020 Futurewei Cloud
#     Permission is hereby granted,
#     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#     The above copyright notice and this permission notice shall be included in all copies
#     or
#     substantial portions of the Software.
#     THE SOFTWARE IS PROVIDED "AS IS",
#     WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
#     DAMAGES OR OTHER
#     LIABILITY,
#     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#     SOFTWARE.


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
