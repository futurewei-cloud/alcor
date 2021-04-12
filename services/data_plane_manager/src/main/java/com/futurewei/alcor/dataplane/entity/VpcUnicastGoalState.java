package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.schema.Goalstate;

public class VpcUnicastGoalState extends UnicastGoalState {
    private String vpcId;

    public VpcUnicastGoalState(String vpcId) {
        super();
        this.vpcId = vpcId;
    }

    public VpcUnicastGoalState(String vpcId, String hostIp, Goalstate.GoalState goalState) {
        super(hostIp, goalState);
        this.vpcId = vpcId;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }
}
