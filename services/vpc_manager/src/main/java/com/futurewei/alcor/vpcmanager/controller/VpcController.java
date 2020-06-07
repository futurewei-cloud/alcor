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

package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.service.VpcService;
import com.futurewei.alcor.vpcmanager.utils.VpcManagementUtil;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.vpc.SegmentInfoInVpc;
import com.futurewei.alcor.web.entity.vpc.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class VpcController {

    @Autowired
    private VpcDatabaseService vpcDatabaseService;

    @Autowired
    private VpcService vpcService;

    /**
     * Verify vpc state and update subnets property when ceating subnet
     * @param projectid
     * @param vpcid
     * @param subnetid
     * @return VpcWebJson
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public VpcWebJson verifyVpcStateByVpcIdAndUpdateSubnetId(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {

        VpcEntity vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (vpcState == null) {
            //TODO: REST error code
            return new VpcWebJson();
        }

        List<String> subnets = vpcState.getSubnets();
        if (subnets == null) {
            subnets = new ArrayList<>();
        }
        subnets.add(subnetid);
        vpcState.setSubnets(subnets);

        this.vpcDatabaseService.addVpc(vpcState);

        return new VpcWebJson(vpcState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public VpcWebJson updateSubnetIdInVpc(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {
        VpcEntity vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);

            if (vpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            List<String> subnets = vpcState.getSubnets();
            if (subnets != null) {
                int index = -1;
                for (int i = 0; i < subnets.size(); i ++) {
                    if (subnets.get(i).equals(subnetid)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    subnets.remove(index);
                }
            }
            vpcState.setSubnets(subnets);

            this.vpcDatabaseService.addVpc(vpcState);

        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        return new VpcWebJson(vpcState);

    }

    /**
     * hows details for a network
     * @param projectid
     * @param vpcid
     * @return vpc state
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}"})
    public VpcWebJson getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcEntity vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (vpcState == null) {
            //TODO: REST error code
            return new VpcWebJson();
        }

        return new VpcWebJson(vpcState);
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/vpcs/bulk"})
    @ResponseStatus(HttpStatus.CREATED)
    public VpcsWebJson createVpcStateBulk(@PathVariable String projectid, @RequestBody VpcsWebJson resource) throws Exception {
        return new VpcsWebJson();
    }

    /**
     * Creates a network
     * @param projectid
     * @param resource
     * @return vpc state
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    public VpcWebJson createVpcState(@PathVariable String projectid, @RequestBody VpcWebRequestJson resource) throws Exception {
        VpcEntity inVpcState = new VpcEntity();

        try {

            if (!VpcManagementUtil.checkVpcRequestResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            VpcWebRequest vpcWebRequest = resource.getNetwork();
            BeanUtils.copyProperties(vpcWebRequest, inVpcState);
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);

            this.vpcDatabaseService.addVpc(inVpcState);

            inVpcState = this.vpcDatabaseService.getByVpcId(inVpcState.getId());
            if (inVpcState == null) {
                throw new ResourcePersistenceException();
            }
            inVpcState = VpcManagementUtil.configureNetworkDefaultParameters(inVpcState);

            // Check segments
            List<SegmentInfoInVpc> segments = vpcWebRequest.getSegments();
            if (segments != null) {
                List<SegmentInfoInVpc> newSegments = new ArrayList<>();
                for (SegmentInfoInVpc segmentInfo : segments) {
                    SegmentInfoInVpc newSegmentInfo = new SegmentInfoInVpc (segmentInfo.getNetworkType(),
                            segmentInfo.getPhysicalNetwork(),
                            segmentInfo.getSegmentationId());
                    newSegments.add(newSegmentInfo);
                }
                inVpcState.setSegments(newSegments);
            }

            // get route info
            RouteWebJson response = this.vpcService.getRoute(inVpcState.getId(), inVpcState);

            // add RouteWebObject
            if (response != null) {
                List<RouteEntity> routeEntityList = inVpcState.getRouteEntities();
                if (routeEntityList == null) {
                    routeEntityList = new ArrayList<>();
                }
                routeEntityList.add(response.getRoute());
                inVpcState.setRouteEntities(routeEntityList);
            }
            this.vpcDatabaseService.addVpc(inVpcState);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new VpcWebJson(inVpcState);
    }

    /**
     * Updates a network
     * @param projectid
     * @param vpcid
     * @param resource
     * @return vpc state
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}"})
    public VpcWebJson updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody VpcWebRequestJson resource) throws Exception {

        VpcEntity inVpcState = new VpcEntity();

        try {

            if (!VpcManagementUtil.checkVpcRequestResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);

            VpcWebRequest vpcWebRequest = resource.getNetwork();
            BeanUtils.copyProperties(vpcWebRequest, inVpcState);
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);
            RestPreconditionsUtil.populateResourceVpcId(inVpcState, vpcid);

            inVpcState = this.vpcDatabaseService.getByVpcId(vpcid);
            if (inVpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            BeanUtils.copyProperties(vpcWebRequest, inVpcState);
            Integer revisionNumber = inVpcState.getRevisionNumber();
            if (revisionNumber == null) {
                inVpcState.setRevisionNumber(1);
            } else {
                inVpcState.setRevisionNumber(revisionNumber + 1);
            }

            this.vpcDatabaseService.addVpc(inVpcState);

            inVpcState = this.vpcDatabaseService.getByVpcId(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new VpcWebJson(inVpcState);
    }

    /**
     * Deletes a network and its associated resources
     * @param projectid
     * @param vpcid
     * @return network id
     * @throws Exception
     */
    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpcs/{vpcid}"})
    public ResponseId deleteVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        VpcEntity vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);
            if (vpcState == null) {
                return new ResponseId();
            }

            vpcDatabaseService.deleteVpc(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }

    /**
     * Lists networks to which the project has access
     * @param projectid
     * @return Map<String, VpcWebResponseObject>
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs")
    public Map getVpcStatesByProjectId(@PathVariable String projectid) throws Exception {
        Map<String, VpcEntity> vpcStates = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcStates = this.vpcDatabaseService.getAllVpcs();
            vpcStates = vpcStates.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return vpcStates;
    }

    /**
     * List and count all networks
     * @return
     * @throws CacheException
     */
    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs/count")
    public Map getVpcCountAndAllVpcStates() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = vpcDatabaseService.getAllVpcs();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }
}