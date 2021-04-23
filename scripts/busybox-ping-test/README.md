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
This is an end to end test where we test two Busybox containers, hosted on same or different hosts, ping each other.The connectivity between the two Busybox containers
is provided by Alcor services and Alcor Control Agent.

## Key Features
  - Builds Alcor and docker images for all alcor services (optional)
  - Stops and removes existing alcor services and starts the alcor services afresh.
  - Checks if the  Alcor Control Agents are running on the hosts over which Busybox containers are to be deployed.
  - Prepares the testcase using provided test payload in the configuration file and generate the goal state.
  - Deploys two busybox containers on the target hosts and assigns the IP/MAC obtained from the goal state generated in previous step.
  - Runs a ping command from one container to another.

## Directory Structure and Files
alcor/scripts/test-automation
1. alcor_services.ini
2. busybox_ping_test.py
3. busybox_container_ops.py
4. busybox_alcor_api_calls.py
5. busybox_helper_functions.py

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

With the above configuration the ignite service will be run as name 'ignite',
The vpc_manager is built from the Dockerfile located in services/vpc_manager folder and the container with the name vpm port 9001 is started.

## Preconditions
Ensure that your target hosts, over which you plan to deploy the Busybox containers 
1. have AlcorControl Agent running.
2. No busy box containers with names like con1, con2 etc. are present (Delete them if they are).

## Running Test
The main file for running the test is busybox_ping_test.py. It is a python script that can be run from command prompt in either of the following two ways:
````
python busybox_ping_test.py
./busybox_ping_test.py
````

You can optionally provide the paramter --build to build all the docker images of the alcor services. This step need not be followed for any subsequent tests, unless any changes are made in Alcor.
	
## After Test Starts
1. It will stop, remove existing Alcor services (if present) and start them all (as listed in alcor_services.ini file)
2. Checks whether the Alcor Control Agents are running on the targets. If found not running, the test stops.
3. Using the test info and payload, generate the end goal states for two end nodes.
4. deploy two busy box containers con1 and con2 on the target hosts and performs the ping test.

## Troubleshooting
1) observe the output from http get request for vpcs, subnets, nodes and ports. Check
     whether the goal state is generated or not. If not, check the configuration in
     alcor_services.ini and redeploy.

2) Run the following commands on target hosts where Alcor Control Agents Busybox
       containers are deployed to clear the initial stages of test:
	````python
       ovs-vsctl del-br br-tun
       ovs-vsctl del-br br-int
	````
    This will clean ovs bridges and we need to restart the Alcor Control Agents.
    Note: The clearing of containers or ovs bridges are not part of test script.

## Quick Start
After making the necessary configuration file changes, run the script with following paramters to get started:
1. ./ping_test.py --build
This will
 - build the alcor services and their docker images
 - and runs the simple test case of two containers under same subnet and security group pinging each other.
2. ./ping_test.py -t 1
This will
 - runs the test case two busyboxy containers on two subnets and same security group
3. ./ping_test.py -t 2
This will
 - runs the test case two busybox containers on one subnet and same two security group

## TO DO
1) Stop the test if one ore more Alcor services are not running.
2) Add error handling for deploying the containers.
3) Jenkins pipeline to be added.
4) Add the feature to start alcor agent on the target hosts (if not already started).
5) Check for the presence of any abandoned busybox containers on target hosts and delete them if needed.

