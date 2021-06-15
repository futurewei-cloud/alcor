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

Host=$1
Port=$2

#Install prerequisites
sudo apt-get install libjsoncpp-dev

#Deploy a VPC
curl -H "Accept: application/json" -H "Content-Type:application/json" \
-X POST "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs" \
--data \
'{"vpc":
{"project_id": "3dda2801-d675-4688-a63f-dcda8d327f50",
	"id": "9192a4d4-ffff-4ece-b3f0-8d36e3d88038",
	"name": "test_vpc",
	"description": "",
	"cidr": "10.0.0.0/16" }}' | json_pp

curl -X GET "$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs/9192a4d4-ffff-4ece-b3f0-8d36e3d88038" | json_pp

#Deploy a subnet
curl -H "Accept: application/json" -H "Content-Type:application/json" \
-X POST "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/subnets" \
--data \
'{"subnet":
{"project_id": "3dda2801-d675-4688-a63f-dcda8d327f50",
	"vpc_id": "9192a4d4-ffff-4ece-b3f0-8d36e3d88038",
	"id": "a87e0f87-a2d9-44ef-9194-9a62f178594e",
	"name": "test_subnet",
	"description": "",
	"cidr": "10.0.0.0/20",
	"gateway_ip": "10.0.0.5",
	"availability_zone": "uswest-1",
	"dhcp_enable": false,
	"primary_dns": null,
	"secondary_dns": null,
	"dns_list": null }}' | json_pp

curl -X GET "$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/subnets/a87e0f87-a2d9-44ef-9194-9a62f178594e" | json_pp

#Deploy a port
curl -H "Accept: application/json" -H "Content-Type:application/json" \
-X POST "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports" \
--data \
'{"port":
{"project_id": "3dda2801-d675-4688-a63f-dcda8d327f50",
	"id": "f37810eb-7f83-45fa-a4d4-1b31e75399df",
	"name": "test_cni_port2",
	"description": "",
	"network_id": "a87e0f87-a2d9-44ef-9194-9a62f178594e",
	"tenant_id": null,
	"admin_state_up": true,
	"mac_address": null,
	"veth_name": "veth0",
	"device_id": null,
	"device_owner": null,
	"status": null,
	"fixed_ips": [],
	"allowed_address_pairs": null,
	"extra_dhcp_opts": null,
	"security_groups": null,
	"binding:host_id": "ephost_0",
	"binding:profile": null,
	"binding:vnic_type": null,
	"network_ns": "/var/run/netns/test_netw_ns",
	"dnsName": null,
	"dnsAssignment": null,
	"fast_path": true }}' | json_pp

curl -X GET "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports/f37810eb-7f83-45fa-a4d4-1b31e75399df" | json_pp

if $3; then
  echo "Clean up....."
  curl -X DELETE "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports/f37810eb-7f83-45fa-a4d4-1b31e75399df" | json_pp
  curl -X DELETE "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs/9192a4d4-ffff-4ece-b3f0-8d36e3d88038/subnets/a87e0f87-a2d9-44ef-9194-9a62f178594e" | json_pp
  curl -X DELETE "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs/9192a4d4-ffff-4ece-b3f0-8d36e3d88038" | json_pp
fi
