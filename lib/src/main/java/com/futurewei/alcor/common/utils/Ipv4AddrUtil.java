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

package com.futurewei.alcor.common.utils;

public class Ipv4AddrUtil {

    public static boolean formatCheck(String strIpv4) {
        if (strIpv4 != null && !strIpv4.isEmpty()) {

            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." +
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

            if (strIpv4.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public static long ipv4ToLong(String strIpv4) {
        String[] ip = strIpv4.split("\\.");
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
    }

    public static String longToIpv4(long longIpv4) {
        return ((longIpv4 >> 24) & 0xFF) +
                "." + ((longIpv4 >> 16) & 0xFF) +
                "." + ((longIpv4 >> 8) & 0xFF) +
                "." + (longIpv4 & 0xFF);
    }

    public static byte[] ipv4ToBytes(String strIpv4) {
        byte[] result = new byte[5];
        result[0] = 0;

        int pos1 = strIpv4.indexOf(".");
        int pos2 = strIpv4.indexOf(".", pos1 + 1);
        int pos3 = strIpv4.indexOf(".", pos2 + 1);

        result[1] = (byte) Integer.parseInt(strIpv4.substring(0, pos1));
        result[2] = (byte) Integer.parseInt(strIpv4.substring(pos1 + 1, pos2));
        result[3] = (byte) Integer.parseInt(strIpv4.substring(pos2 + 1, pos3));
        result[4] = (byte) Integer.parseInt(strIpv4.substring(pos3 + 1));

        return result;
    }
}
