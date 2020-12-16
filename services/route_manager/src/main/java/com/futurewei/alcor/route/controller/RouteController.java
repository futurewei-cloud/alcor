/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.route.controller;

import com.futurewei.alcor.common.config.JaegerTracerHelper;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.route.entity.RouteConstant;
import com.futurewei.alcor.route.service.RouteDatabaseService;
import com.futurewei.alcor.route.service.RouteWithSubnetMapperService;
import com.futurewei.alcor.route.service.RouteWithVpcMapperService;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.route.RoutesWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class RouteController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RouteDatabaseService routeDatabaseService;

    @Autowired
    private RouteWithVpcMapperService routeWithVpcMapperService;

    @Autowired
    private RouteWithSubnetMapperService routeWithSubnetMapperService;

    @RequestMapping(
            method = GET,
            value = {"routes/vpcs/{vpcId}/get"})
    @DurationStatistics
    public RoutesWebJson getRulesByVpcId(@PathVariable String vpcId) throws Exception {

        List<RouteEntity> routes = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);

            routes = this.routeWithVpcMapperService.getRuleByVpcId(vpcId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (routes == null) {
            //TODO: REST error code
            return new RoutesWebJson();
        }

        return new RoutesWebJson(routes);
    }

    @RequestMapping(
            method = GET,
            value = {"routes/subnets/{subnetId}/get"})
    @DurationStatistics
    public RoutesWebJson getRulesBySubnetId(@PathVariable String subnetId) throws Exception {

        List<RouteEntity> routes = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);

            routes = this.routeWithSubnetMapperService.getRuleBySubnetId(subnetId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (routes == null) {
            //TODO: REST error code
            return new RoutesWebJson(new ArrayList<>());
        }

        return new RoutesWebJson(routes);

    }

    @RequestMapping(
            method = GET,
            value = {"/routes/{routeId}"})
    @DurationStatistics
    public RouteWebJson getRuleByRouteId(@PathVariable String routeId) throws Exception {

        RouteEntity routeEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeEntity = this.routeDatabaseService.getByRouteId(routeId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (routeEntity == null) {
            //TODO: REST error code
            return new RouteWebJson();
        }

        return new RouteWebJson(routeEntity);

    }

    @RequestMapping(
            method = POST,
            value = {"/vpcs/{vpcId}/routes"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public RouteWebJson createVpcDefaultRoute(HttpServletRequest request, @PathVariable String vpcId, @RequestBody VpcWebJson resource) throws Exception {

        String serviceName="route";
        Map<String,String> headers=new HashMap();
        Iterator<String> stringIterator = request.getHeaderNames().asIterator();
        while(stringIterator.hasNext())
        {
            String name = stringIterator.next();
            String value=request.getHeader(name);
            headers.put(name,value);
        }
        Tracer tracer = new JaegerTracerHelper().initTracer(serviceName);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        Tracer.SpanBuilder builder = null;
        SpanContext parentSpanContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
        System.out.println("parentSpanContext "+parentSpanContext);
        if (null == parentSpanContext) {
            builder = tracer.buildSpan(serviceName);
        } else {
            builder = tracer.buildSpan("routeMgr").asChildOf(parentSpanContext);
        }
        Span span = builder.start();
        try (Scope op= tracer.scopeManager().activate(span)) {

        RouteEntity routeEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            VpcEntity vpcEntity = resource.getNetwork();
            RestPreconditionsUtil.verifyResourceNotNull(vpcEntity);

            String id = UUID.randomUUID().toString();
            String projectId = vpcEntity.getProjectId();
            String destination = vpcEntity.getCidr();
            String routeTableId = UUID.randomUUID().toString();

            routeEntity = new RouteEntity(projectId, id, "default_route_rule", "",
                    destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, RouteConstant.DEFAULT_ROUTE_TABLE_TYPE, routeTableId);

            this.routeDatabaseService.addRoute(routeEntity);

            this.routeWithVpcMapperService.addMapperByRouteEntity(vpcId, routeEntity);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteWebJson(routeEntity);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        finally
        {
            span.finish();
        }

        return null;
    }

    @RequestMapping(
            method = POST,
            value = {"/subnets/{subnetId}/routes"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public RouteWebJson createSubnetRoute(@PathVariable String subnetId, @RequestBody SubnetWebJson resource) throws Exception {
        RouteEntity routeEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);

            SubnetEntity inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceNotNull(inSubnetState);

            String id = UUID.randomUUID().toString();
            String projectId = inSubnetState.getProjectId();
            String destination = inSubnetState.getCidr();
            String routeTableId = UUID.randomUUID().toString();

            routeEntity = new RouteEntity(projectId, id, "default_route_rule", "",
                    destination, RouteConstant.DEFAULT_TARGET, RouteConstant.DEFAULT_PRIORITY, RouteConstant.DEFAULT_ROUTE_TABLE_TYPE, routeTableId);

            this.routeDatabaseService.addRoute(routeEntity);

            this.routeWithSubnetMapperService.addMapperByRouteEntity(subnetId, routeEntity);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteWebJson(routeEntity);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/vpcs/{vpcId}/routes/{routeId}"})
    @DurationStatistics
    public ResponseId deleteRule(@PathVariable String vpcId, @PathVariable String routeId) throws Exception {
        RouteEntity routeEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeEntity = this.routeDatabaseService.getByRouteId(routeId);
            if (routeEntity == null) {
                return new ResponseId();
            }

            this.routeDatabaseService.deleteRoute(routeId);

            this.routeWithVpcMapperService.deleteMapperByRouteId(vpcId, routeId);
        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
        logger.info("delete successfully —— id: " + routeId);
        return new ResponseId(routeId);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/subnets/{subnetId}/routes/{routeId}"})
    @DurationStatistics
    public ResponseId deleteRuleWithSubnetId(@PathVariable String subnetId, @PathVariable String routeId) throws Exception {
        RouteEntity routeEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routeId);

            routeEntity = this.routeDatabaseService.getByRouteId(routeId);
            if (routeEntity == null) {
                return new ResponseId();
            }

            this.routeDatabaseService.deleteRoute(routeId);

            this.routeWithSubnetMapperService.deleteMapperByRouteId(subnetId, routeId);
        } catch (ParameterNullOrEmptyException e) {
            logger.error(e.getMessage());
            throw new Exception(e);
        }
        logger.info("delete successfully —— id: " + routeId);
        return new ResponseId(routeId);
        
    }

}
