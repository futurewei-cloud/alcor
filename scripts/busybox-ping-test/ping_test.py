#!/usr/bin/python3
import time, os
import argparse
import json
from helper_functions import *
from create_test_setup import *
from container_ops import *
from create_test_cases import *

ALCOR_ROOT = os.path.abspath(os.path.join(__file__ , "../../../"))
ALCOR_SERVICES = ALCOR_ROOT + "/services/"
ALCOR_TEST_DIR = os.path.dirname(os.path.abspath(__file__))
os.chdir("../../")

# Builds the containers as configured in alcor_services.ini file
def build_containers(serv):
    container_list =[]
    mvn_build = "mvn -Dmaven.test.skip=true -DskipTests clean package install"
    container_list.append(mvn_build)

    print("building container images")
    services_list = get_file_list(ALCOR_SERVICES)
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
        start_cmd = make_docker_command("run ", "--name={} -p 10800:10800 -p 10801:10801 -p 47100:47100 -p 47500:47500 -v {} -tid {} sh".format(service_info["name"], mount_option, service_info["name"]))
      else:
        start_cmd = make_docker_command("run ", "--net=host ", "--name {} -p {}:{} -v {} -itd {}".format(service_info["name"], service_info["port"], service_info["port"],mount_option, service_info["name"]))
      start_containers.append(start_cmd)

    if(execute_commands("Start ", start_containers) == True):
      print("All Alcor services started successfully")
      return True
    else:
      print("Couldn not start all alcor services.")
      print("Error! Test Exits")
      sys.exit(1)


def stop_containers(service_list):
    command = "docker container stop "
    for service in service_list:
      execute_command(command + service)


def remove_containers(service_list):
    command = "docker container rm "
    for service in service_list:
      execute_command(command + service)


def main():
    config_file_object = read_config_file()
    services = dict(config_file_object.items("services"))
    service_port_map = get_service_port_map(services)
    parser = argparse.ArgumentParser(description='Busybox ping test', epilog='Example of use: python script_name -b')
    parser.add_argument("-b", "--build", type=str, nargs='?', help=' to build alcor services provide :{} as an option'.format('-b build'))
    parser.add_argument("-t", "--testcase", type=int, nargs='?', help='Test case number or {} for all tests cases '.format('all'))
    parser.add_argument("-s", "--all", type=str, nargs='?', help = 'all tests cases')
    args = parser.parse_args()

    if args.build:
        if(args.build == "build"):
           build_containers(services)
        else:
           print("invoke  as {}".format('-b build'))

    stop_containers(service_port_map.keys())
    remove_containers(service_port_map.keys())
    if(start_containers(services) == True):
      print("All services started Sucessfully")
    else:
      print("Error:couldn't start all alcor services")
      sys.exit(1)

    aca = dict(config_file_object.items("AlcorControlAgents"))
    aca_nodes_ip_mac = check_alcor_agents_running(aca)
    if(len(aca_nodes_ip_mac) != len(aca)):
      print("\nERROR: Alcor Control Agent not running on some of the nodes")
      print("ERROR: Test exits")
      sys.exit(1)
    print("Wait for 20 seconds until all services are started...")
    time.sleep(20)

    if args.testcase:
      if (args.testcase == 1):
        ip_mac_db = prepare_test_case_1(aca_nodes_ip_mac, service_port_map)
      elif(args.testcase == 2):
        ip_mac_db = prepare_test_case_2(aca_nodes_ip_mac, service_port_map)
      else:
        print("Invoke {}".format('-t <testcase number>'))
    else:
      ip_mac_db = create_test_setup(aca_nodes_ip_mac, config_file_object)
      # if args.all== 'all':
      #  print("Invoke both all test cases"
      # ip_mac_db = prepare_all_test_cases(aca_nodes_ip_mac, service_port_map)'''

    #aca_nodes_ip_mac={"10.213.43.161":"90:17:ac:c1:30:68", "10.213.43.163":"90:17:ac:c1:30:3c"}
    #ip_mac_db={"10.0.1.101":"aa:bb:cc:a8:c9:c6", "10.0.1.102":"aa:bb:cc:df:79:f1"}
    container_names_dict = dict(config_file_object.items("test_setup"))["container_names"]
    container_names = json.loads(container_names_dict)
    print(container_names)
    #container_names = ["con1", "con2"]
    print("calling container deploy", aca_nodes_ip_mac, ip_mac_db, container_names)
    busybox_container_deploy(list(aca_nodes_ip_mac.keys()), ip_mac_db, container_names)


if __name__ == "__main__":
    main()

