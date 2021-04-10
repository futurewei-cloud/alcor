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

package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.dataplane.exception.SubnetEntityNotFound;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class LocalCacheImpl implements LocalCache {
    private static final Logger LOG = LoggerFactory.getLogger(LocalCacheImpl.class);

    @Autowired
    private SubnetPortsCache subnetPortsCache;

    @Autowired
    private NodeInfoCache nodeInfoCache;

    @Override
    public void addSubnetPorts(NetworkConfiguration networkConfig) throws Exception {
        List<InternalPortEntity> portEntities = networkConfig.getPortEntities();
        if (portEntities == null) {
            return;
        }

        Map<String, InternalSubnetPorts> subnetPortsMap = new HashMap<>();
        for (InternalPortEntity portEntity: portEntities) {
            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            if (fixedIps == null) {
                LOG.error("Fixed ip of port entity not found");
                continue;
            }

            for (PortEntity.FixedIp fixedIp: fixedIps) {
                String subnetId = fixedIp.getSubnetId();

                PortHostInfo portHostInfo = new PortHostInfo();
                portHostInfo.setPortMac(portEntity.getMacAddress());
                portHostInfo.setPortIp(fixedIp.getIpAddress());
                portHostInfo.setPortId(portEntity.getId());
                portHostInfo.setHostIp(portEntity.getBindingHostIP());
                portHostInfo.setHostId(portEntity.getBindingHostId());

                InternalSubnetPorts subnetPorts = subnetPortsMap.get(subnetId);
                if (subnetPorts == null) {
                    SubnetEntity subnetEntity = getSubnetEntity(networkConfig, fixedIp.getSubnetId());
                    subnetPorts = new InternalSubnetPorts();
                    subnetPorts.setSubnetId(subnetId);
                    subnetPorts.setGatewayPortMac(subnetEntity.getGatewayPortDetail().getGatewayMacAddress());
                    subnetPorts.setGatewayPortIp(subnetEntity.getGatewayIp());
                    subnetPorts.setGatewayPortId(subnetEntity.getGatewayPortDetail().getGatewayPortId());
                    subnetPorts.setPorts(new ArrayList<>());

                    subnetPortsMap.put(subnetId, subnetPorts);
                }

                subnetPorts.getPorts().add(portHostInfo);
            }
        }

        for (Map.Entry<String, InternalSubnetPorts> entry: subnetPortsMap.entrySet()) {
            subnetPortsCache.updateSubnetPorts(entry.getValue());
        }
    }

    private SubnetEntity getSubnetEntity(NetworkConfiguration networkConfig, String subnetId) throws Exception {
        List<InternalSubnetEntity> subnetEntities = networkConfig.getSubnets();
        if (subnetEntities == null) {
            throw new SubnetEntityNotFound();
        }

        for (SubnetEntity subnetEntity: subnetEntities) {
            if (subnetId.equals(subnetEntity.getId())) {
                return subnetEntity;
            }
        }

        throw new SubnetEntityNotFound();
    }

    @Override
    public void updateSubnetPorts(NetworkConfiguration networkConfig) throws Exception {

    }

    @Override
    public void deleteSubnetPorts(NetworkConfiguration networkConfig) {

    }

    @Override
    public InternalSubnetPorts getSubnetPorts(String subnetId) throws Exception {
        return subnetPortsCache.getSubnetPorts(subnetId);
    }

    @Override
    public void updateLocalCache(NetworkConfiguration networkConfig) throws Exception {
        OperationType opType = networkConfig.getOpType();
        switch (opType) {
            case CREATE:
                addSubnetPorts(networkConfig);
                break;
            case UPDATE:
                updateSubnetPorts(networkConfig);
                break;
            case DELETE:
                deleteSubnetPorts(networkConfig);
                break;
            default:
                LOG.error("Update SubnetPorts failed: Unknown operation type");
        }
    }

    @Override
    public void addNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.addNodeInfo(nodeInfo);
    }

    @Override
    public void addNodeInfoBulk(List<NodeInfo> nodeInfos) throws Exception {
        nodeInfoCache.addNodeInfoBulk(nodeInfos);
    }

    @Override
    public void updateNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.updateNodeInfo(nodeInfo);
    }

    @Override
    public void deleteNodeInfo(String nodeId) throws Exception {
        nodeInfoCache.deleteNodeInfo(nodeId);
    }

    @Override
    public NodeInfo getNodeInfo(String nodeId) throws Exception {
        return nodeInfoCache.getNodeInfo(nodeId);
    }

    @Override
    public List<NodeInfo> getNodeInfoByNodeIp(String nodeIp) throws Exception {
        return nodeInfoCache.getNodeInfoByNodeIp(nodeIp);
    }
}
