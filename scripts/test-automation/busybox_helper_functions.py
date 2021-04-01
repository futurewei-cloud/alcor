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

def get_filelist(mypath):
  print(mypath)
  onlyfiles = os.listdir(mypath)
  return onlyfiles

def check_process_running(HOST, process):
    running = False
    COMMAND = 'ps -ef | grep -I {}'.format(process)
    # print("In check process running", HOST, process)
    output = run_command_on_remote(HOST, COMMAND)
    if(output):
      for line in output.decode(encoding='utf-8').split('\n'):
        line = line.strip()
        if not 'grep' in line:
          # print("NOT A GREP LINE",line)
          if(line):
            running = True
            return running


def getmac_from_aca(HOST):
    cmd = "ifconfig | grep -A 2 {} | tail -1".format(HOST)
    output = run_command_on_remote(HOST, cmd)
    addr_string = str(output).split()
    mac_addr = addr_string[addr_string.index('ether')+1]
    return mac_addr


def run_command_on_remote(HOST, COMMAND):
  try:
     ssh1 = sp.Popen(['ssh', '-t', '{}@{}'.format('root', HOST), COMMAND], shell=False, stdout=sp.PIPE, stderr=sp.PIPE)
     result = ssh1.communicate()
     retcode = ssh1.returncode
     # print(retcode)
     if retcode > 0:
       if 'Connection to' not in result[1] and 'closed' not in result[1]:
         print("ERROR: ",result[1])
       #return retcode
     else:
       return result[0]
  except:
     print("Exception Error occured when running command {} on HOST {}:".format(COMMAND,HOST), sys.exc_info()[0])


def run_command_forall(command, services_list):
    status = True
    for service in services_list:
       if(execute_command(command + service)):
         status = False
         #print("Failed to ",repr(str(command)))
    return status


def print_output(output):
   for line in output.decode(encoding='utf-8').split('\n'):
      line = line.strip()
      print(line)


def execute_command(command):
  try:
      pipe = sp.Popen(command,stdout=sp.PIPE,stderr=sp.PIPE,shell=True)
      res = pipe.communicate()
      retcode =pipe.returncode
      #print(retcode)
      if retcode >0:
        print("Failed  to execute command", repr(str(command)))
        #print("stderr =", res[1])
        print_output(res[1])
      else:
        print("SUCCESS for command", command)
        # print output of the command for debug purposes
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


def execute_checkcommand(command):
    try:
        print(command)
        output = sp.check_output("ls /usr/bin/", stderr=sp.STDOUT, universal_newlines=True)
        print(output)
    except sp.CalledProcessError as error:
        errorMessage = ">>> Error while executing:\n" + command + "\n>>> Returned with error:\n" + str(error.output)
        print("ERROR: " + errorMessage)
    except FileNotFoundError as error:
         errorMessage = error.strerror
         print("ERROR: ", errorMessage)


def dict_clean(dict):
 result = {}
 for key,value in dict:
    if(value == 'null'):
      value = 'None'
    elif(value == 'true'):
      value = 'True'
    elif(value == 'false'):
      value = 'False'
    result[key]=value
 return(result)


def get_projectid():
  project_id = get_item_from_section("test_info","project_id")
  return project_id


def get_container_ips():
    ip_addrs = get_item_from_section("test_info","ip_addrs")
    return ip_addrs


def get_item_from_section(section,item):
   test_info = read_configfile_section(section)
   return test_info[item]


def read_configfile_section(section):
    config = configparser.ConfigParser()
    config._interpolation = configparser.ExtendedInterpolation()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    serv = dict(config.items(section))
    return serv

def read_configfile():
    config = configparser.ConfigParser()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    serv = dict(config.items('services'))
    return serv


def get_services_from_conf_file():
    serv = read_configfile()
    service_list = {}
    for service_name in serv.keys():
       service_info = json.loads(serv[service_name])
       service_list[service_info["name"]] = service_info["port"]
    return service_list


def read_aca_ips():
    config = configparser.ConfigParser()
    conf_file =  "{}/alcor_services.ini".format(ALCOR_TEST_DIR)
    config.read(conf_file)
    aca = dict(config.items('AlcorControlAgents'))
    return aca


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


