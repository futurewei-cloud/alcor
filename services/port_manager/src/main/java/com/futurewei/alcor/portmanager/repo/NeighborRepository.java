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
package com.futurewei.alcor.portmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NeighborRepository {
    private static final Logger LOG = LoggerFactory.getLogger(NeighborRepository.class);
    private static final String NEIGHBOR_CACHE_NAME_PREFIX = "neighborCache-";

    private CacheFactory cacheFactory;
    private ICache<String, NeighborInfo> neighborCache;
    private ICache<String, HashSet> vpcCache;

    public NeighborRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.neighborCache = cacheFactory.getCache(NeighborInfo.class);
        this.vpcCache = cacheFactory.getCache(HashSet.class);
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

                neighborCache.putAll(neighborMap);
                neighborMap.entrySet().forEach(item -> {
                    try {
                        if (!vpcCache.containsKey(entry.getKey())) {
                            vpcCache.put(entry.getKey(), new HashSet<>());
                        }
                        vpcCache.get(entry.getKey()).add(item.getValue().getPortIp());
                    } catch (CacheException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    public void updateNeighbors(PortEntity oldPortEntity, List<NeighborInfo> newNeighbors) throws Exception {
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
            //Delete old neighborInfos
            for (String oldPortIp: oldPortIps) {
                neighborCache.remove(oldPortIp);
            }
        }
    }


    @DurationStatistics
    public Map<String, NeighborInfo> getNeighbors(String vpcId) throws CacheException {
        Map<String, NeighborInfo> neighbors = new HashMap<>();
        if (vpcCache.containsKey(vpcId)) {
            Set<String> neighborIps = vpcCache.get(vpcId);
            for (String neighborIp : neighborIps) {
                neighbors.put(neighborIp, neighborCache.get(neighborIp));
            }
        }

        return neighbors;
    }
}





