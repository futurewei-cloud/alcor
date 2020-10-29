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

import com.futurewei.alcor.dataplane.exception.*;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

public class ParametersValidator {

    public static void checkResourceType (Common.ResourceType rsType) throws Exception{
        if (rsType != null) {
            throw new ResourceTypeNotValid();
        }
    }

    public static void checkOperationType (Common.OperationType opType) throws Exception{
        if (opType != null) {
            throw new OperationTypeNotValid();
        }
    }

    public static void checkBindingHostIP(InternalPortEntity port) throws Exception{
        String bindingHostIP = port.getBindingHostIP();
        if (isEmptyString(bindingHostIP)) {
            throw new BindingHostIPNotFound(port.getId());
        }
    }

    public static void checkVpcIdInVpcsInternal(VpcEntity vpc) throws Exception{
        String vpcId = vpc.getId();
        if (isEmptyString(vpcId)) {
            throw new VpcIdNotFound();
        }
    }

    public static void checkTunnelId(InternalSubnetEntity subnet) throws Exception{
        Long tunnelId = subnet.getTunnelId();
        if (tunnelId == null) {
            throw new TunnelIdNotFound(subnet.getId());
        }
    }

    public static void checkHostIp(NeighborInfo neighborInfo) throws Exception{
        String hostIp = neighborInfo.getHostIp();
        if (isEmptyString(hostIp)) {
            throw new HostIPNotFound();
        }
    }

    public static void checkHostId(NeighborInfo neighborInfo) throws Exception{
        String hostId = neighborInfo.getHostId();
        if (isEmptyString(hostId)) {
            throw new HostIdNotFound();
        }
    }

    public static void checkPortIp(NeighborInfo neighborInfo) throws Exception{
        String portIp = neighborInfo.getPortIp();
        if (isEmptyString(portIp)) {
            throw new PortIPNotFound();
        }
    }

    public static void checkPortMac(NeighborInfo neighborInfo) throws Exception{
        String portMac = neighborInfo.getPortMac();
        if (isEmptyString(portMac)) {
            throw new PortMacNotFound();
        }
    }

    public static void checkPortId(NeighborInfo neighborInfo) throws Exception{
        String portId = neighborInfo.getPortId();
        if (isEmptyString(portId)) {
            throw new PortIdNotFound();
        }
    }

    public static void checkVpcIdInNeighborInfo(NeighborInfo neighborInfo) throws Exception{
        String vpcId = neighborInfo.getVpcId();
        if (isEmptyString(vpcId)) {
            throw new VpcIdNotFound();
        }
    }

    public static void checkSubnetId(NeighborInfo neighborInfo) throws Exception{
        String subnetId = neighborInfo.getSubnetId();
        if (isEmptyString(subnetId)) {
            throw new SubnetIdNotFound();
        }
    }

    public static boolean isEmptyString(String string) {
        return string == null || string.isEmpty();
    }

}
