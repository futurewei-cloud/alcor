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

package com.futurewei.alcor.privateipmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class IpAddrRepo implements ICacheRepository<IpAddrAlloc> {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddrRepo.class);
    private ICache<String, IpAddrAlloc> cache;

    @Autowired
    public IpAddrRepo(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(IpAddrAlloc.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("IpAddrRepo init done");
    }

    @Override
    public synchronized IpAddrAlloc findItem(String Ipv4Address) {
        try {
            return cache.get(Ipv4Address);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpAddrRepo findItem() exception:", e);
        }

        return null;
    }

    @Override
    public synchronized Map findAllItems() {
        try {
            return cache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpAddrRepo findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    public Map<String, IpAddrAlloc> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        try {
            return cache.getAll(queryParams);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpAddrRepo findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    public synchronized void addItem(IpAddrAlloc ipAddrAlloc) {
        LOG.error("Add ipAllocation:{}", ipAddrAlloc);

        try {
            cache.put(ipAddrAlloc.getRangeId() + ipAddrAlloc.getIpAddr(), ipAddrAlloc);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpAddrRepo addItem() exception:", e);
        }
    }

    @Override
    @DurationStatistics
    public void addItems(List<IpAddrAlloc> items) throws CacheException {
        Map<String, IpAddrAlloc> ipAddrAllocMap = items.stream().collect(
                Collectors.toMap(item -> item.getRangeId() + item.getIpAddr(), item -> item));
        cache.putAll(ipAddrAllocMap);
    }

    @Override
    public synchronized void deleteItem(String IpAddress) {
        LOG.error("Delete ipAllocation, IpAddress: {}", IpAddress);

        try {
            cache.remove(IpAddress);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpAddrRepo deleteItem() exception:", e);
        }
    }
}
