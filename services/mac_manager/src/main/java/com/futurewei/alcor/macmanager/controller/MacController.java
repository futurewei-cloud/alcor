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
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.web.entity.mac.MacRangeJson;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.macmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class MacController {

    @Autowired
    private MacService service;

    @RequestMapping(
            method = GET,
            value = {"/macs/{macaddress}", "/v4/macs/{macaddress}"})
    public MacStateJson getMacStateByMacAddress(@PathVariable String macaddress) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacAddressInvalidException {

        MacState macState = null;
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);

        try {
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);
            macState = service.getMacStateByMacAddress(macaddress);
        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (MacAddressInvalidException e) {
            throw e;
        } catch (MacRepositoryTransactionErrorException e) {
            throw e;
        }

        if (macState == null) {
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
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacState);
            MacManagerRestPreconditionsUtil.verifyMacStateData(inMacState);
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
            method = POST,
            value = {"/macs/range/{rangeid}", "/v4/macs/range/{rangeid}"})
    @ResponseStatus(HttpStatus.CREATED)
    public MacStateJson createMacStateInRange(@PathVariable String rangeid, @RequestBody MacStateJson resource) throws Exception {
        MacState macState = null;
        try {
            MacState inMacState = resource.getMacState();
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacState);
            MacManagerRestPreconditionsUtil.verifyMacStateData(inMacState);
            macState = service.createMacStateInRange(rangeid, inMacState);
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
            method = PUT,
            value = {"/macs/{macaddress}", "/v4/macs/{macaddress}"})
    public MacStateJson updateMacState(@PathVariable String macaddress, @RequestBody MacStateJson resource) throws Exception {
        MacState macState = null;
        try {
            MacState inMacState = resource.getMacState();
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacState);
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);
            MacManagerRestPreconditionsUtil.verifyMacStateData(inMacState);
            macState = service.updateMacState(macaddress, inMacState);
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
    public String deleteMacAllocation(@PathVariable String macaddress) throws Exception {
        String macAddress = null;
        try {
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);
            macAddress = service.releaseMacState(macaddress);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        return "{mac_address: " + macAddress + "}";
    }

    @RequestMapping(
            method = GET,
            value = {"/macs/ranges/{rangeid}", "/v4/macs/ranges/{rangeid}"})
    public MacRangeJson getMacRangeByMacRangeId(@PathVariable String rangeid) throws Exception {

        MacRange macRange = null;
        try {
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(rangeid);
            macRange = service.getMacRangeByMacRangeId(rangeid);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (macRange == null) {
            //TODO: REST error code
            return new MacRangeJson();
        }
        return new MacRangeJson(macRange);
    }

    @RequestMapping(
            method = GET,
            value = {"/macs/ranges", "/v4/macs/ranges"})
    public Map<String, Collection<MacRange>> getAllMacRanges() throws Exception {
        Map<String, MacRange> macRanges;
        HashMap<String, Collection<MacRange>> map = new HashMap<String, Collection<MacRange>>();
        try {
            macRanges = service.getAllMacRanges();
        } catch (Exception e) {
            //TODO: REST error code
            throw new Exception(e);
        }
        if (macRanges == null) {
            //TODO: REST error code
            map.put("mac_ranges", null);
        } else
            map.put("mac_ranges", macRanges.values());
        return map;
    }

    @RequestMapping(
            method = POST,
            value = {"/macs/ranges", "/v4/macs/ranges"})
    @ResponseStatus(HttpStatus.CREATED)
    public MacRangeJson createMacRange(@RequestBody MacRangeJson resource) throws Exception {
        MacRange macRange = null;
        try {
            MacRange inMacRange = resource.getMacRange();
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacRange);
            MacManagerRestPreconditionsUtil.verifyMacRangeData(inMacRange);
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getFrom());
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getTo());
            macRange = service.createMacRange(inMacRange);
            if (macRange == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return new MacRangeJson(macRange);
    }

    @RequestMapping(
            method = PUT,
            value = {"/macs/ranges/{rangeid}", "/v4/macs/ranges/{rangeid}"})
    public MacRangeJson updateMacRange(@PathVariable String rangeid, @RequestBody MacRangeJson resource) throws Exception {
        MacRange macRange = null;
        try {
            MacRange inMacRange = resource.getMacRange();
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacRange);
            MacManagerRestPreconditionsUtil.verifyMacRangeData(inMacRange);
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getFrom());
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getTo());
            macRange = service.updateMacRange(inMacRange);
            if (macRange == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return new MacRangeJson(macRange);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/macs/ranges/{rangeid}", "/v4/macs/ranges/{rangeid}"})
    public ResponseId deleteMacRange(@PathVariable String rangeid) throws Exception {
        String rangeId = null;
        try {
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(rangeid);
            rangeId = service.deleteMacRange(rangeid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        return new ResponseId(rangeId);
    }
}
