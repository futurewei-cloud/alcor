package com.futurewei.route.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.route.dao.RouteRedisRepository;
import com.futurewei.route.entity.RouteState;
import com.futurewei.route.entity.RouteStateJson;
import com.futurewei.route.entity.*;
import com.futurewei.route.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RouteController {

    @Autowired
    private RouteRedisRepository routeRedisRepository;

    @RequestMapping(
            method = GET,
            value = {"/vpcs/{vpcId}/routes/{routeId}"})
    public RouteStateJson getRule (@PathVariable String vpcId, @PathVariable String routeId) throws Exception {

        RouteState routeState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeState = this.routeRedisRepository.findItem(routeId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (routeState == null) {
            //TODO: REST error code
            return new RouteStateJson();
        }

        return new RouteStateJson(routeState);
    }

    @RequestMapping(
            method = POST,
            value = {"/vpcs/{vpcId}/routes"})
    @ResponseStatus(HttpStatus.CREATED)
    public RouteStateJson createVpcDefaultRoute(@PathVariable String vpcId, @RequestBody VpcStateJson resource) throws Exception {
        RouteState routeState= null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);

            VpcState inVpcState = resource.getVpc();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);

            String id = UUID.randomUUID().toString();
            String projectId = inVpcState.getProjectId();
            String destination = inVpcState.getCidr();
            String routeTableId = UUID.randomUUID().toString();

            routeState = new RouteState(projectId, id, "default_route_rule", "",
                    destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, RouteConstant.DEFAULT_ROUTE_TABLE_TYPE, routeTableId);

            this.routeRedisRepository.addItem(routeState);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteStateJson(routeState);
    }

}
