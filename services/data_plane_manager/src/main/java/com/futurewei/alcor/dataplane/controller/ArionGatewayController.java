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
package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.entity.ArionWing;
import com.futurewei.alcor.dataplane.service.impl.ArionWingService;
import com.futurewei.alcor.web.entity.gateway.ArionWingInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


/*
This controller provide api for Arion master input group and Arion wing information.
It used for Arion wing Consistent Hash. Could be deprecated once Arion Master could be made self-contained
 */

@Slf4j
@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class ArionGatewayController {

    @Autowired
    private ArionWingService arionWingService;

    @PostMapping({"/arionwing"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public ArionWingInfo createGateway(@RequestBody ArionWingInfo arionWingInfo) throws Exception {
        arionWingService.createArionWing(new ArionWing(arionWingInfo.getGroup(), arionWingInfo.getIp(), arionWingInfo.getMac(), arionWingInfo.getVni()));
        return arionWingInfo;
    }

    @PutMapping({"/arionwing/{resource_id}"})
    @DurationStatistics
    public ArionWingInfo updateGateway(@PathVariable String resource_id, @RequestBody ArionWingInfo arionWingInfo) throws Exception {

        arionWingService.updateArionWing(new ArionWing(arionWingInfo.getGroup(), arionWingInfo.getIp(), arionWingInfo.getMac(), arionWingInfo.getVni()));
        return arionWingInfo;
    }

    @DeleteMapping({"/arionwing/{resource_id}"})
    @DurationStatistics
    public void deleteGateway(@PathVariable String resource_id) throws Exception {
        arionWingService.deleteArionWing(resource_id);
    }


    @PostMapping({"/ariongroup/{group_id}"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public String createGatewayGroup(@PathVariable String group_id) throws Exception {
        arionWingService.createArionWingGroup(group_id);
        return group_id;
    }

    @DeleteMapping({"/ariongroup/{group_id}"})
    @DurationStatistics
    public void deleteGatewayGroup(@PathVariable String group_id) throws Exception {
        arionWingService.deleteArionWingGroup(group_id);
    }
}
