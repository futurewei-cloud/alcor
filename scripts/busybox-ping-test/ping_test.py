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
ALCOR_AGENTS_BINARY_PATH = "./repos/aca/build/bin/AlcorControlAgent"

# Builds the Ignite and all Alcor images as configured in
# alcor_services.ini file
def build_containers(services_dict):
    container_list =[]
    mvn_build = "mvn -Dmaven.test.skip=true -DskipTests clean package install"
    container_list.append(mvn_build)

    print("building container images")
    services_list = get_file_list(ALCOR_SERVICES)
    for service_name in services_dict.keys():
      service_path = ALCOR_SERVICES + service_name
      service_info = json.loads(services_dict[service_name])
      build_image = "sudo docker build" + " -t {} ".format(service_info["name"])
      if service_name == "ignite":
         docker_file = "-f {} {}".format(ALCOR_ROOT + service_info["path"], ALCOR_ROOT + "/lib")
      else:
         docker_file = ALCOR_SERVICES + service_name
      docker_build_cmd = build_image + docker_file
      container_list.append(docker_build_cmd)

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
      print("Could not start all alcor services.")
      print("Error! Test Exits")
      sys.exit(1)


def stop_containers(service_list):
    command = "sudo docker container stop "
    for service in service_list:
      execute_command(command + service)


def remove_containers(service_list):
    command = "sudo docker container rm "
    for service in service_list:
      execute_command(command + service)


def main():
    config_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config_file_object = read_config_file(config_file)
    services_dict = dict(config_file_object.items("services"))
    service_port_map = get_service_port_map(services_dict)
    parser = argparse.ArgumentParser(description='Busybox ping test', epilog='Example of use: python script_name -b')
    parser.add_argument("-b", "--build", type=str, nargs='?', help=' to build alcor services provide :{} as an option'.format('-b build'))
    parser.add_argument("-t", "--testcase", type=int, nargs='?', help='Test case number or {} for all tests cases '.format('all'))
    parser.add_argument("-s", "--all", type=str, nargs='?', help = 'all tests cases')
    args = parser.parse_args()

    if args.build:
        if(args.build == "build"):
           build_containers(services_dict)
        else:
           print("To build before running the tests, use '-b build'")
           print("ERROR: Quitting test\n")
           sys.exit(1)

    stop_containers(service_port_map.keys())
    remove_containers(service_port_map.keys())
    if(start_containers(services_dict) == True):
      print("All services started Sucessfully")
    else:
      print("ERROR: All Alcor services did NOT start successfully")
      print("ERROR: Quitting test\n")
      sys.exit(1)

    container_names_dict = dict(config_file_object.items("test_setup"))["container_names"]
    container_names = json.loads(container_names_dict)
    aca = dict(config_file_object.items("AlcorControlAgents"))
    for aca_node,con in zip(aca.values(),container_names):
       print("Busybox container cleanup...", aca_node, con)
       busybox_container_cleanup(aca_node,con)
    time.sleep(10)
    check_alcor_agents_running(aca)
    time.sleep(30)
    if(restart_alcor_agents(aca,ALCOR_AGENTS_BINARY_PATH)== False):
      print("Couldn't start AlcorControlAgent successfully")
      print("ERROR: Test exits")
      sys.exit(1)
    time.sleep(10)
    aca_nodes_ip_mac = get_macaddr_alcor_agents(aca)
    print("ACA nodes IP MAC pair::", aca_nodes_ip_mac)

    if len(aca_nodes_ip_mac) != len(aca):
      print("ERROR: Alcor Control Agent not running on some of the nodes")
      print("ERROR: Quitting test\n")
      sys.exit(1)

    print("Wait for 60 seconds until all services are started...")
    time.sleep(60)

    if args.testcase:
      if (args.testcase == 1):
        ip_mac_db = prepare_test_case_1(aca_nodes_ip_mac, service_port_map)
      elif(args.testcase == 2):
        ip_mac_db = prepare_test_case_2(aca_nodes_ip_mac, service_port_map)
      else:
        print("Invoke {}".format('-t <testcase number>'))
        print("ERROR: Quitting test\n")
        sys.exit(1)
    else:
      ip_mac_db = create_test_setup(aca_nodes_ip_mac, config_file_object)
      # if args.all== 'all':
      #   print("Invoke both all test cases"
      # ip_mac_db = prepare_all_test_cases(aca_nodes_ip_mac, service_port_map)

    #aca_nodes_ip_mac={"10.213.43.161":"90:17:ac:c1:30:68", "10.213.43.163":"90:17:ac:c1:30:3c"}
    #ip_mac_db={"10.0.1.101":"aa:bb:cc:a8:c9:c6", "10.0.1.102":"aa:bb:cc:df:79:f1"}
    #container_names_dict = dict(config_file_object.items("test_setup"))["container_names"]
    #container_names = json.loads(container_names_dict)

    aca_node_ips = list(aca_nodes_ip_mac.keys())
    goal_state_ips = list(ip_mac_db.keys())
    print("Deploying containers on target nodes")
    print("ACA nodes: ", aca_node_ips)
    print("Goal states: ", ip_mac_db)
    print("Container names: ", container_names)
    busybox_container_deploy(aca_node_ips, ip_mac_db, container_names)
    run_ping_test(aca_node_ips, goal_state_ips, container_names)


if __name__ == "__main__":
    main()

