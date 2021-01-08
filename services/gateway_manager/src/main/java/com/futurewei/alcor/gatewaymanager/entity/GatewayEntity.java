package com.futurewei.alcor.gatewaymanager.entity;


import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.gateway.GatewayIp;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class GatewayEntity extends CustomerResource {
    private GatewayType type;
    private String state; // (available)
    private List<GatewayIp> ips;
    private List<String> attachments;
    private List<String> routetables;
    private List<String> tags;
    private String owner;
    private Map<String, String> options;

    public GatewayEntity() {
        this.setId(UUID.randomUUID().toString());
        this.state = "available";
        this.setDescription("internal gateway");
    }
}
