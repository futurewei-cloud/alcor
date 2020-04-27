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
package com.futurewei.alcor.macmanager.service.implement.redis;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.macmanager.dao.redis.MacPoolRedisRepository;
import com.futurewei.alcor.macmanager.dao.redis.MacRangeRedisRepository;
import com.futurewei.alcor.macmanager.dao.redis.MacStateRedisRepository;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RedisMacServiceImpl implements MacService {
    private static final Logger LOG = LoggerFactory.getLogger(RedisMacServiceImpl.class);
    static public Hashtable<String, MacRange> activeMacRanges = new Hashtable<String, MacRange>();
    final String DELIMITER = "/";

    @Autowired
    private MacRangeRedisRepository macRangeRedisRepository;

    @Autowired
    private MacPoolRedisRepository macPoolRedisRepository;

    @Autowired
    private MacStateRedisRepository macStateRedisRepository;

    @Value("${macmanager.oui}")
    private String oui;

    @Value("${macmanager.pool.size}")
    private long nMacPoolSize;

    @Value("${macmanager.retrylimit}")
    private long nRetryLimit;

    public MacState getMacStateByMacAddress(String macAddress) {
        MacState macState = macStateRedisRepository.findItem(macAddress);
        return macState;
    }

    public MacState createMacState(MacState macState) throws Exception {
        MacAddress macAddress = new MacAddress();
        if (macState.getState() == null)
            macState.setState(MacUtil.MAC_STATE_ACTIVE);
        else if (macState.getState().trim().length() == 0)
            macState.setState(MacUtil.MAC_STATE_ACTIVE);
        if (macPoolRedisRepository.getSize() < (nMacPoolSize - 10)) {
            CompletableFuture<Long> completableFuture = CompletableFuture.supplyAsync(() -> {
                long n = 0;
                try {
                    n = generateMacInPool(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return n;
            });
            long l = completableFuture.get();
            completableFuture.thenAccept(System.out::println);
            completableFuture.join();
            LOG.info("{} New MAC addresses were created.", l);
        }

        String strMacAddress = allocateMacState(macState);
        if (strMacAddress != null) {
            macState.setMacAddress(strMacAddress);
            macStateRedisRepository.addItem(macState);
        } else {
            try {
                String nic = generateNic();
                macAddress.setOui(oui);
                macAddress.setNic(nic);
                macState.setMacAddress(macAddress.getMacAddress());
                MacState macState2 = macStateRedisRepository.findItem(macAddress.getMacAddress());
                if (macStateRedisRepository.findItem(macAddress.getMacAddress()) != null)
                    throw (new UniquenessViolationException(MacUtil.MAC_EXCEPTION_UNIQUENESSSS_VILOATION + macAddress.getMacAddress() + macState2.getProjectId()));
                else
                    macStateRedisRepository.addItem(macState);
            } catch (Exception e) {
                throw e;
            }
        }
        return macState;
    }

    @Override
    public MacState updateMacState(String macAddress, MacState macState) throws Exception {
        if (macState != null)
            macStateRedisRepository.updateItem(macState);
        return macState;
    }

    public String releaseMacState(String macAddress) throws Exception {
        MacState macState = macStateRedisRepository.findItem(macAddress);
        if (macState == null) {
            ResourceNotFoundException e = new ResourceNotFoundException("MAC address Not Found");
            throw e;
        } else {
            macStateRedisRepository.deleteItem(macAddress);
            macPoolRedisRepository.addItem(macAddress);
        }
        return macState.getMacAddress();
    }

    @Override
    public MacRange getMacRangeByMacRangeId(String macRangeId) {
        MacRange macRange = macRangeRedisRepository.findItem(macRangeId);
        return macRange;
    }

    @Override
    public Map<String, MacRange> getAllMacRanges() {
        Map<String, MacRange> macRanges = macRangeRedisRepository.findAllItems();
        return macRanges;
    }

    @Override
    public MacRange createMacRange(MacRange macRange) throws Exception {
        if (macRange != null) {
            if (isValidRange(macRange)) {
                macRangeRedisRepository.addItem(macRange);
                if (macRange.getState().equals(MacUtil.MAC_RANGE_STATE_ACTIVE))
                    activeMacRanges.put(macRange.getRangeId(), macRange);
            } else
                throw (new InvalidMacRangeException(MacUtil.MAC_EXCEPTION_RANGE_VALUE_INVALID));
        }
        return macRange;
    }

    @Override
    public MacRange updateMacRange(MacRange macRange) throws Exception {
        if (macRange != null) {
            macRangeRedisRepository.updateItem(macRange);
            if (macRange.getState().equals(MacUtil.MAC_RANGE_STATE_INACTIVE) && activeMacRanges.containsKey(macRange.getRangeId()))
                activeMacRanges.remove(macRange.getRangeId(), macRange);
            else if (macRange.getState().equals(MacUtil.MAC_RANGE_STATE_ACTIVE) && activeMacRanges.containsKey(macRange.getRangeId()) == false)
                activeMacRanges.put(macRange.getRangeId(), macRange);
        }
        return macRange;
    }

    @Override
    public String deleteMacRange(String rangeId) throws Exception {
        if (rangeId != null) {
            macRangeRedisRepository.deleteItem(rangeId);
            activeMacRanges.remove(rangeId);
        }
        return rangeId;
    }

    private String allocateMacState(MacState macState) {
        String strMacAddress = macPoolRedisRepository.getItem();
        if (strMacAddress != null) {
            macPoolRedisRepository.deleteItem(strMacAddress);
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
            if (macStateRedisRepository.findMac(macAddress.getMacAddress()) == null && macPoolRedisRepository.findItem(macAddress.getMacAddress()) == null) {
                nic = nicTemp;
            }
            i++;
        }
        if (nic == null && i >= nRetryLimit)
            throw new RetryLimitExceedException(MacUtil.MAC_EXCEPTION_RETRY_LIMIT_EXCEED);
        return nic;
    }

    private MacRange getMacRange() {
        MacRange macRange = new MacRange();
        if (activeMacRanges.isEmpty())
            getActiveMacRanges();
        int randomIndex = ThreadLocalRandom.current().nextInt(0, activeMacRanges.size());
        Vector<String> vector = new Vector<String>(activeMacRanges.keySet());
        return activeMacRanges.get(vector.elementAt(randomIndex));
    }

    public void getActiveMacRanges() {
        Hashtable<String, MacRange> macRanges = new Hashtable(macRangeRedisRepository.findAllItems());
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
            newRange.createDefault(oui);
            macRangeRedisRepository.addItem(newRange);
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

    public long generateMacInPool(int n) throws Exception {
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
                MacState macState = macStateRedisRepository.findItem(strMacAddress);
                if (macState == null) {
                    macPoolRedisRepository.addItem(strMacAddress);
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
}