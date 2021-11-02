/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.schema.Goalstate.GoalStateV2;
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;

import java.util.ArrayList;
import java.util.List;

public class MulticastGoalStateV2 {
    private List<String> hostIps;
    private List<String> vpcIds;
    private List<String> topics;
    private List<String> subTopics;

    private GoalStateV2 goalState;
    private GoalStateV2.Builder goalStateBuilder;

    public MulticastGoalStateV2() {
        hostIps = new ArrayList<>();
        goalStateBuilder = GoalStateV2.newBuilder();
    }

    public MulticastGoalStateV2(List<String> hostIps, GoalStateV2 goalState) {
        this.hostIps = hostIps;
        this.goalState = goalState;
    }

    public MulticastGoalStateV2(List<String> hostIps, List<String> vpcIds, GoalStateV2 goalState) {
        this.hostIps = hostIps;
        this.vpcIds = vpcIds;
        this.goalState = goalState;
    }

    public List<String> getHostIps() {
        return hostIps;
    }

    public void setHostIps(List<String> hostIps) {
        this.hostIps = hostIps;
    }

    public List<String> getVpcIds() {
        return vpcIds;
    }

    public void setVpcIds(List<String> vpcIds) {
        this.vpcIds = vpcIds;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getSubTopics() {
        return subTopics;
    }

    public void setSubTopics(List<String> subTopics) {
        this.subTopics = subTopics;
    }

    public GoalStateV2 getGoalState() {
        return goalState;
    }

    public void setGoalState(GoalStateV2 goalState) {
        this.goalState = goalState;
    }

    public GoalStateV2.Builder getGoalStateBuilder() {
        return goalStateBuilder;
    }

    public void setGoalStateBuilder(GoalStateV2.Builder goalStateBuilder) {
        this.goalStateBuilder = goalStateBuilder;
    }

    public MulticastGoalStateByte getMulticastGoalStateByte() {
        MulticastGoalStateByte multicastGoalStateByte = new MulticastGoalStateByte();
        multicastGoalStateByte.setNextTopics(this.topics);
        multicastGoalStateByte.setNextSubTopics(this.subTopics);
        multicastGoalStateByte.setGoalStateByte(this.goalState.toByteArray());

        return multicastGoalStateByte;
    }
}