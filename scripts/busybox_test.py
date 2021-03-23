#!/usr/bin/python3
import subprocess
import os
from helper_functions import *
from test_api_calls import *
import time


ALCOR_SCRIPTS = os.path.dirname(os.path.abspath(__file__))
os.chdir("..")
ALCOR_ROOT = os.getcwd()
ALCOR_SERVICES = ALCOR_ROOT +"/services"
services_list = ['ignite', 'vpm', 'snm', 'rm', 'pim' ,'mm' ,'pm' ,'nm', 'sgm', 'ag', 'dpm', 'eim', 'qm', 'nam','gm']
ser_port={}
ip_mac={}

def build_containers():
  container_list =[]
  print("building container images")
  mvn_build = "mvn -Dmaven.test.skip=true -DskipTests clean package install"
  docker_build_ignite = "docker build -t ignite-11 -f" + ALCOR_ROOT + "/lib/ignite.Dockerfile " +  ALCOR_ROOT + "/lib"
  # 9001 vpm
  docker_build_vpm ="docker build -t vpm " + ALCOR_SERVICES + "/vpc_manager/"
  #9002 snm
  docker_build_snm = "docker build -t snm " + ALCOR_SERVICES + "/subnet_manager/"
  #9003 rm
  docker_build_rm = "docker build -t rm " + ALCOR_SERVICES + "/route_manager/"
  # 9004 pim
  docker_build_pim = "docker build -t pim " + ALCOR_SERVICES + "/private_ip_manager/"
  #9005 mm
  docker_build_mm ="docker build -t mm " +  ALCOR_SERVICES + "/mac_manager/"
  #9006 pm
  docker_build_pm = "docker build -t pm " + ALCOR_SERVICES + "/port_manager/"
  #9007 nm
  docker_build_nm = "docker build -t nm " + ALCOR_SERVICES + "/node_manager/"
  #9008 sgm
  docker_build_sgm = "docker build -t sgm " + ALCOR_SERVICES + "/security_group_manager/"
  #9009 ag
  docker_build_ag = "docker build -t ag " + ALCOR_SERVICES + "/api_gateway/"
  #9010 dpm
  docker_build_dpm = "docker build -t dpm " + ALCOR_SERVICES + "/data_plane_manager/"
  #9011 eip
  docker_build_eip = "docker build -t eip " + ALCOR_SERVICES + "/elastic_ip_manager/"
  #9012 qm
  docker_build_qm = "docker build -t qm " + ALCOR_SERVICES + "/quota_manager/"
  #9013 nam
  docker_build_nam = "docker build -t nam " + ALCOR_SERVICES + "/network_acl_manager/"
  #9014 nam
  docker_build_ncm = "docker build -t ncm " + ALCOR_SERVICES + "/network_config_manager/"
  #9015 gm
  docker_build_gm = "docker build -t gm " + ALCOR_SERVICES + "/gateway_manager/"
#  execute_checkcommand(mvn_build)
  container_list.append(mvn_build)
  container_list.append(docker_build_ignite)
  container_list.append(docker_build_vpm)
  container_list.append(docker_build_snm)
  container_list.append(docker_build_rm)
  container_list.append(docker_build_pim)
  container_list.append(docker_build_mm)
  container_list.append(docker_build_pm)
  container_list.append(docker_build_qm)
  container_list.append(docker_build_ag)
  container_list.append(docker_build_dpm)
  container_list.append(docker_build_eip)
  container_list.append(docker_build_nam)
  container_list.append(docker_build_gm)
  if(execute_commands("Build ",container_list) == True):
   print("All Alcor container images built sucessfully")
  
def start_containers():
  start_containers= []
  start_ignite ="docker run --name=ignite -p 10800:10800 -p 10801:10801 -p 47100:47100 -p 47500:47500 -v /tmp:/tmp -tid ignite-11 sh"
  start_vpm = "docker run --net=host --name vpm -p 9001:9001 -v /tmp:/tmp -itd vpm"
  start_snm = "docker run --net=host --name snm -p 9002:9002 -v /tmp:/tmp -itd snm"
  start_rm = "docker run --net=host --name rm  -p 9003:9003 -v /tmp:/tmp -itd rm"
  start_pim="docker run --net=host --name pim -p 9004:9004 -v /tmp:/tmp -itd pim"
  start_mm= "docker run --net=host --name mm  -p 9005:9005 -v /tmp:/tmp -itd mm"
  start_pm="docker run --net=host --name pm  -p 9006:9006 -v /tmp:/tmp -itd pm"
  start_nm="docker run --net=host --name nm  -p 9007:9007 -v /tmp:/tmp -itd nm"
  start_sgm="docker run --net=host --name sgm -p 9008:9008 -v /tmp:/tmp -itd sgm"
  start_ag ="docker run --net=host --name ag  -p 9009:9009 -v /tmp:/tmp -itd ag"
  start_dpm="docker run --net=host --name dpm -p 9010:9010 -v /tmp:/tmp -itd dpm"
  start_eim="docker run --net=host --name eim -p 9011:9011 -v /tmp:/tmp -itd eim"
  start_qm ="docker run --net=host --name qm  -p 9012:9012 -v /tmp:/tmp -itd qm"
  start_nam ="docker run --net=host --name nam -p 9013:9013 -v /tmp:/tmp -itd nam"
  start_gm ="docker run --net=host --name gm  -p 9015:9015 -v /tmp:/tmp -itd gm"
  start_containers.append(start_ignite)
  start_containers.append(start_vpm)
  start_containers.append(start_snm)
  start_containers.append(start_rm)
  start_containers.append(start_pim)
  start_containers.append(start_sgm)
  start_containers.append(start_mm)
  start_containers.append(start_pm)
  start_containers.append(start_nm)
  start_containers.append(start_ag)
  start_containers.append(start_dpm)
  start_containers.append(start_qm)
  start_containers.append(start_eim)
  start_containers.append(start_nam)
  #start_containers.append(start_ncm)
  start_containers.append(start_gm)
  if(execute_commands("Start ",start_containers) == True):
   print("All Alcor services started successfully")


def stop_containers(name = ""):
  command = "docker container stop "
  if (name == ""):
    print("Stopping all containers")
    if(run_command_forall(command,services_list) == True):
     print("All Alcor services stopped Successfully")
  else:
    print("Stopping container",name)
    command = "docker container stop "+ name

def remove_containers(name =""):
  command = "docker container rm "
  if (name == ""):
    print("Removing all containers")
    if(run_command_forall(command,services_list) == True):
      print("All Alcor containers removed successfully")
  else:
    print("Removing container",name)
    command = "docker container rm "+ name

def check_alcoragents_running():
  alcor_file_name = ALCOR_SCRIPTS +'/AlcorAgents.txt'
  no_aca_running = True
  fp = open(alcor_file_name,'r')
  ip_addrs = fp.readlines()
  for ip_addr in ip_addrs:
    ip_addr = ip_addr.strip()
    if not "ip-address" in ip_addr:
       if(check_process_running(ip_addr,"AlcorControlAgent") == True):
         print("AlcorControlAgent is running on {}".format(ip_addr))
         mac_addr = getmac_from_aca(ip_addr)
         print("Mac_addr",mac_addr,"for host",ip_addr)
         ip_mac[ip_addr] = mac_addr
         no_aca_running = False
       else:
         print("AlcorControlAgent is not running on {}".format(ip_addr))
  if(no_aca_running == True):
   return  False
  else:
   return True


 #This function maps services to port numbers in a dictionary,which is later used in http get/post requests
def map_services_port_numbers():
  ser_port["vpm"] = 9001
  ser_port["snm"] = 9002
  ser_port["rm"] =9003
  ser_port["pim"] =9004
  ser_port["mm"]= 9005
  ser_port["pm"] =9006
  ser_port["nm"] = 9007
  ser_port["sgm"] = 9008
  ser_port["ag"] = 9009
  ser_port["dpm"] =9010
  ser_port["eip"] =9011
  ser_port["qm"] =9012
  ser_port["nam"] = 9013
  ser_port["gm"] = 9015


def main():

 map_services_port_numbers()
 stop_containers()
 remove_containers()
 start_containers()

 if(check_alcoragents_running()== False):
    print("No running instance of alcor agent found in AlcorAgents.txt file busy box test can not be performed")
    sys.exit(0)
 print("Wait for 5 seconds until all services are started....")
 time.sleep(20)

 ip_mac_db = create_test_setup(ip_mac,ser_port)
 print("In Main ", ip_mac_db)

'''build_containers()
  print(" IN main")
  stop_containers()
  remove_containers()
  start_containers()
  #print(ALCOR_ROOT)'''

if __name__ == "__main__":
    main()

