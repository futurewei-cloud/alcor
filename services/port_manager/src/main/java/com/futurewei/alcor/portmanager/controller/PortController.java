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


import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.web.entity.port.PortStateJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class PortController {
    @Autowired
    PortService portService;

    /**
     * Create a port, and call the interfaces of each micro-service according to the
     * configuration of the port to create various required resources for the port.
     * If any exception occurs in the added process, we need to roll back
     * the resource allocated from each micro-service.
     * @param projectId Project the port belongs to
     * @param portStateJson Port configuration
     * @return PortStateJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PostMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PortStateJson createPortState(@PathVariable("project_id") String projectId,
                                         @RequestBody PortStateJson portStateJson) throws Exception {
        return portService.createPortState(projectId, portStateJson);
    }

    /**
     * Update the configuration information of port. Resources requested from various
     * micro-services may need to be updated according to the new configuration of port.
     * If any exception occurs in the updated process, we need to roll back
     * the resource added or deleted operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @param portStateJson The new configuration of port
     * @return The new configuration of port
     * @throws Exception Various exceptions that may occur during the update process
     */
    @PutMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public PortStateJson updatePortState(@PathVariable("project_id") String projectId,
                                           @PathVariable("port_id") String portId,
                                           @RequestBody PortStateJson portStateJson) throws Exception {
        return portService.updatePortState(projectId, portId, portStateJson);
    }

    /**
     * Delete the port corresponding to portId from the repository and delete
     * the resources requested by the port from each micro-service.
     * If any exception occurs in the deleted process, we need to roll back
     * the resource deletion operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @throws Exception Various exceptions that may occur during the delete process
     */
    @DeleteMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public void deletePortState(@PathVariable("project_id") String projectId,
                                @PathVariable("port_id") String portId) throws Exception {
        portService.deletePortState(projectId, portId);
    }

    /**
     * Get the configuration of the port by port id
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @return PortStateJson
     * @throws Exception Db operation exception
     */
    @GetMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public PortStateJson getPortState(@PathVariable("project_id") String projectId,
                                           @PathVariable("port_id") String portId) throws Exception {
        return portService.getPortState(projectId, portId);
    }

    /**
     * Get all port information
     * @param projectId Project the port belongs to
     * @return A list of port information
     * @throws Exception Db operation exception
     */
    @GetMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    public List<PortStateJson> listPortState(@PathVariable("project_id") String projectId) throws Exception {
        return portService.listPortState(projectId);
    }
}
