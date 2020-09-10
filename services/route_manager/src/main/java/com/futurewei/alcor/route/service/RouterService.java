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
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.web.entity.route.RouteTable;
import com.futurewei.alcor.web.entity.route.RouteTableWebJson;
import com.futurewei.alcor.web.entity.route.Router;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.List;

public interface RouterService {

    public Router getOrCreateVpcRouter (String projectId, String vpcId) throws CanNotFindVpc, DatabasePersistenceException, CacheException, ExistMultipleVpcRouter;
    public Router createDefaultVpcRouter (String projectId, VpcEntity vpcEntity) throws DatabasePersistenceException;
    public String deleteVpcRouter (String projectId, String vpcId) throws Exception;
    public RouteTable getOrCreateVpcRouteTable (String projectId, String vpcId) throws DatabasePersistenceException, CanNotFindVpc, CacheException, ExistMultipleVpcRouter;
    public RouteTable createDefaultVpcRouteTable (String projectId, Router router) throws DatabasePersistenceException;
    public RouteTable updateVpcRouteTable (String projectId, String vpcId, RouteTableWebJson resource) throws DatabasePersistenceException, CanNotFindVpc, CacheException, ExistMultipleVpcRouter;
    public List<RouteTable> getVpcRouteTables (String projectId, String vpcId) throws CanNotFindVpc;
    public RouteTable getOrCreateSubnetRouteTable(String projectId, String subnetId) throws CanNotFindSubnet, CacheException, ExistMultipleSubnetRouteTable, DatabasePersistenceException;
    public RouteTable updateSubnetRouteTable (String projectId, String subnetId, RouteTableWebJson resource) throws CacheException, DatabasePersistenceException, ExistMultipleSubnetRouteTable;
    public String deleteSubnetRouteTable (String projectId, String subnetId) throws Exception;
}
