package com.futurewei.alcor.netwconfigmanager.entity;

import com.futurewei.alcor.schema.Goalstate.GoalStateV2;

public class HostGoalState {

    private String hostIp;
    private GoalStateV2 goalState;
    private GoalStateV2.Builder goalStateBuilder;

    public HostGoalState() {
        this.goalStateBuilder = GoalStateV2.newBuilder();
    }

    public HostGoalState(String hostIp, GoalStateV2 goalState) {
        this.hostIp = hostIp;
        this.goalState = goalState;
        this.goalStateBuilder = GoalStateV2.newBuilder();
    }

    public String getHostIp() {
        return this.hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public GoalStateV2 getGoalState() {
        return this.goalState;
    }

    public void setGoalState(GoalStateV2 goalState) {
        this.goalState = goalState;
    }

    public GoalStateV2.Builder getGoalStateBuilder() {
        return this.goalStateBuilder;
    }

    public void setGoalStateBuilder(GoalStateV2.Builder goalStateBuilder) {
        this.goalStateBuilder = goalStateBuilder;
    }

    public void completeGoalStateBuilder(){
        this.goalState = this.goalStateBuilder.build();
    }
}
