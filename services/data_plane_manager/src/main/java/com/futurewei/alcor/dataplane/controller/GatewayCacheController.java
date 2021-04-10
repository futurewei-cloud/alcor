/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
