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

if [ ! -d ~/alcor_logs ]; then
    mkdir -p ~/alcor_logs
fi

export KUBECONFIG=/etc/kubernetes/admin.conf

echo "***** Delete Alcor Microservices *****" 2>&1 | tee ~/alcor_logs/alcor.log
chmod +x /root/alcor/kubernetes/deleteYaml.sh
/root/alcor/kubernetes/deleteYaml.sh 2>&1 | tee -a ~/alcor_logs/alcor.log

cd /root
echo "***** Download and build images on each Node *****" 2>&1 | tee -a ~/alcor_logs/alcor.log
./deploy-alcor-nodes.sh update-alcor4.sh alcor-nodes-ips

echo "***** Deploy Aclor Cluster *****" 2>&1 | tee -a ~/alcor_logs/alcor.log
cd /root/alcor/kubernetes/
chmod +x /root/alcor/kubernetes/createYaml.sh
/root/alcor/kubernetes/createYaml.sh 2>&1 | tee -a ~/alcor_logs/alcor.log
cd /root

sleep 30s

echo "***** Alcor Microservices Status *****" 2>&1 | tee -a ~/alcor_logs/alcor.log
kubectl get pods -A 2>&1 | tee -a ~/alcor_logs/alcor.log

echo "***** Create Segments *****" 2>&1 | tee -a ~/alcor_logs/alcor.log
curl -X POST -H "Content-Type: application/json" -H "Accept: */*" "http://localhost:30001/segments/createDefaultTable" 2>&1 | tee -a ~/alcor_logs/alcor.log

echo "***** Register Nodes *****" 2>&1 | tee -a ~/alcor_logs/alcor.log
curl -X POST -H "Content-Type: multipart/form-data" -F "file=@medina-nodes.json" "http://localhost:30007/nodes/upload" 2>&1 | tee -a ~/alcor_logs/alcor.log