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

package com.futurewei.vpcmanager.entity;

import com.futurewei.vpcmanager.comm.logging.Logger;
import com.futurewei.vpcmanager.comm.logging.LoggerFactory;
import com.futurewei.vpcmanager.utils.CommonUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

@Data
public class NodeManager {
    private static int GRPC_SERVER_PORT = 50001;
    private List<HostInfo> nodes;
    private HashMap<String, HostInfo> nodeMap;

    public NodeManager(List<HostInfo> hosts) {
        Logger logger = LoggerFactory.getLogger();
        logger.entering(this.getClass().getName(), "NodeManager(List<HostInfo> hosts)");

        this.nodes = NodeManager.LoadNodes(hosts);
        for (HostInfo host : hosts) {
            System.out.println(host);
            logger.log(Level.INFO, "Log:" + host);
        }
        this.BuildMapFromNodeIdToInfo(this.nodes);
        logger.exiting(this.getClass().getName(), "NodeManager(List<HostInfo> hosts)");

    }

    private static List<HostInfo> LoadNodes(List<HostInfo> hosts) {
        List<HostInfo> hostInfoList = new ArrayList<>(hosts);
        for (int i = 0; i < hostInfoList.size(); i++) {
            HostInfo host = hostInfoList.get(i);
            host.setGRPCServerPort(NodeManager.GRPC_SERVER_PORT);
        }

        return hostInfoList;
    }

    public HostInfo getHostInfoById(String hostId) {
        Logger logger = LoggerFactory.getLogger();
        if (this.nodeMap != null) { //&& !Strings.isNullOrEmpty(hostId)) {
            logger.log(Level.INFO, "Log:" + "[NodeManager] Host id: " + hostId + " info:" + this.nodeMap.get(hostId));
            return this.nodeMap.get(hostId);
        }
        logger.log(Level.WARNING, "[NodeManager] node map is empty");
        return null;
    }

    public HostInfo[] getRandomHosts(int count) {
        HostInfo[] randomHosts = new HostInfo[count];

        for (int i = 0; i < count; i++) {
            int index = CommonUtil.getRandomNumberInRange(0, this.getNodes().size() - 1);
            randomHosts[i] = this.getNodes().get(index);
        }

        return randomHosts;
    }

    private void BuildMapFromNodeIdToInfo(List<HostInfo> hosts) {
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "Entering BuildMapFromNodeIdToInfo");
        if (hosts != null) {
            if (this.nodeMap == null) {
                this.nodeMap = new HashMap<>();
            }

            logger.log(Level.INFO, "hosts size : " + hosts.size());
            for (HostInfo host : hosts) {
                this.nodeMap.put(host.getId(), host);
            }
        }
    }

    public static int getGrpcServerPort() {
        return GRPC_SERVER_PORT;
    }

    public static void setGrpcServerPort(int grpcServerPort) {
        GRPC_SERVER_PORT = grpcServerPort;
    }

    public List<HostInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<HostInfo> nodes) {
        this.nodes = nodes;
    }

    public HashMap<String, HostInfo> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(HashMap<String, HostInfo> nodeMap) {
        this.nodeMap = nodeMap;
    }
}
