package com.futurewei.route.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.route.dao.RouteRedisRepository;
import com.futurewei.route.entity.RouteState;
import com.futurewei.route.entity.RouteStateJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/route")
public class RouteController {

    @Autowired
    private RouteRedisRepository routeRedisRepository;

    @RequestMapping(
            method = GET,
            value = {"/rule/{vpcid}"})
    public RouteStateJson getRule (@PathVariable String vpcid) throws Exception {

        RouteState routeState = null;
        String rule = "You have connected to route manager!";

        try {
            routeState = new RouteState(rule);
        }catch (Exception e) {
            throw new Exception(e);
        }

        if (routeState == null) {
            return new RouteStateJson();
        }
        return new RouteStateJson(routeState);
    }


}
