package com.futurewei.alcor.web.entity.gateway;

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
