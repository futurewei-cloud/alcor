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

import com.futurewei.alcor.dataplane.exception.HostIdNotFound;
import com.futurewei.alcor.dataplane.exception.VpcIdNotFound;
import com.futurewei.alcor.schema.Goalstate.GoalStateV2;
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;

import java.util.*;

public class MulticastGoalStateV2{
    private Set<String> hostIps;
    private Map<String, Set<String>> hostVpcMap;

    private GoalStateV2 goalState;
    private GoalStateV2.Builder goalStateBuilder;

    public MulticastGoalStateV2() {
        hostIps = new HashSet<>();
        goalStateBuilder = GoalStateV2.newBuilder();
    }

    public MulticastGoalStateV2(GoalStateV2 goalState) {
        this.goalState = goalState;
    }

    public MulticastGoalStateV2(Set<String> hostIps, GoalStateV2 goalState) {
        this.hostIps = hostIps;
        this.goalState = goalState;
    }

    public MulticastGoalStateV2(Set<String> hostIps, Map<String, Set<String>> hostVpcMap, GoalStateV2 goalState) {
        this.hostIps = hostIps;
        this.hostVpcMap = hostVpcMap;
        this.goalState = goalState;
    }

    public Set<String> getHostIps() {
        return hostIps;
    }

    public void setHostIps(Set<String> hostIps) {
        this.hostIps = hostIps;
    }

    public void addHostIp(String hostIp) {
        if (this.hostIps == null) {
            this.hostIps = new HashSet<>();
        }
        this.hostIps.add(hostIp);
    }

    public Map<String, Set<String>> getHostVpcMap() {
        return hostVpcMap;
    }

    public void setHostVpcMap(Map<String, Set<String>> hostVpcMap) throws Exception {
        if (hostVpcMap == null) {
            throw new VpcIdNotFound();
        }
        this.hostVpcMap = hostVpcMap;
    }

    public void addHostVpcPair(String hostIp, String vpcId) throws Exception {
        if (hostIp == null) {
            throw new HostIdNotFound();
        }
        if (vpcId == null) {
            throw new VpcIdNotFound();
        }
        if (hostVpcMap.get(hostIp) == null) {
            hostVpcMap.put(hostIp, new HashSet<String>());
        }
        this.hostVpcMap.get(hostIp).add(vpcId);
    }

    public Set<String> getVpcIdSetByHostIp(String hostIp) {
        return hostVpcMap.get(hostIp);
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
}