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

package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.vpcmanager.dao.VpcRepository;
import com.futurewei.alcor.vpcmanager.entity.DebugInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class DebugController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private VpcRepository vpcRepository;


    @RequestMapping("/")
    public String index() {
        return "Greetings from Alcor Vpc Manager!";
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
    public Map getVpcCountAndAllVpcStates() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = this.vpcRepository.findAllItems();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }

    @RequestMapping(
            method = GET,
            value = "/project/all/vpccount")
    public Map getVpcCount() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = this.vpcRepository.findAllItems();
        result.put("Count", dataItems.size());

        return result;
    }

}