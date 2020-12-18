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
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NeighborRepository {
    private static final Logger LOG = LoggerFactory.getLogger(NeighborRepository.class);
    private static final String NEIGHBOR_CACHE_NAME_PREFIX = "neighborCache-";

    private ICache<String, PortNeighbors> neighborCache;
    private CacheFactory cacheFactory;

    public NeighborRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.neighborCache= cacheFactory.getCache(PortNeighbors.class);
    }

    private String getNeighborCacheName(String suffix) {
        return NEIGHBOR_CACHE_NAME_PREFIX + suffix;
    }

    public void createNeighbors(Map<String, List<NeighborInfo>> neighbors) throws Exception {
        if (neighbors != null) {
            for (Map.Entry<String, List<NeighborInfo>> entry : neighbors.entrySet()) {
                Map<String, NeighborInfo> neighborMap = entry.getValue()
                        .stream()
                        .collect(Collectors.toMap(NeighborInfo::getPortIp, Function.identity()));

                ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                        NeighborInfo.class, getNeighborCacheName(entry.getKey()));
                neighborCache.putAll(neighborMap);
            }
        }
    }

    public void updateNeighbors(PortEntity oldPortEntity, List<NeighborInfo> newNeighbors) throws Exception {
        ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                NeighborInfo.class, getNeighborCacheName(oldPortEntity.getVpcId()));

        //Delete old neighborInfos
        if (oldPortEntity.getFixedIps() != null) {
            List<String> oldPortIps = oldPortEntity.getFixedIps().stream()
                    .map(PortEntity.FixedIp::getIpAddress)
                    .collect(Collectors.toList());

            for (String oldPortIp: oldPortIps) {
                neighborCache.remove(oldPortIp);
            }
        }

        //Add new neighborInfos
        if (newNeighbors != null) {
            Map<String, NeighborInfo> neighborMap = newNeighbors
                    .stream()
                    .collect(Collectors.toMap(NeighborInfo::getPortIp, Function.identity()));
            neighborCache.putAll(neighborMap);
        }
    }

    public void deleteNeighbors(PortEntity portEntity) throws Exception {
        if (portEntity.getFixedIps() != null) {
            List<String> oldPortIps = portEntity.getFixedIps().stream()
                    .map(PortEntity.FixedIp::getIpAddress)
                    .collect(Collectors.toList());

            ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                    NeighborInfo.class, getNeighborCacheName(portEntity.getVpcId()));

            //Delete old neighborInfos
            for (String oldPortIp: oldPortIps) {
                neighborCache.remove(oldPortIp);
            }
        }
    }

    @DurationStatistics
    public Map<String, NeighborInfo> getNeighbors(String vpcId) throws CacheException {
        ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                NeighborInfo.class, getNeighborCacheName(vpcId));
        return neighborCache.getAll();
    }
}
