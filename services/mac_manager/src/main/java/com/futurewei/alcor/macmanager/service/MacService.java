package com.futurewei.alcor.macmanager.service;

import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.exception.UniquenessViolationException;

public interface MacService {
    MacState getMacStateByMacAddress(String macAddress);

    String releaseMac(String macAddress) throws Exception;

    MacState createMacState(MacState macState) throws UniquenessViolationException, Exception;
}
