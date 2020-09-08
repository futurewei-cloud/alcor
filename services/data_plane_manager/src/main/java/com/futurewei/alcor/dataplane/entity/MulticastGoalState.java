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

import java.util.List;

public class MulticastGoalState {
    private List<String> hostIps;
    private List<String> nextTopics;
    private Goalstate.GoalState goalState;

    public MulticastGoalState() {

    }

    public MulticastGoalState(List<String> hostIps, Goalstate.GoalState goalState) {
        this.hostIps = hostIps;
        this.goalState = goalState;
    }

    public List<String> getHostIps() {
        return hostIps;
    }

    public void setHostIps(List<String> hostIps) {
        this.hostIps = hostIps;
    }

    public List<String> getNextTopics() {
        return nextTopics;
    }

    public void setNextTopics(List<String> nextTopics) {
        this.nextTopics = nextTopics;
    }

    public Goalstate.GoalState getGoalState() {
        return goalState;
    }

    public void setGoalState(Goalstate.GoalState goalState) {
        this.goalState = goalState;
    }
}
