package com.futurewei.alcor.macmanager.service;

import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.macmanager.exception.*;
import com.futurewei.alcor.web.entity.mac.MacStateBulkJson;


import java.util.Map;

public interface MacService {
    MacState getMacStateByMacAddress(String macAddress) throws Exception;

    MacState createMacState(MacState macState) throws Exception;

    MacState createMacStateInRange(String rangeid, MacState macState ) throws Exception;

    MacState updateMacState(String macaddress, MacState macState) throws Exception;

    String releaseMacState(String macAddress) throws Exception;

    MacRange getMacRangeByMacRangeId(String macRangeId) throws Exception;

    Map<String, MacRange> getAllMacRanges(Map<String, Object[]> queryParams) throws Exception;

    MacRange createMacRange(MacRange macRange) throws Exception;

    MacRange updateMacRange(MacRange macRange) throws Exception;

    String deleteMacRange(String rangeid) throws Exception;

    MacStateBulkJson createMacStateBulk(MacStateBulkJson macStateBulkJson) throws Exception;

    MacStateBulkJson createMacStateBulkInRange(String rangeId, MacStateBulkJson macStateBulkJson) throws Exception;
}
