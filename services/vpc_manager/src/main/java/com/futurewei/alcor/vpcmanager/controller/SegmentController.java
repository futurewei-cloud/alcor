package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.vpcmanager.service.SegmentDatabaseService;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.vpcmanager.utils.SegmentManagementUtil;
import com.futurewei.alcor.web.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class SegmentController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

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
    public SegmentWebResponseJson getSegmentBySegmentId(@PathVariable String projectid, @PathVariable String segmentid) throws Exception {

        SegmentWebResponseObject segmentWebResponseObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(segmentid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentWebResponseObject = this.segmentDatabaseService.getBySegmentId(segmentid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        if (segmentWebResponseObject == null) {
            return new SegmentWebResponseJson();
        }

        return new SegmentWebResponseJson(segmentWebResponseObject);

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
    public SegmentWebResponseJson createSegment(@PathVariable String projectid, @RequestBody SegmentWebRequestJson resource) throws Exception {

        SegmentWebResponseObject segmentWebResponseObject = new SegmentWebResponseObject();
        String networkTypeId = UUID.randomUUID().toString();

        try {
            if (!SegmentManagementUtil.checkSegmentCreateResourceIsValid(resource)) {
                throw new ResourceNotValidException("vpc resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            SegmentWebRequestObject segmentWebRequestObject = resource.getSegment();
            BeanUtils.copyProperties(segmentWebRequestObject, segmentWebResponseObject);
            RestPreconditionsUtil.verifyResourceNotNull(segmentWebResponseObject);
            RestPreconditionsUtil.populateResourceProjectId(segmentWebResponseObject, projectid);

            // verify network type
            String networkType = segmentWebRequestObject.getNetworkType();
            Long key = null;
            if (NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)) {
                key = segmentService.addVxlanEntity(networkTypeId, networkType, segmentWebRequestObject.getVpcId());
            } else if (NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)) {
                key = segmentService.addVlanEntity(networkTypeId, networkType, segmentWebRequestObject.getVpcId());
            }else if (NetworkTypeEnum.GRE.getNetworkType().equals(networkType)) {
                key = segmentService.addGreEntity(networkTypeId, networkType, segmentWebRequestObject.getVpcId());
            }

            if (key != null) {
                segmentWebResponseObject.setSegmentationId(Integer.parseInt(String.valueOf(key)));
                segmentWebResponseObject.setSegmentationUUID(networkTypeId);
            }

            segmentWebResponseObject = SegmentManagementUtil.configureSegmentDefaultParameters(segmentWebResponseObject);
            this.segmentDatabaseService.addSegment(segmentWebResponseObject);

            segmentWebResponseObject = this.segmentDatabaseService.getBySegmentId(segmentWebResponseObject.getId());
            if (segmentWebResponseObject == null) {
                throw new ResourcePersistenceException();
            }

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new SegmentWebResponseJson(segmentWebResponseObject);

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
    public SegmentWebResponseJson updateSegmentBySegmentId(@PathVariable String projectid, @PathVariable String segmentid, @RequestBody SegmentWebRequestJson resource) throws Exception {

        SegmentWebResponseObject segmentWebResponseObject = new SegmentWebResponseObject();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(segmentid);
            SegmentWebRequestObject segmentWebRequestObject = resource.getSegment();
            BeanUtils.copyProperties(segmentWebRequestObject, segmentWebResponseObject);
            RestPreconditionsUtil.verifyResourceNotNull(segmentWebResponseObject);
            RestPreconditionsUtil.populateResourceProjectId(segmentWebResponseObject, projectid);
            RestPreconditionsUtil.populateResourceSegmentId(segmentWebResponseObject, segmentid);

            segmentWebResponseObject = this.segmentDatabaseService.getBySegmentId(segmentid);
            if (segmentWebResponseObject == null) {
                throw new ResourceNotFoundException("Segment not found : " + segmentid);
            }

            this.segmentDatabaseService.addSegment(segmentWebResponseObject);

            segmentWebResponseObject = this.segmentDatabaseService.getBySegmentId(segmentid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new SegmentWebResponseJson(segmentWebResponseObject);

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
    public ResponseId deleteSegmentBySegmentId(@PathVariable String projectid, @PathVariable String segmentid) throws Exception {

        SegmentWebResponseObject segmentWebResponseObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(segmentid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentWebResponseObject = this.segmentDatabaseService.getBySegmentId(segmentid);
            if (segmentWebResponseObject == null) {
                return new ResponseId();
            }

            segmentDatabaseService.deleteSegment(segmentid);

            // Release Network Type
            String networkType = segmentWebResponseObject.getNetworkType();
            Long key = Long.parseLong(String.valueOf(segmentWebResponseObject.getSegmentationId()));
            if (NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)) {
                this.segmentService.releaseVxlanEntity(segmentWebResponseObject.getSegmentationUUID(), key);
            } else if (NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)) {
                this.segmentService.releaseVlanEntity(segmentWebResponseObject.getSegmentationUUID(), key);
            }else if (NetworkTypeEnum.GRE.getNetworkType().equals(networkType)) {
                this.segmentService.releaseGreEntity(segmentWebResponseObject.getSegmentationUUID(), key);
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
    public Map getSegmentsByProjectId(@PathVariable String projectid) throws Exception {

        Map<String, SegmentWebResponseObject> segments = null;

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

}
