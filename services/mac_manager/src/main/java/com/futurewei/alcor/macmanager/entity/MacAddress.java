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


package com.futurewei.alcor.macmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
public class MacAddress {
    public static final String MAC_DELIMITER = "-";
    public static final int MAC_LENGTH = 48;

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

    public static String hexToNic(String hex, int nNicLength) {
        int length = nNicLength / 4;
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

    public static String longToNic(long number, int nNicLength) {
        String mac = hexToNic(Long.toHexString(number), nNicLength);
        return mac;
    }

    public static long macToLong(String mac) {
        mac = mac.replace(MacAddress.MAC_DELIMITER, "");
        long l = Long.valueOf(mac, 16);
        return Long.valueOf(mac, 16);
    }

    public static long nicToLong(String nic) {
        nic = nic.replace(MacAddress.MAC_DELIMITER, "");
        long l = Long.valueOf(nic, 16);
        return Long.valueOf(nic, 16);
    }

    public String getMacAddress() {
        String strMacAddress = oui + MAC_DELIMITER + nic;
        return strMacAddress;
    }

    public void print() {
        System.out.println(getMacAddress());
    }

    public boolean validateMac(String strMac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(strMac);
        return m.find();
    }

    public int getNicLength() {
        String strOui = oui.replace(MacAddress.MAC_DELIMITER, "");
        int nOuiLength = (12 - strOui.length()) * 4;
        int nNicLength = 48 - nOuiLength;
        return nNicLength;
    }
}
