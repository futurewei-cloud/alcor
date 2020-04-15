package com.futurewei.alcor.macmanager.service;

import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.exception.UniquenessViolationException;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public interface MacService {
    MacState getMacStateByMacAddress(String macAddress);

    MacState createMacState(MacState macState) throws Exception;

    MacState updateMacState(String macaddress, MacState macState) throws Exception;

    String releaseMacState(String macAddress) throws Exception;

    MacRange getMacRangeByMacRangeId(String macRangeId);

    Map<String, MacRange> getAllMacRanges();

    MacRange createMacRange(MacRange macRange) throws Exception;

    MacRange updateMacRange(MacRange macRange) throws Exception;

    String deleteMacRange(String rangeid) throws Exception;
}
