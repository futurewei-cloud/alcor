/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.schema.Goalstate.GoalState;

public class HostGoalState {
    private String hostIp;
    private GoalState goalState;

    public HostGoalState() {

    }

    public HostGoalState(String hostIp, GoalState goalState) {
        this.hostIp = hostIp;
        this.goalState = goalState;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public GoalState getGoalState() {
        return goalState;
    }

    public void setGoalState(GoalState goalState) {
        this.goalState = goalState;
    }
}
