#!/usr/bin/python3
import subprocess as sp
import sys, os, configparser
import json
from   subprocess import *

ALCOR_TEST_DIR = os.path.dirname(os.path.abspath(__file__))

def make_docker_command(*argv):
    command ='docker '
    for arg in argv:
      command += arg
    return command


def get_file_list(mypath):
    print(mypath)
    onlyfiles = os.listdir(mypath)
    return onlyfiles


# Check on a given HOST if a given process is running
# Return True/False accordingly 
def check_process_running(HOST, process):
    running = False
    COMMAND = 'ps -ef | grep -I {}'.format(process)
    output = run_command_on_host(HOST, COMMAND)
    if(output):
      for line in output.split('\n'):
        line = line.strip()
        if not 'grep' in line:
          if(line):
            running = True
    return running


# Get the mac address of given host
def get_mac_id(HOST):
    cmd = "ifconfig | grep -A 2 {} | tail -1".format(HOST)
    output = run_command_on_host(HOST, cmd)
    addr_string = str(output).split()
    mac_addr = addr_string[addr_string.index('ether') + 1]
    return mac_addr


# Function to run a given command on a given host
# Returns output on success, otherwise prints error code
def run_command_on_host(HOST, COMMAND):
    try:
      ssh1 = sp.Popen(['ssh', '-t', '{}@{}'.format('root', HOST), COMMAND], shell=False, stdout=sp.PIPE, stderr=sp.PIPE, encoding='utf8')
      result = ssh1.communicate()
      retcode = ssh1.returncode
      if retcode > 0:
        if 'Connection to' not in result[1] and 'closed' not in result[1]:
          print("ERROR: ", result[1])
      else:
        return result[0]
    except Exception as e:
      print(e)
      print("Exception thrown when running command {} on HOST {}:".format(COMMAND, HOST), sys.exc_info()[0])


def print_output(output):
    for line in output.decode(encoding='utf-8').split('\n'):
      line = line.strip()
      print(line)


def execute_command(command):
    try:
      pipe = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True)
      res = pipe.communicate()
      retcode = pipe.returncode
      if retcode > 0:
        print("Failed to execute command", repr(str(command)))
        print_output(res[1])
      else:
        print("SUCCESS for command", command)
        # print output of the command when debugging
        # print_output(res[0])
      return retcode
    except:
      print("ERROR", sys.exc_info()[0])


def execute_commands(cmd, command_list):
    print("execute commands ")
    status = True
    for command in command_list:
      print(cmd, " ..", str(command))
      if(execute_command(command)):
        print("Failed to ", cmd, command)
        status = False
    return status


def dict_clean(dict):
   result = {}
   for key, value in dict:
     if(value == 'null'):
       value = 'None'
     elif(value == 'true'):
       value = 'True'
     elif(value == 'false'):
       value = 'False'
     result[key]=value
   return(result)


# Return project id from config file under section 'test_setup'
def get_projectid():
    test_setup = read_config_file_section("test_setup")
    return test_setup["project_id"]


# Return container 'ip_addrs' from config file under test_setup section
def get_container_ips():
    test_setup = read_config_file_section("test_setup")
    return test_setup["ip_addrs"]


# Return the given section from the config file as a dictionary
def read_config_file_section(section):
    config = configparser.ConfigParser()
    config._interpolation = configparser.ExtendedInterpolation()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    serv = dict(config.items(section))
    return serv


def read_config_file():
    config = configparser.ConfigParser()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    return config
    

def get_service_port_map(serv):
    service_list = {}
    for service_name in serv.keys():
       service_info = json.loads(serv[service_name])
       service_list[service_info["name"]] = service_info["port"]
    return service_list

"""
def read_aca_ips():
    config = configparser.ConfigParser()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    aca = dict(config.items('AlcorControlAgents'))
    return aca """


#This function checks the 'AlcorControlAgent' running on a host and returns its mac address
def check_alcor_agents_running(aca):
    ip_mac = {}
    for ip_addr in aca.values():
      if(check_process_running(ip_addr.strip(), "AlcorControlAgent") == True):
         print("AlcorControlAgent is running on {}".format(ip_addr))
         mac_addr = get_mac_id(ip_addr)
         print("Mac_addr", mac_addr, "for host", ip_addr, "\n")
         ip_mac[ip_addr] = mac_addr
      else:
         print("AlcorControlAgent is not running on {}".format(ip_addr))
    return ip_mac
