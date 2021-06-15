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
package com.futurewei.alcor.securitygroup.service;

import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupsJson;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface SecurityGroupService {
    SecurityGroupJson createSecurityGroup(SecurityGroupJson securityGroupJson) throws Exception;

    SecurityGroupBulkJson createSecurityGroupBulk(String tenantId, String projectId, SecurityGroupBulkJson securityGroupBulkJson) throws Exception;

    SecurityGroupJson updateSecurityGroup(String securityGroupId, SecurityGroupJson securityGroupJson) throws Exception;

    void deleteSecurityGroup(String securityGroupId) throws Exception;

    SecurityGroupJson getSecurityGroup(String securityGroupId) throws Exception;

    SecurityGroupJson getDefaultSecurityGroup(String projectId, String tenantId) throws Exception;

    SecurityGroupsJson listSecurityGroup(Map<String, Object[]> queryParams) throws Exception;

    PortBindingSecurityGroupsJson bindSecurityGroups(PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception;

    PortBindingSecurityGroupsJson unbindSecurityGroups(PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception;
}
