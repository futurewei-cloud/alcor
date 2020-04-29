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
package com.futurewei.alcor.portmanager.controller;

import com.futurewei.alcor.common.entity.PortStateJson;
import com.futurewei.alcor.portmanager.service.PortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class PortController {
    @Autowired
    PortService portService;

    @PostMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PortStateJson createPortState(@PathVariable("project_id") String projectId,
                                         @RequestBody PortStateJson portStateJson) throws Exception {
        return portService.createPortState(projectId, portStateJson);
    }

    @PutMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public PortStateJson updatePortState(@PathVariable("project_id") String projectId,
                                           @PathVariable("port_id") String portId,
                                           @RequestBody PortStateJson portStateJson) throws Exception {
        return portService.updatePortState(projectId, portId, portStateJson);
    }

    @DeleteMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public void deletePortState(@PathVariable("project_id") String projectId,
                                @PathVariable("port_id") String portId) throws Exception {
        portService.deletePortState(projectId, portId);
    }

    @GetMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public PortStateJson getPortState(@PathVariable("project_id") String projectId,
                                           @PathVariable("port_id") String portId) throws Exception {
        return portService.getPortState(projectId, portId);
    }

    @GetMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    public List<PortStateJson> listPortState(@PathVariable("project_id") String projectId) throws Exception {
        return portService.listPortState(projectId);
    }
}
