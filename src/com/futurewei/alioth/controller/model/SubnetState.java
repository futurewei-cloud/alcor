package com.futurewei.alioth.controller.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubnetState {
    private String projectId;
    private String vpcId;
    private String id;
    private String name;
    private String cidr;
    private String description;
    private String availabilityZone;
    private String gatewayIp;
    private Boolean dhcpEnable;
    private String primaryDns;
    private String secondaryDns;
    private List<String> dnsList;

    public SubnetState() {}

    public SubnetState(String projectId, String id, String name, String cidr){
        this(projectId, id, name, cidr, null, null, null, false, null, null, null);
    }

    public SubnetState(SubnetState state){
        this(state.projectId, state.id, state.name, state.cidr, state.description, state.availabilityZone, state.gatewayIp, state.dhcpEnable, state.primaryDns, state.secondaryDns, state.dnsList);
    }

    public SubnetState(String projectId, String id, String name, String cidr, String description, String availabilityZone,
                  String gatewayIp, Boolean dhcpEnable, String primaryDns, String secondaryDns, List<String> dnsList){
        this.projectId = projectId;
        this.id = id;
        this.name = name;
        this.cidr = cidr;
        this.description = description;
        this.availabilityZone = availabilityZone;
        this.gatewayIp = gatewayIp;
        this.dhcpEnable = dhcpEnable;
        this.primaryDns = primaryDns;
        this.secondaryDns = secondaryDns;
        this.dnsList = dnsList == null ? null : new ArrayList<>(dnsList);
    }

}
