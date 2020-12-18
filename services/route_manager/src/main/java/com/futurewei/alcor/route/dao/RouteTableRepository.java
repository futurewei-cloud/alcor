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
package com.futurewei.alcor.route.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.route.RouteTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class RouteTableRepository implements ICacheRepository<RouteTable> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, RouteTable> getCache() {
        return cache;
    }

    private ICache<String, RouteTable> cache;

    @Autowired
    public RouteTableRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(RouteTable.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteTableRepository init completed");
    }

    @Override
    @DurationStatistics
    public RouteTable findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, RouteTable> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, RouteTable> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(RouteTable routeTable) throws CacheException {
        try (Transaction tx = cache.getTransaction().start()) {

            logger.log(Level.INFO, "Add route table, route table Id:" + routeTable.getId());
            cache.put(routeTable.getId(), routeTable);

            tx.commit();
        } catch (Exception e) {
            throw new CacheException();
        }
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        try (Transaction tx = cache.getTransaction().start()) {

            logger.log(Level.INFO, "Delete route table, route table Id:" + id);
            cache.remove(id);

            tx.commit();
        } catch (Exception e) {
            throw new CacheException();
        }
    }
}