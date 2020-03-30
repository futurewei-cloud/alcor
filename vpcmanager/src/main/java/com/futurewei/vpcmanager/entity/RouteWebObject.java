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
