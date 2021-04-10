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
