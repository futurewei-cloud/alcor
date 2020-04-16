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

package com.futurewei.alcor.subnet.controller;

import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.entity.ResponseId;

import com.futurewei.alcor.subnet.config.UnitTestConfig;
import com.futurewei.alcor.subnet.entity.*;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
import com.futurewei.alcor.subnet.utils.RestPreconditionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class SubnetController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubnetDatabaseService subnetDatabaseService;

    @Autowired
    private SubnetService subnetService;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/subnets/{subnetId}", "v4/{projectid}/subnets/{subnetId}"})
    public SubnetStateJson getSubnetStateById(@PathVariable String projectid, @PathVariable String subnetId) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            subnetState = this.subnetDatabaseService.getBySubnetId(subnetId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        if (subnetState == null) {
            //TODO: REST error code
            return new SubnetStateJson();
        }

        return new SubnetStateJson(subnetState);
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/subnets", "v4/{projectid}/subnets"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetStateJson createSubnetState(@PathVariable String projectid, @RequestBody SubnetStateJson resource) throws Exception {
        SubnetState subnetState = null;
        RouteWebJson routeResponse = null;
        MacStateJson macResponse = null;
        String portId = UUID.randomUUID().toString();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceNotNull(resource.getSubnet());

            // TODO: Create a verification framework for all resources
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceFound(inSubnetState.getVpcId());
            RestPreconditionsUtil.populateResourceProjectId(inSubnetState, projectid);

            //Allocate Gateway Mac
            macResponse = this.subnetService.allocateMacGateway(projectid, inSubnetState.getVpcId(), portId);
            logger.info("macResponse: " + macResponse.getMacState().getMac());

            // Verify VPC ID
            VpcStateJson vpcResponse = this.subnetService.verifyVpcId(projectid, inSubnetState.getVpcId());

            //Prepare Route Rule(IPv4/6) for Subnet
            routeResponse = this.subnetService.createRouteRules(inSubnetState.getId(), inSubnetState);




            // Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique
//            IPStateJson ipResponse = this.subnetService.allocateIPGateway(inSubnetState.getId(), inSubnetState.getCidr(), portId);
//            if (ipResponse == null) {
//                throw new ResourcePersistenceException();
//            }

            // set up value of properties for subnetState
            List<RouteWebObject> routes = inSubnetState.getRoutes();
            if (routes == null) {
                routes = new ArrayList<>();
            }
            routes.add(routeResponse.getRoute());
            inSubnetState.setRoutes(routes);
            //subnetState.setGatewayIp(ipResponse.getIpState().getIp());

            this.subnetDatabaseService.addSubnet(inSubnetState);

            subnetState = this.subnetDatabaseService.getBySubnetId(inSubnetState.getId());
            if (subnetState == null) {
                throw new ResourcePersistenceException();
            }

            return new SubnetStateJson(subnetState);

        } catch (ResourcePersistenceException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        } catch (FallbackException e) {
            logger.error(e.getMessage());

            // Subnet fallback
            logger.info("subnet fallback start");
            this.subnetDatabaseService.deleteSubnet(resource.getSubnet().getId());
            logger.info("subnet fallback end");

            // Route fallback
            logger.info("Route fallback start");
            if (routeResponse != null) {
                RouteWebObject route = routeResponse.getRoute();
                this.subnetService.routeFallback(route.getId(), resource.getSubnet().getVpcId());
            }
            logger.info("Route fallback end");

            // Mac fallback
            logger.info("Mac fallback start");
            if (macResponse != null) {
                this.subnetService.macFallback(macResponse.getMacState().getMac());
                //this.subnetService.macFallback(UnitTestConfig.macAddress);
            }
            logger.info("Mac fallback end");
            throw new Exception(e);
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}", "v4/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public SubnetStateJson updateSubnetState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid, @RequestBody SubnetStateJson resource) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceNotNull(inSubnetState);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetState, projectid);
            RestPreconditionsUtil.populateResourceVpcId(inSubnetState, vpcid);

            subnetState = this.subnetDatabaseService.getBySubnetId(subnetid);
            if (subnetState == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetid);
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditionsUtil.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            this.subnetDatabaseService.addSubnet(inSubnetState);
            subnetState = this.subnetDatabaseService.getBySubnetId(subnetid);

        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        return new SubnetStateJson(subnetState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}", "v4/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public ResponseId deleteSubnetState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);

            subnetState = this.subnetDatabaseService.getBySubnetId(subnetid);
            if (subnetState == null) {
                return new ResponseId();
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditionsUtil.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            this.subnetDatabaseService.deleteSubnet(subnetid);

        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        return new ResponseId(subnetid);
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs/{vpcid}/subnets")
    public Map getSubnetStatesByProjectIdAndVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        Map<String, SubnetState> subnetStates = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);
            RestPreconditionsUtil.verifyResourceFound(vpcid);

            subnetStates = this.subnetDatabaseService.getAllSubnets();
            subnetStates = subnetStates.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId())
                            && vpcid.equalsIgnoreCase(state.getValue().getVpcId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        return subnetStates;
    }


}
