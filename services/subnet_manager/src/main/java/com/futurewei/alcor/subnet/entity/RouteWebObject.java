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

package com.futurewei.alcor.subnet.entity;

import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class RouteWebObject extends CustomerResource {

    @NotNull
    private String destination;

    @NotNull
    private String target;

    @NotNull
    private Integer priority;

    @NotNull
    private RouteTableType associatedType;

    @NotNull
    private String associatedTableId;

    public RouteWebObject() {

    }

    public RouteWebObject(String projectId, String Id, String name, String description,
                      String destination, String target, Integer priority, RouteTableType type, String tableId) {
        super(projectId, Id, name, "");
        this.destination = destination;
        this.target = target;
        this.priority = priority;
        this.associatedType = type;
        this.associatedTableId = tableId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public RouteTableType getAssociatedType() {
        return associatedType;
    }

    public void setAssociatedType(RouteTableType associatedType) {
        this.associatedType = associatedType;
    }

    public String getAssociatedTableId() {
        return associatedTableId;
    }

    public void setAssociatedTableId(String associatedTableId) {
        this.associatedTableId = associatedTableId;
    }
}
