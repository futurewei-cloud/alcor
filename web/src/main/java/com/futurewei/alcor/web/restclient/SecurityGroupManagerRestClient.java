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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupsJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;

import java.util.List;

@Configuration
public class SecurityGroupManagerRestClient extends  AbstractRestClient {
    @Value("${microservices.sg.service.url:#{\"\"}}")
    private String securityGroupManagerUrl;

    @DurationStatistics
    public SecurityGroupJson getSecurityGroup(String projectId, String securityGroupId) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/security-groups/" + securityGroupId;
        SecurityGroupJson securityGroupWebJson = restTemplate.getForObject(url, SecurityGroupJson.class);
        if (securityGroupWebJson == null) {
            throw new Exception("Get security group failed");
        }

        return securityGroupWebJson;
    }

    @DurationStatistics
    public SecurityGroupsJson getSecurityGroupBulk(String projectId, List<String> securityGroupIds) throws Exception {
        String queryParameter = buildQueryParameter("id", securityGroupIds);
        String url = securityGroupManagerUrl + "/project/" + projectId + "/security-groups?" + queryParameter;
        SecurityGroupsJson securityGroupsJson = restTemplate.getForObject(url, SecurityGroupsJson.class);
        if (securityGroupsJson == null) {
            throw new Exception("Get security groups failed");
        }

        return securityGroupsJson;
    }

    @DurationStatistics
    public SecurityGroupJson getDefaultSecurityGroup(String projectId, String tenantId) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/security-groups/default/" + tenantId;
        SecurityGroupJson securityGroupWebJson = restTemplate.getForObject(url, SecurityGroupJson.class);
        if (securityGroupWebJson == null) {
            throw new Exception("Get default security group failed");
        }

        return securityGroupWebJson;
    }

    @DurationStatistics
    public PortSecurityGroupsJson bindSecurityGroup(String projectId, PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/bind-security-groups";
        HttpEntity<PortSecurityGroupsJson> request = new HttpEntity<>(portSecurityGroupsJson);
        PortSecurityGroupsJson result = restTemplate.postForObject(url, request, PortSecurityGroupsJson.class);
        if (result == null) {
            throw new Exception("Bind security groups failed");
        }

        return result;
    }

    @DurationStatistics
    public PortSecurityGroupsJson unbindSecurityGroup(String projectId, PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/unbind-security-groups";
        HttpEntity<PortSecurityGroupsJson> request = new HttpEntity<>(portSecurityGroupsJson);
        PortSecurityGroupsJson result = restTemplate.postForObject(url, request, PortSecurityGroupsJson.class);
        if (result == null) {
            throw new Exception("Unbind security groups failed");
        }

        return result;
    }
}
