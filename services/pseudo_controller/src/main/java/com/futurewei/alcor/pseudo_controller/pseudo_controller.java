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
1. Number of ports to generate to each aca node
2. IP of aca_node_one
3. IP of aca_node_two
4. IP of the GRPC call
5. Port of the GRPC call
6. User name of aca_nodes
7. Password of aca_nodes
*/
package com.futurewei.alcor.pseudo_controller;

import com.futurewei.alcor.schema.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class pseudo_controller {

    static String aca_node_one_ip = "ip_one";
    static String aca_node_two_ip = "ip_two";
    static String ncm_ip = "ip_three";
    static int ncm_port = 123;
    static String user_name = "root";
    static String password = "abcdefg";
    static int ports_to_generate_on_each_aca_node = 1;
    static String docker_ps_cmd = "docker ps";
    static String vpc_id_1 = "2b08a5bc-b718-11ea-b3de-111111111112";
    static String port_ip_template = "11111111-b718-11ea-b3de-";
    static String subnet_id_1 = "27330ae4-b718-11ea-b3df-111111111113";
    static String ips_ports_ip_prefix = "10";
    static String mac_port_prefix = "6c:dd:ee:";
    static String project_id = "alcor_testing_project";
    static String default_container_port_interface_name = "eth0";
    static long backgroupd_ping_start_time = 0;
    static long backgroupd_ping_end_time = 0;
    static SortedMap<String, String> ip_mac_map = new TreeMap<>();
    static Vector<String> aca_node_one_create_container_commands = new Vector<>();
    static Vector<String> aca_node_two_create_container_commands = new Vector<>();
    static Vector<String> aca_node_one_ovs_docker_commands = new Vector<>();
    static Vector<String> aca_node_two_ovs_docker_commands = new Vector<>();

    static SortedMap<String, String> port_ip_to_host_ip_map = new TreeMap<>();
    static SortedMap<String, String> port_ip_to_id_map = new TreeMap<>();   // the id of the port should also be the ovs port name of the port, otherwise ACA will run incorrectly.
    static SortedMap<String, String> port_ip_to_container_name = new TreeMap<>();
//    static SortedMap<String, String> port_ip_to_ovs_port_name = new TreeMap<>();
    static Vector<String> node_one_port_ips = new Vector<>();
    static Vector<String> node_two_port_ips = new Vector<>();
    static ExecutorService taskExecutor = Executors.newFixedThreadPool(1);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start of the test controller");
        if (args.length == 7) {
            System.out.println("User passed in params and we need to read them.");
            ports_to_generate_on_each_aca_node = Integer.parseInt(args[0]);
            aca_node_one_ip = args[1];
            aca_node_two_ip = args[2];
            ncm_ip = args[3];
            ncm_port = Integer.parseInt(args[4]);
            user_name = args[5];
            password = args[6];

        }
        generate_ip_macs(ports_to_generate_on_each_aca_node * 2);
        create_containers_on_both_hosts();
        System.out.println("aca_node_one_ip: " + aca_node_one_ip + "\naca_node_two_ip: " + aca_node_two_ip + "\nuser name: " + user_name + "\npassword: " + password);

        System.out.println("Containers setup done, now we gotta construct the GoalStateV2");

        System.out.println("Trying to build the GoalStateV2 for " + ports_to_generate_on_each_aca_node + " Ports");


        Goalstate.GoalStateV2.Builder GoalState_builder_one = Goalstate.GoalStateV2.newBuilder();
        Goalstate.GoalStateV2.Builder GoalState_builder_two = Goalstate.GoalStateV2.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_one = Goalstate.HostResources.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_two = Goalstate.HostResources.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_one_port_one_neighbor = Goalstate.HostResources.newBuilder();

        for (String port_ip : ip_mac_map.keySet()) {
            String host_ip = port_ip_to_host_ip_map.get(port_ip);
            String port_id = port_ip_to_id_map.get(port_ip);
            String port_mac = ip_mac_map.get(port_ip);
            // if it's on node 1, we don't add neighbor info here,
            // start of setting up port 1 on aca node 1
            Port.PortState.Builder new_port_states = Port.PortState.newBuilder();

            new_port_states.setOperationType(Common.OperationType.CREATE);

            // fill in port state structs for port 1
            Port.PortConfiguration.Builder config = new_port_states.getConfigurationBuilder();
            config.
                    setRevisionNumber(2).
                    setUpdateType(Common.UpdateType.FULL).
                    setId(port_id).
                    setVpcId(vpc_id_1).
                    setName(("tap" + port_id.substring(port_id.length()-11, port_id.length()-1))).
                    setAdminStateUp(true).
                    setMacAddress(port_mac);
            Port.PortConfiguration.FixedIp.Builder fixedIpBuilder = Port.PortConfiguration.FixedIp.newBuilder();
            fixedIpBuilder.setSubnetId(subnet_id_1);
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
            NeighborConfiguration_builder.setVpcId(vpc_id_1);
            NeighborConfiguration_builder.setId(port_id+"_n");
            NeighborConfiguration_builder.setMacAddress(port_mac);
            NeighborConfiguration_builder.setHostIpAddress(host_ip);

            Neighbor.NeighborConfiguration.FixedIp.Builder neighbor_fixed_ip_builder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
            neighbor_fixed_ip_builder.setNeighborType(Neighbor.NeighborType.L2);
            neighbor_fixed_ip_builder.setSubnetId(subnet_id_1);
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
            System.out.println("Finished port state for port [" + port_ip + "] on host: [" + host_ip + "]");
        }

        // fill in subnet state structs
        Subnet.SubnetState.Builder new_subnet_states = Subnet.SubnetState.newBuilder();

        new_subnet_states.setOperationType(Common.OperationType.INFO);

        Subnet.SubnetConfiguration.Builder subnet_configuration_builder = Subnet.SubnetConfiguration.newBuilder();

        subnet_configuration_builder.setRevisionNumber(2);
        subnet_configuration_builder.setVpcId(vpc_id_1);
        subnet_configuration_builder.setId(subnet_id_1);
        subnet_configuration_builder.setCidr("10.0.0.0/24");
        subnet_configuration_builder.setTunnelId(21);

        new_subnet_states.setConfiguration(subnet_configuration_builder.build());

        Subnet.SubnetState subnet_state_for_both_nodes = new_subnet_states.build();
        // put the new subnet state of subnet 1 into the goalstatev2

        // fill in VPC state structs
        Vpc.VpcState.Builder new_vpc_states = Vpc.VpcState.newBuilder();
        new_vpc_states.setOperationType(Common.OperationType.INFO);

        Vpc.VpcConfiguration.Builder vpc_configuration_builder = Vpc.VpcConfiguration.newBuilder();
        vpc_configuration_builder.setCidr("10.0.0.0/16");
        vpc_configuration_builder.setId(vpc_id_1);
        vpc_configuration_builder.setName("test_vpc");
        vpc_configuration_builder.setTunnelId(21);
        vpc_configuration_builder.setProjectId(project_id);
        vpc_configuration_builder.setRevisionNumber(2);

        new_vpc_states.setConfiguration(vpc_configuration_builder.build());
        Vpc.VpcState vpc_state_for_both_nodes = new_vpc_states.build();

        GoalState_builder_one.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
        GoalState_builder_two.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
        GoalState_builder_one.putVpcStates(vpc_state_for_both_nodes.getConfiguration().getId(), vpc_state_for_both_nodes);
        GoalState_builder_two.putVpcStates(vpc_state_for_both_nodes.getConfiguration().getId(), vpc_state_for_both_nodes);

        Goalstate.ResourceIdType subnet_resource_id_type = Goalstate.ResourceIdType.newBuilder()
                .setType(Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes.getConfiguration().getId()).build();

        Goalstate.ResourceIdType vpc_resource_id_type = Goalstate.ResourceIdType.newBuilder().setType(Common.ResourceType.VPC).setId(vpc_state_for_both_nodes.getConfiguration().getId()).build();
        host_resource_builder_node_one.addResources(subnet_resource_id_type);
        host_resource_builder_node_two.addResources(subnet_resource_id_type);
        host_resource_builder_node_one_port_one_neighbor.addResources(subnet_resource_id_type);
        host_resource_builder_node_one.addResources(vpc_resource_id_type);
        host_resource_builder_node_two.addResources(vpc_resource_id_type);
        host_resource_builder_node_one_port_one_neighbor.addResources(vpc_resource_id_type);

        GoalState_builder_one.putHostResources(aca_node_one_ip, host_resource_builder_node_one.build());
        GoalState_builder_two.putHostResources(aca_node_two_ip, host_resource_builder_node_two.build());
        GoalState_builder_two.putHostResources(aca_node_one_ip, host_resource_builder_node_one_port_one_neighbor.build());
        Goalstate.GoalStateV2 message_one = GoalState_builder_one.build();
        Goalstate.GoalStateV2 message_two = GoalState_builder_two.build();

        System.out.println("Built GoalState successfully, GoalStateV2 content for PORT1: \n" + message_one.toString() + "\n");
        System.out.println("Built GoalState successfully, GoalStateV2 size for PORT1: \n" + message_one.getSerializedSize() + "\n");

        System.out.println("Built GoalState successfully, GoalStateV2 content for PORT2: \n" + message_two.toString() + "\n");
        System.out.println("Built GoalState successfully, GoalStateV2 size for PORT2: \n" + message_two.getSerializedSize() + "\n");

        System.out.println("Time to call the GRPC functions");

        ManagedChannel channel = ManagedChannelBuilder.forAddress(ncm_ip, ncm_port).usePlaintext().build();
        System.out.println("Constructed channel");
        GoalStateProvisionerGrpc.GoalStateProvisionerStub stub = GoalStateProvisionerGrpc.newStub(channel);

        System.out.println("Created stub");
        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> message_observer = new StreamObserver<>() {
            @Override
            public void onNext(Goalstateprovisioner.GoalStateOperationReply value) {
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
        System.out.println("After the GRPC call, it's time to do the ping test");
        System.out.println("Sleep 20 second first");
        try {
            TimeUnit.SECONDS.sleep(20);

        } catch (Exception e) {
            System.out.println("I can't sleep!!!!");

        }
        List<concurrent_run_cmd> concurrent_ping_cmds = new ArrayList<>();
        for (int i = 0; i < node_one_port_ips.size(); i++) {
            if (i >= node_two_port_ips.size()) {
                break;
            }
            String pinger_ip = node_one_port_ips.get(i);
            String pinger_container_name = port_ip_to_container_name.get(pinger_ip);
            String pingee_ip = node_two_port_ips.get(i);
            String ping_cmd = "docker exec " + pinger_container_name + " ping -I " + pinger_ip + " -c1 " + pingee_ip;
            concurrent_ping_cmds.add(new concurrent_run_cmd(ping_cmd, aca_node_one_ip, user_name, password));
            System.out.println("Ping command is added: [" + ping_cmd + "]");
        }

        System.out.println("Time to execute these ping commands concurrently");

        // Create a thread pool to execute the pings

//        int MAX_THREADS = 5;  // thread pool size
//
//        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        // Concurrently execute the pings.
        for (concurrent_run_cmd cmd : concurrent_ping_cmds) {
             //concurrent
            Thread t = new Thread(cmd);
            t.start();
            // sequential
            //cmd.run()
            // use thread pool
//            pool.execute(cmd);
        }


        System.out.println("End of the test controller");
        channel.shutdown();
        System.out.println("Calling shutdown");
        taskExecutor.shutdown();


        try{
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }catch (InterruptedException e){
            backgroupd_ping_end_time = System.currentTimeMillis();
            System.out.println("Executor interrupted: "+ e.getMessage() + " time interval in milliseconds: " + (backgroupd_ping_end_time - backgroupd_ping_start_time));
        }

        try {
            TimeUnit.SECONDS.sleep(10);

        } catch (Exception e) {
            System.out.println("I can't sleep!!!!");

        }
        System.exit(0);
    }

//    public static void get_ovs_port_name_(String container_name){
//        String cmd = "ovs_vsctl --data=bare --no-heading --columns=name find interface" +
//                "external_ids:container_id=" + container_name + "\n" +
//                "external_ids:container_iface=" + default_container_port_interface_name;
//    }

    private static void create_containers_on_both_hosts() {
        System.out.println("Creating containers on both hosts");
        int i = 1;
        String background_pinger="";
        String background_pingee = "";
        for (String port_ip : ip_mac_map.keySet()) {
            String port_mac = ip_mac_map.get(port_ip);
            String container_name = "test" + Integer.toString(i);
            port_ip_to_container_name.put(port_ip, container_name);
            String create_container_cmd = "docker run -itd --name " + container_name + " --net=none --label test=ACA busybox sh";
            // start time
            String ovs_docker_add_port_cmd = "ovs-docker add-port br-int " + default_container_port_interface_name + " " + container_name + " --ipaddress=" + port_ip + "/16 --macaddress=" + port_mac;
            String ovs_set_vlan_cmd = "ovs-docker set-vlan br-int " + default_container_port_interface_name + " " + container_name + " 1";

            int ip_last_octet = Integer.parseInt(port_ip.split("\\.")[3]);
            if (ip_last_octet % 2 != 0) {
                System.out.println("i = " + i + " , assigning IP: [" + port_ip + "] to node: [" + aca_node_one_ip + "]");
                node_one_port_ips.add(port_ip);
                aca_node_one_create_container_commands.add(create_container_cmd);
                aca_node_one_create_container_commands.add(ovs_docker_add_port_cmd);
                aca_node_one_ovs_docker_commands.add(ovs_set_vlan_cmd);
                port_ip_to_host_ip_map.put(port_ip, aca_node_one_ip);
                background_pinger = port_ip;
            } else {
                System.out.println("i = " + i + " , assigning IP: [" + port_ip + "] to node: [" + aca_node_two_ip + "]");
                node_two_port_ips.add(port_ip);
                aca_node_two_create_container_commands.add(create_container_cmd);
                aca_node_one_create_container_commands.add(ovs_docker_add_port_cmd);
                aca_node_two_ovs_docker_commands.add(ovs_set_vlan_cmd);
                port_ip_to_host_ip_map.put(port_ip, aca_node_two_ip);
                background_pingee = port_ip;
            }
            i++;
        }
        aca_node_one_create_container_commands.add(docker_ps_cmd);
        aca_node_two_create_container_commands.add(docker_ps_cmd);

        execute_ssh_commands(aca_node_one_create_container_commands, aca_node_one_ip, user_name, password);
        execute_ssh_commands(aca_node_two_create_container_commands, aca_node_two_ip, user_name, password);
        backgroupd_ping_start_time = System.currentTimeMillis();;
        System.out.println("DONE creating containers on both hosts, need to start the background pings now.");
        // start the background thread here doing the ping from 1 port to another, util the ping is successful.
        // it pings every 0.001 second, or 1 millisecond
        String background_ping_command = "ping -I " + background_pinger + " -i  0.001 " + background_pingee ;
        System.out.println("Created background ping cmd: " + background_ping_command);
        concurrent_run_cmd c = new concurrent_run_cmd(background_ping_command, aca_node_one_ip, user_name, password);
        taskExecutor.execute(c);

        // After the ping thread was started, execute the other commands, and then keep the Test Controller going
        execute_ssh_commands(aca_node_one_ovs_docker_commands, aca_node_one_ip, user_name, password);
        execute_ssh_commands(aca_node_two_ovs_docker_commands, aca_node_two_ip, user_name, password);
    }


    // Generates IP/MAC for host_many_per_host, and inserts them into the hashmap
    public static void generate_ip_macs(int amount_of_ports_to_generate) {
        int i = 2;
        while (ip_mac_map.size() != amount_of_ports_to_generate) {
            if (i % 100 != 0) {
                String ip_2nd_octet = Integer.toString(i / 10000);
                String ip_3nd_octet = Integer.toString((i % 10000) / 100);
                String ip_4nd_octet = Integer.toString(i % 100);
                String ip_for_port = ips_ports_ip_prefix + "." + ip_2nd_octet + "." + ip_3nd_octet + "." + ip_4nd_octet;
                String mac_for_port = mac_port_prefix + ip_2nd_octet + ":" + ip_3nd_octet + ":" + ip_4nd_octet;
                String id_for_port = port_ip_template + ips_ports_ip_prefix + String.format("%03d", (i / 10000)) + String.format("%03d", ((i % 10000) / 100)) + String.format("%03d", (i % 100));
                System.out.println("Generated Port " + i + " with IP: [" + ip_for_port + "], ID :[ " + id_for_port + "] and MAC: [" + mac_for_port + "]");
                ip_mac_map.put(ip_for_port, mac_for_port);
                port_ip_to_id_map.put(ip_for_port, id_for_port);
            }
            i++;
        }
    }


    public static ArrayList<String> execute_ssh_commands(Vector<String> commands, String host_ip, String host_user_name, String host_password) {
        ArrayList<String> cmd_output = new ArrayList<>();
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
                long start = System.currentTimeMillis();

                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        String line_output = new String (tmp, 0 , i);
                        System.out.print(line_output);
                        cmd_output.add(line_output);
                    }
                    if (channel.isClosed()) {
                        System.out.println("exit-status: " + channel.getExitStatus());
                        break;
                    }
                }
                long end = System.currentTimeMillis();
                System.out.println("End of executing command [" + command + "] on host: " + host_ip +  ", to took " + (end-start) + " milliseconds");
                channel.disconnect();
            }

            session.disconnect();
            System.out.println("DONE");
        } catch (Exception e) {
            System.err.println("Got this error: " + e.getMessage());
            e.printStackTrace();
        }
        return cmd_output;
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
//        Vector<String> cmd_list = new Vector<>();
        System.out.println("Need to execute this command concurrently: [" + this.command_to_run + "]");
//        cmd_list.add(this.command_to_run);
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