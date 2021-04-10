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
