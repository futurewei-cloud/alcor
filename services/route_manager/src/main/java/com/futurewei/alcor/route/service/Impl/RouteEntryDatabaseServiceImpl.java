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
package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.route.dao.RouteEntryRepository;
import com.futurewei.alcor.route.service.RouteEntryDatabaseService;
import com.futurewei.alcor.web.entity.route.RouteEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RouteEntryDatabaseServiceImpl implements RouteEntryDatabaseService{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RouteEntryRepository routeEntryRepository;

    @Override
    @DurationStatistics
    public RouteEntry getByRouteEntryId(String routeEntryId) throws ResourceNotFoundException, ResourcePersistenceException {
        try {
            return this.routeEntryRepository.findItem(routeEntryId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllRouteEntries() throws CacheException {
        return this.routeEntryRepository.findAllItems();
    }

    @Override
    public Map getAllRouteEntries(Map<String, Object[]> queryParams) throws CacheException {
        return this.routeEntryRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addRouteEntry(RouteEntry routeEntry) throws DatabasePersistenceException {
        try {
            this.routeEntryRepository.addItem(routeEntry);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteRouteEntry(String id) throws Exception {
        this.routeEntryRepository.deleteItem(id);
    }
}
