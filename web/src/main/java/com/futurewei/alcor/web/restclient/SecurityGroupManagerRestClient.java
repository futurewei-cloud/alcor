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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.common.http.RestTemplateConfig;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupsJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;

import java.util.List;

@Configuration
@Import(RestTemplateConfig.class)
public class SecurityGroupManagerRestClient extends AbstractRestClient {
    @Value("${microservices.sg.service.url:#{\"\"}}")
    private String securityGroupManagerUrl;

    public SecurityGroupManagerRestClient(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

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
    public PortBindingSecurityGroupsJson bindSecurityGroup(String projectId, PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/bind-security-groups";
        HttpEntity<PortBindingSecurityGroupsJson> request = new HttpEntity<>(portBindingSecurityGroupsJson);
        PortBindingSecurityGroupsJson result = restTemplate.postForObject(url, request, PortBindingSecurityGroupsJson.class);
        if (result == null) {
            throw new Exception("Bind security groups failed");
        }

        return result;
    }

    @DurationStatistics
    public PortBindingSecurityGroupsJson unbindSecurityGroup(String projectId, PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception {
        String url = securityGroupManagerUrl + "/project/" + projectId + "/unbind-security-groups";
        HttpEntity<PortBindingSecurityGroupsJson> request = new HttpEntity<>(portBindingSecurityGroupsJson);
        PortBindingSecurityGroupsJson result = restTemplate.postForObject(url, request, PortBindingSecurityGroupsJson.class);
        if (result == null) {
            throw new Exception("Unbind security groups failed");
        }

        return result;
    }
}
