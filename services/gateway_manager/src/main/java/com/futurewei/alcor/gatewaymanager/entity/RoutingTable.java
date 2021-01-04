package com.futurewei.alcor.gatewaymanager.entity;

import lombok.Data;

import java.util.List;

@Data
public class RoutingTable {
    private String id;
    private String owner; //gateway_id;
    private List<String> associations; //attachment_id
    private List<String> propagations; //attachment_id
    private String state; //available
    private List<Route> routes;
}
