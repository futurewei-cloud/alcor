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
import com.futurewei.alcor.route.dao.RouteRedisRepository;
import com.futurewei.alcor.route.entity.RouteState;
import com.futurewei.alcor.route.entity.RouteStateJson;
import com.futurewei.alcor.route.entity.*;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
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
    private RouteRedisRepository routeRedisRepository;

    @RequestMapping(
            method = GET,
            value = {"/vpcs/{vpcId}/routes/{routeId}"})
    public RouteStateJson getRule(@PathVariable String vpcId, @PathVariable String routeId) throws Exception {

        RouteState routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeState = this.routeRedisRepository.findItem(routeId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (routeState == null) {
            //TODO: REST error code
            return new RouteStateJson();
        }

        return new RouteStateJson(routeState);
    }

    @RequestMapping(
            method = POST,
            value = {"/vpcs/{vpcId}/routes"})
    @ResponseStatus(HttpStatus.CREATED)
    public RouteStateJson createVpcDefaultRoute(@PathVariable String vpcId, @RequestBody VpcStateJson resource) throws Exception {
        RouteState routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);

            VpcState inVpcState = resource.getVpc();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);

            String id = UUID.randomUUID().toString();
            String projectId = inVpcState.getProjectId();
            String destination = inVpcState.getCidr();
            String routeTableId = UUID.randomUUID().toString();

            routeState = new RouteState(projectId, id, "default_route_rule", "",
                    destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, RouteConstant.DEFAULT_ROUTE_TABLE_TYPE, routeTableId);

            this.routeRedisRepository.addItem(routeState);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteStateJson(routeState);
    }

    @RequestMapping(
            method = POST,
            value = {"/subnets/{subnetId}/routes"})
    @ResponseStatus(HttpStatus.CREATED)
    public RouteStateJson createSubnetRoute(@PathVariable String subnetId, @RequestBody SubnetStateJson resource) throws Exception {
        RouteState routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);

            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceNotNull(inSubnetState);

            String id = UUID.randomUUID().toString();
            String projectId = inSubnetState.getProjectId();
            String destination = inSubnetState.getCidr();
            String routeTableId = UUID.randomUUID().toString();

            routeState = new RouteState(projectId, id, "default_route_rule", "",
                    destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, RouteConstant.DEFAULT_ROUTE_TABLE_TYPE, routeTableId);

            this.routeRedisRepository.addItem(routeState);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteStateJson(routeState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/vpcs/{vpcId}/routes/{routeId}"})
    public ResponseId deleteRule(@PathVariable String vpcId, @PathVariable String routeId) throws Exception {
        RouteState routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeState = this.routeRedisRepository.findItem(routeId);
            if (routeState == null) {
                return new ResponseId();
            }

            this.routeRedisRepository.deleteItem(routeId);
        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
        logger.info("delete successfully —— id: " + routeId);
        return new ResponseId(routeId);
    }
}
