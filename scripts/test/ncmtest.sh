#! /bin/sh

# Expect to fail
echo "DELETE Existing node"
curl -X DELETE -H "Content-Type: application/json" http://localhost:9014/nodes/9192a4d4-ffff-4ece-b3f0-8d36e3d85002
echo "GET from empty cache"
curl -X GET -H "Content-Type: application/json" http://localhost:9014/nodes/9192a4d4-ffff-4ece-b3f0-8d36e3d85002
echo "POST, new node"
curl -X POST -H "Content-Type: application/json" --data @../json/createNode.json http://localhost:9014/nodes
echo "GET it back"
curl -X GET -H "Content-Type: application/json" http://localhost:9014/nodes/9192a4d4-ffff-4ece-b3f0-8d36e3d85002
echo "DELETE again node"
curl -X DELETE -H "Content-Type: application/json" http://localhost:9014/nodes/9192a4d4-ffff-4ece-b3f0-8d36e3d85002

# Expect to fail
echo "GET All from empty cache"
curl -X GET -H "Content-Type: application/json" http://localhost:9014/nodes/9192a4d4-ffff-4ece-b3f0-8d36e3d85002
echo "POST, again"
curl -X POST -H "Content-Type: application/json" --data @../json/createNode.json http://localhost:9014/nodes
echo "GET it back"
curl -X GET -H "Content-Type: application/json" http://localhost:9014/nodes/9192a4d4-ffff-4ece-b3f0-8d36e3d85002
echo "Update it"
curl -X PUT -H "Content-Type: application/json" --data @../json/updateNode.json http://localhost:9014/nodes
echo "host_dvr_mac should not be null"
curl -X GET -H "Content-Type: application/json" http://localhost:9014/nodes/9192a4d4-ffff-4ece-b3f0-8d36e3d85002
