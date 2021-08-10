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
import com.futurewei.alcor.common.enumClass.*;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.route.config.ConstantsConfig;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.route.service.*;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.subnet.HostRoute;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import com.futurewei.alcor.common.logging.*;
import org.jetbrains.annotations.NotNull;
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

    @Autowired
    private RouteTableDatabaseService routeTableDatabaseService;

    @Autowired
    private RouterToPMService routerToPMService;

    @Autowired
    private RouterService routerService;

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
        if (routeTable == null || routeTable.getRouteEntities() == null) {
            routeTable = new RouteTable();
            List<RouteEntry> routeEntities = new ArrayList<>();
            String routeTableId = UUID.randomUUID().toString();
            routeTable.setOwner(neutronRouter.getId());
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
    public RouterInterfaceResponse addAnInterfaceToNeutronRouter(String projectid, String portId, String subnetId, String routerId)
            throws SpecifyBothSubnetIDAndPortID, ResourceNotFoundException, ResourcePersistenceException, RouterUnavailable,
            DatabasePersistenceException, PortIDIsAlreadyExist, PortIsAlreadyInUse, SubnetNotBindUniquePortId, RouterHasMultipleVPCs {
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
        portId = subnet.getGatewayPortDetail().getGatewayPortId();
        attachedRouterId = subnet.getAttachedRouterId();

        // check if port_id is used by other router
        if (attachedRouterId != null && !attachedRouterId.equals("")) {
            throw new PortIsAlreadyInUse();
        }
        subnet.setAttachedRouterId(routerId);

        /*
           In order to make Neutron router compatible with VPC scenario.
           We only allow subnet's gateways from the same VPC can be attached to router.
           We use VPC from the first attached gateway as router's owner
           If any attaching gateway after the first one has different VPC, we will issue a warning message.
         */
        List<String> gwPorts = router.getGatewayPorts();
        if (gwPorts == null || gwPorts.size() == 0) {
            router.setOwner(subnet.getVpcId());
        } else {
            for (String port : gwPorts) {
                SubnetsWebJson subnet_o = this.routerToSubnetService.getSubnetsByPortId(router.getProjectId(), port);
                if (!subnet_o.getSubnets().get(0).getVpcId().equals(subnet.getVpcId())) {
                    throw new RouterHasMultipleVPCs();
                }
            }
        }

        // update device_id and device_owner in PM
        PortEntity portEntity = new PortEntity();
        portEntity.setDeviceId(routerId);
        portEntity.setDeviceOwner("network:router_interface");
        subnet.setPort(portEntity);

        this.routerToPMService.updatePort(projectid, portId, portEntity);

        // update subnet
        this.routerToSubnetService.updateSubnet(projectId, subnetid, subnet);
        // TODO: may need to maintain the mapping for new added port and it's subnet in the Route Manager

        List<String> ports = router.getGatewayPorts();
        if (ports == null) {
            ports = new ArrayList<>();
        }

        if (ports.contains(portId)) {
            throw new PortIDIsAlreadyExist();
        }
        ports.add(portId);
        router.setGatewayPorts(ports);
        //update subnet_ids
        List<String> subnet_Ids = router.getSubnetIds();
        if (subnet_Ids == null) {
            subnet_Ids = new ArrayList<>();
        }
        subnet_Ids.add(subnetid);
        router.setSubnetIds(subnet_Ids);

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
            attachedPort = subnet.getGatewayPortDetail().getGatewayPortId();
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
        if (attachedRouterId == null) {
            throw new ResourceNotFoundException();
        }

        String gatewayIp = subnet.getGatewayIp();
        if (gatewayIp != null) {
            RouteTable routeTable = router.getNeutronRouteTable();
            if (routeTable == null) {
                throw new RouterTableNotExist();
            }

            List<RouteEntry> routeEntities = routeTable.getRouteEntities();
            if (routeEntities != null) {
                for (RouteEntry routeEntry : routeEntities) {
                    String nextHop = routeEntry.getNexthop();
                    if (gatewayIp.equals(nextHop)) {
                        throw new RouterInterfaceAreUsedByRoutes();
                    }
                }
            }
            // else part:
            // the router doesn't come with a default routetable which is the OpenStack's scenario, just ignore it.
        }

        // remove interface
        subnet.setAttachedRouterId("");

        List<String> ports = router.getGatewayPorts();
        if (ports == null) {
            return new RouterInterfaceResponse();
        }

        if (portId == null) {
            portId = subnet.getGatewayPortDetail().getGatewayPortId();
        }

        if (ports.contains(portId)) {
            ports.remove(portId);
        }
        router.setGatewayPorts(ports);
        //update subnet_ids
        List<String> subnet_Ids = router.getSubnetIds();
        if (subnet_Ids == null) {
            subnet_Ids = new ArrayList<>();
        }
        subnet_Ids.remove(subnetid);
        router.setSubnetIds(subnet_Ids);

        this.routerDatabaseService.addRouter(router);

        // update device_id and device_owner
        PortEntity portEntity = new PortEntity();
        portEntity.setDeviceId(null);
        portEntity.setDeviceOwner(null);
        subnet.setPort(portEntity);

        this.routerToPMService.updatePort(projectid, portId, portEntity);

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
    public RoutesToNeutronWebResponse addRoutesToNeutronRouter(String routerid, NewRoutesWebRequest requestRouter) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, DatabasePersistenceException, DestinationOrNexthopCanNotBeNull, DestinationSame {
        RoutesToNeutronRouterResponseObject responseRouter = new RoutesToNeutronRouterResponseObject();
        List<NewRoutesRequest> responseRoutes = new ArrayList<>();

        Router router = this.routerDatabaseService.getByRouterId(routerid);
        if (router == null) {
            throw new RouterOrSubnetAndPortNotExistOrNotVisible();
        }
        RouteTable routeTable = router.getNeutronRouteTable();
        List<RouteEntry> routeEntities = routeTable.getRouteEntities();


        List<NewRoutesRequest> requestRoutes = requestRouter.getRoutes();
        for (NewRoutesRequest requestRoute : requestRoutes) {
            String requestDestination = requestRoute.getDestination();
            String requestNexthop = requestRoute.getNexthop();

            if (requestDestination == null || requestNexthop == null) {
                throw new DestinationOrNexthopCanNotBeNull();
            }

            for (RouteEntry routeEntry : routeEntities) {
                String destination = routeEntry.getDestination();
                if (requestDestination.equals(destination)) {
                    throw new DestinationSame();
                }
            }

            RouteEntry routeEntry = new RouteEntry();
            routeEntry.setDestination(requestDestination);
            routeEntry.setNexthop(requestNexthop);
            routeEntities.add(routeEntry);

        }
        routeTable.setRouteEntities(routeEntities);
        router.setNeutronRouteTable(routeTable);
        this.routerDatabaseService.addRouter(router);

        // Construct response
        for (RouteEntry routeEntry : routeEntities) {
            String destination = routeEntry.getDestination();
            String nexthop = routeEntry.getNexthop();
            NewRoutesRequest newRoutesRequest = new NewRoutesRequest(destination, nexthop);
            responseRoutes.add(newRoutesRequest);
        }
        responseRouter.setId(routerid);
        responseRouter.setName(router.getName());
        responseRouter.setRoutes(responseRoutes);

        return new RoutesToNeutronWebResponse(responseRouter);
    }

    @Override
    public RoutesToNeutronWebResponse removeRoutesFromNeutronRouter(String routerid, NewRoutesWebRequest requestRouter) throws RouterOrSubnetAndPortNotExistOrNotVisible, ResourceNotFoundException, ResourcePersistenceException, DestinationOrNexthopCanNotBeNull, DatabasePersistenceException {
        RoutesToNeutronRouterResponseObject responseRouter = new RoutesToNeutronRouterResponseObject();
        List<NewRoutesRequest> responseRoutes = new ArrayList<>();

        Router router = this.routerDatabaseService.getByRouterId(routerid);
        if (router == null) {
            throw new RouterOrSubnetAndPortNotExistOrNotVisible();
        }
        RouteTable routeTable = router.getNeutronRouteTable();
        List<RouteEntry> routeEntities = routeTable.getRouteEntities();
        List<NewRoutesRequest> requestRoutes = requestRouter.getRoutes();

        // TODO: time complexity O(n^2), check if it effect performance
        requestRoutes.forEach(item -> {
            routeEntities.removeIf(routingRule -> routingRule.getDestination().equals(item.getDestination()) && routingRule.getNexthop().equals(item.getNexthop()));
        });
        this.routerDatabaseService.addRouter(router);

        // Construct response
        for (RouteEntry routeEntry : routeEntities) {
            String destination = routeEntry.getDestination();
            String nexthop = routeEntry.getNexthop();
            NewRoutesRequest newRoutesRequest = new NewRoutesRequest(destination, nexthop);
            responseRoutes.add(newRoutesRequest);
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
    public UpdateRoutingRuleResponse updateRoutingRule (String owner, NewRoutesWebRequest newRouteEntry, boolean isDefaultRoutingRules, boolean isAddOperation) throws CacheException, CanNotFindRouteTableByOwner, QueryParamTypeNotSupportException, RouteTableNotUnique, DestinationInvalid, DatabasePersistenceException {
        List<InternalRoutingRule> updateRoutes = new ArrayList<>();
        List<HostRoute> hostRouteToSubnet = new ArrayList<>();

        List<NewRoutesRequest> routes = newRouteEntry.getRoutes();
        if (routes == null) {
            return new UpdateRoutingRuleResponse(new InternalSubnetRoutingTable(), hostRouteToSubnet);
        }

        // find routeTable
        Map<String, Object[]> queryParams =  new HashMap<>();
        Object[] value = new Object[1];
        value[0] = owner;
        queryParams.put("owner", value);

        Map<String, RouteTable> routeTableMap = this.routeTableDatabaseService.getAllRouteTables(queryParams);
        List<RouteTable> routeTables = new ArrayList<>(routeTableMap.values());
        if (routeTables == null || routeTables.size() == 0) {
            logger.log(Level.WARNING, "owner: " + owner);
            throw new CanNotFindRouteTableByOwner();
        } else if (routeTables.size() >= 2) {
            logger.log(Level.WARNING, "owner: " + owner);
            throw new RouteTableNotUnique();
        }

        RouteTable existRouteTable = routeTables.get(0);

        List<RouteEntry> existRoutes = existRouteTable.getRouteEntities();
        // TODO: existRoutes -> MAP: key - des, value - nexthop, "10.0.0.0/16"
        Map<String, RouteEntry> existRoutesMap = new HashMap<>();
        for (RouteEntry existRoute : existRoutes) {
            String[] existDes = existRoute.getDestination().split("\\/");
            String existNetworkIP = existDes[0];
            existRoutesMap.put(existNetworkIP, existRoute);
        }
        // TODO: existRoutes replaced with map
        // Tracking operation type for each routing rule
        for (NewRoutesRequest newRouteRequest : routes) {
            //RouteEntry route = null;
            // TODO: 1. check new-des if it is valid
            String newRouteDestination = newRouteRequest.getDestination();
            String newRouteNexthop = newRouteRequest.getNexthop();
            boolean isDestinationValid = verifyCidrBlock(newRouteDestination);
            if (!isDestinationValid) {
                throw new DestinationInvalid("destination is invalid : " + newRouteDestination);
            }
            String[] newRouteDes = newRouteDestination.split("\\/");
            String newNetworkIP = newRouteDes[0];
            String newBitmask = newRouteDes[1];
            int newBitmaskInt = Integer.parseInt(newBitmask);
            // TODO: 2. flag check if it is from vpc/Neutron(lower priority) or subnet.
            RouteEntry existRoute = existRoutesMap.get(newNetworkIP);
            // can't find: Add Operation - Create (both); Remove Operation - Skip
            if (existRoute == null) {
                if (!isAddOperation) { // Remove Operation - Skip
                    continue;
                }

                InternalRoutingRule internalRoutingRule = null;
                if (isDefaultRoutingRules) {
                    internalRoutingRule = constructNewInternalRoutingRule(OperationType.CREATE, RoutingRuleType.DEFAULT, existRoute, newRouteRequest);

                    existRoutes.add(new RouteEntry(existRouteTable.getProjectId(),
                            UUID.randomUUID().toString(),
                            "route" + UUID.randomUUID().toString(),
                            null,
                            internalRoutingRule.getDestination(),
                            null,
                            ConstantsConfig.LOW_PRIORITY,
                            existRouteTable.getId(),
                            internalRoutingRule.getNextHopIp()));
                } else {
                    internalRoutingRule = constructNewInternalRoutingRule(OperationType.CREATE, RoutingRuleType.STATIC, existRoute, newRouteRequest);
                }

                String Des = internalRoutingRule.getDestination();
                String Nexthop = internalRoutingRule.getNextHopIp();

                updateRoutes.add(internalRoutingRule);
                hostRouteToSubnet.add(new HostRoute(){{setNexthop(Nexthop);setDestination(Des);}});

            } else { // could find:
                // compare which one cover the other one
                if (isDefaultRoutingRules) { // VPC/Neutron
                    String[] existDes = existRoute.getDestination().split("\\/");
                    String existBitmask = existDes[1];
                    int existBitmaskInt = Integer.parseInt(existBitmask);
                    if (isAddOperation) {
                        if (newBitmaskInt <= existBitmaskInt) { // new routing rule bitmask is smaller or equal than old one, drop it
                            continue;
                        } else { // new routing rule bitmask is larger than old one, Create new rule with low priority

                            InternalRoutingRule internalRoutingRule = constructNewInternalRoutingRule(OperationType.CREATE, RoutingRuleType.DEFAULT, existRoute, newRouteRequest);

                            String Des = internalRoutingRule.getDestination();
                            String Nexthop = internalRoutingRule.getNextHopIp();

                            existRoutes.add(new RouteEntry(existRouteTable.getProjectId(),
                                    UUID.randomUUID().toString(),
                                    "route" + UUID.randomUUID().toString(),
                                    null,
                                    Des,
                                    null,
                                    ConstantsConfig.LOW_PRIORITY,
                                    existRouteTable.getId(),
                                    Nexthop));
                            updateRoutes.add(internalRoutingRule);

                        }
                    } else { // remove operation
                        if (newBitmaskInt == existBitmaskInt) { // remove default routes

                            InternalRoutingRule internalRoutingRule = constructNewInternalRoutingRule(OperationType.DELETE, RoutingRuleType.DEFAULT, existRoute, null);

                            existRoutes.remove(existRoute);
                            updateRoutes.add(internalRoutingRule);
                        }

                    }

                } else { // Subnet
                    // new routing rule update old one without checking bitmask
                    InternalRoutingRule internalRoutingRule = constructNewInternalRoutingRule(OperationType.UPDATE, RoutingRuleType.STATIC, existRoute, newRouteRequest);

                    updateRoutes.add(internalRoutingRule);
                    hostRouteToSubnet.add(new HostRoute(){{setNexthop(internalRoutingRule.getNextHopIp());setDestination(internalRoutingRule.getDestination());}});

                }
                existRoutesMap.remove(newNetworkIP);
            }

        }

        if (!isDefaultRoutingRules) {// if from VPC/Neutron, it shouldn't delete default routing rules
            for (Map.Entry<String, RouteEntry> existRouteEntry : existRoutesMap.entrySet()) {
                RouteEntry existRoute = (RouteEntry)existRouteEntry.getValue();
                InternalRoutingRule internalRoutingRule = constructNewInternalRoutingRule(OperationType.DELETE, RoutingRuleType.DEFAULT, existRoute, null);

                updateRoutes.add(internalRoutingRule);

            }
        }

        // update subnet route table
        existRouteTable.setRouteEntities(existRoutes);
        this.routeTableDatabaseService.addRouteTable(existRouteTable);

        // construct List<InternalSubnetRoutingTable>
        InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
        internalSubnetRoutingTable.setSubnetId(owner);

        internalSubnetRoutingTable.setRoutingRules(updateRoutes);

        // construct UpdateRoutingRuleResponse
        UpdateRoutingRuleResponse updateRoutingRuleResponse = new UpdateRoutingRuleResponse(internalSubnetRoutingTable, hostRouteToSubnet);

        return updateRoutingRuleResponse;
    }

    @Override
    public InternalRouterInfo constructInternalRouterInfo (String routerId, List<InternalSubnetRoutingTable> internalSubnetRoutingTableList) {

        String requestId = UUID.randomUUID().toString();
        InternalRouterInfo internalRouterInfo = new InternalRouterInfo();

        InternalRouterConfiguration internalRouterConfiguration = new InternalRouterConfiguration();

        internalRouterConfiguration.setSubnetRoutingTables(internalSubnetRoutingTableList);

        internalRouterConfiguration.setId(routerId);
        internalRouterConfiguration.setRevisionNumber(ConstantsConfig.REVISION_NUMBER);
        internalRouterConfiguration.setFormatVersion(ConstantsConfig.FORMAT_VERSION);
        internalRouterConfiguration.setRequestId(requestId);
        internalRouterConfiguration.setHostDvrMac("");
        internalRouterConfiguration.setMessageType(MessageType.FULL);

        internalRouterInfo.setOperationType(OperationType.INFO);
        internalRouterInfo.setRouterConfiguration(internalRouterConfiguration);

        return internalRouterInfo;

    }

    @Override
    public List<InternalSubnetRoutingTable> constructInternalSubnetRoutingTables(Router router) throws Exception {
        if (router == null) {
            return new ArrayList<>();
        }

        //        List<RouteTable> neutronSubnetRouteTables = router.getNeutronSubnetRouteTables();
        //        if (neutronSubnetRouteTables == null) {
        //            return new ArrayList<>();
        //        }

        List<String> subnetIds = router.getSubnetIds();
        List<RouteTable> neutronSubnetRouteTables = getRouteTablesBySubnetIds(subnetIds, router.getProjectId());
        if (neutronSubnetRouteTables == null) {
            return new ArrayList<>();
        }

        List<InternalSubnetRoutingTable> internalSubnetRoutingTables = new ArrayList<>();

        for (RouteTable routeTable : neutronSubnetRouteTables) {
            InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
            List<RouteEntry> routeEntities = routeTable.getRouteEntities();
            String owner = routeTable.getOwner();

            List<InternalRoutingRule> routing_rules = new ArrayList<>();
            if (routeEntities != null) {
                for (RouteEntry routeEntry : routeEntities) {
                    InternalRoutingRule internalRoutingRule = new InternalRoutingRule(
                            routeEntry.getId(),
                            routeEntry.getName(),
                            routeEntry.getDestination(),
                            routeEntry.getNexthop(),
                            routeEntry.getPriority(),
                            OperationType.INFO,
                            null);
                    routing_rules.add(internalRoutingRule);
                }
            }

            internalSubnetRoutingTable.setSubnetId(owner);
            internalSubnetRoutingTable.setRoutingRules(routing_rules);
            internalSubnetRoutingTables.add(internalSubnetRoutingTable);
        }
        return internalSubnetRoutingTables;
    }
    
    @Override
    public List<RouteTable> getRouteTablesBySubnetIds(List<String> subnetIds, String projectid) throws Exception {
        if (subnetIds == null) {
            return null;
        }

        List<RouteTable> routeTables = new ArrayList<>();

        for (String subnetId : subnetIds) {
            RouteTable routeTable = new RouteTable(this.routerService.getSubnetRouteTable(projectid, subnetId));
            routeTables.add(routeTable);
        }

        return routeTables;
    }

    private InternalRoutingRule constructNewInternalRoutingRule(OperationType operationType, RoutingRuleType routingRuleType, RouteEntry route, NewRoutesRequest newRouteRequest) {
        if (route == null && newRouteRequest == null) {
            return new InternalRoutingRule();
        }

        InternalRoutingRule internalRoutingRule = new InternalRoutingRule();

        InternalRoutingRuleExtraInfo routingRuleExtraInfo = new InternalRoutingRuleExtraInfo();
        //routingRuleExtraInfo.setNextHopMac();
        // TODO: insert destination type - if it is vpc router, configure value according to target
        routingRuleExtraInfo.setDestinationType(VpcRouteTarget.LOCAL);

        if (route == null) {
            internalRoutingRule.setId(UUID.randomUUID().toString());
        } else {
            internalRoutingRule.setId(route.getId());
            internalRoutingRule.setName(route.getName());
        }

        if (newRouteRequest != null) {
            internalRoutingRule.setDestination(newRouteRequest.getDestination());
        } else {
            internalRoutingRule.setDestination(route.getDestination());
        }
        // TODO: translate target to nextHop - it is vpc router operation
        if (newRouteRequest != null) {
            internalRoutingRule.setNextHopIp(newRouteRequest.getNexthop());
        } else {
            internalRoutingRule.setNextHopIp(route.getNexthop());
        }
        // TODO: set priority - configure priority according to RoutingRuleType
        if (routingRuleType.getRoutingRuleType().equals(ConstantsConfig.DEFAULT_ROUTINGRULETYPE)) {
            internalRoutingRule.setPriority(ConstantsConfig.LOW_PRIORITY);
        } else if (routingRuleType.getRoutingRuleType().equals(ConstantsConfig.STATIC_ROUTINGRULETYPE)) {
            internalRoutingRule.setPriority(ConstantsConfig.HIGH_PRIORITY);
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
        List<String> ports = router.getGatewayPorts();

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
            // TODO: need to deal with Neutron router's default routing table
            RouteTable neutronRouteTable = router.getNeutronRouteTable();

            InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
            internalSubnetRoutingTable.setSubnetId(entry.getValue());
            RouteTable subnetRouteTable = null;
            try {
                subnetRouteTable = this.routerService.getSubnetRouteTable(router.getProjectId(), entry.getValue());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Subnet" + entry.getValue() + "'s routing table is empty!");;
            }

            List<InternalRoutingRule> routingRules = new ArrayList<>();
            List<RouteEntry> routeEntities = new ArrayList<>();
            //List<RouteEntry> routeEntities = neutronRouteTable.getRouteEntities();
            if (subnetRouteTable != null) {
                routeEntities = subnetRouteTable.getRouteEntities();
            }
            for (RouteEntry routeEntry : routeEntities) {
                InternalRoutingRule internalRoutingRule = new InternalRoutingRule();
                internalRoutingRule.setId(routeEntry.getId());
                internalRoutingRule.setPriority(routeEntry.getPriority());
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

    public boolean verifyCidrBlock(String cidr) {
        if (cidr == null) {
            return false;
        }
        String[] cidrs = cidr.split("\\/", -1);
        // verify cidr suffix
        if (cidrs.length > 2 || cidrs.length == 0) {
            return false;
        } else if (cidrs.length == 2) {
            if (!ControllerUtil.isPositive(cidrs[1])) {
                return false;
            }
            int suffix = Integer.parseInt(cidrs[1]);
            // if suffix verification is correcct, then verify prefix.
            if (suffix < 16 || suffix > 28) {
                if (suffix == 0 && "0.0.0.0".equals(cidrs[0]))
                {
                    return true;
                } else {
                    return false;
                }
            }
        }
        // verify cidr prefix
        String[] addr = cidrs[0].split("\\.", -1);
        if (addr.length != 4) {
            return false;
        }
        for (String f : addr) {
            if (!ControllerUtil.isPositive(f)) {
                return false;
            }
            int n = Integer.parseInt(f);
            if (n < 0 || n > 255) {
                return false;
            }
        }
        return true;

    }
}
