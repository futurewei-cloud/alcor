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
import com.futurewei.alcor.web.entity.*;
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
    public SubnetWebJson getSubnetStateById(@PathVariable String projectId, @PathVariable String subnetId) throws Exception {

        SubnetWebObject subnetWebObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditionsUtil.verifyResourceFound(projectId);

            subnetWebObject = this.subnetDatabaseService.getBySubnetId(subnetId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        if (subnetWebObject == null) {
            //TODO: REST error code
            return new SubnetWebJson();
        }

        return new SubnetWebJson(subnetWebObject);
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectId}/subnets/bulk", "v4/{projectId}/subnets/bulk"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetsWebJson createSubnetStateBulk(@PathVariable String projectId, @RequestBody SubnetWebJson resource) throws Exception {
        return new SubnetsWebJson();
    }

        @RequestMapping(
            method = POST,
            value = {"/project/{projectId}/subnets", "v4/{projectId}/subnets"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetWebJson createSubnetState(@PathVariable String projectId, @RequestBody SubnetWebJson resource) throws Exception {
        long start = System.currentTimeMillis();
        SubnetWebObject subnetWebObject = null;
        RouteWebJson routeResponse = null;
        MacStateJson macResponse = null;
        IpAddrRequest ipResponse = null;
        AtomicReference<RouteWebJson> routeResponseAtomic = new AtomicReference<>();
        AtomicReference<MacStateJson> macResponseAtomic = new AtomicReference<>();
        AtomicReference<IpAddrRequest> ipResponseAtomic = new AtomicReference<>();
        String portId = UUID.randomUUID().toString();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyResourceNotNull(resource.getSubnet());

            // TODO: Create a verification framework for all resources
            SubnetWebObject inSubnetWebObject = resource.getSubnet();
            String subnetId = inSubnetWebObject.getId();
            String vpcId = inSubnetWebObject.getVpcId();
            String cidr = inSubnetWebObject.getCidr();
            RestPreconditionsUtil.verifyResourceFound(vpcId);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetWebObject, projectId);

            //Allocate Gateway Mac
            CompletableFuture<MacStateJson> macFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return this.subnetService.allocateMacAddressForGatewayPort(projectId, vpcId, portId);
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
                    return this.subnetService.createRouteRules(subnetId, inSubnetWebObject);
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


            // Verify/Allocate Gateway IP
            CompletableFuture<IpAddrRequest> ipFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return this.subnetService.allocateIpAddressForGatewayPort(subnetId, cidr);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, ThreadPoolExecutorUtils.SELECT_POOL_EXECUTOR).handle((s, e) -> {
                ipResponseAtomic.set(s);
                if (e != null) {
                    throw new CompletionException(e);
                }
                return s;
            });

            // Synchronous blocking
            CompletableFuture<Void> allFuture = CompletableFuture.allOf(vpcFuture, macFuture, routeFuture, ipFuture);
            allFuture.join();

            macResponse = macFuture.join();
            routeResponse = routeFuture.join();
            ipResponse = ipFuture.join();

            logger.info("Total processing time:" + (System.currentTimeMillis() - start) + "ms");

            // set up value of properties for subnetState
            List<RouteWebObject> routes = new ArrayList<>();
            routes.add(routeResponse.getRoute());
            inSubnetWebObject.setRoutes(routes);

            MacState macState = macResponse.getMacState();
            if (macState != null) {
                inSubnetWebObject.setGatewayMacAddress(macState.getMacAddress());
            }
            inSubnetWebObject.setGatewayIp(ipResponse.getIp());
            if (ipResponse.getIpVersion() == 4) {
                inSubnetWebObject.setIpV4RangeId(ipResponse.getRangeId());
            }else if (ipResponse.getIpVersion() == 6) {
                inSubnetWebObject.setIpV6RangeId(ipResponse.getRangeId());
            }

            this.subnetDatabaseService.addSubnet(inSubnetWebObject);

//            subnetState = this.subnetDatabaseService.getBySubnetId(subnetId);
//            if (SubnetState == null) {
//                throw new ResourcePersistenceException();
//            }

            return new SubnetWebJson(inSubnetWebObject);

        } catch (CompletionException e) {
            this.subnetService.fallbackOperation(routeResponseAtomic, macResponseAtomic, ipResponseAtomic, resource, e.getMessage());
            throw new Exception(e);
        } catch (DatabasePersistenceException e) {
            this.subnetService.fallbackOperation(routeResponseAtomic, macResponseAtomic, ipResponseAtomic, resource, e.getMessage());
            throw new Exception(e);
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectId}/subnets/{subnetId}", "v4/{projectId}/subnets/{subnetId}"})
    public SubnetWebJson updateSubnetState(@PathVariable String projectId, @PathVariable String subnetId, @RequestBody SubnetWebJson resource) throws Exception {

        SubnetWebObject subnetWebObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            SubnetWebObject inSubnetWebObject = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceNotNull(inSubnetWebObject);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetWebObject, projectId);

            subnetWebObject = this.subnetDatabaseService.getBySubnetId(subnetId);
            if (subnetWebObject == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetId);
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetWebObject.getProjectId(), projectId);

            this.subnetDatabaseService.addSubnet(inSubnetWebObject);
            subnetWebObject = this.subnetDatabaseService.getBySubnetId(subnetId);

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

        return new SubnetWebJson(subnetWebObject);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectId}/subnets/{subnetId}", "v4/{projectId}/subnets/{subnetId}"})
    public ResponseId deleteSubnetState(@PathVariable String projectId, @PathVariable String subnetId) throws Exception {

        SubnetWebObject subnetWebObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);

            subnetWebObject = this.subnetDatabaseService.getBySubnetId(subnetId);
            if (subnetWebObject == null) {
                return new ResponseId();
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetWebObject.getProjectId(), projectId);

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
            value = "/project/{projectId}/subnets")
    public SubnetsWebJson getSubnetStatesByProjectIdAndVpcId(@PathVariable String projectId) throws Exception {
        Map<String, SubnetWebObject> subnetStates = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyResourceFound(projectId);

            subnetStates = this.subnetDatabaseService.getAllSubnets();
            subnetStates = subnetStates.entrySet().stream()
                    .filter(state -> projectId.equalsIgnoreCase(state.getValue().getProjectId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
        List<SubnetWebObject> subnetWebObjectList = new ArrayList<>();
        for (Map.Entry<String, SubnetWebObject> entry : subnetStates.entrySet()) {
            SubnetWebObject tmp = (SubnetWebObject) entry.getValue();
            subnetWebObjectList.add(tmp);
        }
        return new SubnetsWebJson(subnetWebObjectList);
    }


}
