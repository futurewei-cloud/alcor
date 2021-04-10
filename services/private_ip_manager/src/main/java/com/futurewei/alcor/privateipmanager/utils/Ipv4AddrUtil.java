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

package com.futurewei.alcor.privateipmanager.utils;

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
