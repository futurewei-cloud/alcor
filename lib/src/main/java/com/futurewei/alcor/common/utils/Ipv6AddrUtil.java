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
package com.futurewei.alcor.common.utils;

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

    public static boolean ipv6PrefixCheck(String strIpv6) {
       List<String> patternList = ImmutableList.of("^(?:((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}"
                + "((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|"
                + "(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}"
                + "(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))"
                + "(/(([0-9])|([0-9]{2})|(1[0-1][0-9])|(12[0-8]))))$",
                "^(?:(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|"
                + "((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)"
                + "(/.+))$");

        Pattern[] patterns = new Pattern[patternList.size()];

        for (int i = 0; i < patternList.size(); ++i) {
           patterns[i] = Pattern.compile(patternList.get(i));
        }

        for (int i = 0; i < patterns.length; ++i) {
            if (!patterns[i].matcher(strIpv6).matches()) {
                return false;
            }
        }

        return true;
    }
}
