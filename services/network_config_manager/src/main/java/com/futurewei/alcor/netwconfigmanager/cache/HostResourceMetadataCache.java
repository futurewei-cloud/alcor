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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class HostResourceMetadataCache {

    // Map <HostId, List<ResoruceIDType>>
    private ICache<String, ResourceMeta> hostResourceMetas;
    private CacheFactory cacheFactory;

    public Transaction getTransaction() {
        return cacheFactory.getTransaction();
    }

    public void commit() throws CacheException {
        cacheFactory.commit();
    }

    public void rollback() throws CacheException {
        cacheFactory.rollback();
    }

    @Autowired
    public HostResourceMetadataCache(CacheFactory cacheFactory) {
        this.hostResourceMetas = cacheFactory.getCache(ResourceMeta.class);
        this.cacheFactory = cacheFactory;
    }

    @DurationStatistics
    public ResourceMeta getResourceMeta(String hostId) throws Exception {
        ResourceMeta resourceState = this.hostResourceMetas.get(hostId);

        return resourceState;
    }

    @DurationStatistics
    public void addResourceMeta(ResourceMeta resourceMeta) throws Exception {
        this.hostResourceMetas.put(resourceMeta.getOwnerId(), resourceMeta);
    }

    @DurationStatistics
    public void updateResourceMeta(ResourceMeta resourceMeta) throws Exception {
        this.hostResourceMetas.put(resourceMeta.getOwnerId(), resourceMeta);
    }

    @DurationStatistics
    public void deleteResourceMeta(String hostId) throws Exception {
        this.hostResourceMetas.remove(hostId);
    }
}
