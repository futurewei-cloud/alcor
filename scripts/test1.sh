curl -X POST -H "Content-Type: application/json" --data @json/cvpc.json http://localhost:9009/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs
curl -X POST -H "Content-Type: application/json" --data @json/csn.json http://localhost:9009/project/3dda2801-d675-4688-a63f-dcda8d327f50/subnets
curl -X POST -H "Content-Type: application/json" --data @json/node.json http://localhost:9007/v4/nodes
curl -X POST -H "Content-Type: application/json" --data @json/sg.json http://localhost:9008/v4/3dda2801-d675-4688-a63f-dcda8d327f50/security-groups
curl -X POST -H "Content-Type: application/json" --data @json/cp1b.json http://localhost:9009/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports
