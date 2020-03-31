package com.futurewei.route.entity;

import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RouteState extends CustomerResource {

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

    public RouteState() {

    }

    public RouteState(String projectId, String Id, String name, String description,
                      String destination, String target, Integer priority, RouteTableType type, String tableId) {
        super(projectId, Id, name, "");
        this.destination = destination;
        this.target = target;
        this.priority = priority;
        this.associatedType = type;
        this.associatedTableId = tableId;
    }
}
