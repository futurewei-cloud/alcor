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

package com.futurewei.alcor.macmanager.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNullException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.macmanager.dao.MacRedisRepository;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.entity.MacStateJson;
import com.futurewei.alcor.macmanager.service.MacAddressService;
import com.futurewei.alcor.macmanager.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
public class MacController {

    final String DELIMITER = "/";

    @Autowired
    private MacAddressService service;

    @RequestMapping(
            method = GET,
            value = {"/macs/{macaddress}", "/v4/macaddress/{macaddress}"})
    public MacStateJson getMacStateByMacAddress(@PathVariable String macaddress) throws Exception {

        MacState macState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
            macState = this.service.getMacStateByMacAddress(macaddress);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (macState == null) {
            //TODO: REST error code
            return new MacStateJson();
        }
        return new MacStateJson(macState);
    }

    @RequestMapping(
            method = POST,
            value = {"/macs", "/v4/macs"})
    @ResponseStatus(HttpStatus.CREATED)
    public MacStateJson createMacState(@RequestBody MacStateJson resource) throws Exception {
        MacState macState = null;

        try {
            MacState inMacState = resource.getMacState();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacState);
            macState = service.createMacState(inMacState);
            if (macState == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return new MacStateJson(macState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/macs/{macaddress}", "/v4/macs/{macaddress}"})
    public ResponseId deleteMacState(@PathVariable String macaddress) throws Exception {
        MacState macState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
            macState = service.releaseMac(macaddress);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        return new ResponseId(macState.getMacAddress());
    }
}
