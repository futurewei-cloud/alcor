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
package com.futurewei.alcor.privateipmanager.utils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Ipv6AddrUtil {
    public static boolean formatCheck(String strIp) {
        if (strIp != null && !strIp.isEmpty()) {

            String regex = "(^((([0-9A-Fa-f]{1,4}:){7}(([0-9A-Fa-f]{1,4}){1}|:))"
                    + "|(([0-9A-Fa-f]{1,4}:){6}((:[0-9A-Fa-f]{1,4}){1}|"
                    + "((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|"
                    + "([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|"
                    + "[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|"
                    + "(([0-9A-Fa-f]{1,4}:){5}((:[0-9A-Fa-f]{1,4}){1,2}|"
                    + ":((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|"
                    + "([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|"
                    + "[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|"
                    + "(([0-9A-Fa-f]{1,4}:){4}((:[0-9A-Fa-f]{1,4}){1,3}"
                    + "|:((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|"
                    + "([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|[0-1][0-9][0-9]|"
                    + "([0-9]){1,2})){3})|:))|(([0-9A-Fa-f]{1,4}:){3}((:[0-9A-Fa-f]{1,4}){1,4}|"
                    + ":((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|"
                    + "([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|"
                    + "[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|"
                    + "(([0-9A-Fa-f]{1,4}:){2}((:[0-9A-Fa-f]{1,4}){1,5}|"
                    + ":((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|"
                    + "([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|"
                    + "[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))"
                    + "|(([0-9A-Fa-f]{1,4}:){1}((:[0-9A-Fa-f]{1,4}){1,6}"
                    + "|:((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|"
                    + "([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|"
                    + "[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|"
                    + "(:((:[0-9A-Fa-f]{1,4}){1,7}|(:[fF]{4}){0,1}:((22[0-3]|2[0-1][0-9]|"
                    + "[0-1][0-9][0-9]|([0-9]){1,2})"
                    + "([.](25[0-5]|2[0-4][0-9]|[0-1][0-9][0-9]|([0-9]){1,2})){3})|:)))$)";

            if (strIp.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public static String bigIntToIpv6(BigInteger bigIntIpv6) {
        byte[] bytes = bigIntIpv6.toByteArray();
        byte[] unsignedBytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        if (bytes.length == 4 || bytes.length == 16) {
            unsignedBytes = bytes;
        }

        try {
            String ip = InetAddress.getByAddress(unsignedBytes).toString();
            return ip.substring(ip.indexOf('/') + 1).trim();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static BigInteger ipv6ToBitInt(String strIpv6) {
        byte[] bytes = new byte[17];
        bytes[0] = 0;
        int ib = 16;
        boolean comFlag = false;

        if (strIpv6.startsWith(":")) {
            strIpv6 = strIpv6.substring(1);
        }

        String groups[] = strIpv6.split(":");
        for (int ig = groups.length - 1; ig > -1; ig--) {
            if (groups[ig].contains(".")) {
                byte[] temp = Ipv4AddrUtil.ipv4ToBytes(groups[ig]);
                bytes[ib--] = temp[4];
                bytes[ib--] = temp[3];
                bytes[ib--] = temp[2];
                bytes[ib--] = temp[1];
                comFlag = true;
            } else if ("".equals(groups[ig])) {
                int zlg = 9 - (groups.length + (comFlag ? 1 : 0));
                while (zlg-- > 0) {
                    bytes[ib--] = 0;
                    bytes[ib--] = 0;
                }
            } else {
                int temp = Integer.parseInt(groups[ig], 16);
                bytes[ib--] = (byte) temp;
                bytes[ib--] = (byte) (temp >> 8);
            }
        }

        return new BigInteger(bytes);
    }
}
