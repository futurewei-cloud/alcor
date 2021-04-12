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
