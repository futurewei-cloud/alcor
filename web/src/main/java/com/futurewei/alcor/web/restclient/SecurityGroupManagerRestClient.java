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

import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;

public class SecurityGroupManagerRestClient extends  AbstractRestClient {
    @Value("${microservices.securitygroup.service.url:#{\"\"}}")
    private String securityGroupManagerUrl;

    public SecurityGroupJson getSecurityGroup(String projectId, String securityGroupId) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/security-groups/" + securityGroupId;
        SecurityGroupJson securityGroupWebJson = restTemplate.getForObject(url, SecurityGroupJson.class);
        if (securityGroupWebJson == null) {
            throw new Exception("Get security group failed");
        }

        return securityGroupWebJson;
    }

    public SecurityGroupJson getDefaultSecurityGroup(String projectId, String tenantId) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/security-groups/default/" + tenantId;
        SecurityGroupJson securityGroupWebJson = restTemplate.getForObject(url, SecurityGroupJson.class);
        if (securityGroupWebJson == null) {
            throw new Exception("Get default security group failed");
        }

        return securityGroupWebJson;
    }

    public PortSecurityGroupsJson bindSecurityGroups(String projectId, PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/bind-security-groups";
        HttpEntity<PortSecurityGroupsJson> request = new HttpEntity<>(portSecurityGroupsJson);
        PortSecurityGroupsJson result = restTemplate.postForObject(url, request, PortSecurityGroupsJson.class);
        if (result == null) {
            throw new Exception("Bind security groups failed");
        }

        return result;
    }

    public PortSecurityGroupsJson unbindSecurityGroups(String projectId, PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/unbind-security-groups";
        HttpEntity<PortSecurityGroupsJson> request = new HttpEntity<>(portSecurityGroupsJson);
        PortSecurityGroupsJson result = restTemplate.postForObject(url, request, PortSecurityGroupsJson.class);
        if (result == null) {
            throw new Exception("Unbind security groups failed");
        }

        return result;
    }
}
