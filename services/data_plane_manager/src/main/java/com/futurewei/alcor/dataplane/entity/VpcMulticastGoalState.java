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
public class VpcMulticastGoalState extends MulticastGoalState{
    private List<String> vpcIds;

    public List<String> getVpcIds() {
        return vpcIds;
    }

    public void setVpcIds(List<String> vpcIds) {
        this.vpcIds = vpcIds;
    }

    public VpcMulticastGoalState() {
        super();
        this.vpcIds = new ArrayList<>();
    }

    public VpcMulticastGoalState(List<String> vpcIds, List<String> hostIps, GoalState goalState) {
        super(hostIps, goalState);
        this.vpcIds = vpcIds;
    }
}
