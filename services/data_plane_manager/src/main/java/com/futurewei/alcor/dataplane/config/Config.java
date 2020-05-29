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
package com.futurewei.alcor.dataplane.config;

import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Config {

    public static boolean isOVS=true;
    public static FileWriter TIME_STAMP_FILE;
    public static BufferedWriter TIME_STAMP_WRITER;
    public static String LOG_FILE_PATH = "timestamp.log";

    public static long TOTAL_TIME = 0;
    public static int TOTAL_REQUEST = 0;
    public static long MAX_TIME = Long.MIN_VALUE;
    public static long MIN_TIME = Long.MAX_VALUE;
    public static long APP_START_TS = 0;


    static {
        try {
            File file = new File(LOG_FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();
            }

            TIME_STAMP_FILE = new FileWriter(file);
            TIME_STAMP_WRITER = new BufferedWriter(TIME_STAMP_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String PRODUCER_CLIENT_ID = "vpc_controller_p2";
    // :ae:c8:FF:FF";
    public static List<HostInfo> epHosts = null;
    public static int EP_PER_HOST = 1;

}
