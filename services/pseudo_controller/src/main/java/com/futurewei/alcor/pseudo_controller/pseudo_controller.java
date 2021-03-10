/*
This is the code for the test controller, for testing the reactions between the Network Configuration manager and
the ACA.

Params:
1. Number of ports to generate to each aca node
2. IP of aca_node_one
3. IP of aca_node_two
4. User name of aca_nodes
5. Password of aca_nodes
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

import java.io.InputStream;
import java.io.OutputStream;


public class pseudo_controller {

    static String aca_node_one_ip = "ip_one";
    static String aca_node_two_ip = "ip_two";
    static String user_name = "root";
    static String password = "abcdefg";
    static int ports_to_generate_on_each_aca_node = 1;
    static String vpc_id_1 = "2b08a5bc-b718-11ea-b3de-111111111112";
    static String port_id_1 = "11111111-b718-11ea-b3de-111111111112";
    static String port_id_2 = "13333333-b718-11ea-b3de-111111111114";
    static String subnet_id_1 = "27330ae4-b718-11ea-b3df-111111111113";
    public static void main(String[] args){
        System.out.println("Start of the test controller");
        if(args.length == 5){
            System.out.println("User passed in params and we need to read them.");
            ports_to_generate_on_each_aca_node = Integer.parseInt(args[0]);
            aca_node_one_ip = args[1];
            aca_node_two_ip = args[2];
            user_name = args[3];
            password = args[4];
        }

        System.out.println("aca_node_one_ip: " + aca_node_one_ip + "\naca_node_two_ip: " + aca_node_two_ip + "\nuser name: "+user_name+"\npassword: "+password);
//        execute_ssh_commands("docker run -itd --name test1 --net=none busybox sh", aca_node_one_ip, user_name, password);
//        execute_ssh_commands("ovs-docker add-port br-int eth0 test1 --ipaddress=10.0.0.2/16 --macaddress=6c:dd:ee:00:00:02 && ovs-docker set-vlan br-int eth0 test1 1", aca_node_one_ip, user_name, password);
//
//        execute_ssh_commands("docker ps", aca_node_one_ip, user_name, password);
//        execute_ssh_commands("docker run -itd --name test2 --net=none busybox sh", aca_node_two_ip, user_name, password);
//        execute_ssh_commands("ovs-docker add-port br-int eth0 test2 --ipaddress=10.0.0.3/16 --macaddress=6c:dd:ee:00:00:03 && ovs-docker set-vlan br-int eth0 test2 1", aca_node_two_ip, user_name, password);
//
//        execute_ssh_commands("docker ps", aca_node_two_ip, user_name, password);


        System.out.println("Containers setup done, now we gotta construct the GoalStateV2");

        System.out.println("Trying to build the goalstatev2");


        Goalstate.GoalStateV2.Builder GoalState_builder = Goalstate.GoalStateV2.newBuilder();

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

        GoalState_builder.putPortStates(aca_node_one_ip+"-2",new_port_states.build());

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

        GoalState_builder.putSubnetStates(aca_node_one_ip+"-1", subnet_state_for_both_nodes);
        GoalState_builder.putSubnetStates(aca_node_two_ip+"-1", subnet_state_for_both_nodes);

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

        GoalState_builder.putNeighborStates(aca_node_one_ip+"-3", new_neighborState_builder.build());

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

        new_port_states.setConfiguration(config_2.build());
        System.out.println("Port config builder content for port 2: \n" + new_port_states_port_2.getConfiguration().getMacAddress() + "\n");

        GoalState_builder.putPortStates(aca_node_two_ip+"-2",new_port_states.build());

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

        GoalState_builder.putNeighborStates(aca_node_two_ip+"-3", new_neighborState_builder_port_2.build());
        // end of setting neighbor state of port 1 on node 2


        // end of setting up port 2 on aca node 2

        Goalstate.GoalStateV2 message = GoalState_builder.build();
        System.out.println("Built GoalState successfully, GoalStateV2 content: \n"+message.toString()+"\n");

        System.out.println("Time to call the GRPC functions");

//        ManagedChannel channel = ManagedChannelBuilder.forAddress(aca_node_one_ip, 123).usePlaintext().build();
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



        System.out.println("End of the test controller");
    }

    public static void execute_ssh_commands(String command, String host_ip, String host_user_name, String host_password){
        try{

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

    }
}
