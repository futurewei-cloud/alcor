from create_test_setup import *

def create_security_groups(port):
     security_groups ={}
     url = 'http://localhost:{}/project/{}/security-groups'.format(port, get_projectid())
     sg_info = read_configfile_section("security_groups")
     sginfo = json.loads(sg_info['security_group_info'])
     '''security_groups["security_group"]=sginfo
     post_httprequest(url,security_groups)'''

     print(sg_info,sginfo)
     security_groups_info = read_configfile_section("test_case2")
     print(security_groups_info)
     id_list = json.loads(security_groups_info['security_group_ids']) 
     id_names = json.loads(security_groups_info['sg_names'])
     for name,id in zip(id_names,id_list):
        print(name,id)
        sginfo['id'] = id
        sginfo['name'] = name
        print("After changing ...sginfo",sginfo,type(sginfo))
        security_groups["security_group"]=sginfo
        print("\n\n\n")
        print("Posting ..",security_groups)
        print("url is ",url)
        post_httprequest(url,security_groups)
     return id_list


def attach_subnets_to_router(rm_port,snm_port,router_id):
   url = 'http://localhost:{}/project/{}/routers/{}/add_router_interface'.format(rm_port, get_projectid(),router_id)
   print("attaching subnets to test cases")
   subnet_info = read_configfile_section("test_case1")
   id_list = json.loads(subnet_info['subnet_ids']) 
   id_names = json.loads(subnet_info['subnet_names'])
   device_ids = json.loads(subnet_info['device_ids'])
   cidrs  = json.loads(subnet_info['cidrs'])
   for id in id_list:
      subnet_info ={"subnet_id":id}
      put_httprequest(url,subnet_info)
   subnet = {}
   url = 'http://localhost:{}/project/{}/subnets'.format(snm_port, get_projectid())
   subnet_info = read_configfile_section("subnet_info")
   subnetinfo = json.loads(subnet_info['subnet_info'])
   for cidr,id,name in zip(cidrs,id_list,id_names):
      subnetinfo['cidr'] = cidr
      subnetinfo['id'] = id
      subnetinfo['name'] = name
      subnet["subnet"] = subnetinfo
      post_httprequest(url, subnet)
   return id_list,device_ids


#test case 1 is with two nodes with different subnet ids in same same sg
def prepare_test_case1(ip_mac,ser_port):
   serv = read_configfile()
   create_default_segment_table(ser_port["vpm"])
   change = {'change':'cidr','cidr':"10.0.0.0/16"}
  
   create_vpc(ser_port["vpm"],change)
   get_vpcs(ser_port["vpm"])
   create_node(ip_mac, ser_port["nm"])
   router_id =create_router_interface(ser_port["rm"])
   id_list,device_ids  =attach_subnets_to_router(ser_port["rm"],ser_port["snm"],router_id)
   get_subnets(ser_port["snm"])
   change_ports = {"change":["subnet_id","device_id"],"subnet_id":id_list,"device_id":device_ids}
   create_security_group(ser_port["sgm"])
   create_ports_s(ser_port["pm"],change_ports)

   

# test case 2 is with two nodes in same subnet and different seurity groups
def prepare_test_case2(ip_mac,ser_port):
   serv = read_configfile()
   create_default_segment_table(ser_port["vpm"])
   create_vpc(ser_port["vpm"])
   create_node(ip_mac, ser_port["nm"])
   create_subnet(ser_port["snm"])
   id_list = create_security_groups(ser_port["sgm"])
   change_ports = {"change":"security_groups","security_groups":id_list}
   create_ports_s(ser_port["pm"],change_ports)

def prepare_test_case_1(ip_mac,ser_port):
    prepare_test_case1(ip_mac,ser_port)
    ip_mac_db = get_mac_from_db()
    print("after first test case  return",ip_mac_db)
    return ip_mac_db

def prepare_test_case_2(ip_mac,ser_port):
    print("RRR runing  second test case")
    prepare_test_case2(ip_mac,ser_port)
    ip_mac_db = get_mac_from_db()
    print("after second test case  return",ip_mac_db)
    return ip_mac_db

     
def create_ports_s(port,change_ports):
  url= 'http://localhost:{}/project/{}/ports'.format(port,get_projectid())
  port_info = read_configfile_section("port_info")
  port_dict = port_info['port_info']
  port_dict = json.loads(port_dict, strict=False)
  port_name = port_dict['name']
  port_id  = port_dict['id']
  ip_addrs  = port_dict['fixed_ips']
  node_name  = port_dict['binding:host_id']
  subnet_ids =[]
  security_groups = []
  changes = change_ports['change']
  device_ids =[]
  if 'subnet_id' in changes:
  #if change_ports['change'] == 'subnet_id':
    subnet_ids = change_ports['subnet_id']
  else:
    subnet_ids.append(port_dict['subnet_id'])
    subnet_ids.append(port_dict['subnet_id'])
  if 'device_id' in changes:
    device_ids = change_ports['device_id']
  else:
    device_ids.append(port_dict['device_id'])
    device_ids.append(port_dict['device_id'])
  if 'security_groups' in changes:
    security_groups = change_ports['security_groups']
  else:
    security_groups.append(port_dict['security_groups'])
    security_groups.append(port_dict['security_groups'])
  for index in range(len(ip_addrs)):
    ports = {}
    port_info = {"admin_state_up":True,"allowed_address_pairs":[{"ip_address":"11.11.11.11","mac_address":"00-AA-BB-15-EB-3F"}],"binding:host_id":node_name[index],"binding:vif_details":{},"create_at":"string","description": "string","device_id":device_ids[index],"device_owner": "compute:nova","dns_assignment": {},"dns_domain": "string","dns_name": "string","extra_dhcp_opts": [{"ip_version": "string","opt_name":"string","opt_value": "string"}],"fast_path": True,"fixed_ips":[{"ip_address": ip_addrs[index],"subnet_id":subnet_ids[index]}],"id": port_id[index],"mac_learning_enabled": True,"name": port_name[index],"network_id": port_dict['network_id'],"network_ns": "string","port_security_enabled": True,"project_id":port_dict['project_id'],"qos_network_policy_id": "string","qos_policy_id": "string","revision_number": 0,"security_groups": [security_groups[index]],"tags": ["string"],"tenant_id":port_dict['tenant_id'],"update_at": "string","uplink_status_propagation": True,"veth_name":"string"}
    ports["port"] = port_info
    print(ports,url)
    post_httprequest(url, ports)

