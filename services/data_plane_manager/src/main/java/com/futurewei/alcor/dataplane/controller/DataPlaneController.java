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
import com.futurewei.alcor.dataplane.service.DataPlaneService;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.futurewei.alcor.dataplane.utils.RestParameterValidator.checkNetworkConfiguration;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class DataPlaneController {

    @Autowired
    private DataPlaneService dataPlaneService;

    @PostMapping({"/network-configuration", "v4/network-configuration"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public InternalDPMResultList createNetworkConfiguration(@RequestBody NetworkConfiguration networkConfiguration) throws Exception {
        checkNetworkConfiguration(networkConfiguration);
        return dataPlaneService.createNetworkConfiguration(networkConfiguration);
    }

    @PutMapping({"/network-configuration", "v4/network-configuration"})
    @DurationStatistics
    public InternalDPMResultList updateNetworkConfiguration(@RequestBody NetworkConfiguration networkConfiguration) throws Exception {
        checkNetworkConfiguration(networkConfiguration);
        return dataPlaneService.updateNetworkConfiguration(networkConfiguration);
    }

    @DeleteMapping({"/network-configuration", "v4/network-configuration"})
    @DurationStatistics
    public InternalDPMResultList deleteNetworkConfiguration(@RequestBody NetworkConfiguration networkConfiguration) throws Exception {
        checkNetworkConfiguration(networkConfiguration);
        return dataPlaneService.deleteNetworkConfiguration(networkConfiguration);
    }
}
