#!/bin/bash

Host=$1
Port=$2

#Deploy a second ort
curl -H "Accept: application/json" -H "Content-Type:application/json" \
	-X POST "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports" \
	--data \
	'{"port":
{"project_id": "3dda2801-d675-4688-a63f-dcda8d327f50",
	"id": "33333333-7f83-45fa-a4d4-1b31e7539933",
	"name": "test_cni_port33",
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
	"binding:host_id": "ephost_2",
	"binding:profile": null,
	"binding:vnic_type": null,
	"network_ns": "/var/run/netns/test_netw_ns3",
	"dnsName": null,
	"dnsAssignment": null,
	"fast_path": true }}' | json_pp

curl -X GET "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports/33333333-7f83-45fa-a4d4-1b31e7539933" | json_pp

if $3 ; then
	echo "Clean up....."
        curl -X DELETE "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/ports/f37810eb-7f83-45fa-a4d4-1b31e75399df" | json_pp
	curl -X DELETE "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs/9192a4d4-ffff-4ece-b3f0-8d36e3d88038/subnets/a87e0f87-a2d9-44ef-9194-9a62f178594e" | json_pp
	curl -X DELETE "http://$Host:$Port/project/3dda2801-d675-4688-a63f-dcda8d327f50/vpcs/9192a4d4-ffff-4ece-b3f0-8d36e3d88038" | json_pp
fi

