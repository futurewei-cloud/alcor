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
package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.dataplane.model.HostInfo;
import com.futurewei.alcor.dataplane.model.SubnetState;
import com.futurewei.alcor.dataplane.service.nodemgmt.DataCenterConfig;

public class ControllerConfig {

    public static String projectId = "3dda2801-d675-4688-a63f-dcda8d327f50";
    public static String vpcId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88038";
    public static String subnetId = "a87e0f87-a2d9-44ef-9194-9a62f178594e";
    private static int InitNumOfTransitSwitch = 3;
    private static int InitNumOfTransitRouter = 1;

    public static SubnetState customerSubnetState = new SubnetState(
            ControllerConfig.projectId,
            ControllerConfig.vpcId,
            ControllerConfig.subnetId,
            "Subnet1",
            "10.0.0.0/20",
            "10.0.0.5");

    public static HostInfo[] transitSwitchHosts = (DataCenterConfig.nodeManager == null ? null : DataCenterConfig.nodeManager.getRandomHosts(ControllerConfig.InitNumOfTransitSwitch));
    public static HostInfo[] transitRouterHosts = (DataCenterConfig.nodeManager == null ? null : DataCenterConfig.nodeManager.getRandomHosts(ControllerConfig.InitNumOfTransitRouter));

    public static int epCounter = 0;
}
