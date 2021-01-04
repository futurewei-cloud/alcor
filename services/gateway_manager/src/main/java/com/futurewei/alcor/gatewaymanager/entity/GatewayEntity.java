package com.futurewei.alcor.gatewaymanager.entity;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GatewayEntity {
    private String id;
    private GatewayType type;
    private String name;
    private String description;
    private String state; // (available)
    private List<GatewayIP> ips;
    private List<String> attachments;
    private List<String> routetables;
    private List<String> tags;
    private String owner;
    private Map<String, String> options;
}
