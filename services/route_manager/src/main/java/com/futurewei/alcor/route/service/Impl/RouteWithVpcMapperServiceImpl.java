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
import com.futurewei.alcor.route.dao.RouteRepository;
import com.futurewei.alcor.route.dao.RouteWithVpcMapperRepository;
import com.futurewei.alcor.route.service.RouteWithVpcMapperService;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteWithVpcMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RouteWithVpcMapperServiceImpl implements RouteWithVpcMapperService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RouteWithVpcMapperRepository routeWithVpcMapperRepository;

    @Autowired
    RouteRepository routeRepository;

    @Override
    public RouteWithVpcMapper getByVpcId(String vpcId) throws ResourceNotFoundException, ResourcePersistenceException {
        try {
            return this.routeWithVpcMapperRepository.findItem(vpcId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<RouteEntity> getRuleByVpcId(String vpcId) throws ResourceNotFoundException, ResourcePersistenceException {

        List<RouteEntity> routes = new ArrayList<>();

        try {
            RouteWithVpcMapper routeWithVpcMapper =  this.routeWithVpcMapperRepository.findItem(vpcId);
            if (routeWithVpcMapper == null) {
                return null;
            }

            List<String> routeIds = routeWithVpcMapper.getRouteIds();
            for (String routeId : routeIds) {
                RouteEntity route = this.routeRepository.findItem(routeId);
                if (route != null) {
                    routes.add(route);
                }
            }

            return routes;

        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map getAllMappers() throws CacheException {
        return this.routeWithVpcMapperRepository.findAllItems();
    }

    @Override
    public void addMapper(RouteWithVpcMapper routeWithVpcMapper) throws DatabasePersistenceException {
        try {
            this.routeWithVpcMapperRepository.addItem(routeWithVpcMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void addMapperByRouteEntity(String vpcId, RouteEntity routeEntity) throws DatabasePersistenceException {
        try {
            if (routeEntity == null) {
                return;
            }

            String routeId = routeEntity.getId();
            RouteWithVpcMapper routeWithVpcMapper = getByVpcId(vpcId);
            if (routeWithVpcMapper == null) {
                routeWithVpcMapper = new RouteWithVpcMapper();
                routeWithVpcMapper.setVpcId(vpcId);
            }
            List<String> routeIds = routeWithVpcMapper.getRouteIds();
            if (routeIds == null) {
                routeIds = new ArrayList<>();
            }
            routeIds.add(routeId);
            routeWithVpcMapper.setRouteIds(routeIds);

            this.routeWithVpcMapperRepository.addItem(routeWithVpcMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteMapper(String id) throws CacheException {
        this.routeWithVpcMapperRepository.deleteItem(id);
    }

    @Override
    public void deleteMapperByRouteId(String vpcId, String routeId) throws CacheException, ResourceNotFoundException, ResourcePersistenceException {
        if (vpcId == null || routeId == null) {
            return;
        }
        RouteWithVpcMapper routeWithVpcMapper = getByVpcId(vpcId);
        if (routeWithVpcMapper == null) {
            return;
        }
        List<String> routeIds = routeWithVpcMapper.getRouteIds();
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
        routeIds.remove(index);
        routeWithVpcMapper.setRouteIds(routeIds);

        this.routeWithVpcMapperRepository.addItem(routeWithVpcMapper);
    }
}
