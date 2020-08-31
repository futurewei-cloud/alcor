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

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.web.entity.port.*;
import com.futurewei.alcor.web.entity.router.RouterSubnetUpdateInfo;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import com.futurewei.alcor.web.rbac.aspect.Rbac;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.futurewei.alcor.common.constants.CommonConstants.QUERY_ATTR_HEADER;
import static com.futurewei.alcor.portmanager.util.RestParameterValidator.checkPort;
import static com.futurewei.alcor.portmanager.util.RestParameterValidator.checkRouterSubnetUpdateInfo;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class PortController {

    @Autowired
    PortService portService;

    @Autowired
    private HttpServletRequest request;

    @Value("${alcor.vif_type}")
    private String vifType;

    /**
     * Create a port, and call the interfaces of each micro-service according to the
     * configuration of the port to create various required resources for the port.
     * If any exception occurs in the added process, we need to roll back
     * the resource allocated from each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebJson Port configuration
     * @return PortWebJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @Rbac(resource ="port")
    @PostMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public PortWebJson createPort(@PathVariable("project_id") String projectId,
                                         @RequestBody PortWebJson portWebJson) throws Exception {
        PortEntity portEntity = portWebJson.getPortEntity();
        if (StringUtil.isNullOrEmpty(portEntity.getVpcId())) {
            throw new NetworkIdRequired();
        }

        checkPort(portEntity);

        if(portEntity.getBindingVifType() == null){
            portEntity.setBindingVifType(vifType);
        }

        return portService.createPort(projectId, portWebJson);
    }

    /**
     * Create multiple ports, and call the interfaces of each micro-service according to the
     * configuration of the port to create various required resources for all ports.
     * If an exception occurs during the creation of multiple ports, we need to roll back
     * the resource allocated from each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebBulkJson Multiple ports configuration
     * @return PortWebBulkJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @PostMapping({"/project/{project_id}/ports/bulk", "v4/{project_id}/ports/bulk"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public PortWebBulkJson createPortBulk(@PathVariable("project_id") String projectId,
                                             @RequestBody PortWebBulkJson portWebBulkJson) throws Exception {
        for (PortEntity portEntity: portWebBulkJson.getPortEntities()) {
            checkPort(portEntity);
        }

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
    @Rbac(resource ="port")
    @PutMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    @DurationStatistics
    public PortWebJson updatePort(@PathVariable("project_id") String projectId,
                                           @PathVariable("port_id") String portId,
                                           @RequestBody PortWebJson portWebJson) throws Exception {
        checkPort(portWebJson.getPortEntity());
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
    @DurationStatistics
    public PortWebBulkJson updatePortBulk(@PathVariable("project_id") String projectId,
                                         @RequestBody PortWebBulkJson portWebBulkJson) throws Exception {
        for (PortEntity portEntity: portWebBulkJson.getPortEntities()) {
            checkPort(portEntity);
        }

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
    @Rbac(resource ="port")
    @DeleteMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    @DurationStatistics
    public void deletePort(@PathVariable("project_id") String projectId,
                                @PathVariable("port_id") String portId) throws Exception {
        portService.deletePort(projectId, portId);
    }

    /**
     * Get the configuration of the port by port id
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @return PortWebJson
     * @throws Exception Db operation exception
     */
    @Rbac(resource ="port")
    @FieldFilter(type=PortEntity.class)
    @GetMapping({"/project/{project_id}/ports/{port_id}", "v4/{project_id}/ports/{port_id}"})
    @DurationStatistics
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
    @Rbac(resource ="port")
    @FieldFilter(type=PortEntity.class)
    @GetMapping({"/project/{project_id}/ports", "v4/{project_id}/ports"})
    @DurationStatistics
    public PortWebBulkJson listPort(@PathVariable("project_id") String projectId) throws Exception {

        Map<String, String[]> requestParams = (Map<String, String[]>)request.getAttribute(QUERY_ATTR_HEADER);
        requestParams = requestParams == null ? request.getParameterMap():requestParams;
        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(requestParams, PortEntity.class);

        List<PortWebJson> portWebJsonList = portService.listPort(projectId, queryParams);
        List<PortEntity> portsList = portWebJsonList.stream()
                .map(PortWebJson::getPortEntity)
                .collect(Collectors.toList());

        return new PortWebBulkJson(portsList);
    }

    /**
     * Update neighbor tables and send them to DPM when adding or deleting gateway port
     * @param projectId Project Id
     * @param routerSubnetUpdateInfo Router's latest subnet information
     * @return RouterSubnetUpdateInfo
     * @throws Exception Db operation exception
     */
    @Rbac(resource ="port")
    @PostMapping({"/project/{project_id}/update-l3-neighbors", "v4/{project_id}/update-l3-neighbors"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public RouterSubnetUpdateInfo updateL3Neighbors(@PathVariable("project_id") String projectId,
                                                    @RequestBody RouterSubnetUpdateInfo routerSubnetUpdateInfo) throws Exception {
        checkRouterSubnetUpdateInfo(routerSubnetUpdateInfo);
        return portService.updateL3Neighbors(projectId, routerSubnetUpdateInfo);
    }
}
