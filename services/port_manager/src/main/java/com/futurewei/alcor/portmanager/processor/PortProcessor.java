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
