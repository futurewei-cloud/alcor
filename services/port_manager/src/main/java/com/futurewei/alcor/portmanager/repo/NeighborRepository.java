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

    private ICache<String, PortNeighbors> neighborCache;
    private ICache<String, String> vpcIdProjectId;
    private CacheFactory cacheFactory;

    public NeighborRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.vpcIdProjectId = cacheFactory.getCache(String.class);
        this.neighborCache= cacheFactory.getCache(PortNeighbors.class);
    }

    private String getNeighborCacheName(String suffix) {
        return NEIGHBOR_CACHE_NAME_PREFIX + suffix;
    }

    public void createNeighbors(String projectId, Map<String, List<NeighborInfo>> neighbors) throws Exception {
        if (neighbors != null) {
            for (Map.Entry<String, List<NeighborInfo>> entry : neighbors.entrySet()) {
                Map<String, NeighborInfo> neighborMap = entry.getValue()
                        .stream()
                        .collect(Collectors.toMap(neighbor -> neighbor.getVpcId() + "/" + neighbor.getPortIp(), Function.identity()));
                SortedMap neighborsortMap = new TreeMap(neighborMap);
                CacheConfiguration cfg = CommonUtil.getCacheConfiguration(getNeighborCacheName(projectId));
                ICache<String, NeighborInfo> neighborCache = cacheFactory.getCache(NeighborInfo.class, cfg);
                neighborCache.putAll(neighborsortMap);

            }
        }
    }

    public void updateNeighbors(PortEntity oldPortEntity, List<NeighborInfo> newNeighbors) throws Exception {
        String vpcId = oldPortEntity.getVpcId();
        CacheConfiguration cfg = CommonUtil.getCacheConfiguration(getNeighborCacheName(oldPortEntity.getProjectId()));
        ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                NeighborInfo.class, cfg);

        //Delete old neighborInfos
        if (oldPortEntity.getFixedIps() != null) {
            List<String> oldPortIps = oldPortEntity.getFixedIps().stream()
                    .map(PortEntity.FixedIp::getIpAddress)
                    .collect(Collectors.toList());

            for (String oldPortIp: oldPortIps) {
                neighborCache.remove(vpcId + "/" + oldPortIp);
            }
        }

        //Add new neighborInfos
        if (newNeighbors != null) {
            Map<String, NeighborInfo> neighborMap = newNeighbors
                    .stream()
                    .collect(Collectors.toMap(neighbor -> neighbor.getVpcId() + "/" + neighbor.getPortIp(), Function.identity()));
            SortedMap neighborsortMap = new TreeMap(neighborMap);
            neighborCache.putAll(neighborsortMap);
        }
    }

    public void deleteNeighbors(PortEntity portEntity) throws Exception {
        if (portEntity.getFixedIps() != null) {
            String vpcId = portEntity.getVpcId();
            List<String> oldPortIps = portEntity.getFixedIps().stream()
                    .map(PortEntity.FixedIp::getIpAddress)
                    .collect(Collectors.toList());

            CacheConfiguration cfg = CommonUtil.getCacheConfiguration(getNeighborCacheName(portEntity.getProjectId()));
            ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                    NeighborInfo.class, cfg);

            //Delete old neighborInfos
            for (String oldPortIp: oldPortIps) {
                neighborCache.remove(vpcId + "/" + oldPortIp);
            }
        }
    }

    @DurationStatistics
    public void addProject(String projectId, String vpcId) throws CacheException {
        vpcIdProjectId.put(vpcId, projectId);
    }

    @DurationStatistics
    public Map<String, NeighborInfo> getNeighbors(String vpcId) throws CacheException {
        if (!vpcIdProjectId.containsKey(vpcId)) {
            return new HashMap<>();
        }
        String projectId = vpcIdProjectId.get(vpcId);
        CacheConfiguration cfg = CommonUtil.getCacheConfiguration(getNeighborCacheName(projectId));
        ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                NeighborInfo.class, cfg);
        Map<String, Object[]> queryParams =  new HashMap<>();
        Object[] value = new Object[1];
        value[0] = vpcId;
        queryParams.put("vpcId", value);
        return neighborCache.getAll(queryParams);
    }
}