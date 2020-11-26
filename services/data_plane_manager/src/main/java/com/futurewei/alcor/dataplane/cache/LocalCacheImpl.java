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
package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.dataplane.exception.SubnetEntityNotFound;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class LocalCacheImpl implements LocalCache {
    @Autowired
    private SubnetPortsCache subnetPortsCache;

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
                    subnetPorts.setGatewayPortMac(subnetEntity.getGatewayMacAddress());
                    subnetPorts.setGatewayPortIp(subnetEntity.getGatewayIp());

                    //FIXME: get the gateway port id
                    subnetPorts.setGatewayPortId(null);
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
}
