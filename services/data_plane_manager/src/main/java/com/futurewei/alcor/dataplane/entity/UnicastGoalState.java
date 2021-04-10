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

import com.futurewei.alcor.schema.Goalstate.GoalState;
import com.futurewei.alcor.web.entity.dataplane.UnicastGoalStateByte;

/**
 * UnicastGoalState contains a goalState object and the destination host ip address hostIp,
 * to which the GoalState object is sent. In order to support the hierarchical topic of pulsar,
 * it also contains the nextTopic field, which is used by pulsar to determine which topic to
 * send the goalState to. The field goalStateBuilder is a temporary object that is used to
 * construct a goalState object that must be cleared before the UnicastGoalState is sent.
 */
public class UnicastGoalState {
    private String hostIp;
    private String nextTopic;
    private GoalState goalState;
    private GoalState.Builder goalStateBuilder;

    public UnicastGoalState() {
        goalStateBuilder = GoalState.newBuilder();
    }

    public UnicastGoalState(String hostIp, GoalState goalState) {
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

    public UnicastGoalStateByte getUnicastGoalStateByte() {
        UnicastGoalStateByte unicastGoalStateByte = new UnicastGoalStateByte();
        unicastGoalStateByte.setNextTopic(this.nextTopic);
        unicastGoalStateByte.setGoalStateByte(this.goalState.toByteArray());

        return unicastGoalStateByte;
    }
}
