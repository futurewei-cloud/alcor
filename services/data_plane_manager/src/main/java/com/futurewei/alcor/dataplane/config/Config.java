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

import com.futurewei.alcor.dataplane.service.NodeManager;
import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.futurewei.alcor.schema.Subnet.SubnetState;

public class Config {

    public static String projectId = "3dda2801-d675-4688-a63f-dcda8d327f50";
    public static String subnetId = "a87e0f87-a2d9-44ef-9194-9a62f178594e";
    private static int InitNumOfTransitSwitch = 3;
    private static int InitNumOfTransitRouter = 1;
    public static final int THREADS_LIMIT = 100;
    public static boolean IS_PARALLEL = true;
    public static String HOST_ID_PREFIX = "es8-";
    public static long Tunnel_Id = 3000;

    public static FileWriter TIME_STAMP_FILE;
    public static BufferedWriter TIME_STAMP_WRITER;
    public static String LOG_FILE_PATH = "timestamp.log";

    public static long TOTAL_TIME = 0;
    public static int TOTAL_REQUEST = 0;
    public static long MAX_TIME = Long.MIN_VALUE;
    public static long MIN_TIME = Long.MAX_VALUE;
    public static long APP_START_TS = 0;

    public static String gRPCServerIp = "172.17.0.1";

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

    public static String VETH_NAME = "veth0";
    public static SubnetState customerSubnetState =
            SubnetState.newBuilder().build();
    public static String PRODUCER_CLIENT_ID = "vpc_controller_p2";
    public static String GATEWAY_MAC_ADDRESS = "02:42:ac:11:00:0d"; //"0e:73
    // :ae:c8:FF:FF";
    public static List<HostInfo> epHosts = null;
    public static int EP_PER_HOST = 1;

    public static HostInfo[] transitSwitchHosts =
            (NodeManager.nodeManager == null ? null :
                    NodeManager.nodeManager.getRandomHosts(Config.InitNumOfTransitSwitch));
    public static HostInfo[] transitRouterHosts =
            (NodeManager.nodeManager == null ? null :
                    NodeManager.nodeManager.getRandomHosts(Config.InitNumOfTransitRouter));

    public static int epCounter = 0;
}
