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


package com.futurewei.alcor.web.entity.mac;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


@Data
public class MacAddress {

    public static final String MAC_DELIMITER = "-";
    public static final int NIC_LENGTH = 24;

    @JsonIgnore
    private String oui;

    @JsonIgnore
    private String nic;


    public MacAddress() {
        oui = null;
        nic = null;
    }

    public MacAddress(MacAddress macAddress) {
        this(macAddress.oui, macAddress.nic);
    }

    public MacAddress(String oui, String nic) {
        this.oui = oui;
        this.nic = nic;
    }

    public MacAddress(String strMacAddress) {
        if (strMacAddress.length() >= 8)
            this.oui = strMacAddress.substring(0, 8);
        if (strMacAddress.length() > 8)
            this.nic = strMacAddress.substring(9);
    }

    public static String longToHex(long number) {
        String hex = Long.toHexString(number);
        return hex;
    }

    public static String hexToMac(String hex) {
        int length = MacAddress.NIC_LENGTH / 4;
        int lengthDelimiter = Math.round(length / 2) - 1;
        hex = hex.toUpperCase();
        while (hex.length() < length)
            hex = "0" + hex;
        StringBuffer buffer = new StringBuffer(length + lengthDelimiter);
        buffer.insert(0, hex);
        for (int i = 1; i <= lengthDelimiter; i++) {
            buffer.insert(i * 2 + (i - 1), MacAddress.MAC_DELIMITER);
        }
        return buffer.toString();
    }

    public static String longToMac(long number) {
        String mac = hexToMac(Long.toHexString(number));
        return mac;
    }

    public static long macToLong(String mac) {
        mac = mac.replace(MacAddress.MAC_DELIMITER, "");
        long l = Long.valueOf(mac, 16);
        return Long.valueOf(mac, 16);
    }

    public String getMacAddress() {
        String strMacAddress = oui + MAC_DELIMITER + nic;
        return strMacAddress;
    }

    public void print() {
        System.out.println(getMacAddress());
    }
}
