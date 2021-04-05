package com.futurewei.alcor.netwconfigmanager.entity;

import com.futurewei.alcor.schema.Goalstate;

import java.util.ArrayList;
import java.util.List;

public class ResourceMeta {

    private String ownerId;
    private List<Goalstate.ResourceIdType> resources;

    public ResourceMeta(String ownerId, List<Goalstate.ResourceIdType> resources) {
        this.ownerId = ownerId;
        this.resources = new ArrayList<>(resources);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<Goalstate.ResourceIdType> getResources() {
        return this.resources;
    }
}
