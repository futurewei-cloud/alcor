/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.common.enumClass.VpcRouteTarget;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.route.entity.RouteConstant;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.route.service.*;
import com.futurewei.alcor.route.utils.RouteManagerUtil;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
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
    private VpcRouterToSubnetService vpcRouterToSubnetService;

    @Autowired
    private RouteTableDatabaseService routeTableDatabaseService;

    @Autowired
    private RouteEntryDatabaseService routeEntryDatabaseService;


    @Override
    public Router getOrCreateVpcRouter(String projectId, String vpcId) throws CanNotFindVpc, DatabasePersistenceException, CacheException, OwnMultipleVpcRouterException {
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
            throw new OwnMultipleVpcRouterException();
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
        RouteEntry routeEntry = new RouteEntry(projectId, routeEntryId, "default_vpc_routeEntry", "", destination, VpcRouteTarget.LOCAL.getVpcRouteTarget(), RouteConstant.DEFAULT_PRIORITY, routeTableId, null);
        routeEntities.add(routeEntry);
        this.routeEntryDatabaseService.addRouteEntry(routeEntry);

        RouteTable routeTable = new RouteTable(projectId, routeTableId, "default_vpc_routeTable", "", routeEntities, RouteTableType.VPC.getRouteTableType(), owner);
        vpcRouteTables.add(routeTable);
        this.routeTableDatabaseService.addRouteTable(routeTable);

        Router router = new Router(projectId, routerId, "default_vpc_router", "",
                null, vpcRouteTables, "VPC:" + owner, ports, projectId, true, null, null, routeTableId);
        this.routerDatabaseService.addRouter(router);

        return router;
    }

    @Override
    public String deleteVpcRouter(String projectId, String vpcId) throws Exception {
        Router router = getOrCreateVpcRouter(projectId, vpcId);
        if (router == null) {
            return null;
        }

        // check if there is any subnet exists in the VPC
        List<RouteTable> vpcRouteTable = router.getVpcRouteTables();
        SubnetsWebJson subnetsWebJson = this.vpcRouterToSubnetService.getSubnetsByVpcId(projectId, vpcId);
        if (subnetsWebJson != null) {
            ArrayList<SubnetEntity> subnets = subnetsWebJson.getSubnets();
            if (subnets != null && subnets.size() > 0) {
                throw new VpcNonEmptyException();
            }
        }

        // delete router and route tables
        this.routerDatabaseService.deleteRouter(router.getId());
        for (RouteTable routeTable : vpcRouteTable) {
            String routeTableType = routeTable.getRouteTableType();
            if (RouteTableType.VPC.getRouteTableType().equals(routeTableType)) {
                this.routeTableDatabaseService.deleteRouteTable(routeTable.getId());
            }
        }

        return router.getId();
    }

    @Override
    public RouteTable getOrCreateVpcRouteTable(String projectId, String vpcId) throws DatabasePersistenceException, CanNotFindVpc, CacheException, OwnMultipleVpcRouterException {
        RouteTable routeTable = null;

        // Get or create a router for a Vpc
        Router router = getOrCreateVpcRouter(projectId, vpcId);

        // If VPC has a VPC routing table, return the routing table’s state
        List<RouteTable> vpcRouteTables = router.getVpcRouteTables();
        for (RouteTable vpcRouteTable : vpcRouteTables) {
            String routeTableType = vpcRouteTable.getRouteTableType();
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
        List<RouteEntry> routeEntities = new ArrayList<>();

        // create a VPC routing table and pump-in the VPC default routing rules
        RouteEntry routeEntry = new RouteEntry(projectId, routeEntryId, "default_vpc_routeEntry", "", null, VpcRouteTarget.LOCAL.getVpcRouteTarget(), RouteConstant.DEFAULT_PRIORITY, routeTableId, null);
        routeEntities.add(routeEntry);
        this.routeEntryDatabaseService.addRouteEntry(routeEntry);

        RouteTable routeTable = new RouteTable(projectId, routeTableId, "default_vpc_routeTable", "", routeEntities, RouteTableType.VPC.getRouteTableType(), owner);

        this.routeTableDatabaseService.addRouteTable(routeTable);

        router.setVpcDefaultRouteTableId(routeTableId);
        this.routerDatabaseService.addRouter(router);

        return routeTable;
    }

    @Override
    public RouteTable updateVpcRouteTable(String projectId, String vpcId, RouteTableWebJson resource) throws DatabasePersistenceException, CanNotFindVpc, CacheException, OwnMultipleVpcRouterException, ResourceNotFoundException, ResourcePersistenceException {
        RouteTable routeTable = null;
        RouteTable inRoutetable = resource.getRoutetable();

        // Get or create a router for a Vpc
        Router router = getOrCreateVpcRouter(projectId, vpcId);

        // check if there is a vpc default routetable
        List<RouteTable> vpcRouteTables = router.getVpcRouteTables();
        String vpcDefaultRouteTableId = router.getVpcDefaultRouteTableId();
        routeTable = this.routeTableDatabaseService.getByRouteTableId(vpcDefaultRouteTableId);

        if (routeTable == null) {
            String routeTableId = inRoutetable.getId();
            if (routeTableId == null) {
                routeTableId = UUID.randomUUID().toString();
                inRoutetable.setId(routeTableId);
            }
            inRoutetable.setRouteTableType(RouteTableType.VPC.getRouteTableType());
            vpcRouteTables.add(inRoutetable);
            router.setVpcRouteTables(vpcRouteTables);
            this.routerDatabaseService.addRouter(router);

            return inRoutetable;
        } else {
            List<RouteEntry> inRouteEntities = inRoutetable.getRouteEntities();

            routeTable.setRouteEntities(inRouteEntities);
            vpcRouteTables.add(routeTable);
            router.setVpcRouteTables(vpcRouteTables);
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
            return null;
        }
        return router.getVpcRouteTables();
    }

    @Override
    public RouteTable getSubnetRouteTable(String projectId, String subnetId) throws CacheException, OwnMultipleSubnetRouteTablesException, DatabasePersistenceException, ResourceNotFoundException, ResourcePersistenceException, CanNotFindSubnet, OwnMultipleVpcRouterException, CanNotFindVpc {
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
            // create a subnet route table
            SubnetWebJson subnetWebJson = this.vpcRouterToSubnetService.getSubnet(projectId, subnetId);
            String vpcId = subnetWebJson.getSubnet().getVpcId();
            Router router = getOrCreateVpcRouter(projectId, vpcId);

            String vpcDefaultRouteTableId = router.getVpcDefaultRouteTableId();
            routeTable = this.routeTableDatabaseService.getByRouteTableId(vpcDefaultRouteTableId);
            return routeTable;
        } else if (routeTableMap.size() > 1) {
            throw new OwnMultipleSubnetRouteTablesException();
        } else {
            for (Map.Entry<String, RouteTable> entry : routeTableMap.entrySet()) {
                routeTable = (RouteTable)entry.getValue();
            }
        }

        return routeTable;
    }

    @Override
    public RouteTable updateSubnetRouteTable(String projectId, String subnetId, RouteTableWebJson resource) throws CacheException, DatabasePersistenceException, OwnMultipleSubnetRouteTablesException, CanNotFindVpc, CanNotFindSubnet, ResourceNotFoundException, ResourcePersistenceException, OwnMultipleVpcRouterException {
        RouteTable routeTable = new RouteTable();
        RouteTable inRoutetable = resource.getRoutetable();
        // Get or create a router for a Subnet
        routeTable = getSubnetRouteTable(projectId, subnetId);
        if (routeTable != null) {
            RouteManagerUtil.copyPropertiesIgnoreNull(inRoutetable, routeTable);
            this.routeTableDatabaseService.addRouteTable(routeTable);

            // TODO: notify Subnet Manager to update L3 neighbor for all ports in the same subnet

        }

        return routeTable;
    }

    @Override
    public String deleteSubnetRouteTable(String projectId, String subnetId) throws Exception {
        RouteTable routeTable = null;

        // Get or create a router for a Subnet
        routeTable = getSubnetRouteTable(projectId, subnetId);
        if (routeTable == null) {
            return null;
        }

        String routeTableId = routeTable.getId();

        this.routeTableDatabaseService.deleteRouteTable(routeTableId);

        return routeTableId;
    }

    @Override
    public RouteTable createNeutronSubnetRouteTable(String projectId, String subnetId, RouteTableWebJson resource, List<RouteEntry> routes) throws DatabasePersistenceException {

        // configure a new route table
        RouteTable routeTable = new RouteTable();
        String id = UUID.randomUUID().toString();
        routeTable.setId(id);
        routeTable.setDescription("");
        routeTable.setName("subnet-" + id + "-routetable");
        routeTable.setProjectId(projectId);
        routeTable.setRouteTableType(RouteTableType.NEUTRON_SUBNET.getRouteTableType());
        routeTable.setOwner(subnetId);

        routeTable.setRouteEntities(routes);

        //RouteTable inRoutetable = resource.getRoutetable();
        // RouteManagerUtil.copyPropertiesIgnoreNull(inRoutetable, routeTable);
        this.routeTableDatabaseService.addRouteTable(routeTable);

        return routeTable;
    }


}
