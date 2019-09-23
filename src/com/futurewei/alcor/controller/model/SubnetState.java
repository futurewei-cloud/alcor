package com.futurewei.alcor.controller.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubnetState extends CustomerResource {

    private String vpcId;
    private String cidr;

    private String availabilityZone;
    private String gatewayIp;
    private Boolean dhcpEnable;
    private String primaryDns;
    private String secondaryDns;
    private List<String> dnsList;

    public SubnetState() {}

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr){
        this(projectId, vpcId, id, name, cidr,null, null, null, false, null, null, null);
    }

    public SubnetState(SubnetState state){
        this(state.getProjectId(), state.getVpcId(), state.getId(), state.getName(), state.getCidr(), state.getDescription(),
                state.getAvailabilityZone(), state.getGatewayIp(), state.getDhcpEnable(), state.getPrimaryDns(), state.getSecondaryDns(), state.getDnsList());
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr, String description, String availabilityZone,
                  String gatewayIp, Boolean dhcpEnable, String primaryDns, String secondaryDns, List<String> dnsList){

        super(projectId, id, name, description);

        this.vpcId = vpcId;
        this.cidr = cidr;
        this.availabilityZone = availabilityZone;
        this.gatewayIp = gatewayIp;
        this.dhcpEnable = dhcpEnable;
        this.primaryDns = primaryDns;
        this.secondaryDns = secondaryDns;
        this.dnsList = dnsList == null ? null : new ArrayList<>(dnsList);
    }

}
