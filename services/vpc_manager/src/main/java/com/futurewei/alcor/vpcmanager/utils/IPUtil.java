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
