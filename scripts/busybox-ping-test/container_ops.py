#!/bin/bash/python3
from helper_functions import *
from termcolor import colored

#container_names = ["con1", "con2"]

def busybox_container_cleanup(ip, con):
    print("Cleaning up busybox container", con)
    command = "ovs-docker del-ports br-int ".format(con)
    output = run_command_on_host(ip, command)
    command = "docker container stop ".format(con)
    output = run_command_on_host(ip, command)
    command = "docker container rm {} -f ".format(con)
    output = run_command_on_host(ip, command)


def busybox_container_deploy(target_ips, ip_mac_db, container_names):
    print("Deploying busybox container")
    index = 0;
    size = len(container_names)
    print(container_names)
    for db_ip, db_mac in ip_mac_db.items():
       con = container_names[index]
       aca_ip = target_ips[index]
       index = index + 1
       command1 = "docker run -itd --name " + con + " --net=none busybox sh"
       command2 = "ovs-docker add-port br-int eth1 " + con + " --ipaddress=" + db_ip + "/24" + " --macaddress=" + db_mac
       command3 = "ovs-docker set-vlan br-int eth1 " + con + " 1"
       output   = "deploying busybox " + con + " on " + aca_ip
       output = run_command_on_host(aca_ip, command1)
       output = run_command_on_host(aca_ip, command2)
       output = run_command_on_host(aca_ip, command3)
       ip_addrs = list(ip_mac_db.keys())
    run_ping_test(target_ips, list(ip_mac_db.keys()), container_names)


def run_ping_test(target_machines, ip_addrs, container_names):
    index_0 = 0
    index_1 = 1
    ping_counts = 2

    ping_0_to_1 = "docker exec -it " + container_names[index_0] + " ping -c " + str(ping_counts) + " " + ip_addrs[index_1]
    ping_1_to_0 = "docker exec -it " + container_names[index_1] + " ping -c " + str(ping_counts) + " " + ip_addrs[index_0]

    HOST = target_machines[index_0]
    print("Ping test on ", HOST)
    output1 = run_command_on_host(HOST, ping_0_to_1)

    HOST = target_machines[index_1]
    print("Ping test on ", HOST)
    output2 = run_command_on_host(HOST, ping_1_to_0)

    expected_output = "2 packets transmitted, 2 packets received"
    if expected_output in str(output1) and expected_output in str(output2):
        print (colored("PING TEST SUCCESSFULL", 'green'))
    else:
        print(colored('PING TEST FAILED', 'red'))
