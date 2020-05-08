package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourceNullException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.vpcmanager.service.SegmentDatabaseService;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
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

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/segments/{segmentid}", "/v4/{projectid}/segments/{segmentid}"})
    public SegmentWebResponseJson getSegmentBySegmentId(@PathVariable String projectid, @PathVariable String segmentid) throws Exception {

        SegmentWebResponseObject segmentWebResponseObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(segmentid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentWebResponseObject = this.segmentDatabaseService.getBySegmentId(segmentid);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (segmentWebResponseObject == null) {
            //TODO: REST error code
            return new SegmentWebResponseJson();
        }

        return new SegmentWebResponseJson(segmentWebResponseObject);

    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/segments", "/v4/{projectid}/segments"})
    @ResponseStatus(HttpStatus.CREATED)
    public SegmentWebResponseJson createSegment(@PathVariable String projectid, @RequestBody SegmentWebRequestJson resource) throws Exception {

        SegmentWebResponseObject segmentWebResponseObject = new SegmentWebResponseObject();
        String networkTypeId = UUID.randomUUID().toString();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            SegmentWebRequestObject segmentWebRequestObject = resource.getSegment();
            BeanUtils.copyProperties(segmentWebRequestObject, segmentWebResponseObject);
            RestPreconditionsUtil.verifyResourceNotNull(segmentWebResponseObject);
            RestPreconditionsUtil.populateResourceProjectId(segmentWebResponseObject, projectid);

            // verify network type
            String networkType = segmentWebRequestObject.getNetworkType();
            Long key = null;
            if (NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)) {
                key = segmentService.addVxlanEntity(segmentWebRequestObject.getId(), networkTypeId, networkType);
            } else if (NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)) {
                key = segmentService.addVlanEntity(segmentWebRequestObject.getId(), networkTypeId, networkType);
            }else if (NetworkTypeEnum.GRE.getNetworkType().equals(networkType)) {
                key = segmentService.addGreEntity(segmentWebRequestObject.getId(), networkTypeId, networkType);
            }

            if (key != null) {
                segmentWebResponseObject.setSegmentationId(Integer.parseInt(String.valueOf(key)));
            }
            segmentWebResponseObject.setSegmentationUUID(networkTypeId);

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

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/segments/{segmentid}", "/v4/{projectid}/segments/{segmentid}"})
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

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/segments/{segmentid}", "/v4/{projectid}/segments/{segmentid}"})
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

            // Release Network Type
            String networkType = segmentWebResponseObject.getNetworkType();
            Long key = Long.parseLong(String.valueOf(segmentWebResponseObject.getSegmentationId()));
            if (NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)) {
                this.segmentService.releaseVxlanEntity(segmentWebResponseObject.getSegmentationUUID(), key);
            } else if (NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)) {
                this.segmentService.releaseVlanEntity(segmentWebResponseObject.getSegmentationUUID(), key);
            }else if (NetworkTypeEnum.GRE.getNetworkType().equals(networkType)) {

            }

            segmentDatabaseService.deleteSegment(segmentid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(segmentid);

    }

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
