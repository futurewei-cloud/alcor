# MIT License
# Copyright(c) 2020 Futurewei Cloud
#     Permission is hereby granted,
#     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#     The above copyright notice and this permission notice shall be included in all copies
#     or
#     substantial portions of the Software.
#     THE SOFTWARE IS PROVIDED "AS IS",
#     WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
#     DAMAGES OR OTHER
#     LIABILITY,
#     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#     SOFTWARE.

#!/usr/bin/python3
import requests
import time
import json
from busybox_helper_functions import *

ip_mac_db = {}

def post_httprequest(url, data=""):
  try:
     headers = {
               'Content-Type': 'application/json',
               'Accept': '*/*',
              }
     #print(url,data)
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
  node_info = read_configfile_section("node_info")
  node_dict = node_info['node_info']
  nodeinfo = json.loads(node_dict)
  node_name = nodeinfo['node_name']
  node_id  = nodeinfo['node_id']

  for key, value in ip_mac.items():
    key_index = list(ip_mac).index(key) if key in ip_mac else None
    node_info = {"local_ip":str(key), "mac_address":str(value), "node_id":node_id[key_index], "node_name":node_name[key_index], "server_port":nodeinfo['server_port'], "veth":nodeinfo['veth']}
    data["host_info"] = node_info
    post_httprequest(url, data)


def create_vpc(port):
  network = {}
  url = 'http://localhost:{}/project/{}/vpcs'.format(port, get_projectid())
  network_info = read_configfile_section("vpc_info")
  network_dict = network_info['vpc_info']
  networkinfo = json.loads(network_dict)
  network_info = {"admin_state_up":True, "revision_number":0, "cidr":networkinfo['cidr'], "default":True, "description":"vpc", "dns_domain":"domain", "id":networkinfo['id'], "is_default":True, "mtu":1400, "name":"sample_vpc", "port_security_enabled":True, "project_id":networkinfo['project_id']}
  network["network"] = network_info
  post_httprequest(url, network)


def create_subnet(port):
  subnet = {}
  url = 'http://localhost:{}/project/{}/subnets'.format(port, get_projectid())
  subnet_info = read_configfile_section("subnet_info")
  subnetinfo = json.loads(subnet_info['subnet_info'])
  subnet["subnet"] = subnetinfo
  post_httprequest(url, subnet)


def create_security_groups(port):
  security_groups ={}
  url = 'http://localhost:{}/project/{}/security-groups'.format(port, get_projectid())
  sg_info = read_configfile_section("security_groups")
  sginfo = json.loads(sg_info['security_group_info'])
  security_groups["security_group"]=sginfo
  post_httprequest(url,security_groups)


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
  url= 'http://localhost:{}/project/{}/ports'.format(port,get_projectid())
  port_info = read_configfile_section("port_info")
  port_dict = port_info['port_info']
  port_dict = json.loads(port_dict, strict=False)
  port_name = port_dict['name']
  port_id  = port_dict['id']
  ip_addrs  = port_dict['fixed_ips']
  node_name  = port_dict['binding:host_id']

  for index in range(len(ip_addrs)):
    ports = {}
    port_info = {"admin_state_up":True,"allowed_address_pairs":[{"ip_address":"11.11.11.11","mac_address":"00-AA-BB-15-EB-3F"}],"binding:host_id":node_name[index],"binding:vif_details":{},"create_at":"string","description": "string","device_id":port_dict['device_id'],"device_owner": "compute:nova","dns_assignment": {},"dns_domain": "string","dns_name": "string","extra_dhcp_opts": [{"ip_version": "string","opt_name":"string","opt_value": "string"}],"fast_path": True,"fixed_ips":[{"ip_address": ip_addrs[index],"subnet_id":port_dict['subnet_id']}],"id": port_id[index],"mac_learning_enabled": True,"name": port_name[index],"network_id": port_dict['network_id'],"network_ns": "string","port_security_enabled": True,"project_id":port_dict['project_id'],"qos_network_policy_id": "string","qos_policy_id": "string","revision_number": 0,"security_groups": [port_dict['security_groups']],"tags": ["string"],"tenant_id":port_dict['tenant_id'],"update_at": "string","uplink_status_propagation": True,"veth_name":"string"}
    ports["port"] = port_info
    post_httprequest(url, ports) #print(ports)

def get_mac_from_db():
  print("\n\n\n>>>>>>>")
  print("IP & MAC stored in ignite db", ip_mac_db)
  return ip_mac_db


def create_test_setup(ip_mac, ser_port):
  print("In create test setup")
  create_default_segment_table(ser_port["vpm"])
  create_vpc(ser_port["vpm"])
  get_vpcs(ser_port["vpm"])

  create_node(ip_mac, ser_port["nm"])
  get_nodes(ser_port["nm"])

  create_subnet(ser_port["snm"])
  get_subnets(ser_port["snm"])

  create_security_groups(ser_port["sgm"])
  create_ports(ser_port["pm"])
  get_ports(ser_port["pm"])

  ip_mac_db = get_mac_from_db()
  return ip_mac_db

