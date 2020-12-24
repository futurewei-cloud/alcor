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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.privateipmanager.entity.VpcIpRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class VpcIpRangeRepo implements ICacheRepository<VpcIpRange> {
    private static final Logger LOG = LoggerFactory.getLogger(VpcIpRangeRepo.class);

    private ICache<String, VpcIpRange> vpcIpRangeCache;

    @Autowired
    public VpcIpRangeRepo(CacheFactory cacheFactory) {
        vpcIpRangeCache = cacheFactory.getCache(VpcIpRange.class);
    }


    @Override
    public VpcIpRange findItem(String id) throws CacheException {
        return null;
    }

    @Override
    public Map<String, VpcIpRange> findAllItems() throws CacheException {
        return null;
    }

    @Override
    public Map<String, VpcIpRange> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return vpcIpRangeCache.getAll(queryParams);
    }

    @Override
    public void addItem(VpcIpRange newItem) throws CacheException {

    }

    @Override
    public void addItems(List<VpcIpRange> items) throws CacheException {

    }

    @Override
    public void deleteItem(String id) throws CacheException {

    }
}
