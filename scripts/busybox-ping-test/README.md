# MIT License
```
Copyright(c) 2020 Futurewei Cloud
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files(the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

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
3. Checks whether the ACAs are running on the targets. If found not running, the test stops.
4. Using the test info and payload provided in config file, generate the end goal states for two end nodes.
5. Deploy two busy box containers con1 and con2 on the target hosts and runs a ping command from one container to another.

## ACA on target hosts
1. Following packages are required to build and run ACA. Install the following packages on target hosts. Though not mentioned below, installing these pacakges will require sudo permissions.
```
openvswitch-switch
openvswitch-common
apache-pulsar-client
apache-pulsar-client-dev
```

2. The library 'openvswitch' is also required. This library can only be installed from source. Get a clone of this library from github and checkout 2.12 branch.
```
https://github.com/openvswitch/ovs.git
```
Install the following packages before building ovs
```
make
autoconf
libtool
c-ares
```
Now go to the ovs source and update the file 'configure.ac' and edit th line carrying LT_INIT to enable shared library creation before building:
* LT_INIT (enable_shared)
```
./configure --prefix=/usr --localstatedir=/var --sysconfdir=/etc
make
make install.
```

3. After the successful installation of ovs, start the following services:
```
sudo systemctl openvswitch-switch restart
sudo /usr/local/share/openvswitch/scripts/ovs-ctl start
```
The script ovs-ctsl starts the services vsdb-server and ovs-vswitchd.

4. If ACA or ovs services throw bridge related errors, clear the existing bridges for any given container on target hosts. The test script takes care of these itself. However, if you ever manually try to start ACA, following commands can be used to clear existing bridges.
```
  ovs-vsctl del-br br-tun
  ovs-vsctl del-br br-int
  ovs-docker del-ports br-int <container_name>
```

5. Following commands can be used to diagnose the target node's ovs services and bridges:
```
ovs-vsctl show
ovs-ofctl dump-flows br-tun
ovs-ofctl dump-flows br-int
```

## Troubleshooting
1) During the runing of test script, the user account 'ubuntu' from Alcor host will be making ssh connection to the target hosts. Ensure that user ubuntu has password less ssh access to the target hosts. Copy the contents of id_rsa.pub file of user 'ubuntu' (located at ~/.ssh) and paste into the file ~/.ssh/authorized_keys on target host.

2) Often after running the tests from a terminal on the Alcor hosts leaves the stdout and stdin in an unknown state. You can fix it by running
```
reset
```
While typing reset command you will not be able to see it. But once run, the terminal is restored.

3) While running the tests from Jenkins, it is essential that the jenkins user also has password less access to the target hosts. Easiest way to ensure that to copy the entire ~/.ssh folder of user 'ubuntu' on to the jenkins home directory, which is usually at /var/lib/jenkins. Ensure while copying that file attributes are preserved.
```
cp -pr /home/ubuntu/.ssh /var/lib/jenkins
chown -R jenkins:jenkins /var/lib/jenkins/.ssh
```
Go through the jenkins help file available in alcor-int repository to get addtional details on running tests through jenkins.

4) If the tests ever fails due to errors from Alcor API calls then observe the output from http get request from these calls. Check the configuration in alcor_services.ini and redeploy by manaully calling these APIs.


## Quick Start
After making the necessary configuration file changes, run the script with following paramters to get started:
1. ./ping_test.py -b build
 - build the alcor services and their docker images and
 - runs the simple test case of two containers under same subnet and security group pinging each other.
2. ./ping_test.py -t 1
 - runs the test case of two busyboxy containers on two subnets and same security group
3. ./ping_test.py -t 2
 - runs the test case of two busybox containers on one subnet and two security groups



