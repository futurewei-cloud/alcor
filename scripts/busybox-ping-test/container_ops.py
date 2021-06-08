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

from helper_functions import *
from termcolor import colored

def busybox_container_cleanup(aca_node_ip, con):
    print("Cleaning up busybox container", con)
    command = "sudo ovs-docker del-ports br-int {}".format(con)
    output = run_command_on_host(aca_node_ip, command)
    print("Cleanup task: ", command, "\n", output, "\n")
    command = "sudo docker container stop {}".format(con)
    output = run_command_on_host(aca_node_ip, command)
    print("Cleanup task: ", command, "\n", output, "\n")
    command = "sudo docker container rm {} -f ".format(con)
    output = run_command_on_host(aca_node_ip, command)
    print("Cleanup task: ", command, "\n", output, "\n")
    command = "sudo ovs-vsctl del-br br-tun"
    output = run_command_on_host(aca_node_ip, command)
    print("Cleanup task: ", command, "\n", output, "\n")
    command = "sudo ovs-vsctl del-br br-int"
    output = run_command_on_host(aca_node_ip, command)
    print("Cleanup task: ", command, "\n", output, "\n")
    command = "sudo /usr/local/share/openvswitch/scripts/ovs-ctl start"
    output = run_command_on_host(aca_node_ip, command)
    print("Cleanup task: ", command, "\n", output, "\n")


def busybox_container_deploy(target_ips, ip_mac_db, container_names):
    index = 0;
    size = len(container_names)
    for db_ip, db_mac in ip_mac_db.items():
       con = container_names[index]
       aca_ip = target_ips[index]
       index = index + 1
       command1 = "sudo docker run -itd --name " + con + " --net=none busybox sh"
       command2 = "sudo ovs-docker add-port br-int eth1 " + con + " --ipaddress=" + db_ip + "/24" + " --macaddress=" + db_mac
       command3 = "sudo ovs-docker set-vlan br-int eth1 " + con + " 1"
       print("deploying busybox " + con + " on " + aca_ip)
       if con == "con1":
            gw = "10.0.1.1"
       else:
            gw = "10.0.2.1"
       command4 = "sudo docker exec -u root --privileged -it " + con + " route add default gw " + gw
       output = run_command_on_host(aca_ip, command1)
       print(con, "deploy task: ", output, "\n")

       output = run_command_on_host(aca_ip, command2)
       print(con, "deploy task: ", output, "\n")

       output = run_command_on_host(aca_ip, command3)
       print(con, "deploy task: ", output, "\n")
       output = run_command_on_host(aca_ip, command4)
       print(con, "add default gw : ", output, "\n")

       ip_addrs = list(ip_mac_db.keys())


def run_ping_test(target_machines, ip_addrs, container_names):
    index_0 = 0
    index_1 = 1
    ping_counts = 2

    ping_0_to_1 = "sudo docker exec -it " + container_names[index_0] + " ping -c " + str(ping_counts) + " " + ip_addrs[index_1]
    ping_1_to_0 = "sudo docker exec -it " + container_names[index_1] + " ping -c " + str(ping_counts) + " " + ip_addrs[index_0]

    HOST = target_machines[index_0]
    print("Ping test on ", HOST)
    output1 = run_command_on_host(HOST, ping_0_to_1)
    print("DDD: run_ping_test: 0 -> 1: ", output1)

    HOST = target_machines[index_1]
    print("Ping test on ", HOST)
    output2 = run_command_on_host(HOST, ping_1_to_0)
    print("DDD: run_ping_test: 1 -> 0: ", output2)

    expected_output = "2 packets transmitted, 2 packets received"
    if expected_output in str(output1) and expected_output in str(output2):
        print (colored("PING TEST SUCCESSFULL", 'green'))
    else:
        print(colored('PING TEST FAILED', 'red'))

