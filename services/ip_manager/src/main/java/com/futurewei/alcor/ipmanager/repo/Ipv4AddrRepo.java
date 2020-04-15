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

package com.futurewei.alcor.ipmanager.repo;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.ipmanager.entity.Ipv4AddrAlloc;
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
public class Ipv4AddrRepo implements ICacheRepository<Ipv4AddrAlloc> {
    private static final Logger LOG = LoggerFactory.getLogger(Ipv4AddrRepo.class);
    private ICache<String, Ipv4AddrAlloc> cache;

    @Autowired
    public Ipv4AddrRepo(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(Ipv4AddrAlloc.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("Ipv4Repository init done");
    }

    @Override
    public synchronized Ipv4AddrAlloc findItem(String Ipv4Address) {
        try {
            return cache.get(Ipv4Address);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4Repository findItem() exception:", e);
        }

        return null;
    }

    @Override
    public synchronized Map findAllItems() {
        try {
            return cache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4Repository findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    public synchronized void addItem(Ipv4AddrAlloc ipv4AddrAlloc) {
        LOG.error("Add ipAllocation:{}", ipv4AddrAlloc);

        try {
            cache.put(ipv4AddrAlloc.getSubnetId() + ipv4AddrAlloc.getIpv4Addr(), ipv4AddrAlloc);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4Repository addItem() exception:", e);
        }
    }

    @Override
    public synchronized void deleteItem(String Ipv4Address) {
        LOG.error("Delete ipAllocation, Ipv4Address:{}", Ipv4Address);

        try {
            cache.remove(Ipv4Address);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4Repository deleteItem() exception:", e);
        }
    }
}
