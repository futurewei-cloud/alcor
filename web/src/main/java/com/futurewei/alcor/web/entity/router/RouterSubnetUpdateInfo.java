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
package com.futurewei.alcor.web.entity.router;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RouterSubnetUpdateInfo {
    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("operation_type")
    private OperationType operationType;

    @JsonProperty("old_subnet_ids")
    private List<String> oldSubnetIds;

    public enum OperationType {
        ADD("add"),
        DELETE("delete");

        private String type;

        OperationType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public RouterSubnetUpdateInfo() {

    }

    public RouterSubnetUpdateInfo(String vpcId, String subnetId, OperationType operationType, List<String> oldSubnetIds) {
        this.vpcId = vpcId;
        this.subnetId = subnetId;
        this.operationType = operationType;
        this.oldSubnetIds = oldSubnetIds;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public List<String> getOldSubnetIds() {
        return oldSubnetIds;
    }

    public void setOldSubnetIds(List<String> oldSubnetIds) {
        this.oldSubnetIds = oldSubnetIds;
    }
}
