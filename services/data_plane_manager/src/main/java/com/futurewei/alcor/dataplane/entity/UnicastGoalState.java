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

import com.futurewei.alcor.schema.Goalstate;

public class UnicastGoalState {
    private String hostIp;
    private String nextTopic;
    private Goalstate.GoalState goalState;

    public UnicastGoalState() {

    }

    public UnicastGoalState(String hostIp, Goalstate.GoalState goalState) {
        this.hostIp = hostIp;
        this.goalState = goalState;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getNextTopic() {
        return nextTopic;
    }

    public void setNextTopic(String nextTopic) {
        this.nextTopic = nextTopic;
    }

    public Goalstate.GoalState getGoalState() {
        return goalState;
    }

    public void setGoalState(Goalstate.GoalState goalState) {
        this.goalState = goalState;
    }
}
