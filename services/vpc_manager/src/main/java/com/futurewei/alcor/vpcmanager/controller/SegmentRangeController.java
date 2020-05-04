package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourceNullException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.vpcmanager.service.SegmentRangeDatabaseService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class SegmentRangeController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SegmentRangeDatabaseService segmentRangeDatabaseService;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/network_segment_ranges/{network_segment_range_id}", "/v4/{projectid}/network_segment_ranges/{network_segment_range_id}"})
    public NetworkSegmentRangeWebResponseJson getSegmentRangeBySegmentRangeId(@PathVariable String projectid, @PathVariable String network_segment_range_id) throws Exception {

        NetworkSegmentRangeWebResponseObject segmentRangeWebResponseObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(network_segment_range_id);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentRangeWebResponseObject = this.segmentRangeDatabaseService.getBySegmentRangeId(network_segment_range_id);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (segmentRangeWebResponseObject == null) {
            //TODO: REST error code
            return new NetworkSegmentRangeWebResponseJson();
        }

        return new NetworkSegmentRangeWebResponseJson(segmentRangeWebResponseObject);

    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/network_segment_ranges", "/v4/{projectid}/network_segment_ranges"})
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkSegmentRangeWebResponseJson createSegmentRange(@PathVariable String projectid, @RequestBody NetworkSegmentRangeWebRequestJson resource) throws Exception {

        NetworkSegmentRangeWebResponseObject segmentRangeWebResponseObject = new NetworkSegmentRangeWebResponseObject();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            NetworkSegmentRangeWebRequestObject segmentRangeWebRequestObject = resource.getNetwork_segment_range();
            BeanUtils.copyProperties(segmentRangeWebRequestObject, segmentRangeWebResponseObject);
            RestPreconditionsUtil.verifyResourceNotNull(segmentRangeWebResponseObject);
            RestPreconditionsUtil.populateResourceProjectId(segmentRangeWebResponseObject, projectid);

            this.segmentRangeDatabaseService.addSegmentRange(segmentRangeWebResponseObject);

            segmentRangeWebResponseObject = this.segmentRangeDatabaseService.getBySegmentRangeId(segmentRangeWebResponseObject.getId());
            if (segmentRangeWebResponseObject == null) {
                throw new ResourcePersistenceException();
            }

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new NetworkSegmentRangeWebResponseJson(segmentRangeWebResponseObject);

    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/network_segment_ranges/{network_segment_range_id}", "/v4/{projectid}/network_segment_ranges/{network_segment_range_id}"})
    public NetworkSegmentRangeWebResponseJson updateSegmentRangeBySegmentRangeId(@PathVariable String projectid, @PathVariable String network_segment_range_id, @RequestBody NetworkSegmentRangeWebRequestJson resource) throws Exception {

        NetworkSegmentRangeWebResponseObject segmentRangeWebResponseObject = new NetworkSegmentRangeWebResponseObject();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(network_segment_range_id);
            NetworkSegmentRangeWebRequestObject segmentRangeWebRequestObject = resource.getNetwork_segment_range();
            BeanUtils.copyProperties(segmentRangeWebRequestObject, segmentRangeWebResponseObject);
            RestPreconditionsUtil.verifyResourceNotNull(segmentRangeWebResponseObject);
            RestPreconditionsUtil.populateResourceProjectId(segmentRangeWebResponseObject, projectid);
            RestPreconditionsUtil.populateResourceSegmentRangeId(segmentRangeWebResponseObject, network_segment_range_id);

            segmentRangeWebResponseObject = this.segmentRangeDatabaseService.getBySegmentRangeId(network_segment_range_id);
            if (segmentRangeWebResponseObject == null) {
                throw new ResourceNotFoundException("Segment range not found : " + network_segment_range_id);
            }

            this.segmentRangeDatabaseService.addSegmentRange(segmentRangeWebResponseObject);

            segmentRangeWebResponseObject = this.segmentRangeDatabaseService.getBySegmentRangeId(network_segment_range_id);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new NetworkSegmentRangeWebResponseJson(segmentRangeWebResponseObject);

    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/network_segment_ranges/{network_segment_range_id}", "/v4/{projectid}/network_segment_ranges/{network_segment_range_id}"})
    public ResponseId deleteSegmentRangeBySegmentId(@PathVariable String projectid, @PathVariable String network_segment_range_id) throws Exception {

        NetworkSegmentRangeWebResponseObject segmentRangeWebResponseObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(network_segment_range_id);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentRangeWebResponseObject = this.segmentRangeDatabaseService.getBySegmentRangeId(network_segment_range_id);
            if (segmentRangeWebResponseObject == null) {
                return new ResponseId();
            }

            segmentRangeDatabaseService.deleteSegmentRange(network_segment_range_id);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(network_segment_range_id);

    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/network_segment_ranges")
    public Map getSegmentRangesByProjectId(@PathVariable String projectid) throws Exception {

        Map<String, NetworkSegmentRangeWebResponseObject> segmentRanges = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentRanges = this.segmentRangeDatabaseService.getAllSegmentRanges();
            segmentRanges = segmentRanges.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return segmentRanges;

    }
}
