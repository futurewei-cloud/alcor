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

import com.futurewei.alcor.common.enumClass.*;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.route.config.ConstantsConfig;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.route.service.NeutronRouterService;
import com.futurewei.alcor.route.service.NeutronRouterToSubnetService;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.service.RouterExtraAttributeDatabaseService;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import com.futurewei.alcor.common.logging.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.logging.Level;

@Service
public class NeutronRouterServiceImpl implements NeutronRouterService {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    private NeutronRouterToSubnetService routerToSubnetService;

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private RouterExtraAttributeDatabaseService routerExtraAttributeDatabaseService;

    @Override
    public NeutronRouterWebRequestObject getNeutronRouter(String routerId) throws ResourceNotFoundException, ResourcePersistenceException, RouterUnavailable {
        NeutronRouterWebRequestObject neutronRouterWebRequestObject = new NeutronRouterWebRequestObject();

        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new RouterUnavailable();
        }
        RouterExtraAttribute routerExtraAttribute = null;
        if (router.getRouterExtraAttributeId() != null) {
            routerExtraAttribute = this.routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(router.getRouterExtraAttributeId());
        }

        BeanUtils.copyProperties(router, neutronRouterWebRequestObject);
        RouteTable routeTable = router.getNeutronRouteTable();
        neutronRouterWebRequestObject.setRouteTable(routeTable);
        if (routerExtraAttribute != null) {
            BeanUtils.copyProperties(routerExtraAttribute, neutronRouterWebRequestObject);
            neutronRouterWebRequestObject.setId(routerId);
        }
        return neutronRouterWebRequestObject;
    }

    @Override
    public NeutronRouterWebRequestObject saveRouterAndRouterExtraAttribute(NeutronRouterWebRequestObject neutronRouter) throws NeutronRouterIsNull, DatabasePersistenceException {
        if (neutronRouter == null) {
            throw new NeutronRouterIsNull();
        }
        NeutronRouterWebRequestObject inNeutronRouter = new NeutronRouterWebRequestObject();
        BeanUtils.copyProperties(neutronRouter, inNeutronRouter);

        String attachedRouterExtraAttributeId = UUID.randomUUID().toString();

        Router router = new Router();
        RouterExtraAttribute routerExtraAttribute = new RouterExtraAttribute();

        BeanUtils.copyProperties(neutronRouter, router);
        BeanUtils.copyProperties(neutronRouter, routerExtraAttribute);
        routerExtraAttribute.setId(attachedRouterExtraAttributeId);
        RouteTable routeTable = neutronRouter.getRouteTable();
        if (routeTable == null) {
            routeTable = new RouteTable();
            List<RouteEntry> routeEntities = new ArrayList<>();
            String routeTableId = UUID.randomUUID().toString();
            routeTable.setId(routeTableId);
            routeTable.setRouteEntities(routeEntities);
            routeTable.setRouteTableType(RouteTableType.NEUTRON_ROUTER.getRouteTableType());
        }
        router.setNeutronRouteTable(routeTable);
        router.setRouterExtraAttributeId(attachedRouterExtraAttributeId);
        inNeutronRouter.setRouteTable(routeTable);

        this.routerDatabaseService.addRouter(router);
        this.routerExtraAttributeDatabaseService.addRouterExtraAttribute(routerExtraAttribute);


        return inNeutronRouter;

    }

    @Override
    public RouterInterfaceResponse addAnInterfaceToNeutronRouter(String projectid, String portId, String subnetId, String routerId) throws SpecifyBothSubnetIDAndPortID, ResourceNotFoundException, ResourcePersistenceException, RouterUnavailable, DatabasePersistenceException, PortIDIsAlreadyExist, PortIsAlreadyInUse, SubnetNotBindUniquePortId {
        if (portId != null && subnetId != null) {
            throw new SpecifyBothSubnetIDAndPortID();
        }

        SubnetEntity subnet = null;
        String subnetid = null;
        String projectId = null;
        String attachedRouterId = null;

        // Only pass in the value of the port
        if (portId != null && subnetId == null) {

            // get subnet by port id
            SubnetsWebJson subnetsWebJson = this.routerToSubnetService.getSubnetsByPortId(projectid, portId);
            if (subnetsWebJson == null) {
                return new RouterInterfaceResponse();
            }
            ArrayList<SubnetEntity> subnets = subnetsWebJson.getSubnets();
            if (subnets.size() == 0) {
                return new RouterInterfaceResponse();
            }
            if (subnets.size() != 1) {
                throw new SubnetNotBindUniquePortId();
            }
            subnet = subnets.get(0);
            subnetid = subnet.getId();
        }
        // Only pass in the value of the subnet
        else if (portId == null && subnetId != null) {

            // get subnet by subnet id
            SubnetWebJson subnetWebJson = this.routerToSubnetService.getSubnet(projectid, subnetId);
            subnet = subnetWebJson.getSubnet();
            if (subnet == null) {
                logger.log(Level.WARNING, "can not find subnet by subnet id :" + subnetId);
                return new RouterInterfaceResponse();
            }
            subnetid = subnetId;
        } else {
            return new RouterInterfaceResponse();
        }
        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new RouterUnavailable(routerId);
        }

        projectId = subnet.getProjectId();
        portId = subnet.getGatewayPortId();
        attachedRouterId = subnet.getAttachedRouterId();

        // check if port_id is used by other router
        if (attachedRouterId != null && !attachedRouterId.equals("")) {
            throw new PortIsAlreadyInUse();
        }
        subnet.setAttachedRouterId(routerId);

        // update device_id and device_owner
        PortEntity portEntity = new PortEntity();
        portEntity.setDeviceId(routerId);
        portEntity.setDeviceOwner("network:router_interface");
        subnet.setPort(portEntity);

        // update subnet
        this.routerToSubnetService.updateSubnet(projectId, subnetid, subnet);
        // TODO: may need to maintain the mapping for new added port and it's subnet in the Route Manager

        List<String> ports = router.getPorts();
        if (ports == null) {
            ports = new ArrayList<>();
        }

        if (ports.contains(portId)) {
            throw new PortIDIsAlreadyExist();
        }
        ports.add(portId);
        router.setPorts(ports);
        this.routerDatabaseService.addRouter(router);

        // Construct response
        List<String> subnetIds = new ArrayList<>() {
        };
        subnetIds.add(subnetid);
        return new RouterInterfaceResponse(routerId, subnet.getVpcId(), portId, subnetid, subnetIds, projectId, projectId, subnet.getTags());
    }

    @Override
    public RouterInterfaceResponse removeAnInterfaceToNeutronRouter(String projectid, String portId, String subnetId, String routerId) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, AttachedPortsNotMatchPortId, RouterTableNotExist, RouterInterfaceAreUsedByRoutes, SubnetNotBindUniquePortId, DatabasePersistenceException {
        SubnetEntity subnet = null;
        String projectId = null;
        String subnetid = null;
        String attachedPort = null;
        String attachedRouterId = null;

        // if pass in both port_id and subnet_id, check conflict
        if (portId != null && subnetId != null) {
            SubnetWebJson subnetWebJson = this.routerToSubnetService.getSubnet(projectid, subnetId);
            subnet = subnetWebJson.getSubnet();
            if (subnet == null) {
                logger.log(Level.WARNING, "can not find subnet by subnet id :" + subnetId);
                return new RouterInterfaceResponse();
            }
            attachedPort = subnet.getGatewayPortId();
            if (attachedPort != null) {
                if (!attachedPort.equals(portId)) {
                    throw new AttachedPortsNotMatchPortId();
                }
            } else {
                logger.log(Level.WARNING, "There is no IP address on the port");
                return new RouterInterfaceResponse();
            }
            subnetid = subnetId;

        } else if (portId != null && subnetId == null) {
            // get subnet by port id
            SubnetsWebJson subnetsWebJson = this.routerToSubnetService.getSubnetsByPortId(projectid, portId);
            if (subnetsWebJson == null) {
                return new RouterInterfaceResponse();
            }
            ArrayList<SubnetEntity> subnets = subnetsWebJson.getSubnets();
            if (subnets.size() == 0) {
                return new RouterInterfaceResponse();
            }
            if (subnets.size() != 1) {
                throw new SubnetNotBindUniquePortId();
            }
            subnet = subnets.get(0);
            subnetid = subnet.getId();
        } else if (portId == null && subnetId != null) {
            // get subnet by subnet id
            SubnetWebJson subnetWebJson = this.routerToSubnetService.getSubnet(projectid, subnetId);
            subnet = subnetWebJson.getSubnet();
            if (subnet == null) {
                logger.log(Level.WARNING, "can not find subnet by subnet id :" + subnetId);
                return new RouterInterfaceResponse();
            }
            subnetid = subnetId;
        } else {
            return new RouterInterfaceResponse();
        }

        // check if the router or the subnet and port do not exist or are not visible
        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new RouterOrSubnetAndPortNotExistOrNotVisible();
        }

        // check if you try to delete the router interface for subnets that are used by one or more route
        projectId = subnet.getProjectId();
        attachedRouterId = subnet.getAttachedRouterId();
        String gatewayIp = subnet.getGatewayIp();
        if (gatewayIp != null) {
            RouteTable routeTable = router.getNeutronRouteTable();
            if (routeTable == null) {
                throw new RouterTableNotExist();
            }

            List<RouteEntry> routeEntities = routeTable.getRouteEntities();
            for (RouteEntry routeEntry : routeEntities) {
                String nextHop = routeEntry.getNexthop();
                if (gatewayIp.equals(nextHop)) {
                    throw new RouterInterfaceAreUsedByRoutes();
                }
            }

        }

        // remove interface
        subnet.setAttachedRouterId("");

        List<String> ports = router.getPorts();
        if (ports == null) {
            return new RouterInterfaceResponse();
        }

        if (portId == null) {
            portId = subnet.getGatewayPortId();
        }

        if (ports.contains(portId)) {
            ports.remove(portId);
        }
        router.setPorts(ports);
        this.routerDatabaseService.addRouter(router);

        // update device_id and device_owner
        PortEntity portEntity = new PortEntity();
        portEntity.setDeviceId(null);
        portEntity.setDeviceOwner(null);
        subnet.setPort(portEntity);

        // update subnet
        this.routerToSubnetService.updateSubnet(projectId, subnetid, subnet);
        // TODO: may need to maintain the mapping for new added port and it's subnet in the Route Manager

        // Construct response
        List<String> subnetIds = new ArrayList<>() {
        };
        subnetIds.add(subnetid);
        return new RouterInterfaceResponse(routerId, subnet.getVpcId(), portId, subnetid, subnetIds, projectId, projectId, subnet.getTags());

    }

    @Override
    public RoutesToNeutronWebResponse addRoutesToNeutronRouter(String routerid, RoutesToNeutronRouterRequestObject requestRouter) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, DatabasePersistenceException, DestinationOrNexthopCanNotBeNull {
        RoutesToNeutronRouterResponseObject responseRouter = new RoutesToNeutronRouterResponseObject();
        List<RoutesToNeutronRouteObject> responseRoutes = new ArrayList<>();

        Router router = this.routerDatabaseService.getByRouterId(routerid);
        if (router == null) {
            throw new RouterOrSubnetAndPortNotExistOrNotVisible();
        }
        RouteTable routeTable = router.getNeutronRouteTable();
        List<RouteEntry> routeEntities = routeTable.getRouteEntities();


        List<RoutesToNeutronRouteObject> requestRoutes = requestRouter.getRoutes();
        for (RoutesToNeutronRouteObject requestRoute : requestRoutes) {
            boolean isExit = false;
            String requestDestination = requestRoute.getDestination();
            String requestNexthop = requestRoute.getNexthop();

            if (requestDestination == null || requestNexthop == null) {
                throw new DestinationOrNexthopCanNotBeNull();
            }

            for (RouteEntry routeEntry : routeEntities) {
                String destination = routeEntry.getDestination();
                String nexthop = routeEntry.getNexthop();
                if (destination.equals(requestDestination) && nexthop.equals(requestNexthop)) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                RouteEntry routeEntry = new RouteEntry();
                routeEntry.setDestination(requestDestination);
                routeEntry.setNexthop(requestNexthop);
                routeEntities.add(routeEntry);

                RoutesToNeutronRouteObject routesToNeutronRouteObject = new RoutesToNeutronRouteObject(requestDestination, requestNexthop);
                responseRoutes.add(routesToNeutronRouteObject);
            }
        }
        routeTable.setRouteEntities(routeEntities);
        router.setNeutronRouteTable(routeTable);
        this.routerDatabaseService.addRouter(router);

        // Construct response
        for (RouteEntry routeEntry : routeEntities) {
            String destination = routeEntry.getDestination();
            String nexthop = routeEntry.getNexthop();
            RoutesToNeutronRouteObject routesToNeutronRouteObject = new RoutesToNeutronRouteObject(destination, nexthop);
            responseRoutes.add(routesToNeutronRouteObject);
        }
        responseRouter.setId(routerid);
        responseRouter.setName(router.getName());
        responseRouter.setRoutes(responseRoutes);

        return new RoutesToNeutronWebResponse(responseRouter);
    }

    @Override
    public RoutesToNeutronWebResponse removeRoutesToNeutronRouter(String routerid, RoutesToNeutronRouterRequestObject requestRouter) throws RouterOrSubnetAndPortNotExistOrNotVisible, ResourceNotFoundException, ResourcePersistenceException, DestinationOrNexthopCanNotBeNull, DatabasePersistenceException {
        RoutesToNeutronRouterResponseObject responseRouter = new RoutesToNeutronRouterResponseObject();
        List<RoutesToNeutronRouteObject> responseRoutes = new ArrayList<>();

        Router router = this.routerDatabaseService.getByRouterId(routerid);
        if (router == null) {
            throw new RouterOrSubnetAndPortNotExistOrNotVisible();
        }
        RouteTable routeTable = router.getNeutronRouteTable();
        List<RouteEntry> routeEntities = routeTable.getRouteEntities();

        List<RoutesToNeutronRouteObject> requestRoutes = requestRouter.getRoutes();
        // TODO: time complexity O(n^2), check if it effect performance
        for (RoutesToNeutronRouteObject requestRoute : requestRoutes) {
            String requestDestination = requestRoute.getDestination();
            String requestNexthop = requestRoute.getNexthop();

            if (requestDestination == null || requestNexthop == null) {
                throw new DestinationOrNexthopCanNotBeNull();
            }

            for (int i = 0; i < routeEntities.size(); i++) {
                RouteEntry routeEntry = routeEntities.get(i);
                String destination = routeEntry.getDestination();
                String nexthop = routeEntry.getNexthop();
                if (destination.equals(requestDestination) && nexthop.equals(requestNexthop)) {
                    routeEntities.remove(i);
                    break;
                }
            }
        }
        routeTable.setRouteEntities(routeEntities);
        router.setNeutronRouteTable(routeTable);
        this.routerDatabaseService.addRouter(router);

        // Construct response
        for (RouteEntry routeEntry : routeEntities) {
            String destination = routeEntry.getDestination();
            String nexthop = routeEntry.getNexthop();
            RoutesToNeutronRouteObject routesToNeutronRouteObject = new RoutesToNeutronRouteObject(destination, nexthop);
            responseRoutes.add(routesToNeutronRouteObject);
        }
        responseRouter.setId(routerid);
        responseRouter.setName(router.getName());
        responseRouter.setRoutes(responseRoutes);

        return new RoutesToNeutronWebResponse(responseRouter);
    }

    @Override
    public ConnectedSubnetsWebResponse getConnectedSubnets(String projectId, String vpcId, String subnetId) throws ResourceNotFoundException, ResourcePersistenceException, SubnetNotBindUniquePortId, UnsupportedOperationException {
        List<SubnetEntity> subnetEntities = new ArrayList<>();
        InternalRouterInfo internalRouterInfo = new InternalRouterInfo();
        ConnectedSubnetsWebResponse connectedSubnetsWebResponse = new ConnectedSubnetsWebResponse(internalRouterInfo, subnetEntities);

        SubnetWebJson subnetWebJson = this.routerToSubnetService.getSubnet(projectId, subnetId);
        if (!this.ValidateSubnetAndConnectedRouter(subnetWebJson, subnetId)) {
            logger.log(Level.WARNING, "Validation failed for SubnetAndConnectedRouter | subnet id:" + subnetId);
            return connectedSubnetsWebResponse;
        }

        Router router = this.routerDatabaseService.getByRouterId(subnetWebJson.getSubnet().getAttachedRouterId());
        if (!this.ValidateRouter(router)) {
            logger.log(Level.WARNING, "Validation failed for router | router :" + router);
            return connectedSubnetsWebResponse;
        }

        String routeTableType = router.getNeutronRouteTable().getRouteTableType();
        Map<String, String> gwPortToSubnetIdMap = new HashMap<>();
        if (routeTableType.equals("neutron_router")) {
            boolean processedResult = this.ProcessNeutronRouterAndPopulateSubnetIds(projectId, router, subnetEntities, gwPortToSubnetIdMap);
            if (!processedResult) {
                logger.log(Level.WARNING, "Process failed for Neutron router | project id:" + projectId + "router id: " + router.getId());
                return connectedSubnetsWebResponse;
            }
        } else if (routeTableType.equals("vpc_router")) {
            // TODO: vpc route operation
            throw new UnsupportedOperationException();
        } else {
            throw new UnsupportedOperationException();
        }

        PopulateInternalRouterInfo(router, gwPortToSubnetIdMap, internalRouterInfo);
        connectedSubnetsWebResponse.setInternalRouterInfo(internalRouterInfo);
        connectedSubnetsWebResponse.setSubnetEntities(subnetEntities);

        return connectedSubnetsWebResponse;
    }

    @Override
    public InternalRouterInfo updateRoutingRule (String routerId, String subnetId, RoutesToNeutronRouterRequestObject routerObject) throws ResourceNotFoundException, ResourcePersistenceException, RouterUnavailable, RouterTableNotExist, DestinationOrNexthopCanNotBeNull, OwnerInNeutronRouteTableNotFound {
        List<InternalRoutingRule> updateRoutes = new ArrayList<>();

        List<RoutesToNeutronRouteObject> routes = routerObject.getRoutes();
        if (routes == null || routes.size() == 0) {
            return new InternalRouterInfo();
        }

        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new RouterUnavailable(routerId);
        }

        // find neutron routeTable
        RouteTable neutronRouteTable = null;
        if (subnetId == null) {
            // call from RM (RM path)
            neutronRouteTable = router.getNeutronRouteTable();
            if (neutronRouteTable == null) {
                throw new RouterTableNotExist();
            }
        } else {
            // call from SM (SM path)
            List<RouteTable> neutronSubnetRouteTables = router.getNeutronSubnetRouteTables();
            for (RouteTable neutronSubnetRouteTable : neutronSubnetRouteTables) {
                String owner = neutronSubnetRouteTable.getOwner();
                if (owner == null) {
                    throw new OwnerInNeutronRouteTableNotFound();
                }
                if (owner.equals(subnetId)) {
                    neutronRouteTable = neutronSubnetRouteTable;
                    break;
                }
            }
            if (neutronRouteTable == null) {
                throw new RouterTableNotExist();
            }
        }

        List<RouteEntry> existRoutes = neutronRouteTable.getRouteEntities();

        // Tracking operation type for each routing rule
        for (RoutesToNeutronRouteObject newRoute : routes) {
            RouteEntry route = null;
            String newRouteDestination = newRoute.getDestination();
            String newRouteNexthop = newRoute.getNexthop();
            for (RouteEntry existRoute : existRoutes) {
                String existRouteDestination = existRoute.getDestination();
                if (existRouteDestination == null || newRouteDestination == null || newRouteNexthop == null) {
                    throw new DestinationOrNexthopCanNotBeNull();
                }
                if (existRouteDestination.equals(newRouteDestination)) {
                    route = existRoute;
                    break;
                }
            }

            if (route == null) {

                InternalRoutingRule internalRoutingRule = constructNewInternalRoutingRule(OperationType.CREATE, RoutingRuleType.NEUTRON, route);

                updateRoutes.add(internalRoutingRule);

            } else {
                // TODO: if it is vpc router, we need also compare with their 'target' field value
                if (newRouteNexthop != route.getNexthop()) {

                    InternalRoutingRule internalRoutingRule = constructNewInternalRoutingRule(OperationType.UPDATE, RoutingRuleType.NEUTRON, route);

                    updateRoutes.add(internalRoutingRule);

                    existRoutes.remove(route);

                }
            }
        }

        for (RouteEntry existRoute : existRoutes) {

            InternalRoutingRule internalRoutingRule = constructNewInternalRoutingRule(OperationType.DELETE, RoutingRuleType.NEUTRON, existRoute);

            updateRoutes.add(internalRoutingRule);

        }

        // construct internalRouterInfo
        String requestId = UUID.randomUUID().toString();
        InternalRouterInfo internalRouterInfo = new InternalRouterInfo();

        InternalRouterConfiguration internalRouterConfiguration = new InternalRouterConfiguration();

        List<InternalSubnetRoutingTable> subnetRoutingTables = new ArrayList<>();

        InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
        if (subnetId != null) {
            internalSubnetRoutingTable.setSubnetId(subnetId);
        } else {
            internalSubnetRoutingTable.setSubnetId(neutronRouteTable.getOwner());
        }
        internalSubnetRoutingTable.setRoutingRules(updateRoutes);
        subnetRoutingTables.add(internalSubnetRoutingTable);


        internalRouterConfiguration.setRevisionNumber(ConstantsConfig.REVISION_NUMBER);
        internalRouterConfiguration.setFormatVersion(ConstantsConfig.FORMAT_VERSION);
        internalRouterConfiguration.setRequestId(requestId);
        internalRouterConfiguration.setHostDvrMac("");
        internalRouterConfiguration.setMessageType(MessageType.FULL);
        internalRouterConfiguration.setSubnetRoutingTables(subnetRoutingTables);

        internalRouterInfo.setOperationType(OperationType.INFO);
        internalRouterInfo.setRouterConfiguration(internalRouterConfiguration);

        return internalRouterInfo;
    }

    private InternalRoutingRule constructNewInternalRoutingRule(OperationType operationType, RoutingRuleType routingRuleType, RouteEntry route) {
        if (route == null) {
            return new InternalRoutingRule();
        }

        InternalRoutingRule internalRoutingRule = new InternalRoutingRule();

        InternalRoutingRuleExtraInfo routingRuleExtraInfo = new InternalRoutingRuleExtraInfo();
        //routingRuleExtraInfo.setNextHopMac();
        // TODO: insert destination type - if it is vpc router, configure value according to target
        routingRuleExtraInfo.setDestinationType(VpcRouteTarget.LOCAL);

        internalRoutingRule.setId(route.getId());
        internalRoutingRule.setName(route.getName());
        internalRoutingRule.setDestination(route.getDestination());
        // TODO: translate target to nextHop - it is vpc router operation
        internalRoutingRule.setNextHopIp(route.getNexthop());
        // TODO: set priority - configure priority according to RoutingRuleType
        if (routingRuleType.getRoutingRuleType().equals(ConstantsConfig.ROUTINGRULETYPE)) {
            internalRoutingRule.setPriority(String.valueOf(route.getPriority()));
        }
        internalRoutingRule.setOperationType(operationType);
        internalRoutingRule.setRoutingRuleExtraInfo(routingRuleExtraInfo);

        return internalRoutingRule;

    }

    private boolean ValidateSubnetAndConnectedRouter(SubnetWebJson subnetWebJson, String subnetId) {
        // get subnet
        SubnetEntity subnet = subnetWebJson.getSubnet();
        if (subnet == null) {
            logger.log(Level.WARNING, "can not find subnet by subnet id :" + subnetId);
            return false;
        }

        // get subnet's route table type
        String connectedRouterId = subnet.getAttachedRouterId();
        if (connectedRouterId == null) {
            return false;
        }

        return true;
    }

    private boolean ValidateRouter(Router router) {
        if (router == null) {
            return false;
        }

        RouteTable routeTable = router.getNeutronRouteTable();
        if (routeTable == null) {
            return false;
        }

        String routeTableType = routeTable.getRouteTableType();
        if (routeTableType == null) {
            return false;
        }

        return true;
    }

    private boolean ProcessNeutronRouterAndPopulateSubnetIds(String projectId, Router router, List<SubnetEntity> subnetEntities, Map<String, String> gwPortToSubnetIdMap) throws SubnetNotBindUniquePortId {
        List<String> ports = router.getPorts();

        // check ports
        if (ports == null || ports.size() == 0) {
            return false;
        }

        for (String portId : ports) {
            // get subnet by port id
            // TODO: could maintain subnet-port mapping
            SubnetsWebJson subnetsWebJson = this.routerToSubnetService.getSubnetsByPortId(projectId, portId);
            if (subnetsWebJson == null) {
                return false;
            }

            ArrayList<SubnetEntity> subnets = subnetsWebJson.getSubnets();
            if (subnets.size() == 0) {
                return false;
            }
            if (subnets.size() > 1) {
                throw new SubnetNotBindUniquePortId();
            }

            SubnetEntity subnetEntity = subnets.get(0);
            subnetEntities.add(subnetEntity);
            gwPortToSubnetIdMap.put(portId, subnetEntity.getId());
        }

        return true;
    }

    private void PopulateInternalRouterInfo(Router router, Map<String, String> gwPortToSubnetIdMap, InternalRouterInfo internalRouterInfo) {
        InternalRouterConfiguration configuration = new InternalRouterConfiguration();
        configuration.setId(router.getId());
        configuration.setFormatVersion(ConstantsConfig.FORMAT_VERSION);
        configuration.setMessageType(MessageType.FULL);
        configuration.setRequestId("");
        configuration.setHostDvrMac(ConstantsConfig.HOST_DVR_MAC);
        configuration.setRevisionNumber(ConstantsConfig.REVISION_NUMBER);

        List<InternalSubnetRoutingTable> subnetRoutingTables = new ArrayList<>();
        for (Map.Entry<String,String> entry : gwPortToSubnetIdMap.entrySet()) {
            // NOTE: We assume that Neutron router only has one route table here
            RouteTable neutronRouteTable = router.getNeutronRouteTable();

            InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
            internalSubnetRoutingTable.setSubnetId(entry.getValue());

            List<InternalRoutingRule> routingRules = new ArrayList<>();
            List<RouteEntry> routeEntities = neutronRouteTable.getRouteEntities();
            for (RouteEntry routeEntry : routeEntities) {
                InternalRoutingRule internalRoutingRule = new InternalRoutingRule();
                internalRoutingRule.setId(routeEntry.getId());
                internalRoutingRule.setPriority(routeEntry.getPriority().toString());
                internalRoutingRule.setNextHopIp(routeEntry.getNexthop());
                internalRoutingRule.setName(routeEntry.getName());
                internalRoutingRule.setDestination(routeEntry.getDestination());
                internalRoutingRule.setOperationType(OperationType.CREATE);

                InternalRoutingRuleExtraInfo routingRuleExtraInfo = new InternalRoutingRuleExtraInfo();
                routingRuleExtraInfo.setNextHopMac("");
                routingRuleExtraInfo.setDestinationType(VpcRouteTarget.LOCAL);

                internalRoutingRule.setRoutingRuleExtraInfo(routingRuleExtraInfo);

                routingRules.add(internalRoutingRule);
            }

            internalSubnetRoutingTable.setRoutingRules(routingRules);
            subnetRoutingTables.add(internalSubnetRoutingTable);
        }

        configuration.setSubnetRoutingTables(subnetRoutingTables);

        internalRouterInfo.setOperationType(OperationType.INFO);
        internalRouterInfo.setRouterConfiguration(configuration);
    }
}
