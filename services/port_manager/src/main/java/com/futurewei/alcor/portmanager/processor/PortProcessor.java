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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PortProcessor extends AbstractProcessor {
    private void updateName(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newName = newPortEntity.getName();
        String oldName = oldPortEntity.getName();

        if (newName != null && !newName.equals(oldName)) {
            oldPortEntity.setName(newName);
        }
    }

    private void updateAdminState(PortEntity newPortEntity, PortEntity oldPortEntity) {
        boolean newAdminStateUp = newPortEntity.isAdminStateUp();
        boolean oldAdminStateUp = oldPortEntity.isAdminStateUp();

        if (newAdminStateUp != oldAdminStateUp) {
            oldPortEntity.setAdminStateUp(newAdminStateUp);
        }
    }

    private void updateBindingHostId(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newBindingHostId = newPortEntity.getBindingHostId();
        String oldBindingHostId = oldPortEntity.getBindingHostId();

        if (newBindingHostId != null && !newBindingHostId.equals(oldBindingHostId)) {
            oldPortEntity.setBindingHostId(newBindingHostId);
        }
    }

    private void updateBindingProfile(PortEntity newPortEntity, PortEntity oldPortEntity) {
        BindingProfile newBindingProfile = newPortEntity.getBindingProfile();
        BindingProfile oldBindingProfile = oldPortEntity.getBindingProfile();

        if (newBindingProfile != null && !newBindingProfile.equals(oldBindingProfile)) {
            oldPortEntity.setBindingProfile(newBindingProfile);
        }
    }

    private void updateBindingVnicType(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newBindingVnicType = newPortEntity.getBindingVnicType();
        String oldBindingVnicType = oldPortEntity.getBindingVnicType();

        if (newBindingVnicType != null && !newBindingVnicType.equals(oldBindingVnicType)) {
            oldPortEntity.setBindingVnicType(newBindingVnicType);
        }
    }

    private void updateDescription(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newDescription = newPortEntity.getDescription();
        String oldDescription = oldPortEntity.getDescription();

        if (newDescription != null && !newDescription.equals(oldDescription)) {
            oldPortEntity.setDescription(newDescription);
        }
    }

    private void updateDeviceId(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newDeviceId = newPortEntity.getDeviceId();
        String oldDeviceId = oldPortEntity.getDeviceId();

        if (newDeviceId != null && !newDeviceId.equals(oldDeviceId)) {
            oldPortEntity.setDeviceId(newDeviceId);
        }
    }

    private void updateDeviceOwner(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newDeviceOwner = newPortEntity.getDeviceOwner();
        String oldDeviceOwner = oldPortEntity.getDeviceOwner();

        if (newDeviceOwner != null && !newDeviceOwner.equals(oldDeviceOwner)) {
            oldPortEntity.setDeviceOwner(newDeviceOwner);
        }
    }

    private void updateDnsDomain(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newDnsDomain = newPortEntity.getDnsDomain();
        String oldDnsDomain = oldPortEntity.getDnsDomain();

        if (newDnsDomain != null && !newDnsDomain.equals(oldDnsDomain)) {
            oldPortEntity.setDnsDomain(newDnsDomain);
        }
    }

    private void updateDnsName(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newDnsName = newPortEntity.getDnsName();
        String oldDnsName = oldPortEntity.getDnsName();

        if (newDnsName != null && !newDnsName.equals(oldDnsName)) {
            oldPortEntity.setDnsName(newDnsName);
        }
    }

    private void updateExtraDhcpOpts(PortEntity newPortEntity, PortEntity oldPortEntity) {
        List<PortEntity.ExtraDhcpOpt> newExtraDhcpOpts = newPortEntity.getExtraDhcpOpts();
        List<PortEntity.ExtraDhcpOpt> oldExtraDhcpOpts = oldPortEntity.getExtraDhcpOpts();

        if (newExtraDhcpOpts != null && !newExtraDhcpOpts.equals(oldExtraDhcpOpts)) {
            oldPortEntity.setExtraDhcpOpts(newExtraDhcpOpts);
        }
    }

    private void updateAllowedAddressPairs(PortEntity newPortEntity, PortEntity oldPortEntity) {
        List<PortEntity.AllowAddressPair> newAllowedAddressPairs = newPortEntity.getAllowedAddressPairs();
        List<PortEntity.AllowAddressPair> oldAllowedAddressPairs = oldPortEntity.getAllowedAddressPairs();

        if (newAllowedAddressPairs != null && !newAllowedAddressPairs.equals(oldAllowedAddressPairs)) {
            oldPortEntity.setAllowedAddressPairs(newAllowedAddressPairs);
        }
    }

    private void updatePortSecurityEnabled(PortEntity newPortEntity, PortEntity oldPortEntity) {
        boolean newPortSecurityEnabled = newPortEntity.isPortSecurityEnabled();
        boolean oldPortSecurityEnabled = oldPortEntity.isPortSecurityEnabled();

        if (newPortSecurityEnabled != oldPortSecurityEnabled) {
            oldPortEntity.setPortSecurityEnabled(newPortSecurityEnabled);
        }
    }

    private void updateQosPolicyId(PortEntity newPortEntity, PortEntity oldPortEntity) {
        String newQosPolicyId = newPortEntity.getQosPolicyId();
        String oldQosPolicyId = oldPortEntity.getQosPolicyId();

        if (newQosPolicyId != null && !newQosPolicyId.equals(oldQosPolicyId)) {
            oldPortEntity.setQosPolicyId(newQosPolicyId);
        }
    }


    private void updateMacLearningEnabled(PortEntity newPortEntity, PortEntity oldPortEntity) {
        boolean newMacLearningEnabled = newPortEntity.isMacLearningEnabled();
        boolean oldMacLearningEnabled = oldPortEntity.isMacLearningEnabled();

        if (newMacLearningEnabled != oldMacLearningEnabled) {
            oldPortEntity.setMacLearningEnabled(newMacLearningEnabled);
        }
    }

    private InternalPortEntity buildInternalPortEntity(PortEntity portEntity) {
        InternalPortEntity internalPortEntity =
                new InternalPortEntity(portEntity, null, null, null, null);
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
    void updateProcess(PortContext context) {
        PortEntity newPortEntity = context.getNewPortEntity();
        PortEntity oldPortEntity = context.getOldPortEntity();

        updateName(newPortEntity, oldPortEntity);

        updateAdminState(newPortEntity, oldPortEntity);

        updateBindingHostId(newPortEntity, oldPortEntity);

        updateBindingProfile(newPortEntity, oldPortEntity);

        updateBindingVnicType(newPortEntity, oldPortEntity);

        updateDescription(newPortEntity, oldPortEntity);

        updateDeviceId(newPortEntity, oldPortEntity);

        updateDeviceOwner(newPortEntity, oldPortEntity);

        updateDnsDomain(newPortEntity, oldPortEntity);

        updateDnsName(newPortEntity, oldPortEntity);

        updateExtraDhcpOpts(newPortEntity, oldPortEntity);

        updateAllowedAddressPairs(newPortEntity, oldPortEntity);

        updatePortSecurityEnabled(newPortEntity, oldPortEntity);

        updateQosPolicyId(newPortEntity, oldPortEntity);

        updateMacLearningEnabled(newPortEntity, oldPortEntity);

        buildInternalPortEntities(context, Collections.singletonList(oldPortEntity));
    }

    @Override
    void deleteProcess(PortContext context) {
        buildInternalPortEntities(context, context.getPortEntities());
    }
}
