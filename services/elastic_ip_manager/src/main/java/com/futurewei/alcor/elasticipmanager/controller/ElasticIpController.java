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
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpParameterException;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpRangeService;
import com.futurewei.alcor.privateipmanager.service.ElasticIpService;
import com.futurewei.alcor.web.entity.elasticip.*;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
public class ElasticIpController {
    @Autowired
    ElasticIpService elasticipService;

    @Autowired
    ElasticIpRangeService elasticIpRangeService;

    private boolean checkIpAddress(String ipAddress, Integer ipVersion) {
        boolean isValid = false;
        if (ipVersion == IpVersion.IPV4.getVersion()) {
            isValid = Ipv4AddrUtil.formatCheck(ipAddress);
        } else if (ipVersion == IpVersion.IPV6.getVersion()) {
            isValid = Ipv6AddrUtil.formatCheck(ipAddress);
        }

        return isValid;
    }

    private void createElasticIpParameterProcess(String projectId, ElasticIpInfo elasticIpInfo)
            throws ElasticIpParameterException {

        boolean bValid = true;

        if (elasticIpInfo.getProjectId() == null) {
            elasticIpInfo.setProjectId(projectId);
        } else if (!projectId.equals(elasticIpInfo.getProjectId())) {
            bValid = false;
        }

        if (elasticIpInfo.getRangeId() == null) {
            bValid = false;
        }

        if (elasticIpInfo.getElasticIpVersion() == null) {
            elasticIpInfo.setElasticIpVersion(4);
        } else if (elasticIpInfo.getElasticIpVersion() != IpVersion.IPV4.getVersion() &&
                elasticIpInfo.getElasticIpVersion() != IpVersion.IPV6.getVersion()) {
            bValid = false;
        }

        if (elasticIpInfo.getElasticIp() != null &&
                this.checkIpAddress(elasticIpInfo.getElasticIp(), elasticIpInfo.getElasticIpVersion())) {
            bValid = false;
        }

        if (elasticIpInfo.getPortId() == null &&
                (elasticIpInfo.getPrivateIp() != null || elasticIpInfo.getPrivateIpVersion() != null)) {
            bValid = false;
        }

        if (elasticIpInfo.getPrivateIp() != null) {
            if (elasticIpInfo.getPrivateIpVersion() == null) {
                elasticIpInfo.setPrivateIpVersion(IpVersion.IPV4.getVersion());
            }
            if (this.checkIpAddress(elasticIpInfo.getPrivateIp(), elasticIpInfo.getPrivateIpVersion())) {
                bValid = false;
            }
        }

        if (!bValid) {
            throw new ElasticIpParameterException();
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

    @DeleteMapping("/project/{project_id}/elasticips/{elasticip_id}")
    public ResponseId deleteElasticIp(@PathVariable("project_id") String projectId,
                                      @PathVariable("elasticip_id") String elasticIpId) throws Exception {

        elasticipService.deleteElasticIp(projectId, elasticIpId);

        return new ResponseId(elasticIpId);
    }
}
