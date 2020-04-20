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

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.macmanager.dao.MacPoolRedisRepository;
import com.futurewei.alcor.macmanager.dao.MacRangeRedisRepository;
import com.futurewei.alcor.macmanager.dao.MacRedisRepository;
import com.futurewei.alcor.macmanager.entity.MacAddress;
import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.exception.UniquenessViolationException;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.macmanager.utils.MacUtil;
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
public class MacRedisServiceImpl implements MacService {
    static public Hashtable<String, MacRange> activeMacRanges = new Hashtable<String, MacRange>();
    final String DELIMITER = "/";

    @Autowired
    private MacRangeRedisRepository macRangeRedisRepository;

    @Autowired
    private MacPoolRedisRepository macPoolRedisRepository;

    @Autowired
    private MacRedisRepository macRedisRepository;

    @Value("${macmanager.oui}")
    private String oui;

    @Value("${macmanager.pool.size}")
    private long nMacPoolSize;

    public MacState getMacStateByMacAddress(String macAddress) {
        MacState macState = macRedisRepository.findItem(macAddress);
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
            System.out.println(("# of new MAC: " + l));
            completableFuture.join();
        }

        String strMacAddress = allocateMacState(macState);
        if (strMacAddress != null) {
            macState.setMacAddress(strMacAddress);
            macRedisRepository.addItem(macState);
        } else {
            String nic = generateNic();
            macAddress.setOui(oui);
            macAddress.setNic(nic);
            macState.setMacAddress(macAddress.getMacAddress());
            MacState macState2 = macRedisRepository.findItem(macAddress.getMacAddress());
            if (macRedisRepository.findItem(macAddress.getMacAddress()) != null)
                throw (new UniquenessViolationException("This mac address is not unique!!" + macAddress.getMacAddress() + macState2.getProjectId()));
            else
                macRedisRepository.addItem(macState);
        }

        return macState;
    }

    public MacState createMacState1(MacState macState) throws Exception {
        MacAddress macAddress = new MacAddress();
        if (macState.getState() == null)
            macState.setState(MacUtil.MAC_STATE_ACTIVE);
        else if (macState.getState().trim().length() == 0)
            macState.setState(MacUtil.MAC_STATE_ACTIVE);
        String strMacAddress = allocateMacState(macState);
        if (strMacAddress != null) {
            macState.setMacAddress(strMacAddress);
            macRedisRepository.addItem(macState);
        } else {
            String nic = generateNic();
            macAddress.setOui(oui);
            macAddress.setNic(nic);
            macState.setMacAddress(macAddress.getMacAddress());
            MacState macState2 = macRedisRepository.findItem(macAddress.getMacAddress());
            if (macRedisRepository.findItem(macAddress.getMacAddress()) != null)
                throw (new UniquenessViolationException("This mac address is not unique!!" + macAddress.getMacAddress() + macState2.getProjectId()));
            else
                macRedisRepository.addItem(macState);
        }
        return macState;
    }

    @Override
    public MacState updateMacState(String macAddress, MacState macState) throws Exception {
        if (macState != null)
            macRedisRepository.updateItem(macState);
        return macState;
    }

    public String releaseMacState(String macAddress) throws Exception {
        MacState macState = macRedisRepository.findItem(macAddress);
        if (macState == null) {
            ResourceNotFoundException e = new ResourceNotFoundException("MAC address Not Found");
            throw e;
        } else {
            macRedisRepository.deleteItem(macAddress);
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
            macRangeRedisRepository.addItem(macRange);
            if (macRange.getState().equals(MacUtil.MAC_RANGE_STATE_ACTIVE))
                activeMacRanges.put(macRange.getRangeId(), macRange);
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

    private String generateNic() {
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

        while (nic == null) {
            randomNic = ThreadLocalRandom.current().nextLong(from, to);
            String nicTemp = MacAddress.hexToMac(Long.toHexString(randomNic));
            macAddress.setNic(nicTemp);
            if (macRedisRepository.findMac(macAddress.getMacAddress()) == null && macPoolRedisRepository.findItem(macAddress.getMacAddress()) == null) {
                nic = nicTemp;
            }
        }
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

    public long generateMacInPool(int n) throws Exception {
        long nReturn = 0;
        ArrayList<String> list = new ArrayList<String>();
        if (n < 1) return nReturn;
        MacAddress macAddress = new MacAddress();
        for (int i = 0; i < n; i++) {
            String nic = generateNic();
            macAddress.setOui(oui);
            macAddress.setNic(nic);
            String strMacAddress = macAddress.getMacAddress();
            MacState macState = macRedisRepository.findItem(strMacAddress);
            if (macState == null) {
                macPoolRedisRepository.addItem(strMacAddress);
                nReturn++;
            }
        }
        return nReturn;
    }
}