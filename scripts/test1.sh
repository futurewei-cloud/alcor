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


curl -X POST -H "Content-Type: application/json" --data @json/cvpc.json http://localhost:9009/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs
curl -X POST -H "Content-Type: application/json" --data @json/csn.json http://localhost:9009/project/3dda2801-d675-4688-a63f-dcda8d327f50/subnets
curl -X POST -H "Content-Type: application/json" --data @json/node.json http://localhost:9007/v4/nodes
curl -X POST -H "Content-Type: application/json" --data @json/sg.json http://localhost:9008/v4/3dda2801-d675-4688-a63f-dcda8d327f50/security-groups
curl -X POST -H "Content-Type: application/json" --data @json/cp1b.json http://localhost:9009/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports
