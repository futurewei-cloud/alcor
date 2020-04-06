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
package com.futurewei.alcor.macmanager.service;

import com.futurewei.alcor.macmanager.dao.MacRedisRepository;
import com.futurewei.alcor.macmanager.dao.OuiRedisRepository;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.entity.OuiState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MacAddressService {

    final String DELIMITER1 = "/";
    final String DELIMITER2 = "-";
    final int KEY_LENGTH = 8;

    @Autowired
    private OuiRedisRepository ouiRedisRepository;

    @Autowired
    private MacRedisRepository macRedisRepository;

    public MacState getMacStateByMacAddress(String macAddress) {
        String key = getKey(macAddress);
        macRedisRepository.setKey(key);
        MacState macState = macRedisRepository.findItem(macAddress);
        return macState;
    }

    public MacState releaseMac(String macAddress) {
        String key = getKey(macAddress);
        macRedisRepository.setKey(key);
        MacState macState = macRedisRepository.findItem(macAddress);
        macState.setProjectId("");
        macState.setVpcId("");
        macState.setPortId("");
        macRedisRepository.updateItem(macState);
        return macState;
    }

    public MacState createMacState(MacState macState) throws Exception {
        String macAddress;
        String projectId = macState.getProjectId();
        String vpcId = macState.getVpcId();
        String portId = macState.getPortId();
        String oui = generateOui(projectId, vpcId);
        String nic = generateNic(oui, projectId, vpcId, portId);
        macAddress = oui + DELIMITER2 + nic;
        macState.setMacAddress(macAddress);
        return macState;
    }

    public Map getMacStateByVpcIdPort(String projectId, String vpcId, String portId) {
        String hk = makeKey(projectId, vpcId, portId);
        String oui = ouiRedisRepository.findOui(hk);
        macRedisRepository.setKey(oui);
        return macRedisRepository.findMacAddressesbyVpcPort(portId);
    }

    private String generateOui(String projectId, String vpcId) {
        long randomOui;
        String hk = projectId + DELIMITER1 + vpcId;
        String oui = ouiRedisRepository.findOui(hk);
        if (oui == null) {
            while (oui == null) {
                randomOui = ThreadLocalRandom.current().nextLong(0, 2 ^ 24);
                String ouiTemp = hexToMac(Long.toHexString(randomOui));
                if (macRedisRepository.exisingOui(ouiTemp) == false)
                    oui = ouiTemp;
            }
            ouiRedisRepository.addItem(new OuiState(projectId + DELIMITER1 + vpcId, oui));
        }
        return oui;
    }

    private String generateNic(String oui, String projectId, String vpcId, String portId) {
        String nic = null;
        long randomNic;

        macRedisRepository.setKey(oui);
        while (nic == null) {
            randomNic = ThreadLocalRandom.current().nextLong(0, 2 ^ 24);
            String nicTemp = hexToMac(Long.toHexString(randomNic));
            if (macRedisRepository.findMac(nicTemp) == null) {
                String macAddress = oui + DELIMITER2 + nicTemp;
                macRedisRepository.addItem(new MacState(macAddress, projectId, vpcId, portId));
                nic = nicTemp;
            }
        }
        return nic;
    }

    private String hexToMac(String hex) {
        hex = hex.toUpperCase();
        while (hex.length() < (KEY_LENGTH - 2))
            hex = "0" + hex;
        StringBuffer buffer = new StringBuffer(KEY_LENGTH);
        buffer.insert(0, hex);
        buffer.insert(2, ":");
        buffer.insert(5, ":");

        return buffer.toString();
    }

    private String getKey(String macAddress) {
        String key = macAddress.substring(0, KEY_LENGTH);
        return key;
    }

    private String makeKey(String projectid, String vpcid, String port) {
        String key = projectid;
        key = key.concat(DELIMITER1);
        key = key.concat(vpcid);
        key = key.concat(DELIMITER1);
        key = key.concat(port);

        return key;
    }
}

