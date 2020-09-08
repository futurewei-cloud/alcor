package com.futurewei.alcor.route.service.Impl;/*
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

import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.route.entity.RouteConstant;
import com.futurewei.alcor.route.exception.CanNotFindVpc;
import com.futurewei.alcor.route.exception.VpcRouterContainsSubnetRoutingTables;
import com.futurewei.alcor.route.service.*;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteEntry;
import com.futurewei.alcor.web.entity.route.RouteTable;
import com.futurewei.alcor.web.entity.route.Router;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RouterServiceImpl implements RouterService {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private VpcRouterToVpcService vpcRouterToVpcService;

    @Autowired
    private RouteTableDatabaseService routeTableDatabaseService;

    @Autowired
    private RouteEntryDatabaseService routeEntryDatabaseService;


    @Override
    public Router getOrCreateVpcRouter(String projectId, String vpcId) throws CanNotFindVpc, DatabasePersistenceException {
        // If VPC already has a router, return the router state
        VpcWebJson vpcResponse = this.vpcRouterToVpcService.getVpcWebJson(projectId, vpcId);
        VpcEntity vpcEntity = vpcResponse.getNetwork();
        Router router = vpcEntity.getRouter();
        if (router != null) {
            return router;
        }

        // If VPC doesn’t have a router, create a new router, create a VPC routing table and pump-in the VPC default routing rules
        router = createDefaultVpcRouter(projectId, vpcEntity);

        return router;
    }

    @Override
    public Router createDefaultVpcRouter(String projectId, VpcEntity vpcEntity) throws DatabasePersistenceException {
        String routerId = UUID.randomUUID().toString();
        String routeTableId = UUID.randomUUID().toString();
        String routeEntryId = UUID.randomUUID().toString();
        String owner = vpcEntity.getId();
        String destination = vpcEntity.getCidr();
        List<RouteTable> vpcRouteTables = new ArrayList<>();
        List<String> ports = new ArrayList<>();
        List<RouteEntry> routeEntities = new ArrayList<>();

        // create a VPC routing table and pump-in the VPC default routing rules
        RouteEntry routeEntry = new RouteEntry(projectId, routeEntryId, "default_vpc_routeEntry", "", destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, routeTableId, null);
        routeEntities.add(routeEntry);
        this.routeEntryDatabaseService.addRouteEntry(routeEntry);

        RouteTable routeTable = new RouteTable(projectId, routeTableId, "default_vpc_routeTable", "", routeEntities, RouteTableType.VPC, owner);
        vpcRouteTables.add(routeTable);
        this.routeTableDatabaseService.addRouteTable(routeTable);

        Router router = new Router(projectId, routerId, "default_vpc_router", "",
                null, vpcRouteTables, owner, ports, projectId, true, null, null);
        this.routerDatabaseService.addRouter(router);

        return router;
    }

    @Override
    public String deleteVpcRouter(String projectId, String vpcId) throws Exception {
        VpcWebJson vpcResponse = this.vpcRouterToVpcService.getVpcWebJson(projectId, vpcId);
        VpcEntity vpcEntity = vpcResponse.getNetwork();
        Router router = vpcEntity.getRouter();
        if (router == null) {
            return null;
        }

        // check if the VPC router contains subnet routing table
        List<RouteTable> vpcRouteTable = router.getVpcRouteTable();
        if (vpcRouteTable == null || vpcRouteTable.size() == 0) {
            return null;
        }
        for (RouteTable routeTable : vpcRouteTable) {
            String routeTableType = routeTable.getRouteTableType().getRouteTableType();
            if (RouteTableType.PRIVATE_SUBNET.getRouteTableType().equals(routeTableType) || RouteTableType.PUBLIC_SUBNET.getRouteTableType().equals(routeTableType)) {
                throw new VpcRouterContainsSubnetRoutingTables();
            }
        }

        // delete router and route tables
        this.routerDatabaseService.deleteRouter(router.getId());
        for (RouteTable routeTable : vpcRouteTable) {
            this.routeTableDatabaseService.deleteRouteTable(routeTable.getId());
        }

        return router.getId();
    }


}
