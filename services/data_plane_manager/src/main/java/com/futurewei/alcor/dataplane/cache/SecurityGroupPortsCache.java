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
import com.futurewei.alcor.dataplane.entity.InternalPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class SecurityGroupPortsCache {
    // The cache is a map(securityGroupId, portIds)
    private ICache<String, InternalPorts> securityGroupPortsCache;

    @Autowired
    public SecurityGroupPortsCache(CacheFactory cacheFactory) {
        securityGroupPortsCache = cacheFactory.getCache(InternalPorts.class);
    }

    @DurationStatistics
    public InternalPorts getSecurityGroupPorts(String sgId) throws CacheException {
        return securityGroupPortsCache.get(sgId);
    }

    @DurationStatistics
    public Map<String, InternalPorts> getAllSecurityGroupPorts() throws CacheException {
        return securityGroupPortsCache.getAll();
    }

    @DurationStatistics
    public Map<String, InternalPorts> getAllSecurityGroupPorts(Map<String, Object[]> queryParams) throws CacheException {
        return securityGroupPortsCache.getAll(queryParams);
    }

    @DurationStatistics
    public synchronized void addSecurityGroupPorts(InternalPorts internalPorts) throws Exception {
        securityGroupPortsCache.put(internalPorts.getSecurityGroupId(), internalPorts);
    }

    @DurationStatistics
    public void updateSecurityGroupPorts(InternalPorts internalPorts) throws Exception {
        securityGroupPortsCache.put(internalPorts.getSecurityGroupId(), internalPorts);
    }

    @DurationStatistics
    public void deleteSecurityGroupPorts(String sgId) throws Exception {
        securityGroupPortsCache.remove(sgId);
    }

}
