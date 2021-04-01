Introduction - This is an end to end test where we test two Busybox containers, hosted on same or different hosts, ping each other. The connectivity between the two Busybox containers is provided by Alcor services and Alcor Control Agent.

The test performs the following task:
- (optionally) builds, stops and removes existing alcor services and starts the alcor services afresh.
- Checks if the  Alcor Control Agents are running on the hosts over which Busybox containers are to be deployed.
- Prepares the testcase using provided test payload in the configuration file and generate the goal state.
- Deploys two busybox containers on the target hosts and assigns the IP/MAC obtained from the goal state generated in previous step.
- Runs a ping command from one container to another.

Repository:
alcor
Location and files:
alcor/scripts/test-automation
1)alcor_services.ini
2)busybox_ping_test.py
3)busybox_container_ops.py
4)busybox_alcor_api_calls.py
5)busybox_helper_functions.py

How to configure the test?
Test configuration should be presented in the file alcor_services.ini. Configuration file has following sections:
[services]: Carries the list of alcor service folders, service names and the ports they use.
[AlcorControlAgents]: New line separated list of hosts over which ACA is running and on which Busybox containers will be deployed.
[test_info]: Carries the data necessary for creating the end goal states.
[vpc_info]
[node_info]
[subnet_info]
[security_groups]
[port_info]: These carry the test payload that is needed to generate the end goal state.

Example:
You can configure the alcor services name, port number in the following way:
[services]
    ignite                 = {"name":"ignite", "port":10800, "path":"/lib/ignite", Dockerfile"}
    vpc_manager            = {"name":"vpm",    "port":9001}
    subnet_manager         = {"name":"snm",    "port":9002}
With the above configuration the ignite service will be run as name 'ignite', The vpc_manager is built from the Dockerfile located in services/vpc_manager folder and the container with the name vpm port 9001 is started.


Precondtions:
Ensure that your target hosts, over which you plan to deploy the Busybox containers have Alcor Control Agent running.


How to run the test?
The main file for running the test is busybox_ping_test.py. It is a python script that can be started from command prompt as:
- python busybox_ping_test.py
- ./busybox_ping_test.py
- You can optionally provide the paramter -b to build all the docker images of the alcor services. This step need not be followed for any subsequent tests, unless any changes are made in Alcor.
- Once the tests starts:
a) It will stop, remove existing Alcor services (if present) and start them all (as listed in alcor_services.ini file)
b) Checks whether the Alcor Control Agents are running on the targets. If found not running, the test stops.
c) Using the test info and payload, generate the end goal states for two end nodes.
d) deploy two busy box containers con1 and con2 on the target hosts and performs the ping test.
	
Troubleshoot test fails :
1) observe the output from http get request for vpcs, subnets, nodes and ports. Check whether the goal state is generated or not. If not, check the configuration in alcor_services.ini and redeploy.
2) Run the following commands on target hosts where Alcor Control Agents Busybox containers are deployed to clear the initial stages of test:
     ovs-vsctl del-br br-tun
     ovs-vsctl del-br br-int
This will clean ovs bridges and we need to restart the Alcor Control Agents.

Note: The clearing of containers or ovs bridges are not part of test script.

TO DO
1) Stop the test if one ore more Alcor services are not running.
2) Add error handling for deploying the containers.

