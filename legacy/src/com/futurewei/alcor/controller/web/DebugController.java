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

package com.futurewei.alcor.controller.web;

import com.futurewei.alcor.controller.db.repo.SubnetRedisRepository;
import com.futurewei.alcor.controller.db.repo.VpcRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class DebugController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private VpcRedisRepository vpcRedisRepository;

    @Autowired
    private SubnetRedisRepository subnetRedisRepository;

    @RequestMapping("/")
    public String index() {
        return "Greetings from Alioth!";
    }

    @RequestMapping(
            method = GET,
            value = "/debug")
    public DebugInfo getDebugInfo(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new DebugInfo(counter.incrementAndGet(),
                String.format(template, name));
    }

    @RequestMapping(
            method = GET,
            value = "/project/all/vpcs")
    public Map getVpcCountAndAllVpcStates() {
        Map result = new HashMap<String, Object>();
        Map dataItems = this.vpcRedisRepository.findAllItems();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }

    @RequestMapping(
            method = GET,
            value = "/project/all/vpccount")
    public Map getVpcCount() {
        Map result = new HashMap<String, Object>();
        Map dataItems = this.vpcRedisRepository.findAllItems();
        result.put("Count", dataItems.size());

        return result;
    }

    @RequestMapping(
            method = GET,
            value = "/project/all/subnets")
    public Map getAllSubnetStates() {
        Map result = new HashMap<String, Object>();
        Map dataItems = this.subnetRedisRepository.findAllItems();
        result.put("Count", dataItems.size());
        result.put("Subnets", dataItems);

        return result;
    }
}