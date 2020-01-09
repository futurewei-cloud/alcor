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

package com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt;

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.utilities.Common;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class NodeManager {

    private static int GRPC_SERVER_PORT = 50001;
    private List<HostInfo> nodes;
    private HashMap<String, HostInfo> nodeMap;

    public NodeManager(List<HostInfo> hosts) {
        this.nodes = NodeManager.AssignNodes(hosts);
        this.BuildMapFromNodeIdToInfo(this.nodes);
    }

    private static List<HostInfo> AssignNodes(List<HostInfo> hosts) {
        for (int i = 0; i < hosts.size(); i++) {
            hosts.get(i).setGRPCServerPort(NodeManager.GRPC_SERVER_PORT);
        }

        return new ArrayList<>(hosts);
    }

    public HostInfo getHostInfoById(String hostId) {
        if (this.nodeMap != null) { //&& !Strings.isNullOrEmpty(hostId)) {
            System.out.println("host id: " + hostId);
            return this.nodeMap.get(hostId);
        }

        System.out.println("[NodeManager] node map is empty");
        return null;
    }

    public HostInfo[] getRandomHosts(int count) {
        HostInfo[] randomHosts = new HostInfo[count];

        for (int i = 0; i < count; i++) {
            int index = Common.getRandomNumberInRange(0, this.getNodes().size() - 1);
            randomHosts[i] = this.getNodes().get(index);
        }

        return randomHosts;
    }

    private void BuildMapFromNodeIdToInfo(List<HostInfo> hosts) {
        System.out.println("Entering BuildMapFromNodeIdToInfo");
        if (hosts != null) {
            if (this.nodeMap == null) {
                this.nodeMap = new HashMap<>();
            }

            System.out.println("hosts size : " + hosts.size());
            for (HostInfo host : hosts) {
                this.nodeMap.put(host.getId(), host);
            }
        }
    }
}
