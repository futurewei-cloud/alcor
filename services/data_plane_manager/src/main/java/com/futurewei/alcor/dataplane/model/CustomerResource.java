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

package com.futurewei.alcor.dataplane.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerResource implements Serializable {

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    public CustomerResource() {

    }

    public CustomerResource(CustomerResource state) {
        this(state.projectId, state.id, state.name, state.description);
    }

    public CustomerResource(String projectId, String id, String name, String description) {
        this.projectId = projectId;
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
