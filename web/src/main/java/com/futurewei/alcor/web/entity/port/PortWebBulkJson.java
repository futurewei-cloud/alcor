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
package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PortWebBulkJson {
    @JsonProperty("ports")
    private List<PortEntity> portEntities;

    public PortWebBulkJson() {
    }

    public PortWebBulkJson(List<PortEntity> portEntities) {
        this.portEntities = portEntities;
    }

    public List<PortEntity> getPortEntities() {
        return portEntities;
    }

    public void setPortEntities(List<PortEntity> portEntities) {
        this.portEntities = portEntities;
    }
}
