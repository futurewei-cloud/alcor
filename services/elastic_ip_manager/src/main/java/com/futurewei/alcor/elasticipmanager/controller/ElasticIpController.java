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

package com.futurewei.alcor.elasticipmanager.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpNoProjectIdException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.*;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpRangeService;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpService;
import com.futurewei.alcor.elasticipmanager.utils.ElasticIpControllerUtils;
import com.futurewei.alcor.web.entity.elasticip.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class ElasticIpController {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpController.class);

    @Autowired
    ElasticIpService elasticipService;

    @Autowired
    ElasticIpRangeService elasticIpRangeService;

    /**
     * Create an elastic ip, and communicate with port and node services if the
     * elastic ip associated with a port.
     * @param projectId Project the elastic ip belongs to
     * @param request Elastic ip configuration
     * @return ElasticIpInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PostMapping("/project/{project_id}/elasticips")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ElasticIpInfoWrapper createElasticIp(@PathVariable("project_id") String projectId,
                                                @RequestBody ElasticIpInfoWrapper request) throws Exception {

        ElasticIpInfo requestInfo = request.getElasticip();

        ElasticIpControllerUtils.createElasticIpParameterProcess(projectId, requestInfo);

        ElasticIpInfo result = elasticipService.createElasticIp(requestInfo);

        return new ElasticIpInfoWrapper(result);
    }

    /**
     * Update an elastic ip, and communicate with port and node services if
     * the elastic ip's association state is changed.
     * @param projectId Project the elastic ip belongs to
     * @param elasticIpId Uuid of the elastic ip
     * @param request Elastic ip configuration
     * @return ElasticIpInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PutMapping("/project/{project_id}/elasticips/{elasticip_id}")
    @ResponseBody
    public ElasticIpInfoWrapper updateElasticIp(@PathVariable("project_id") String projectId,
                                                @PathVariable("elasticip_id") String elasticIpId,
                                                @RequestBody ElasticIpInfoWrapper request) throws Exception {

        ElasticIpInfo requestInfo = request.getElasticip();

        ElasticIpControllerUtils.updateElasticIpParameterProcess(projectId, elasticIpId, requestInfo);

        ElasticIpInfo result = elasticipService.updateElasticIp(requestInfo);

        return new ElasticIpInfoWrapper(result);
    }

    /**
     * Delete an elastic ip, and communicate with port and node services if the elastic ip
     * associated with a port.
     * @param projectId Project the elastic ip belongs to
     * @param elasticIpId Uuid of the elastic ip
     * @return ResponseId
     * @throws Exception Various exceptions that may occur during the create process
     */
    @DeleteMapping("/project/{project_id}/elasticips/{elasticip_id}")
    public ResponseId deleteElasticIp(@PathVariable("project_id") String projectId,
                                      @PathVariable("elasticip_id") String elasticIpId) throws Exception {

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        }

        if (StringUtils.isEmpty(elasticIpId)) {
            throw new ElasticIpNoIdException();
        }

        elasticipService.deleteElasticIp(projectId, elasticIpId);

        return new ResponseId(elasticIpId);
    }

    /**
     * Get the information of an elastic ip.
     * @param projectId Project the elastic ip belongs to
     * @param elasticIpId Uuid of the elastic ip
     * @return ElasticIpInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @GetMapping(value = {"/project/{project_id}/elasticips/{elasticip_id}"})
    public ElasticIpInfoWrapper getElasticIp(@PathVariable("project_id") String projectId,
                                             @PathVariable("elasticip_id") String elasticIpId)
            throws Exception {

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        }

        if (StringUtils.isEmpty(elasticIpId)) {
            throw new ElasticIpNoIdException();
        }

        ElasticIpInfo eip = elasticipService.getElasticIp(projectId, elasticIpId);

        return new ElasticIpInfoWrapper(eip);
    }

    /**
     * Get a list of information of each elastic ip belongs to the project.
     * @param projectId Project the elastic ip belongs to
     * @return ElasticIpsInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @GetMapping(value = {"/project/{project_id}/elasticips"})
    public ElasticIpsInfoWrapper getElasticIps(@PathVariable("project_id") String projectId)
            throws Exception {

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        }

        List<ElasticIpInfo> eips = elasticipService.getElasticIps(projectId);

        return new ElasticIpsInfoWrapper(eips);
    }

    /**
     * Create an elastic ip range, of which a list of allocation address ranges
     * included for assigning addresses to elastic ips.
     * @param request Elastic ip range configuration
     * @return ElasticIpRangeInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PostMapping("/elasticip-ranges")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ElasticIpRangeInfoWrapper createElasticIpRange(@RequestBody ElasticIpRangeInfoWrapper request)
            throws Exception {

        ElasticIpRangeInfo requestInfo = request.getElasticIpRange();

        ElasticIpControllerUtils.createElasticIpRangeParameterProcess(requestInfo);

        ElasticIpRangeInfo result = elasticIpRangeService.createElasticIpRange(requestInfo);

        return new ElasticIpRangeInfoWrapper(result);
    }

    /**
     * Update an elastic ip range.
     * @param elasticIpRangeId Uuid of the elastic ip range
     * @param request Elastic ip range configuration
     * @return ElasticIpRangeInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PutMapping("/elasticip-ranges/{elasticip_range_id}")
    @ResponseBody
    public ElasticIpRangeInfoWrapper updateElasticIpRange(@PathVariable("elasticip_range_id") String elasticIpRangeId,
                                                          @RequestBody ElasticIpRangeInfoWrapper request)
            throws Exception {

        ElasticIpRangeInfo requestInfo = request.getElasticIpRange();

        ElasticIpControllerUtils.updateElasticIpRangeParameterProcess(elasticIpRangeId, requestInfo);

        ElasticIpRangeInfo result = elasticIpRangeService.updateElasticIpRange(requestInfo);

        return new ElasticIpRangeInfoWrapper(result);
    }

    /**
     * Delete an elastic ip range. It will throw a exception if there is any elastic ip
     * has assigned an address belongs to this elastic ip range.
     * @param elasticIpRangeId Uuid of the elastic ip range
     * @return ResponseId
     * @throws Exception Various exceptions that may occur during the create process
     */
    @DeleteMapping("/elasticip-ranges/{elasticip_range_id}")
    public ResponseId deleteElasticIpRange(@PathVariable("elasticip_range_id") String elasticIpRangeId)
            throws Exception {

        if (StringUtils.isEmpty(elasticIpRangeId)) {
            throw new ElasticIpNoIdException();
        }

        elasticIpRangeService.deleteElasticIpRange(elasticIpRangeId);

        return new ResponseId(elasticIpRangeId);
    }

    /**
     * Get the information of an elastic ip range.
     * @param elasticIpRangeId Uuid of the elastic ip range
     * @return ElasticIpRangeInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @GetMapping(value = {"/elasticip-ranges/{elasticip_range_id}"})
    public ElasticIpRangeInfoWrapper getElasticIpRange(@PathVariable("elasticip_range_id") String elasticIpRangeId)
            throws Exception {

        if (StringUtils.isEmpty(elasticIpRangeId)) {
            throw new ElasticIpNoIdException();
        }

        ElasticIpRangeInfo eipRange = elasticIpRangeService.getElasticIpRange(elasticIpRangeId);

        return new ElasticIpRangeInfoWrapper(eipRange);
    }

    /**
     * Get a list of information of each elastic ip range.
     * @return ElasticIpRangesInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @GetMapping(value = {"/elasticip-ranges"})
    public ElasticIpRangesInfoWrapper getElasticIpRanges() throws Exception {

        List<ElasticIpRangeInfo> eipRanges = elasticIpRangeService.getElasticIpRanges();

        return new ElasticIpRangesInfoWrapper(eipRanges);
    }
}
