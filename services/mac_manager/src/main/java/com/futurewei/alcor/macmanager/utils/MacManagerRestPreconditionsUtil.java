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

package com.futurewei.alcor.macmanager.utils;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.macmanager.entity.MacAddress;
import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.exception.MacAddressInvalidException;
import com.futurewei.alcor.macmanager.exception.MacRangeInvalidException;
import com.futurewei.alcor.macmanager.exception.MacStateInvalidException;
import org.thymeleaf.util.StringUtils;

public class MacManagerRestPreconditionsUtil {
    public static void verifyParameterNotNullorEmpty(String resourceId) throws ParameterNullOrEmptyException {
        if (StringUtils.isEmpty(resourceId)) {
            throw new ParameterNullOrEmptyException("Empty parameter");
        }
    }

    public static void verifyParameterNotNullorEmpty(MacState resource) throws ParameterNullOrEmptyException {
        if (resource == null) {
            throw new ParameterNullOrEmptyException("null parameter");
        }
    }

    public static void verifyParameterNotNullorEmpty(MacRange resource) throws ParameterNullOrEmptyException {
        if (resource == null) {
            throw new ParameterNullOrEmptyException("null parameter");
        }
    }

    public static void verifyMacAddressFormat(String strMacAddress) throws MacAddressInvalidException {
        MacAddress macAddress = new MacAddress();
        if (macAddress.validateMac(strMacAddress) == false) {
            throw new MacAddressInvalidException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_INVALID_FORMAT);
        }
    }

    public static void verifyMacStateData(MacState macState) throws MacStateInvalidException {
        String projectId = macState.getProjectId();
        String vpcId = macState.getVpcId();
        String portId = macState.getPortId();
        String state = macState.getState();

        projectId = projectId.trim();
        vpcId = vpcId.trim();
        portId = portId.trim();
        state = state.trim();
        if (projectId == null || vpcId == null || portId == null)
            throw new MacStateInvalidException(MacManagerConstant.MAC_EXCEPTION_MACSTATE_NULL);

        if (projectId.length() == 0 || vpcId.length() == 0 || portId.length() == 0)
            throw new MacStateInvalidException(MacManagerConstant.MAC_EXCEPTION_MACSTATE_INVALID_EMPTY);
        if (state != null) {
            if (state.length() > 0) {
                if (state.equals(MacManagerConstant.MAC_STATE_ACTIVE) == false && state.equals(MacManagerConstant.MAC_STATE_INACTIVE) == false)
                    throw new MacStateInvalidException(MacManagerConstant.MAC_EXCEPTION_MACSTATE_INVALID_DATA);
            }
        }
    }

    public static void verifyMacRangeData(MacRange macRange) throws MacRangeInvalidException {
        String rangeId = macRange.getRangeId();
        String from = macRange.getFrom();
        String to = macRange.getTo();
        String state = macRange.getState();

        rangeId = rangeId.trim();
        from = from.trim();
        to = to.trim();
        state = state.trim();
        if (rangeId == null || from == null || to == null)
            throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_VALUE_NULL);

        if (rangeId.length() == 0 || from.length() == 0 || to.length() == 0)
            throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_INVALID_EMPTY);

        if (state != null) {
            if (state.length() > 0) {
                if (state.equals(MacManagerConstant.MAC_STATE_ACTIVE) == false && state.equals(MacManagerConstant.MAC_STATE_INACTIVE) == false)
                    throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_INVALID_DATA);
            }
        }
    }
}