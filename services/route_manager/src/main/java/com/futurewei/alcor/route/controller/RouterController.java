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
package com.futurewei.alcor.route.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNotValidException;
import com.futurewei.alcor.common.logging.*;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.route.exception.CanNotFindVpc;
import com.futurewei.alcor.route.exception.ExistMultipleSubnetRouteTable;
import com.futurewei.alcor.route.exception.VpcContainsSubnet;
import com.futurewei.alcor.route.service.RouteTableDatabaseService;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.service.RouterService;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.route.utils.RouteManagerUtil;
import com.futurewei.alcor.web.entity.route.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
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

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/router"})
    @DurationStatistics
    public RouterWebJson getOrCreateVpcRouter(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        Router router = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            router = this.routerService.getOrCreateVpcRouter(projectid, vpcid);

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
        } catch (VpcContainsSubnet e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        }

        return new ResponseId(routerId);
    }

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/vpcroutetable"})
    @DurationStatistics
    public RouteTableWebJson getOrCreateVpcRouteTable(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        RouteTable routetable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routetable = this.routerService.getOrCreateVpcRouteTable(projectid, vpcid);

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

        } catch (Exception e) {
            throw e;
        }

        return new RouteTablesWebJson(routetables);
    }

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

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/subnets/{subnetid}/routetable"})
    @DurationStatistics
    public RouteTableWebJson getOrCreateSubnetRouteTable(@PathVariable String projectid, @PathVariable String subnetid) throws Exception {

        RouteTable routeTable = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routeTable = this.routerService.getSubnetRouteTable(projectid, subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (ExistMultipleSubnetRouteTable e) {
            logger.log(Level.WARNING, e.getMessage() + " , subnetId: " + subnetid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouteTableWebJson(routeTable);
    }

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

            routetable = this.routerService.updateSubnetRouteTable(projectid, subnetid, resource);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (ExistMultipleSubnetRouteTable e) {
            logger.log(Level.WARNING, e.getMessage() + " , subnetId: " + subnetid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouteTableWebJson(routetable);
    }

    @RequestMapping(
            method = DELETE,
            value = {"//project/{projectid}/subnets/{subnetid}/routetable"})
    @DurationStatistics
    public ResponseId deleteSubnetRouteTable(@PathVariable String projectid, @PathVariable String subnetid) throws Exception {

        String routerId = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routerId = this.routerService.deleteSubnetRouteTable(projectid, subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        }

        return new ResponseId(routerId);
    }

}
