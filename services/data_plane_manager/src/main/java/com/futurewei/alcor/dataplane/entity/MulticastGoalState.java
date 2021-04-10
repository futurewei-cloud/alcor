/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
