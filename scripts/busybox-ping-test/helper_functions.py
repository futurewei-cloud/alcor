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

import subprocess as sp
import sys, os, pwd, configparser
import json,time
from   subprocess import *

ALCOR_TEST_DIR = os.path.dirname(os.path.abspath(__file__))

def make_docker_command(*argv):
    command ='sudo docker '
    for arg in argv:
      command += arg
    return command


def get_file_list(mypath):
    print(mypath)
    onlyfiles = os.listdir(mypath)
    return onlyfiles

def get_aca_pid_and_kill(HOST,output):
  print(output)
  kill_status = False
  pid=[]
  if output:
    for x in output.split():
      print("Pid is ",x)
      if x !='None':
        print(type(x),x,"x is not None inside")
        COMMAND = 'sudo kill -9 {}'.format(int(x))
        output = run_command_on_host(HOST,COMMAND)
      else:
        print("None pid")
      if not output:
        kill_status = True
  return kill_status


def kill_running_aca(HOST):
     #COMMAND = "sudo ps aux | grep {}".format("AlcorControlAgent")
     COMMAND = 'sudo pidof {}'.format("AlcorControlAgent")
     output = run_command_on_host(HOST,COMMAND)
     #print("OOO",output)
     if get_aca_pid_and_kill(HOST,str(output)) == True:
       return True
     else:
       return False

def restart_alcor_agents(aca,path):
   for HOST in aca.values():
      COMMAND = 'sudo {} -d'.format(path)
      print(COMMAND)
      ssh1 = sp.Popen(['ssh',
             '-t','{}@{}'.format(get_username(), HOST), COMMAND],shell=False,stdout=sp.PIPE,
             stderr=sp.PIPE, encoding='utf8')

      output1 = ssh1.poll()
      print(aca,"Restart output1 ",output1)
      time.sleep(2)
      output2 = ssh1.poll()
      print(aca,"Restart output2 ",output2)
   if output1 == 255 or output2 == 255:
     return False
   else:
     return True

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

def get_username():
   #return pwd.getpwuid( os.getuid() )[ 0 ]
   return 'ubuntu'


# Function to run a given command on a given host
# Returns output on success, otherwise prints error code
def run_command_on_host(HOST, COMMAND):
    try:
      print("run_command_on_host: U = {}, H = {}, C = {}".format(get_username(), HOST, COMMAND))
      ssh1 = sp.Popen(['ssh',
                       '-o StrictHostKeyChecking=no',
                       '-o UserKnownHostsFile=/dev/null',
                       '-tt',
                       '{}@{}'.format(get_username(), HOST), COMMAND],
                       shell=False,
                       stdout=sp.PIPE,
                       stderr=sp.PIPE,
                       encoding='utf8')
      result = ssh1.communicate()
      print("Remote output",result)
      retcode = ssh1.returncode
      if "Segmentation fault" in str(result):
        return "segmentation  fault"
      print("Remote: ", retcode)
      if retcode > 0:
        print(result[1],retcode)
        if 'Connection to' not in result[1] and 'closed' not in result[1]:
          print("ERROR: ", result[1])
      else:
        #print("In else ",result[0])
        return result[0]
    except Exception as e:
      print(e)
      print("Exception thrown when running command {} on HOST {}:".format(COMMAND, HOST), sys.exc_info()[0])


def print_output(output):
    for line in output.decode(encoding='utf-8').split('\n'):
      line = line.strip()
      print(line)


# check if all alcor services are up and running.
# Success: all ports from 9001 through 9016 should show up in netstat output
# try for 5 minutes, waiting 10 seconds each time
# NOTE: Update the port list when new services are added.
# 9001 9002 9003 9004 9005 9006 9007 9008 9009 9010 9011 9012 9015 9016
def check_alcor_services():
    wait_limit = 300
    sleep_time = 10
    wait_time = 0
    try:
        command = "netstat -ant | awk -F: '/90[0-9][0-9]/ {print $4}' | sed 's/[\t ]*$//' | sort -n | tr '[\n]' ' ' | sed 's/[\t ]*$//'"
        iter = 1
        while wait_time < wait_limit:
            pipe = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True)
            res = pipe.communicate()
            retcode = pipe.returncode
            if retcode > 0:
                print("Failed to execute command", repr(str(command)))
                print_output(res[1])
            elif "9001 9002 9003 9004 9005 9006 9007 9008 9009 9010 9011 9012 9014 9015" in str(res):
                print("SUCCESS for: ", command, "\n")
                return True
            elif "9001 9002 9003 9004 9005 9006 9007 9008 9009 9010 9011 9012 9015 9016" in str(res):
                print("SUCCESS for: ", command, "\n")
                return True
            iter = iter + 1
            wait_time = wait_time + sleep_time
            time.sleep(sleep_time)

        return False
    except:
        print("ERROR", sys.exc_info()[0])
    return False



def execute_command(command):
    try:
      pipe = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True)
      res = pipe.communicate()
      retcode = pipe.returncode
      if retcode > 0:
        print("Failed to execute command", repr(str(command)))
        print_output(res[1])
      else:
        print("SUCCESS for: ", command, "\n")
        # print output of the command when debugging
        # print_output(res[0])
      return retcode
    except:
      print("ERROR", sys.exc_info()[0])


def execute_commands(cmd, command_list):
    print("Executing commands in given list\n")
    status = True
    for command in command_list:
      print(cmd, ":    ", str(command))
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
    proj = test_setup["project_id"]
    return proj.replace('"', '')


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


def read_config_file(config_file):
    config = configparser.ConfigParser()
    config.read(config_file)
    return config


def get_service_port_map(serv):
    service_list = {}
    for service_name in serv.keys():
       service_info = json.loads(serv[service_name])
       service_list[service_info["name"]] = service_info["port"]
    return service_list

def get_macaddr_alcor_agents(aca):
   ip_mac ={}
   for ip_addr in aca.values():
      mac_addr = get_mac_id(ip_addr)
      print("Mac_addr", mac_addr, "for host", ip_addr, "\n")
      ip_mac[ip_addr] = mac_addr
   return ip_mac

#This function checks the 'AlcorControlAgent' running on a host and returns its mac address
def check_alcor_agents_running(aca):
    for ip_addr in aca.values():
      if(check_process_running(ip_addr.strip(), "AlcorControlAgent") == True):
         print("AlcorControlAgent is running on {}".format(ip_addr))
         if(kill_running_aca(ip_addr)== True):
           print("Running Alcor agent on {} has been killed Successfully".format(ip_addr))
         else:
           print("Running Alcor agent on {} couldn't be killed".format(ip_addr))
         '''mac_addr = get_mac_id(ip_addr)
         print("Mac_addr", mac_addr, "for host", ip_addr, "\n")
         ip_mac[ip_addr] = mac_addr'''
      else:
         print("AlcorControlAgent is not running on {}".format(ip_addr))

def get_gateway_for_ip(ip_addr):
    gateways = read_config_file_section("gateways")
    # print(gateways)
    gateway_info = json.loads(gateways["gateway_info"])
    # print(gateway_info)
    for gw in gateway_info:
        for ip in gw["ips"]:
            if ip == ip_addr:
                # print("FOUND GW: IP = {}, GW = {}".format(ip_addr, gw["gw"]))
                return gw["gw"]
    return None
