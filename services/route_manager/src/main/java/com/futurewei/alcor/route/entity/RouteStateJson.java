package com.futurewei.alcor.route.entity;

import lombok.Data;

@Data
public class RouteStateJson {

    private RouteState route;

    public RouteStateJson () {}

    public RouteStateJson (RouteState routeState) {
        this.route = routeState;
    }

    public RouteState getRoute() {
        return route;
    }

    public void setRoute(RouteState route) {
        this.route = route;
    }
}
