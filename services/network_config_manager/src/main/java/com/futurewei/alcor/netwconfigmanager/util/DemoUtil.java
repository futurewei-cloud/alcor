package com.futurewei.alcor.netwconfigmanager.util;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.schema.*;

import java.util.Map;

public class DemoUtil {

    final public static String aca_node_one_ip = "10.213.43.92";
    final public static String aca_node_two_ip = "10.213.43.93";
    final public static String vpc_id_1 = "2b08a5bc-b718-11ea-b3de-111111111112";
    final public static String port_id_1 = "11111111-b718-11ea-b3de-111111111112";
    final public static String port_id_2 = "13333333-b718-11ea-b3de-111111111114";
    final public static String subnet_id_1 = "27330ae4-b718-11ea-b3df-111111111113";

    public static void populateHostGoalState(Map<String, HostGoalState> hostGoalStates){

        System.out.println("Trying to build the GoalStateV2");

        Goalstate.GoalStateV2.Builder GoalState_builder = Goalstate.GoalStateV2.newBuilder();
        Goalstate.GoalStateV2.Builder GoalState_builder_two = Goalstate.GoalStateV2.newBuilder();
        Goalstate.GoalStateV2.Builder GoalState_builder_one_neighbor = Goalstate.GoalStateV2.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_one = Goalstate.HostResources.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_two = Goalstate.HostResources.newBuilder();
        Goalstate.HostResources.Builder host_resource_builder_node_one_port_one_neighbor = Goalstate.HostResources.newBuilder();

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
                setAdminStateUp(true).
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
        host_resource_builder_node_one.addResources(port_one_resource_id);


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
        GoalState_builder_two.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
        GoalState_builder_one_neighbor.putSubnetStates(subnet_state_for_both_nodes.getConfiguration().getId(), subnet_state_for_both_nodes);
        Goalstate.ResourceIdType subnet_resource_id_type = Goalstate.ResourceIdType.newBuilder()
                .setType(Common.ResourceType.SUBNET).setId(subnet_state_for_both_nodes.getConfiguration().getId()).build();
        host_resource_builder_node_one.addResources(subnet_resource_id_type);
        host_resource_builder_node_two.addResources(subnet_resource_id_type);
        host_resource_builder_node_one_port_one_neighbor.addResources(subnet_resource_id_type);


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
        GoalState_builder_two.putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
        GoalState_builder_one_neighbor.putNeighborStates(neighborState_node_one.getConfiguration().getId(), neighborState_node_one);
        Goalstate.ResourceIdType resource_id_type_neighbor_node_one = Goalstate.ResourceIdType.newBuilder().
                setType(Common.ResourceType.NEIGHBOR).setId(neighborState_node_one.getConfiguration().getId()).build();
        host_resource_builder_node_one_port_one_neighbor.addResources(resource_id_type_neighbor_node_one);

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
                setAdminStateUp(true).
                setMacAddress("6c:dd:ee:00:00:03");
        Port.PortConfiguration.FixedIp.Builder fixedIpBuilder_port_2 = Port.PortConfiguration.FixedIp.newBuilder();
        fixedIpBuilder_port_2.setSubnetId(subnet_id_1);
        fixedIpBuilder_port_2.setIpAddress("10.10.0.3");
        config_2.addFixedIps(fixedIpBuilder_port_2.build());
        Port.PortConfiguration.SecurityGroupId securityGroupId_port_2 = Port.PortConfiguration.SecurityGroupId.newBuilder().setId("2").build();
        config_2.addSecurityGroupIds(securityGroupId_port_2);

        new_port_states_port_2.setConfiguration(config_2.build());
        System.out.println("Port config builder content for port 2: \n" + new_port_states_port_2.getConfiguration().getMacAddress() + "\n");
        Port.PortState port_state_two = new_port_states_port_2.build();
        GoalState_builder_two.putPortStates(port_state_two.getConfiguration().getId(),port_state_two);
        Goalstate.ResourceIdType resource_id_type_port_two = Goalstate.ResourceIdType.newBuilder()
                .setType(Common.ResourceType.PORT).setId(port_state_two.getConfiguration().getId()).build();
        host_resource_builder_node_two.addResources(resource_id_type_port_two);

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
        GoalState_builder_two.putNeighborStates(neighborState_two.getConfiguration().getId(), neighborState_two);
        Goalstate.ResourceIdType resource_id_type_neighbor_two = Goalstate.ResourceIdType.newBuilder()
                .setType(Common.ResourceType.NEIGHBOR).setId(neighborState_two.getConfiguration().getId()).build();
        host_resource_builder_node_two.addResources(resource_id_type_neighbor_two);

        // end of setting neighbor state of port 1 on node 2


        // end of setting up port 2 on aca node 2
        GoalState_builder.putHostResources(aca_node_one_ip, host_resource_builder_node_one.build());
        GoalState_builder_two.putHostResources(aca_node_two_ip, host_resource_builder_node_two.build());
        GoalState_builder_two.putHostResources(aca_node_one_ip, host_resource_builder_node_one_port_one_neighbor.build());
        GoalState_builder_one_neighbor.putHostResources(aca_node_one_ip, host_resource_builder_node_one_port_one_neighbor.build());
        Goalstate.GoalStateV2 message_one = GoalState_builder.build();
        Goalstate.GoalStateV2 message_two = GoalState_builder_two.build();
        Goalstate.GoalStateV2 message_one_neighbor = GoalState_builder_one_neighbor.build();

//        System.out.println("Built GoalState successfully, GoalStateV2 content for PORT1: \n"+message_one.toString()+"\n");
//        System.out.println("Built GoalState successfully, GoalStateV2 content for PORT2: \n"+message_two.toString()+"\n");
//        System.out.println("Built GoalState successfully, GoalStateV2 content for PORT1+Neighbor: \n"+message_one_neighbor.toString()+"\n");
//
        HostGoalState hostGoalState = new HostGoalState();
        hostGoalState.setHostIp(aca_node_one_ip);
        hostGoalState.setGoalState(message_one_neighbor);
        hostGoalStates.put(aca_node_one_ip, hostGoalState);
    }
}
