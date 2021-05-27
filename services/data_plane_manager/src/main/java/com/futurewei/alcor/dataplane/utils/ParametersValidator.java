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
