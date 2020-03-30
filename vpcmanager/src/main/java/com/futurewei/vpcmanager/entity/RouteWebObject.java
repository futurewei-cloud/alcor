package com.futurewei.vpcmanager.entity;

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
}
