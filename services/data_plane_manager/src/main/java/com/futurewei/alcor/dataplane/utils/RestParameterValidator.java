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
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static void checkOperationType(OperationType opType) throws OperationTypeInvalid {
        Set<OperationType> operationTypes = new HashSet<>(Arrays.asList(OperationType.values()));
        for (OperationType operationType: operationTypes) {
            if (operationType.equals(opType)) {
                return;
            }
        }

        throw new OperationTypeInvalid();
    }

    public static void checkNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        checkOperationType(networkConfig.getOpType());
        checkVpcEntities(networkConfig.getVpcs());
        checkSubnetEntities(networkConfig.getSubnets());
        checkPortEntities(networkConfig.getPortEntities());
    }
}
