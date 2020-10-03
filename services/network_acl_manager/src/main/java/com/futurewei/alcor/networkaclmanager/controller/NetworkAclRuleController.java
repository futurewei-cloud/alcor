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
