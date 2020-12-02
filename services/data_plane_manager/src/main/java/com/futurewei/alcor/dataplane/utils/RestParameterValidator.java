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
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.util.StringUtils;

import java.util.List;

public class RestParameterValidator {

    public static void checkVpcEntities(List<VpcEntity> vpcEntities) throws Exception {
        if (vpcEntities != null) {
            for (VpcEntity vpcEntity: vpcEntities) {
                if (StringUtils.isEmpty(vpcEntity.getId())) {
                    throw new VpcIdRequired();
                }

                if (StringUtils.isEmpty(vpcEntity.getProjectId())) {
                    throw new ProjectIdRequired();
                }
            }
        }
    }

    public static void checkSubnetEntities(List<InternalSubnetEntity> subnetEntities) throws Exception {
        if (subnetEntities != null) {
            for (InternalSubnetEntity subnetEntity: subnetEntities) {
                if (StringUtils.isEmpty(subnetEntity.getId())) {
                    throw new SubnetIdNotRequired();
                }

                if (StringUtils.isEmpty(subnetEntity.getVpcId())) {
                    throw new VpcIdRequired();
                }

                if (StringUtils.isEmpty(subnetEntity.getProjectId())) {
                    throw new ProjectIdRequired();
                }

                if (StringUtils.isEmpty(subnetEntity.getName())) {
                    throw new SubnetNameRequired();
                }

                if (StringUtils.isEmpty(subnetEntity.getCidr())) {
                    throw new SubnetCidrRequired();
                }

                if (StringUtils.isEmpty(subnetEntity.getGatewayIp())) {
                    throw new SubnetGatewayIpRequired();
                }

                if (StringUtils.isEmpty(subnetEntity.getGatewayPortDetail().getGatewayMacAddress())) {
                    throw new SubnetGatewayMacRequired();
                }
            }
        }
    }

    public static void checkPortEntities(List<InternalPortEntity> portEntities) throws Exception {
        if (portEntities != null) {
            for (InternalPortEntity portEntity: portEntities) {
                if (StringUtils.isEmpty(portEntity.getId())) {
                    throw new PortIdRequired();
                }

                if (StringUtils.isEmpty(portEntity.getProjectId())) {
                    throw new ProjectIdRequired();
                }

                if (StringUtils.isEmpty(portEntity.getVpcId())) {
                    throw new VpcIdRequired();
                }

                if (StringUtils.isEmpty(portEntity.getMacAddress())) {
                    throw new PortMacAddressRequired();
                }

                if (portEntity.getFixedIps() == null || portEntity.getFixedIps().size() == 0) {
                    throw new PortFixedIpRequired();
                }

                for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
                    if (StringUtils.isEmpty(fixedIp.getSubnetId()) ||
                            StringUtils.isEmpty(fixedIp.getIpAddress())) {
                        throw new PortFixedIpInvalid();
                    }
                }

                /*
                if (portEntity.getSecurityGroups() == null || portEntity.getSecurityGroups().size() == 0) {
                    //
                }

                for (String securityGroupId: portEntity.getSecurityGroups()) {
                    if (StringUtils.isEmpty(securityGroupId)) {
                        //
                    }
                }*/
            }
        }
    }

    public static void checkNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        checkVpcEntities(networkConfig.getVpcs());
        checkSubnetEntities(networkConfig.getSubnets());
        checkPortEntities(networkConfig.getPortEntities());
    }
}
