package com.futurewei.alcor.netwconfigmanager.entity;

import java.util.HashMap;
import java.util.List;

public class VpcResourceMeta {

    private String vni;

    // Private IP => List<ResourceMetadata>
    private HashMap<String, List<ResourceMeta>> resourceMetas;

    public VpcResourceMeta(String vni, HashMap<String, List<ResourceMeta>> resourceMetas) {
        this.vni = vni;
        this.resourceMetas = new HashMap<>(resourceMetas);
    }

    public String getVni() {
        return this.vni;
    }

    public HashMap<String, List<ResourceMeta>> getResourceMetas() {
        return this.resourceMetas;
    }
}
