/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.macmanager.utils;

import com.futurewei.alcor.macmanager.controller.MacController;
import com.futurewei.alcor.web.entity.mac.MacAddress;

public class MacUtils {

    public static String hexToNic(String hex, int nNicLength) {
        int length = nNicLength / 4;
        int lengthDelimiter = Math.round(length / 2) - 1;
        StringBuilder hexBuilder = new StringBuilder(hex.toUpperCase());
        while (hexBuilder.length() < length)
            hexBuilder.insert(0, "0");
        for (int i = 1; i <= lengthDelimiter; i++) {
            hexBuilder.insert(i * 2 + (i - 1), MacAddress.MAC_DELIMITER);
        }
        return hexBuilder.toString();
    }

    public static String longToNic(long number, int nNicLength) {
        return hexToNic(Long.toHexString(number), nNicLength);
    }

    public static long macToLong(String mac) {
        mac = mac.replace(MacAddress.MAC_DELIMITER, "");
        return Long.valueOf(mac, 16);
    }

    public static long nicToLong(String nic) {
        nic = nic.replace(MacAddress.MAC_DELIMITER, "");
        return Long.valueOf(nic, 16);
    }

    public static int nicBinaryLength(String oui){
        String strOui = oui.replace(MacAddress.MAC_DELIMITER, "");
        int nOuiLength = (12 - strOui.length()) * 4;
        return 48 - nOuiLength;
    }

    public static int nicHexLength(String oui){
        String strOui = oui.replace(MacAddress.MAC_DELIMITER, "");
        return 12 - strOui.length();
    }

    public static String getMacSuffix(String oui, String mac){
        return mac.replaceFirst(oui + MacAddress.MAC_DELIMITER, "");
    }

    public static String getMacSuffixNoDelimiter(String oui, String mac){
        return mac.replaceFirst(oui + MacAddress.MAC_DELIMITER, "")
                .replaceAll(MacAddress.MAC_DELIMITER, "");
    }

    public static String longToMac(String oui, long nic){
        int nicLength = nicHexLength(oui);
        return oui + MacAddress.MAC_DELIMITER + longToNic(nic, nicLength);
    }

}
