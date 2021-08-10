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
package com.futurewei.alcor.route.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.OperationType;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourceNotValidException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.route.exception.NeutronRouteTableIsNull;
import com.futurewei.alcor.route.exception.RouterUnavailable;
import com.futurewei.alcor.route.exception.RouterHasAttachedInterfaces;
import com.futurewei.alcor.route.service.*;
import com.futurewei.alcor.route.utils.RouteManagerUtil;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.common.logging.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class NeutronRouterController {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private RouterExtraAttributeDatabaseService routerExtraAttributeDatabaseService;

    @Autowired
    private NeutronRouterService neutronRouterService;

    @Autowired
    private RouterToDPMService routerToDPMService;

    @Autowired
    private RouterToPMService routerToPMService;

    @Autowired
    private HttpServletRequest request;

    /**
     * Show a Neutron router
     * @param routerid
     * @param projectid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/routers/{routerid}"})
    @DurationStatistics
    public NeutronRouterWebJson getNeutronRouterByRouterId(@PathVariable String projectid,@PathVariable String routerid) throws Exception {

        NeutronRouterWebRequestObject neutronRouterWebRequestObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            neutronRouterWebRequestObject = this.neutronRouterService.getNeutronRouter(routerid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (RouterUnavailable e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + routerid);
            return new NeutronRouterWebJson();
        }

        return new NeutronRouterWebJson(neutronRouterWebRequestObject);
    }

    /**
     * List Neutron routers
     * @param projectid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/routers"})
    @DurationStatistics
    public NeutronRoutersWebJson getNeutronRouters(@PathVariable String projectid) throws Exception {

        List<NeutronRouterWebRequestObject> neutronRouters = new ArrayList<>();

        Map<String, Router> routers = null;
        RouterExtraAttribute routerExtraAttribute = null;

        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(request.getParameterMap(), Router.class);

        ControllerUtil.handleUserRoles(request.getHeader(ControllerUtil.TOKEN_INFO_HEADER), queryParams);
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routers = this.routerDatabaseService.getAllRouters(queryParams);
            if (routers == null) {
                return new NeutronRoutersWebJson();
            }

            for (Map.Entry<String, Router> entry : routers.entrySet()) {
                NeutronRouterWebRequestObject neutronRouterWebRequestObject = new NeutronRouterWebRequestObject();
                Router router = (Router) entry.getValue();
                String routerExtraAttributeId = router.getRouterExtraAttributeId();
                if (routerExtraAttributeId != null && !routerExtraAttributeId.equals("")) {
                    routerExtraAttribute = this.routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(routerExtraAttributeId);
                }

                if (routerExtraAttribute != null) {
                    BeanUtils.copyProperties(routerExtraAttribute, neutronRouterWebRequestObject);
                }
                BeanUtils.copyProperties(router, neutronRouterWebRequestObject);

                neutronRouters.add(neutronRouterWebRequestObject);
            }

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return new NeutronRoutersWebJson(neutronRouters);
    }

    /**
     * Create a Neutron router
     * @param projectid
     * @param resource
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/routers"})
    @DurationStatistics
    public NeutronRouterWebJson createNeutronRouters(@PathVariable String projectid, @RequestBody NeutronRouterWebJson resource) throws Exception {

        NeutronRouterWebRequestObject neutronRouterWebRequestObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);

            // check resource
            if (!RouteManagerUtil.checkNeutronRouterWebResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            neutronRouterWebRequestObject = resource.getRouter();
            String id = neutronRouterWebRequestObject.getId();

            if (id == null || StringUtils.isEmpty(id)) {
                UUID routerId = UUID.randomUUID();
                neutronRouterWebRequestObject.setId(routerId.toString());
            }
            RestPreconditionsUtil.verifyResourceNotNull(neutronRouterWebRequestObject);

            // configure default value
            neutronRouterWebRequestObject.setProjectId(projectid);
            neutronRouterWebRequestObject = RouteManagerUtil.configureNeutronRouterParameters(neutronRouterWebRequestObject);

            // save router and router_extra_attribute
            neutronRouterWebRequestObject = this.neutronRouterService.saveRouterAndRouterExtraAttribute(neutronRouterWebRequestObject);

        } catch (Exception e) {
            throw e;
        }

        return new NeutronRouterWebJson(neutronRouterWebRequestObject);
    }

    /**
     * Update a Neutron router
     * @param projectid
     * @param routerid
     * @param resource
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/routers/{routerid}"})
    @DurationStatistics
    public NeutronRouterWebJson updateNeutronRouterByRouterId(@PathVariable String projectid,@PathVariable String routerid, @RequestBody NeutronRouterWebJson resource) throws Exception {
        NeutronRouterWebRequestObject inNeutronRouter = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);

            // check resource
            if (!RouteManagerUtil.checkNeutronRouterWebResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            NeutronRouterWebRequestObject neutronRouterWebRequestObject = resource.getRouter();
            inNeutronRouter = this.neutronRouterService.getNeutronRouter(routerid);

            RouteManagerUtil.copyPropertiesIgnoreNull(neutronRouterWebRequestObject, inNeutronRouter);

            // save router and router_extra_attribute
            inNeutronRouter = this.neutronRouterService.saveRouterAndRouterExtraAttribute(inNeutronRouter);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (RouterUnavailable e) {
            throw e;
        }

        return new NeutronRouterWebJson(inNeutronRouter);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/routers/{routerid}"})
    @DurationStatistics
    public ResponseId deleteNeutronRouterByRouterId(@PathVariable String projectid, @PathVariable String routerid) throws Exception {

        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerid);
        RestPreconditionsUtil.verifyResourceFound(projectid);

        Router router = this.routerDatabaseService.getByRouterId(routerid);
        if (router == null) {
            return new ResponseId();
        }
        List<String> ports = router.getGatewayPorts();
        if (ports != null && ports.size() != 0) {
            throw new RouterHasAttachedInterfaces();
        }
        // TODO: also need to consider internet gw port, if internet gateway ports for subnets are still in this router, the deletion should return error message to user. Now we unset internet gw
        this.routerDatabaseService.deleteRouter(routerid);

        RouterExtraAttribute routerExtraAttribute = this.routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(router.getRouterExtraAttributeId());
        if (routerExtraAttribute == null) {
            return new ResponseId();
        }
        this.routerExtraAttributeDatabaseService.deleteRouterExtraAttribute(routerExtraAttribute.getId());

        return new ResponseId(routerid);

    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/routers/{routerid}/add_router_interface"})
    @DurationStatistics
    public RouterInterfaceResponse addInterfaceToNeutronRouter(@PathVariable String projectid, @PathVariable String routerid, @RequestBody RouterInterfaceRequest resource) throws Exception {

        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerid);
        RestPreconditionsUtil.verifyResourceFound(projectid);

        String portId = resource.getPortId();
        String subnetId = resource.getSubnetId();

        RouterInterfaceResponse routerInterfaceResponse = this.neutronRouterService.addAnInterfaceToNeutronRouter(projectid, portId, subnetId, routerid);

        if (subnetId == null) {
            subnetId = routerInterfaceResponse.getSubnetId();
        }

        // TODO: return all connected subnet-ids to Port Manager. The algorithm as follow:
        //1. get ports array from the router.
        Router router = this.routerDatabaseService.getByRouterId(routerid);
        List<String> gatewayPorts = router.getGatewayPorts();
        //2. call Port Manager's /project/{project_id}/update-l3-neighbors/{new_subnet_id} with BODY {operation_type, vpcid, [old_subnet_ids]}.
        //Need to check if there is only one gateway port exists in the current router, we don't need to request PM for update-l3-neighbors. This operation only happen when there are more than 2 ports exist in the router.
        if (gatewayPorts != null && gatewayPorts.size() > 0) {
            // TODO: waiting for PM new API
            // construct InternalRouterInfo
            RouteTable neutronRouteTable = router.getNeutronRouteTable();
            if (neutronRouteTable == null) {
                throw new NeutronRouteTableIsNull();
            }
            List<RouteEntry> routeEntities = neutronRouteTable.getRouteEntities();
            List<NewRoutesRequest> routes = new ArrayList<>();
            for (RouteEntry routeEntry : routeEntities) {
                NewRoutesRequest newRoutesRequest = new NewRoutesRequest(routeEntry.getDestination(), routeEntry.getNexthop());
                routes.add(newRoutesRequest);
            }

            NewRoutesWebRequest routerRoutingRules = new NewRoutesWebRequest(routes);

            UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetId, routerRoutingRules, true, true);
            InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();
            List<InternalSubnetRoutingTable> internalSubnetRoutingTableList = this.neutronRouterService.constructInternalSubnetRoutingTables(router);
            internalSubnetRoutingTableList.add(internalSubnetRoutingTable);
            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(routerid, internalSubnetRoutingTableList);
            this.routerToPMService.updateL3Neighbors(projectid, router.getOwner(), subnetId, "add", gatewayPorts, internalRouterInfo);
        }

        return routerInterfaceResponse;

    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/routers/{routerid}/remove_router_interface"})
    @DurationStatistics
    public RouterInterfaceResponse removeInterfaceToNeutronRouter(@PathVariable String projectid, @PathVariable String routerid, @RequestBody RouterInterfaceRequest resource) throws Exception {

        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerid);
        RestPreconditionsUtil.verifyResourceFound(projectid);

        String portId = resource.getPortId();
        String subnetId = resource.getSubnetId();

        RouterInterfaceResponse routerInterfaceResponse = this.neutronRouterService.removeAnInterfaceToNeutronRouter(projectid, portId, subnetId, routerid);

        if (subnetId == null) {
            subnetId = routerInterfaceResponse.getSubnetId();
        }

        // TODO: return all connected subnet-ids to Port Manager. The algorithm as follow:
        //1. get ports array from the router.
        Router router = this.routerDatabaseService.getByRouterId(routerid);
        List<String> gatewayPorts = router.getGatewayPorts();
        //2. call Port Manager's /project/{project_id}/update-l3-neighbors/{new_subnet_id} with BODY {operation_type, vpcid, [old_subnet_ids]}.
        //Need to check if there is only one gateway port exists in the current router, we don't need to request PM for update-l3-neighbors. This operation only happen when there are more than 2 ports exist in the router.
        if (gatewayPorts != null && gatewayPorts.size() > 0) {
            // TODO: waiting for PM new API
            // construct InternalRouterInfo
            RouteTable neutronRouteTable = router.getNeutronRouteTable();
            List<RouteEntry> routeEntities = neutronRouteTable.getRouteEntities();
            List<NewRoutesRequest> routes = new ArrayList<>();
            for (RouteEntry routeEntry : routeEntities) {
                NewRoutesRequest newRoutesRequest = new NewRoutesRequest(routeEntry.getDestination(), routeEntry.getNexthop());
                routes.add(newRoutesRequest);
            }

            NewRoutesWebRequest routerRoutingRules = new NewRoutesWebRequest(routes);

            UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetId, routerRoutingRules, true, false);
            InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();

            List<InternalSubnetRoutingTable> internalSubnetRoutingTableList = this.neutronRouterService.constructInternalSubnetRoutingTables(router);
            internalSubnetRoutingTableList.add(internalSubnetRoutingTable);
            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(routerid, internalSubnetRoutingTableList);
            this.routerToPMService.updateL3Neighbors(projectid, router.getOwner(), subnetId, "delete", gatewayPorts, internalRouterInfo);
        }

        return routerInterfaceResponse;

    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/routers/{routerid}/add_extra_routes"})
    @DurationStatistics
    public RoutesToNeutronWebResponse addRoutesToNeutronRouter(@PathVariable String projectid, @PathVariable String routerid, @RequestBody RoutesToNeutronWebRequest resource) throws Exception {

        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerid);
        RestPreconditionsUtil.verifyResourceFound(projectid);

        NewRoutesWebRequest newRoutes = resource.getNewRoutesWebRequest();
        if (newRoutes == null) {
            return new RoutesToNeutronWebResponse();
        }
        // List<String> ports -> port entity -> subnet id
        Router router = this.routerDatabaseService.getByRouterId(routerid);
        RoutesToNeutronWebResponse routesToNeutronWebResponse = this.neutronRouterService.addRoutesToNeutronRouter(routerid, newRoutes);

        List<String> gatewayPorts = router.getGatewayPorts();
        List<String> subnetIds = this.routerToPMService.getSubnetIdsFromPM(projectid, gatewayPorts);
        if (subnetIds != null) {
            // sub-level routing rule update
            List<InternalSubnetRoutingTable> internalSubnetRoutingTableList = new ArrayList<>();
            for (String subnetId : subnetIds) {
                UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetId, newRoutes, true, true);
                InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();
                internalSubnetRoutingTableList.add(internalSubnetRoutingTable);
            }

            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(routerid, internalSubnetRoutingTableList);
            internalRouterInfo.setOperationType(OperationType.UPDATE);

            // send InternalRouterInfo contract to DPM
            this.routerToDPMService.sendInternalRouterInfoToDPM(internalRouterInfo);
        }

        return routesToNeutronWebResponse;

    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/routers/{routerid}/remove_extra_routes"})
    @DurationStatistics
    public RoutesToNeutronWebResponse removeRoutesFromNeutronRouter(@PathVariable String projectid, @PathVariable String routerid, @RequestBody RoutesToNeutronWebRequest resource) throws Exception {

        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerid);
        RestPreconditionsUtil.verifyResourceFound(projectid);

        NewRoutesWebRequest newRoutes = resource.getNewRoutesWebRequest();
        if (newRoutes == null) {
            return new RoutesToNeutronWebResponse();
        }
        // List<String> ports -> port entity -> subnet id
        Router router = this.routerDatabaseService.getByRouterId(routerid);

        RoutesToNeutronWebResponse routesToNeutronWebResponse = this.neutronRouterService.removeRoutesFromNeutronRouter(routerid, newRoutes);

        List<String> gatewayPorts = router.getGatewayPorts();
        List<String> subnetIds = this.routerToPMService.getSubnetIdsFromPM(projectid, gatewayPorts);
        if (subnetIds != null) {
            // sub-level routing rule update
            List<InternalSubnetRoutingTable> internalSubnetRoutingTableList = new ArrayList<>();
            for (String subnetId : subnetIds) {
                UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetId, newRoutes, true, false);
                InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();
                internalSubnetRoutingTableList.add(internalSubnetRoutingTable);
            }

            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(routerid, internalSubnetRoutingTableList);

            internalRouterInfo.setOperationType(OperationType.DELETE);
            // send InternalRouterInfo contract to DPM
            this.routerToDPMService.sendInternalRouterInfoToDPM(internalRouterInfo);
        }

        return routesToNeutronWebResponse;

    }

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}/connected-subnets"})
    @DurationStatistics
    public ConnectedSubnetsWebResponse getConnectedSubnets(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {

        ConnectedSubnetsWebResponse connectedSubnetsWebResponse = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            connectedSubnetsWebResponse = this.neutronRouterService.getConnectedSubnets(projectid, vpcid, subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        }

        return connectedSubnetsWebResponse;

    }

}
