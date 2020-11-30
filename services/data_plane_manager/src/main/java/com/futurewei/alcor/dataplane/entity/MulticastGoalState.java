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
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;

import java.util.ArrayList;
import java.util.List;

/**
 * MulticastGoalState represents the GoalState that needs to be sent by multicast,
 * that is the same GoalState message needs to be sent to multiple host, MulticastGoalState
 * including a goalState object and multiple destination host ip addresses to which
 * the GoalState object is sent. It also contains a nextTopics field to support
 * the hierarchical topic of pulsar, which is used by pulsar to decide which topics to
 * send goalState to. The field goalStateBuilder is a temporary object that is used to
 * construct a goalState object that must be cleared before the MulticastGoalState is sent.
 */
public class MulticastGoalState {
    private List<String> hostIps;
    private List<String> nextTopics;
    private GoalState goalState;
    private GoalState.Builder goalStateBuilder;

    public MulticastGoalState() {
        hostIps = new ArrayList<>();
        goalStateBuilder = GoalState.newBuilder();
    }

    public MulticastGoalState(List<String> hostIps, GoalState goalState) {
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

    public GoalState getGoalState() {
        return goalState;
    }

    public GoalState.Builder getGoalStateBuilder() {
        return goalStateBuilder;
    }

    public void setGoalStateBuilder(GoalState.Builder goalStateBuilder) {
        this.goalStateBuilder = goalStateBuilder;
    }

    public void setGoalState(GoalState goalState) {
        this.goalState = goalState;
    }

    public MulticastGoalStateByte getMulticastGoalStateByte() {
        MulticastGoalStateByte multicastGoalStateByte = new MulticastGoalStateByte();
        multicastGoalStateByte.setNextTopics(this.nextTopics);
        multicastGoalStateByte.setGoalStateByte(this.goalState.toByteArray());

        return multicastGoalStateByte;
    }
}
