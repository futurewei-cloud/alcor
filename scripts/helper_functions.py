#!/usr/bin/python3
import subprocess as sp
from subprocess import *
import sys

# ok
def make_command(*argv):
    command =''
    for arg in argv:
      command += arg
    return command

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
       print("error",result[1])
       # return
     else:
       return result[0]
  except:
     print("Exception Error:", sys.exc_info()[0])


def run_command_forall(command, services_list):
    status = True
    # print("SSS", len(services_list))
    for service in services_list:
       # print("S ", service)
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

#execute_command("mvn -Dmaven.test.skip=true -DskipTests clean package install")
#execute_command("docker build -t ignite-11 -f/root/pingtest/alcor/lib/ignite.Dockerfile /root/pingtest/alcor/lib")
#execute_command("ls /usr/bin")

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

