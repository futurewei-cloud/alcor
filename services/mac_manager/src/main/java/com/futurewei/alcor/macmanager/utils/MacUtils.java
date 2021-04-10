/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.macmanager.utils;

import com.futurewei.alcor.macmanager.controller.MacController;
import com.futurewei.alcor.web.entity.mac.MacAddress;

public class MacUtils {

    public static String hexToNic(String hex, int nNicLength) {
        int lengthDelimiter = Math.round(nNicLength / 2) - 1;
        StringBuilder hexBuilder = new StringBuilder(hex.toLowerCase());
        while (hexBuilder.length() < nNicLength) {
            hexBuilder.insert(0, "0");
        }
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
