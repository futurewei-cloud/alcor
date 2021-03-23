#!/usr/bin/python3
import requests
import time
import json
from helper_functions import *

node_id     = ["1112a4d4-ffff-4ece-b3f0-8d36e3d85001", "1112a4d4-ffff-4ece-b3f0-8d36e3d85002",]
subnet_id   = "8182a4d4-ffff-4ece-b3f0-8d36e3d88002"
network_id  = "9192a4d4-ffff-4ece-b3f0-8d36e3d88001"
project_id  = "3dda2801-d675-4688-a63f-dcda8d327f50"
tenant_id   = "3dda2801-d675-4688-a63f-dcda8d327f50"
security_group_id ="3dda2801-d675-4688-a63f-dcda8d111111"
device_id="8182a4d4-ffff-4ece-b3f0-8d36e3d00003" 
port_id = ["7122a4d4-fffm-5eee-b3f0-8d36e3d00106","7122a4d4-fffn-5eee-b3f0-8d36e3d00106"]
ip_addrs =["10.0.1.101","10.0.1.102"]
ip_mac_db = {}

def post_httprequest(url, data=""):
 try:
     headers = {
               'Content-Type': 'application/json',
               'Accept': '*/*',
              }
     response = requests.post(url, data = json.dumps(data), headers=headers)
     if(response.ok):
       print("POST Success", url)
       if 'ports' in url:
        valid_response = json.loads(response.text,object_pairs_hook=dict_clean)
        get_mac_for_ips(valid_response)
     else:
       response.raise_for_status()
 except requests.exceptions.HTTPError as err:
     print("POST Failed for {} with error".format(url, response.text))
     print(response.json)
     print("ERROR",err)
     raise SystemExit(err)

def get_mac_for_ips(valid_response):
  ports_info = valid_response["port"]
  #print(ports_info["mac_address"])
  #print(ports_info["fixed_ips"][0]["ip_address"])
  key = ports_info["fixed_ips"][0]["ip_address"]
  value =  ports_info["mac_address"]
  ip_mac_db[key] = value


def get_httprequest(url):
  try:
     response = requests.get(url)
     if(response.ok):
       print("GET Success", url)
       return response.text
     else:
       response.raise_for_status()
  except requests.HTTPError as exception:
  #except:requests.exceptions.HTTPError as e:
       print("GET failed for url", url)
       raise SystemExit(exception)


def create_default_segment_table(port):
  url ='http://localhost:{}/segments/createDefaultTable'.format(port)
  time.sleep(3)
  post_httprequest(url)


def create_node(ip_mac, port):
  url= 'http://localhost:{}/nodes'.format(port)
  data = {}
  for key, value in ip_mac.items():
     key_index = list(ip_mac).index(key) if key in ip_mac else None
     node_name = "node" + str(key_index)
     node_info = {"local_ip":str(key), "mac_address":str(value), "node_id":node_id[key_index], "node_name":node_name, "server_port":8080, "veth":"eth0"}
     data["host_info"] = node_info
     post_httprequest(url, data)


def create_vpc(port):
  network = {}
  url = 'http://localhost:{}/project/{}/vpcs'.format(port, project_id)
  network_info = {"admin_state_up":True, "revision_number":0, "cidr":"10.0.1.0/24", "default":True, "description":"vpc", "dns_domain":"domain", "id":network_id, "is_default":True, "mtu":1400, "name":"sample_vpc", "port_security_enabled":True, "project_id":project_id}
  network["network"]=network_info
  post_httprequest(url, network)


def create_subnet(port):
  subnet = {}
  url = 'http://localhost:{}/project/{}/subnets'.format(port, project_id)
  subnet_info = { "cidr":"10.0.1.0/24", "id":subnet_id, "ip_version":4, "network_id":network_id, "name":"subnet1", "host_routes":[{"destination":"10.0.1.0/24","nexthop":"10.0.1.1"} ] }
  subnet["subnet"] = subnet_info 
  post_httprequest(url, subnet)

def create_security_groups(port):
  security_groups ={}
  url = 'http://localhost:{}/project/{}/security-groups'.format(port, project_id)
  security_group_info = {"create_at":"string","description":"string","id":security_group_id,"name":"sg1","project_id":project_id,"security_group_rules":[],"tenant_id":tenant_id,"update_at":"string"}
  security_groups["security_group"]=security_group_info
  post_httprequest(url,security_groups)


def get_subnets(port):
  url = 'http://localhost:{}/project/{}/subnets'.format(port, project_id)
  print(get_httprequest(url))


def get_nodes(port):
  url= 'http://localhost:{}/nodes'.format(port)
  print(get_httprequest(url))


def get_vpcs(port):
  url= 'http://localhost:{}/project/{}/vpcs'.format(port,project_id)
  print(get_httprequest(url))

def create_ports(port,ip_addrs,port_id):
  url= 'http://localhost:{}/project/{}/ports'.format(port,project_id)
  for index in range(len(ip_addrs)):
   ports ={}
   port_info= {"admin_state_up":True,"allowed_address_pairs":[{"ip_address":"11.11.11.11","mac_address":"00-AA-BB-15-EB-3F"}],"binding:host_id":"node1","binding:vif_details":{},"create_at":"string","description": "string","device_id":device_id,"device_owner": "compute:nova","dns_assignment": {},"dns_domain": "string","dns_name": "string","extra_dhcp_opts": [{"ip_version": "string","opt_name":"string","opt_value": "string"}],"fast_path": True,"fixed_ips":[{"ip_address": ip_addrs[index],"subnet_id":subnet_id}],"id": port_id[index],"mac_learning_enabled": True,"name": "port{}.format(index)","network_id": network_id,"network_ns": "string","port_security_enabled": True,"project_id": project_id,"qos_network_policy_id": "string","qos_policy_id": "string","revision_number": 0,"security_groups": [security_group_id],"tags": ["string"],"tenant_id":tenant_id,"update_at": "string","uplink_status_propagation": True,"veth_name":"string"}
   ports["port"]= port_info
   post_httprequest(url,ports) #print(ports)

def get_mac_from_db():
   print("\n\n\n>>>>>>>")
   print("ip_mac stroed in ignite db",ip_mac_db)
   return ip_mac_db



def create_test_setup(ip_mac, ser_port):
  print("IN create test setup", ip_mac,ser_port["vpm"])
  create_default_segment_table(ser_port["vpm"])
  create_vpc(ser_port["vpm"])
  #get_vpcs(ser_port["vpm"])

  create_node(ip_mac, ser_port["nm"])
#  get_nodes(ser_port["nm"])

  create_subnet(ser_port["snm"])
 # get_subnets(ser_port["snm"])

  create_security_groups(ser_port["sgm"])
  create_ports(ser_port["pm"],ip_addrs,port_id)
  ip_mac_db = get_mac_from_db()
  return ip_mac_db
