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

package com.futurewei.alcor.vpcmanager.utils;

public class IPUtil {

    public static Integer countIpNumberByStartIpAndEndIp(String startIp, String endIp) {
        String[] startIps = startIp.split("\\.");
        String[] endIps = endIp.split("\\.");
        int ip1 = 0;
        int ip2 = 0;
        int ip3 = 0;
        int ip4 = 0;
        String ip = "";
        int count  = 0;
        String writeLine = "";
        for (ip1 = Integer.parseInt(startIps[0]); ip1 < (Integer.parseInt(endIps[0]) + 1); ip1++) {
            if (ip1 == Integer.parseInt(endIps[0])) {
                for (ip2 = Integer.parseInt(startIps[1]); ip2 < (Integer.parseInt(endIps[1]) + 1); ip2++) {
                    if (ip2 == Integer.parseInt(endIps[1])) {
                        for (ip3 = Integer.parseInt(startIps[2]); ip3 < (Integer.parseInt(endIps[2]) + 1); ip3++) {
                            if (ip3 == Integer.parseInt(endIps[2])) {
                                for (ip4 = Integer.parseInt(startIps[3]); ip4 < (Integer.parseInt(endIps[3]) + 1); ip4++) {
                                    count ++;
                                }
                            } else {
                                for (ip4 = 0; ip4 < 256; ip4++) {
                                    count ++;
                                }
                            }
                        }
                    } else {
                        for (ip3 = 0; ip3 < 256; ip3++) {
                            for (ip4 = 0; ip4 < 256; ip4++) {
                                count ++;
                            }
                        }
                    }
                }
            } else {
                for (ip2 = 0; ip2 < 256; ip2++) {
                    for (ip3 = 0; ip3 < 256; ip3++) {
                        for (ip4 = 0; ip4 < 256; ip4++) {
                            count ++;
                        }
                    }
                }
            }
        }
        return count;
    }

}
