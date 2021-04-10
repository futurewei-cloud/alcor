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

#!/bin/bash/python3
from busybox_helper_functions import *
from busybox_alcor_api_calls import *
from termcolor import colored

# Subnet mask can be obtained from variable
# Error handling for docker commands
# All global variables can be placed in a different file and be imported.

container_names = ["con1", "con2"]

def busybox_container_cleanup(ip,con):
  command = "ovs-docker del-ports br-int ".format(con)
  output = run_command_on_remote(ip, command)
  command = "docker container stop ".format(con)
  output = run_command_on_remote(ip, command)
  command = "docker container rm {} -f ".format(con)
  output = run_command_on_remote(ip, command)



def busybox_container_deploy(ip_mac, ip_mac_db):
   for con, (aca_ip, aca_mac), (db_ip, db_mac) in zip(container_names, ip_mac.items(), ip_mac_db.items()):
     print(con, aca_ip, db_ip, db_mac)
     print("cleaning containers..")
     busybox_container_cleanup(aca_ip,con)
     command1 = "docker run -itd --name " + con + " --net=none busybox sh"
     command2 = "ovs-docker add-port br-int eth1 " + con + " --ipaddress=" + db_ip + "/24" + " --macaddress=" + db_mac
     command3 = "ovs-docker set-vlan br-int eth1 " + con + " 1"
     output   = "deploying busybox " + con + " on " + aca_ip
     print(output)
     output = run_command_on_remote(aca_ip, command1)
     output = run_command_on_remote(aca_ip, command2)
     output = run_command_on_remote(aca_ip, command3)
     ip_addrs = list(ip_mac_db.keys())
     run_ping_test(ip_mac,ip_addrs)

def run_ping_test(ip_mac,ip_addrs):
   src_index = 0
   dest_index = 1
   ping_counts = 2
   container_name = container_names[src_index]
   dest_bb_ip = ip_addrs[dest_index]
   command1 = "docker exec -it " + container_name + " ping -c " + str(ping_counts) + " " + dest_bb_ip
   container_name = container_names[dest_index]
   dest_bb_ip = ip_addrs[src_index]
   command2 = "docker exec -it " + container_name + " ping -c " + str(ping_counts) + " " + dest_bb_ip
   target_machine_list = list(ip_mac.keys())
   target_machine1 = target_machine_list[src_index]
   target_machine2 = target_machine_list[dest_index]
   output1 = "running command " + command1 + " on " + target_machine1
   output1 =  run_command_on_remote(target_machine1, command1)
   #print("PING OUTPUT",output1)
   check_string = "2 packets transmitted, 2 packets received"
   output2 =  run_command_on_remote(target_machine2, command2)
   #print("PING OUTPUT",output2)
   temp = str(output1)
   if check_string in str(output1):
     if check_string in str(output2):
         print (colored("PING  TEST SUCCESSFULL", 'green'))
   else:
     print(colored('PING TEST FAILED','red'))

