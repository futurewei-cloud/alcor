/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.elasticipmanager.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpNoProjectIdException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.*;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpRangeService;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpService;
import com.futurewei.alcor.elasticipmanager.utils.ElasticIpControllerUtils;
import com.futurewei.alcor.web.entity.elasticip.*;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import com.futurewei.alcor.web.rbac.aspect.Rbac;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.futurewei.alcor.common.constants.CommonConstants.QUERY_ATTR_HEADER;


@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class ElasticIpController {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpController.class);

    @Autowired
    ElasticIpService elasticipService;

    @Autowired
    ElasticIpRangeService elasticIpRangeService;

    @Autowired
    private HttpServletRequest request;

    /**
     * Create an elastic ip, and communicate with port and node services if the
     * elastic ip associated with a port.
     * @param projectId Project the elastic ip belongs to
     * @param request Elastic ip configuration
     * @return ElasticIpInfoWrapper
     * @throws Exception Various exceptions that may occur during the create process
     */
    @Rbac(resource ="eip")
    @PostMapping("/project/{project_id}/elasticips")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
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
    @Rbac(resource ="eip")
    @PutMapping("/project/{project_id}/elasticips/{elasticip_id}")
    @ResponseBody
    @DurationStatistics
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
    @Rbac(resource ="eip")
    @DeleteMapping("/project/{project_id}/elasticips/{elasticip_id}")
    @DurationStatistics
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
    @Rbac(resource ="eip")
    @GetMapping(value = {"/project/{project_id}/elasticips/{elasticip_id}"})
    @DurationStatistics
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
    @Rbac(resource ="eip")
    @GetMapping(value = {"/project/{project_id}/elasticips"})
    @FieldFilter(type= ElasticIp.class)
    @DurationStatistics
    public ElasticIpsInfoWrapper getElasticIps(@PathVariable("project_id") String projectId)
            throws Exception {

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        }

        Map<String, String[]> requestParams = (Map<String, String[]>)request.getAttribute(QUERY_ATTR_HEADER);
        requestParams = requestParams == null ? request.getParameterMap():requestParams;
        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(requestParams, ElasticIp.class);

        List<ElasticIpInfo> eips = elasticipService.getElasticIps(projectId, queryParams);

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
    @DurationStatistics
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
    @DurationStatistics
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
    @DurationStatistics
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
    @DurationStatistics
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
    @DurationStatistics
    public ElasticIpRangesInfoWrapper getElasticIpRanges() throws Exception {

        List<ElasticIpRangeInfo> eipRanges = elasticIpRangeService.getElasticIpRanges();

        return new ElasticIpRangesInfoWrapper(eipRanges);
    }
}
