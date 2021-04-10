/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/



package com.futurewei.alcor.web.entity.mac;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
public class MacAddress {
    public static final String MAC_DELIMITER = ":";
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
