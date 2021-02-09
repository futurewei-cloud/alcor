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
package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.dataplane.exception.GatewayInfoCacheExists;
import com.futurewei.alcor.dataplane.exception.GatewayInfoCacheNotFound;
import com.futurewei.alcor.dataplane.service.GatewayCacheService;
import com.futurewei.alcor.web.entity.gateway.GatewayInfoJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class GatewayCacheController {

    @Autowired
    private GatewayCacheService gatewayCacheService;

    @PostMapping({"/project/{project_id}/gatewayinfo", "/project/{project_id}/v4/gatewayinfo"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public ResponseId createGatewayInfo(@PathVariable("project_id") String projectId, @RequestBody GatewayInfoJson gatewayInfo) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        String response_id = gatewayCacheService.createGatewayInfo(gatewayInfo.getGatewayInfo());
        if (response_id == null) {
            throw new GatewayInfoCacheExists();
        }
        log.info("GatewayInfo created success, GatewayInfo is: {}", gatewayInfo);
        return new ResponseId(response_id);
    }

    @PutMapping({"/project/{project_id}/gatewayinfo/{resource_id}", "/project/{project_id}/v4/gatewayinfo/{resource_id}"})
    @DurationStatistics
    public ResponseId updateGatewayInfo(@PathVariable("project_id") String projectId, @PathVariable String resource_id, @RequestBody GatewayInfoJson gatewayInfo) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        String response_id = gatewayCacheService.updateGatewayInfo(resource_id, gatewayInfo.getGatewayInfo());
        if (response_id == null) {
            throw new GatewayInfoCacheNotFound();
        }
        log.info("GatewayInfo updated success, GatewayInfo is: {}", gatewayInfo);
        return new ResponseId(response_id);
    }

    @DeleteMapping({"/project/{project_id}/gatewayinfo/{resource_id}", "/project/{project_id}/v4/gatewayinfo/{resource_id}"})
    @DurationStatistics
    public void deleteGatewayInfo(@PathVariable("project_id") String projectId, @PathVariable String resource_id) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        gatewayCacheService.deleteGatewayInfo(resource_id);
        log.info("GatewayInfo deleted success, resource_id is: {}", resource_id);
    }
}
