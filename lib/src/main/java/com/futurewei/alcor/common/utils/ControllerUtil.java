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

public class ControllerUtil {

    private static String GenereateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenereateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }

    public static boolean isPositive(String val) {
        if (val == null || val.length() == 0) {
            return false;
        }
        int n = val.length();
        if (n == 0 || n > 3) {  // ex. 127.0.0.00001 is wrong
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (!Character.isDigit(val.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
