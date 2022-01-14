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
package com.futurewei.alcor.netwconfigmanager.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Subnet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class ResourceStateCache <K, V>{
    // Map <ResourceId, ResourceState>
    private ICache<String, Object> hostResourceStates;

    @Autowired
    public ResourceStateCache(CacheFactory cacheFactory) {
        this.hostResourceStates = cacheFactory.getCache(Object.class);
    }

    @DurationStatistics
    public Object getResourceState(String resourceId) throws Exception {
        Object resourceState = this.hostResourceStates.get(resourceId);

        return resourceState;
    }

    @DurationStatistics
    public Map<String, Object> getResourceStates() throws Exception {
        return this.hostResourceStates.getAll();
    }

    @DurationStatistics
    public Map<String, Object> getResourceStates(Set<String> resourceIds) throws Exception {
        return this.hostResourceStates.getAll(resourceIds);
    }

    @DurationStatistics
    public void addResourceState(String resourceId, Object resourceState) throws Exception {
        this.hostResourceStates.put(resourceId, resourceState);
    }

    @DurationStatistics
    public void addResourceStates(SortedMap<? extends String, ?> items) throws Exception {
        this.hostResourceStates.putAll(items);
    }

    @DurationStatistics
    public void updateResourceState(String resourceId, Object resourceState) throws Exception {
        this.hostResourceStates.put(resourceId, resourceState);
    }

    @DurationStatistics
    public void deleteResourceState(String resourceId) throws Exception {
        this.hostResourceStates.remove(resourceId);
    }
}
