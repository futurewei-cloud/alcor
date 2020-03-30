package com.futurewei.route.entity;

import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

@Data
public class RouteState extends CustomerResource {

    private String rule;

    public RouteState (String rule) {
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}
