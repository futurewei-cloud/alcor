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
import com.futurewei.alcor.subnet.entity.*;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
import com.futurewei.alcor.subnet.utils.RestPreconditionsUtil;
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
        String portId = UUID.randomUUID().toString();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceNotNull(resource.getSubnet());

            // TODO: Create a verification framework for all resources
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceFound(inSubnetState.getVpcId());
            RestPreconditionsUtil.populateResourceProjectId(inSubnetState, projectid);

            this.subnetDatabaseService.addSubnet(inSubnetState);

            subnetState = this.subnetDatabaseService.getBySubnetId(inSubnetState.getId());
            if (subnetState == null) {
                throw new ResourcePersistenceException();
            }

            // Verify VPC ID
            VpcStateJson vpcResponse = this.subnetService.verifyVpcId(projectid, inSubnetState.getVpcId());
            if (vpcResponse == null) {
                throw new FallbackException("fallback request");
            }

            //Prepare Route Rule(IPv4/6) for Subnet
            RouteWebJson routeResponse = this.subnetService.createRouteRules(inSubnetState.getId(), inSubnetState);
            if (routeResponse == null) {
                throw new FallbackException("fallback request");
            }

            //Allocate Gateway Mac
//            MacStateJson macResponse = this.subnetService.allocateMacGateway(projectid, inSubnetState.getVpcId(), portId);
//            if (macResponse == null) {
//                throw new ResourcePersistenceException();
//            }

            // Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique
//            IPStateJson ipResponse = this.subnetService.allocateIPGateway(inSubnetState.getId(), inSubnetState.getCidr(), portId);
//            if (ipResponse == null) {
//                throw new ResourcePersistenceException();
//            }

            // set up value of properties for subnetState
            List<RouteWebObject> routes = subnetState.getRoutes();
            if (routes == null) {
                routes = new ArrayList<>();
            }
            routes.add(routeResponse.getRoute());
            subnetState.setRoutes(routes);
            //subnetState.setGatewayIp(ipResponse.getIpState().getIp());

        } catch (ResourcePersistenceException e) {
            throw new Exception(e);
        } catch (FallbackException e) {
            // Route info of subnet rollback
            List<RouteWebObject> routes = resource.getSubnet().getRoutes();
            if (routes != null) {
                RouteWebObject route = routes.get(routes.size() - 1);
                this.subnetService.routeRollback(route.getId(), resource.getSubnet().getVpcId());
            }
            throw new Exception(e);
        }

        return new SubnetStateJson(subnetState);
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
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
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
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
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
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return subnetStates;
    }


}
