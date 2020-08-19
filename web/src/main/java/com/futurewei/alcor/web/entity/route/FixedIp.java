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
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;

public class FixedIp {

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("ip_address")
    private String ipAddress;

    public FixedIp() {

    }

    public FixedIp(String subnetId, String ipAddress) {
        this.subnetId = subnetId;
        this.ipAddress = ipAddress;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "FixedIp{" +
                "subnetId='" + subnetId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof PortEntity.FixedIp)) {
            return false;
        }

        PortEntity.FixedIp fixedIp = (PortEntity.FixedIp)object;

        return this.subnetId == fixedIp.getSubnetId() && this.ipAddress == fixedIp.getIpAddress();
    }

}
