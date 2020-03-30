package com.futurewei.vpcmanager.entity;

import lombok.Data;

@Data
public class RouteWebJson {

    private RouteWebObject route;

    public RouteWebJson() {
    }

    public RouteWebJson(RouteWebObject route) {
        this.route = route;
    }

    public RouteWebObject getRoute() {
        return route;
    }

    public void setRoute(RouteWebObject route) {
        this.route = route;
    }
}
