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
package com.futurewei.alcor.networkaclmanager.controller;

import com.futurewei.alcor.networkaclmanager.service.NetworkAclRuleService;
import com.futurewei.alcor.web.entity.networkacl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.futurewei.alcor.networkaclmanager.util.RestParameterValidator.*;

@RestController
public class NetworkAclRuleController {
    @Autowired
    private NetworkAclRuleService networkAclRuleService;

    @PostMapping("/project/{project_id}/network-acl-rules")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkAclRuleWebJson createNetworkAclRule(@PathVariable("project_id") String projectId,
                                                      @RequestBody NetworkAclRuleWebJson networkAclRuleWebJson) throws Exception {
        NetworkAclRuleEntity networkAclRuleEntity = networkAclRuleWebJson.getNetworkAclRuleEntity();
        checkNetworkAclRule(networkAclRuleEntity);
        networkAclRuleWebJson.setNetworkAclRuleEntity(networkAclRuleService.createNetworkAclRule(networkAclRuleEntity));
        return networkAclRuleWebJson;
    }

    @PostMapping("/project/{project_id}/network-acl-rules/bulk")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkAclRuleBulkWebJson createNetworkAclRuleBulk(@PathVariable("project_id") String projectId,
                                                              @RequestBody NetworkAclRuleBulkWebJson networkAclRuleBulkWebJson) throws Exception {
        checkProjectId(projectId);
        List<NetworkAclRuleEntity> networkAclRuleEntities =
                networkAclRuleBulkWebJson.getNetworkAclRuleEntities();
        for (NetworkAclRuleEntity networkAclRuleEntity: networkAclRuleEntities) {
            checkNetworkAclRule(networkAclRuleEntity);
        }

        networkAclRuleBulkWebJson.setNetworkAclRuleEntities(
                networkAclRuleService.createNetworkAclRuleBulk(networkAclRuleEntities));
        return networkAclRuleBulkWebJson;
    }

    @PutMapping("/project/{project_id}/network-acl-rules/{network_acl_rule_id}")
    public NetworkAclRuleWebJson updateNetworkAclRule(@PathVariable("project_id") String projectId,
                                              @PathVariable("network_acl_rule_id") String networkAclRuleId,
                                              @RequestBody NetworkAclRuleWebJson networkAclRuleWebJson) throws Exception {
        NetworkAclRuleEntity networkAclRuleEntity = networkAclRuleWebJson.getNetworkAclRuleEntity();
        checkNetworkAclRule(networkAclRuleEntity);
        networkAclRuleWebJson.setNetworkAclRuleEntity(networkAclRuleService.updateNetworkAclRule(networkAclRuleId, networkAclRuleEntity));
        return networkAclRuleWebJson;
    }

    @PutMapping("/project/{project_id}/network-acl-rules/bulk")
    public NetworkAclRuleBulkWebJson updateNetworkAclBulk(@PathVariable("project_id") String projectId,
                                                      @RequestBody NetworkAclRuleBulkWebJson networkAclRuleBulkWebJson) throws Exception {
        checkProjectId(projectId);
        List<NetworkAclRuleEntity> networkAclRuleEntities =
                networkAclRuleBulkWebJson.getNetworkAclRuleEntities();
        for (NetworkAclRuleEntity networkAclRuleEntity: networkAclRuleEntities) {
            checkNetworkAclRule(networkAclRuleEntity);
        }

        networkAclRuleBulkWebJson.setNetworkAclRuleEntities(
                networkAclRuleService.updateNetworkAclRuleBulk(networkAclRuleEntities));
        return networkAclRuleBulkWebJson;
    }

    @DeleteMapping("/project/{project_id}/network-acl-rules/{network_acl_rule_id}")
    public void deleteNetworkAclRule(@PathVariable("project_id") String projectId,
                                 @PathVariable("network_acl_rule_id") String networkAclRuleId) throws Exception {
        networkAclRuleService.deleteNetworkAclRule(networkAclRuleId);
    }

    @GetMapping("/project/{project_id}/network-acl-rules/{network_acl_rule_id}")
    public NetworkAclRuleWebJson getNetworkAclRule(@PathVariable("project_id") String projectId,
                                           @PathVariable("network_acl_rule_id") String networkAclRuleId) throws Exception {
        return new NetworkAclRuleWebJson(networkAclRuleService.getNetworkAclRule(networkAclRuleId));
    }

    @GetMapping("/project/{project_id}/network-acl-rules")
    public List<NetworkAclRuleEntity> listNetworkAclRule(@PathVariable("project_id") String projectId) throws Exception {
        return networkAclRuleService.listNetworkAclRule();
    }
}
