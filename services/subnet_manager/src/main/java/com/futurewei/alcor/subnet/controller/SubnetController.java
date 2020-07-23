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

import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.common.utils.DateUtil;
import com.futurewei.alcor.subnet.exception.GatewayIpUnsupported;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
import com.futurewei.alcor.subnet.utils.RestPreconditionsUtil;
import com.futurewei.alcor.subnet.utils.SubnetManagementUtil;
import com.futurewei.alcor.subnet.utils.ThreadPoolExecutorUtils;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.subnet.*;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class SubnetController {

    @Autowired
    private HttpServletRequest request;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubnetDatabaseService subnetDatabaseService;

    @Autowired
    private SubnetService subnetService;

    @FieldFilter(type=SubnetEntity.class)
    @RequestMapping(
            method = GET,
            value = {"/project/{projectId}/subnets/{subnetId}"})
    public SubnetWebJson getSubnetStateById(@PathVariable String projectId, @PathVariable String subnetId) throws Exception {

        SubnetEntity subnetEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditionsUtil.verifyResourceFound(projectId);

            subnetEntity = this.subnetDatabaseService.getBySubnetId(subnetId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        if (subnetEntity == null) {
            //TODO: REST error code
            return new SubnetWebJson();
        }

        return new SubnetWebJson(subnetEntity);
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectId}/subnets/bulk"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetsWebJson createSubnetStateBulk(@PathVariable String projectId, @RequestBody SubnetWebJson resource) throws Exception {
        return new SubnetsWebJson();
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectId}/subnets"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetWebJson createSubnetState(@PathVariable String projectId, @RequestBody SubnetRequestWebJson resource) throws Exception {
        long start = System.currentTimeMillis();
        SubnetEntity inSubnetEntity = new SubnetEntity();
        RouteWebJson routeResponse = null;
        MacStateJson macResponse = null;
        IpAddrRequest ipResponse = null;
        AtomicReference<RouteWebJson> routeResponseAtomic = new AtomicReference<>();
        AtomicReference<MacStateJson> macResponseAtomic = new AtomicReference<>();
        AtomicReference<IpAddrRequest> ipResponseAtomic = new AtomicReference<>();
        String portId = UUID.randomUUID().toString();

        if(StringUtils.isEmpty(resource.getSubnet().getId())){
            String subnetId = UUID.randomUUID().toString();
            resource.getSubnet().setId(subnetId);
        }

        try {
            if (!SubnetManagementUtil.checkSubnetRequestResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyResourceNotNull(resource.getSubnet());

            // TODO: Create a verification framework for all resources
            SubnetWebRequestObject subnetWebRequestObject = resource.getSubnet();
            BeanUtils.copyProperties(subnetWebRequestObject, inSubnetEntity);

            String subnetId = inSubnetEntity.getId();
            String vpcId = inSubnetEntity.getVpcId();
            String cidr = inSubnetEntity.getCidr();
            String gatewayIp = inSubnetEntity.getGatewayIp();
            boolean gatewayIpIsValid = SubnetManagementUtil.checkGatewayIpInputSupported(gatewayIp, cidr);
            if (!gatewayIpIsValid) {
                throw new GatewayIpUnsupported();
            }
            boolean gatewayIpIsInAllocatedRange = SubnetManagementUtil.checkGatewayIpIsInAllocatedRange(gatewayIp, cidr);

            RestPreconditionsUtil.verifyResourceFound(vpcId);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetEntity, projectId);

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
            CompletableFuture<VpcWebJson> vpcFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return this.subnetService.verifyVpcId(projectId, vpcId);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, ThreadPoolExecutorUtils.SELECT_POOL_EXECUTOR);

            //Prepare Route Rule(IPv4/6) for Subnet
            SubnetEntity subnet = new SubnetEntity();
            BeanUtils.copyProperties(inSubnetEntity, subnet);
            CompletableFuture<RouteWebJson> routeFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return this.subnetService.createRouteRules(subnetId, subnet);
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
                    return this.subnetService.allocateIpAddressForGatewayPort(subnetId, cidr, vpcId, gatewayIp, gatewayIpIsInAllocatedRange);
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
            List<RouteEntity> routeEntities = new ArrayList<>();
            routeEntities.add(routeResponse.getRoute());
            inSubnetEntity.setRouteEntities(routeEntities);

            MacState macState = macResponse.getMacState();
            if (macState != null) {
                inSubnetEntity.setGatewayMacAddress(macState.getMacAddress());
            }
            if (gatewayIpIsInAllocatedRange) {
                inSubnetEntity.setGatewayIp(ipResponse.getIp());
            } else {
                String gatewayIP = SubnetManagementUtil.setGatewayIpValue(gatewayIp, cidr);
                if (gatewayIp != null) {
                    inSubnetEntity.setGatewayIp(gatewayIP);
                }
            }
            if (ipResponse != null && ipResponse.getIpVersion() == 4) {
                inSubnetEntity.setIpV4RangeId(ipResponse.getRangeId());
            }else if (ipResponse != null &&  ipResponse.getIpVersion() == 6) {
                inSubnetEntity.setIpV6RangeId(ipResponse.getRangeId());
            }

            // create_at and update_at
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(currentTime);
            String utc = DateUtil.localToUTC(dateString, "yyyy-MM-dd HH:mm:ss");
            inSubnetEntity.setCreated_at(utc);
            inSubnetEntity.setUpdated_at(utc);

            // tenant_id
            String tenantId = inSubnetEntity.getTenantId();
            if (tenantId == null) {
                inSubnetEntity.setTenantId(inSubnetEntity.getProjectId());
            }

            // enable_dhcp
            Boolean dhcpEnable = inSubnetEntity.getDhcpEnable();
            if (dhcpEnable == null) {
                inSubnetEntity.setDhcpEnable(true);
            }

            // allocation_pools
            List<AllocationPool> allocationPoolList = inSubnetEntity.getAllocationPools();
            if (allocationPoolList == null || allocationPoolList.size() == 0) {
                String[] ips = this.subnetService.cidrToFirstIpAndLastIp(cidr);
                List<AllocationPool> allocationPools = new ArrayList<>();
                AllocationPool allocationPool = new AllocationPool(ips[0], ips[1]);
                allocationPools.add(allocationPool);
                inSubnetEntity.setAllocationPools(allocationPools);
            }

            // tags
////            List<String> tags = inSubnetWebResponseObject.getTags();
////            if (tags == null) {
////                tags = new ArrayList<String>(){{add("tag1,tag2");}};
////                inSubnetWebResponseObject.setTags(tags);
////            }

            // revision_number
            Integer revisionNumber = inSubnetEntity.getRevisionNumber();
            if (revisionNumber == null || revisionNumber < 1) {
                inSubnetEntity.setRevisionNumber(1);
            }

            this.subnetDatabaseService.addSubnet(inSubnetEntity);

//            SubnetWebObject subnet = this.subnetDatabaseService.getBySubnetId(subnetId);
//            if (subnet == null) {
//                throw new ResourcePersistenceException();
//            }

            // update to vpc with subnet id
            this.subnetService.addSubnetIdToVpc(subnetId, projectId, vpcId);

            return new SubnetWebJson(inSubnetEntity);

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
            value = {"/project/{projectId}/subnets/{subnetId}"})
    public SubnetWebJson updateSubnetState(@PathVariable String projectId, @PathVariable String subnetId, @RequestBody SubnetRequestWebJson resource) throws Exception {

        SubnetEntity subnetEntity = null;

        try {

//            if (!SubnetManagementUtil.checkSubnetRequestResourceIsValid(resource)) {
//                throw new ResourceNotValidException("request resource is invalid");
//            }
            Preconditions.checkNotNull(resource, "resource can not be null");
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            SubnetWebRequestObject inSubnetWebResponseObject = resource.getSubnet();
            Preconditions.checkNotNull(inSubnetWebResponseObject, "Empty resource");
//            RestPreconditionsUtil.verifyResourceNotNull(inSubnetWebResponseObject);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetWebResponseObject, projectId);

            subnetEntity = this.subnetDatabaseService.getBySubnetId(subnetId);
            if (subnetEntity == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetId);
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetEntity.getProjectId(), projectId);
            BeanUtils.copyProperties(inSubnetWebResponseObject, subnetEntity,
                    CommonUtil.getBeanNullPropertyNames(inSubnetWebResponseObject));
            Integer revisionNumber = subnetEntity.getRevisionNumber();
            if (revisionNumber == null || revisionNumber < 1) {
                subnetEntity.setRevisionNumber(1);
            } else {
                subnetEntity.setRevisionNumber(revisionNumber + 1);
            }


            this.subnetDatabaseService.addSubnet(subnetEntity);
            subnetEntity = this.subnetDatabaseService.getBySubnetId(subnetId);

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

        return new SubnetWebJson(subnetEntity);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectId}/subnets/{subnetId}"})
    public ResponseId deleteSubnetState(@PathVariable String projectId, @PathVariable String subnetId) throws Exception {

        SubnetEntity subnetEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);

            subnetEntity = this.subnetDatabaseService.getBySubnetId(subnetId);
            if (subnetEntity == null) {
                return new ResponseId();
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetEntity.getProjectId(), projectId);

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

    @FieldFilter(type=SubnetEntity.class)
    @RequestMapping(
            method = GET,
            value = "/project/{projectId}/subnets")
    public SubnetsWebJson getSubnetStatesByProjectIdAndVpcId(@PathVariable String projectId) throws Exception {

        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(request.getParameterMap(), SubnetEntity.class);
        queryParams.put("project_id", new String[]{projectId});

        Map<String, SubnetEntity> subnetStates = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyResourceFound(projectId);

            subnetStates = this.subnetDatabaseService.getAllSubnets(queryParams);

        } catch (ParameterNullOrEmptyException | ResourceNotFoundException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }

        List<SubnetEntity> subnetEntityList = new ArrayList<>();
        for (Map.Entry<String, SubnetEntity> entry : subnetStates.entrySet()) {
            SubnetEntity tmp = (SubnetEntity) entry.getValue();
            subnetEntityList.add(tmp);
        }
        return new SubnetsWebJson(subnetEntityList);
    }


}
