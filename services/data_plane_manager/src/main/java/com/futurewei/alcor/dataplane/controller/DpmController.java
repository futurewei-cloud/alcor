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

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.exception.OperationTypeInvalid;
import com.futurewei.alcor.dataplane.service.DpmService;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.futurewei.alcor.dataplane.utils.RestParameterValidator.checkNetworkConfiguration;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class DpmController {

    @Autowired
    private DpmService dpmService;

    @PostMapping({"/network-configuration", "v4/network-configuration"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public InternalDPMResultList createNetworkConfiguration(@RequestBody NetworkConfiguration networkConfiguration) throws Exception {
        checkNetworkConfiguration(networkConfiguration);
        return dpmService.createNetworkConfiguration(networkConfiguration);
    }

    @PutMapping({"/network-configuration", "v4/network-configuration"})
    @DurationStatistics
    public InternalDPMResultList updateNetworkConfiguration(@RequestBody NetworkConfiguration networkConfiguration) throws Exception {
        checkNetworkConfiguration(networkConfiguration);
        OperationType opType = networkConfiguration.getOpType();
        if (OperationType.UPDATE.equals(opType)) {
            return dpmService.updateNetworkConfiguration(networkConfiguration);
        } else if (OperationType.DELETE.equals(opType)) {
            return this.deleteNetworkConfiguration(networkConfiguration);
        }

        throw new OperationTypeInvalid();
    }

    @DeleteMapping({"/network-configuration", "v4/network-configuration"})
    @DurationStatistics
    public InternalDPMResultList deleteNetworkConfiguration(@RequestBody NetworkConfiguration networkConfiguration) throws Exception {
        checkNetworkConfiguration(networkConfiguration);
        return dpmService.deleteNetworkConfiguration(networkConfiguration);
    }
}
