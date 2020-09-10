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
import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.route.entity.RouteConstant;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.route.service.*;
import com.futurewei.alcor.route.utils.RouteManagerUtil;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public Router getOrCreateVpcRouter(String projectId, String vpcId) throws CanNotFindVpc, DatabasePersistenceException, CacheException, ExistMultipleVpcRouter {
        Router router = null;

        // If VPC already has a router, return the router state
        Map<String, Router> routerMap = null;
        Map<String, Object[]> queryParams = new HashMap<>();
        Object[] values = new Object[1];
        values[0] = vpcId;
        queryParams.put("owner", values);

        routerMap = this.routerDatabaseService.getAllRouters(queryParams);

        if (routerMap == null) {
            routerMap = new HashMap<>();
        }

        if (routerMap.size() > 1) {
            throw new ExistMultipleVpcRouter();
        } else if (routerMap.size() == 1) {
            for (Map.Entry<String, Router> entry : routerMap.entrySet()) {
                router = (Router)entry.getValue();
                return router;
            }
        } else {
            // get vpc entity to create default route table and route route rule
            VpcWebJson vpcResponse = this.vpcRouterToVpcService.getVpcWebJson(projectId, vpcId);
            VpcEntity vpcEntity = vpcResponse.getNetwork();


            // If VPC doesn’t have a router, create a new router, create a VPC routing table and pump-in the VPC default routing rules
            router = createDefaultVpcRouter(projectId, vpcEntity);
        }

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

    @Override
    public RouteTable getOrCreateVpcRouteTable(String projectId, String vpcId) throws DatabasePersistenceException, CanNotFindVpc, CacheException, ExistMultipleVpcRouter {
        RouteTable routeTable = null;

        // Get or create a router for a Vpc
        Router router = getOrCreateVpcRouter(projectId, vpcId);

        // If VPC has a VPC routing table, return the routing table’s state
        List<RouteTable> vpcRouteTables = router.getVpcRouteTable();
        for (RouteTable vpcRouteTable : vpcRouteTables) {
            String routeTableType = vpcRouteTable.getRouteTableType().getRouteTableType();
            if (RouteTableType.VPC.getRouteTableType().equals(routeTableType)) {
                return vpcRouteTable;
            }
        }

        // If VPC doesn’t have a VPC routing table, this operation will create a VPC routing table and pump-in the VPC default routing rules.
        routeTable = createDefaultVpcRouteTable(projectId, router);

        return routeTable;
    }

    @Override
    public RouteTable createDefaultVpcRouteTable(String projectId, Router router) throws DatabasePersistenceException {
        String routeTableId = UUID.randomUUID().toString();
        String routeEntryId = UUID.randomUUID().toString();
        String owner = router.getOwner();
        List<RouteTable> vpcRouteTables = router.getVpcRouteTable();
        List<RouteEntry> routeEntities = new ArrayList<>();

        // create a VPC routing table and pump-in the VPC default routing rules
        RouteEntry routeEntry = new RouteEntry(projectId, routeEntryId, "default_vpc_routeEntry", "", null, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, routeTableId, null);
        routeEntities.add(routeEntry);
        this.routeEntryDatabaseService.addRouteEntry(routeEntry);

        RouteTable routeTable = new RouteTable(projectId, routeTableId, "default_vpc_routeTable", "", routeEntities, RouteTableType.VPC, owner);
        vpcRouteTables.add(routeTable);
        this.routeTableDatabaseService.addRouteTable(routeTable);

        vpcRouteTables.add(routeTable);
        router.setVpcRouteTable(vpcRouteTables);
        this.routerDatabaseService.addRouter(router);

        return routeTable;
    }

    @Override
    public RouteTable updateVpcRouteTable(String projectId, String vpcId, RouteTableWebJson resource) throws DatabasePersistenceException, CanNotFindVpc, CacheException, ExistMultipleVpcRouter {
        RouteTable routeTable = null;
        RouteTable inRoutetable = resource.getRoutetable();

        // Get or create a router for a Vpc
        Router router = getOrCreateVpcRouter(projectId, vpcId);

        // check if there is a vpc default routetable
        List<RouteTable> vpcRouteTables = router.getVpcRouteTable();
        for (RouteTable vpcRouteTable : vpcRouteTables) {
            String routeTableType = vpcRouteTable.getRouteTableType().getRouteTableType();
            if (RouteTableType.VPC.getRouteTableType().equals(routeTableType)) {
                routeTable = vpcRouteTable;
                vpcRouteTables.remove(vpcRouteTable);
                break;
            }
        }

        if (routeTable == null) {
            String routeTableId = inRoutetable.getId();
            if (routeTableId == null) {
                routeTableId = UUID.randomUUID().toString();
                inRoutetable.setId(routeTableId);
            }
            inRoutetable.setRouteTableType(RouteTableType.VPC);
            vpcRouteTables.add(inRoutetable);
            router.setVpcRouteTable(vpcRouteTables);
            this.routerDatabaseService.addRouter(router);

            return inRoutetable;
        } else {
            List<RouteEntry> routeEntities = routeTable.getRouteEntities();
            List<RouteEntry> inRouteEntities = inRoutetable.getRouteEntities();

            for (RouteEntry routeEntry : routeEntities) {
                if (!inRouteEntities.contains(routeEntry)) {
                    // TODO: check if existing rules are currently used by other subnet’s routing table
                }
            }
            routeTable.setRouteEntities(inRouteEntities);
            vpcRouteTables.add(routeTable);
            router.setVpcRouteTable(vpcRouteTables);
            this.routerDatabaseService.addRouter(router);

            return routeTable;
        }

    }

    @Override
    public List<RouteTable> getVpcRouteTables(String projectId, String vpcId) throws CanNotFindVpc {
        VpcWebJson vpcResponse = this.vpcRouterToVpcService.getVpcWebJson(projectId, vpcId);
        VpcEntity vpcEntity = vpcResponse.getNetwork();
        Router router = vpcEntity.getRouter();
        if (router == null) {
            return new ArrayList<RouteTable>();
        }
        return router.getVpcRouteTable();
    }

    @Override
    public RouteTable getOrCreateSubnetRouteTable(String projectId, String subnetId) throws CacheException, ExistMultipleSubnetRouteTable, DatabasePersistenceException {
        RouteTable routeTable = null;

        Map<String, RouteTable> routeTableMap = null;
        Map<String, Object[]> queryParams = new HashMap<>();
        Object[] values = new Object[1];
        values[0] = subnetId;
        queryParams.put("owner", values);

        routeTableMap = this.routeTableDatabaseService.getAllRouteTables(queryParams);
        if (routeTableMap == null) {
            routeTableMap = new HashMap<>();
        }

        if (routeTableMap.size() == 0) {
            // TODO: call subnet API to create Subnet route table and route rule
            return routeTable;
        } else if (routeTableMap.size() > 1) {
            throw new ExistMultipleSubnetRouteTable();
        } else {
            for (Map.Entry<String, RouteTable> entry : routeTableMap.entrySet()) {
                routeTable = (RouteTable)entry.getValue();
            }
        }

        return routeTable;
    }

    @Override
    public RouteTable updateSubnetRouteTable(String projectId, String subnetId, RouteTableWebJson resource) throws CacheException, DatabasePersistenceException, ExistMultipleSubnetRouteTable {
        RouteTable routeTable = new RouteTable();
        RouteTable inRoutetable = resource.getRoutetable();
        // Get or create a router for a Subnet
        routeTable = getOrCreateSubnetRouteTable(projectId, subnetId);

        RouteManagerUtil.copyPropertiesIgnoreNull(inRoutetable, routeTable);
        this.routeTableDatabaseService.addRouteTable(routeTable);

        // TODO: notify Subnet Manager to update L3 neighbor for all ports in the same subnet

        return routeTable;
    }

    @Override
    public String deleteSubnetRouteTable(String projectId, String subnetId) throws Exception {
        RouteTable routeTable = null;

        // Get or create a router for a Subnet
        routeTable = getOrCreateSubnetRouteTable(projectId, subnetId);
        if (routeTable == null) {
            return null;
        }

        String routeTableId = routeTable.getId();

        this.routeTableDatabaseService.deleteRouteTable(routeTableId);

        return routeTableId;
    }


}
