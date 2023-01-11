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
package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.entity.ArionWing;
import com.futurewei.alcor.dataplane.service.SecurityGroupService;
import com.futurewei.alcor.dataplane.service.impl.ArionWingService;
import com.futurewei.alcor.web.entity.gateway.ArionWingInfo;
import com.futurewei.alcor.web.entity.route.RouteTable;
import com.futurewei.alcor.web.entity.route.RouteTableWebJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;


@Slf4j
@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class SecurityGroupController {

    @Autowired
    private SecurityGroupService securityGroupService;

    @PostMapping({"/securitygrouprules"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public String createSecurityGroupRule(@RequestBody SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        securityGroupService.updateSecurityGroupRule(securityGroupRuleJson);
        return "Success created";
    }

    @PostMapping({"/securitygrouprules/bulk"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public String createSecurityGroupRules(@RequestBody SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws Exception {
        securityGroupService.updateSecurityGroupRules(securityGroupRuleBulkJson);
        return "Success created";
    }

    @PutMapping({"/securitygrouprules"})
    @DurationStatistics
    public String updateSecurityGroupRules(@PathVariable SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        securityGroupService.updateSecurityGroupRule(securityGroupRuleJson);
        return "Success updated";
    }

    @DeleteMapping({"/securitygrouprules/{resource_id}"})
    @DurationStatistics
    public void deleteSecurityGroupRule(@RequestParam String resource_id) throws Exception {
        securityGroupService.deleteSecurityGroupRules(new ArrayList<>(){{add(resource_id);}});
    }

    @RequestMapping(
            method = GET,
            value = {"/securitygrouprules/{security_group_id}"})
    @DurationStatistics
    public Collection<SecurityGroupRule> getSecurityGroupRules(@PathVariable String security_group_id) throws Exception {
        return securityGroupService.getSecurityGroupRules(security_group_id);
    }
}
