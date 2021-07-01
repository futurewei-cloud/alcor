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

package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourceNotValidException;
import com.futurewei.alcor.common.exception.ResourceNullException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.service.VpcService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.vpcmanager.utils.VpcManagementUtil;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.*;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import com.futurewei.alcor.web.rbac.aspect.Rbac;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.futurewei.alcor.common.constants.CommonConstants.QUERY_ATTR_HEADER;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class VpcController {

    @Autowired
    private VpcDatabaseService vpcDatabaseService;

    @Autowired
    private VpcService vpcService;

    @Autowired
    private HttpServletRequest request;

    /**
     * hows details for a network
     *
     * @param projectid
     * @param vpcid
     * @return vpc state
     * @throws Exception
     */
    @Rbac(resource ="vpc")
    @FieldFilter(type = VpcEntity.class)
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}"})
    @DurationStatistics
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
    @DurationStatistics
    public VpcsWebJson createVpcStateBulk(@PathVariable String projectid, @RequestBody VpcsWebJson resource) throws Exception {
        return new VpcsWebJson();
    }

    /**
     * Creates a network
     *
     * @param projectid
     * @param resource
     * @return vpc state
     * @throws Exception
     */
    @Rbac(resource ="vpc")
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public VpcWebJson createVpcState(@PathVariable String projectid, @RequestBody VpcWebRequestJson resource) throws Exception {
        VpcEntity inVpcState = new VpcEntity();

        if (StringUtils.isEmpty(resource.getNetwork().getId())) {
            UUID vpcId = UUID.randomUUID();
            resource.getNetwork().setId(vpcId.toString());
        }

        try {

            if (!VpcManagementUtil.checkVpcRequestResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            VpcWebRequest vpcWebRequest = resource.getNetwork();
            BeanUtils.copyProperties(vpcWebRequest, inVpcState);
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);

            inVpcState = VpcManagementUtil.configureNetworkDefaultParameters(inVpcState);

            // Check segments
            List<SegmentInfoInVpc> segments = vpcWebRequest.getSegments();
            if (segments != null) {
                List<SegmentInfoInVpc> newSegments = new ArrayList<>();
                for (SegmentInfoInVpc segmentInfo : segments) {
                    SegmentInfoInVpc newSegmentInfo = new SegmentInfoInVpc(segmentInfo.getNetworkType(),
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

            // allocate a segment for network
            inVpcState = this.vpcService.allocateSegmentForNetwork(inVpcState);

            this.vpcDatabaseService.addVpc(inVpcState);

            // register VPC with GM
            this.vpcService.registerVpc(inVpcState);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new VpcWebJson(inVpcState);
    }

    /**
     * Updates a network
     *
     * @param projectid
     * @param vpcid
     * @param resource
     * @return vpc state
     * @throws Exception
     */
    @Rbac(resource ="vpc")
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}"})
    @DurationStatistics
    public VpcWebJson updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody VpcWebRequestJson resource) throws Exception {

        VpcEntity inVpcState = new VpcEntity();

        try {
            //TODO for update it's incremental update, so no need check this
//            if (!VpcManagementUtil.checkVpcRequestResourceIsValid(resource)) {
//                throw new ResourceNotValidException("request resource is invalid");
//            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);

            VpcWebRequest vpcWebRequest = resource.getNetwork();
            BeanUtils.copyProperties(vpcWebRequest, inVpcState);
//            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);
            RestPreconditionsUtil.populateResourceVpcId(inVpcState, vpcid);

            inVpcState = this.vpcDatabaseService.getByVpcId(vpcid);
            if (inVpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            // null field no need copy
            BeanUtils.copyProperties(vpcWebRequest, inVpcState,
                    CommonUtil.getBeanNullPropertyNames(vpcWebRequest));
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
     *
     * @param projectid
     * @param vpcid
     * @return network id
     * @throws Exception
     */
    @Rbac(resource ="vpc")
    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpcs/{vpcid}"})
    @DurationStatistics
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

            this.vpcService.checkSubnetsAreEmpty(vpcState);

            vpcDatabaseService.deleteVpc(vpcid);

            // unRegister VPC with GM
            this.vpcService.unRegisterVpc(vpcState);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }

    /**
     * Lists networks to which the project has access
     *
     * @param projectId
     * @return Map<String, VpcWebResponseObject>
     * @throws Exception
     */
    @Rbac(resource ="vpc")
    @FieldFilter(type = VpcEntity.class)
    @RequestMapping(
            method = GET,
            value = "/project/{projectId}/vpcs")
    @DurationStatistics
    public VpcsWebJson getVpcStatesByProjectId(@PathVariable String projectId) throws Exception {
        Map<String, VpcEntity> vpcStates = null;
        Map<String, String[]> requestParams = (Map<String, String[]>)request.getAttribute(QUERY_ATTR_HEADER);
        requestParams = requestParams == null ? request.getParameterMap():requestParams;
        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(requestParams, VpcEntity.class);

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyResourceFound(projectId);

            vpcStates = this.vpcDatabaseService.getAllVpcs(queryParams);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return new VpcsWebJson(new ArrayList<>(vpcStates.values()));
    }

    /**
     * List and count all networks
     *
     * @return
     * @throws CacheException
     */
    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs/count")
    @DurationStatistics
    public Map getVpcCountAndAllVpcStates() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = vpcDatabaseService.getAllVpcs();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }

    /**
     * Updates a network with subnet id
     * @param projectid
     * @param vpcid
     * @param subnetid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    @DurationStatistics
    public VpcWebJson addSubnetIdToVpcState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {

        VpcEntity inVpcState = new VpcEntity();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);

            inVpcState = this.vpcDatabaseService.getByVpcId(vpcid);
            if (inVpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            List<String> subnets = inVpcState.getSubnets();
            if (subnets == null) {
                subnets = new ArrayList<>();
            }
            if (!subnets.contains(subnetid)) {
                subnets.add(subnetid);
            }
            inVpcState.setSubnets(subnets);

            this.vpcDatabaseService.addVpc(inVpcState);

            inVpcState = this.vpcDatabaseService.getByVpcId(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new VpcWebJson(inVpcState);

    }

    /**
     * delete subnet id in a network
     * @param projectid
     * @param vpcid
     * @param subnetid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnetid/{subnetid}"})
    @DurationStatistics
    public VpcWebJson deleteSubnetIdInVpcState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {

        VpcEntity inVpcState = new VpcEntity();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);

            inVpcState = this.vpcDatabaseService.getByVpcId(vpcid);
            if (inVpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            List<String> subnets = inVpcState.getSubnets();
            if (subnets == null || !subnets.contains(subnetid)) {
                return new VpcWebJson(inVpcState);
            }
            subnets.remove(subnetid);

            inVpcState.setSubnets(subnets);

            this.vpcDatabaseService.addVpc(inVpcState);

            inVpcState = this.vpcDatabaseService.getByVpcId(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new VpcWebJson(inVpcState);

    }
}