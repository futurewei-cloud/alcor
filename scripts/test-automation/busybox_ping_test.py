#!/usr/bin/python3
import subprocess
import os
from busybox_helper_functions import *
from busybox_alcor_api_calls import *
from busybox_container_ops import *
import time
import configparser

ALCOR_ROOT = os.path.abspath(os.path.join(__file__ , "../../../"))
ALCOR_SERVICES = ALCOR_ROOT + "/services/"
ALCOR_TEST_DIR = os.path.dirname(os.path.abspath(__file__))
os.chdir("../../")


def read_configfile():
    config = configparser.ConfigParser()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    serv = dict(config.items('services'))
    return serv


def read_aca_ips():
    config = configparser.ConfigParser()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    aca = dict(config.items('AlcorControlAgents'))
    return aca


def build_containers(serv):
    container_list =[]
    mvn_build = "mvn -Dmaven.test.skip=true -DskipTests clean package install"
    container_list.append(mvn_build)

    print("building container images")
    services_list = get_filelist(ALCOR_SERVICES)
    for service_name in serv.keys():
      service_path = ALCOR_SERVICES + service_name
      service_info = json.loads(serv[service_name])
      if service_name == "ignite":
         build_cmd = make_docker_command("build ", " -t {}".format(service_info["name"]), " -f {} ".format(ALCOR_ROOT + service_info["path"]), ALCOR_ROOT + "/lib")
      else:
         build_cmd = make_docker_command("build ", "-t {} {}".format(service_info["name"], ALCOR_SERVICES + service_name))
      container_list.append(build_cmd)

    if(execute_commands("Build ", container_list) == True):
       print("All Alcor services built successfully")


def start_containers(serv):
    start_containers= []
    mount_option = "/tmp:/tmp"
    for service_name in serv.keys():
      service_info = json.loads(serv[service_name])
      if service_name == "ignite":
        start_cmd = make_docker_command("run ", "--name={} -p 10800:10800 -p 10801:10801 -p 47100:47100 -p 47500:47500 -v {} -tid {} sh".format(service_info["img_name"], mount_option, service_info["name"]))
      else:
        start_cmd = make_docker_command("run ", "--net=host ", "--name {} -p {}:{} -v {} -itd {}".format(service_info["name"], service_info["port"], service_info["port"],mount_option, service_info["name"]))
      start_containers.append(start_cmd)

    if(execute_commands("Start ", start_containers) == True):
      print("All Alcor services started successfully")


def get_services_from_conf_file():
    serv = read_configfile()
    service_list = {}
    for service_name in serv.keys():
       service_info = json.loads(serv[service_name])
       if "ignite-11" in service_info["name"]:
         service_list["ignite"] = 10800
       else:
         service_list[service_info["name"]] = service_info["port"]
    return service_list


def stop_containers(name = ""):
    services_list = get_services_from_conf_file()
    command = "docker container stop "
    if (name == ""):
      print("Stopping all containers")
      if(run_command_forall(command, services_list.keys()) == True):
       print("All Alcor services stopped Successfully")
    else:
      print("Stopping container", name)
      command = "docker container stop " + name


def remove_containers(name = ""):
    services_list = get_services_from_conf_file()
    command = "docker container rm "
    if (name == ""):
      print("Removing all containers")
      if(run_command_forall(command, services_list.keys()) == True):
        print("All Alcor containers removed successfully")
    else:
      print("Removing container", name)
      command = "docker container rm " + name


def check_alcoragents_running(aca):
    ip_mac = {}
    for ip_addr in aca.values():
      if(check_process_running(ip_addr.strip(), "AlcorControlAgent") == True):
         print("AlcorControlAgent is running on {}".format(ip_addr))
         mac_addr = getmac_from_aca(ip_addr)
         print("Mac_addr", mac_addr, "for host", ip_addr, "\n")
         ip_mac[ip_addr] = mac_addr
      else:
         print("AlcorControlAgent is not running on {}".format(ip_addr))
    return ip_mac


def main(args = None):
    """services = read_configfile()
    if args is None:
      args = sys.argv[1:]
      if("-b" in args):
        build_containers(services)

    stop_containers()
    remove_containers()
    start_containers(services)"""

    aca = read_aca_ips()
    ip_mac = check_alcoragents_running(aca)
    if(len(ip_mac) != len(aca)):
       print("\nERROR: Alcor Control Agent not running on some of the nodes")
       print("ERROR: Test exits")
       sys.exit(1)
    print("Wait for 5 seconds until all services are started....")
    #time.sleep(5)
    services_list = get_services_from_conf_file()
    ip_mac_db = create_test_setup(ip_mac, services_list)

    #ip_mac={"10.213.43.161":"aa:bb:cc:3b:4d:02", "10.213.43.163":"aa:bb:cc:3d:b2:58"}
    #ip_mac_db={"10.0.1.101":"aa:bb:cc:3b:4d:02", "10.0.1.102":"aa:bb:cc:3d:b2:58"}
    #busybox_container_deploy(ip_mac, ip_mac_db)
    #run_ping_test(ip_mac)"""

if __name__ == "__main__":
     main()

