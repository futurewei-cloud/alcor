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

import java.io.InputStream;


public class pseudo_controller {

    static String aca_node_one_ip = "ip_one";
    static String aca_node_two_ip = "ip_two";
    static String ncm_ip = "ip_three";
    static int ncp_port = 123;
    static String user_name = "root";
    static String password = "abcdefg";
    static int ports_to_generate_on_each_aca_node = 1;
    static String vpc_id_1 = "2b08a5bc-b718-11ea-b3de-111111111112";
    static String port_id_1 = "11111111-b718-11ea-b3de-111111111112";
    static String port_id_2 = "13333333-b718-11ea-b3de-111111111114";
    static String subnet_id_1 = "27330ae4-b718-11ea-b3df-111111111113";
    public static void main(String[] args){
        System.out.println("Start of the test controller");
        if(args.length == 7){
            System.out.println("User passed in params and we need to read them.");
            ports_to_generate_on_each_aca_node = Integer.parseInt(args[0]);
            aca_node_one_ip = args[1];
            aca_node_two_ip = args[2];
            ncm_ip = args[3];
            ncp_port = Integer.parseInt(args[4]);
            user_name = args[5];
            password = args[6];

        }

        System.out.println("aca_node_one_ip: " + aca_node_one_ip + "\naca_node_two_ip: " + aca_node_two_ip + "\nuser name: "+user_name+"\npassword: "+password);
        execute_ssh_commands("docker run -itd --name test1 --net=none busybox sh", aca_node_one_ip, user_name, password);
        execute_ssh_commands("ovs-docker add-port br-int eth0 test1 --ipaddress=10.0.0.2/16 --macaddress=6c:dd:ee:00:00:02", aca_node_one_ip, user_name, password);
        execute_ssh_commands("ovs-docker set-vlan br-int eth0 test1 1", aca_node_one_ip, user_name, password);
        execute_ssh_commands("docker ps", aca_node_one_ip, user_name, password);
        execute_ssh_commands("docker exec test1 ifconfig", aca_node_one_ip, user_name, password);
        execute_ssh_commands("docker run -itd --name test2 --net=none busybox sh", aca_node_two_ip, user_name, password);
        execute_ssh_commands("ovs-docker add-port br-int eth0 test2 --ipaddress=10.0.0.3/16 --macaddress=6c:dd:ee:00:00:03", aca_node_two_ip, user_name, password);
        execute_ssh_commands("ovs-docker set-vlan br-int eth0 test2 1", aca_node_two_ip, user_name, password);
        execute_ssh_commands("docker ps", aca_node_two_ip, user_name, password);
        execute_ssh_commands("docker exec test2 ifconfig", aca_node_two_ip, user_name, password);



        System.out.println("Containers setup done, now we gotta construct the GoalStateV2");

        System.out.println("Trying to build the GoalStateV2");


        Goalstate.GoalStateV2.Builder GoalState_builder = Goalstate.GoalStateV2.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_one = Goalstate.HostResources.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_two = Goalstate.HostResources.newBuilder();

        // start of setting up port 1 on aca node 1
        Port.PortState.Builder new_port_states = Port.PortState.newBuilder();

        new_port_states.setOperationType(Common.OperationType.CREATE);

        // fill in port state structs for port 1
        Port.PortConfiguration.Builder config = new_port_states.getConfigurationBuilder();
        config.
                setRevisionNumber(2).
                setUpdateType(Common.UpdateType.FULL).
                setId(port_id_1).
                setVpcId(vpc_id_1).
                setName(("tap" + port_id_1).substring(0, 14)).
                setMacAddress("6c:dd:ee:00:00:02");
        Port.PortConfiguration.FixedIp.Builder fixedIpBuilder = Port.PortConfiguration.FixedIp.newBuilder();
        fixedIpBuilder.setSubnetId(subnet_id_1);
        fixedIpBuilder.setIpAddress("10.10.0.2");
        config.addFixedIps(fixedIpBuilder.build());
        Port.PortConfiguration.SecurityGroupId securityGroupId = Port.PortConfiguration.SecurityGroupId.newBuilder().setId("2").build();
        config.addSecurityGroupIds(securityGroupId);

        new_port_states.setConfiguration(config.build());
        System.out.println("Port config builder content for port 1: \n" + new_port_states.getConfiguration().getMacAddress() + "\n");
        Port.PortState port_state_one = new_port_states.build();
        GoalState_builder.putPortStates(port_state_one.getConfiguration().getId(),port_state_one);
        Goalstate.ResourceIdType.Builder port_one_resource_Id_builder = Goalstate.ResourceIdType.newBuilder();
        port_one_resource_Id_builder.setType(Common.ResourceType.PORT).setId(port_state_one.getConfiguration().getId());
        Goalstate.ResourceIdType port_one_resource_id = port_one_resource_Id_builder.build();
//        Goalstate.HostResources.Builder port_one_host_resource_builder = Goalstate.HostResources.newBuilder();
        host_resource_builder_node_one.addResources(port_one_resource_id);
//        Goalstate.HostResources port_one_host_resource = port_one_host_resource_builder.build();
//        GoalState_builder.putHostResources(aca_node_one_ip, port_one_host_resource);

        System.out.println("Finished port state for port 1.");

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

        GoalState_builder.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
        Goalstate.ResourceIdType subnet_resource_id_type = Goalstate.ResourceIdType.newBuilder()
                .setType(Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes.getConfiguration().getId()).build();
        host_resource_builder_node_one.addResources(subnet_resource_id_type);
        host_resource_builder_node_two.addResources(subnet_resource_id_type);
//        Goalstate.HostResources subnet_host_resource = Goalstate.HostResources.newBuilder().addResources(subnet_resource_id_type).build();
//        GoalState_builder.putHostResources(aca_node_one_ip, subnet_host_resource);
//        GoalState_builder.putHostResources(aca_node_two_ip, subnet_host_resource);


        System.out.println("Subnet state is finished, content: \n" + subnet_state_for_both_nodes.getConfiguration().getCidr());

        // add a new neighbor state with CREATE
        Neighbor.NeighborState.Builder new_neighborState_builder = Neighbor.NeighborState.newBuilder();
        new_neighborState_builder.setOperationType(Common.OperationType.CREATE);

        // fill in neighbor state structs of port 3
        Neighbor.NeighborConfiguration.Builder NeighborConfiguration_builder = Neighbor.NeighborConfiguration.newBuilder();
        NeighborConfiguration_builder.setRevisionNumber(2);
        NeighborConfiguration_builder.setVpcId(vpc_id_1);
        NeighborConfiguration_builder.setId(port_id_2);
        NeighborConfiguration_builder.setMacAddress("6c:dd:ee:00:00:03");
        NeighborConfiguration_builder.setHostIpAddress(aca_node_two_ip);

        Neighbor.NeighborConfiguration.FixedIp.Builder neighbor_fixed_ip_builder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
        neighbor_fixed_ip_builder.setNeighborType(Neighbor.NeighborType.L2);
        neighbor_fixed_ip_builder.setSubnetId(subnet_id_1);
        neighbor_fixed_ip_builder.setIpAddress("10.0.0.3");

        NeighborConfiguration_builder.addFixedIps(neighbor_fixed_ip_builder.build());

        new_neighborState_builder.setConfiguration(NeighborConfiguration_builder.build());
        Neighbor.NeighborState neighborState_node_one = new_neighborState_builder.build();
        GoalState_builder.putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
        Goalstate.ResourceIdType resource_id_type_neighbor_node_one = Goalstate.ResourceIdType.newBuilder().
                setType(Common.ResourceType.NEIGHBOR).setId(neighborState_node_one.getConfiguration().getId()).build();
        host_resource_builder_node_one.addResources(resource_id_type_neighbor_node_one);
//        Goalstate.HostResources host_resource_neighbor_node_one = Goalstate.HostResources.newBuilder()
//                .addResources(resource_id_type_neighbor_node_one).build();
//        GoalState_builder.putHostResources(aca_node_one_ip, host_resource_neighbor_node_one);

        // end of setting up port 1 on aca node 1

        // start of setting up port 2 on aca node 2

        Port.PortState.Builder new_port_states_port_2 = Port.PortState.newBuilder();

        new_port_states_port_2.setOperationType(Common.OperationType.CREATE);

        // fill in port state structs for port 2
        Port.PortConfiguration.Builder config_2 = new_port_states_port_2.getConfigurationBuilder();
        config_2.
                setRevisionNumber(2).
                setUpdateType(Common.UpdateType.FULL).
                setId(port_id_2).
                setVpcId(vpc_id_1).
                setName(("tap" + port_id_2).substring(0, 14)).
                setMacAddress("6c:dd:ee:00:00:03");
        Port.PortConfiguration.FixedIp.Builder fixedIpBuilder_port_2 = Port.PortConfiguration.FixedIp.newBuilder();
        fixedIpBuilder_port_2.setSubnetId(subnet_id_1);
        fixedIpBuilder_port_2.setIpAddress("10.10.0.3");
        config_2.addFixedIps(fixedIpBuilder_port_2.build());
        Port.PortConfiguration.SecurityGroupId securityGroupId_port_2 = Port.PortConfiguration.SecurityGroupId.newBuilder().setId("2").build();
        config.addSecurityGroupIds(securityGroupId_port_2);

        new_port_states_port_2.setConfiguration(config_2.build());
        System.out.println("Port config builder content for port 2: \n" + new_port_states_port_2.getConfiguration().getMacAddress() + "\n");
        Port.PortState port_state_two = new_port_states_port_2.build();
        GoalState_builder.putPortStates(port_state_two.getConfiguration().getId(),port_state_two);
        Goalstate.ResourceIdType resource_id_type_port_two = Goalstate.ResourceIdType.newBuilder()
                .setType(Common.ResourceType.PORT).setId(port_state_two.getConfiguration().getId()).build();
        host_resource_builder_node_two.addResources(resource_id_type_port_two);
//        Goalstate.HostResources host_resources_port_two = Goalstate.HostResources.newBuilder()
//                .addResources(resource_id_type_port_two).build();
//        GoalState_builder.putHostResources(aca_node_two_ip, host_resources_port_two);

        System.out.println("Finished port state for port 2.");

        // setting neighbor state of port 1 on node 2

        // add a new neighbor state with CREATE
        Neighbor.NeighborState.Builder new_neighborState_builder_port_2 = Neighbor.NeighborState.newBuilder();
        new_neighborState_builder_port_2.setOperationType(Common.OperationType.CREATE);

        // fill in neighbor state structs of port 3
        Neighbor.NeighborConfiguration.Builder NeighborConfiguration_builder_node_2 = Neighbor.NeighborConfiguration.newBuilder();
        NeighborConfiguration_builder_node_2.setRevisionNumber(2);
        NeighborConfiguration_builder_node_2.setVpcId(vpc_id_1);
        NeighborConfiguration_builder_node_2.setId(port_id_1);
        NeighborConfiguration_builder_node_2.setMacAddress("6c:dd:ee:00:00:02");
        NeighborConfiguration_builder_node_2.setHostIpAddress(aca_node_one_ip);

        Neighbor.NeighborConfiguration.FixedIp.Builder neighbor_fixed_ip_builder_node_2 = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
        neighbor_fixed_ip_builder_node_2.setNeighborType(Neighbor.NeighborType.L2);
        neighbor_fixed_ip_builder_node_2.setSubnetId(subnet_id_1);
        neighbor_fixed_ip_builder_node_2.setIpAddress("10.0.0.2");

        NeighborConfiguration_builder_node_2.addFixedIps(neighbor_fixed_ip_builder_node_2.build());

        new_neighborState_builder_port_2.setConfiguration(NeighborConfiguration_builder_node_2.build());
        Neighbor.NeighborState neighborState_two = new_neighborState_builder_port_2.build();
        GoalState_builder.putNeighborStates(neighborState_two.getConfiguration().getId(), neighborState_two);
        Goalstate.ResourceIdType resource_id_type_neighbor_two = Goalstate.ResourceIdType.newBuilder()
                .setType(Common.ResourceType.NEIGHBOR).setId(neighborState_two.getConfiguration().getId()).build();
        host_resource_builder_node_two.addResources(resource_id_type_neighbor_two);
//        Goalstate.HostResources host_resources_neighbor_two = Goalstate.HostResources.newBuilder()
//                .addResources(resource_id_type_neighbor_two).build();
//        GoalState_builder.putHostResources(aca_node_two_ip, host_resources_neighbor_two);

        // end of setting neighbor state of port 1 on node 2


        // end of setting up port 2 on aca node 2
        GoalState_builder.putHostResources(aca_node_one_ip, host_resource_builder_node_one.build());
        GoalState_builder.putHostResources(aca_node_two_ip, host_resource_builder_node_two.build());
        Goalstate.GoalStateV2 message = GoalState_builder.build();
        System.out.println("Built GoalState successfully, GoalStateV2 content: \n"+message.toString()+"\n");
        System.out.println("Host resources count: " + message.getHostResourcesCount());
        System.out.println("Time to call the GRPC functions");

//        ManagedChannel channel = ManagedChannelBuilder.forAddress(grpc_ip, grpc_port).usePlaintext().build();
//
//        GoalStateProvisionerGrpc.GoalStateProvisionerStub stub = GoalStateProvisionerGrpc.newStub(channel);
////        boolean execute_ping = false;
//        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> message_observer = new StreamObserver<>() {
//            @Override
//            public void onNext(Goalstateprovisioner.GoalStateOperationReply value) {
//                System.out.println("onNext function with this GoalStateOperationReply: \n" + value.toString() +"\n");
////                final boolean grpc_call_successful = value.getOperationStatuses(0).getOperationStatus().equals(Common.OperationStatus.SUCCESS);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                System.out.println("onError function with this GoalStateOperationReply: \n" + t.getMessage() +"\n");
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("onCompleted");
//            }
//        };
//        io.grpc.stub.StreamObserver<Goalstate.GoalStateV2> response_observer = stub.pushGoalStatesStream(message_observer);
//
//        response_observer.onNext(message);
//        response_observer.onCompleted();
//        System.out.println("After the GRPC call, it's time to do the ping test");
//        execute_ssh_commands("docker exec test2 ping -I 10.0.0.3 -c1 10.0.0.2", aca_node_two_ip, user_name, password);
//        execute_ssh_commands("docker exec test1 ping -I 10.0.0.2 -c1 10.0.0.3", aca_node_two_ip, user_name, password);
        System.out.println("Ping test finished, clean up the containers and the ovs-docker commands");
//        execute_ssh_commands("ovs-docker del-port br-int eth0 test2", aca_node_two_ip, user_name, password);
//        execute_ssh_commands("docker rm -f test2", aca_node_two_ip, user_name, password);

//        execute_ssh_commands("ovs-docker del-port br-int eth0 test1", aca_node_one_ip, user_name, password);
//        execute_ssh_commands("docker rm -f test1", aca_node_one_ip, user_name, password);

        System.out.println("End of the test controller");
    }

    public static void execute_ssh_commands(String command, String host_ip, String host_user_name, String host_password){
        try{
            System.out.println("Start of executing command ["+command+"] on host: "+host_ip);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session=jsch.getSession(host_user_name, host_ip, 22);
            session.setPassword(host_password);
            session.setConfig(config);
            session.connect();
            System.out.println("Connected");

            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();
            session.disconnect();
            System.out.println("DONE");
        }catch(Exception e){
            System.err.println("Got this error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("End of executing command ["+command+"] on host: "+host_ip);
    }
}
