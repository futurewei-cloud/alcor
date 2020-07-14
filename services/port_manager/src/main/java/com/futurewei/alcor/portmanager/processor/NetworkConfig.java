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
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.List;

public class NetworkConfig {
    private List<ExtendPortEntity> portEntities;

    private List<VpcEntity> vpcEntities;

    private List<InternalSubnetEntity> subnetEntities;

    private List<SecurityGroup> securityGroups;

    public static class ExtendPortEntity extends PortEntity {
        private InternalPortEntity internalPortEntity;
        private String bindingHostId;

        public ExtendPortEntity(InternalPortEntity internalPortEntity, String bindingHostId) {
            this.internalPortEntity = internalPortEntity;
            this.bindingHostId = bindingHostId;
        }

        public InternalPortEntity getInternalPortEntity() {
            return internalPortEntity;
        }

        public void setInternalPortEntity(InternalPortEntity internalPortEntity) {
            this.internalPortEntity = internalPortEntity;
        }

        public String getBindingHostId() {
            return bindingHostId;
        }

        public void setBindingHostId(String bindingHostId) {
            this.bindingHostId = bindingHostId;
        }
    }

    public NetworkConfig() {
    }

    public List<ExtendPortEntity> getPortEntities() {
        return portEntities;
    }

    public void setPortEntities(List<ExtendPortEntity> portEntities) {
        this.portEntities = portEntities;
    }

    public List<VpcEntity> getVpcEntities() {
        return vpcEntities;
    }

    public void setVpcEntities(List<VpcEntity> vpcEntities) {
        this.vpcEntities = vpcEntities;
    }

    public List<InternalSubnetEntity> getSubnetEntities() {
        return subnetEntities;
    }

    public void setSubnetEntities(List<InternalSubnetEntity> subnetEntities) {
        this.subnetEntities = subnetEntities;
    }

    public List<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }
}
