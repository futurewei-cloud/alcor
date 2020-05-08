package com.futurewei.alcor.macmanager.service;

import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacState;

import java.util.Map;

public interface MacService {
    MacState getMacStateByMacAddress(String macAddress) throws Exception;

    MacState createMacState(MacState macState) throws Exception;

    MacState updateMacState(String macaddress, MacState macState) throws Exception;

    String releaseMacState(String macAddress) throws Exception;

    MacRange getMacRangeByMacRangeId(String macRangeId) throws Exception;

    Map<String, MacRange> getAllMacRanges() throws Exception;

    MacRange createMacRange(MacRange macRange) throws Exception;

    MacRange updateMacRange(MacRange macRange) throws Exception;

    String deleteMacRange(String rangeid) throws Exception;
}
