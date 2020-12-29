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
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.port.BindingProfile;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.lang.reflect.Field;
import java.util.*;

public class PortProcessor extends AbstractProcessor {
    private InternalPortEntity buildInternalPortEntity(PortEntity portEntity) {
        InternalPortEntity internalPortEntity =
                new InternalPortEntity(portEntity, null, null);
        NetworkConfig.ExtendPortEntity extendPortEntity =
                new NetworkConfig.ExtendPortEntity(internalPortEntity, portEntity.getBindingHostId());

        return extendPortEntity;
    }

    private void buildInternalPortEntities(PortContext context, List<PortEntity> portEntities) {
        List<InternalPortEntity> internalPortEntities = new ArrayList<>();

        for (PortEntity portEntity: portEntities) {
            if (portEntity.getBindingHostId() != null) {
                internalPortEntities.add(buildInternalPortEntity(portEntity));
            }
        }

        context.getNetworkConfig().setPortEntities(internalPortEntities);
    }

    @Override
    void createProcess(PortContext context) {
        for (PortEntity portEntity: context.getPortEntities()) {
            portEntity.setProjectId(context.getProjectId());
            if (portEntity.getId() == null) {
                portEntity.setId(UUID.randomUUID().toString());
            }
        }

        buildInternalPortEntities(context, context.getPortEntities());
    }

    @Override
    void updateProcess(PortContext context) throws Exception {
        PortEntity newPortEntity = context.getNewPortEntity();
        PortEntity oldPortEntity = context.getOldPortEntity();

        /**
         * Fields with values of null in the new port entity are populated
         * with the corresponding fields in the old port entity.
         */
        List<Field> allFields = new ArrayList<>();
        Class entityClass = PortEntity.class;
        while (entityClass != null) {
            allFields.addAll(Arrays.asList(entityClass.getDeclaredFields()));
            entityClass = entityClass.getSuperclass();
        }

        for (Field field: allFields) {
            field.setAccessible(true);
            Object oldValue = field.get(oldPortEntity);
            Object newValue = field.get(newPortEntity);
            if (newValue == null) {
                field.set(newPortEntity, oldValue);
            }
        }

        buildInternalPortEntities(context, Collections.singletonList(newPortEntity));
    }

    @Override
    void deleteProcess(PortContext context) {
        buildInternalPortEntities(context, context.getPortEntities());
    }
}
