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

package com.futurewei.alcor.privateipmanager.repo;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import com.futurewei.alcor.common.db.CacheException;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@ComponentScan(value="com.futurewei.alcor.common.db")
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
    public synchronized void addItem(IpAddrAlloc ipAddrAlloc) {
        LOG.error("Add ipAllocation:{}", ipAddrAlloc);

        try {
            cache.put(ipAddrAlloc.getSubnetId() + ipAddrAlloc.getIpAddr(), ipAddrAlloc);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpAddrRepo addItem() exception:", e);
        }
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
