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
package com.futurewei.alcor.macmanager.utils;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.macmanager.exception.MacAddressInvalidException;
import com.futurewei.alcor.macmanager.exception.MacRangeInvalidException;
import com.futurewei.alcor.macmanager.exception.MacStateInvalidException;
import com.futurewei.alcor.web.entity.mac.MacAddress;
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.web.entity.mac.MacState;
import org.thymeleaf.util.StringUtils;

import javax.crypto.Mac;

public class MacManagerRestPreconditionsUtil {
    public static void verifyParameterNotNullorEmpty(String resourceId) throws ParameterNullOrEmptyException {
        if (StringUtils.isEmpty(resourceId)) {
            throw new ParameterNullOrEmptyException("Empty parameter");
        }
    }

    public static void verifyParameterNotNullorEmpty(MacState resource) throws ParameterNullOrEmptyException {
        if (resource == null) {
            throw new ParameterNullOrEmptyException("mac params is null");
        }
    }

    public static void verifyParameterNotNullorEmpty(MacRange resource) throws ParameterNullOrEmptyException {
        if (resource == null) {
            throw new ParameterNullOrEmptyException("null parameter");
        }
    }

    public static void verifyMacAddressFormat(String strMacAddress) throws MacAddressInvalidException {
        MacAddress macAddress = new MacAddress();
        if (!macAddress.validateMac(strMacAddress)) {
            throw new MacAddressInvalidException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_INVALID_FORMAT);
        }
    }

    public static void verifyMacStateData(MacState macState) throws MacStateInvalidException {
        String projectId = macState.getProjectId();
        String vpcId = macState.getVpcId();
        String portId = macState.getPortId();
        String state = macState.getState();

        if (projectId == null || vpcId == null || portId == null)
            throw new MacStateInvalidException(MacManagerConstant.MAC_EXCEPTION_REQUIRE_PARAMS_NULL);

        projectId = projectId.trim();
        vpcId = vpcId.trim();
        portId = portId.trim();

        if (projectId.length() == 0 || vpcId.length() == 0 || portId.length() == 0)
            throw new MacStateInvalidException(MacManagerConstant.MAC_EXCEPTION_REQUIRE_PARAMS_EMPTY);

        if (!StringUtils.isEmpty(state)) {
            state = state.trim();
            if (!state.equals(MacManagerConstant.MAC_STATE_ACTIVE) && !state.equals(MacManagerConstant.MAC_STATE_INACTIVE))
                throw new MacStateInvalidException(MacManagerConstant.MAC_EXCEPTION_STATE_INVALID);
        }
    }

    public static void verifyMacRangeData(MacRange macRange) throws MacRangeInvalidException {
        String rangeId = macRange.getRangeId();
        String from = macRange.getFrom();
        String to = macRange.getTo();
        String state = macRange.getState();

        if (StringUtils.isEmpty(rangeId) || StringUtils.isEmpty(from) || StringUtils.isEmpty(to))
            throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_VALUE_NULL);

        rangeId = rangeId.trim();
        from = from.trim();
        to = to.trim();
        state = state.trim();

        if (rangeId.length() == 0 || from.length() == 0 || to.length() == 0)
            throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_INVALID_EMPTY);

        if (!StringUtils.isEmpty(state)) {
            if (!state.equals(MacManagerConstant.MAC_STATE_ACTIVE) && !state.equals(MacManagerConstant.MAC_STATE_INACTIVE))
                throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_INVALID_DATA);
        }
    }
}