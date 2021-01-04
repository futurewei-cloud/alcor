package com.futurewei.alcor.gatewaymanager.entity;

public enum RouteType {

    STATIC("static"),
    PROPAGATED("propagated");

    private final String routeType;

    RouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getRouteType() {
        return routeType;
    }
}
