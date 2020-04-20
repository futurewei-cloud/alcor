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
import com.futurewei.alcor.subnet.utils.ThreadPoolExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
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
            value = {"/project/{projectId}/subnets/{subnetId}", "v4/{projectId}/subnets/{subnetId}"})
    public SubnetStateJson getSubnetStateById(@PathVariable String projectId, @PathVariable String subnetId) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditionsUtil.verifyResourceFound(projectId);

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
            value = {"/project/{projectId}/subnets", "v4/{projectId}/subnets"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetStateJson createSubnetState(@PathVariable String projectId, @RequestBody SubnetStateJson resource) throws Exception {
        long start = System.currentTimeMillis();
        SubnetState subnetState = null;
        RouteWebJson routeResponse = null;
        MacStateJson macResponse = null;
        AtomicReference<RouteWebJson> routeResponseAtomic = new AtomicReference<>();
        AtomicReference<MacStateJson> macResponseAtomic = new AtomicReference<>();
        String portId = UUID.randomUUID().toString();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyResourceNotNull(resource.getSubnet());

            // TODO: Create a verification framework for all resources
            SubnetState inSubnetState = resource.getSubnet();
            String subnetId = inSubnetState.getId();
            String vpcId = inSubnetState.getVpcId();
            RestPreconditionsUtil.verifyResourceFound(vpcId);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetState, projectId);

            //Allocate Gateway Mac
            CompletableFuture<MacStateJson> macFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return this.subnetService.allocateMacGateway(projectId, vpcId, portId);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, ThreadPoolExecutorUtils.SELECT_POOL_EXECUTOR).handle((s, e) -> {
                macResponseAtomic.set(s);
                if (e != null) {
                    throw new CompletionException(e);
                }
                return s;
            });

            // Verify VPC ID
            CompletableFuture<VpcStateJson> vpcFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return this.subnetService.verifyVpcId(projectId, vpcId);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, ThreadPoolExecutorUtils.SELECT_POOL_EXECUTOR);

            //Prepare Route Rule(IPv4/6) for Subnet
            CompletableFuture<RouteWebJson> routeFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return this.subnetService.createRouteRules(subnetId, inSubnetState);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, ThreadPoolExecutorUtils.SELECT_POOL_EXECUTOR).handle((s, e) -> {
                routeResponseAtomic.set(s);
                if (e != null) {
                    throw new CompletionException(e);
                }
                return s;
            });


            // Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique
//            IPStateJson ipResponse = this.subnetService.allocateIPGateway(inSubnetState.getId(), inSubnetState.getCidr(), portId);
//            if (ipResponse == null) {
//                throw new ResourcePersistenceException();
//            }

            // Synchronous blocking
            CompletableFuture<Void> allFuture = CompletableFuture.allOf(vpcFuture, macFuture, routeFuture);
            allFuture.join();

            macResponse = macFuture.join();
            routeResponse = routeFuture.join();
            logger.info("Total processing time:" + (System.currentTimeMillis() - start) + "ms");

            // set up value of properties for subnetState
            List<RouteWebObject> routes = new ArrayList<>();
            routes.add(routeResponse.getRoute());
            inSubnetState.setRoutes(routes);
            //subnetState.setGatewayIp(ipResponse.getIpState().getIp());

            this.subnetDatabaseService.addSubnet(inSubnetState);

//            subnetState = this.subnetDatabaseService.getBySubnetId(subnetId);
//            if (SubnetState == null) {
//                throw new ResourcePersistenceException();
//            }

            return new SubnetStateJson(inSubnetState);

        } catch (CompletionException e) {
            this.subnetService.fallbackOperation(routeResponseAtomic, macResponseAtomic, resource, e.getMessage());
            throw new Exception(e);
        } catch (DatabaseAddException e) {
            this.subnetService.fallbackOperation(routeResponseAtomic, macResponseAtomic, resource, e.getMessage());
            throw new Exception(e);
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectId}/subnets/{subnetId}", "v4/{projectId}/subnets/{subnetId}"})
    public SubnetStateJson updateSubnetState(@PathVariable String projectId, @PathVariable String subnetId, @RequestBody SubnetStateJson resource) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceNotNull(inSubnetState);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetState, projectId);

            subnetState = this.subnetDatabaseService.getBySubnetId(subnetId);
            if (subnetState == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetId);
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetState.getProjectId(), projectId);

            this.subnetDatabaseService.addSubnet(inSubnetState);
            subnetState = this.subnetDatabaseService.getBySubnetId(subnetId);

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
            value = {"/project/{projectId}/subnets/{subnetId}", "v4/{projectId}/subnets/{subnetId}"})
    public ResponseId deleteSubnetState(@PathVariable String projectId, @PathVariable String subnetId) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);

            subnetState = this.subnetDatabaseService.getBySubnetId(subnetId);
            if (subnetState == null) {
                return new ResponseId();
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetState.getProjectId(), projectId);

            this.subnetDatabaseService.deleteSubnet(subnetId);

        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        return new ResponseId(subnetId);
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectId}/vpcs/{vpcId}/subnets")
    public Map getSubnetStatesByProjectIdAndVpcId(@PathVariable String projectId, @PathVariable String vpcId) throws Exception {
        Map<String, SubnetState> subnetStates = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            RestPreconditionsUtil.verifyResourceFound(projectId);
            RestPreconditionsUtil.verifyResourceFound(vpcId);

            subnetStates = this.subnetDatabaseService.getAllSubnets();
            subnetStates = subnetStates.entrySet().stream()
                    .filter(state -> projectId.equalsIgnoreCase(state.getValue().getProjectId())
                            && vpcId.equalsIgnoreCase(state.getValue().getVpcId()))
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
