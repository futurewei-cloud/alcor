package com.futurewei.alcor.web.entity.gateway;


import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GatewayEntity extends CustomerResource {
    private GatewayType type;
    private String status; // (available)
    private List<GatewayIp> ips;
    private List<String> attachments;
    private List<String> routetables;
    private List<String> tags;
    private String owner;
    private Map<String, String> options;

    public GatewayEntity() {
    }

    public GatewayEntity(String projectId, String id, String name, String description, GatewayType type, String status, List<GatewayIp> ips, List<String> attachments, List<String> routetables, List<String> tags, String owner, Map<String, String> options) {
        super(projectId, id, name, description);
        this.type = type;
        this.status = status;
        this.ips = ips;
        this.attachments = attachments;
        this.routetables = routetables;
        this.tags = tags;
        this.owner = owner;
        this.options = options;
    }
}
