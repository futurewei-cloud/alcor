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
}
