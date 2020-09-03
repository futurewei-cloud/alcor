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
import com.futurewei.alcor.route.dao.RouterExtraAttributeRepository;
import com.futurewei.alcor.route.service.RouterExtraAttributeDatabaseService;
import com.futurewei.alcor.web.entity.route.RouterExtraAttribute;
import com.futurewei.alcor.common.logging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RouterExtraAttributeDatabaseServiceImpl implements RouterExtraAttributeDatabaseService {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    RouterExtraAttributeRepository routerExtraAttributeRepository;

    @Override
    @DurationStatistics
    public RouterExtraAttribute getByRouterExtraAttributeId(String routerExtraAttributeId) throws ResourceNotFoundException, ResourcePersistenceException {
        try {
            return this.routerExtraAttributeRepository.findItem(routerExtraAttributeId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllRouterExtraAttributes() throws CacheException {
        return this.routerExtraAttributeRepository.findAllItems();
    }

    @Override
    public Map getAllRouterExtraAttributes(Map<String, Object[]> queryParams) throws CacheException {
        return this.routerExtraAttributeRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addRouterExtraAttribute(RouterExtraAttribute routerExtraAttribute) throws DatabasePersistenceException {
        try {
            this.routerExtraAttributeRepository.addItem(routerExtraAttribute);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteRouterExtraAttribute(String id) throws Exception {
        this.routerExtraAttributeRepository.deleteItem(id);
    }
}
