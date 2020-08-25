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

import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.route.service.NeutronRouterService;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.service.RouterExtraAttributeDatabaseService;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class NeutronRouterServiceImpl implements NeutronRouterService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${microservices.subnet.service.url}")
    private String subnetUrl;

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private RouterExtraAttributeDatabaseService routerExtraAttributeDatabaseService;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public NeutronRouterWebRequestObject getNeutronRouter(String routerId) throws ResourceNotFoundException, ResourcePersistenceException, CanNotFindRouter {
        NeutronRouterWebRequestObject neutronRouterWebRequestObject = new NeutronRouterWebRequestObject();

        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new CanNotFindRouter();
        }

        RouterExtraAttribute routerExtraAttribute = this.routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(router.getRouterExtraAttributeId());

        BeanUtils.copyProperties(router, neutronRouterWebRequestObject);
        RouteTable routeTable = router.getRouteTable();
        neutronRouterWebRequestObject.setRouteTable(routeTable);
        if (routerExtraAttribute != null) {
            BeanUtils.copyProperties(routerExtraAttribute, neutronRouterWebRequestObject);
        }
        return neutronRouterWebRequestObject;
    }

    @Override
    public void saveRouterAndRouterExtraAttribute(NeutronRouterWebRequestObject neutronRouter) throws NeutronRouterIsNull, DatabasePersistenceException {
        if (neutronRouter == null) {
            throw new NeutronRouterIsNull();
        }

        Router router = new Router();
        RouterExtraAttribute routerExtraAttribute = new RouterExtraAttribute();

        BeanUtils.copyProperties(neutronRouter, router);
        BeanUtils.copyProperties(neutronRouter, routerExtraAttribute);
        RouteTable routeTable = neutronRouter.getRouteTable();
        router.setRouteTable(routeTable);

        this.routerDatabaseService.addRouter(router);
        this.routerExtraAttributeDatabaseService.addRouterExtraAttribute(routerExtraAttribute);

    }

    @Override
    public RouterInterfaceResponse addAnInterfaceToNeutronRouter(String portId, String subnetId, String routerId) throws SpecifyBothSubnetIDAndPortID, ResourceNotFoundException, ResourcePersistenceException, CanNotFindRouter, DatabasePersistenceException, PortIDIsAlreadyExist, PortIsAlreadyInUse, SubnetNotBindUniquePortId {
        if (portId != null && subnetId != null) {
            throw new SpecifyBothSubnetIDAndPortID();
        }

        SubnetEntity subnet = null;
        String projectId = null;
        String port = null;
        String attachedRouterId = null;

        // Only pass in the value of the port
        if (portId != null && subnetId == null) {

            // 通过port id 找到 subnet
            SubnetsWebJson subnetsWebJson = getSubnetsByPortId(portId);
            if (subnetsWebJson == null) {
                return new RouterInterfaceResponse();
            }
            ArrayList<SubnetEntity> subnets = subnetsWebJson.getSubnets();
            if (subnets.size() != 1) {
                throw new SubnetNotBindUniquePortId();
            }
            subnet = subnets.get(0);
        }
        // Only pass in the value of the subnet
        else if (portId == null && subnetId != null) {

            // get subnet by subnet id
            SubnetWebJson subnetWebJson = getSubnet(subnetId);
            if (subnetWebJson == null) {
                logger.warn("can not find subnet by subnet id :" + subnetId);
                return new RouterInterfaceResponse();
            }
            subnet = subnetWebJson.getSubnet();
        } else {
            return new RouterInterfaceResponse();
        }
        projectId = subnet.getProjectId();
        port = subnet.getPortId();
        attachedRouterId = subnet.getAttachedRouterId();

        // check port_id是否已经被其它router使用
        if (attachedRouterId != null) {
            throw new PortIsAlreadyInUse();
        }
        subnet.setAttachedRouterId(routerId);

        // update 两个device fields
        PortEntity portEntity = new PortEntity();
        portEntity.setDeviceId(routerId);
        portEntity.setDeviceOwner("network:router_interface");
        subnet.setPort(portEntity);

        // update subnet
        updateSubnet(projectId, subnetId, subnet);

        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new CanNotFindRouter();
        }
        List<String> ports = router.getPorts();

        if (ports.contains(portId)) {
            throw new PortIDIsAlreadyExist();
        }
        ports.add(portId);
        router.setPorts(ports);
        this.routerDatabaseService.addRouter(router);

        // 构造 response
        List<String> subnetIds = new ArrayList<>(){{add(subnetId);}};
        return new RouterInterfaceResponse(routerId, subnet.getVpcId(), portId, subnetId, subnetIds, projectId, projectId, subnet.getTags());
    }

    @Override
    public RouterInterfaceResponse removeAnInterfaceToNeutronRouter(String portId, String subnetId, String routerId) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, AttachedPortsNotMatchPortId, RouterTableNotExist, RouterInterfaceAreUsedByRoutes, SubnetNotBindUniquePortId {
        SubnetEntity subnet = null;
        String projectId = null;
        String attachedPort = null;
        String attachedRouterId = null;

        // 如果两个一起传，检查是否有冲突
        if (portId != null && subnetId != null) {
            SubnetWebJson subnetWebJson = getSubnet(subnetId);
            if (subnetWebJson == null) {
                logger.warn("can not find subnet by subnet id :" + subnetId);
                return new RouterInterfaceResponse();
            }
            subnet = subnetWebJson.getSubnet();
            attachedPort = subnet.getPortId();
            if (attachedPort != null) {
                if (!attachedPort.equals(portId)) {
                    throw new AttachedPortsNotMatchPortId();
                }
            } else {
                logger.warn("There is no IP address on the port");
                return new RouterInterfaceResponse();
            }

        }else if (portId != null && subnetId == null) {
            // 通过port id 找到 subnet
            SubnetsWebJson subnetsWebJson = getSubnetsByPortId(portId);
            if (subnetsWebJson == null) {
                return new RouterInterfaceResponse();
            }
            ArrayList<SubnetEntity> subnets = subnetsWebJson.getSubnets();
            if (subnets.size() != 1) {
                throw new SubnetNotBindUniquePortId();
            }
            subnet = subnets.get(0);
        }else if (portId == null && subnetId != null) {
            // get subnet by subnet id
            SubnetWebJson subnetWebJson = getSubnet(subnetId);
            if (subnetWebJson == null) {
                logger.warn("can not find subnet by subnet id :" + subnetId);
                return new RouterInterfaceResponse();
            }
            subnet = subnetWebJson.getSubnet();
        }else {
            return new RouterInterfaceResponse();
        }

        // 第二个 check
        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new RouterOrSubnetAndPortNotExistOrNotVisible();
        }

        // 第三个check
        projectId = subnet.getProjectId();
        attachedRouterId = subnet.getAttachedRouterId();
        String gatewayIp = subnet.getGatewayIp();
        if (gatewayIp != null) {
            RouteTable routeTable = router.getRouteTable();
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
        subnet.setPortId(null);

        // update 两个device fields
        PortEntity portEntity = new PortEntity();
        portEntity.setDeviceId(routerId);
        portEntity.setDeviceOwner("network:router_interface");
        subnet.setPort(portEntity);

        // update subnet
        updateSubnet(projectId, subnetId, subnet);

        // 构造 response
        List<String> subnetIds = new ArrayList<>(){{add(subnetId);}};
        return new RouterInterfaceResponse(routerId, subnet.getVpcId(), portId, subnetId, subnetIds, projectId, projectId, subnet.getTags());

    }

    @Override
    public RoutesToNeutronWebResponse addRoutesToNeutronRouter(String routerid, RoutesToNeutronRouterRequestObject requestRouter) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, DatabasePersistenceException, DestinationOrNexthopCanNotBeNull {
        RoutesToNeutronRouterResponseObject responseRouter = new RoutesToNeutronRouterResponseObject();
        List<RoutesToNeutronRouteObject> responseRoutes = new ArrayList<>();

        Router router = this.routerDatabaseService.getByRouterId(routerid);
        if (router == null) {
            throw new RouterOrSubnetAndPortNotExistOrNotVisible();
        }
        RouteTable routeTable = router.getRouteTable();
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
        router.setRouteTable(routeTable);
        this.routerDatabaseService.addRouter(router);

        // 构造 response
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
        RouteTable routeTable = router.getRouteTable();
        List<RouteEntry> routeEntities = routeTable.getRouteEntities();

        List<RoutesToNeutronRouteObject> requestRoutes = requestRouter.getRoutes();
        for (RoutesToNeutronRouteObject requestRoute : requestRoutes) {
            String requestDestination = requestRoute.getDestination();
            String requestNexthop = requestRoute.getNexthop();

            if (requestDestination == null || requestNexthop == null) {
                throw new DestinationOrNexthopCanNotBeNull();
            }

            for (int i = 0; i < routeEntities.size(); i ++) {
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
        router.setRouteTable(routeTable);
        this.routerDatabaseService.addRouter(router);

        // 构造 response
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
    public SubnetWebJson getSubnet(String subnetId) {
        String subnetManagerServiceUrl = subnetUrl + "/subnets/" + subnetId;
        SubnetWebJson response = restTemplate.getForObject(subnetManagerServiceUrl, SubnetWebJson.class);
        return response;
    }

    @Override
    public SubnetsWebJson getSubnetsByPortId(String portId) {
        String subnetManagerServiceUrl = subnetUrl + "/subnets?port_id=" + portId;
        SubnetsWebJson response = restTemplate.getForObject(subnetManagerServiceUrl, SubnetsWebJson.class);
        return response;
    }

    @Override
    public void updateSubnet(String projectid, String subnetId, SubnetEntity subnet) {
        String subnetManagerServiceUrl = subnetUrl + "/project/" + projectid + "/subnets/" + subnetId;
        HttpEntity<SubnetWebJson> request = new HttpEntity<>(new SubnetWebJson(subnet));
        restTemplate.put(subnetManagerServiceUrl, request, SubnetWebJson.class);
    }

}
