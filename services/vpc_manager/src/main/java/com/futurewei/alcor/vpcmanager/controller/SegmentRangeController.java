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
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.service.SegmentRangeDatabaseService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.vpcmanager.utils.SegmentRangeManagementUtil;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeEntity;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeWebRequestJson;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeWebRequest;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeWebResponseJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class SegmentRangeController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SegmentRangeDatabaseService segmentRangeDatabaseService;

    /**
     * Shows details for a network segment range
     * @param projectid
     * @param network_segment_range_id
     * @return segment range state
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/network_segment_ranges/{network_segment_range_id}"})
    @DurationStatistics
    public NetworkSegmentRangeWebResponseJson getSegmentRangeBySegmentRangeId(@PathVariable String projectid, @PathVariable String network_segment_range_id) throws Exception {

        NetworkSegmentRangeEntity segmentRangeWebResponseObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(network_segment_range_id);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            segmentRangeWebResponseObject = this.segmentRangeDatabaseService.getBySegmentRangeId(network_segment_range_id);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        if (segmentRangeWebResponseObject == null) {
            return new NetworkSegmentRangeWebResponseJson();
        }

        return new NetworkSegmentRangeWebResponseJson(segmentRangeWebResponseObject);

    }

    /**
     * Creates a network segment range
     * @param projectid
     * @param resource
     * @return segment range state
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/network_segment_ranges"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public NetworkSegmentRangeWebResponseJson createSegmentRange(@PathVariable String projectid, @RequestBody NetworkSegmentRangeWebRequestJson resource) throws Exception {

        NetworkSegmentRangeEntity segmentRangeWebResponseObject = new NetworkSegmentRangeEntity();

        try {

            if (!SegmentRangeManagementUtil.checkSegmentRangeRequestResourceIsValid(resource)) {
                throw new ResourceNotValidException("request resource is invalid");
            }

            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            NetworkSegmentRangeWebRequest segmentRangeWebRequestObject = resource.getNetwork_segment_range();
            BeanUtils.copyProperties(segmentRangeWebRequestObject, segmentRangeWebResponseObject);
            RestPreconditionsUtil.verifyResourceNotNull(segmentRangeWebResponseObject);
            RestPreconditionsUtil.populateResourceProjectId(segmentRangeWebResponseObject, projectid);

            segmentRangeWebResponseObject = SegmentRangeManagementUtil.configureSegmentRangeDefaultParameters(segmentRangeWebResponseObject);
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

    /**
     * Updates a network segment range
     * @param projectid
     * @param network_segment_range_id
     * @param resource
     * @return segment range state
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/network_segment_ranges/{network_segment_range_id}"})
    @DurationStatistics
    public NetworkSegmentRangeWebResponseJson updateSegmentRangeBySegmentRangeId(@PathVariable String projectid, @PathVariable String network_segment_range_id, @RequestBody NetworkSegmentRangeWebRequestJson resource) throws Exception {

        NetworkSegmentRangeEntity segmentRangeWebResponseObject = new NetworkSegmentRangeEntity();

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(network_segment_range_id);
            NetworkSegmentRangeWebRequest segmentRangeWebRequestObject = resource.getNetwork_segment_range();
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

    /**
     * Deletes a network segment range
     * @param projectid
     * @param network_segment_range_id
     * @return segment range id
     * @throws Exception
     */
    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/network_segment_ranges/{network_segment_range_id}"})
    @DurationStatistics
    public ResponseId deleteSegmentRangeBySegmentId(@PathVariable String projectid, @PathVariable String network_segment_range_id) throws Exception {

        NetworkSegmentRangeEntity segmentRangeWebResponseObject = null;

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

    /**
     * Lists network segment ranges to which the admin has access
     * @param projectid
     * @return Map<String, NetworkSegmentRangeWebResponseObject>
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/network_segment_ranges")
    @DurationStatistics
    public Map getSegmentRangesByProjectId(@PathVariable String projectid) throws Exception {

        Map<String, NetworkSegmentRangeEntity> segmentRanges = null;

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
