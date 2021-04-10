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

package com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.utilities.Common;
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
            int index = Common.getRandomNumberInRange(0, this.getNodes().size() - 1);
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
}
