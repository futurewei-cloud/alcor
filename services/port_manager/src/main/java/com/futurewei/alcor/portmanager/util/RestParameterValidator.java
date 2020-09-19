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
package com.futurewei.alcor.portmanager.util;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.web.entity.port.IpAllocation;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.VifType;
import com.futurewei.alcor.web.entity.port.VnicType;
import com.futurewei.alcor.web.entity.route.RouterUpdateInfo;
import org.springframework.util.StringUtils;

import java.util.*;

public class RestParameterValidator {
    public static void checkMacAddress(PortEntity portEntity) throws Exception {
        String macAddress = portEntity.getMacAddress();
        if (macAddress != null) {
            String regex = "([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}";
            if (!macAddress.matches(regex)) {
                throw new MacAddressInvalid();
            }
        }
    }

    public static void checkFixedIps(PortEntity portEntity) throws Exception {
        List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
        if (fixedIps != null) {
            for (PortEntity.FixedIp fixedIp: fixedIps) {
                if (fixedIp.getSubnetId() == null && fixedIp.getIpAddress() == null) {
                    throw new FixedIpsInvalid();
                }

                if (fixedIp.getSubnetId() != null && "".equals(fixedIp.getSubnetId())) {
                    throw new FixedIpsInvalid();
                }

                if (fixedIp.getIpAddress() != null &&
                        !Ipv4AddrUtil.formatCheck(fixedIp.getIpAddress())
                        && !Ipv6AddrUtil.formatCheck(fixedIp.getIpAddress()) ) {
                    throw new FixedIpsInvalid();
                }
            }
        }
    }

    public static void checkBindingProfile(PortEntity portEntity) {

    }

    public static void checkBindingVifDetails(PortEntity portEntity) {

    }

    public static void checkBindingVifType(PortEntity portEntity) throws VifTypeInvalid {
        if (portEntity.getBindingVifType() != null) {
            Set<VifType> vifTypeSet = new HashSet<>(Arrays.asList(VifType.values()));
            for (VifType vifType: vifTypeSet) {
                if (vifType.getVifType().equals(portEntity.getBindingVifType())) {
                    return;
                }
            }

            throw new VifTypeInvalid();
        }
    }

    public static void checkBindingVnicType(PortEntity portEntity) throws VnicTypeInvalid {
        if (portEntity.getBindingVnicType() != null) {
            Set<VnicType> vnicTypeSet = new HashSet<>(Arrays.asList(VnicType.values()));
            for (VnicType vnicType: vnicTypeSet) {
                if (vnicType.getVnicType().equals(portEntity.getBindingVnicType())) {
                    return;
                }
            }

            throw new VnicTypeInvalid();
        }
    }

    public static void checkIpAllocation(PortEntity portEntity) throws IpAllocationInvalid {
        if (portEntity.getIpAllocation() != null) {
            Set<IpAllocation> ipAllocationSet = new HashSet<>(Arrays.asList(IpAllocation.values()));
            for (IpAllocation ipAllocation: ipAllocationSet) {
                if (ipAllocation.getIpAllocation().equals(portEntity.getIpAllocation())) {
                    return;
                }
            }

            throw new IpAllocationInvalid();
        }
    }

    public static void checkPort(PortEntity portEntity) throws Exception {
        //Check mac address
        checkMacAddress(portEntity);

        //Check FixedIps
        checkFixedIps(portEntity);

        //Check binding profile
        checkBindingProfile(portEntity);

        //Check binding vif details
        checkBindingVifDetails(portEntity);

        //Check binding vif type
        checkBindingVifType(portEntity);

        //Check binding vif type
        checkBindingVnicType(portEntity);

        //Check ip allocation
        checkIpAllocation(portEntity);
    }

    public static void checkRouterSubnetUpdateInfo(RouterUpdateInfo routerUpdateInfo) throws Exception {
        if (StringUtils.isEmpty(routerUpdateInfo.getVpcId())) {
            throw new VpcIdInvalid();
        }

        if (StringUtils.isEmpty(routerUpdateInfo.getSubnetId())) {
            throw new SubnetIdInvalid();
        }

        RouterUpdateInfo.OperationType operationType = routerUpdateInfo.getOperationType();
        if (!RouterUpdateInfo.OperationType.ADD.equals(operationType) &&
                !RouterUpdateInfo.OperationType.DELETE.equals(operationType)) {
            throw new OperationTypeInvalid();
        }

        if (routerUpdateInfo.getOldSubnetIds() == null) {
            routerUpdateInfo.setOldSubnetIds(new ArrayList<>());
        }
    }
}
