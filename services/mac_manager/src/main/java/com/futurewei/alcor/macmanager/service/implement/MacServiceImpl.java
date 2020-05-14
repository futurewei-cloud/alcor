/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.macmanager.service.implement;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.macmanager.dao.MacPoolRepository;
import com.futurewei.alcor.macmanager.dao.MacRangeRepository;
import com.futurewei.alcor.macmanager.dao.MacStateRepository;
import com.futurewei.alcor.macmanager.entity.MacAddress;
import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.exception.InvalidMacRangeException;
import com.futurewei.alcor.macmanager.exception.RetryLimitExceedException;
import com.futurewei.alcor.macmanager.exception.UniquenessViolationException;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.macmanager.utils.MacUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MacServiceImpl implements MacService {
    private static final Logger logger = LoggerFactory.getLogger(MacServiceImpl.class);
    static public Hashtable<String, MacRange> activeMacRanges = new Hashtable<String, MacRange>();
    final String DELIMITER = "/";

    @Autowired
    private MacRangeRepository macRangeRepository;

    @Autowired
    private MacStateRepository macStateRepository;

    @Autowired
    private MacPoolRepository macPoolRepository;

    @Value("${macmanager.oui}")
    private String oui;

    @Value("${macmanager.pool.size}")
    private long nMacPoolSize;

    @Value("${macmanager.retrylimit}")
    private long nRetryLimit;

    public MacState getMacStateByMacAddress(String macAddress) throws Exception {
        MacState macState = null;
        if (macAddress == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            macState = macStateRepository.findItem(macAddress);
        } catch (Exception e) {
            throw e;
        }
        return macState;
    }

    public MacState createMacState(MacState macState) throws Exception {
        if (macState == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        MacAddress macAddress = new MacAddress();
        if (macState.getState() == null)
            macState.setState(MacUtil.MAC_STATE_ACTIVE);
        else if (macState.getState().trim().length() == 0)
            macState.setState(MacUtil.MAC_STATE_ACTIVE);
        if (macPoolRepository.getSize() < (nMacPoolSize - 10)) {
            CompletableFuture<Long> completableFuture = CompletableFuture.supplyAsync(() -> {
                long n = 0;
                try {
                    n = generateMacInPool(20);
                } catch (Exception e) {

                }
                return n;
            });
            long l = completableFuture.get();
            completableFuture.thenAccept(System.out::println);
            completableFuture.join();
            logger.info("{} New MAC addresses were created.", l);
        }

        String strMacAddress = allocateMacState(macState);
        if (strMacAddress != null) {
            macState.setMacAddress(strMacAddress);
            macStateRepository.addItem(macState);
        } else {
            try {
                String nic = generateNic();
                macAddress.setOui(oui);
                macAddress.setNic(nic);
                macState.setMacAddress(macAddress.getMacAddress());
                MacState macState2 = macStateRepository.findItem(macAddress.getMacAddress());
                if (macStateRepository.findItem(macAddress.getMacAddress()) != null)
                    throw (new UniquenessViolationException(MacUtil.MAC_EXCEPTION_UNIQUENESSSS_VILOATION + macAddress.getMacAddress() + macState2.getProjectId()));
                else
                    macStateRepository.addItem(macState);
            } catch (Exception e) {
                throw e;
            }
        }
        return macState;
    }

    @Override
    public MacState updateMacState(String macAddress, MacState macState) throws Exception {
        if (macAddress == null || macState == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (macAddress.equals(macState.getMacAddress()) == false)
            throw (new ParameterUnexpectedValueException(MacUtil.MAC_EXCEPTION_PARAMETER_INVALID));
        if (macStateRepository.findItem(macAddress) != null) {
            macStateRepository.addItem(macState);
        } else {
            throw (new ResourceNotFoundException(MacUtil.MAC_EXCEPTION_MAC_NOT_EXISTING));
        }
        return macState;
    }

    public String releaseMacState(String macAddress) throws Exception {
        if (macAddress == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        MacState macState = macStateRepository.findItem(macAddress);
        if (macState == null) {
            throw (new ResourceNotFoundException(MacUtil.MAC_EXCEPTION_MAC_NOT_EXISTING));
        } else {
            try {
                macStateRepository.deleteItem(macAddress);
                macPoolRepository.addItem(new MacAddress(macAddress));
            } catch (Exception e) {
                throw e;
            }
        }
        return macState.getMacAddress();
    }

    @Override
    public MacRange getMacRangeByMacRangeId(String macRangeId) throws Exception {
        if (macRangeId == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        MacRange macRange = null;
        try {
            macRange = macRangeRepository.findItem(macRangeId);
        } catch (Exception e) {
            throw e;
        }
        return macRange;
    }

    @Override
    public Map<String, MacRange> getAllMacRanges() throws Exception {
        Hashtable<String, MacRange> macRanges = new Hashtable<String, MacRange>();
        try {
            macRanges.putAll(macRangeRepository.findAllItems());
        } catch (Exception e) {
            throw e;
        }
        return macRanges;
    }

    @Override
    public MacRange createMacRange(MacRange macRange) throws Exception {
        if (macRange == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (isValidRange(macRange) == false)
            throw (new InvalidMacRangeException(MacUtil.MAC_EXCEPTION_RANGE_VALUE_INVALID));
        try {
            macRangeRepository.addItem(macRange);
            if (macRange.getState().equals(MacUtil.MAC_RANGE_STATE_ACTIVE))
                activeMacRanges.put(macRange.getRangeId(), macRange);
        } catch (Exception e) {
            throw e;
        }
        return macRange;
    }

    @Override
    public MacRange updateMacRange(MacRange macRange) throws Exception {
        if (macRange == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (isValidRange(macRange) == false)
            throw (new InvalidMacRangeException(MacUtil.MAC_EXCEPTION_RANGE_VALUE_INVALID));
        try {
            macRangeRepository.deleteItem(macRange.getRangeId());
            macRangeRepository.addItem(macRange);
            if (macRange.getState().equals(MacUtil.MAC_RANGE_STATE_INACTIVE) && activeMacRanges.containsKey(macRange.getRangeId()))
                activeMacRanges.remove(macRange.getRangeId(), macRange);
            else if (macRange.getState().equals(MacUtil.MAC_RANGE_STATE_ACTIVE) && activeMacRanges.containsKey(macRange.getRangeId()) == false)
                activeMacRanges.put(macRange.getRangeId(), macRange);
        } catch (Exception e) {
            throw e;
        }
        return macRange;
    }

    @Override
    public String deleteMacRange(String rangeId) throws Exception {
        if (rangeId == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            macRangeRepository.deleteItem(rangeId);
            activeMacRanges.remove(rangeId);
        } catch (Exception e) {
            throw e;
        }
        return rangeId;
    }

    private String allocateMacState(MacState macState) throws Exception {
        String strMacAddress = null;
        if (macState == null)
            throw (new ParameterNullOrEmptyException(MacUtil.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            strMacAddress = macPoolRepository.getItem();
            updateMacAllocationStatus(strMacAddress);
        } catch (Exception e) {
            throw e;
        }
        return strMacAddress;
    }

    private String generateNic() throws Exception {
        String nic = null;
        MacAddress macAddress = new MacAddress();
        long randomNic;
        Long from = (long) 0;
        Long to = (long) Math.pow(2, MacAddress.NIC_LENGTH);

        MacRange macRange = getMacRange();
        if (macRange != null) {
            from = MacAddress.macToLong(new MacAddress(macRange.getFrom()).getNic());
            to = MacAddress.macToLong(new MacAddress(macRange.getTo()).getNic());
        }

        int i = 0;
        while (nic == null && i < nRetryLimit) {
            randomNic = ThreadLocalRandom.current().nextLong(from, to);
            String nicTemp = MacAddress.hexToMac(Long.toHexString(randomNic));
            macAddress.setNic(nicTemp);
            if ((macStateRepository.findItem(macAddress.getMacAddress()) == null) && (macPoolRepository.findItem(macAddress.getMacAddress()) == null)) {
                nic = nicTemp;
            }
            i++;
        }
        if (nic == null)
            throw new RetryLimitExceedException(MacUtil.MAC_EXCEPTION_RETRY_LIMIT_EXCEED);
        return nic;
    }

    private MacRange getMacRange() throws Exception {
        MacRange macRange = new MacRange();
        if (activeMacRanges.isEmpty())
            getActiveMacRanges();
        int randomIndex = ThreadLocalRandom.current().nextInt(0, activeMacRanges.size());
        Vector<String> vector = new Vector<String>(activeMacRanges.keySet());
        return activeMacRanges.get(vector.elementAt(randomIndex));
    }

    private void getActiveMacRanges() throws Exception {
        Hashtable<String, MacRange> macRanges = new Hashtable(macRangeRepository.findAllItems());
        if (macRanges == null)
            macRanges = new Hashtable<String, MacRange>();
        int nSize = macRanges.size();
        if (nSize > 0) {
            for (Map.Entry<String, MacRange> entry : macRanges.entrySet()) {
                if (entry.getValue().getState().equals(MacUtil.MAC_RANGE_STATE_ACTIVE)) {
                    activeMacRanges.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (macRanges != null) {
            MacRange newRange = new MacRange();
            newRange = createDefaultRange(oui);
            macRangeRepository.addItem(newRange);
            activeMacRanges.put(newRange.getRangeId(), newRange);
        }
    }

    private boolean isValidRange(MacRange macRange) {
        String strFrom = macRange.getFrom();
        String strTo = macRange.getTo();
        long from = MacAddress.macToLong(new MacAddress(strFrom).getNic());
        long to = MacAddress.macToLong(new MacAddress(strTo).getNic());
        return from < to;
    }

    private MacRange createDefaultRange(String oui) {
        String rangeId = MacUtil.DEFAULT_RANGE;
        long nNicLength = (long)Math.pow(2,MacAddress.NIC_LENGTH);
        String strFrom  = MacAddress.longToMac(0);
        String strTo = MacAddress.longToMac(nNicLength - 1);
        String from = new MacAddress(oui, strFrom).getMacAddress();
        String to = new MacAddress(oui, strTo).getMacAddress();
        String state = MacUtil.MAC_RANGE_STATE_ACTIVE;
        BitSet bitSet = new BitSet((int)nNicLength);
        MacRange defaultRange = new MacRange(rangeId, from, to, state);
        defaultRange.setBitSet(bitSet);
        return defaultRange;
    }

    private long generateMacInPool(int n) throws Exception {
        Exception exception = null;
        long nReturn = 0;
        ArrayList<String> list = new ArrayList<String>();
        if (n < 1) return nReturn;
        MacAddress macAddress = new MacAddress();
        for (int i = 0; i < n; i++) {
            try {
                String nic = generateNic();
                macAddress.setOui(oui);
                macAddress.setNic(nic);
                String strMacAddress = macAddress.getMacAddress();
                MacState macState = macStateRepository.findItem(strMacAddress);
                if (macState == null) {
                    updateMacAllocationStatus(strMacAddress);
                    macPoolRepository.addItem(new MacAddress(strMacAddress));
                    nReturn++;
                }
            } catch (RetryLimitExceedException e) {
                exception = e;
            }
        }
        if (exception != null)
            throw exception;
        return nReturn;
    }

    private void updateMacAllocationStatus(String strMacAddress) throws Exception {
        MacRange deafultRange = getMacRangeByMacRangeId(MacUtil.DEFAULT_RANGE);
        BitSet bitSet = deafultRange.getBitSet();
        int ndx = macToIndex(deafultRange, strMacAddress);
        bitSet.set(ndx);
        macRangeRepository.addItem(deafultRange);
    }

    private int macToIndex(MacRange range, String strMac) {
        int ndx = 0;
        MacAddress mac = new MacAddress(strMac);
        long nMac1 = MacAddress.macToLong(strMac);
        long nMac2 = MacAddress.macToLong(range.getFrom());
        ndx = (int) (nMac1 - nMac2);
        return ndx;
    }
}
