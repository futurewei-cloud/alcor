package com.futurewei.alcor.web.entity.gsinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.port.PortState;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

public class GoalStateForNorth extends CustomerResource {
    public boolean isFastPath() {
        return isFastPath;
    }

    public void setFastPath(boolean fastPath) {
        isFastPath = fastPath;
    }

    private boolean isFastPath=false;

    public Common.ResourceType getRsType() {
        return rsType;
    }

    public void setRsType(Common.ResourceType rsType) {
        this.rsType = rsType;
    }

    private Common.ResourceType rsType;
    private Common.OperationType opType;

    public Common.OperationType getOpType() {
        return opType;
    }

    public void setOpType(Common.OperationType opType) {
        this.opType = opType;
    }

    public boolean isAllOrNone() {
        return allOrNone;
    }

    public void setAllOrNone(boolean allOrNone) {
        this.allOrNone = allOrNone;
    }

    boolean allOrNone=true;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("bridge_name")
    private String bridgeName;

    @JsonProperty("port_state")
    private PortState[] portStates;

    @JsonProperty("vpc")
    private VpcEntity[] vpcs;

    public VpcEntity[] getVpcs() {
        return vpcs;
    }

    public void setVpcs(VpcEntity[] vpcs) {
        this.vpcs = vpcs;
    }

    public SubnetEntity[] getSubnets() {
        return subnets;
    }

    public void setSubnets(SubnetEntity[] subnets) {
        this.subnets = subnets;
    }

    @JsonProperty("subnet")
    private SubnetEntity[] subnets;

    public PortState[] getPortStates() {
        return portStates;
    }

    public void setPortStates(PortState[] portState) {
        this.portStates = portState;
    }

    public HostInfoNorth[] getHostInfoNorth() {
        return hostInfoNorths;
    }

    public void setHostInfoNorth(HostInfoNorth[] hostInfoNorths) {
        this.hostInfoNorths = hostInfoNorths;
    }

    @JsonProperty("host_info")
    private HostInfoNorth[] hostInfoNorths;
}
