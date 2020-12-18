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
package com.futurewei.alcor.portmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.portmanager.entity.SubnetPortIds;
import com.futurewei.alcor.portmanager.exception.FixedIpsInvalid;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SubnetPortsRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SubnetPortsRepository.class);
    private static final String GATEWAY_PORT_DEVICE_OWNER = "network:router_interface";

    private CacheFactory cacheFactory;
    private ICache<String, SubnetPortIds> subnetPortIdsCache;

    public SubnetPortsRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.subnetPortIdsCache= cacheFactory.getCache(SubnetPortIds.class);
    }

    private List<SubnetPortIds> getSubnetPortIds(List<PortEntity> portEntities) {
        Map<String, SubnetPortIds> subnetPortIdsMap = new HashMap<>();
        for (PortEntity portEntity: portEntities) {
            if (GATEWAY_PORT_DEVICE_OWNER.equals(portEntity.getDeviceOwner())) {
                continue;
            }

            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            if (fixedIps == null) {
                LOG.warn("Port:{} has no ip address", portEntity.getId());
                continue;
            }

            for (PortEntity.FixedIp fixedIp: fixedIps) {
                String subnetId = fixedIp.getSubnetId();
                if (!subnetPortIdsMap.containsKey(subnetId)) {
                    SubnetPortIds subnetPortIds = new SubnetPortIds(subnetId, new HashSet<>());
                    subnetPortIdsMap.put(subnetId, subnetPortIds);
                }

                subnetPortIdsMap.get(subnetId).getPortIds().add(portEntity.getId());
            }
        }

        return new ArrayList<>(subnetPortIdsMap.values());
    }


    public void addSubnetPortIds(List<PortEntity> portEntities) throws Exception {
        //Store the mapping between subnet id and port id
        List<SubnetPortIds> subnetPortIdsList = getSubnetPortIds(portEntities);

        for (SubnetPortIds item: subnetPortIdsList) {
            String subnetId = item.getSubnetId();
            Set<String> portIds = item.getPortIds();

            SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
            if (subnetPortIds == null) {
                subnetPortIds = new SubnetPortIds(subnetId, new HashSet<>(portIds));
            } else {
                subnetPortIds.getPortIds().addAll(portIds);
            }

            subnetPortIdsCache.put(subnetId, subnetPortIds);
        }
    }

    public void updateSubnetPortIds(PortEntity oldPortEntity, PortEntity newPortEntity) throws Exception {
        //Update Subnet port ids cache
        if (oldPortEntity.getFixedIps() == null || newPortEntity.getFixedIps() == null) {
            LOG.error("Can not find fixed ip in port entity");
            throw new FixedIpsInvalid();
        }

        List<String> oldSubnetIds = oldPortEntity.getFixedIps().stream()
                .map(PortEntity.FixedIp::getSubnetId)
                .collect(Collectors.toList());

        List<String> newSubnetIds = oldPortEntity.getFixedIps().stream()
                .map(PortEntity.FixedIp::getSubnetId)
                .collect(Collectors.toList());

        if (!oldSubnetIds.equals(newSubnetIds)) {
            //Delete port ids from subnetPortIdsCache
            for (String subnetId: oldSubnetIds) {
                SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
                if (subnetPortIds != null) {
                    subnetPortIds.getPortIds().remove(oldPortEntity.getId());
                    subnetPortIdsCache.put(subnetId, subnetPortIds);
                }
            }

            //Add new port ids to subnetPortIdsCache
            for (String subnetId: newSubnetIds) {
                SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
                if (subnetPortIds != null) {
                    subnetPortIds.getPortIds().add(newPortEntity.getId());
                } else {
                    Set<String> portIds = new HashSet<>();
                    portIds.add(newPortEntity.getId());
                    subnetPortIds = new SubnetPortIds(subnetId, portIds);
                }
                subnetPortIdsCache.put(subnetId, subnetPortIds);
            }
        }
    }

    public void deleteSubnetPortIds(PortEntity portEntity) throws Exception {
        if (portEntity.getFixedIps() == null) {
            LOG.error("Can not find fixed ip in port entity");
            throw new FixedIpsInvalid();
        }

        List<String> subnetIds = portEntity.getFixedIps().stream()
                .map(PortEntity.FixedIp::getSubnetId)
                .collect(Collectors.toList());

        //Delete port ids from subnetPortIdsCache
        for (String subnetId: subnetIds) {
            SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
            if (subnetPortIds != null) {
                subnetPortIds.getPortIds().remove(portEntity.getId());
                subnetPortIdsCache.put(subnetId, subnetPortIds);
            }
        }
    }

    @DurationStatistics
    public int getSubnetPortNumber(String subnetId) throws CacheException {
        SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
        if (subnetPortIds == null) {
            return 0;
        }

        return subnetPortIds.getPortIds().size();
    }
}
