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
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
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
     * @param portWebJson Port configuration
     * @return PortStateJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PostMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PortWebJson createPort(@PathVariable("project_id") String projectId,
                                       @RequestBody PortWebJson portWebJson) throws Exception {
        return portService.createPort(projectId, portWebJson);
    }

    /**
     * Create multiple ports, and call the interfaces of each micro-service according to the
     * configuration of the port to create various required resources for all ports.
     * If an exception occurs during the creation of multiple ports, we need to roll back
     * the resource allocated from each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebBulkJson Multiple ports configuration
     * @return PortStateBulkJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PostMapping({"/project/{project_id}/ports/bulk", "v4/{project_id}/ports/bulk"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PortWebBulkJson createPortBulk(@PathVariable("project_id") String projectId,
                                             @RequestBody PortWebBulkJson portWebBulkJson) throws Exception {
        return portService.createPortBulk(projectId, portWebBulkJson);
    }

    /**
     * Update the configuration information of port. Resources requested from various
     * micro-services may need to be updated according to the new configuration of port.
     * If any exception occurs in the updated process, we need to roll back
     * the resource added or deleted operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @param portWebJson The new configuration of port
     * @return The new configuration of port
     * @throws Exception Various exceptions that may occur during the update process
     */
    @PutMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public PortWebJson updatePort(@PathVariable("project_id") String projectId,
                                       @PathVariable("port_id") String portId,
                                       @RequestBody PortWebJson portWebJson) throws Exception {
        return portService.updatePort(projectId, portId, portWebJson);
    }

    /**
     * Update the configuration information of ports. Resources requested from various
     * micro-services may need to be updated according to the new configuration of ports.
     * If an exception occurs during the update, we need to roll back
     * the resource added or deleted operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebBulkJson The new configuration of ports
     * @return The new configuration of ports
     * @throws Exception Various exceptions that may occur during the update process
     */
    @PutMapping({"/project/{project_id}/ports/bulk", "v4/{project_id}/ports/bulk"})
    public PortWebBulkJson updatePortBulk(@PathVariable("project_id") String projectId,
                                         @RequestBody PortWebBulkJson portWebBulkJson) throws Exception {
        return portService.updatePortBulk(projectId, portWebBulkJson);
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
    public void deletePort(@PathVariable("project_id") String projectId,
                                @PathVariable("port_id") String portId) throws Exception {
        portService.deletePort(projectId, portId);
    }

    /**
     * Get the configuration of the port by port id
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @return PortStateJson
     * @throws Exception Db operation exception
     */
    @GetMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    public PortWebJson getPort(@PathVariable("project_id") String projectId,
                                    @PathVariable("port_id") String portId) throws Exception {
        return portService.getPort(projectId, portId);
    }

    /**
     * Get all port information
     * @param projectId Project the port belongs to
     * @return A list of port information
     * @throws Exception Db operation exception
     */
    @GetMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    public List<PortWebJson> listPort(@PathVariable("project_id") String projectId) throws Exception {
        return portService.listPort(projectId);
    }
}
