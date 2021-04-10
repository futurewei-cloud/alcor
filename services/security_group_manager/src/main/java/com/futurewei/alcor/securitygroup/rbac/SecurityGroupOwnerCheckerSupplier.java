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

package com.futurewei.alcor.securitygroup.rbac;

import com.futurewei.alcor.common.rbac.OwnerChecker;
import com.futurewei.alcor.securitygroup.service.SecurityGroupRuleService;
import com.futurewei.alcor.securitygroup.service.SecurityGroupService;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import com.futurewei.alcor.web.rbac.aspect.OwnerCheckerSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class SecurityGroupOwnerCheckerSupplier implements OwnerCheckerSupplier {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupOwnerCheckerSupplier.class);
    private static final String SECURITY_GROUP_RESOURCE_RULE_NAME = "security-group-rules";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SecurityGroupService securityGroupService;

    @Autowired
    private SecurityGroupRuleService securityGroupRuleService;

    @Override
    public OwnerChecker getOwnerChecker() {
        return () -> {
            String path = request.getServletPath();
            // only check foreign api
            if (!path.startsWith("/project")) {
                return true;
            }
            String[] pathInfo = path.split("/");
            if (pathInfo.length < 5) {
                return true;
            }
            String projectId = pathInfo[2];
            String resourceId = pathInfo[4];
            String resourceName = pathInfo[3];
            try {
                if (resourceName.contains(SECURITY_GROUP_RESOURCE_RULE_NAME)) {
                    SecurityGroupRuleJson securityGroupRuleJson = securityGroupRuleService.getSecurityGroupRule(resourceId);
                    return projectId.equals(securityGroupRuleJson.getSecurityGroupRule().getProjectId());
                }
                SecurityGroupJson securityGroupJson = securityGroupService.getSecurityGroup(resourceId);
                return projectId.equals(securityGroupJson.getSecurityGroup().getProjectId());
            } catch (Exception e) {
                LOG.error("get security group from db error {}", e.getMessage());
                return false;
            }
        };
    }
}
