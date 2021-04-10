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
@ComponentScan(value = "com.futurewei.alcor.common.utils")
@ComponentScan(value = "com.futurewei.alcor.web.restclient")
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
        } else if (OperationType.CREATE.equals(opType)) {
            return dpmService.createNetworkConfiguration(networkConfiguration);
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
