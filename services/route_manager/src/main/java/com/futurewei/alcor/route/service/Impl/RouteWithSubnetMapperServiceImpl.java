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
import com.futurewei.alcor.route.dao.RouteRepository;
import com.futurewei.alcor.route.dao.RouteWithSubnetMapperRepository;
import com.futurewei.alcor.route.service.RouteWithSubnetMapperService;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.SubnetToRouteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RouteWithSubnetMapperServiceImpl implements RouteWithSubnetMapperService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RouteWithSubnetMapperRepository routeWithSubnetMapperRepository;

    @Autowired
    RouteRepository routeRepository;

    @Override
    public SubnetToRouteMapper getBySubnetId(String subnetId) {
        try {
            return this.routeWithSubnetMapperRepository.findItem(subnetId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    public List<RouteEntity> getRuleBySubnetId(String subnetId) {
        List<RouteEntity> routes = new ArrayList<>();

        try {
            SubnetToRouteMapper subnetToRouteMapper =  this.routeWithSubnetMapperRepository.findItem(subnetId);
            if (subnetToRouteMapper == null) {
                return null;
            }

            List<String> routeIds = subnetToRouteMapper.getRouteIds();
            for (String routeId : routeIds) {
                RouteEntity route = this.routeRepository.findItem(routeId);
                if (route != null) {
                    routes.add(route);
                }
            }

            return routes;

        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    public Map getAllMappers() throws CacheException {
        return this.routeWithSubnetMapperRepository.findAllItems();
    }

    @Override
    public void addMapper(SubnetToRouteMapper subnetToRouteMapper) throws DatabasePersistenceException {
        try {
            this.routeWithSubnetMapperRepository.addItem(subnetToRouteMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void addMapperByRouteEntity(String subnetId, RouteEntity routeEntity) throws DatabasePersistenceException {
        try {
            if (routeEntity == null) {
                return;
            }

            String routeId = routeEntity.getId();
            SubnetToRouteMapper subnetToRouteMapper = getBySubnetId(subnetId);
            if (subnetToRouteMapper == null) {
                subnetToRouteMapper = new SubnetToRouteMapper();
                subnetToRouteMapper.setSubnetId(subnetId);
            }
            List<String> routeIds = subnetToRouteMapper.getRouteIds();
            if (routeIds == null) {
                routeIds = new ArrayList<>();
            }
            routeIds.add(routeId);
            subnetToRouteMapper.setRouteIds(routeIds);

            this.routeWithSubnetMapperRepository.addItem(subnetToRouteMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteMapper(String id) throws Exception {
        this.routeWithSubnetMapperRepository.deleteItem(id);
    }

    @Override
    public void deleteMapperByRouteId(String subnetId, String routeId) throws Exception {
        if (subnetId == null || routeId == null) {
            return;
        }
        SubnetToRouteMapper subnetToRouteMapper = getBySubnetId(subnetId);
        if (subnetToRouteMapper == null) {
            return;
        }
        List<String> routeIds = subnetToRouteMapper.getRouteIds();
        if (routeIds == null) {
            return;
        }
        int index = -1;
        for (int i = 0; i < routeIds.size(); i ++) {
            if (routeIds.get(i).equals(routeId)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            routeIds.remove(index);
        }
        subnetToRouteMapper.setRouteIds(routeIds);

        this.routeWithSubnetMapperRepository.addItem(subnetToRouteMapper);
    }
}
