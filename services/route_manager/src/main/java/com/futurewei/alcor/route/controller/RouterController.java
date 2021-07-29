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
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNotValidException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.route.service.*;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.route.utils.RouteManagerUtil;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.subnet.HostRoute;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class RouterController {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private RouterService routerService;

    @Autowired
    private RouteTableDatabaseService routeTableDatabaseService;

    @Autowired
    private NeutronRouterService neutronRouterService;

    @Autowired
    private VpcRouterToSubnetService vpcRouterToSubnetService;

    @Autowired
    private RouterToDPMService routerToDPMService;

    /**
     * Get VPC router
     * @param projectid
     * @param vpcid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/router"})
    @DurationStatistics
    public RouterWebJson getVpcRouter(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        Router router = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            router = this.routerService.getVpcRouter(projectid, vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        if (router == null)
        {
            throw new CanNotFindRouter();
        }

        return new RouterWebJson(router);
    }

    /**
     * Create VPC router
     * @param projectid
     * @param vpcid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/vpcs/{vpcid}/router"})
    @DurationStatistics
    public RouterWebJson createVpcRouter(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        Router router = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            router = this.routerService.createVpcRouter(projectid, vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouterWebJson(router);
    }

    /**
     * Delete VPC router
     * @param projectid
     * @param vpcid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpcs/{vpcid}/router"})
    @DurationStatistics
    public ResponseId deleteVpcRouter(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        String routerId = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routerId = this.routerService.deleteVpcRouter(projectid, vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (VpcNonEmptyException e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        }

        return new ResponseId(routerId);
    }

    /**
     * Get VPC default route table
     * @param projectid
     * @param vpcid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/vpcroutetable"})
    @DurationStatistics
    public RouteTableWebJson getVpcRouteTable(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        RouteTable routetable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routetable = this.routerService.getVpcRouteTable(projectid, vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouteTableWebJson(routetable);
    }

    /**
     * Create VPC default route table
     * @param projectid
     * @param vpcid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/vpcs/{vpcid}/vpcroutetable"})
    @DurationStatistics
    public RouteTableWebJson createVpcRouteTable(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        RouteTable routetable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routetable = this.routerService.createVpcRouteTable(projectid, vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouteTableWebJson(routetable);
    }

    /**
     * Update VPC route table
     * @param projectid
     * @param vpcid
     * @param resource
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}/vpcroutetable"})
    @DurationStatistics
    public RouteTableWebJson updateVpcRouteTable(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody RouteTableWebJson resource) throws Exception {

        RouteTable routetable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            // check resource
            if (!RouteManagerUtil.checkVpcDefaultRouteTableWebJsonResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            routetable = this.routerService.updateVpcRouteTable(projectid, vpcid, resource);

            RouteTable newRoutetable = resource.getRoutetable();
            List<RouteEntry> routeEntities = newRoutetable.getRouteEntities();
            if (routeEntities == null) {
                return new RouteTableWebJson(routetable);
            }
            NewRoutesWebRequest newRouteEntry = new NewRoutesWebRequest();
            List<NewRoutesRequest> routes = new ArrayList<>();
            for (RouteEntry routeEntry : routeEntities) {
                NewRoutesRequest newRoutesRequest = new NewRoutesRequest();
                newRoutesRequest.setDestination(routeEntry.getDestination());
                // TODO: need to use target to get real IP address from GM
                newRoutesRequest.setNexthop(routeEntry.getTarget());
                routes.add(newRoutesRequest);
            }
            newRouteEntry.setRoutes(routes);

            // find subnets related to this vpc (getVpcRouteTables)
            Router router = this.routerService.getVpcRouter(projectid, vpcid);
            if (router == null)
            {
                throw new CanNotFindRouter();
            }
            List<RouteTable> vpcRouteTables = router.getVpcRouteTables();

            // sub-level routing rule update
            List<InternalSubnetRoutingTable> internalSubnetRoutingTableList = new ArrayList<>();
            for (RouteTable routeTable : vpcRouteTables) {
                String subnetId = routeTable.getOwner();
                UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetId, newRouteEntry, true, true);
                InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();
                internalSubnetRoutingTableList.add(internalSubnetRoutingTable);
            }

            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(router.getId(), internalSubnetRoutingTableList);

            // send InternalRouterInfo contract to DPM
            this.routerToDPMService.sendInternalRouterInfoToDPM(internalRouterInfo);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouteTableWebJson(routetable);
    }

    /**
     * List all routing tables in VPC
     * @param projectid
     * @param vpcid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/routetables"})
    @DurationStatistics
    public RouteTablesWebJson getVpcRouteTables(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        List<RouteTable> routetables = new ArrayList<>();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routetables = this.routerService.getVpcRouteTables(projectid, vpcid);

            if (routetables == null)
            {
                throw new RouterTableNotExist();
            }

        } catch (Exception e) {
            throw e;
        }

        return new RouteTablesWebJson(routetables);
    }

    /**
     * Show a routing table
     * @param projectid
     * @param routetableid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/routetables/{routetableid}"})
    @DurationStatistics
    public RouteTableWebJson getVpcRouteTableById(@PathVariable String projectid, @PathVariable String routetableid) throws Exception {

        RouteTable routetable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routetableid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routetable = this.routeTableDatabaseService.getByRouteTableId(routetableid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        }

        return new RouteTableWebJson(routetable);
    }

    /**
     * Show Subnet route table
     * @param projectid
     * @param subnetid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/subnets/{subnetid}/routetable"})
    @DurationStatistics
    public RouteTableWebJson getSubnetRouteTable(@PathVariable String projectid, @PathVariable String subnetid) throws Exception {

        RouteTable routeTable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routeTable = this.routerService.getSubnetRouteTable(projectid, subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (OwnMultipleSubnetRouteTablesException e) {
            logger.log(Level.WARNING, e.getMessage() + " , subnetId: " + subnetid);
            throw e;
        }

        return new RouteTableWebJson(routeTable);
    }

    /**
     * Create neutron subnet routeTable
     * @param projectid
     * @param subnetid
     * @param resource
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/subnets/{subnetid}/routetable"})
    @DurationStatistics
    public RouteTableWebJson createSubnetRouteTable(@PathVariable String projectid, @PathVariable String subnetid, @RequestBody RouteTableWebJson resource) throws Exception {

        RouteTable routeTable = resource.getRoutetable();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            // check resource
            if (!RouteManagerUtil.checkCreateSubnetRouteTableWebJsonResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            // get Subnet
            SubnetWebJson subnetWebJson= this.vpcRouterToSubnetService.getSubnet(projectid, subnetid);
            String routerId = subnetWebJson.getSubnet().getAttachedRouterId();

            // add host route
            List<RouteEntry> routeEntities = routeTable.getRouteEntities();
            List<RouteEntry> routes = new ArrayList<>();
            for (RouteEntry routeEntry : routeEntities) {
                String uuid = UUID.randomUUID().toString();
                RouteEntry newRoute = new RouteEntry(projectid, uuid, "route-" + uuid, routeEntry.getDescription(),
                        routeEntry.getDestination(), null, 100, null, routeEntry.getNexthop());
                routes.add(newRoute);
            }

            routeTable = this.routerService.createSubnetRouteTable(projectid, subnetid, resource, routes);

            // sub-level routing rule update
            List<RouteEntry> inRouteEntities = routeTable.getRouteEntities();
            NewRoutesWebRequest newRoutes = new NewRoutesWebRequest();
            List<NewRoutesRequest> inRoutes = new ArrayList<>();
            if (inRouteEntities != null) {
                for (RouteEntry routeEntry : inRouteEntities) {
                    String destination = routeEntry.getDestination();
                    String nexthop = routeEntry.getNexthop();
                    NewRoutesRequest newRoutesRequest = new NewRoutesRequest(destination, nexthop);
                    inRoutes.add(newRoutesRequest);
                }
            }
            newRoutes.setRoutes(inRoutes);

            UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetid, newRoutes, false, true);
            InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();
            List<InternalSubnetRoutingTable> internalSubnetRoutingTables = new ArrayList<>();
            internalSubnetRoutingTables.add(internalSubnetRoutingTable);
            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(routerId, internalSubnetRoutingTables);
            List<HostRoute> hostRouteToSubnet = updateRoutingRuleResponse.getHostRouteToSubnet();

            // send InternalRouterInfo contract to DPM
            this.routerToDPMService.sendInternalRouterInfoToDPM(internalRouterInfo);

            // update routes in subnet manager
            if (hostRouteToSubnet == null) {
                throw new HostRoutesToSubnetIsNull();
            }
            this.vpcRouterToSubnetService.updateRoutingRuleInSubnetManager(projectid, subnetid, hostRouteToSubnet);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouteTableWebJson(routeTable);
    }

    /**
     * Update Subnet route table
     * @param projectid
     * @param subnetid
     * @param resource
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/subnets/{subnetid}/routetable"})
    @DurationStatistics
    public RouteTableWebJson updateSubnetRouteTable(@PathVariable String projectid, @PathVariable String subnetid, @RequestBody RouteTableWebJson resource) throws Exception {

        RouteTable routetable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            // check resource
            if (!RouteManagerUtil.checkSubnetRouteTableWebJsonResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            // get Subnet
            SubnetWebJson subnetWebJson= this.vpcRouterToSubnetService.getSubnet(projectid, subnetid);
            String routerId = subnetWebJson.getSubnet().getAttachedRouterId();

            routetable = resource.getRoutetable();
            // sub-level routing rule update
            List<RouteEntry> routeEntities = routetable.getRouteEntities();
            NewRoutesWebRequest newRoutes = new NewRoutesWebRequest();
            List<NewRoutesRequest> routes = new ArrayList<>();
            if (routeEntities != null) {
                for (RouteEntry routeEntry : routeEntities) {
                    String destination = routeEntry.getDestination();
                    String nexthop = routeEntry.getNexthop();
                    NewRoutesRequest newRoutesRequest = new NewRoutesRequest(destination, nexthop);
                    routes.add(newRoutesRequest);
                }
            }
            newRoutes.setRoutes(routes);
            UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetid, newRoutes, false, true);
            InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();
            List<InternalSubnetRoutingTable> internalSubnetRoutingTables = new ArrayList<>();
            internalSubnetRoutingTables.add(internalSubnetRoutingTable);
            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(routerId, internalSubnetRoutingTables);
            List<HostRoute> hostRouteToSubnet = updateRoutingRuleResponse.getHostRouteToSubnet();

            this.routerService.updateSubnetRouteTable(projectid, subnetid, updateRoutingRuleResponse);

            // send InternalRouterInfo contract to DPM
            this.routerToDPMService.sendInternalRouterInfoToDPM(internalRouterInfo);

            // update routes in subnet manager
            if (hostRouteToSubnet == null) {
                throw new HostRoutesToSubnetIsNull();
            }
            this.vpcRouterToSubnetService.updateRoutingRuleInSubnetManager(projectid, subnetid, hostRouteToSubnet);
        } catch (ParameterNullOrEmptyException | HostRoutesToSubnetIsNull e) {
            throw e;
        } catch (OwnMultipleSubnetRouteTablesException e) {
            logger.log(Level.WARNING, e.getMessage() + " , subnetId: " + subnetid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouteTableWebJson(routetable);
    }

    /**
     * Delete Subnet route table
     * @param projectid
     * @param subnetid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/subnets/{subnetid}/routetable"})
    @DurationStatistics
    public ResponseId deleteSubnetRouteTable(@PathVariable String projectid, @PathVariable String subnetid) throws Exception {

        String routerTableId = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            // get Subnet
            SubnetWebJson subnetWebJson= this.vpcRouterToSubnetService.getSubnet(projectid, subnetid);
            String routerId = subnetWebJson.getSubnet().getAttachedRouterId();

            // sub-level routing rule update
            NewRoutesWebRequest newRoutes = new NewRoutesWebRequest();
            List<NewRoutesRequest> routes = new ArrayList<>();
            newRoutes.setRoutes(routes);
            UpdateRoutingRuleResponse updateRoutingRuleResponse = this.neutronRouterService.updateRoutingRule(subnetid, newRoutes, false, false);
            InternalSubnetRoutingTable internalSubnetRoutingTable = updateRoutingRuleResponse.getInternalSubnetRoutingTable();
            List<InternalSubnetRoutingTable> internalSubnetRoutingTables = new ArrayList<>();
            internalSubnetRoutingTables.add(internalSubnetRoutingTable);
            InternalRouterInfo internalRouterInfo = this.neutronRouterService.constructInternalRouterInfo(routerId, internalSubnetRoutingTables);
            List<HostRoute> hostRouteToSubnet = updateRoutingRuleResponse.getHostRouteToSubnet();

            // send InternalRouterInfo contract to DPM
            this.routerToDPMService.sendInternalRouterInfoToDPM(internalRouterInfo);

            // update routes in subnet manager
            if (hostRouteToSubnet == null) {
                throw new HostRoutesToSubnetIsNull();
            }
            this.vpcRouterToSubnetService.updateRoutingRuleInSubnetManager(projectid, subnetid, hostRouteToSubnet);

            routerTableId = this.routerService.deleteSubnetRouteTable(projectid, subnetid);

        } catch (ParameterNullOrEmptyException | HostRoutesToSubnetIsNull e) {
            throw e;
        }

        return new ResponseId(routerTableId);
    }

}
