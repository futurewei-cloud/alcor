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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class SubnetPortsCache {
    // The cache is a map(subnetId, subnetPorts)
    private ICache<String, InternalSubnetPorts> subnetPortsCache;

    @Autowired
    public SubnetPortsCache(CacheFactory cacheFactory) {
        subnetPortsCache = cacheFactory.getCache(InternalSubnetPorts.class);
    }

    @DurationStatistics
    public InternalSubnetPorts getSubnetPorts(String subnetId) throws CacheException {
        return subnetPortsCache.get(subnetId);
    }

    @DurationStatistics
    public Map<String, InternalSubnetPorts> getAllSubnetPorts() throws CacheException {
        return subnetPortsCache.getAll();
    }

    @DurationStatistics
    public Map<String, InternalSubnetPorts> getAllSubnetPorts(Map<String, Object[]> queryParams) throws CacheException {
        return subnetPortsCache.getAll(queryParams);
    }

    @DurationStatistics
    public synchronized void addSubnetPorts(InternalSubnetPorts internalSubnetPorts) throws Exception {
        subnetPortsCache.put(internalSubnetPorts.getSubnetId(), internalSubnetPorts);
    }

    @DurationStatistics
    public void updateSubnetPorts(InternalSubnetPorts internalSubnetPorts) throws Exception {
        subnetPortsCache.put(internalSubnetPorts.getSubnetId(), internalSubnetPorts);
    }

    @DurationStatistics
    public void deleteSubnetPorts(String subnetId) throws Exception {
        subnetPortsCache.remove(subnetId);
    }

}
