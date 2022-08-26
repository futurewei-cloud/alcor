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
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.TracingClientInterceptor;
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

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.futurewei.alcor.pseudo_controller.alcor_http_api_test.alcor_http_api_test;

@Component
public class ncm_test {
    @Value("#{'${compute_node_ips}'.split(',')}")
    ArrayList<String> compute_node_ips;
    @Value("#{'${compute_node_macs}'.split(',')}")
    ArrayList<String> compute_node_macs;
    @Value("#{'${compute_node_user_names}'.split(',')}")
    ArrayList<String> compute_node_usernames;
    @Value("#{'${compute_node_passwords}'.split(',')}")
    ArrayList<String> compute_node_passwords;
    @Value("#{'${ports_to_generate_on_each_compute_node}'.split(',')}")
    ArrayList<Integer> ports_to_generate_on_each_compute_node;
    @Value("${node_one_ip:ip_one}")
    String aca_node_one_ip;
    @Value("${node_one_mac:mac_one}")
    String aca_node_one_mac;
    @Value("${node_two_ip:ip_two}")
    String aca_node_two_ip;
    @Value("${node_two_mac:mac_two}")
    String aca_node_two_mac;
    int NUMBER_OF_NODES = 0;
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
    @Value("${arion_master_ip:arion_master_ip}")
    String arion_master_ip;
    @Value("${arion_rest_port:456}")
    int arion_master_rest_port;
    @Value("${arion_grpc_port:456}")
    int arion_master_grpc_port;
    @Value("${arion_dp_controller_ip:arion_dp_controller_ip}")
    String arion_dp_controller_ip;
    JSONArray arion_gw_ip_macs;
    @Value("${use_arion_agent:false}")
    Boolean use_arion_agent;

    static String docker_ps_cmd = "docker ps";
    static String vpc_id_1 = "2b08a5bc-b718-11ea-b3de-111111111112";
    static String port_ip_template = "11111111-b718-11ea-b3de-";
    static String subnet_id_1 = "27330ae4-b718-11ea-b3df-111111111113";
    static String subnet_id_2 = "27330ae4-b718-11ea-b3df-111111111114";
    static String ips_ports_ip_prefix = "10";
    static String mac_port_prefix = "00:00:01:";//"6c:dd:ee:";
    static String project_id = "alcor_testing_project";
    static SortedMap<String, String> ip_mac_map = new TreeMap<>();
    static SortedMap<String, SortedMap<String, String>> subnet_prefix_to_ip_mac_map = new TreeMap<>();
    static Vector<String> aca_node_one_commands = new Vector<>();
    static Vector<String> aca_node_two_commands = new Vector<>();
    static SortedMap<String, String> port_ip_to_host_ip_map = new TreeMap<>();
    static SortedMap<String, String> port_ip_to_id_map = new TreeMap<>();
    static SortedMap<String, String> port_ip_to_container_name = new TreeMap<>();

    // key is compute node IP, value is an array of port IPs on that compute node.
    static TreeMap<String, Vector<String>> compute_node_ip_to_ports = new TreeMap<>();

    static TreeMap<String, String> port_ip_to_subnet_id = new TreeMap<>();
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
    // if true, pings should be executed as bash command; otherwise, pings should be executed as ssh commands.
    boolean is_aca_node_one_local = false;

    static int number_of_subnets = 2;
    static ArrayList<String> subnets_ips_ports_ip_prefix = new ArrayList<>();
    static ArrayList<String> subnets_macs_ports_mac_third_octects = new ArrayList<>();

    static String DEFAULT_ARION_GROUP_NAME = "group1";
    public ncm_test(){
        System.out.println("Start of NCM Test!");
    }

    public void run_test_against_ncm() {
        try {
            ArrayList<String> local_ips = getNonLoopbackIPAddressList(true,true);
            // if on the same host, execute bash command; otherwise, execute ssh command.
            is_aca_node_one_local = local_ips.contains(compute_node_ips.get(0)/*aca_node_one_ip*/);
        }catch (SocketException e){
            System.err.println("Get this error when trying to collect localhost IPs: " + e.getMessage());
            return;
        }
        int number_of_gws_each_subnet_gets = 0;

        String arion_master_restful_url = arion_master_ip+":"+arion_master_rest_port;
        if(test_against_aroin){
            System.out.println("Test against Arion: " + test_against_aroin + ", ArionMaster IP: " + arion_master_ip + ", Arion Master REST port: " + arion_master_rest_port + ", Arion Master gRPC port: " + arion_master_grpc_port + ", Arion DP Controller IP: " + arion_dp_controller_ip);
            InputStream is = com.futurewei.alcor.pseudo_controller.pseudo_controller.class.getResourceAsStream("/arion_data.json");
            JSONParser jsonParser = new JSONParser();
            try {
                InputStreamReader reader = new InputStreamReader(is, "UTF-8");
                arion_data_json_object = (JSONObject)jsonParser.parse(reader);
                reader.close();
            }catch (IOException | NullPointerException | ParseException e ){
                System.out.println("Unable to read from json data file: " + e.getMessage() + "\nAborting...");
                return;
            }
            setup_arion_gateway_cluster_and_nodes();
        }
        System.out.println("There are "+ number_of_vpcs+" VPCs, " + number_of_subnets);
        int total_amount_of_ports = 0;

        compute_node_ips.forEach( ip -> {
            compute_node_ip_to_ports.put(ip, new Vector<>());
        });

        for (int i = 0 ; i < ports_to_generate_on_each_compute_node.size(); i++){
            total_amount_of_ports += ports_to_generate_on_each_compute_node.get(i);
        }
        NUMBER_OF_NODES = compute_node_ips.size();
        generate_ip_macs(total_amount_of_ports);
        create_containers_on_both_hosts_concurrently(total_amount_of_ports);
        System.out.println("Compute node IPs: " + compute_node_ips + "\nuser name: " + compute_node_usernames + "\npassword: " + compute_node_passwords);

        System.out.println("Containers setup done, now we gotta construct the GoalStateV2");

        System.out.println("Trying to build the GoalStateV2 for " + (ports_to_generate_on_aca_node_one + ports_to_generate_on_aca_node_two) + " Ports");

        TreeMap<String, Goalstate.GoalStateV2.Builder> compute_node_ip_to_GoalStateV2_map = new TreeMap<>();
        TreeMap<String, Goalstate.HostResources.Builder> compute_node_ip_to_host_resource_map = new TreeMap<>();

        compute_node_ips.forEach(ip -> {
                    Goalstate.GoalStateV2.Builder goalstate_builder_for_current_compute_node = Goalstate.GoalStateV2.newBuilder();
                    Goalstate.HostResources.Builder host_resource_builder_for_current_compute_node = Goalstate.HostResources.newBuilder();
                    compute_node_ip_to_GoalStateV2_map.put(ip, goalstate_builder_for_current_compute_node);
                    compute_node_ip_to_host_resource_map.put(ip, host_resource_builder_for_current_compute_node);
                }
        );

        // a new Goalstate and its builder for Arion master; should have only neigbhor states.
        Goalstateprovisioner.NeighborRulesRequest.Builder arion_neighbor_rule_request_builder = Goalstateprovisioner.NeighborRulesRequest.newBuilder();
        arion_neighbor_rule_request_builder.setFormatVersion(1);
        arion_neighbor_rule_request_builder.setRequestId("arion_routing_rule-" + (System.currentTimeMillis() / 1000L));

        for (int vpc_number = 1 ; vpc_number <= number_of_vpcs ; vpc_number ++){
            // generate VPC ID and Subnet ID for the current VPC
            int current_vpc_tunnel_id = 21 + vpc_number -1;
            String current_vpc_id = vpc_id_1.substring(0, vpc_id_1.length()-3)+String.format("%03d", (vpc_number));
            Vpc.VpcConfiguration.Builder vpc_configuration_builder = Vpc.VpcConfiguration.newBuilder();

            for( int subnet_number = 1 ; subnet_number <= number_of_subnets ; subnet_number ++){
                String current_subnet_id = subnet_id_1.substring(0,  subnet_id_1.length()-3) + String.format("%03d", (subnet_number));
                String current_subnet_ip_prefix = subnets_ips_ports_ip_prefix.get(subnet_number - 1);
                ArrayList<String> ports_ips_for_current_subnet = (ArrayList<String>) ip_mac_map.keySet().stream().filter(ip -> ip.substring(0,2).equals(current_subnet_ip_prefix)).collect(Collectors.toList()); // lgtm [java/abstract-to-concrete-cast]
                System.out.println("Current vpc_id: " + current_vpc_id + ", current subnet id: " + current_subnet_id);
                System.out.println("For current_subnet_ip_prefix [" + current_subnet_ip_prefix + "], we have " + ports_ips_for_current_subnet.size() + " ports:\n" + ports_ips_for_current_subnet);

                // Generate three unique octets for each VPC
                // instead of using the same mac_port_prefix for all ports
                String mac_first_octet, mac_second_octet, mac_third_octet;

                mac_first_octet = Integer.toHexString(vpc_number / 10000);
                mac_second_octet = Integer.toHexString((vpc_number % 10000) / 100 );
                // not auto auto generaing the third octect, as now it is now related to the number_of_subnets.
//            mac_third_octet = Integer.toHexString(vpc_number % 100);

                String current_vpc_mac_prefix = mac_first_octet + ":" + mac_second_octet + ":";

            /*
             generate port states for the current VPC, currently all VPCs has the same number ports, and these ports'
             IP range and mac range will be the same.
            */
                for (String port_ip : ports_ips_for_current_subnet) {
                    String host_ip = port_ip_to_host_ip_map.get(port_ip);
                    String port_id = port_ip_to_id_map.get(port_ip);
                    // replace the first 3 digits of the port id with the current vpc_number,
                    // if vpc_number == 1 then the first 3 digits of this port_id will be "001"
                    String vpc_port_id = String.format("%02d", (vpc_number)) + String.format("%02d", subnet_number) + port_id.substring(4);
                    String port_mac = ip_mac_map.get(port_ip).replaceFirst("00:00:"/*mac_port_prefix*/, current_vpc_mac_prefix);
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
                    NeighborConfiguration_builder.setMacAddress(compute_node_macs.get(compute_node_ips.indexOf(host_ip))/*host_ip == aca_node_one_ip ? aca_node_one_mac : aca_node_two_mac*/);
                    NeighborConfiguration_builder.setHostIpAddress(host_ip);

                    Neighbor.NeighborConfiguration.FixedIp.Builder neighbor_fixed_ip_builder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
                    neighbor_fixed_ip_builder.setNeighborType(Neighbor.NeighborType.L2);
                    neighbor_fixed_ip_builder.setSubnetId(current_subnet_id);
                    neighbor_fixed_ip_builder.setIpAddress(port_ip);
                    neighbor_fixed_ip_builder.setMacAddress(port_mac);
                    neighbor_fixed_ip_builder.setTunnelId(current_vpc_tunnel_id);
                    // use only 1 group ID for now. Used to be current_subnet_id
                    neighbor_fixed_ip_builder.setArionGroup(DEFAULT_ARION_GROUP_NAME);

                    NeighborConfiguration_builder.addFixedIps(neighbor_fixed_ip_builder.build());

                    new_neighborState_builder.setConfiguration(NeighborConfiguration_builder.build());
                    Neighbor.NeighborState neighborState_node_one = new_neighborState_builder.build();

                    // Need to include the Port state in all scenarios.
                    compute_node_ip_to_GoalStateV2_map.get(host_ip).putPortStates(port_state_one.getConfiguration().getId(), port_state_one);
                    compute_node_ip_to_host_resource_map.get(host_ip).addResources(port_one_resource_id);
                    port_ip_to_subnet_id.put(port_ip, current_subnet_id);

                    if(test_against_aroin){
                        // We should put the neighbor states into the goalstate to Arion Master.
                        arion_neighbor_rule_request_builder.addNeigborstates(neighborState_node_one);
                    }else{
                        // NOT test against Arion; we should put the neigbhor states into the goalstate to NCM.
                        compute_node_ips.forEach( ip -> {
                            // only add this neighbor state to host other than the port's local host.
                            if (ip != host_ip) {
                                compute_node_ip_to_GoalStateV2_map.get(host_ip).putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
                                Goalstate.ResourceIdType resource_id_type_neighbor_node_one = Goalstate.ResourceIdType.newBuilder().
                                        setType(Common.ResourceType.NEIGHBOR).setId(neighborState_node_one.getConfiguration().getId()).build();
                                compute_node_ip_to_host_resource_map.get(host_ip).addResources(resource_id_type_neighbor_node_one);
                            }
                        });
                    }
//                System.out.println("Finished port state for port [" + port_ip + "] on host: [" + host_ip + "]");
                }
                // fill in subnet state structs
                Subnet.SubnetState.Builder new_subnet_states = Subnet.SubnetState.newBuilder();

                new_subnet_states.setOperationType(Common.OperationType.INFO);

                Subnet.SubnetConfiguration.Builder subnet_configuration_builder = Subnet.SubnetConfiguration.newBuilder();

                subnet_configuration_builder.setRevisionNumber(2);
                subnet_configuration_builder.setVpcId(current_vpc_id);
                subnet_configuration_builder.setId(current_subnet_id);
                String current_subnet_cidr = current_subnet_ip_prefix + ".0.0.0/24";
                subnet_configuration_builder.setCidr(current_subnet_cidr);
                subnet_configuration_builder.setTunnelId(current_vpc_tunnel_id);
                subnet_configuration_builder.setGateway(Subnet.SubnetConfiguration.Gateway.newBuilder().setIpAddress("0.0.0.0").setMacAddress("6c:dd:ee:0:0:40").build());

                new_subnet_states.setConfiguration(subnet_configuration_builder.build());

                Subnet.SubnetState subnet_state_for_both_nodes = new_subnet_states.build();

                compute_node_ips.forEach(ip -> {
                    compute_node_ip_to_GoalStateV2_map.get(ip).putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
                    Goalstate.ResourceIdType subnet_resource_id_type = Goalstate.ResourceIdType.newBuilder()
                            .setType(Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes.getConfiguration().getId()).build();
                    compute_node_ip_to_host_resource_map.get(ip).addResources(subnet_resource_id_type);
                });

                // put gateway information in the goalstates, each subnet should have one gateway state.
                if (test_against_aroin){
                    Gateway.GatewayState.Builder new_gateway_state_builder = Gateway.GatewayState.newBuilder();
                    new_gateway_state_builder.setOperationType(Common.OperationType.CREATE);
                    Gateway.GatewayConfiguration.Builder gateway_configuration_builder = Gateway.GatewayConfiguration.newBuilder();
                    String current_gateway_id = "tc-gateway-"+vpc_number+"-"+subnet_number;
                    vpc_configuration_builder.addGatewayIds(current_gateway_id);
                    gateway_configuration_builder.setId(current_gateway_id);
                    gateway_configuration_builder.setRequestId("tc-gateway-request-"+vpc_number);
                    gateway_configuration_builder.setGatewayType(Gateway.GatewayType.ARION/*ZETA*/);
                    Gateway.GatewayConfiguration.arion.Builder arion_builder = Gateway.GatewayConfiguration.arion.newBuilder();
                    arion_builder.setVpcId(current_vpc_id);
                    arion_builder.setVni(current_vpc_tunnel_id);
                    arion_builder.setPortInbandOperation(arion_port_inbound_operation);
                    arion_builder.setSubnetId(current_subnet_id);
                    gateway_configuration_builder.setArionInfo(arion_builder.build());
                    // switch to arion, instead of zeta.
//                    Gateway.GatewayConfiguration.zeta.Builder zeta_builder = Gateway.GatewayConfiguration.zeta.newBuilder();
//                    zeta_builder.setPortInbandOperation(arion_port_inbound_operation);
//                    gateway_configuration_builder.setZetaInfo(zeta_builder.build());
                    for(int i = 0 ; i < number_of_gws_each_subnet_gets ; i ++){
                        JSONObject current_gw = (JSONObject) arion_gw_ip_macs.get(((subnet_number - 1) * number_of_gws_each_subnet_gets) + i);
                        String current_arion_wing_ip = (String) current_gw.get("ip");
                        String current_arion_wing_mac = (String) current_gw.get("mac");
                        Gateway.GatewayConfiguration.destination.Builder destination_builder = Gateway.GatewayConfiguration.destination.newBuilder();
                        destination_builder.setIpAddress(current_arion_wing_ip);
                        destination_builder.setMacAddress(current_arion_wing_mac);
                        gateway_configuration_builder.addDestinations(destination_builder.build());
                        System.out.println("Adding GW destination with IP: " + current_arion_wing_ip + " and MAC:"+current_arion_wing_mac + " to subnet " + current_subnet_id);
                    }
                    new_gateway_state_builder.setConfiguration(gateway_configuration_builder.build());
                    Gateway.GatewayState current_gateway_state_for_both_nodes = new_gateway_state_builder.build();
                    Goalstate.ResourceIdType gateway_resource_id_type = Goalstate.ResourceIdType.newBuilder().setType(Common.ResourceType.GATEWAY).setId(current_gateway_state_for_both_nodes.getConfiguration().getId()).build();
                    compute_node_ips.forEach(ip -> {
                        compute_node_ip_to_GoalStateV2_map.get(ip).putGatewayStates(current_gateway_state_for_both_nodes.getConfiguration().getId(),current_gateway_state_for_both_nodes);
                        compute_node_ip_to_host_resource_map.get(ip).addResources(gateway_resource_id_type);
                    });
                }
            }

            // fill in VPC state structs
            Vpc.VpcState.Builder new_vpc_states = Vpc.VpcState.newBuilder();
            new_vpc_states.setOperationType(Common.OperationType.INFO);

            vpc_configuration_builder.setCidr("10.0.0.0/16");
            vpc_configuration_builder.setId(current_vpc_id);
            vpc_configuration_builder.setName("test_vpc");
            vpc_configuration_builder.setTunnelId(21 + vpc_number - 1);
            vpc_configuration_builder.setProjectId(project_id);
            vpc_configuration_builder.setRevisionNumber(2);



            new_vpc_states.setConfiguration(vpc_configuration_builder.build());
            Vpc.VpcState vpc_state_for_both_nodes = new_vpc_states.build();
            Goalstate.ResourceIdType vpc_resource_id_type = Goalstate.ResourceIdType.newBuilder().setType(Common.ResourceType.VPC).setId(vpc_state_for_both_nodes.getConfiguration().getId()).build();

            compute_node_ips.forEach(ip -> {
                compute_node_ip_to_GoalStateV2_map.get(ip).putVpcStates(vpc_state_for_both_nodes.getConfiguration().getId(), vpc_state_for_both_nodes);
                compute_node_ip_to_host_resource_map.get(ip).addResources(vpc_resource_id_type);
            });

            if(test_against_aroin){
                JSONObject current_vpc_json_object = new JSONObject();
                current_vpc_json_object.put("vpc_id", current_vpc_id);
                //VNI == Tunnel ID
                current_vpc_json_object.put("vni", current_vpc_tunnel_id);
                JSONObject current_vpc_response = alcor_http_api_test.call_post_api_with_json(arion_master_restful_url + "/vpc", current_vpc_json_object);
                System.out.println("Setup VPC: " + current_vpc_id + " response: " + current_vpc_response.toJSONString());
            }
        }

        // build the GroutingRuleRequest, to be sent to Arion Master
        Goalstateprovisioner.NeighborRulesRequest arion_neighbor_state_request = arion_neighbor_rule_request_builder.build();


        compute_node_ips.forEach(ip -> {
            compute_node_ip_to_GoalStateV2_map.get(ip).putHostResources(ip, compute_node_ip_to_host_resource_map.get(ip).build());
        });

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
        GoalStateProvisionerGrpc.GoalStateProvisionerStub stub = GoalStateProvisionerGrpc.newStub(/*tracingClientInterceptor.intercept*/(channel));
        Span parentSpan = tracer.activeSpan();
        Span span;
        if(parentSpan != null){
            span = tracer.buildSpan("alcor-tc-send-gs").asChildOf(((Span) parentSpan).context()).start();
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

        // if only tests against NCM, then no need to call the arion related APIs, we can simply send the GoalStateV2 to NCM
        if (!test_against_aroin) {
            compute_node_ips.forEach(ip -> {
                Goalstate.GoalStateV2 goalstatev2_for_host = compute_node_ip_to_GoalStateV2_map.get(ip).build();

                System.out.println("Built GoalState successfully for compute node: " + ip + ", GoalStateV2 content for host: \n" + goalstatev2_for_host.toString() + "\n");
                System.out.println("GoalStateV2 size in bytes for compute node: " + ip + "\n" + goalstatev2_for_host.getSerializedSize() + "\n");
                response_observer.onNext(goalstatev2_for_host);
            });

            System.out.println("After calling onNext");
            response_observer.onCompleted();

            System.out.println("Wait no longer than 6000 seconds until both goalstates are sent to both hosts.");
            Awaitility.await().atMost(6000, TimeUnit.SECONDS).until(()-> finished_sending_goalstate_hosts_count == NUMBER_OF_NODES);
            span.finish();
        System.out.println("[Test Controller] Child span after finish: "+span.toString());
        }else {
            Span arion_span;
            System.out.println("Now send the Neighbor Rule messages to Arion Master via gRPC");
            String arion_address_without_http = arion_master_ip.replaceAll("http://", "");
            ManagedChannel arion_channel = ManagedChannelBuilder.forAddress(arion_address_without_http, arion_master_grpc_port).usePlaintext().build();
            GoalStateProvisionerGrpc.GoalStateProvisionerStub arion_stub = GoalStateProvisionerGrpc.newStub(/*tracingClientInterceptor.intercept*/(arion_channel));
            if(parentSpan != null){
                arion_span = tracer.buildSpan("alcor-tc-send-gs").asChildOf(parentSpan.context()).start();
                System.out.println("[Test Controller] Got parent span: "+parentSpan.toString());
            }else{
                arion_span = tracer.buildSpan("alcor-tc-send-gs").start();
            }
            System.out.println("Constructed channel and span.");
            System.out.println("[Test Controller] Built child span: "+span.toString());
            Scope arion_scope = tracer.scopeManager().activate(span);
            span.log("random log line for Arion.");
            StreamObserver<Goalstateprovisioner.GoalStateOperationReply> arion_message_observer = new StreamObserver<>() {
                @Override
                public void onNext(Goalstateprovisioner.GoalStateOperationReply value) {
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
            System.out.println("Arion Neighbor Message: \n" + arion_neighbor_state_request);
            arion_stub.pushGoalstates(arion_neighbor_state_request, arion_message_observer);
            System.out.println("FOR ARION: Connected the observers");

            System.out.println("FOR ARION: After calling onNext");

            System.out.println("For ARION: Wait no longer than 6000 seconds until Routing Rules are sent to Arion Master.");
            Awaitility.await().atMost(6000, TimeUnit.SECONDS).until(()-> finished_sending_goalstate_hosts_count >= 1 );
            String default_setup_url = "http://"+ arion_dp_controller_ip + ":5000/default_setup" + "/?use_arion_agent="+use_arion_agent.toString();
            String get_nodes_url = "http://"+ arion_dp_controller_ip + ":5000/nodes";
            System.out.println("Calling Arion DP Controller at " + default_setup_url + " for default_setup.");

            HttpClient c = HttpClientBuilder.create().build();
            HttpGet getConnection = new HttpGet(default_setup_url);
            HttpGet get_nodes_connection = new HttpGet(get_nodes_url);
            getConnection.setHeader("Content-Type", "application/json");
            get_nodes_connection.setHeader("Content-Type", "application/json");
            /* TODO: The following HTTP GET request returns a list of GWs, but we don't know which ArionNode has which GWs,
            *  the Nodes info with which GWs belongs to this node was recently added to the GET /nodes API,
            *  in order to achieve sharding, we should consider utilizing the GET /nodes API after calling the GET /default_setup API,
            *  retrieve the ArionNode IPs and its GWs IPs/MACs, then assigning them to the GotewayStates accordingly.
            * */


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

                if (use_arion_agent) {
                    System.out.println("Need to run the arion agent on each Arion Wing Node to program the EBPF Maps");
                    JSONArray nodes = (JSONArray) arion_data_json_object.get("NODE_data");
                    String current_time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                    for (int i = 0 ; i < nodes.size() ; i ++) {
                        JSONObject current_node = (JSONObject) nodes.get(i);
                        String start_arion_agent_ssh_command = "sudo nohup " + (String) current_node.get("arion_agent_location") +
                                " -d -a " + arion_master_ip + " -p " + arion_master_grpc_port + " -g " + DEFAULT_ARION_GROUP_NAME +
                                " > /tmp/"+ current_time + ".log 2>&1 &";
                        Vector<String> cmds = new Vector<>();
                        cmds.add(start_arion_agent_ssh_command);
                        execute_ssh_commands(cmds, (String) current_node.get("ip_control"), (String) current_node.get("id_control"), (String) current_node.get("pwd_control"));
                    }
                    try {
                        TimeUnit.SECONDS.sleep(30);

                    } catch (Exception e) {
                        System.out.println("I can't sleep!!!!");

                    }
                }

                HttpResponse get_nodes_response = c.execute(get_nodes_connection);
                System.out.println("Get this /nodes status code: " + get_nodes_response.getStatusLine().getStatusCode() + "\nresponse: " + get_nodes_response.toString());
                HttpEntity get_nodes_response_entity = get_nodes_response.getEntity();
                String get_nodes_json_string = EntityUtils.toString(get_nodes_response_entity);
                JSONArray nodes = (JSONArray) new JSONParser().parse(get_nodes_json_string);
                JSONArray gws_ip_mac_json_array = new JSONArray();
                for (int i = 0 ; i < nodes.size() ; i ++) {
                    JSONObject current_arion_wing_node = (JSONObject) nodes.get(i);
                    String node_ip = (String) current_arion_wing_node.get("ip_control");
                    JSONArray current_arion_wing_node_gws_json_array = (JSONArray) current_arion_wing_node.get("gws");
                    for (int j = 0 ; j < current_arion_wing_node_gws_json_array.size() ; j ++) {
                        JSONObject current_gw_for_node = (JSONObject) current_arion_wing_node_gws_json_array.get(j);
                        System.out.println("Arion Wing Node IP: " + node_ip + ", " + j + "th GW IP: " + (String) current_gw_for_node.get("ip") + ", MAC: " + (String) current_gw_for_node.get("mac"));
                        gws_ip_mac_json_array.add(current_gw_for_node);
                    }
                }
                arion_gw_ip_macs = gws_ip_mac_json_array;
                number_of_gws_each_subnet_gets = (int) Math.ceil(arion_gw_ip_macs.size() / number_of_subnets);
                int finalNumber_of_gws_each_subnet_gets = number_of_gws_each_subnet_gets;
                compute_node_ips.forEach(ip -> {
                    Goalstate.GoalStateV2.Builder current_gsv2_builder = compute_node_ip_to_GoalStateV2_map.get(ip);
                    // Add the gws, retrived from the GET /nodes call, to the gateway states.
                    int subnet_number = 1;
                    for (String gatewa_state_id : current_gsv2_builder.getGatewayStatesMap().keySet()){
                        Gateway.GatewayState gateway_state = current_gsv2_builder.getGatewayStatesMap().get(gatewa_state_id);
                        gateway_state.getConfiguration().toBuilder().clearDestinations();
                        for(int i = 0; i < finalNumber_of_gws_each_subnet_gets; i ++){
                            JSONObject current_gw = (JSONObject) arion_gw_ip_macs.get(((subnet_number - 1) * finalNumber_of_gws_each_subnet_gets) + i);
                            String current_arion_wing_ip = (String) current_gw.get("ip");
                            String current_arion_wing_mac = (String) current_gw.get("mac");
                            Gateway.GatewayConfiguration.destination.Builder destination_builder = Gateway.GatewayConfiguration.destination.newBuilder();
                            destination_builder.setIpAddress(current_arion_wing_ip);
                            destination_builder.setMacAddress(current_arion_wing_mac);
                            gateway_state.getConfiguration().toBuilder().addDestinations(destination_builder.build()).build();
                            System.out.println("Adding GW destination with IP: " + current_arion_wing_ip + " and MAC:"+current_arion_wing_mac + " to subnet " + subnet_number);
                        }
                        subnet_number ++;
                    }

                    Goalstate.GoalStateV2 goalstatev2_for_host = compute_node_ip_to_GoalStateV2_map.get(ip).build();

                    System.out.println("Built GoalState successfully for compute node: " + ip + ", GoalStateV2 content for host: \n" + goalstatev2_for_host.toString() + "\n");
                    System.out.println("GoalStateV2 size in bytes for compute node: " + ip + "\n" + goalstatev2_for_host.getSerializedSize() + "\n");
                    response_observer.onNext(goalstatev2_for_host);
                    System.out.println("After calling onNext");
                });
                response_observer.onCompleted();
                System.out.println("Wait no longer than 6000 seconds until both goalstates are sent to both hosts.");
                Awaitility.await().atMost(6000, TimeUnit.SECONDS).until(()-> finished_sending_goalstate_hosts_count >= (NUMBER_OF_NODES + 1));
            } catch (IOException | ParseException e) {
                System.out.println("FROM ARION: Got error when calling /default_setup: " + e.getMessage() + ", aborting...");
                e.printStackTrace();
                return;
            }
            System.out.println("For ARION: Wait no longer than 6000 seconds until Routing Rules are sent to Arion Master.");

        }

        System.out.println("After the GRPC call, it's time to do the ping test");

        System.out.println("Sleep 10 seconds before executing the ping");
        try {
            TimeUnit.SECONDS.sleep(10);

        } catch (Exception e) {
            System.out.println("I can't sleep!!!!");

        }
        List<concurrent_run_cmd> concurrent_ping_cmds = new ArrayList<>();

        Vector<String> node_one_port_ips = compute_node_ip_to_ports.get(compute_node_ips.get(0));
        Vector<String> node_last_port_ips = compute_node_ip_to_ports.get(compute_node_ips.get(compute_node_ips.size()-1));
        String pinger_compute_node_user_name = compute_node_usernames.get(0);
        String pinger_compute_node_password = compute_node_passwords.get(0);
        for (int i = 0; i < node_last_port_ips.size(); i ++){
            String pingee_ip = node_last_port_ips.get(i);
            String pinger_ip = "";
            for (String one_port_ip_on_node_one : node_one_port_ips) {
                if (port_ip_to_subnet_id.get(one_port_ip_on_node_one) == port_ip_to_subnet_id.get(pingee_ip)) {
                    pinger_ip = one_port_ip_on_node_one;
                    break;
                }
            }
            if (pinger_ip != "") {
                String pinger_container_name = port_ip_to_container_name.get(pinger_ip);
                String ping_cmd = "docker exec " + pinger_container_name + " ping -I " + pinger_ip + " -c1 " + pingee_ip;
                concurrent_ping_cmds.add(new concurrent_run_cmd(ping_cmd, compute_node_ips.get(0), pinger_compute_node_user_name, pinger_compute_node_password, is_aca_node_one_local));
//            System.out.println("Ping command is added: [" + ping_cmd + "]");
            }else {
                System.out.println("For port [" + pingee_ip + "], there is no corresponding port in same subnet on node one, thus skipping ping for this port.");
            }

        }

        System.out.println("Time to execute these ping commands concurrently");



        if(whether_to_create_containers_and_ping == CREATE_CONTAINER_AND_PING){


            // Execute the pings.
            for (concurrent_run_cmd cmd : concurrent_ping_cmds) {
                if (user_chosen_ping_method == CONCURRENT_PING_MODE) {
                    //concurrent, now use the threadpool to execute.
                    concurrent_create_containers_thread_pool.execute(cmd);
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
        System.out.println("Setup Gateway Cluster response: " + cluster_response.toJSONString());

        JSONArray nodes = (JSONArray) arion_data_json_object.get("NODE_data");
        for(int i = 0 ; i < nodes.size(); i ++){
            JSONObject current_node = (JSONObject) nodes.get(i);
            JSONObject current_node_response = alcor_http_api_test.call_post_api_with_json(arion_master_restful_url+"/arionnode", current_node);
            System.out.println("Setup Gateway Node " + i + " response: " + current_node_response.toJSONString() /*+ "\nSleeping 10 seconds before setting up the next Arion Node..."*/);
        }
        return;
    }

    // Generates IP/MAC for host_many_per_host, and inserts them into the hashmap
    public static void generate_ip_macs(int amount_of_ports_to_generate) {
        System.out.println("There are " + number_of_subnets + " subnets, need to generate " + amount_of_ports_to_generate + " ports for each subnet.");
        int i = 2;


        // The number of subnets shall not excess 256 - 10 = 246.
        for (int s = 0 ; s < number_of_subnets ; s ++){
            String current_subnet_ports_ip_prefix = String.valueOf(Integer.parseInt(ips_ports_ip_prefix) + s);
            String current_subnet_ports_ip_third_octect = String.format("%02X", s);
            System.out.println("The " + s + "th subnet has port IP prefix of " + current_subnet_ports_ip_prefix + ":, and the mac prefix of 00:00:"+current_subnet_ports_ip_third_octect);
            subnets_ips_ports_ip_prefix.add(current_subnet_ports_ip_prefix);
            subnets_macs_ports_mac_third_octects.add(current_subnet_ports_ip_third_octect);
        }

        //        Each subnet gets the same number of ports
        while (ip_mac_map.size() != amount_of_ports_to_generate * number_of_subnets) {
            if (i % 100 != 0) {
                String ip_2nd_octet = Integer.toString(i / 10000);
                String ip_3nd_octet = Integer.toString((i % 10000) / 100);
                String ip_4nd_octet = Integer.toString(i % 100);
                for (int j = 0 ; j < subnets_ips_ports_ip_prefix.size() ; j ++ ){
                    String ip_for_port = subnets_ips_ports_ip_prefix.get(j) + "." + ip_2nd_octet + "." + ip_3nd_octet + "." + ip_4nd_octet;
                    String mac_for_port = "00:00:" + subnets_macs_ports_mac_third_octects.get(j) + ":" + ip_2nd_octet + ":" + ip_3nd_octet + ":" + ip_4nd_octet;
                    String id_for_port = port_ip_template + subnets_ips_ports_ip_prefix.get(j) + String.format("%03d", (i / 10000)) + String.format("%03d", ((i % 10000) / 100)) + String.format("%03d", (i % 100));
                    ip_mac_map.put(ip_for_port, mac_for_port);
                    port_ip_to_id_map.put(ip_for_port, id_for_port);
                    System.out.println("Generated Port " + i + " with IP: [" + ip_for_port + "], ID :[ " + id_for_port + "] and MAC: [" + mac_for_port + "]");
                }
            }
            i++;
        }
        System.out.println("Finished generating " + amount_of_ports_to_generate + " ports for each subnet, ip->mac map has "+ ip_mac_map.size() +" entries, ip->id map has "+port_ip_to_id_map.size()+" entries");
    }

    private void create_containers_on_both_hosts_concurrently(int total_amount_of_ports_per_subnet) {
        System.out.println("Creating containers on both hosts, ip_mac_map has " + ip_mac_map.keySet().size() + "keys");
        int i = 1;
        String background_pinger = "";
        String background_pingee = "";
        // use a countdown latch to wait for the threads to finish.
        CountDownLatch latch = new CountDownLatch(ip_mac_map.keySet().size());

        int current_compute_node_index = 0;
        String compute_node_ip_for_this_port = compute_node_ips.get(current_compute_node_index);
        ArrayList<String> port_ip_arrayList = new ArrayList<>();
        port_ip_arrayList.addAll(ip_mac_map.keySet());

        // each subnet has the same number of ports.
        for (int j = 0 ; j < total_amount_of_ports_per_subnet ; j ++){
            for (int k = 0 ; k < number_of_subnets ; k ++) {
                // using this way in order to get a port from each subnet to add on a compute node
                // trying to spread the ports in different subnets evenly on each compute node.
                String port_ip = port_ip_arrayList.get(total_amount_of_ports_per_subnet * k + j);
                String port_mac = ip_mac_map.get(port_ip);
                String container_name = "test" + Integer.toString(i);
                port_ip_to_container_name.put(port_ip, container_name);
                String create_container_cmd = "docker run -itd --name " + container_name + " --net=none --label test=ACA busybox sh";
                String ovs_docker_add_port_cmd = "sudo ovs-docker add-port br-int eth0 " + container_name + " --ipaddress=" + port_ip + "/16 --macaddress=" + port_mac;
                String ovs_set_vlan_cmd = "sudo ovs-docker set-vlan br-int eth0 " + container_name + " 1";
                Vector<String> create_one_container_and_assign_IP_vlax_commands = new Vector<>();
                create_one_container_and_assign_IP_vlax_commands.add(create_container_cmd);
                create_one_container_and_assign_IP_vlax_commands.add(ovs_docker_add_port_cmd);
                create_one_container_and_assign_IP_vlax_commands.add(ovs_set_vlan_cmd);

//            String compute_node_ip_for_this_port = compute_node_ips.get( i % NUMBER_OF_NODES);
                System.out.println("i = " + i + " , assigning IP: [" + port_ip + "] to node: [" + compute_node_ip_for_this_port + "]");
                port_ip_to_host_ip_map.put(port_ip, compute_node_ip_for_this_port);
                compute_node_ip_to_ports.get(compute_node_ip_for_this_port).add(port_ip);

                if(whether_to_create_containers_and_ping == CREATE_CONTAINER_AND_PING){
                    String finalCompute_node_ip_for_this_port = compute_node_ip_for_this_port;
                    concurrent_create_containers_thread_pool.execute(() -> {
                        execute_ssh_commands(create_one_container_and_assign_IP_vlax_commands,
                                finalCompute_node_ip_for_this_port,
                                compute_node_usernames.get(compute_node_ips.indexOf(finalCompute_node_ip_for_this_port)),
                                compute_node_passwords.get(compute_node_ips.indexOf(finalCompute_node_ip_for_this_port)));
                        latch.countDown();
                    });
                }

                // if enough amount of ports is generated for this compute node, then we begin generating ports for the next one.
                if (compute_node_ip_to_ports.get(compute_node_ip_for_this_port).size() >= ports_to_generate_on_each_compute_node.get(current_compute_node_index) * number_of_subnets) {
                    System.out.println("We have already generated [" + compute_node_ip_to_ports.get(compute_node_ip_for_this_port).size() + "] ports/containers for compute node ["
                            + compute_node_ip_for_this_port + "], it has reached its target amount [" + ports_to_generate_on_each_compute_node.get(current_compute_node_index) +
                            "], time to generate ports/containers for the next host."
                    );
                    current_compute_node_index ++;
                    if (current_compute_node_index < compute_node_ips.size()){
                        compute_node_ip_for_this_port = compute_node_ips.get(current_compute_node_index);
                    }
                }

                if (compute_node_ip_for_this_port == compute_node_ips.get(0)){
                    // this is a pinger
                    background_pinger = port_ip;
                }else {
                    // this is a pingee
                    background_pingee = port_ip;
                }
                i++;
            }
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
                concurrent_run_cmd c = new concurrent_run_cmd(background_ping_command, compute_node_ips.get(0), compute_node_usernames.get(0), compute_node_passwords.get(0), is_aca_node_one_local);
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
                OutputStream out = channel.getOutputStream();
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream in = channel.getInputStream();
                ((ChannelExec) channel).setPty(true);
                channel.connect();
                out.write((host_password + "\n").getBytes());
                out.flush();
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

    private static ArrayList<String> getNonLoopbackIPAddressList(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        ArrayList<String> localIpList = new ArrayList<>();
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        System.out.println("Local IP address: " + addr.getHostAddress());
                        localIpList.add(addr.getHostAddress());
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        System.out.println("Local IP address: " + addr.getHostAddress());
                        localIpList.add((addr.getHostAddress()));
                    }
                }
            }
        }
        return localIpList;
    }

    class concurrent_run_cmd implements Runnable {
        String command_to_run, host, user_name, password;
        boolean is_local;

        @Override
        public void run() {
            System.out.println("Need to execute this command concurrently: [" + this.command_to_run + "]");

            if (this.is_local){
                executeBashCommand(command_to_run);
            }else{
                Vector<String> cmd_list = new Vector<>();
                cmd_list.add(this.command_to_run);
                execute_ssh_commands(cmd_list, host, user_name, password);
            }
        }

        public concurrent_run_cmd(String cmd, String host, String user_name, String password, boolean is_local) {
            this.command_to_run = cmd;
            this.host = host;
            this.user_name = user_name;
            this.password = password;
            this.is_local = is_local;
        }

    }
}
