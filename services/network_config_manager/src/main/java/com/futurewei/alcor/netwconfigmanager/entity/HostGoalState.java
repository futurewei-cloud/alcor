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
