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
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.web.entity.mac.*;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.macmanager.exception.MacAddressInvalidException;
import com.futurewei.alcor.macmanager.exception.MacRepositoryTransactionErrorException;
import com.futurewei.alcor.macmanager.utils.MacManagerRestPreconditionsUtil;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class MacController {

    @Autowired
    private MacService service;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(
            method = GET,
            value = {"/macs/{macaddress}", "/v4/macs/{macaddress}"})
    @DurationStatistics
    public MacStateJson getMacStateByMacAddress(@PathVariable String macaddress) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacAddressInvalidException {

        MacState macState = null;
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);

        try {
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
            MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);
            macState = service.getMacStateByMacAddress(macaddress);
        } catch (ParameterNullOrEmptyException | MacRepositoryTransactionErrorException | MacAddressInvalidException e) {
            throw e;
        }

        return new MacStateJson(macState);
    }

    @RequestMapping(
            method = POST,
            value = {"/macs", "/v4/macs"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public MacStateJson createMacState(@RequestBody MacStateJson resource) throws Exception {
        MacState inMacState = resource.getMacState();
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacState);
        MacManagerRestPreconditionsUtil.verifyMacStateData(inMacState);
        MacState macState = service.createMacState(inMacState);
        return new MacStateJson(macState);
    }

    @RequestMapping(
            method = POST,
            value = {"/macs/bulk", "/v4/macs/bulk"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public MacStateBulkJson createMacStateBulk(@RequestBody MacStateBulkJson resource) throws Exception {
        for(MacState macState: resource.getMacStates()){
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macState);
            MacManagerRestPreconditionsUtil.verifyMacStateData(macState);
        }

        return service.createMacStateBulk(resource);
    }

    @RequestMapping(
            method = POST,
            value = {"/macs/range/{rangeId}/bulk", "/v4/macs/range/{rangeId}/bulk"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public MacStateBulkJson createMacStateBulkInRange(@PathVariable String rangeId, @RequestBody MacStateBulkJson resource) throws Exception {
        for(MacState macState: resource.getMacStates()){
            MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macState);
            MacManagerRestPreconditionsUtil.verifyMacStateData(macState);
        }

        return service.createMacStateBulkInRange(rangeId, resource);
    }

    @RequestMapping(
            method = POST,
            value = {"/macs/range/{rangeId}", "/v4/macs/range/{rangeId}"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public MacStateJson createMacStateInRange(@PathVariable String rangeId, @RequestBody MacStateJson resource) throws Exception {
        MacState inMacState = resource.getMacState();
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacState);
        MacManagerRestPreconditionsUtil.verifyMacStateData(inMacState);
        MacState macState = service.createMacStateInRange(rangeId, inMacState);
        return new MacStateJson(macState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/macs/{macaddress}", "/v4/macs/{macaddress}"})
    @DurationStatistics
    public MacStateJson updateMacState(@PathVariable String macaddress, @RequestBody MacStateJson resource) throws Exception {
        MacState inMacState = resource.getMacState();
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacState);
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);
        MacManagerRestPreconditionsUtil.verifyMacStateData(inMacState);
        MacState macState = service.updateMacState(macaddress, inMacState);
        return new MacStateJson(macState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/macs/{macaddress}", "/v4/macs/{macaddress}"})
    @DurationStatistics
    public String deleteMacAllocation(@PathVariable String macaddress) throws Exception {
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(macaddress);
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(macaddress);
        String macAddress = service.releaseMacState(macaddress);
        return "{mac_address: " + macAddress + "}";
    }

    @RequestMapping(
            method = GET,
            value = {"/macs/ranges/{rangeid}", "/v4/macs/ranges/{rangeid}"})
    @DurationStatistics
    public MacRangeJson getMacRangeByMacRangeId(@PathVariable String rangeid) throws Exception {
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(rangeid);
        MacRange macRange = service.getMacRangeByMacRangeId(rangeid);
        return new MacRangeJson(macRange);
    }

    @FieldFilter(type = MacRange.class)
    @RequestMapping(
            method = GET,
            value = {"/macs/ranges", "/v4/macs/ranges"})
    @DurationStatistics
    public Map<String, Collection<MacRange>> getAllMacRanges() throws Exception {

        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(request.getParameterMap(), MacRange.class);

        Map<String, MacRange> macRanges;
        HashMap<String, Collection<MacRange>> map = new HashMap<String, Collection<MacRange>>();
        macRanges = service.getAllMacRanges(queryParams);
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
    @DurationStatistics
    public MacRangeJson createMacRange(@RequestBody MacRangeJson resource) throws Exception {
        MacRange inMacRange = resource.getMacRange();
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacRange);
        MacManagerRestPreconditionsUtil.verifyMacRangeData(inMacRange);
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getFrom());
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getTo());
        MacRange macRange = service.createMacRange(inMacRange);
        return new MacRangeJson(macRange);
    }

    @RequestMapping(
            method = PUT,
            value = {"/macs/ranges/{rangeid}", "/v4/macs/ranges/{rangeid}"})
    @DurationStatistics
    public MacRangeJson updateMacRange(@PathVariable String rangeid, @RequestBody MacRangeJson resource) throws Exception {
        MacRange inMacRange = resource.getMacRange();
        if(inMacRange.getRangeId() == null){
            inMacRange.setRangeId(rangeid);
        }
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(inMacRange);
        MacManagerRestPreconditionsUtil.verifyMacRangeData(inMacRange);
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getFrom());
        MacManagerRestPreconditionsUtil.verifyMacAddressFormat(inMacRange.getTo());
        MacRange macRange = service.updateMacRange(inMacRange);
        if (macRange == null) {
            throw new ResourcePersistenceException();
        }

        return new MacRangeJson(macRange);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/macs/ranges/{rangeid}", "/v4/macs/ranges/{rangeid}"})
    @DurationStatistics
    public ResponseId deleteMacRange(@PathVariable String rangeid) throws Exception {
        MacManagerRestPreconditionsUtil.verifyParameterNotNullorEmpty(rangeid);
        String rangeId = service.deleteMacRange(rangeid);
        return new ResponseId(rangeId);
    }
}
