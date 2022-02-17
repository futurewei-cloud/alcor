/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


/*
This is the code for the test controller, for testing the reactions between the Network Configuration manager and
the ACA.

Params:
0. Number of VPCs to generate
1. Number of ports to generate on aca node one per VPC
2. Number of ports to generate on aca node two per VPC
3. IP of aca_node_one
4. IP of aca_node_two
5. IP of the GRPC call
6. Port of the GRPC call
7. User name of aca_nodes
8. Password of aca_nodes
9. Ping mode, either CONCURRENT_PING_MODE(0 and default), or SEQUENTIAL_PING_MODE(other numnbers)
10. Whether execute background ping or not. If set to 1, execute background ping; otherwise, don't execute background ping
11. Whether to create containers and execute ping.
*/
package com.futurewei.alcor.pseudo_controller;

import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.google.common.util.concurrent.RateLimiter;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.TracingClientInterceptor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.awaitility.Awaitility;
import io.jaegertracing.Configuration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;


import javax.annotation.PostConstruct;
import javax.swing.text.PlainDocument;

@SpringBootApplication
//@Component
//@Configurable
public class pseudo_controller {

    @Value("${node_one_ip:ip_one}")
    String aca_node_one_ip;
    @Value("${node_two_ip:ip_two}")
    String aca_node_two_ip;
    static final int NUMBER_OF_NODES = 2;
    @Value("${ncm_ip:ip_three}")
    String ncm_ip;
    @Value("${ncm_port:123}")
    int ncm_port;
    @Value("${user_name:root}")
    String user_name;
    @Value("${password:abcdefg}")
    String password;
    @Value("${ports_node_one:1}")
    int ports_to_generate_on_aca_node_one;
    @Value("${ports_node_two:1}")
    int ports_to_generate_on_aca_node_two;
    @Value("${ping_mode:1}")
    int user_chosen_ping_method;
    @Value("${background_ping:0}")
    int user_chosen_execute_background_ping;
    static final int CREATE_CONTAINER_AND_PING = 0;
    @Value("${create_container_and_ping:0}")
    int whether_to_create_containers_and_ping = CREATE_CONTAINER_AND_PING;
    @Value("${number_of_vpcs:1}")
    int number_of_vpcs;
    @Value("${test_against_ncm:true}")
    Boolean test_against_ncm;
    @Value("${vpm_ip:192.168.0.0}")
    String vpm_ip;
    @Value("${vpm_port:1234}")
    String vpm_port;
    @Value("${snm_ip:192.168.0.0}")
    String snm_ip;
    @Value("${snm_port:1234}")
    String snm_port;
    @Value("${pm_ip:192.168.0.0}")
    String pm_ip;
    @Value("${pm_port:1234}")
    String pm_port;
    /*
        vpc_cidr_slash, the number after the slash in the vpc CIDR, decides how big the VPC is,
        such as 10.0.0.0/16 or 10.0.0.0/8.
    */
    @Value("${vpc_cidr_slash:16}")
    int vpc_cidr_slash;
    /*
        tenant_amount = concurrency when calling APIs.
    */
    @Value("${tenant_amount:1}")
    int tenant_amount;
    /*
        project_amount_per_tenant, each tenant can have multiple projects.
    */
    @Value("${project_amount_per_tenant:1}")
    int project_amount_per_tenant;
    /*
        vpc_amount_per_project, each project can have multiple VPCs.
        each VPC can have the same CIDR
    */
    @Value("${vpc_amount_per_project:1}")
    int vpc_amount_per_project;
    /*
        subnet_amount_per_vpc, each VPC can have multiple subnets.
    */
    @Value("${subnet_amount_per_vpc:2}")
    int subnet_amount_per_vpc;
    /*
        port_amount_per_subnet, each subnet can have multiple ports.
    */
    @Value("${port_amount_per_subnet:3}")
    int port_amount_per_subnet;
    @Value("${test_vpc_api:false}")
    Boolean test_vpc_api;
    @Value("${test_subnet_api:false}")
    Boolean test_subnet_api;
    @Value("${test_port_api:false}")
    Boolean test_port_api;
    @Value("${call_api_rate:1}")
    int call_api_rate;


    static String docker_ps_cmd = "docker ps";
    static String vpc_id_1 = "2b08a5bc-b718-11ea-b3de-111111111112";
    static String port_ip_template = "11111111-b718-11ea-b3de-";
    static String subnet_id_1 = "27330ae4-b718-11ea-b3df-111111111113";
    static String subnet_id_2 = "27330ae4-b718-11ea-b3df-111111111114";
    static String ips_ports_ip_prefix = "10";
    static String mac_port_prefix = "00:00:01:";//"6c:dd:ee:";
    static String project_id = "alcor_testing_project";
    static SortedMap<String, String> ip_mac_map = new TreeMap<>();
    static Vector<String> aca_node_one_commands = new Vector<>();
    static Vector<String> aca_node_two_commands = new Vector<>();
    static SortedMap<String, String> port_ip_to_host_ip_map = new TreeMap<>();
    static SortedMap<String, String> port_ip_to_id_map = new TreeMap<>();
    static SortedMap<String, String> port_ip_to_container_name = new TreeMap<>();
    static Vector<String> node_one_port_ips = new Vector<>();
    static Vector<String> node_two_port_ips = new Vector<>();
    static final int CONCURRENT_PING_MODE = 0;
    static final int THREAD_POOL_SIZE = 10;
    static ExecutorService concurrent_create_containers_thread_pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    static ExecutorService backgroundPingExecutor = Executors.newFixedThreadPool(1);
    static final int DO_EXECUTE_BACKGROUND_PING = 1;
    static int finished_sending_goalstate_hosts_count = 0;

    static final int DEFAULT_VLAN_ID = 1;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(pseudo_controller.class, args);
    }

    @PostConstruct
    private void runTest(){
        System.out.println("Running pseudo controller code!");

        if (test_against_ncm){
            run_test_against_ncm();
        }else{
            run_test_against_alcor_apis();
        }
        System.exit(0);
    }

    private void run_test_against_ncm(){

        System.out.println("There are "+ number_of_vpcs+" VPCs, ACA node one has "+ ports_to_generate_on_aca_node_one + " ports per VPC;\nACA node two has "+ports_to_generate_on_aca_node_two+" ports per VPC. \nTotal ports per VPC: "+(ports_to_generate_on_aca_node_one + ports_to_generate_on_aca_node_two));
        generate_ip_macs(ports_to_generate_on_aca_node_one + ports_to_generate_on_aca_node_two);
        create_containers_on_both_hosts_concurrently();
        System.out.println("aca_node_one_ip: " + aca_node_one_ip + "\naca_node_two_ip: " + aca_node_two_ip + "\nuser name: " + user_name + "\npassword: " + password);

        System.out.println("Containers setup done, now we gotta construct the GoalStateV2");

        System.out.println("Trying to build the GoalStateV2 for " + (ports_to_generate_on_aca_node_one + ports_to_generate_on_aca_node_two) + " Ports");


        Goalstate.GoalStateV2.Builder GoalState_builder_one = Goalstate.GoalStateV2.newBuilder();
        Goalstate.GoalStateV2.Builder GoalState_builder_two = Goalstate.GoalStateV2.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_one = Goalstate.HostResources.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_two = Goalstate.HostResources.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_one_port_one_neighbor = Goalstate.HostResources.newBuilder();

        for (int vpc_number = 1 ; vpc_number <= number_of_vpcs ; vpc_number ++){
            // generate VPC ID and Subnet ID for the current VPC
            String current_vpc_id = vpc_id_1.substring(0, vpc_id_1.length()-3)+String.format("%03d", (vpc_number));
            String current_subnet_id = subnet_id_1.substring(0,  subnet_id_1.length()-3) + String.format("%03d", (vpc_number));
            System.out.println("Current vpc_id: " + current_vpc_id + ", current subnet id: " + current_subnet_id);
            // Generate three unique octets for each VPC
            // instead of using the same mac_port_prefix for all ports
            String mac_first_octet, mac_second_octet, mac_third_octet;

            mac_first_octet = Integer.toHexString(vpc_number / 10000);
            mac_second_octet = Integer.toHexString((vpc_number % 10000) / 100 );
            mac_third_octet = Integer.toHexString(vpc_number % 100);

            String current_vpc_mac_prefix = mac_first_octet + ":" + mac_second_octet + ":" + mac_third_octet + ":";

            /*
             generate port states for the current VPC, currently all VPCs has the same number ports, and these ports'
             IP range and mac range will be the same.
            */
            for (String port_ip : ip_mac_map.keySet()) {
                String host_ip = port_ip_to_host_ip_map.get(port_ip);
                String port_id = port_ip_to_id_map.get(port_ip);
                // replace the first 3 digits of the port id with the current vpc_number,
                // if vpc_number == 1 then the first 3 digits of this port_id will be "001"
                String vpc_port_id = String.format("%03d", (vpc_number)) + port_id.substring(3);
                String port_mac = ip_mac_map.get(port_ip).replaceFirst(mac_port_prefix, current_vpc_mac_prefix);
                // if it's on node 1, we don't add neighbor info here,
                // start of setting up port 1 on aca node 1
                Port.PortState.Builder new_port_states = Port.PortState.newBuilder();

                new_port_states.setOperationType(Common.OperationType.CREATE);

                // fill in port state structs for port 1
                Port.PortConfiguration.Builder config = new_port_states.getConfigurationBuilder();
                config.
                        setRevisionNumber(2).
                        setUpdateType(Common.UpdateType.FULL).
                        setId(vpc_port_id).
                        setVpcId(current_vpc_id).
                        setName(("tap" + vpc_port_id).substring(0, 14)).
                        setAdminStateUp(true).
                        setMacAddress(port_mac);
                Port.PortConfiguration.FixedIp.Builder fixedIpBuilder = Port.PortConfiguration.FixedIp.newBuilder();
                fixedIpBuilder.setSubnetId(current_subnet_id);
                fixedIpBuilder.setIpAddress(port_ip);
                config.addFixedIps(fixedIpBuilder.build());
                Port.PortConfiguration.SecurityGroupId securityGroupId = Port.PortConfiguration.SecurityGroupId.newBuilder().setId("2").build();
                config.addSecurityGroupIds(securityGroupId);

                new_port_states.setConfiguration(config.build());

                Port.PortState port_state_one = new_port_states.build();
                Goalstate.ResourceIdType.Builder port_one_resource_Id_builder = Goalstate.ResourceIdType.newBuilder();
                port_one_resource_Id_builder.setType(Common.ResourceType.PORT).setId(port_state_one.getConfiguration().getId());
                Goalstate.ResourceIdType port_one_resource_id = port_one_resource_Id_builder.build();

                // add a new neighbor state with CREATE
                Neighbor.NeighborState.Builder new_neighborState_builder = Neighbor.NeighborState.newBuilder();
                new_neighborState_builder.setOperationType(Common.OperationType.CREATE);

                // fill in neighbor state structs of port 3
                Neighbor.NeighborConfiguration.Builder NeighborConfiguration_builder = Neighbor.NeighborConfiguration.newBuilder();
                NeighborConfiguration_builder.setRevisionNumber(2);
                NeighborConfiguration_builder.setVpcId(current_vpc_id);
                NeighborConfiguration_builder.setId(vpc_port_id + "_n");
                NeighborConfiguration_builder.setMacAddress(port_mac);
                NeighborConfiguration_builder.setHostIpAddress(host_ip);

                Neighbor.NeighborConfiguration.FixedIp.Builder neighbor_fixed_ip_builder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
                neighbor_fixed_ip_builder.setNeighborType(Neighbor.NeighborType.L2);
                neighbor_fixed_ip_builder.setSubnetId(current_subnet_id);
                neighbor_fixed_ip_builder.setIpAddress(port_ip);

                NeighborConfiguration_builder.addFixedIps(neighbor_fixed_ip_builder.build());

                new_neighborState_builder.setConfiguration(NeighborConfiguration_builder.build());
                Neighbor.NeighborState neighborState_node_one = new_neighborState_builder.build();

                if (host_ip.equals(aca_node_one_ip)) {

                    GoalState_builder_one.putPortStates(port_state_one.getConfiguration().getId(), port_state_one);

                    host_resource_builder_node_one.addResources(port_one_resource_id);
                    // if this port is on host_one, then it is a neighbor for ports on host_two

                    GoalState_builder_two.putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
                    Goalstate.ResourceIdType resource_id_type_neighbor_node_one = Goalstate.ResourceIdType.newBuilder().
                            setType(Common.ResourceType.NEIGHBOR).setId(neighborState_node_one.getConfiguration().getId()).build();
                    host_resource_builder_node_two.addResources(resource_id_type_neighbor_node_one);
                } else {
                    GoalState_builder_two.putPortStates(port_state_one.getConfiguration().getId(), port_state_one);

                    host_resource_builder_node_two.addResources(port_one_resource_id);
                    // if this port is on host_two, then it is a neighbor for ports on host_one
                    GoalState_builder_two.putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
                    Goalstate.ResourceIdType resource_id_type_neighbor_node_one = Goalstate.ResourceIdType.newBuilder().
                            setType(Common.ResourceType.NEIGHBOR).setId(neighborState_node_one.getConfiguration().getId()).build();
                    host_resource_builder_node_one_port_one_neighbor.addResources(resource_id_type_neighbor_node_one);
                }
//                System.out.println("Finished port state for port [" + port_ip + "] on host: [" + host_ip + "]");
            }

//        Router.RouterState.Builder router_state_builder = Router.RouterState.newBuilder();
//
//        Router.RouterConfiguration.Builder router_configuration_builder = Router.RouterConfiguration.newBuilder();
//
//        Router.RouterConfiguration.RoutingRule.Builder router_rule_builder = Router.RouterConfiguration.RoutingRule.newBuilder();
//
//        Router.RouterConfiguration.RoutingRuleExtraInfo.Builder routing_rule_extra_info_builder = Router.RouterConfiguration.RoutingRuleExtraInfo.newBuilder();
//
//        routing_rule_extra_info_builder
//                .setDestinationType(Router.DestinationType.VPC_GW)
//                .setNextHopMac("6c:dd:ee:0:0:40");
//
//        router_rule_builder
//                .setId("tc_sample_routing_rule")
//                .setName("tc_sample_routing_rule")
//                .setDestination("10.0.0.0/24")
//                .setNextHopIp(aca_node_one_ip)
//                .setPriority(999)
//                .setRoutingRuleExtraInfo(routing_rule_extra_info_builder.build());
//
//        Router.RouterConfiguration.SubnetRoutingTable.Builder subnet_routing_table_builder = Router.RouterConfiguration.SubnetRoutingTable.newBuilder();
//        subnet_routing_table_builder
//                .setSubnetId(subnet_id_1)
//                .addRoutingRules(router_rule_builder.build());
//
//        Router.RouterConfiguration.SubnetRoutingTable.Builder subnet_routing_table_builder_two = Router.RouterConfiguration.SubnetRoutingTable.newBuilder();
//        subnet_routing_table_builder_two
//                .setSubnetId(subnet_id_2)
//                .addRoutingRules(router_rule_builder.build());
//
//        router_configuration_builder
//                .setRevisionNumber(777)
//                .setRequestId("tc_sample_routing_rule"+"_rs")
//                .setId("tc_sample_routing_rule"+"_r")
//                .setUpdateType(Common.UpdateType.FULL)
//                .setHostDvrMacAddress("6c:dd:ee:0:0:40")
//                .addSubnetRoutingTables(subnet_routing_table_builder.build())
//                .addSubnetRoutingTables(subnet_routing_table_builder_two.build());
//
//        router_state_builder
//                .setOperationType(Common.OperationType.INFO)
//                .setConfiguration(router_configuration_builder.build());
//        Router.RouterState router_state = router_state_builder.build();
//
//        GoalState_builder_two.putRouterStates(router_state.getConfiguration().getId(), router_state);
//        GoalState_builder_one.putRouterStates(router_state.getConfiguration().getId(), router_state);
//        Goalstate.ResourceIdType resource_id_type_router_node_two = Goalstate.ResourceIdType.newBuilder().
//                setType(Common.ResourceType.ROUTER)
//                .setId(router_state.getConfiguration().getId())
//                .build();
//        host_resource_builder_node_two.addResources(resource_id_type_router_node_two);
//        host_resource_builder_node_one.addResources(resource_id_type_router_node_two);
            // fill in subnet state structs
            Subnet.SubnetState.Builder new_subnet_states = Subnet.SubnetState.newBuilder();

            new_subnet_states.setOperationType(Common.OperationType.INFO);

            Subnet.SubnetConfiguration.Builder subnet_configuration_builder = Subnet.SubnetConfiguration.newBuilder();

            subnet_configuration_builder.setRevisionNumber(2);
            subnet_configuration_builder.setVpcId(current_vpc_id);
            subnet_configuration_builder.setId(current_subnet_id);
            subnet_configuration_builder.setCidr("10.0.0.0/24");
            subnet_configuration_builder.setTunnelId(21 + vpc_number -1);
            subnet_configuration_builder.setGateway(Subnet.SubnetConfiguration.Gateway.newBuilder().setIpAddress("0.0.0.0").setMacAddress("6c:dd:ee:0:0:40").build());

            new_subnet_states.setConfiguration(subnet_configuration_builder.build());

            Subnet.SubnetState subnet_state_for_both_nodes = new_subnet_states.build();

            // fill in subnet state structs
//        Subnet.SubnetState.Builder new_subnet_states_two = Subnet.SubnetState.newBuilder();
//
//        new_subnet_states_two.setOperationType(Common.OperationType.INFO);
//
//        Subnet.SubnetConfiguration.Builder subnet_configuration_builder_two = Subnet.SubnetConfiguration.newBuilder();
//
//        subnet_configuration_builder_two.setRevisionNumber(2);
//        subnet_configuration_builder_two.setVpcId(vpc_id_1);
//        subnet_configuration_builder_two.setId(subnet_id_2);
//        subnet_configuration_builder_two.setCidr("10.0.0.0/24");
//        subnet_configuration_builder_two.setTunnelId(22);
//        subnet_configuration_builder_two.setGateway(Subnet.SubnetConfiguration.Gateway.newBuilder().setIpAddress("0.0.0.1").setMacAddress("6c:dd:ee:0:0:41").build());
//
//        new_subnet_states_two.setConfiguration(subnet_configuration_builder_two.build());
//
//        Subnet.SubnetState subnet_state_for_both_nodes_two = new_subnet_states_two.build();

            // put the new subnet state of subnet 1 into the goalstatev2

            // fill in VPC state structs
            Vpc.VpcState.Builder new_vpc_states = Vpc.VpcState.newBuilder();
            new_vpc_states.setOperationType(Common.OperationType.INFO);

            Vpc.VpcConfiguration.Builder vpc_configuration_builder = Vpc.VpcConfiguration.newBuilder();
            vpc_configuration_builder.setCidr("10.0.0.0/16");
            vpc_configuration_builder.setId(current_vpc_id);
            vpc_configuration_builder.setName("test_vpc");
            vpc_configuration_builder.setTunnelId(21 + vpc_number - 1);
            vpc_configuration_builder.setProjectId(project_id);
            vpc_configuration_builder.setRevisionNumber(2);

            new_vpc_states.setConfiguration(vpc_configuration_builder.build());
            Vpc.VpcState vpc_state_for_both_nodes = new_vpc_states.build();

            GoalState_builder_one.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
//        GoalState_builder_one.putSubnetStates(subnet_state_for_both_nodes_two.getConfiguration().getId(), subnet_state_for_both_nodes_two);
            GoalState_builder_two.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
//        GoalState_builder_two.putSubnetStates(subnet_state_for_both_nodes_two.getConfiguration().getId(), subnet_state_for_both_nodes_two);
            GoalState_builder_one.putVpcStates(vpc_state_for_both_nodes.getConfiguration().getId(), vpc_state_for_both_nodes);
            GoalState_builder_two.putVpcStates(vpc_state_for_both_nodes.getConfiguration().getId(), vpc_state_for_both_nodes);

            Goalstate.ResourceIdType subnet_resource_id_type = Goalstate.ResourceIdType.newBuilder()
                    .setType(Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes.getConfiguration().getId()).build();
//        Goalstate.ResourceIdType subnet_resource_id_type_two = Goalstate.ResourceIdType.newBuilder()
//                .setType(Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes_two.getConfiguration().getId()).build();

            Goalstate.ResourceIdType vpc_resource_id_type = Goalstate.ResourceIdType.newBuilder().setType(Common.ResourceType.VPC).setId(vpc_state_for_both_nodes.getConfiguration().getId()).build();
            host_resource_builder_node_one.addResources(subnet_resource_id_type);
            host_resource_builder_node_two.addResources(subnet_resource_id_type);
            host_resource_builder_node_one_port_one_neighbor.addResources(subnet_resource_id_type);
//        host_resource_builder_node_one.addResources(subnet_resource_id_type_two);
//        host_resource_builder_node_two.addResources(subnet_resource_id_type_two);
//        host_resource_builder_node_one_port_one_neighbor.addResources(subnet_resource_id_type_two);
            host_resource_builder_node_one.addResources(vpc_resource_id_type);
            host_resource_builder_node_two.addResources(vpc_resource_id_type);
            host_resource_builder_node_one_port_one_neighbor.addResources(vpc_resource_id_type);
        }


        GoalState_builder_one.putHostResources(aca_node_one_ip, host_resource_builder_node_one.build());
        GoalState_builder_two.putHostResources(aca_node_two_ip, host_resource_builder_node_two.build());
        GoalState_builder_two.putHostResources(aca_node_one_ip, host_resource_builder_node_one_port_one_neighbor.build());
        Goalstate.GoalStateV2 message_one = GoalState_builder_one.build();
        Goalstate.GoalStateV2 message_two = GoalState_builder_two.build();

//        System.out.println("Built GoalState successfully, GoalStateV2 content for PORT1: \n" + message_one.toString() + "\n");
//        System.out.println("Built GoalState successfully, GoalStateV2 content for PORT2: \n" + message_two.toString() + "\n");
        System.out.println("GoalStateV2 size in bytes for host1: \n" + message_one.getSerializedSize() + "\n");
        System.out.println("GoalStateV2 size in bytes for host2: \n" + message_two.getSerializedSize() + "\n");

        System.out.println("Time to call the GRPC functions");
        // Use tracer and interceptor to trace grpc calls.
        Configuration.SamplerConfiguration samplerConfiguration = Configuration.SamplerConfiguration
                .fromEnv()
                .withType(ConstSampler.TYPE)
                .withParam(1);
        Configuration.ReporterConfiguration reporterConfiguration = Configuration.ReporterConfiguration
                .fromEnv()
                .withSender(Configuration.SenderConfiguration.fromEnv().withAgentHost(ncm_ip).withAgentPort(6831))
                .withLogSpans(true);
        Configuration configuration = new Configuration("alcor-test-controller")
                .withSampler(samplerConfiguration)
                .withReporter(reporterConfiguration);

        Tracer tracer = configuration.getTracer();
        System.out.println("[Test Controller] Got this global tracer: "+tracer.toString());

        TracingClientInterceptor tracingClientInterceptor = TracingClientInterceptor
                .newBuilder()
                .withTracer(tracer)
                .withVerbosity()
                .withStreaming()
                .build();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(ncm_ip, ncm_port).usePlaintext().build();
        System.out.println("Constructed channel");
        GoalStateProvisionerGrpc.GoalStateProvisionerStub stub = GoalStateProvisionerGrpc.newStub(tracingClientInterceptor.intercept(channel));
        Span parentSpan = tracer.activeSpan();
        Span span;
        if(parentSpan != null){
            span = tracer.buildSpan("alcor-tc-send-gs").asChildOf(parentSpan.context()).start();
            System.out.println("[Test Controller] Got parent span: "+parentSpan.toString());
        }else{
            span = tracer.buildSpan("alcor-tc-send-gs").start();
        }
        System.out.println("[Test Controller] Built child span: "+span.toString());
        Scope cscope = tracer.scopeManager().activate(span);
        span.log("abcdefg");
        System.out.println("Created stub");
        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> message_observer = new StreamObserver<>() {
            @Override
            public void onNext(Goalstateprovisioner.GoalStateOperationReply value) {
                finished_sending_goalstate_hosts_count ++ ;
                System.out.println("onNext function with this GoalStateOperationReply: \n" + value.toString() + "\n");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError function with this GoalStateOperationReply: \n" + t.getMessage() + "\n");
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }
        };
        System.out.println("Created GoalStateOperationReply observer class");
        io.grpc.stub.StreamObserver<Goalstate.GoalStateV2> response_observer = stub.pushGoalStatesStream(message_observer);
        System.out.println("Connected the observers");
        response_observer.onNext(message_one);
        response_observer.onNext(message_two);

        System.out.println("After calling onNext");
        response_observer.onCompleted();

        System.out.println("Wait no longer than 6000 seconds until both goalstates are sent to both hosts.");
        Awaitility.await().atMost(6000, TimeUnit.SECONDS).until(()-> finished_sending_goalstate_hosts_count == NUMBER_OF_NODES);
        span.finish();
        System.out.println("[Test Controller] Child span after finish: "+span.toString());


//        System.out.println("Try to send gsv1 to the host!");
//
//        ManagedChannel v1_chan_aca_1 = ManagedChannelBuilder.forAddress(aca_node_one_ip, 50001).usePlaintext().build();
//        ManagedChannel v1_chan_aca_2 = ManagedChannelBuilder.forAddress(aca_node_two_ip, 50001).usePlaintext().build();
//
//        GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub v1_stub_aca_1 = GoalStateProvisionerGrpc.newBlockingStub(v1_chan_aca_1);
//        GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub v1_stub_aca_2 = GoalStateProvisionerGrpc.newBlockingStub(v1_chan_aca_2);
//
//
//        //  try to send gsv1 to the host, to see if the server supports gsv1 or not.
//        for(int i = 0 ; i < ports_to_generate_on_each_aca_node ; i ++){
//            System.out.println("Sending the " + i + "th gsv1 to ACA1 at: "+aca_node_one_ip);
//            Goalstateprovisioner.GoalStateOperationReply reply_v1_aca_1 = v1_stub_aca_1.pushNetworkResourceStates(Goalstate.GoalState.getDefaultInstance());
//            System.out.println("Received the " + i + "th reply: " + reply_v1_aca_1.toString()+" from ACA1 at: "+aca_node_one_ip);
//            System.out.println("Sending the " + i + "th gsv1 to ACA2 at: "+aca_node_two_ip);
//            Goalstateprovisioner.GoalStateOperationReply reply_v1_aca_2 = v1_stub_aca_2.pushNetworkResourceStates(Goalstate.GoalState.getDefaultInstance());
//            System.out.println("Received the " + i + "th reply: " + reply_v1_aca_2.toString()+" from ACA1 at: "+aca_node_two_ip);
//        }
//        System.out.println("Done sending gsv1 to the host!");

        System.out.println("After the GRPC call, it's time to do the ping test");

        System.out.println("Sleep 10 seconds before executing the ping");
        try {
            TimeUnit.SECONDS.sleep(10);

        } catch (Exception e) {
            System.out.println("I can't sleep!!!!");

        }
        List<concurrent_run_cmd> concurrent_ping_cmds = new ArrayList<>();

        for (int i = 0; i < node_two_port_ips.size(); i++) {
            String pinger_ip = node_one_port_ips.get(i % node_one_port_ips.size());
            String pinger_container_name = port_ip_to_container_name.get(pinger_ip);
//            String pingee_ip = node_two_port_ips.get(i);
            String pingee_ip = node_two_port_ips.get(i);
            String ping_cmd = "docker exec " + pinger_container_name + " ping -I " + pinger_ip + " -c1 " + pingee_ip;
            concurrent_ping_cmds.add(new concurrent_run_cmd(ping_cmd, aca_node_one_ip, user_name, password));
//            System.out.println("Ping command is added: [" + ping_cmd + "]");
        }

        System.out.println("Time to execute these ping commands concurrently");

        if(whether_to_create_containers_and_ping == CREATE_CONTAINER_AND_PING){
            // Execute the pings.
            for (concurrent_run_cmd cmd : concurrent_ping_cmds) {
                if (user_chosen_ping_method == CONCURRENT_PING_MODE) {
                    //concurrent
                    Thread t = new Thread(cmd);
                    t.start();
                } else {
                    // sequential
                    cmd.run();
                }
            }
        }

        System.out.println("End of the test controller");
        channel.shutdown();
        try {
            TimeUnit.SECONDS.sleep(10);

        } catch (Exception e) {
            System.out.println("I can't sleep!!!!");

        }
    }

    private JSONObject call_post_api_with_json(String url, JSONObject parameter){
//        System.out.println("Calling URL: " + url);
        JSONObject response_json = null;
        HttpClient c = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        String result = "";
        try {
            StringEntity s = new StringEntity(parameter.toString(), "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(s);
            HttpResponse httpResponse = c.execute(post);

            // Get the input stream
            HttpEntity response_entity = httpResponse.getEntity();

            String json_string = EntityUtils.toString(response_entity);

            response_json = (JSONObject) new JSONParser().parse(json_string);

        }catch (Exception e){
            e.printStackTrace();
        }
        return response_json;
    }

    private void run_test_against_alcor_apis(){
        System.out.println("Beginning of alcor API test, need to generate: "
                + tenant_amount + " tenants, \n"
                + project_amount_per_tenant + " projects for each tenant, \n"
                + vpc_amount_per_project + " VPCs for each project, \n"
                + subnet_amount_per_vpc + " subnets for each VPC, \n"
                + port_amount_per_subnet + " ports for each subnet.");
        ArrayList<String> tenant_uuids = new ArrayList<>();
        SortedMap<String, ArrayList<String>> tenant_projects = new TreeMap<>();
        SortedMap<String, ArrayList<JSONObject>> project_vpcs = new TreeMap<>();
        SortedMap<String, ArrayList<JSONObject>> vpc_subnets = new TreeMap<>();
        SortedMap<String, ArrayList<JSONObject>> subnet_ports = new TreeMap<>();

        ArrayList<String> vpc_port_ips = null;
        int subnet_port_amount = 1;
        for (int i = 0 ; i < tenant_amount ; i ++){
            String current_tenant_uuid = UUID.randomUUID().toString();
            tenant_uuids.add(current_tenant_uuid);
            ArrayList<String> current_tenant_projects = new ArrayList<>();
            for (int j = 0 ; j < project_amount_per_tenant ; j++){
                String current_project_id = UUID.randomUUID().toString();
                current_tenant_projects.add(current_project_id);
                ArrayList<JSONObject> vpcs_inside_a_project = new ArrayList<>();
                for (int k = 0 ; k < vpc_amount_per_project ; k ++){
                    /*
                        If you set it to /8 you will get a out-of-memory error.
                        /12 gives you more than 2 ^ 20 ports in a VPC, which is
                        1,048,576, without causing the out-of-memory error.
                    */
                    String vpc_cidr = "10.0.0.0/" + vpc_cidr_slash;
                    JSONObject vpc_payload = new JSONObject();
                    JSONObject network = new JSONObject();
                    String current_vpc_id = UUID.randomUUID().toString();
                    network.put("admin_state_up", true);
                    network.put("revision_number", 0);
                    network.put("cidr", vpc_cidr);
                    network.put("default", true);
                    network.put("description", "vpc-"+k);
                    network.put("dns_domain", "test-dns-domain");
                    network.put("id", current_vpc_id);
                    network.put("is_default", true);
                    network.put("mtu", 1400);
                    network.put("name", "vpc-"+k);
                    network.put("port_security_enabled", true);
                    network.put("project_id", current_project_id);
                    vpc_payload.put("network", network);
                    vpcs_inside_a_project.add(vpc_payload);

                    /*
                        1. Generate all port IPs from VPC CIDR range.
                        2. If port_amount_per_subnet > len(port_ips), port_amount_per_subnet = len(port_ips)
                        3. Divide port IPs into groups based on subnet_amount_per_vpc;
                        4. Each group is a subnet, calculate subnet CIDR and form its subnet payload and ports payload
                    */
                    if (null == vpc_port_ips){
                        try {
                            System.out.println("Need to generate port IPs for the first time.");
                            IPAddressString whole_vpc_address = new IPAddressString(vpc_cidr);
                            IPAddressSeqRange whole_vpc_address_range = whole_vpc_address.toSequentialRange();
                            Iterator<IPAddress> range_iterator = (Iterator<IPAddress>) whole_vpc_address_range.stream().iterator();

                            vpc_port_ips = new ArrayList<>();
                            while (range_iterator.hasNext()){
                                vpc_port_ips.add(range_iterator.next().toString());
                            }
                            subnet_port_amount = (vpc_port_ips.size() / subnet_amount_per_vpc);
                            System.out.println("Finished generating port IPs. Each subnet should have " + subnet_port_amount + " ports");
                        } catch (AddressStringException e) {
                            e.printStackTrace();
                        }
                    }
                    /*
                        Create subnet payload based on vpc payload
                    */
                    if (test_subnet_api){
                        ArrayList<JSONObject> current_vpc_subnets = new ArrayList<>();
                        System.out.println("Generating subnets");
                        for (int l = 0 ; l < subnet_amount_per_vpc ; l ++){
                            String subnet_start_ip = vpc_port_ips.get((l * subnet_port_amount) + 0);
                            String subnet_end_ip = vpc_port_ips.get((l * subnet_port_amount) + subnet_port_amount - 1);
                            IPAddressString subnet_start_ip_address_string = new IPAddressString(subnet_start_ip);
                            IPAddressString subnet_end_ip_address_string = new IPAddressString(subnet_end_ip);
                            IPAddress subnet_start_ip_address = subnet_start_ip_address_string.getAddress();
                            IPAddress subnet_end_ip_address = subnet_end_ip_address_string.getAddress();
                            IPAddressSeqRange subnet_range = subnet_start_ip_address.toSequentialRange(subnet_end_ip_address);
                            IPAddress blocks[] = subnet_range.spanWithPrefixBlocks();
                            String subnet_cidr = blocks[0].toString();
                            System.out.println("Subnet cidr = " + subnet_cidr);
                            String current_subnet_id = UUID.randomUUID().toString();
                            JSONObject subnet_payload = new JSONObject();
                            JSONObject subnet = new JSONObject();
                            subnet.put("cidr", subnet_cidr);
                            subnet.put("id", current_subnet_id);
                            subnet.put("ip_version", 4);
                            subnet.put("network_id", current_vpc_id);
                            subnet.put("name", "subnet"+l);
                            subnet_payload.put("subnet", subnet);
                            current_vpc_subnets.add(subnet_payload);
                            if (test_port_api){
                                List<String> subnet_port_ips = vpc_port_ips.subList((l * subnet_port_amount) + 0, (l * subnet_port_amount) + subnet_port_amount);
                                ArrayList<JSONObject> current_subnet_ports = new ArrayList<>();
//                                System.out.println("Generating ports for current subnet, it has " + subnet_port_ips.size() + " ports");
                                for(String port_ip_in_subnet : subnet_port_ips){
                                    JSONObject port_payload = new JSONObject();
                                    JSONObject port = new JSONObject();
                                    port.put("admin_state_up", true);
                                    port.put("description", "test_port");
                                    port.put("device_id", "test_device_id");
                                    port.put("device_owner", "compute:nova");
                                    port.put("fast_path", true);
                                    JSONArray fixed_ips = new JSONArray();
                                    JSONObject subnet_fixed_ip = new JSONObject();
                                    subnet_fixed_ip.put("ip_address", port_ip_in_subnet);
                                    subnet_fixed_ip.put("subnet_id", current_subnet_id);
                                    fixed_ips.add(subnet_fixed_ip);
                                    port.put("fixed_ips", fixed_ips);
                                    port.put("id", UUID.randomUUID().toString());
                                    port.put("mac_learning_enabled", true);
                                    port.put("network_id", current_vpc_id);
                                    port.put("securi_enabled", true);
                                    port.put("project_id", current_project_id);
                                    port.put("revision_number", 0);
                                    port.put("tenant_id", current_tenant_uuid);
                                    port.put("uplink_status_propagation", true);

                                    port_payload.put("port", port);
                                    current_subnet_ports.add(port_payload);
                                }
//                                System.out.println("Finished generating ports for subnet.");
                                subnet_ports.put(current_subnet_id, current_subnet_ports);
                            }

                        }
                        System.out.println("Finished generating subnets for vpc.");
                        vpc_subnets.put(current_vpc_id, current_vpc_subnets);
                    }

                }
                project_vpcs.put(current_project_id, vpcs_inside_a_project);
            }
            tenant_projects.put(current_tenant_uuid, current_tenant_projects);
        }

        System.out.println("Created JSON payloads for " + tenant_uuids.size() + " tenants, \neach tenant has "
                + tenant_projects.get(tenant_projects.firstKey()).size() + " projects, \neach project has "
                + project_vpcs.get(project_vpcs.firstKey()).size() + " vpcs, \neach vpc has "
                + (test_subnet_api ? vpc_subnets.get(vpc_subnets.firstKey()).size() : 0) + " subnets, \neach subnet has "
                + (test_port_api ? subnet_ports.get(subnet_ports.firstKey()).size() : 0) + " ports.");

        System.out.println("Time to call those APIs! Calling APIs at " + call_api_rate + "/second");
        // Maximum 100 API calls per second.
        RateLimiter rateLimiter = RateLimiter.create(call_api_rate);
        // Create a thread pool that has the same amount of threads as the rateLimiter
        ExecutorService concurrent_create_resource_thread_pool = Executors.newFixedThreadPool(call_api_rate);

        if (test_vpc_api){
            System.out.println("Time to test VPC API!");
            ArrayList<JSONObject> create_vpc_jobs = new ArrayList<>();
            for (String project_id : project_vpcs.keySet()) {
                create_vpc_jobs.addAll(project_vpcs.get(project_id));
            }

            int vpc_call_amount = create_vpc_jobs.size();
            CountDownLatch latch = new CountDownLatch(vpc_call_amount);
            int latch_wait_seconds = (vpc_call_amount / call_api_rate) + 1;
            System.out.println("This VPC test will call createVPC API " + vpc_call_amount +
                    " times, at the rate of " + call_api_rate + "/second, it will wait at most "
                    + latch_wait_seconds + " seconds");
            AtomicInteger create_vpc_success_count = new AtomicInteger(0);
            long call_vpc_api_start_time = System.currentTimeMillis();
            for (JSONObject vpc_job : create_vpc_jobs) {
                rateLimiter.acquire();
                String current_project_id = (String)((JSONObject)vpc_job.get("network")).get("project_id");
                String create_vpc_url = "http://" + vpm_ip + ":" + vpm_port + "/project/" + current_project_id + "/vpcs";
                concurrent_create_resource_thread_pool.execute(() -> {
                    JSONObject create_vpc_response = call_post_api_with_json(create_vpc_url, vpc_job);
                    if (null != create_vpc_response && create_vpc_response.containsKey("network")){
//                                System.out.println("Created VPC successfully");
                        create_vpc_success_count.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            try {
                if (test_subnet_api || test_port_api){
                    /*
                        If we are testing subnet API or port API, we need to wait until the VPC is created.
                    */
                    latch.await(600, TimeUnit.SECONDS);
                }else{
                    /* we actually don't need to wait latch_wait_seconds
                        because if we start the wait after the last call, we should actually wait for the last call.
                        So we will be waiting only 1 second at most.
                    */
                    latch.await(/*latch_wait_seconds*/ 1, TimeUnit.SECONDS);
                }
                long call_vpc_api_end_time = System.currentTimeMillis();
                System.out.println("Total amount of calling createVPC API " + vpc_call_amount +
                        " times, finished "+ ( vpc_call_amount - latch.getCount())
                        + " times, succeeded " + create_vpc_success_count.get() + " times, at the rate of "
                        + call_api_rate + "/second, it took "
                        + (call_vpc_api_end_time - call_vpc_api_start_time) + " milliseconds");
            } catch (InterruptedException e) {
                System.err.println("Waited 60 seconds but can't get VPC response!");
                e.printStackTrace();
            }
        }

        if (test_subnet_api){
            System.out.println("Time to test subnet API!");
            ArrayList<JSONObject> create_subnet_jobs = new ArrayList<>();
            for (String vpc_id : vpc_subnets.keySet()) {
                create_subnet_jobs.addAll(vpc_subnets.get(vpc_id));
            }
            CountDownLatch latch = new CountDownLatch(create_subnet_jobs.size());
            int subnet_call_amount = create_subnet_jobs.size();
            int latch_wait_seconds = (subnet_call_amount / call_api_rate) + 1;
            System.out.println("This subnet test will call createSubnet API " + subnet_call_amount +
                    " times, at the rate of " + call_api_rate + "/second, it will wait at most "
                    + latch_wait_seconds + " seconds");
            AtomicInteger create_subnet_success_count = new AtomicInteger(0);
            long call_subnet_api_start_time = System.currentTimeMillis();
            for (JSONObject subnet_job : create_subnet_jobs) {
                rateLimiter.acquire();
                String current_project_id = (String)((JSONObject)subnet_job.get("subnet")).get("project_id");
                String create_subnet_url = "http://" + snm_ip + ":" + snm_port + "/project/" + current_project_id + "/subnets";
                concurrent_create_resource_thread_pool.execute(() -> {
                    JSONObject create_vpc_response = call_post_api_with_json(create_subnet_url, subnet_job);
                    if (null != create_vpc_response && create_vpc_response.containsKey("subnet")){
//                                System.out.println("Created VPC successfully");
                        create_subnet_success_count.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            try {
                if (test_port_api){
                    /*
                        If we are testing port API, we need to wait until the VPC is created.
                    */
                    latch.await(600, TimeUnit.SECONDS);
                }else{
                    /* we actually don't need to wait latch_wait_seconds
                        because if we start the wait after the last call, we should actually wait for the last call.
                        So we will be waiting only 1 second at most.
                    */
                    latch.await(/*latch_wait_seconds*/ 1, TimeUnit.SECONDS);
                }
                long call_subnet_api_end_time = System.currentTimeMillis();
                System.out.println("Total amount of calling createSubnet API " + subnet_call_amount +
                        " times, finished "+ ( subnet_call_amount - latch.getCount())
                        + " times, succeeded " + create_subnet_success_count.get() + " times, at the rate of "
                        + call_api_rate + "/second, it took "
                        + (call_subnet_api_end_time - call_subnet_api_start_time) + " milliseconds");
            } catch (InterruptedException e) {
                System.err.println("Waited 60 seconds but can't get subnet response!");
                e.printStackTrace();
            }
        }

        if (test_port_api){
            System.out.println("Time to test port API!");
            ArrayList<JSONObject> create_port_jobs = new ArrayList<>();
            for (String subnet_id : subnet_ports.keySet()) {
                ArrayList<JSONObject> current_subnet_ports = subnet_ports.get(subnet_id);
                //remove the first and last IP, maybe those are reserved.
                current_subnet_ports.remove(current_subnet_ports.size() - 1);
                current_subnet_ports.remove(0);
                create_port_jobs.addAll(current_subnet_ports);
            }
            CountDownLatch latch = new CountDownLatch(create_port_jobs.size());
            int port_call_amount = create_port_jobs.size();
            int latch_wait_seconds = (port_call_amount / call_api_rate) + 1;
            System.out.println("This port test will call createPort API " + port_call_amount +
                    " times, at the rate of " + call_api_rate + "/second, it will wait at most "
                    + latch_wait_seconds + " seconds");
            AtomicInteger create_port_success_count = new AtomicInteger(0);
            long call_port_api_start_time = System.currentTimeMillis();
            for (JSONObject port_job : create_port_jobs) {
                rateLimiter.acquire();
                String current_project_id = (String)((JSONObject)port_job.get("port")).get("project_id");
                String create_port_url = "http://" + pm_ip + ":" + pm_port + "/project/" + current_project_id + "/ports";
                concurrent_create_resource_thread_pool.execute(() -> {
                    JSONObject create_vpc_response = call_post_api_with_json(create_port_url, port_job);
                    if (null != create_vpc_response && create_vpc_response.containsKey("port")){
//                                System.out.println("Created VPC successfully");
                        create_port_success_count.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            try {
                /* we actually don't need to wait latch_wait_seconds
                    because if we start the wait after the last call, we should actually wait for the last call.
                    So we will be waiting only 1 second at most.
                */
                latch.await(/*latch_wait_seconds*/ 1, TimeUnit.SECONDS);
                long call_port_api_end_time = System.currentTimeMillis();
                System.out.println("Total amount of calling createPort API " + port_call_amount +
                        " times, finished "+ ( port_call_amount - latch.getCount())
                        + " times, succeeded" + create_port_success_count.get() + " times, at the rate of "
                        + call_api_rate + "/second, it took "
                        + (call_port_api_end_time - call_port_api_start_time) + " milliseconds");
            } catch (InterruptedException e) {
                System.err.println("Waited 60 seconds but can't get port response!");
                e.printStackTrace();
            }
        }
        /*
        System.out.println("Try to call API to create a VPC!");
        JSONObject example_vpc_payload = new JSONObject();
        String project_id = "12345";
        String vpc_id = "54321";
        Boolean admin_state_up = true;
        int revision_number = 0;
        String cidr = "10.0.0.0/16";
        Boolean network_default = true;
        String description = "test_description";
        String dns_domain = "test_dns_domain";
        Boolean is_default = true;
        int mtu = 1400;
        String name = "test_vpc";
        Boolean port_security_enabled = true;

        JSONObject network = new JSONObject();
        network.put("admin_state_up", admin_state_up);
        network.put("revision_number", revision_number);
        network.put("cidr", cidr);
        network.put("default", network_default);
        network.put("description", description);
        network.put("dns_domain", dns_domain);
        network.put("id", vpc_id);
        network.put("is_default", is_default);
        network.put("mtu", mtu);
        network.put("name", name);
        network.put("port_security_enabled", port_security_enabled);
        network.put("project_id", project_id);
        example_vpc_payload.put("network", network);

        String create_vpc_url = "http://" + vpm_ip + ":" + vpm_port + "/project/" + project_id + "/vpcs";
        JSONObject create_vpc_response = call_post_api_with_json(create_vpc_url, example_vpc_payload);
        if (null != create_vpc_response){
            System.out.println("Create VPC response: \n" + create_vpc_response);
        }

        String subnet_id = "112233";
        String subnet_cidr = "10.0.1.0/24";
        int subnet_ip_version = 4;
        String network_id = vpc_id;
        String subnet_name = "test_subnet";

        JSONObject example_subnet_payload = new JSONObject();
        JSONObject subnet = new JSONObject();
        subnet.put("cidr", subnet_cidr);
        subnet.put("id", subnet_id);
        subnet.put("ip_version", subnet_ip_version);
        subnet.put("network_id", network_id);
        subnet.put("name", subnet_name);

        example_subnet_payload.put("subnet", subnet);
        String create_subnet_url = "http://" + snm_ip + ":" + snm_port + "/project/" + project_id + "/subnets";
        JSONObject create_subnet_response = call_post_api_with_json(create_subnet_url, example_subnet_payload);
        String subnet_start_ip = null;
        String subnet_end_ip = null;
        JSONObject subnet_content = null;
        JSONArray subnet_allocation_pools = null;
        ArrayList<String> port_ips = new ArrayList<String>();
        if (null != create_subnet_response){
            System.out.println("Create subnet response: \n" + create_subnet_response);
            subnet_content = (JSONObject) create_subnet_response.get("subnet");
            subnet_allocation_pools = (JSONArray) subnet_content.get("allocation_pools");
            JSONObject subnet_allocations_pool_first_element = (JSONObject) subnet_allocation_pools.get(0);
            subnet_start_ip = (String) subnet_allocations_pool_first_element.get("start");
            subnet_end_ip = (String) subnet_allocations_pool_first_element.get("end");
            IPAddressString start = new IPAddressString(subnet_start_ip);
            IPAddressString end = new IPAddressString(subnet_end_ip);
            IPAddress start_addr = start.getAddress();
            IPAddress end_addr = end.getAddress();
            IPAddressSeqRange range = start_addr.toSequentialRange(end_addr);
            Iterator<IPAddress> range_iterator = (Iterator<IPAddress>) range.stream().iterator();
            while (range_iterator.hasNext()){
                IPAddress port_ip_address = range_iterator.next();
                System.out.println("Current port IP: " + port_ip_address.toString());
                port_ips.add(port_ip_address.toString());
            }
            ArrayList<JSONObject> ports_json_objects = new ArrayList<>();
            for (String port_ip_address : port_ips){
                JSONObject example_port_payload = new JSONObject();
                JSONObject port = new JSONObject();
                port.put("admin_state_up", admin_state_up);
                port.put("description", "test_port");
                port.put("device_id", "test_device_id");
                port.put("device_owner", "compute:nova");
                port.put("fast_path", true);
                JSONArray fixed_ips = new JSONArray();
                JSONObject subnet_fixed_ip = new JSONObject();
                subnet_fixed_ip.put("ip_address", port_ip_address);
                subnet_fixed_ip.put("subnet_id", subnet_id);
                fixed_ips.add(subnet_fixed_ip);
                port.put("fixed_ips", fixed_ips);
                port.put("id", UUID.randomUUID().toString());
                port.put("mac_learning_enabled", true);
                port.put("network_id", vpc_id);
                port.put("securi_enabled", true);
                port.put("project_id", project_id);
                port.put("revision_number", 0);
                port.put("tenant_id", project_id);
                port.put("uplink_status_propagation", true);

                example_port_payload.put("port", port);
                ports_json_objects.add(example_port_payload);
            }
            String create_port_url = "http://" + pm_ip + ":"+ pm_port + "/project/" + project_id + "/ports";
            if(ports_json_objects.size() > 0 ){
                JSONObject create_port_response = call_post_api_with_json(create_port_url, ports_json_objects.get(0));
                if (null != create_port_response){
                    System.out.println("Create Port response: \n" + create_port_response);
                }
            }
        }
        */
    }


    private void create_containers_on_both_hosts_concurrently() {
        System.out.println("Creating containers on both hosts, ip_mac_map has " + ip_mac_map.keySet().size() + "keys");
        int i = 1;
        String background_pinger = "";
        String background_pingee = "";
        // use a countdown latch to wait for the threads to finish.
        CountDownLatch latch = new CountDownLatch(ip_mac_map.keySet().size());

        for (String port_ip : ip_mac_map.keySet()) {
            String port_mac = ip_mac_map.get(port_ip);
            String container_name = "test" + Integer.toString(i);
            port_ip_to_container_name.put(port_ip, container_name);
            String create_container_cmd = "docker run -itd --name " + container_name + " --net=none --label test=ACA busybox sh";
            String ovs_docker_add_port_cmd = "ovs-docker add-port br-int eth0 " + container_name + " --ipaddress=" + port_ip + "/16 --macaddress=" + port_mac;
            String ovs_set_vlan_cmd = "ovs-docker set-vlan br-int eth0 " + container_name + " 1";
            Vector<String> create_one_container_and_assign_IP_vlax_commands = new Vector<>();
            create_one_container_and_assign_IP_vlax_commands.add(create_container_cmd);
            create_one_container_and_assign_IP_vlax_commands.add(ovs_docker_add_port_cmd);
            create_one_container_and_assign_IP_vlax_commands.add(ovs_set_vlan_cmd);

//            int ip_last_octet = Integer.parseInt(port_ip.split("\\.")[3]);
            if (node_one_port_ips.size() != ports_to_generate_on_aca_node_one) {
//                System.out.println("i = " + i + " , assigning IP: [" + port_ip + "] to node: [" + aca_node_one_ip + "]");
                node_one_port_ips.add(port_ip);
                port_ip_to_host_ip_map.put(port_ip, aca_node_one_ip);
                if(whether_to_create_containers_and_ping == CREATE_CONTAINER_AND_PING){
                    concurrent_create_containers_thread_pool.execute(() -> {
                        execute_ssh_commands(create_one_container_and_assign_IP_vlax_commands, aca_node_one_ip, user_name, password);
                        latch.countDown();
                    });
                }
                background_pinger = port_ip;
            } else {
//                System.out.println("i = " + i + " , assigning IP: [" + port_ip + "] to node: [" + aca_node_two_ip + "]");
                node_two_port_ips.add(port_ip);
                port_ip_to_host_ip_map.put(port_ip, aca_node_two_ip);
                if(whether_to_create_containers_and_ping == CREATE_CONTAINER_AND_PING){
                    concurrent_create_containers_thread_pool.execute(() -> {
                        execute_ssh_commands(create_one_container_and_assign_IP_vlax_commands, aca_node_two_ip, user_name, password);
                        latch.countDown();
                    });
                }
                background_pingee = port_ip;
            }
            i++;
        }

        if(whether_to_create_containers_and_ping == CREATE_CONTAINER_AND_PING){
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (user_chosen_execute_background_ping == DO_EXECUTE_BACKGROUND_PING) {
                // start the background thread here doing the ping from 1 port to another, util the ping is successful.
                // it pings every 0.001 second, or 1 millisecond, for 60 seconds
                String background_ping_command = "docker exec " + port_ip_to_container_name.get(background_pinger) + " ping -I " + background_pinger + " -c 60000 -i  0.001 " + background_pingee + " > /home/user/background_ping_output.log";
                System.out.println("Created background ping cmd: " + background_ping_command);
                concurrent_run_cmd c = new concurrent_run_cmd(background_ping_command, aca_node_one_ip, user_name, password);
                backgroundPingExecutor.execute(c);
            }
        }

        System.out.println("DONE creating containers on both hosts, host 1 has "+node_one_port_ips.size()+" ports, host 2 has "+node_two_port_ips.size()+" ports");
    }


    // Generates IP/MAC for host_many_per_host, and inserts them into the hashmap
    public static void generate_ip_macs(int amount_of_ports_to_generate) {
        System.out.println("Need to generate " + amount_of_ports_to_generate + " ports");
        int i = 2;
        while (ip_mac_map.size() != amount_of_ports_to_generate) {
            if (i % 100 != 0) {
                String ip_2nd_octet = Integer.toString(i / 10000);
                String ip_3nd_octet = Integer.toString((i % 10000) / 100);
                String ip_4nd_octet = Integer.toString(i % 100);
                String ip_for_port = ips_ports_ip_prefix + "." + ip_2nd_octet + "." + ip_3nd_octet + "." + ip_4nd_octet;
                String mac_for_port = mac_port_prefix + ip_2nd_octet + ":" + ip_3nd_octet + ":" + ip_4nd_octet;
                String id_for_port = port_ip_template + ips_ports_ip_prefix + String.format("%03d", (i / 10000)) + String.format("%03d", ((i % 10000) / 100)) + String.format("%03d", (i % 100));
//                System.out.println("Generated Port " + i + " with IP: [" + ip_for_port + "], ID :[ " + id_for_port + "] and MAC: [" + mac_for_port + "]");
                ip_mac_map.put(ip_for_port, mac_for_port);
                port_ip_to_id_map.put(ip_for_port, id_for_port);
            }
            i++;
        }
        System.out.println("Finished generating " + amount_of_ports_to_generate + " ports, ip->mac map has "+ ip_mac_map.size() +" entries, ip->id map has "+port_ip_to_id_map.size()+" entries");
    }


    public static void execute_ssh_commands(Vector<String> commands, String host_ip, String host_user_name, String host_password) {
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session = jsch.getSession(host_user_name, host_ip, 22);
            session.setPassword(host_password);
            session.setConfig(config);
            session.connect();
            System.out.println("Connected");
            for (int j = 0; j < commands.size(); j++) {
                String command = commands.get(j);
                System.out.println("Start of executing command [" + command + "] on host: " + host_ip);
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream in = channel.getInputStream();
                channel.connect();
                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        System.out.print(new String(tmp, 0, i));
                    }
                    if (channel.isClosed()) {
                        System.out.println("exit-status: " + channel.getExitStatus());
                        break;
                    }
                }
                System.out.println("End of executing command [" + command + "] on host: " + host_ip);
                channel.disconnect();
            }

            session.disconnect();
            System.out.println("DONE");
        } catch (Exception e) {
            System.err.println("Got this error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void executeBashCommand(String command) {
//        boolean success = false;
        System.out.println("Executing BASH command:\n   " + command);
        Runtime r = Runtime.getRuntime();
        // Use bash -c so we can handle things like multi commands separated by ; and
        // things like quotes, $, |, and \. My tests show that command comes as
        // one argument to bash, so we do not need to quote it to make it one thing.
        // Also, exec may object if it does not have an executable file as the first thing,
        // so having bash here makes it happy provided bash is installed and in path.
        String[] commands = {"bash", "-x", "-c", command};
        try {
            Process p = r.exec(commands);

            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";

            while ((line = b.readLine()) != null) {
                System.out.println(line);
            }

            b.close();
//            success = true;
        } catch (Exception e) {
            System.err.println("Failed to execute bash with command: " + command);
            e.printStackTrace();
        }
        //        return success;
    }
}

class concurrent_run_cmd implements Runnable {
    String command_to_run, host, user_name, password;

    @Override
    public void run() {
        Vector<String> cmd_list = new Vector<>();
        System.out.println("Need to execute this command concurrently: [" + this.command_to_run + "]");
        cmd_list.add(this.command_to_run);
//        pseudo_controller.execute_ssh_commands(cmd_list, host, user_name, password);
        pseudo_controller.executeBashCommand(command_to_run);
    }

    public concurrent_run_cmd(String cmd, String host, String user_name, String password) {
        this.command_to_run = cmd;
        this.host = host;
        this.user_name = user_name;
        this.password = password;
    }

}