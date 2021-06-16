#!/usr/bin/python3

# MIT License
# Copyright(c) 2020 Futurewei Cloud
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files(the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import requests
import time
import json
from prepare_payload import *

def create_default_segment_table(port):
  print("Create default segment table")
  url ='http://localhost:{}/segments/createDefaultTable'.format(port)
  time.sleep(3)
  post_httprequest(url)
  print("SUCCESS: Created default segment table\n")


def create_node(port, ip_mac):
  print("Creating nodes")
  url= 'http://localhost:{}/nodes'.format(port)
  data = {}
  node_info = read_config_file_section("node_info")
  node_dict = node_info['node_info']
  nodeinfo  = json.loads(node_dict)
  node_name = nodeinfo['node_name']
  node_id   = nodeinfo['node_id']

  for key, value in ip_mac.items():
    key_index = list(ip_mac).index(key) if key in ip_mac else None
    node_info = {"local_ip":str(key), "mac_address":str(value), "node_id":node_id[key_index], "node_name":node_name[key_index], "server_port":nodeinfo['server_port'], "veth":nodeinfo['veth']}
    data["host_info"] = node_info
    post_httprequest(url, data)
  print("SUCCESS: Created nodes\n")


def create_router_interface(port):
  print("Creating router interface")
  router={}
  url = 'http://localhost:{}/project/{}/routers'.format(port, get_projectid())
  router_info = read_config_file_section("router_info")
  router_dict = router_info['router_info']
  routerinfo = json.loads(router_dict)
  route_info = {"admin_state_up": True,"availability_zone_hints": ["string"], "availability_zones": ["string"],"conntrack_helpers": ["string"],"description": "string","distributed": True,"external_gateway_info": {"enable_snat": True,"external_fixed_ips": [ ],"network_id": routerinfo['network_id']},"flavor_id": "string","gateway_ports": [ ], "ha": True,"id":routerinfo['id'] ,"name": routerinfo['name'],"owner": routerinfo['owner'], "project_id":routerinfo['project_id'],"revision_number": 0,"routetable": {},"service_type_id": "string","status": "BUILD","tags": ["string"],"tenant_id": routerinfo['tenant_id']}
  router['router'] = route_info
  post_httprequest(url, router)
  print("SUCCESS: Created router interface\n")
  return routerinfo['id']

# Second parameter is to indicate if the call is made for base test case or any other test case.
def create_vpc(port, change={}):
  print("Creating VPC")
  network = {}
  url = 'http://localhost:{}/project/{}/vpcs'.format(port, get_projectid())
  network_info = read_config_file_section("vpc_info")
  network_dict = network_info['vpc_info']
  networkinfo = json.loads(network_dict)
  if('change' in change):
     networkinfo[change['change']] = change[change['change']]
  network_info = {"admin_state_up":True, "revision_number":0, "cidr":networkinfo['cidr'], "default":True, "description":"vpc", "dns_domain":"domain", "id":networkinfo['id'], "is_default":True, "mtu":1400, "name":"sample_vpc", "port_security_enabled":True, "project_id":networkinfo['project_id']}
  print(network_info)
  network["network"] = network_info
  post_httprequest(url, network)
  print("SUCCESS: Created VPC\n")


def create_subnet(port):
  print("Creating Subnet")
  subnet = {}
  url = 'http://localhost:{}/project/{}/subnets'.format(port, get_projectid())
  subnet_info = read_config_file_section("subnet_info")
  subnetinfo = json.loads(subnet_info['subnet_info'])
  subnet["subnet"] = subnetinfo
  print("Posting subnet", subnet)
  post_httprequest(url, subnet)
  print("SUCCESS: Creating Subnet\n")


def create_security_group(port):
  print("Creating security group")
  security_groups = {}
  url = 'http://localhost:{}/project/{}/security-groups'.format(port, get_projectid())
  sg_info = read_config_file_section("security_groups")
  sginfo = json.loads(sg_info['security_group_info'])
  security_groups["security_group"]=sginfo
  post_httprequest(url,security_groups)
  print("SUCCESS: Created security group\n")


def get_subnets(port):
  url = 'http://localhost:{}/project/{}/subnets'.format(port, get_projectid())
  print(get_httprequest(url))


def get_nodes(port):
  url= 'http://localhost:{}/nodes'.format(port)
  print(get_httprequest(url))


def get_vpcs(port):
  url= 'http://localhost:{}/project/{}/vpcs'.format(port,get_projectid())
  print(get_httprequest(url))


def get_ports(port):
  url= 'http://localhost:{}/project/{}/ports'.format(port,get_projectid())
  print(get_httprequest(url))


def create_ports(port):
  print("Creating Goal State Ports")
  url= 'http://localhost:{}/project/{}/ports'.format(port, get_projectid())
  port_info = read_config_file_section("port_info")
  port_dict = port_info['port_info']
  port_dict = json.loads(port_dict, strict=False)
  port_name = port_dict['name']
  port_id   = port_dict['id']
  ip_addrs  = port_dict['fixed_ips']
  node_name = port_dict['binding:host_id']
  device_id = port_dict['device_id']

  for index in range(len(ip_addrs)):
    ports = {}
    port_info = {"admin_state_up":True,"allowed_address_pairs":[{"ip_address":"11.11.11.1","mac_address":"00-AA-BB-15-EB-3F"}],"binding:host_id":node_name[index],"binding:vif_details":{},"create_at":"string","description":"string","device_id":device_id[index],"device_owner":"compute:nova","dns_assignment":{},"dns_domain":"string","dns_name":"string","extra_dhcp_opts":[{"ip_version":"string","opt_name":"string","opt_value":"string"}],"fast_path": True,"fixed_ips":[{"ip_address":ip_addrs[index],"subnet_id":port_dict['subnet_id']}],"id":port_id[index],"mac_learning_enabled":True,"name":port_name[index],"network_id":port_dict['network_id'],"network_ns": "string","port_security_enabled":True,"project_id":port_dict['project_id'],"qos_network_policy_id":"string","qos_policy_id":"string","revision_number":0,"security_groups":[port_dict['security_groups']],"tags":["string"],"tenant_id":port_dict['tenant_id'],"update_at":"string","uplink_status_propagation":True,"veth_name":"string"}
    ports["port"] = port_info
    post_httprequest(url, ports)

  print("SUCCESS: Created Goal State Ports\n")


def create_test_setup(ip_mac, config_file_object):
  print("Calling Alcor APIs to generate Goal States")
  services = dict(config_file_object.items("services"))
  service_port_map = get_service_port_map(services)

  create_default_segment_table(service_port_map["sgs"])

  #network_info = dict(config_file_object.items("vpc_info"))
  create_vpc(service_port_map["vpm"])
  #get_vpcs(service_port_map["vpm"])

  #node_info = dict(config_file_object.items("node_info"))
  create_node(service_port_map["nm"], ip_mac)
  #get_nodes(service_port_map["nm"])

  #subnet_info = dict(config_file_object.items("subnet_info"))
  create_subnet(service_port_map["snm"])
  #get_subnets(service_port_map["snm"])

  #sg_info = dict(config_file_object.items("security_groups"))
  create_security_group(service_port_map["sgm"])

  #port_info = dict(config_file_object.items("port_info"))
  create_ports(service_port_map["pm"])
  #get_ports(service_port_map["pm"])

  ip_mac_db = get_mac_from_db()
  print("Goal State IP/MACs: ", ip_mac_db)
  return ip_mac_db
