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
package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import lombok.Data;

@Data
public class InternalSubnetEntity extends SubnetEntity {
    @JsonProperty("tunnel_id")
    private Long tunnelId;

    public InternalSubnetEntity()
    {
    }
    public InternalSubnetEntity(SubnetEntity subnetEntity, Long tunnelId) {
        super(subnetEntity);
        this.tunnelId = tunnelId;
    }

    public Long getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(Long tunnelId) {
        this.tunnelId = tunnelId;
    }
}
