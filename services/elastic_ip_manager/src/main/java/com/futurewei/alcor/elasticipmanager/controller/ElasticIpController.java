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
import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.elasticipmanager.config.IpVersion;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpIdConfilictException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpNoProjectIdException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpProjectIdConflictException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.*;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpQueryFormatException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeBadRangesException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeNoIdException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeVersionException;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpRangeService;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpService;
import com.futurewei.alcor.web.entity.elasticip.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


@RestController
public class ElasticIpController {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpController.class);

    @Autowired
    ElasticIpService elasticipService;

    @Autowired
    ElasticIpRangeService elasticIpRangeService;

    private boolean isIpAddressInvalid(String ipAddress, Integer ipVersion) {
        boolean isInvalid = true;
        if (ipVersion.equals(IpVersion.IPV4.getVersion())) {
            isInvalid = !Ipv4AddrUtil.formatCheck(ipAddress);
        } else if (ipVersion.equals(IpVersion.IPV6.getVersion())) {
            isInvalid = !Ipv6AddrUtil.formatCheck(ipAddress);
        }

        return isInvalid;
    }

    private boolean isIpVersionInvalid(Integer ipVersion) {
        return !ipVersion.equals(IpVersion.IPV4.getVersion()) && !ipVersion.equals(IpVersion.IPV6.getVersion());
    }

    private boolean isAllocationRangesInvalid(Integer ipVersion,
                                              List<ElasticIpRange.AllocationRange> allocationRanges) {

        boolean isInvalid = false;
        if (allocationRanges != null) {

            try {
                if (ipVersion.equals(IpVersion.IPV4.getVersion())) {
                    for (ElasticIpRange.AllocationRange range: allocationRanges) {
                        long start = Ipv4AddrUtil.ipv4ToLong(range.getStart());
                        long end = Ipv4AddrUtil.ipv4ToLong(range.getEnd());
                        if (start > end) {
                            isInvalid = true;
                        }
                    }
                } else if (ipVersion.equals(IpVersion.IPV6.getVersion())) {
                    for (ElasticIpRange.AllocationRange range: allocationRanges) {
                        BigInteger start = Ipv6AddrUtil.ipv6ToBitInt(range.getStart());
                        BigInteger end = Ipv6AddrUtil.ipv6ToBitInt(range.getEnd());
                        if (start.compareTo(end) > 0) {
                            isInvalid = true;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                isInvalid = true;
            }
        }
        return isInvalid;
    }

    private void createElasticIpParameterProcess(String projectId, ElasticIpInfo elasticIpInfo) throws Exception {

        if (elasticIpInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        } else if (elasticIpInfo.getProjectId() == null) {
            elasticIpInfo.setProjectId(projectId);
        } else if (!projectId.equals(elasticIpInfo.getProjectId())) {
            throw new ElasticIpProjectIdConflictException();
        }

        if (StringUtils.isEmpty(elasticIpInfo.getRangeId())) {
            throw new ElasticIpNoRangeIdException();
        }

        if (elasticIpInfo.getElasticIpVersion() == null) {
            elasticIpInfo.setElasticIpVersion(IpVersion.IPV4.getVersion());
        } else if (this.isIpVersionInvalid(elasticIpInfo.getElasticIpVersion())) {
            throw new ElasticIpEipVersionException();
        }

        if (elasticIpInfo.getElasticIp() != null) {
            if (this.isIpAddressInvalid(elasticIpInfo.getElasticIp(), elasticIpInfo.getElasticIpVersion())) {
                throw new ElasticIpEipAddressException();
            }
        }

        if (StringUtils.isEmpty(elasticIpInfo.getPortId())) {
            if (elasticIpInfo.getPrivateIp()!= null || elasticIpInfo.getPrivateIpVersion() != null) {
                throw new ElasticIpNoPortIdException();
            }
        } else {
            if (elasticIpInfo.getPrivateIpVersion() == null) {
                elasticIpInfo.setPrivateIpVersion(IpVersion.IPV4.getVersion());
            } else if (this.isIpVersionInvalid(elasticIpInfo.getPrivateIpVersion())) {
                throw new ElasticIpPipVersionException();
            }

            if (elasticIpInfo.getPrivateIp() != null) {
                if (this.isIpAddressInvalid(elasticIpInfo.getPrivateIp(), elasticIpInfo.getPrivateIpVersion())) {
                    throw new ElasticIpPipAddressException();
                }
            }
        }

        if (elasticIpInfo.getName() == null) {
            elasticIpInfo.setName("");
        }

        if (elasticIpInfo.getDescription() == null) {
            elasticIpInfo.setDescription("");
        }
    }

    private void updateElasticIpParameterProcess(String projectId, String elasticIpId, ElasticIpInfo elasticIpInfo)
            throws Exception {

        if (elasticIpInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        } else if (elasticIpInfo.getProjectId() == null) {
            elasticIpInfo.setProjectId(projectId);
        } else if (!projectId.equals(elasticIpInfo.getProjectId())) {
            throw new ElasticIpProjectIdConflictException();
        }

        if (StringUtils.isEmpty(elasticIpId)) {
            throw new ElasticIpNoIdException();
        } else if (elasticIpInfo.getId() == null) {
            elasticIpInfo.setId(elasticIpId);
        } else if (!elasticIpId.equals(elasticIpInfo.getId())) {
            throw new ElasticIpIdConfilictException();
        }

        if (elasticIpInfo.getElasticIpVersion() != null &&
                this.isIpVersionInvalid(elasticIpInfo.getElasticIpVersion())) {
            throw new ElasticIpEipVersionException();
        }

        if (elasticIpInfo.getElasticIp() != null) {
            if (elasticIpInfo.getElasticIpVersion() == null) {
                elasticIpInfo.setElasticIpVersion(IpVersion.IPV4.getVersion());
            }
            if (this.isIpAddressInvalid(elasticIpInfo.getElasticIp(), elasticIpInfo.getElasticIpVersion())) {
                throw new ElasticIpEipAddressException();
            }
        }

        if (StringUtils.isEmpty(elasticIpInfo.getPortId())) {
            if (elasticIpInfo.getPrivateIp() != null || elasticIpInfo.getPrivateIpVersion() != null) {
                throw new ElasticIpNoPortIdException();
            }
        } else {
            if (elasticIpInfo.getPrivateIpVersion() != null &&
                    this.isIpVersionInvalid(elasticIpInfo.getPrivateIpVersion())) {
                throw new ElasticIpPipVersionException();
            }

            if (elasticIpInfo.getPrivateIp() != null) {
                if (elasticIpInfo.getPrivateIp() == null) {
                    elasticIpInfo.setPrivateIpVersion(IpVersion.IPV4.getVersion());
                }
                if (this.isIpAddressInvalid(elasticIpInfo.getPrivateIp(), elasticIpInfo.getPrivateIpVersion())) {
                    throw new ElasticIpPipAddressException();
                }
            }
        }
    }

    private void createElasticIpRangeParameterProcess(ElasticIpRangeInfo elasticIpRangeInfo) throws Exception {

        if (elasticIpRangeInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (elasticIpRangeInfo.getIpVersion() == null) {
            elasticIpRangeInfo.setIpVersion(IpVersion.IPV4.getVersion());
        } else if (this.isIpVersionInvalid(elasticIpRangeInfo.getIpVersion())) {
            throw new ElasticIpRangeVersionException();
        }

        List<ElasticIpRange.AllocationRange> allocationRanges = elasticIpRangeInfo.getAllocationRanges();
        if (allocationRanges != null) {
            if (this.isAllocationRangesInvalid(elasticIpRangeInfo.getIpVersion(), allocationRanges)) {
                throw new ElasticIpRangeBadRangesException();
            }
        } else {
            elasticIpRangeInfo.setAllocationRanges(new ArrayList<>());
        }

        if (elasticIpRangeInfo.getName() == null) {
            elasticIpRangeInfo.setName("");
        }

        if (elasticIpRangeInfo.getDescription() == null) {
            elasticIpRangeInfo.setDescription("");
        }
    }

    private void updateElasticIpRangeParameterProcess(String elasticipRangeId,
                                                      ElasticIpRangeInfo elasticIpRangeInfo) throws Exception {

        if (elasticIpRangeInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (StringUtils.isEmpty(elasticipRangeId)) {
            throw new ElasticIpRangeNoIdException();
        } else if (elasticIpRangeInfo.getId() == null) {
            elasticIpRangeInfo.setId(elasticipRangeId);
        } else if (!elasticipRangeId.equals(elasticIpRangeInfo.getId())) {
            throw new ElasticIpIdConfilictException();
        }

        if (elasticIpRangeInfo.getIpVersion() != null && this.isIpVersionInvalid(elasticIpRangeInfo.getIpVersion())) {
            throw new ElasticIpRangeVersionException();
        }

        List<ElasticIpRange.AllocationRange> allocationRanges = elasticIpRangeInfo.getAllocationRanges();
        if (allocationRanges != null) {
            if (this.isAllocationRangesInvalid(elasticIpRangeInfo.getIpVersion(), allocationRanges)) {
                throw new ElasticIpRangeBadRangesException();
            }
        }
    }

    @PostMapping("/project/{project_id}/elasticips")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ElasticIpInfoWrapper createElasticIp(@PathVariable("project_id") String projectId,
                                                @RequestBody ElasticIpInfoWrapper request) throws Exception {

        ElasticIpInfo requestInfo = request.getElasticip();

        this.createElasticIpParameterProcess(projectId, requestInfo);

        ElasticIpInfo result = elasticipService.createElasticIp(requestInfo);

        return new ElasticIpInfoWrapper(result);
    }

    @PutMapping("/project/{project_id}/elasticips/{elasticip_id}")
    @ResponseBody
    public ElasticIpInfoWrapper updateElasticIp(@PathVariable("project_id") String projectId,
                                                @PathVariable("elasticip_id") String elasticipId,
                                                @RequestBody ElasticIpInfoWrapper request) throws Exception {

        ElasticIpInfo requestInfo = request.getElasticip();

        this.updateElasticIpParameterProcess(projectId, elasticipId, requestInfo);

        ElasticIpInfo result = elasticipService.updateElasticIp(requestInfo);

        return new ElasticIpInfoWrapper(result);
    }

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

    @GetMapping(value = {"/project/{project_id}/elasticips/{elasticip_id}"})
    public ElasticIpInfoWrapper getElasticIp(@PathVariable String projectId, @PathVariable String elasticIpId)
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

    @GetMapping(value = {"/project/{project_id}/elasticips"})
    public ElasticIpsInfoWrapper getElasticIps(@PathVariable String projectId)
            throws Exception {

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        }

        List<ElasticIpInfo> eips = elasticipService.getElasticIps(projectId);

        return new ElasticIpsInfoWrapper(eips);
    }

    @PostMapping("/elasticip-ranges")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ElasticIpRangeInfoWrapper createElasticIpRange(@RequestBody ElasticIpRangeInfoWrapper request)
            throws Exception {

        ElasticIpRangeInfo requestInfo = request.getElasticIpRange();

        this.createElasticIpRangeParameterProcess(requestInfo);

        ElasticIpRangeInfo result = elasticIpRangeService.createElasticIpRange(requestInfo);

        return new ElasticIpRangeInfoWrapper(result);
    }

    @PutMapping("/elasticip-ranges/{elasticip_range_id}")
    @ResponseBody
    public ElasticIpRangeInfoWrapper updateElasticIp(@PathVariable("elasticip_range_id") String elasticipRangeId,
                                                @RequestBody ElasticIpRangeInfoWrapper request) throws Exception {

        ElasticIpRangeInfo requestInfo = request.getElasticIpRange();

        this.updateElasticIpRangeParameterProcess(elasticipRangeId, requestInfo);

        ElasticIpRangeInfo result = elasticIpRangeService.createElasticIpRange(requestInfo);

        return new ElasticIpRangeInfoWrapper(result);
    }

    @DeleteMapping("/elasticip-ranges/{elasticip_range_id}")
    public ResponseId deleteElasticIpRange(@PathVariable("elasticip_range_id") String elasticIpRangeId)
            throws Exception {

        if (StringUtils.isEmpty(elasticIpRangeId)) {
            throw new ElasticIpNoIdException();
        }

        elasticIpRangeService.deleteElasticIpRange(elasticIpRangeId);

        return new ResponseId(elasticIpRangeId);
    }

    @GetMapping(value = {"/elasticip-ranges/{elasticip_range_id}"})
    public ElasticIpRangeInfoWrapper getElasticIpRange(@PathVariable String elasticIpRangeId) throws Exception {

        if (StringUtils.isEmpty(elasticIpRangeId)) {
            throw new ElasticIpNoIdException();
        }

        ElasticIpRangeInfo eipRange = elasticIpRangeService.getElasticIpRange(elasticIpRangeId);

        return new ElasticIpRangeInfoWrapper(eipRange);
    }

    @GetMapping(value = {"/elasticip-ranges"})
    public ElasticIpRangesInfoWrapper getElasticIpRanges() throws Exception {

        List<ElasticIpRangeInfo> eipRanges = elasticIpRangeService.getElasticIpRanges();

        return new ElasticIpRangesInfoWrapper(eipRanges);
    }
}
