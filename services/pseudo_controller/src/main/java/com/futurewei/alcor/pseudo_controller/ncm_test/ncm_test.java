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
This is the code testing the intereactions between the Network Configuration manager and
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

package com.futurewei.alcor.pseudo_controller.ncm_test;

import com.futurewei.alcor.schema.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
//import io.opentracing.Scope;
//import io.opentracing.Span;
//import io.opentracing.Tracer;
//import io.opentracing.contrib.grpc.TracingClientInterceptor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.awaitility.Awaitility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.futurewei.alcor.pseudo_controller.alcor_http_api_test.alcor_http_api_test;

@Component
public class ncm_test {
    @Value("${node_one_ip:ip_one}")
    String aca_node_one_ip;
    @Value("${node_one_mac:mac_one}")
    String aca_node_one_mac;
    @Value("${node_two_ip:ip_two}")
    String aca_node_two_ip;
    @Value("${node_two_mac:mac_two}")
    String aca_node_two_mac;
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

    @Value("${test_against_arion:false}")
    Boolean test_against_aroin;
    @Value("${arion_wing_ips:[]}")
    String[] arion_wing_ips;
    @Value("${arion_wing_macs:[]}")
    String[] arion_wing_macs;
    @Value("${arion_master_ip:arion_master_ip}")
    String arion_master_ip;
    @Value("${arion_rest_port:456}")
    int arion_master_rest_port;
    @Value("${arion_grpc_port:456}")
    int arion_master_grpc_port;
    @Value("${arion_dp_controller_ip:arion_dp_controller_ip}")
    String arion_dp_controller_ip;
    JSONArray arion_gw_ip_macs;

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
    static int arion_port_inbound_operation = 8300;
    static final int DEFAULT_VLAN_ID = 1;
    static JSONObject arion_data_json_object;

    public ncm_test(){
        System.out.println("Start of NCM Test!");
    }

    public void run_test_against_ncm(){
        System.out.println("Test against Arion: " + test_against_aroin + " , Arion Wing IPs: " + Arrays.toString(arion_wing_ips) + ", Arion Wing Macs: "+ Arrays.toString(arion_wing_macs) +
                ", ArionMaster IP: " + arion_master_ip + ", Arion Master REST port: " + arion_master_rest_port + ", Arion Master gRPC port: " + arion_master_grpc_port + ", Arion DP Controller IP: " + arion_dp_controller_ip);
        if (arion_wing_ips.length != arion_wing_macs.length) {
            System.out.println("There are " + arion_wing_ips.length + " Wing IPs but there are "+ arion_wing_macs.length + "Wing MACs and the number's don't match. Please check your application.properties. Aborting.");
            return;
        }
        String arion_master_restful_url = arion_master_ip+":"+arion_master_rest_port;
        if(test_against_aroin){
            InputStream is = com.futurewei.alcor.pseudo_controller.pseudo_controller.class.getResourceAsStream("/arion_data.json");
            JSONParser jsonParser = new JSONParser();
            try {
                arion_data_json_object = (JSONObject)jsonParser.parse(new InputStreamReader(is, "UTF-8"));
            }catch (IOException | NullPointerException | ParseException e ){
                System.out.println("Unable to read from json data file: " + e.getMessage() + "\nAborting...");
                return;
            }
            arion_gw_ip_macs = (JSONArray) arion_data_json_object.get("gws");
            setup_arion_gateway_cluster_and_nodes();
        }
        System.out.println("There are "+ number_of_vpcs+" VPCs, ACA node one has "+ ports_to_generate_on_aca_node_one + " ports per VPC;\nACA node two has "+ports_to_generate_on_aca_node_two+" ports per VPC. \nTotal ports per VPC: "+(ports_to_generate_on_aca_node_one + ports_to_generate_on_aca_node_two));
        generate_ip_macs(ports_to_generate_on_aca_node_one + ports_to_generate_on_aca_node_two);
        create_containers_on_both_hosts_concurrently();
        System.out.println("aca_node_one_ip: " + aca_node_one_ip + "\naca_node_two_ip: " + aca_node_two_ip + "\nuser name: " + user_name + "\npassword: " + password);

        System.out.println("Containers setup done, now we gotta construct the GoalStateV2");

        System.out.println("Trying to build the GoalStateV2 for " + (ports_to_generate_on_aca_node_one + ports_to_generate_on_aca_node_two) + " Ports");


        com.futurewei.alcor.schema.Goalstate.GoalStateV2.Builder GoalState_builder_one = com.futurewei.alcor.schema.Goalstate.GoalStateV2.newBuilder();
        com.futurewei.alcor.schema.Goalstate.GoalStateV2.Builder GoalState_builder_two = com.futurewei.alcor.schema.Goalstate.GoalStateV2.newBuilder();
        com.futurewei.alcor.schema.Goalstate.HostResources.Builder host_resource_builder_node_one = com.futurewei.alcor.schema.Goalstate.HostResources.newBuilder();
        com.futurewei.alcor.schema.Goalstate.HostResources.Builder host_resource_builder_node_two = com.futurewei.alcor.schema.Goalstate.HostResources.newBuilder();
        com.futurewei.alcor.schema.Goalstate.HostResources.Builder host_resource_builder_node_one_port_one_neighbor = com.futurewei.alcor.schema.Goalstate.HostResources.newBuilder();

        // a new Goalstate and its builder for Arion master; should have only neigbhor states.
        com.futurewei.arion.schema.Goalstateprovisioner.RoutingRulesRequest.Builder routing_rule_request_builder = com.futurewei.arion.schema.Goalstateprovisioner.RoutingRulesRequest.newBuilder();
        routing_rule_request_builder.setFormatVersion(1);
        routing_rule_request_builder.setRequestId("arion_routing_rule-" + (System.currentTimeMillis() / 1000L));

        for (int vpc_number = 1 ; vpc_number <= number_of_vpcs ; vpc_number ++){
            // generate VPC ID and Subnet ID for the current VPC
            int current_vpc_tunnel_id = 21 + vpc_number -1;
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
                com.futurewei.alcor.schema.Port.PortState.Builder new_port_states = com.futurewei.alcor.schema.Port.PortState.newBuilder();

                new_port_states.setOperationType(com.futurewei.alcor.schema.Common.OperationType.CREATE);

                // fill in port state structs for port 1
                com.futurewei.alcor.schema.Port.PortConfiguration.Builder config = new_port_states.getConfigurationBuilder();
                config.
                        setRevisionNumber(2).
                        setUpdateType(com.futurewei.alcor.schema.Common.UpdateType.FULL).
                        setId(vpc_port_id).
                        setVpcId(current_vpc_id).
                        setName(("tap" + vpc_port_id).substring(0, 14)).
                        setAdminStateUp(true).
                        setMacAddress(port_mac);
                com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp.Builder fixedIpBuilder = com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp.newBuilder();
                fixedIpBuilder.setSubnetId(current_subnet_id);
                fixedIpBuilder.setIpAddress(port_ip);
                config.addFixedIps(fixedIpBuilder.build());
                com.futurewei.alcor.schema.Port.PortConfiguration.SecurityGroupId securityGroupId = com.futurewei.alcor.schema.Port.PortConfiguration.SecurityGroupId.newBuilder().setId("2").build();
                config.addSecurityGroupIds(securityGroupId);

                new_port_states.setConfiguration(config.build());

                com.futurewei.alcor.schema.Port.PortState port_state_one = new_port_states.build();
                com.futurewei.alcor.schema.Goalstate.ResourceIdType.Builder port_one_resource_Id_builder = com.futurewei.alcor.schema.Goalstate.ResourceIdType.newBuilder();
                port_one_resource_Id_builder.setType(Common.ResourceType.PORT).setId(port_state_one.getConfiguration().getId());
                com.futurewei.alcor.schema.Goalstate.ResourceIdType port_one_resource_id = port_one_resource_Id_builder.build();

                // add a new neighbor state with CREATE
                com.futurewei.alcor.schema.Neighbor.NeighborState.Builder new_neighborState_builder = com.futurewei.alcor.schema.Neighbor.NeighborState.newBuilder();
                new_neighborState_builder.setOperationType(Common.OperationType.CREATE);

                // fill in neighbor state structs of port 3
                com.futurewei.alcor.schema.Neighbor.NeighborConfiguration.Builder NeighborConfiguration_builder = com.futurewei.alcor.schema.Neighbor.NeighborConfiguration.newBuilder();
                NeighborConfiguration_builder.setRevisionNumber(2);
                NeighborConfiguration_builder.setVpcId(current_vpc_id);
                NeighborConfiguration_builder.setId(vpc_port_id + "_n");
                NeighborConfiguration_builder.setMacAddress(port_mac);
                NeighborConfiguration_builder.setHostIpAddress(host_ip);

                com.futurewei.alcor.schema.Neighbor.NeighborConfiguration.FixedIp.Builder neighbor_fixed_ip_builder = com.futurewei.alcor.schema.Neighbor.NeighborConfiguration.FixedIp.newBuilder();
                neighbor_fixed_ip_builder.setNeighborType(Neighbor.NeighborType.L2);
                neighbor_fixed_ip_builder.setSubnetId(current_subnet_id);
                neighbor_fixed_ip_builder.setIpAddress(port_ip);

                NeighborConfiguration_builder.addFixedIps(neighbor_fixed_ip_builder.build());

                new_neighborState_builder.setConfiguration(NeighborConfiguration_builder.build());
                com.futurewei.alcor.schema.Neighbor.NeighborState neighborState_node_one = new_neighborState_builder.build();

                // Need to include the Port state in all scenarios.
                if (host_ip.equals(aca_node_one_ip)) {
                    GoalState_builder_one.putPortStates(port_state_one.getConfiguration().getId(), port_state_one);

                    host_resource_builder_node_one.addResources(port_one_resource_id);
                } else {
                    GoalState_builder_two.putPortStates(port_state_one.getConfiguration().getId(), port_state_one);

                    host_resource_builder_node_two.addResources(port_one_resource_id);
                }
                if(test_against_aroin){
                    // We should put the neighbor states into the goalstate to Arion Master.
                    com.futurewei.arion.schema.Goalstateprovisioner.RoutingRulesRequest.RoutingRule.Builder current_routing_rule_builder = com.futurewei.arion.schema.Goalstateprovisioner.RoutingRulesRequest.RoutingRule.newBuilder();
                    current_routing_rule_builder.setOperationType(com.futurewei.arion.schema.Common.OperationType.CREATE);
                    current_routing_rule_builder.setIp(port_ip);
                    current_routing_rule_builder.setHostip(host_ip);
                    current_routing_rule_builder.setMac(port_mac);
                    current_routing_rule_builder.setTunnelId(current_vpc_tunnel_id);
                    current_routing_rule_builder.setHostmac(host_ip.equals(aca_node_one_ip)? aca_node_one_mac : aca_node_two_mac);
                    routing_rule_request_builder.addRoutingRules(current_routing_rule_builder.build());

                }else{
                    // NOT test against Arion; we should put the neigbhor states into the goalstate to NCM.
                    if (host_ip.equals(aca_node_one_ip)) {
                        // if this port is on host_one, then it is a neighbor for ports on host_two

                        GoalState_builder_two.putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
                        com.futurewei.alcor.schema.Goalstate.ResourceIdType resource_id_type_neighbor_node_one = com.futurewei.alcor.schema.Goalstate.ResourceIdType.newBuilder().
                                setType(com.futurewei.alcor.schema.Common.ResourceType.NEIGHBOR).setId(neighborState_node_one.getConfiguration().getId()).build();
                        host_resource_builder_node_two.addResources(resource_id_type_neighbor_node_one);
                    } else {
                        // if this port is on host_two, then it is a neighbor for ports on host_one
                        GoalState_builder_two.putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
                        com.futurewei.alcor.schema.Goalstate.ResourceIdType resource_id_type_neighbor_node_one = com.futurewei.alcor.schema.Goalstate.ResourceIdType.newBuilder().
                                setType(com.futurewei.alcor.schema.Common.ResourceType.NEIGHBOR).setId(neighborState_node_one.getConfiguration().getId()).build();
                        host_resource_builder_node_one_port_one_neighbor.addResources(resource_id_type_neighbor_node_one);
                    }
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
            com.futurewei.alcor.schema.Subnet.SubnetState.Builder new_subnet_states = com.futurewei.alcor.schema.Subnet.SubnetState.newBuilder();

            new_subnet_states.setOperationType(com.futurewei.alcor.schema.Common.OperationType.INFO);

            com.futurewei.alcor.schema.Subnet.SubnetConfiguration.Builder subnet_configuration_builder = com.futurewei.alcor.schema.Subnet.SubnetConfiguration.newBuilder();

            subnet_configuration_builder.setRevisionNumber(2);
            subnet_configuration_builder.setVpcId(current_vpc_id);
            subnet_configuration_builder.setId(current_subnet_id);
            subnet_configuration_builder.setCidr("10.0.0.0/24");
            subnet_configuration_builder.setTunnelId(current_vpc_tunnel_id);
            subnet_configuration_builder.setGateway(com.futurewei.alcor.schema.Subnet.SubnetConfiguration.Gateway.newBuilder().setIpAddress("0.0.0.0").setMacAddress("6c:dd:ee:0:0:40").build());

            new_subnet_states.setConfiguration(subnet_configuration_builder.build());

            com.futurewei.alcor.schema.Subnet.SubnetState subnet_state_for_both_nodes = new_subnet_states.build();

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
            com.futurewei.alcor.schema.Vpc.VpcState.Builder new_vpc_states = com.futurewei.alcor.schema.Vpc.VpcState.newBuilder();
            new_vpc_states.setOperationType(com.futurewei.alcor.schema.Common.OperationType.INFO);

            com.futurewei.alcor.schema.Vpc.VpcConfiguration.Builder vpc_configuration_builder = com.futurewei.alcor.schema.Vpc.VpcConfiguration.newBuilder();
            vpc_configuration_builder.setCidr("10.0.0.0/16");
            vpc_configuration_builder.setId(current_vpc_id);
            vpc_configuration_builder.setName("test_vpc");
            vpc_configuration_builder.setTunnelId(21 + vpc_number - 1);
            vpc_configuration_builder.setProjectId(project_id);
            vpc_configuration_builder.setRevisionNumber(2);

            // put gateway information in the goalstates.
            if (test_against_aroin){
                com.futurewei.alcor.schema.Gateway.GatewayState.Builder new_gateway_state_builder = com.futurewei.alcor.schema.Gateway.GatewayState.newBuilder();
                new_gateway_state_builder.setOperationType(com.futurewei.alcor.schema.Common.OperationType.CREATE);
                com.futurewei.alcor.schema.Gateway.GatewayConfiguration.Builder gateway_configuration_builder = com.futurewei.alcor.schema.Gateway.GatewayConfiguration.newBuilder();
                String current_gateway_id = "tc-gateway-"+vpc_number;
                vpc_configuration_builder.addGatewayIds(current_gateway_id);
                gateway_configuration_builder.setId(current_gateway_id);
                gateway_configuration_builder.setRequestId("tc-gateway-request-"+vpc_number);
                gateway_configuration_builder.setGatewayType(com.futurewei.alcor.schema.Gateway.GatewayType.ZETA);
                com.futurewei.alcor.schema.Gateway.GatewayConfiguration.zeta.Builder zeta_builder = com.futurewei.alcor.schema.Gateway.GatewayConfiguration.zeta.newBuilder();
                zeta_builder.setPortInbandOperation(arion_port_inbound_operation);
                gateway_configuration_builder.setZetaInfo(zeta_builder.build());
                for(int i = 0 ; i < arion_gw_ip_macs.size() ; i ++){
                    JSONObject current_gw = (JSONObject) arion_gw_ip_macs.get(i);
                    String current_arion_wing_ip = (String) current_gw.get("ip");
                    String current_arion_wing_mac = (String) current_gw.get("mac");
                    com.futurewei.alcor.schema.Gateway.GatewayConfiguration.destination.Builder destination_builder = com.futurewei.alcor.schema.Gateway.GatewayConfiguration.destination.newBuilder();
                    destination_builder.setIpAddress(current_arion_wing_ip);
                    destination_builder.setMacAddress(current_arion_wing_mac);
                    gateway_configuration_builder.addDestinations(destination_builder.build());
                    System.out.println("Adding GW destination with IP: " + current_arion_wing_ip + " and MAC:"+current_arion_wing_mac);
                }
                new_gateway_state_builder.setConfiguration(gateway_configuration_builder.build());
                com.futurewei.alcor.schema.Gateway.GatewayState current_gateway_state_for_both_nodes = new_gateway_state_builder.build();
                GoalState_builder_one.putGatewayStates(current_gateway_state_for_both_nodes.getConfiguration().getId(),current_gateway_state_for_both_nodes);
                GoalState_builder_two.putGatewayStates(current_gateway_state_for_both_nodes.getConfiguration().getId(), current_gateway_state_for_both_nodes);
                com.futurewei.alcor.schema.Goalstate.ResourceIdType gateway_resource_id_type = com.futurewei.alcor.schema.Goalstate.ResourceIdType.newBuilder().setType(com.futurewei.alcor.schema.Common.ResourceType.GATEWAY).setId(current_gateway_state_for_both_nodes.getConfiguration().getId()).build();
                host_resource_builder_node_one.addResources(gateway_resource_id_type);
                host_resource_builder_node_two.addResources(gateway_resource_id_type);
                host_resource_builder_node_one_port_one_neighbor.addResources(gateway_resource_id_type);
            }

            new_vpc_states.setConfiguration(vpc_configuration_builder.build());
            com.futurewei.alcor.schema.Vpc.VpcState vpc_state_for_both_nodes = new_vpc_states.build();



            GoalState_builder_one.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
//        GoalState_builder_one.putSubnetStates(subnet_state_for_both_nodes_two.getConfiguration().getId(), subnet_state_for_both_nodes_two);
            GoalState_builder_two.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
//        GoalState_builder_two.putSubnetStates(subnet_state_for_both_nodes_two.getConfiguration().getId(), subnet_state_for_both_nodes_two);
            GoalState_builder_one.putVpcStates(vpc_state_for_both_nodes.getConfiguration().getId(), vpc_state_for_both_nodes);
            GoalState_builder_two.putVpcStates(vpc_state_for_both_nodes.getConfiguration().getId(), vpc_state_for_both_nodes);

            com.futurewei.alcor.schema.Goalstate.ResourceIdType subnet_resource_id_type = com.futurewei.alcor.schema.Goalstate.ResourceIdType.newBuilder()
                    .setType(com.futurewei.alcor.schema.Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes.getConfiguration().getId()).build();
//        Goalstate.ResourceIdType subnet_resource_id_type_two = Goalstate.ResourceIdType.newBuilder()
//                .setType(Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes_two.getConfiguration().getId()).build();

            com.futurewei.alcor.schema.Goalstate.ResourceIdType vpc_resource_id_type = com.futurewei.alcor.schema.Goalstate.ResourceIdType.newBuilder().setType(com.futurewei.alcor.schema.Common.ResourceType.VPC).setId(vpc_state_for_both_nodes.getConfiguration().getId()).build();
            host_resource_builder_node_one.addResources(subnet_resource_id_type);
            host_resource_builder_node_two.addResources(subnet_resource_id_type);
            host_resource_builder_node_one_port_one_neighbor.addResources(subnet_resource_id_type);
//        host_resource_builder_node_one.addResources(subnet_resource_id_type_two);
//        host_resource_builder_node_two.addResources(subnet_resource_id_type_two);
//        host_resource_builder_node_one_port_one_neighbor.addResources(subnet_resource_id_type_two);
            host_resource_builder_node_one.addResources(vpc_resource_id_type);
            host_resource_builder_node_two.addResources(vpc_resource_id_type);
            host_resource_builder_node_one_port_one_neighbor.addResources(vpc_resource_id_type);

            if(test_against_aroin){
                JSONObject current_vpc_json_object = new JSONObject();
                current_vpc_json_object.put("vpc_id", current_vpc_id);
                //VNI == Tunnel ID
                current_vpc_json_object.put("vni", current_vpc_tunnel_id);
                JSONObject current_vpc_response = alcor_http_api_test.call_post_api_with_json(arion_master_restful_url + "/vpc", current_vpc_json_object);
                System.out.println("Setup VPC: " + current_vpc_id + " response: " + current_vpc_response.toJSONString() /*+ "\nsleep 5 seconds before the next VPC..."*/);
//                try {
//                    TimeUnit.SECONDS.sleep(5);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }

        // build the GroutingRuleRequest, to be sent to Arion Master
        com.futurewei.arion.schema.Goalstateprovisioner.RoutingRulesRequest routing_rule_request = routing_rule_request_builder.build();

        GoalState_builder_one.putHostResources(aca_node_one_ip, host_resource_builder_node_one.build());
        GoalState_builder_two.putHostResources(aca_node_two_ip, host_resource_builder_node_two.build());
        GoalState_builder_two.putHostResources(aca_node_one_ip, host_resource_builder_node_one_port_one_neighbor.build());
        com.futurewei.alcor.schema.Goalstate.GoalStateV2 message_one = GoalState_builder_one.build();
        com.futurewei.alcor.schema.Goalstate.GoalStateV2 message_two = GoalState_builder_two.build();

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

//        Tracer tracer = configuration.getTracer();
//        System.out.println("[Test Controller] Got this global tracer: "+tracer.toString());

//        TracingClientInterceptor tracingClientInterceptor = TracingClientInterceptor
//                .newBuilder()
//                .withTracer(tracer)
//                .withVerbosity()
//                .withStreaming()
//                .build();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(ncm_ip, ncm_port).usePlaintext().build();
        System.out.println("Constructed channel");
        com.futurewei.alcor.schema.GoalStateProvisionerGrpc.GoalStateProvisionerStub stub = com.futurewei.alcor.schema.GoalStateProvisionerGrpc.newStub(/*tracingClientInterceptor.intercept*/(channel));
//        Span parentSpan = tracer.activeSpan();
//        Span span;
//        if(parentSpan != null){
//            span = tracer.buildSpan("alcor-tc-send-gs").asChildOf(parentSpan.context()).start();
//            System.out.println("[Test Controller] Got parent span: "+parentSpan.toString());
//        }else{
//            span = tracer.buildSpan("alcor-tc-send-gs").start();
//        }
//        System.out.println("[Test Controller] Built child span: "+span.toString());
//        Scope cscope = tracer.scopeManager().activate(span);
//        span.log("abcdefg");
        System.out.println("Created stub");
        StreamObserver<com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply> message_observer = new StreamObserver<>() {
            @Override
            public void onNext(com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply value) {
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
        io.grpc.stub.StreamObserver<com.futurewei.alcor.schema.Goalstate.GoalStateV2> response_observer = stub.pushGoalStatesStream(message_observer);
        System.out.println("Connected the observers");
        response_observer.onNext(message_one);
        response_observer.onNext(message_two);

        System.out.println("After calling onNext");
        response_observer.onCompleted();

        System.out.println("Wait no longer than 6000 seconds until both goalstates are sent to both hosts.");
        Awaitility.await().atMost(6000, TimeUnit.SECONDS).until(()-> finished_sending_goalstate_hosts_count == NUMBER_OF_NODES);
//        span.finish();
//        System.out.println("[Test Controller] Child span after finish: "+span.toString());

        if (test_against_aroin){
//            Span arion_span;
            System.out.println("Now send the Routing Rule messages to Arion Master via gRPC");
            String arion_address_without_http = arion_master_ip.replaceAll("http://", "");
            ManagedChannel arion_channel = ManagedChannelBuilder.forAddress(arion_address_without_http, arion_master_grpc_port).usePlaintext().build();
            com.futurewei.arion.schema.GoalStateProvisionerGrpc.GoalStateProvisionerStub arion_stub = com.futurewei.arion.schema.GoalStateProvisionerGrpc.newStub(/*tracingClientInterceptor.intercept*/(arion_channel));
//            if(parentSpan != null){
//                arion_span = tracer.buildSpan("alcor-tc-send-gs").asChildOf(parentSpan.context()).start();
//                System.out.println("[Test Controller] Got parent span: "+parentSpan.toString());
//            }else{
//                arion_span = tracer.buildSpan("alcor-tc-send-gs").start();
//            }
//            System.out.println("Constructed channel and span.");
//            System.out.println("[Test Controller] Built child span: "+span.toString());
//            Scope arion_scope = tracer.scopeManager().activate(span);
//            span.log("random log line for Arion.");
            StreamObserver<com.futurewei.arion.schema.Goalstateprovisioner.GoalStateOperationReply> arion_message_observer = new StreamObserver<>() {
                @Override
                public void onNext(com.futurewei.arion.schema.Goalstateprovisioner.GoalStateOperationReply value) {
                    finished_sending_goalstate_hosts_count ++ ;
                    System.out.println("FROM ARION: onNext function with this GoalStateOperationReply: \n" + value.toString() + "\n");
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("FROM ARION: onError function with this GoalStateOperationReply: \n" + t.getMessage() + "\n");
                }

                @Override
                public void onCompleted() {
                    System.out.println("FROM ARION: onCompleted");
                }
            };
            System.out.println("FOR ARION: Created GoalStateOperationReply observer class");
            /*io.grpc.stub.StreamObserver<Goalstateprovisioner.RoutingRulesRequest> arion_response_observer = */ arion_stub.pushGoalstates(routing_rule_request, arion_message_observer);
            System.out.println("FOR ARION: Connected the observers");

            System.out.println("FOR ARION: After calling onNext");

            System.out.println("For ARION: Wait no longer than 6000 seconds until Routing Rules are sent to Arion Master.");
            Awaitility.await().atMost(6000, TimeUnit.SECONDS).until(()-> finished_sending_goalstate_hosts_count == (NUMBER_OF_NODES + 1) );
            String default_setup_url = "http://"+ arion_dp_controller_ip + "/default_setup";
            System.out.println("Calling Arion DP Controller at " + default_setup_url + " for default_setup.");

            HttpClient c = HttpClientBuilder.create().build();
            HttpGet getConnection = new HttpGet(default_setup_url);
            getConnection.setHeader("Content-Type", "application/json");
            try {
                HttpResponse default_setup_response = c.execute(getConnection);
                System.out.println("Get this /default_setup status code: " + default_setup_response.getStatusLine().getStatusCode() + "\nresponse: " + default_setup_response.toString());
                HttpEntity response_entity = default_setup_response.getEntity();

                String json_string = EntityUtils.toString(response_entity);

                JSONArray gws = (JSONArray) new JSONParser().parse(json_string);;
                for (int i = 0 ; i < gws.size() ; i ++ ){
                    JSONObject current_gateway = (JSONObject) gws.get(i);
                    String gw_ip = (String)current_gateway.get("ip");
                    String gw_mac = (String)current_gateway.get("mac");
                    System.out.println("Current Gateway IP:[" + gw_ip + "], Gateway MAC:[" + gw_mac + "]");
                }
            } catch (IOException | ParseException e) {
                System.out.println("FROM ARION: Got error when calling /default_setup: " + e.getMessage() + ", aborting...");
                e.printStackTrace();
                return;
            }
            System.out.println("For ARION: Wait no longer than 6000 seconds until Routing Rules are sent to Arion Master.");

        }

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

    // Send the ZGC_data and NODE_data to Arion Master, in order to set up gateway cluster and gateway nodes.
    public void setup_arion_gateway_cluster_and_nodes(){
        JSONObject cluster_data = (JSONObject) arion_data_json_object.get("ZGC_data");
        String arion_master_restful_url = arion_master_ip + ":" + arion_master_rest_port;
        JSONObject cluster_response = alcor_http_api_test.call_post_api_with_json(arion_master_restful_url+"/gatewaycluster", cluster_data );
//        System.out.println("Setup Gateway Cluster response: " + cluster_response.toJSONString() + "\nSleep 10 seconds before setting up Arion Nodes...");
//        try {
//            TimeUnit.SECONDS.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        JSONArray nodes = (JSONArray) arion_data_json_object.get("NODE_data");
        for(int i = 0 ; i < nodes.size(); i ++){
            JSONObject current_node = (JSONObject) nodes.get(i);
            JSONObject current_node_response = alcor_http_api_test.call_post_api_with_json(arion_master_restful_url+"/arionnode", current_node);
            System.out.println("Setup Gateway Node " + i + " response: " + current_node_response.toJSONString() /*+ "\nSleeping 10 seconds before setting up the next Arion Node..."*/);
//            try {
//                TimeUnit.SECONDS.sleep(5);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        return;
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

    class concurrent_run_cmd implements Runnable {
        String command_to_run, host, user_name, password;

        @Override
        public void run() {
//            Vector<String> cmd_list = new Vector<>();
            System.out.println("Need to execute this command concurrently: [" + this.command_to_run + "]");
//            cmd_list.add(this.command_to_run);
//        pseudo_controller.execute_ssh_commands(cmd_list, host, user_name, password);
            executeBashCommand(command_to_run);
        }

        public concurrent_run_cmd(String cmd, String host, String user_name, String password) {
            this.command_to_run = cmd;
            this.host = host;
            this.user_name = user_name;
            this.password = password;
        }

    }
}
