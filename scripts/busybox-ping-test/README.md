# Busybox Ping Test

# In this README:
- [Introduction](#Introduction)
- [Key Features](#Key-Features)
- [Directory Structure](#Directory-Structure)
- [Test Configuration](#Test-Configuration)
- [Examples](#Examples)
- [Preconditions](#Preconditions)
- [Troubleshooting](#Troubleshooting)
- [Running Test](#Running-Test)

## Introduction
This is an end to end test where we test two Busybox containers, hosted on same or different hosts, ping each other.The connectivity between the two Busybox containers is provided by Alcor services and Alcor Control Agent.

## Key Features of Test Script
  - Builds Alcor and docker images for all alcor services (optional).
  - Stops and removes existing alcor services and starts the alcor services afresh.
  - Stops any currently running ACA on target computers where Busybox containers are to be deployed.
  - Clears any existing busybox containers on the target hosts.
  - Checks if the Alcor Control Agents are running on the target hosts.
  - Prepares the testcase using provided test payload in the configuration file and generate the goal state.
  - Deploys two busybox containers on the target hosts and assigns the IP/MAC obtained from the goal state generated in previous step.
  - Runs a ping command from one container to another.

## Directory Structure and Files
alcor/scripts/test-automation
1. alcor_services.ini
2. ping_test.py
3. helper_functions.py
4. container_ops.py
5. prepare_payload.py
6. create_test_setup.py
7. create_test_cases.py

## Test Configuration
Test configuration should be presented in the file alcor_services.ini. Configuration file has following sections:
1. [services]: Carries the list of alcor service folders, service names and the ports they use.
2. [AlcorControlAgents]: New line separated list of hosts over which ACA is running and on which Busybox containers will be deployed.
3. [test_info]: Carries the data necessary for creating the end goal states.
4. [vpc_info], [node_info], [subnet_info], [security_groups], [port_info]: These carry the test payload that is needed to generate the end goal state.

## Example
You can configure the alcor services name, port number in the following way:
[services]
1. ignite                 = {"name":"ignite", "port":10800, "path":"/lib/ignite",Dockerfile"}
2. vpc_manager            = {"name":"vpm",    "port":9001}
3. subnet_manager         = {"name":"snm",    "port":9002}

With the above configuration the ignite service will be run with the name 'ignite',
The vpc_manager is built from the Dockerfile located in services/vpc_manager folder and the container with the name vpm port 9001 is started.

## Preconditions
Ensure that your target hosts, over which you plan to deploy the Busybox containers
1. Have Alcor Control Agent binaries located at /home/ubuntu/repos/aca/build/bin/AlcorControlAgent

## Running Test
The main file for running the test is ping_test.py. It is a python script that can be run from command prompt in either of the following two ways:
```
python3 busybox_ping_test.py
./busybox_ping_test.py
```

You can optionally provide the paramter "-b build" to build all the docker images of the alcor services. This step need not be followed for any subsequent tests, unless any changes are made in Alcor.
	
## After Test Starts
1. It will stop, remove existing Alcor services (if present) and start them all (as listed in alcor_services.ini file)
2. Checks the target hosts if any Alcor Control Agent (ACA) is running. If yes, it is killed and ACA restarted.
2. Checks whether the ACAs are running on the targets. If found not running, the test stops.
3. Using the test info and payload provided in config file, generate the end goal states for two end nodes.
4. deploy two busy box containers con1 and con2 on the target hosts and runs a ping command from one container to another.

## Troubleshooting
1) observe the output from http get request for vpcs, subnets, nodes and ports. Check whether the goal state is generated or not. If not, check the configuration in alcor_services.ini and redeploy.

2) Run the following commands on target hosts to clear the initial stages of test:
	````python
       ovs-vsctl del-br br-tun
       ovs-vsctl del-br br-int
	````
3)

## Quick Start
After making the necessary configuration file changes, run the script with following paramters to get started:
1. ./ping_test.py -b build
This will
 - build the alcor services and their docker images and
 - runs the simple test case of two containers under same subnet and security group pinging each other.
2. ./ping_test.py -t 1
This will
 - runs the test case of two busyboxy containers on two subnets and same security group
3. ./ping_test.py -t 2
This will
 - runs the test case of two busybox containers on one subnet and two security groups


