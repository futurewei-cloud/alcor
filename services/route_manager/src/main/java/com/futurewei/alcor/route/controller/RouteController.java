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
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.route.entity.*;
import com.futurewei.alcor.route.service.RouteDatabaseService;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class RouteController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RouteDatabaseService routeDatabaseService;

    @RequestMapping(
            method = GET,
            value = {"/vpcs/{vpcId}/routes/{routeId}"})
    public RouteWebJson getRuleByVpcId(@PathVariable String vpcId, @PathVariable String routeId) throws Exception {

        RouteEntity routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeState = this.routeDatabaseService.getByRouteId(routeId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (routeState == null) {
            //TODO: REST error code
            return new RouteWebJson();
        }

        return new RouteWebJson(routeState);
    }

    @RequestMapping(
            method = GET,
            value = {"/subnets/{subnetId}/routes/{routeId}"})
    public RouteWebJson getRuleBySubnetId(@PathVariable String subnetId, @PathVariable String routeId) throws Exception {

        RouteEntity routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeState = this.routeDatabaseService.getByRouteId(routeId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (routeState == null) {
            //TODO: REST error code
            return new RouteWebJson();
        }

        return new RouteWebJson(routeState);

    }

    @RequestMapping(
            method = POST,
            value = {"/vpcs/{vpcId}/routes"})
    @ResponseStatus(HttpStatus.CREATED)
    public RouteWebJson createVpcDefaultRoute(@PathVariable String vpcId, @RequestBody VpcWebJson resource) throws Exception {
        RouteEntity routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            VpcEntity vpcEntity = resource.getNetwork();
            RestPreconditionsUtil.verifyResourceNotNull(vpcEntity);

            String id = UUID.randomUUID().toString();
            String projectId = vpcEntity.getProjectId();
            String destination = vpcEntity.getCidr();
            String routeTableId = UUID.randomUUID().toString();

            routeState = new RouteEntity(projectId, id, "default_route_rule", "",
                    destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, RouteConstant.DEFAULT_ROUTE_TABLE_TYPE, routeTableId);

            this.routeDatabaseService.addRoute(routeState);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteWebJson(routeState);
    }

    @RequestMapping(
            method = POST,
            value = {"/subnets/{subnetId}/routes"})
    @ResponseStatus(HttpStatus.CREATED)
    public RouteWebJson createSubnetRoute(@PathVariable String subnetId, @RequestBody SubnetWebJson resource) throws Exception {
        RouteEntity routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);

            SubnetEntity inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceNotNull(inSubnetState);

            String id = UUID.randomUUID().toString();
            String projectId = inSubnetState.getProjectId();
            String destination = inSubnetState.getCidr();
            String routeTableId = UUID.randomUUID().toString();

            routeState = new RouteEntity(projectId, id, "default_route_rule", "",
                    destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, RouteConstant.DEFAULT_ROUTE_TABLE_TYPE, routeTableId);

            this.routeDatabaseService.addRoute(routeState);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteWebJson(routeState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/vpcs/{vpcId}/routes/{routeId}"})
    public ResponseId deleteRule(@PathVariable String vpcId, @PathVariable String routeId) throws Exception {
        RouteEntity routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeState = this.routeDatabaseService.getByRouteId(routeId);
            if (routeState == null) {
                return new ResponseId();
            }

            this.routeDatabaseService.deleteRoute(routeId);
        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
        logger.info("delete successfully —— id: " + routeId);
        return new ResponseId(routeId);
    }
}
