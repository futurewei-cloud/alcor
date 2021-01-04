package com.futurewei.alcor.gatewaymanager.entity;

import lombok.Data;

@Data
public class Route {
    private String id;
    private String destination;
    private String target;
    private RouteType type;
    private String status; //(active | blackhole)
}
