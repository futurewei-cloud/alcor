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
import com.futurewei.alcor.common.enumClass.OperationType;

public class InternalRouterInfo {
    @JsonProperty("operation_type")
    private OperationType operation_type;

    @JsonProperty("configuration")
    private InternalRouterConfiguration configuration;

    public InternalRouterInfo() {

    }

    public InternalRouterInfo(OperationType operation_type, InternalRouterConfiguration configuration) {
        this.operation_type = operation_type;
        this.configuration = configuration;
    }

    public OperationType getOperationType() { return this.operation_type; }
    public void setOperationType(OperationType operation_type) { this.operation_type = operation_type; }

    public InternalRouterConfiguration getRouterConfiguration() { return this.configuration; }
    public void setRouterConfiguration(InternalRouterConfiguration configuration) {
        this.configuration = configuration;
    }
}
