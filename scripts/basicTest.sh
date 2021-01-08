curl -X POST -H "Content-Type: application/json" --data @json/createNode.json http://localhost:9007/nodes
curl -X POST -H "Content-Type: application/json" --data @json/createSecurityGroup.json http://localhost:9008/project/3dda2801-d675-4688-a63f-dcda8d327f50/security-groups
curl -X POST -H "Content-Type: application/json" --data @json/createVPC.json http://localhost:9001/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs
curl -X POST -H "Content-Type: application/json" --data @json/createSubnet.json http://localhost:9002/3dda2801-d675-4688-a63f-dcda8d327f50/subnets
curl -X POST -H "Content-Type: application/json" --data @json/createPort.json http://localhost:9006/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports
