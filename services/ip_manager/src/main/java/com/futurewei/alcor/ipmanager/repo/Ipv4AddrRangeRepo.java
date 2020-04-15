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
import com.futurewei.alcor.ipmanager.entity.Ipv4AddrRange;
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
public class Ipv4AddrRangeRepo implements ICacheRepository<Ipv4AddrRange> {
    private static final Logger LOG = LoggerFactory.getLogger(Ipv4AddrRangeRepo.class);
    private ICache<String, Ipv4AddrRange> cache;

    @Autowired
    public Ipv4AddrRangeRepo(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(Ipv4AddrRange.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("Ipv4RangeRepository init done");
    }

    @Override
    public Ipv4AddrRange findItem(String subnetId) {
        try {
            return cache.get(subnetId);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4RangeRepository findItem() exception:", e);
        }

        return null;
    }

    @Override
    public Map findAllItems() {
        try {
            return cache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4RangeRepository findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    public void addItem(Ipv4AddrRange ipv4AddrRange) {
        LOG.error("Add ipv4Range:{}", ipv4AddrRange);

        try {
            cache.put(ipv4AddrRange.getSubnetId(), ipv4AddrRange);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4RangeRepository addItem() exception:", e);
        }
    }

    @Override
    public void deleteItem(String subnetId) {
        LOG.error("Delete ipv4Range, subnetId:{}", subnetId);

        try {
            cache.remove(subnetId);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("Ipv4RangeRepository deleteItem() exception:", e);
        }
    }
}
