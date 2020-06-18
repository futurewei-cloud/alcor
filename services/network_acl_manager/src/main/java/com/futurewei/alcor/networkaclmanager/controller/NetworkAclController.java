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

import com.futurewei.alcor.networkaclmanager.service.NetworkAclService;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclBulkWebJson;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclEntity;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.futurewei.alcor.networkaclmanager.util.RestParameterValidator.checkNetworkAcl;
import static com.futurewei.alcor.networkaclmanager.util.RestParameterValidator.checkProjectId;

@RestController
public class NetworkAclController {
    @Autowired
    private NetworkAclService networkAclService;

    @PostMapping("/project/{project_id}/network-acls")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkAclWebJson createNetworkAcl(@PathVariable("project_id") String projectId,
                                              @RequestBody NetworkAclWebJson networkAclWebJson) throws Exception {
        checkProjectId(projectId);
        NetworkAclEntity networkAclEntity = networkAclWebJson.getNetworkAclEntity();
        checkNetworkAcl(networkAclEntity);
        networkAclWebJson.setNetworkAclEntity(
                networkAclService.createNetworkAcl(projectId, networkAclEntity));
        return networkAclWebJson;
    }

    @PostMapping("/project/{project_id}/network-acls/bulk")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkAclBulkWebJson createNetworkAclBulk(@PathVariable("project_id") String projectId,
                                                      @RequestBody NetworkAclBulkWebJson networkAclBulkWebJson) throws Exception {
        checkProjectId(projectId);
        List<NetworkAclEntity> networkAclEntities = networkAclBulkWebJson.getNetworkAclEntities();
        for (NetworkAclEntity networkAclEntity: networkAclEntities) {
            checkNetworkAcl(networkAclEntity);
        }

        networkAclBulkWebJson.setNetworkAclEntities(
                networkAclService.createNetworkAclBulk(projectId, networkAclEntities));
        return networkAclBulkWebJson;
    }

    @PutMapping("/project/{project_id}/network-acls/{network_acl_id}")
    public NetworkAclWebJson updateNetworkAcl(@PathVariable("project_id") String projectId,
                                  @PathVariable("network_acl_id") String networkAclId,
                                  @RequestBody NetworkAclWebJson networkAclWebJson) throws Exception {
        checkProjectId(projectId);
        NetworkAclEntity networkAclEntity = networkAclWebJson.getNetworkAclEntity();
        checkNetworkAcl(networkAclEntity);
        networkAclWebJson.setNetworkAclEntity(
                networkAclService.updateNetworkAcl(projectId, networkAclId, networkAclEntity));
        return networkAclWebJson;
    }

    @PutMapping("/project/{project_id}/network-acls/bulk")
    public NetworkAclBulkWebJson updateNetworkAclBulk(@PathVariable("project_id") String projectId,
                                              @RequestBody NetworkAclBulkWebJson networkAclBulkWebJson) throws Exception {
        checkProjectId(projectId);
        List<NetworkAclEntity> networkAclEntities = networkAclBulkWebJson.getNetworkAclEntities();
        for (NetworkAclEntity networkAclEntity: networkAclEntities) {
            checkNetworkAcl(networkAclEntity);
        }

        networkAclBulkWebJson.setNetworkAclEntities(
                networkAclService.updateNetworkAclBulk(projectId, networkAclEntities));
        return networkAclBulkWebJson;
    }

    @DeleteMapping("/project/{project_id}/network-acls/{network_acl_id}")
    public void deleteNetworkAcl(@PathVariable("project_id") String projectId,
                           @PathVariable("network_acl_id") String networkAclId) throws Exception {
        checkProjectId(projectId);
        networkAclService.deleteNetworkAcl(networkAclId);
    }

    @GetMapping("/project/{project_id}/network-acls/{network_acl_id}")
    public NetworkAclWebJson getNetworkAcl(@PathVariable("project_id") String projectId,
                               @PathVariable("network_acl_id") String networkAclId) throws Exception {
        checkProjectId(projectId);
        return new NetworkAclWebJson(networkAclService.getNetworkAcl(networkAclId));
    }

    @GetMapping("/project/{project_id}/network-acls")
    public List<NetworkAclEntity> listNetworkAcl(@PathVariable("project_id") String projectId) throws Exception {
        checkProjectId(projectId);
        return networkAclService.listNetworkAcl();
    }
}
