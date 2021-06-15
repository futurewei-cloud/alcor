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

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.exception.NetworkKeyNotEnoughException;
import com.futurewei.alcor.vpcmanager.service.SegmentDatabaseService;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.vpcmanager.utils.SegmentManagementUtil;
import com.futurewei.alcor.web.entity.vpc.SegmentEntity;
import com.futurewei.alcor.web.entity.vpc.SegmentWebRequestJson;
import com.futurewei.alcor.web.entity.vpc.SegmentWebRequest;
import com.futurewei.alcor.web.entity.vpc.SegmentWebResponseJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class SegmentController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VpcDatabaseService vpcDatabaseService;

    @Autowired
    private SegmentDatabaseService segmentDatabaseService;

    @Autowired
    private SegmentService segmentService;

    /**
     * Shows details for a segment
     * @param projectid
     * @param segmentid
     * @return segment state
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/segments/{segmentid}"})
    @DurationStatistics
    public SegmentWebResponseJson getSegmentBySegmentId(@PathVariable String projectid, @PathVariable String segmentid) throws Exception {

        SegmentEntity segmentEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(segmentid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentEntity = this.segmentDatabaseService.getBySegmentId(segmentid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        if (segmentEntity == null) {
            return new SegmentWebResponseJson();
        }

        return new SegmentWebResponseJson(segmentEntity);

    }

    /**
     * Creates a segment
     * @param projectid
     * @param resource
     * @return segment state
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/segments"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public SegmentWebResponseJson createSegment(@PathVariable String projectid, @RequestBody SegmentWebRequestJson resource) throws Exception {

        SegmentEntity segmentEntity = new SegmentEntity();
        String networkTypeId = UUID.randomUUID().toString();

        try {

            if (!SegmentManagementUtil.checkSegmentRequestResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            SegmentWebRequest segmentWebRequest = resource.getSegment();
            BeanUtils.copyProperties(segmentWebRequest, segmentEntity);
            RestPreconditionsUtil.verifyResourceNotNull(segmentEntity);
            RestPreconditionsUtil.populateResourceProjectId(segmentEntity, projectid);

            String vpcId = segmentWebRequest.getVpcId();
            VpcEntity vpcState = this.vpcDatabaseService.getByVpcId(vpcId);

            // verify network type
            String networkType = segmentWebRequest.getNetworkType();
            Long key = null;
            Integer mtu = vpcState.getMtu();
            if (NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)) {
                key = segmentService.addVxlanEntity(networkTypeId, networkType, vpcId, mtu);
            } else if (NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)) {
                key = segmentService.addVlanEntity(networkTypeId, networkType, vpcId, mtu);
            }else if (NetworkTypeEnum.GRE.getNetworkType().equals(networkType)) {
                key = segmentService.addGreEntity(networkTypeId, networkType, vpcId, mtu);
            }

            if (key != null) {
                segmentEntity.setSegmentationId(Integer.parseInt(String.valueOf(key)));
                segmentEntity.setSegmentationUUID(networkTypeId);
            }

            segmentEntity = SegmentManagementUtil.configureSegmentDefaultParameters(segmentEntity);
            this.segmentDatabaseService.addSegment(segmentEntity);

            segmentEntity = this.segmentDatabaseService.getBySegmentId(segmentEntity.getId());
            if (segmentEntity == null) {
                throw new ResourcePersistenceException();
            }

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        } catch (NetworkKeyNotEnoughException e) {
            throw new Exception(e);
        }

        return new SegmentWebResponseJson(segmentEntity);

    }

    /**
     * Updates a segment
     * @param projectid
     * @param segmentid
     * @param resource
     * @return segment state
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/segments/{segmentid}"})
    @DurationStatistics
    public SegmentWebResponseJson updateSegmentBySegmentId(@PathVariable String projectid, @PathVariable String segmentid, @RequestBody SegmentWebRequestJson resource) throws Exception {

        SegmentEntity segmentEntity = new SegmentEntity();

        try {

            if (!SegmentManagementUtil.checkSegmentRequestResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(segmentid);
            SegmentWebRequest segmentWebRequest = resource.getSegment();
            BeanUtils.copyProperties(segmentWebRequest, segmentEntity);
            RestPreconditionsUtil.verifyResourceNotNull(segmentEntity);
            RestPreconditionsUtil.populateResourceProjectId(segmentEntity, projectid);
            RestPreconditionsUtil.populateResourceSegmentId(segmentEntity, segmentid);

            segmentEntity = this.segmentDatabaseService.getBySegmentId(segmentid);
            if (segmentEntity == null) {
                throw new ResourceNotFoundException("Segment not found : " + segmentid);
            }

            BeanUtils.copyProperties(segmentWebRequest, segmentEntity);
            Integer revisionNumber = segmentEntity.getRevisionNumber();
            if (revisionNumber == null) {
                segmentEntity.setRevisionNumber(1);
            } else {
                segmentEntity.setRevisionNumber(revisionNumber + 1);
            }

            this.segmentDatabaseService.addSegment(segmentEntity);

            segmentEntity = this.segmentDatabaseService.getBySegmentId(segmentid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new SegmentWebResponseJson(segmentEntity);

    }

    /**
     * Deletes a segment and its associated resources
     * @param projectid
     * @param segmentid
     * @return segment id
     * @throws Exception
     */
    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/segments/{segmentid}"})
    @DurationStatistics
    public ResponseId deleteSegmentBySegmentId(@PathVariable String projectid, @PathVariable String segmentid) throws Exception {

        SegmentEntity segmentEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(segmentid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentEntity = this.segmentDatabaseService.getBySegmentId(segmentid);
            if (segmentEntity == null) {
                return new ResponseId();
            }

            segmentDatabaseService.deleteSegment(segmentid);

            // Release Network Type
            String networkType = segmentEntity.getNetworkType();
            Long key = Long.parseLong(String.valueOf(segmentEntity.getSegmentationId()));
            if (NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)) {
                this.segmentService.releaseVxlanEntity(segmentEntity.getSegmentationUUID(), key);
            } else if (NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)) {
                this.segmentService.releaseVlanEntity(segmentEntity.getSegmentationUUID(), key);
            }else if (NetworkTypeEnum.GRE.getNetworkType().equals(networkType)) {
                this.segmentService.releaseGreEntity(segmentEntity.getSegmentationUUID(), key);
            }


        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(segmentid);

    }

    /**
     * Lists segments to which the project has acces
     * @param projectid
     * @return Map<String, SegmentWebResponseObject>
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/segments")
    @DurationStatistics
    public Map getSegmentsByProjectId(@PathVariable String projectid) throws Exception {

        Map<String, SegmentEntity> segments = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segments = this.segmentDatabaseService.getAllSegments();
            segments = segments.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return segments;

    }

    @RequestMapping(
            method = POST,
            value = "/segments/createDefaultTable")
    @DurationStatistics
    public void createDefaultTable() throws Exception {

        try {

            this.segmentService.createDefaultNetworkTypeTable();

        } catch (Exception e) {
            throw e;
        }


    }

}
