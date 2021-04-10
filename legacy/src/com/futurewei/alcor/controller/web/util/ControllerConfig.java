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

package com.futurewei.alcor.controller.web.util;

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.DataCenterConfig;

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
