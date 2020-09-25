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

import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class DataPlaneManagerUtil {

    public static ExclusionStrategy myExclusionStrategy =
            new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fa) {
                    return fa.getName().equals("tenantId");
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            };
    public static Gson gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy)
            .enableComplexMapKeySerialization()
            .create();

    public static String autoGenerateUTsInput(int operationType, int resourceType) {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();

        // set operationType and
        networkConfiguration.setOpType(Common.OperationType.forNumber(operationType));
        networkConfiguration.setRsType(Common.ResourceType.forNumber(resourceType));

        List<InternalPortEntity> portEntities = new ArrayList<>();
        InternalPortEntity port = new InternalPortEntity();
        port.setBindingHostIP("bindingHostIP");
        portEntities.add(port);
        networkConfiguration.setPortEntities(portEntities);

        List<VpcEntity> vpcs = new ArrayList<>();
        VpcEntity vpc = new VpcEntity();
        vpcs.add(vpc);
        networkConfiguration.setVpcs(vpcs);

        List<InternalSubnetEntity> subnets = new ArrayList<>();
        InternalSubnetEntity subnet = new InternalSubnetEntity();
        subnets.add(subnet);
        networkConfiguration.setSubnets(subnets);

        List<SecurityGroup> securityGroups = new ArrayList<>();
        SecurityGroup securityGroup = new SecurityGroup();
        networkConfiguration.setSecurityGroups(securityGroups);


        String input = gson.toJson(networkConfiguration);
        return input;
    }

    public static String autoGenerateUTsOutput() {
        return null;
    }

}
