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
package com.futurewei.alcor.route.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteWithSubnetMapper;

import java.util.List;
import java.util.Map;

public interface RouteWithSubnetMapperService {

    public RouteWithSubnetMapper getBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;
    public List<RouteEntity> getRuleBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllMappers () throws CacheException;
    public void addMapper (RouteWithSubnetMapper routeWithSubnetMapper) throws DatabasePersistenceException;
    public void addMapperByRouteEntity (String subnetId, RouteEntity routeEntity) throws DatabasePersistenceException;
    public void deleteMapper (String id) throws CacheException;
    public void deleteMapperByRouteId (String subnetId, String routeId) throws CacheException, ResourceNotFoundException, ResourcePersistenceException;


}
